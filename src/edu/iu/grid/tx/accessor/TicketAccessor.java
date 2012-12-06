package edu.iu.grid.tx.accessor;

import java.io.File;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import javax.xml.soap.SOAPException;

import edu.iu.grid.tx.SyncModel;
import edu.iu.grid.tx.ticket.Ticket;

public interface TicketAccessor 
{
	//create new ticket (Add reverse_email to some kind of CC list)
	public String create(Ticket ticket, String reverse_assignee) throws Exception;

	//update the ticket - insert all description *after) last_synctime
	public void update(Ticket new_ticket, Date last_synctime, Ticket current_ticket) throws Exception;
	/*
	//returns the timestamp of the last ticket update 
	public Timestamp getUpdatetime(String ticket_id) throws Exception;
	*/
	
	//get ticket
	public Ticket get(String ticket_id) throws Exception;
	
	public String parseTicketID(String something);
	
	public ArrayList<Attachment> getAttachments(String ticket_id) throws Exception;
	public File downloadAttachment(String ticket_id, Attachment attachment) throws Exception;
	public String uploadAttachment(String ticket_id, Attachment attachment) throws Exception;

	public boolean isTicketExchanged(Ticket ticket, String reverse_assignee);
	
}
