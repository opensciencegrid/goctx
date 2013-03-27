package edu.iu.grid.tx.converter;

import org.apache.log4j.Logger;

import edu.iu.grid.tx.ticket.FPTicket;
import edu.iu.grid.tx.ticket.RTTicket;
import edu.iu.grid.tx.ticket.Ticket;
import edu.iu.grid.tx.ticket.RTTicket.PastHistoryEntry;

public class RT2FPTicketConverter implements TicketConverter {
	static Logger logger = Logger.getLogger(RT2FPTicketConverter.class);
	
	public Ticket convert(Ticket _input) {
		RTTicket rt = (RTTicket)_input;
		FPTicket fp = new FPTicket();
		
		fp.setTitle(rt.getSubject());
		
		//get all past history
		for(PastHistoryEntry rt_entry : rt.getPastHistory()) {
	 		FPTicket.DescriptionEntry fp_entry = fp.new DescriptionEntry();
	 		fp_entry.content = rt_entry.content;
	 		fp_entry.name = rt_entry.name;
	 		fp_entry.time = rt_entry.time;
			fp.addDescription(fp_entry);
		}
		
		//rt "text" is simply a first fp description
		FPTicket.DescriptionEntry first_entry = fp.new DescriptionEntry();
		first_entry.content = rt.getText();
		first_entry.name = rt.getRequestor();
		first_entry.time = rt.getCreated();
		fp.addDescription(first_entry);	
		
		fp.setSubmitter(rt.getCreator());
		fp.setPriority(convertPriority(rt.getPriority()));
		fp.setStatus(convertStatus(rt.getStatus()));
		fp.setUpdatetime(rt.getUpdatetime());
		
		fp.setOriginNote(rt.getOriginNote());
		
		fp.setMetadata("SUBMITTER_NAME", rt.getCreator());
		fp.setMetadata("SUBMITTED_VIA", "RT/" + rt.getQueue());
		
		return fp;
	}
	
	protected String convertPriority(int rtpriority) {
		if(rtpriority == 100) return "1"; //"Critical";
		if(rtpriority >= 75) return "2"; //"High";
		if(rtpriority >= 50) return "3"; //"Elevated";
		return "4";//Normal
	}

	protected String convertStatus(String rt_status) {
		if(rt_status.equals("resolved")) return "Resolved";
		if(rt_status.equals("rejected")) return "Resolved";
		if(rt_status.equals("stalled")) return "Resolved";
		if(rt_status.equals("deleted")) return "Resolved";
		return "Engineering";
	}
	/*
	protected String convertTicketType(String rt_type) {
		if(rt_type.equals("Security")) return "Security";
		return "Problem/Request";
	}
	*/
}

