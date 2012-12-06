package edu.iu.grid.tx.converter;

import java.rmi.RemoteException;

import org.apache.log4j.Logger;

import edu.iu.grid.tx.soap.ggus.GGUSServiceStub.GHD_PriorityType;
import edu.iu.grid.tx.ticket.FPTicket;
import edu.iu.grid.tx.ticket.GGUSTicket;
import edu.iu.grid.tx.ticket.Ticket;
import edu.iu.grid.tx.ticket.GGUSTicket.PastHistoryEntry;

public class GGUS2FPTicketConverter implements TicketConverter {
	static Logger logger = Logger.getLogger(GGUS2FPTicketConverter.class);
	
	public FPTicket convert(Ticket _input) {
		GGUSTicket ggus = (GGUSTicket)_input;
		FPTicket fp = new FPTicket();
		fp.setTitle(ggus.getShortDescription());
			
		//get all past history
		try {
			for(PastHistoryEntry ggus_entry : ggus.getPastHistory()) {
				FPTicket.DescriptionEntry fp_entry = fp.new DescriptionEntry();
				fp_entry.content = ggus_entry.content;
				fp_entry.name = ggus_entry.name;
				fp_entry.time = ggus_entry.time;
				fp.addDescription(fp_entry);
			}
		} catch (RemoteException e) {
			logger.error(e);
		}
		
		//ggus description is simply a first fp description
		FPTicket.DescriptionEntry first_entry = fp.new DescriptionEntry();
		first_entry.content = ggus.getDescription();
		first_entry.name = ggus.getSubmitterName();
		first_entry.time = ggus.getDateOfCreation().getTime();
		fp.addDescription(first_entry);	
		
		fp.setSubmitter(convertSubmitter(ggus.getSubmitterName()));
		fp.setPriority(convertPriority(ggus.getPriority()));
		fp.setStatus(convertStatus(ggus.getStatus()));
		fp.setPhone(ggus.getPhone());
		
		fp.setOriginNote(ggus.getOriginNote());
		fp.setMetadata("GGUS_TICKET_ID", ggus.getTicketID());
		if(ggus.getTicketType() != null) {
			fp.setMetadata("GGUS_TICKET_TYPE", ggus.getTicketType().getValue());
		}
		if(ggus.getTypeOfProblem() != null) {
			fp.setMetadata("GGUS_PROBLEM_TYPE", ggus.getTypeOfProblem());
		}
		
		fp.setMetadata("SUBMITTER_NAME", ggus.getSubmitterName());
		fp.setMetadata("SUBMITTED_VIA", "GGUS");
		
		if(ggus.getSubmitterDN() != null) {
			fp.setMetadata("SUBMITTER_DN", ggus.getSubmitterDN());
		}
		
		fp.setUpdatetime(ggus.getUpdatetime());
		return fp;
	}
	
	protected String convertPriority(GHD_PriorityType priority) {
		if(priority == null) { 
			logger.warn("null GGUS ticket priority passed - defaulting to Normal");
			return "Normal";
		}
		String value = priority.getValue();
		if(value.equals("top priority")) return "1"; //"Critical";
		if(value.equals("very urgent")) return "2"; //"High";
		if(value.equals("urgent")) return "3"; //"Elevated";
		if(value.equals("less urgent")) return "4"; //"Normal";
		
		logger.warn("Unknown ggus priority: " + value + " (defaulting to Normal)");
		return "4";
	}

	protected String convertStatus(String ggus_status) {
		if(ggus_status == null) { 
			logger.warn("null GGUS ticket status passed - defaulting to Engineering");
			return "Engineering";
		}
		if(ggus_status.equals("new")) return "Engineering";
		if(ggus_status.equals("assigned")) return "Engineering";
		if(ggus_status.equals("in progress")) return "Engineering";
		if(ggus_status.equals("waiting for reply")) return "Engineering";
		if(ggus_status.equals("on hold")) return "Engineering";
		if(ggus_status.equals("reopened")) return "Engineering";
		if(ggus_status.equals("solved")) return "Closed";
		if(ggus_status.equals("unsolved")) return "Closed";
		if(ggus_status.equals("verified")) return "Closed";
		if(ggus_status.equals("closed")) return "Closed";
		
		logger.warn("Unknown ggus status: " + ggus_status + " (defaulting to Engineering)");
		return "Engineering";
	}
	
	//GGUS name looks like "Soichi Hayashi 461343".. let's limit to the first 2 tokens
	protected String convertSubmitter(String name) {
	
		if(name == null) { 
			//this should never happen, but did happen on 3/10/2010 
			//https://gus.fzk.de/ws/ticket_info.php?ticket=56359
			logger.warn("null GGUS submitter passed - defaulting to 'Empty Submitter'");
			return "Empty Submitter"; 
		}
		
		String tokens[] = name.split(" ");
		if(tokens.length > 2) {
			name = tokens[0] + " " + tokens[1];
		}
		return name;
	}
}

