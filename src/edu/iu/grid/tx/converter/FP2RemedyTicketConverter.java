package edu.iu.grid.tx.converter;

import org.apache.log4j.Logger;

import edu.iu.grid.tx.ticket.FPTicket;
import edu.iu.grid.tx.ticket.RTTicket;
import edu.iu.grid.tx.ticket.Ticket;

public class FP2RemedyTicketConverter implements TicketConverter {
	static Logger logger = Logger.getLogger(FP2RemedyTicketConverter.class);

	public Ticket convert(Ticket _input) {
		FPTicket fp = (FPTicket)_input;
		RTTicket rt = new RTTicket();
		
		rt.setLastUpdated(fp.getUpdatetime());
		rt.setCreated(fp.getFirstDescription().time);//pulling the description time is good enough?
		rt.setRequestor(fp.getEmail());
		rt.setLastUpdated(fp.getLatestDescription().time);//pulling the last description may not be good enough..
		//String last_modifier = fp.getLastModifier();
		//rt.setLastModifier(last_modifier + " (via Footprints)");
		rt.setStatus(convertStatus(fp.getStatus()));
		rt.setPriority(convertPriority(fp.getPriority()));
		rt.setSubject(fp.getTitle());
		rt.setCreator(fp.getSubmitterFirstname() + " " + fp.getSubmitterLastname());
		rt.setText(fp.getFirstDescription().content);
		rt.setOriginNote(fp.getOriginNote());
		
		//copy all past history
		for(FPTicket.DescriptionEntry fpdesc : fp.getDescriptions()) {
			//ignore the first description (that's set in ticket text)
			if(fpdesc == fp.getFirstDescription()) continue;
			
			//if(fpdesc == fp.getFirstDescription()) continue;
			
			RTTicket.PastHistoryEntry entry = rt.new PastHistoryEntry();
			entry.content = fpdesc.content;
			entry.name = fpdesc.name;
			entry.time = fpdesc.time;
			rt.addPastHistory(entry);
		}
		
		convertCustom(fp, rt);
		
		return rt;
	}
	
	protected void convertCustom(FPTicket source, RTTicket dest) {
		//override me
	}
	
	private String convertStatus(String fpstatus) {		
		if(fpstatus.equals("Engineering")) return "open";
		if(fpstatus.equals("Customer")) return "open";
		if(fpstatus.equals("Network__bAdministration")) return "open";
		if(fpstatus.equals("Support__bAgency")) return "open";
		if(fpstatus.equals("Vendor")) return "open";
		
		if(fpstatus.equals("Resolved")) return "resolved";
		if(fpstatus.equals("Closed")) return "resolved";
		
		//unknown FP status... should I throw?
		return "open";
	}
	
	protected Integer convertPriority(String fppriority) {
		//TODO - what do I do if WSDL changes the order??
		if(fppriority.equals("1")) return 100;//top priority
		if(fppriority.equals("2")) return 75;//very urgent
		if(fppriority.equals("3")) return 50;//urgent
		if(fppriority.equals("4")) return 25;//less urgent
		
		//all else...
		return 0;
	}
}

