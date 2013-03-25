package edu.iu.grid.tx.converter;

import java.math.BigInteger;
import java.rmi.RemoteException;

import org.apache.log4j.Logger;

import edu.iu.grid.tx.ticket.FPTicket;
import edu.iu.grid.tx.ticket.ServiceNowTicket;
import edu.iu.grid.tx.ticket.Ticket;

public class FP2ServiceNowTicketConverter implements TicketConverter {
	static Logger logger = Logger.getLogger(FP2ServiceNowTicketConverter.class);

	public Ticket convert(Ticket _input) {
		FPTicket fp = (FPTicket)_input;
		ServiceNowTicket servicenow = new ServiceNowTicket(null);
		
		servicenow.setShortDescription(fp.getTitle());
		servicenow.setDescription(fp.getFirstDescription().content);
		servicenow.setUpdatetime(fp.getUpdatetime());
		servicenow.setIncidentState(convertStatus(fp.getStatus()));
		servicenow.setUrgency(convertPriority(fp.getPriority()));
		servicenow.setShortDescription(fp.getTitle());
		servicenow.setCreatedBy(fp.getSubmitterFirstname() + " " + fp.getSubmitterLastname());
		servicenow.setCreatedOn(fp.getFirstDescription().time);
		servicenow.setOriginNote(fp.getOriginNote());
		servicenow.setExternalTicketNumber("GOC"+fp.getProjectID() + " #" + fp.getTicketID());
		
		//non convertible fields (accessor uses default(for create) / or current value(for update) for these fields)
		servicenow.setCategory(null);
		servicenow.setSubcategory(null);
		servicenow.setCustomerCategorization(null);
		servicenow.setItem(null);
		servicenow.setOperationalCategory(null);
		servicenow.setReportedSource(null);
		servicenow.setService(null);
		servicenow.setImpact(null);

		//copy all past history
		for(FPTicket.DescriptionEntry fpdesc : fp.getDescriptions()) {
			//ignore the first - this was set as ticket description
			if(fpdesc == fp.getFirstDescription()) continue;
			
			//everything else is stored as "history"
			ServiceNowTicket.Comment comment = servicenow.new Comment();
			comment.content = fpdesc.content;
			comment.name = fpdesc.name;
			comment.time = fpdesc.time;
			try {
				servicenow.addComment(comment);
			} catch (RemoteException e) {
				logger.error(e);
			}
		}
		
		return servicenow;
	}
	
	protected BigInteger convertPriority(String priority) {
		if(priority.equals("1")) return new BigInteger("1"); //"Critical";
		if(priority.equals("2")) return new BigInteger("2"); //"High";
		if(priority.equals("3")) return new BigInteger("3"); //"Elevated";
		if(priority.equals("4")) return new BigInteger("4"); //"Normal";
		logger.warn("Unknown fp priority: " + priority + " (defaulting to 4:Normal)");
		return new BigInteger("4");
	}

	protected BigInteger convertStatus(String status) {
		if(status != null) {
			if(status.equals("Engineering")) return new BigInteger("2");//work in progress
			if(status.equals("Customer")) return new BigInteger("2");//work in progress
			if(status.equals("Network__bAdministration")) return new BigInteger("2");//work in progress
			if(status.equals("Support__bAgency")) return new BigInteger("2");//work in progress
			if(status.equals("Vendor")) return new BigInteger("2");//work in progress
			if(status.equals("Resolved")) return new BigInteger("7");//resolved? 
			if(status.equals("Closed")) return new BigInteger("6");//6 is my guess			
			//8 -- canceled
			//9 - no such state
		}
		
		logger.warn("Unknown fp state: " + status + " (defaulting to Work in Progress)");
		return new BigInteger("2");
	}
}

