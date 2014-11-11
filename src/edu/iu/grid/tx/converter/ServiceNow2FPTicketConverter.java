package edu.iu.grid.tx.converter;

import java.math.BigInteger;
import java.rmi.RemoteException;
import org.apache.log4j.Logger;
import edu.iu.grid.tx.ticket.FPTicket;
import edu.iu.grid.tx.ticket.ServiceNowTicket;
import edu.iu.grid.tx.ticket.Ticket;
import edu.iu.grid.tx.ticket.ServiceNowTicket.Comment;

public class ServiceNow2FPTicketConverter implements TicketConverter {
	static Logger logger = Logger.getLogger(ServiceNow2FPTicketConverter.class);
	
	public FPTicket convert(Ticket _input) {
		ServiceNowTicket servicenow = (ServiceNowTicket)_input;
		FPTicket fp = new FPTicket();
		fp.setTitle(servicenow.getShortDescription());
			
		//get all past history
		try {
			for(Comment entry : servicenow.getComments()) {
				FPTicket.DescriptionEntry fp_entry = fp.new DescriptionEntry();
				fp_entry.content = entry.content;
				fp_entry.name = entry.name;
				fp_entry.time = entry.time;
				fp.addDescription(fp_entry);
			}
		} catch (RemoteException e) {
			logger.error(e);
		}
		
		//servicwnow description is simply a first fp description
		FPTicket.DescriptionEntry first_entry = fp.new DescriptionEntry();
		first_entry.content = servicenow.getDescription();
		first_entry.name = servicenow.getCreatedBy();
		first_entry.time = servicenow.getCreatedOn();
		fp.addDescription(first_entry);	
		
		//servicenow close info should be added as the last comment
		ServiceNowTicket.CloseInfo close_info = servicenow.getCloseInfo();
		if(close_info != null) {
			FPTicket.DescriptionEntry close_entry = fp.new DescriptionEntry();
			close_entry.content = "["+close_info.code + "]\n" + close_info.note;
			close_entry.name = close_info.by; //empty until ticket gets closed (per Mike B.)... but it's ok?
			
			//TODO - close_info.at will be set when ticket is closed. if it's set, use it instead of getUpdatetime();
			//close_entry.time = close_info.at; 
			close_entry.time = servicenow.getUpdatetime(); 
			
			fp.addDescription(close_entry);
		}
		
		fp.setSubmitter(servicenow.getCreatedBy());
		fp.setPriority(convertUrgency2Priority(servicenow.getUrgency()));
		fp.setStatus(convertState(servicenow.getIncidentState()));

		//service now doesn't provide following fields
		/*
		fp.setPhone(null);
		fp.setEmail(null);
		*/
		fp.setTicketType("");
		
		fp.setOriginNote(servicenow.getOriginNote());
		fp.setMetadata("SERVICENOW_TICKET_ID", servicenow.getTicketID());	
		fp.setMetadata("SUBMITTER_NAME", servicenow.getCreatedBy());
		fp.setMetadata("SUBMITTED_VIA", "SERVICENOW");		
		fp.setUpdatetime(servicenow.getUpdatetime());
		return fp;
	}
	
	protected String convertUrgency2Priority(BigInteger urgency) {
		if(urgency.equals(new BigInteger("1"))) return "1"; //"Critical";
		if(urgency.equals(new BigInteger("2"))) return "2"; //"High";
		if(urgency.equals(new BigInteger("3"))) return "3"; //"Elevated";
		if(urgency.equals(new BigInteger("4"))) return "4"; //"Normal";
		logger.warn("Unknown servicenow urgency: " + urgency + " (defaulting to Normal)");
		return "4";
	}

	protected String convertState(BigInteger state) {
		if(state.equals(new BigInteger("9999"))) return "Engineering"; //New (?)
		if(state.equals(new BigInteger("1"))) return "Engineering"; //Assigned
		if(state.equals(new BigInteger("2"))) return "Engineering"; //Work in Progress
		if(state.equals(new BigInteger("4"))) return "Engineering"; //Pending
		if(state.equals(new BigInteger("7"))) return "Resolved"; //Resolved
		if(state.equals(new BigInteger("8"))) return "Resolved"; //Canceled
		if(state.equals(new BigInteger("6"))) return "Resolved"; //Closed //6 is my guess
		
		logger.warn("Unknown servicenow state: " + state + " (defaulting to Engineering)");
		return "Engineering";
	}
}

