package edu.iu.grid.tx.converter;

import java.rmi.RemoteException;

import org.apache.log4j.Logger;

import edu.iu.grid.tx.soap.ggus.GGUSServiceStub.GHD_PriorityType;
import edu.iu.grid.tx.ticket.FPTicket;
import edu.iu.grid.tx.ticket.GGUSTicket;
import edu.iu.grid.tx.ticket.Ticket;

public class FP2GGUSTicketConverter implements TicketConverter {
	static Logger logger = Logger.getLogger(FP2GGUSTicketConverter.class);

	public Ticket convert(Ticket _input) {
		FPTicket fp = (FPTicket)_input;
		GGUSTicket ggus = new GGUSTicket(null);
		
		ggus.setUpdatetime(fp.getUpdatetime());
		String last_modifier = fp.getLastModifier();
		ggus.setLastModifier(last_modifier + " (via Footprints)");
		ggus.setStatus(convertStatus(fp.getStatus()));
		ggus.setPriority(convertPriority(fp.getPriority()));
		ggus.setShortDescription(fp.getTitle());
		ggus.setSubmitterName(fp.getSubmitterFirstname() + " " + fp.getSubmitterLastname());
		ggus.setDescription(fp.getFirstDescription().content);
		ggus.setPhone(fp.getPhone());
		//ggus.setTypeOfProblem(convertType(fp.getTicketType()));
		ggus.setSubmitterDN(fp.getMetadata("SUBMITTER_DN"));
		
		//don't copy email address - since it will create duplicate notifications.
		//ggus.setEmail(fp.getEmail());
		
		ggus.setOriginID(fp.getTicketID());
		ggus.setOriginNote(fp.getOriginNote());
		
		//copy all past history
		for(FPTicket.DescriptionEntry fpdesc : fp.getDescriptions()) {
			//ignore the first - this is set as ggus ticket description
			if(fpdesc == fp.getFirstDescription()) continue;
			//ignore the last - this will be entered as ticket diary
			if(fpdesc == fp.getLatestDescription()) continue;
			
			//everything else is stored as "history"
			GGUSTicket.PastHistoryEntry entry = ggus.new PastHistoryEntry();
			entry.content = fpdesc.content;
			entry.name = fpdesc.name;
			entry.time = fpdesc.time;
			try {
				ggus.addPastHistory(entry);
			} catch (RemoteException e) {
				logger.error(e);
			}
		}
		
		String lastupdate = fp.getLatestDescription().content.trim();
		
		//if there is only 1 description (first == last), then we've already 
		//entered the "last" description as the ggus ticket descriptipn
		if(fp.getLatestDescription() == fp.getFirstDescription()) {
			lastupdate = "";
		}
		
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
		
		return ggus;
	}
	private String convertStatus(String fpstatus) {		
		if(fpstatus != null) {
			if(fpstatus.equals("Engineering")) return "in progress";
			if(fpstatus.equals("Customer")) return "in progress";
			if(fpstatus.equals("Network__bAdministration")) return "in progress";
			if(fpstatus.equals("Support__bAgency")) return "in progress";
			if(fpstatus.equals("Vendor")) return "in progress";	
			if(fpstatus.equals("Resolved")) return "solved";
			if(fpstatus.equals("Closed")) return "solved";			
		}
		
		logger.warn("Unknown FP status to convert to GGUS status: " + fpstatus + " - converting to assigned.");
		return "assigned";
	}
	
	protected GHD_PriorityType convertPriority(String fppriority) {
		if(fppriority != null) {
			//TODO - what do I do if WSDL changes the order??
			if(fppriority.equals("1")) return GHD_PriorityType.value4;//top priority
			if(fppriority.equals("2")) return GHD_PriorityType.value3;//very urgent
			if(fppriority.equals("3")) return GHD_PriorityType.value2;//urgent
			if(fppriority.equals("4")) return GHD_PriorityType.value1;//less urgent
		}
		
		logger.warn("Unknown FP priority to convert to GGUS proority: " + fppriority + " - converting to normal");
		return GHD_PriorityType.value5;//normal
	}
	
	/*
	protected String convertType(String fptype) {
		if(fptype != null) {
			//TODO - what do I do if WSDL changes the order??
			if(fptype.equals("Security")) return "Security";
			if(fptype.equals("Problem/Request")) return "Other";
		}
		
		logger.warn("Unknown FP type to convert to GGUS type: " + fptype + " - converting to other");
		return "Other";
	}
	*/
}

