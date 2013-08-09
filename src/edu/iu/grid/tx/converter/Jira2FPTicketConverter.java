package edu.iu.grid.tx.converter;

import org.apache.log4j.Logger;

import edu.iu.grid.tx.ticket.FPTicket;
import edu.iu.grid.tx.ticket.JiraTicket;
import edu.iu.grid.tx.ticket.JiraTicket.Comment;
import edu.iu.grid.tx.ticket.Ticket;

public class Jira2FPTicketConverter implements TicketConverter {
	static Logger logger = Logger.getLogger(Jira2FPTicketConverter.class);
	
	public FPTicket convert(Ticket _input) {
		JiraTicket jira = (JiraTicket)_input;
		FPTicket fp = new FPTicket();
		fp.setTitle(jira.getSummary());
			
		//get all past history
		for(Comment jira_comment : jira.comments) {
			FPTicket.DescriptionEntry fp_entry = fp.new DescriptionEntry();
			fp_entry.content = jira_comment.body;
			fp_entry.name = jira_comment.author_name;
			fp_entry.time = jira_comment.time;
			fp.addDescription(fp_entry);
		}
		
		//copy jira description as first fp history
		FPTicket.DescriptionEntry first_entry = fp.new DescriptionEntry();
		first_entry.content = jira.getDescription();
		first_entry.name = jira.getReporterID();
		first_entry.time = jira.getCreated();
		fp.addDescription(first_entry);	
		
		fp.setSubmitter(jira.getReporterID());
		fp.setPriority(convertPriority(jira.getPriority()));
		fp.setStatus(convertStatus(jira.getStatus()));
		
		fp.setOriginNote(jira.getOriginNote());
		fp.setTicketType(convertTicketType(jira.getIssueType()));
		
		//TODO - should I store metadata?
		//fp.setMetadata("JIRA_TICKET_ID", jira.getTicketID());
		//fp.setMetadata("JIRA_ISSUE_TYPE", jira.getIssueType());
	
		fp.setUpdatetime(jira.getUpdatetime());
		return fp;
	}
	
	protected String convertPriority(String priority) {
		if(priority == null) { 
			logger.warn("null Jira ticket priority passed - defaulting to Normal");
			return "Normal";
		}
		if(priority.equals("Blocker")) return "1"; //"Critical";
		if(priority.equals("Critical")) return "2"; //"High";
		if(priority.equals("Major")) return "3"; //"Elevated";
		if(priority.equals("Minor")) return "4"; //"Normal";
		
		logger.warn("Unknown jira priority: " + priority + " (defaulting to Major)");
		return "3";
	}

	protected String convertStatus(String jira_status) {
		if(jira_status == null) { 
			logger.warn("null Jira ticket status passed - defaulting to Engineering");
			return "Engineering";
		}
		if(jira_status.equals("Closed")) return "Closed";
		if(jira_status.equals("Resolved")) return "Resolved";
		if(jira_status.equals("Open")) return "Engineering";
		
		logger.warn("Unknown jira status: " + jira_status + " (defaulting to Open)");
		return "Open";
	}
	
	private String convertTicketType(String jira_issue_type) {
		return "Problem/Request";
	}
}

