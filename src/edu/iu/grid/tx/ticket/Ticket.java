package edu.iu.grid.tx.ticket;

import java.util.Date;

//try sticking to become a simple setter / getter for various fields..
//any kind of logic handling should be left for converter. 
public interface Ticket {
	public String getTicketID();
	public void setTicketID(String id);
	public Date getUpdatetime();
	
	public void setOriginNote(String origin_note);
	public String getOriginNote();
	public void mergeMeta(Ticket source_ticket);
}
