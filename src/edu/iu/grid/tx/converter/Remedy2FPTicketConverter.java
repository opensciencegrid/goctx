package edu.iu.grid.tx.converter;

import org.apache.log4j.Logger;

import edu.iu.grid.tx.ticket.FPTicket;
import edu.iu.grid.tx.ticket.RemedyTicket;
import edu.iu.grid.tx.ticket.Ticket;

public class Remedy2FPTicketConverter implements TicketConverter {
	static Logger logger = Logger.getLogger(Remedy2FPTicketConverter.class);
	
	public Ticket convert(Ticket _input) {
		RemedyTicket remedy = (RemedyTicket)_input;
		FPTicket fp = new FPTicket();

		convertCustom(remedy, fp);
		return fp;
	}
	
	protected void convertCustom(RemedyTicket remedy, FPTicket fp) {
		//override me
	}
}

