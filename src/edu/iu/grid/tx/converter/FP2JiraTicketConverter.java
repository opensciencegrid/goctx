package edu.iu.grid.tx.converter;

import org.apache.log4j.Logger;

import edu.iu.grid.tx.ticket.FPTicket;
import edu.iu.grid.tx.ticket.JiraTicket;
import edu.iu.grid.tx.ticket.Ticket;

public class FP2JiraTicketConverter implements TicketConverter {
	static Logger logger = Logger.getLogger(FP2JiraTicketConverter.class);

	public Ticket convert(Ticket _input) {
		FPTicket fp = (FPTicket)_input;
		JiraTicket jira = new JiraTicket();
		
		jira.setUpdatetime(fp.getUpdatetime());
		jira.setSummary(fp.getTitle());
		jira.setDescription(fp.getFirstDescription().content);
		jira.setCreated(fp.getFirstDescription().time);
		jira.setStatus(convertStatus(fp.getStatus()));
		jira.setPriority(convertPriority(fp.getPriority()));
		jira.setIssueType(convertTicketType(fp.getTicketType()));
		
		jira.setOriginNote(fp.getOriginNote());
		
		//copy all past history
		for(FPTicket.DescriptionEntry fpdesc : fp.getDescriptions()) {
			//ignore the first - this is set as ggus ticket description
			if(fpdesc == fp.getFirstDescription()) continue;
			
			//everything else is stored as "history"
			JiraTicket.Comment entry = jira.new Comment();
			entry.body = fpdesc.content;
			entry.author_name = fpdesc.name;
			entry.author_email = null;
			entry.time = fpdesc.time;
			jira.comments.add(entry);
		}
		
		String lastupdate = fp.getLatestDescription().content.trim();
		
		//if there is only 1 description (first == last), then we've already 
		//entered the "last" description as the ggus ticket descriptipn
		if(fp.getLatestDescription() == fp.getFirstDescription()) {
			lastupdate = "";
		}
		
		/*
		if(fp.getStatus().equals("Resolved") || fp.getStatus().equals("Closed")) {
			//if status is set to solved, then we *must* have some solution content for GGUS
			if(lastupdate.length() == 0) {
				lastupdate = "(No description given)";
			}
			ggus.setDetailedSolution(lastupdate); //not used?
			ggus.setDiary(null);
		} else {
			ggus.setDetailedSolution(null);
			ggus.setDiary(lastupdate);
		}
		*/
		
		return jira;
	}
	private String convertStatus(String fpstatus) {		
		if(fpstatus != null) {
			if(fpstatus.equals("Engineering")) return "Open";
			if(fpstatus.equals("Customer")) return "Open";
			if(fpstatus.equals("Network__bAdministration")) return "Open";
			if(fpstatus.equals("Support__bAgency")) return "Open";
			if(fpstatus.equals("Vendor")) return "Open";	
			if(fpstatus.equals("Resolved")) return "Resolved";
			if(fpstatus.equals("Closed")) return "Closed";			
		}
		
		logger.warn("Unknown FP status to convert to Jira status: " + fpstatus + " - converting to 'Open'.");
		return "Open";
	}
	
	private String convertPriority(String fppriority) {
		if(fppriority != null) {
			//TODO - what do I do if WSDL changes the order??
			if(fppriority.equals("1")) return "Blocker";
			if(fppriority.equals("2")) return "Critical";
			if(fppriority.equals("3")) return "Major";
			if(fppriority.equals("4")) return "Minor";
		}
		
		logger.warn("Unknown FP priority to convert to Jira proority: " + fppriority + " - converting to Major");
		return "Major";
	}
	
	private String convertTicketType(String fptype) {
		return "Task"; //override
	}
}

