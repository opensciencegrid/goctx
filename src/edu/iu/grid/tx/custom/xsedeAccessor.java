package edu.iu.grid.tx.custom;

import java.util.Date;

import org.apache.axis2.AxisFault;

import edu.iu.grid.tx.accessor.RTAccessor;
import edu.iu.grid.tx.ticket.RTTicket;
import edu.iu.grid.tx.ticket.Ticket;

public class xsedeAccessor extends RTAccessor {

	public xsedeAccessor(String baseuri, String user, String password, String default_queue, Boolean basic_auth) throws AxisFault {
		super(baseuri, user, password, default_queue, true);
		// TODO Auto-generated constructor stub
	}
	
	public void update(Ticket new_ticket, Date last_synctime, Ticket current_ticket) {		
		RTTicket rtnew_ticket = (RTTicket)new_ticket;
		
		//If current RT resource is "new", and we are trying to reset to "open", leave it "new".
		RTTicket rtcurrent_ticket = (RTTicket)current_ticket;
		if(rtcurrent_ticket.getStatus().equals("new") && rtnew_ticket.getStatus().equals("open")) {
			rtnew_ticket.setStatus("new");
		}
		
		super.update(rtnew_ticket, last_synctime, current_ticket);
	}
	
	public RTTicket get(String id) {
		RTTicket ticket = super.get(id);
		ticket.setOriginNote("XSEDE Ticket: Queue=" + ticket.getQueue() + " ID=" + ticket.getTicketID());
		return ticket;
	}

}
