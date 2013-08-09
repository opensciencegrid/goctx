package edu.iu.grid.tx.ticket;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class JiraTicket implements Ticket {
		
	private String id;
	@Override
	public void setTicketID(String _id) { id = _id; }
	@Override
	public String getTicketID() { return id; }
	
	private String summary;
	public void setSummary(String it) { summary = it; }
	public String getSummary() { return summary; }	
	
	private String description;
	public void setDescription(String it) { description = it; }
	public String getDescription() { return description; }	
	
	private String status; 
	public void setStatus(String it) { status = it; }
	public String getStatus() { return status; }
	
	private String priority;
	public void setPriority(String it) { priority = it; }
	public String getPriority() { return priority; }
	/*
	public class DefaultPriority {
		public static final String Blocker = "1";
		public static final String Critical = "2";
		public static final String Major = "3";
		public static final String Minor = "4";
		public static final String Trivial = "5";
	}
	*/
	
	private Date created;
	public void setCreated(Date time) { created = time; }
	public Date getCreated() { return created; }
	
	private Date updatedtime;
	public void setUpdatetime(Date time) { updatedtime = time; }
	@Override
	public Date getUpdatetime() { return updatedtime; }
	
	private String assignee_id; 
	public void setAssigneeID(String it) { assignee_id = it; }
	public String getAssigneeID() { return assignee_id; }	
	
	private String reporter_id;
	public void setReporterID(String it) { reporter_id = it; }
	public String getReporterID() { return reporter_id; }	

	
	///////////////////////////////////////////////////////////////////////////////////////////////
	// comments
	public ArrayList<Comment> comments = new ArrayList<Comment>();
	public class Comment {
		public String body;
		public String author_name;
		public String author_email;
		public Date time;
	}
	
	public String getCommentsAfter(Date after) 
	{ 
		String ret = "";
		for(Comment entry : comments) {
			if(after == null || entry.time.after(after)) {
				//TODO should I show author email address?
				ret += entry.body + "\n" + " -- by " + entry.author_name + " at " + entry.time.toString() + "\n\n";
			}
		}
		return ret;
	}
		
	private String origin_note;
	@Override
	public String getOriginNote() { return origin_note; }
	@Override
	public void setOriginNote(String origin_note) { this.origin_note = origin_note; }

	private String issuetype;
	public void setIssueType(String it) { issuetype = it; }
	public String getIssueType() { return issuetype; }	
}
