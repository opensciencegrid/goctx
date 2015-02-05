package edu.iu.grid.tx.ticket;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class FPTicket implements Ticket 
{
	private String id;
	public void setTicketID(String _id) { id = _id; }
	public String getTicketID() { return id; }
	
	private String projectid;
	public void setProjectID(String _it) { projectid = _it; }
	public String getProjectID() { return projectid; }

	private String submitter_firstname;
	private String submitter_lastname;
	public void setSubmitter(String _name) { 
		int pos = _name.indexOf(" ");
		if(pos == -1) {
			submitter_firstname = _name;
			submitter_lastname = "";
		} else {
			submitter_firstname = _name.substring(0, pos);
			submitter_lastname = _name.substring(pos+1, _name.length());
		}
	}
	
	public void setSubmitterFirstname(String _name) { submitter_firstname = _name; }
	public void setSubmitterLastname(String _name) { submitter_lastname = _name; }
	public String getSubmitterFirstname() { return submitter_firstname; }
	public String getSubmitterLastname() { return submitter_lastname; }
	
	private String ticket_type;
	public void setTicketType(String _type_of_problem) { ticket_type = _type_of_problem; }
	public String getTicketType() { return ticket_type; }
	
	private String priority;
	public void setPriority(String _priority) { priority = _priority; }
	public String getPriority() { return priority; }
	
	private String status;
	public void setStatus(String _status) { status = _status; }
	public String getStatus() { return status; }
	
	private String title;
	public void setTitle(String _d) { title = _d; }
	public String getTitle() { return title; }
	
	private ArrayList<String> assignees = new ArrayList<String>();
	public void addAssignee(String a) { assignees.add(a); }
	public ArrayList<String> getAssignees() { return assignees; }
	
	private ArrayList<String> ccs = new ArrayList<String>();
	public void addCC(String a) { ccs.add(a); }
	public ArrayList<String> getCCs() { return ccs; }

	private Date update_time;
	public void setUpdatetime(Date time) { update_time = time; }
	public Date getUpdatetime() { return update_time; }
	
	private String phone;
	public void setPhone(String it) { phone = it; }
	public String getPhone() { return phone; }

	private String email;
	public void setEmail(String it) { email = it; }
	public String getEmail() { return email; }
	
	/////////////////////////////////////////////////////////////////////////////////////
	// Description
	//
	public class DescriptionEntry {
		public String content = "";
		public String name = "";
		public Date time = new Date();
	}
	private ArrayList<DescriptionEntry> descriptions = new ArrayList<DescriptionEntry>();
	public void addDescription(DescriptionEntry entry) { descriptions.add(entry); }
	public ArrayList<DescriptionEntry> getDescriptions() {
		return descriptions;
	}
	public DescriptionEntry getLatestDescription() 
	{ 
		if(descriptions.size() > 0) {
			DescriptionEntry last = descriptions.get(0);
			if(last.time.equals(update_time)) {
				return descriptions.get(0);
			} else {
				return new DescriptionEntry(); //no content was added during the last update
			}
		}
		return new DescriptionEntry();
	}
	public String getDescriptionsAfter(Date after) 
	{ 
		String updates = "";
		for(DescriptionEntry entry : descriptions) {
			if(after == null || entry.time.after(after)) {
				updates += entry.content + "\n -- by " + entry.name + " at " + entry.time.toString() + "\n\n";
			}
		}
		
		return updates;
	}
	
	public DescriptionEntry getFirstDescription() { 
		if(descriptions.size() > 0) {
			return descriptions.get(descriptions.size()-1);
		}
		return new DescriptionEntry();
	}
	
	public String getLastModifier() {
		if(descriptions.size() > 0) {
			return descriptions.get(0).name;
		}
		return "";
	}
	public String getPastHistory() {
		String past_history = "";
		int i = 1;
		for(DescriptionEntry entry : descriptions) {
			//don't include the very last one (it's available through getLatestDescription())
			if(i == descriptions.size()) break;
			
			//prepend this entry (if there is any content)
			String entry_s = entry.content + "\n -- by " + entry.name + " at " + entry.time.toString() + "\n\n";
			past_history = entry_s + past_history;
			
			++i;
		
		}
		return past_history;
	}	
	
	/*
	private String destination_vo = "Ops";
	public void setDestinationVO(String it) { destination_vo = it; }
	public String getDestinationVO() { return destination_vo; }
	
	private String originating_vo = "Ops";
	public void setOriginatingVO(String it) { originating_vo = it; }
	public String getOriginatingVO() { return originating_vo; }
	*/
	private String origin_note;
	public String getOriginNote() { return origin_note; }
	public void setOriginNote(String origin_note) { this.origin_note = origin_note; }
	
	//following is only used by GOC Ticket
	private HashMap<String, String> metadata = new HashMap<String, String>();
	public void setMetadata(String key, String value) { metadata.put(key, value); }
	public String getMetadata(String key) { return metadata.get(key); }
	public HashMap<String, String> getMetadata() { return metadata; }
	
	@Override
	public void mergeMeta(Ticket source_ticket) {
		//nothing to do.. see comment above TicketExchange::processUpdate() / source_new_ticket.mergeMeta(source_ticket); for more detail
	}
}
