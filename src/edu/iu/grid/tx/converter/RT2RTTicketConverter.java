package edu.iu.grid.tx.converter;

import org.apache.log4j.Logger;

import edu.iu.grid.tx.ticket.RTTicket;
import edu.iu.grid.tx.ticket.Ticket;

public class RT2RTTicketConverter implements TicketConverter {
	static Logger logger = Logger.getLogger(RT2RTTicketConverter.class);

	public Ticket convert(Ticket _input) {

        logger.debug("mikedebug200: start convert RT to RT.");

		RTTicket rtin = (RTTicket)_input;
		RTTicket rt = new RTTicket();
		
		rt.setLastUpdated(rtin.getLastUpdated());
		rt.setCreated(rtin.getCreated());
		rt.setRequestor(rtin.getRequestor());
		rt.setLastUpdated(rtin.getLastUpdated());
		rt.setSubject(rtin.getSubject());
		rt.setCreator(rtin.getCreator());
		rt.setText(rtin.getText());
		rt.setOriginNote(rtin.getOriginNote());
		
        //get all past history
        for(RTTicket.PastHistoryEntry rtin_entry : rtin.getPastHistory()) {
            RTTicket.PastHistoryEntry entry = rt.new PastHistoryEntry();
            entry.content = rtin_entry.content;
            entry.name = rtin_entry.name;
            entry.time = rtin_entry.time;
            rt.addPastHistory(entry);
        }  

		convertCustom(rtin, rt);
		
        logger.debug("mikedebug290: end convert RT to RT.");
		return rt;
	}
	
	protected void convertCustom(RTTicket source, RTTicket dest) {
		//override me
	}
	
}

