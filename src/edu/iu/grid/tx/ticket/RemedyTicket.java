package edu.iu.grid.tx.ticket;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class RemedyTicket implements Ticket {
	
	private String id;
	public void setTicketID(String _id) { id = _id; }
	public String getTicketID() { return id; }
	
	private String queue;
	public void setQueue(String q) { queue = q; }
	public String getQueue() { return queue; }
	
	private String owner;
	public void setOwner(String it) { owner = it; }
	public String getOwner() { return owner; }
	
	private String creator;
	public void setCreator(String it) { creator = it; }
	public String getCreator() { return creator; }
	
	private String subject;
	public void setSubject(String it) { subject = it; }
	public String getSubject() { return subject; }	
	
	private String status;
	public void setStatus(String it) { status = it; }
	public String getStatus() { return status; }
	
	private Integer priority;
	public void setPriority(Integer it) { priority = it; }
	public Integer getPriority() { return priority; }

	private String requestor;
	public void setRequestor(String it) { requestor = it; }
	public String getRequestor() { return requestor; }	
	
	private String cc;
	public void setCC(String it) { cc = it; }
	public String getCC() { return cc; }	
	
	private Date created;
	public void setCreated(Date time) { created = time; }
	public Date getCreated() { return created; }
	
	private Date last_updated;
	public void setLastUpdated(Date time) { last_updated = time; }
	public Date getLastUpdated() { return last_updated; }
	
	private String text;
	public void setText(String it) { text = it; }
	public String getText() { return text; }	
	
	private HashMap<String, String> custom_fields = new HashMap<String, String>();
	public void setCustomField(String key, String value) {
		custom_fields.put(key, value);
	}
	public HashMap<String, String> getCustomFields() {
		return custom_fields;
	}
	
	//base function
	public Date getUpdatetime() {
		return getLastUpdated();
	}
	
	public ArrayList<PastHistoryEntry> pasthistories = new ArrayList<PastHistoryEntry>();
	public class PastHistoryEntry {
		public String content;
		public String name;
		public Date time;
	}

	public void addPastHistory(PastHistoryEntry entry) { 
		pasthistories.add(entry); 
	}
	
	public ArrayList<PastHistoryEntry> getPastHistory() {
		return pasthistories;
	}
	
	public String getPastHistoryAfter(Date after) 
	{ 
		String updates = "";
		for(PastHistoryEntry entry : pasthistories) {
			if(after == null || entry.time.after(after)) {
				updates = entry.content + "\n" + " -- by " + entry.name + " at " + entry.time.toString() + "\n\n" + updates;
			}
		}
		return updates;
	}
	
	private String origin_note;
	public String getOriginNote() { return origin_note; }
	public void setOriginNote(String origin_note) { this.origin_note = origin_note; }
}
