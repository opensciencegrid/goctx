package edu.iu.grid.tx.ticket;

import java.math.BigInteger;
import java.rmi.RemoteException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;

import org.apache.log4j.Logger;

import edu.iu.grid.tx.accessor.ServiceNowAccessor;
import edu.iu.grid.tx.soap.servicenow.ServiceNow_sys_journal_fieldStub.GetRecordsResult_type0;

public class ServiceNowTicket implements Ticket {
	
	static Logger logger = Logger.getLogger(ServiceNowTicket.class);
	SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	public ServiceNowTicket(ServiceNowAccessor accessor) {
		this.accessor = accessor;
		df.setTimeZone(TimeZone.getTimeZone("UTC"));
	}
	
	private String id;
	public void setTicketID(String _id) { id = _id; }
	public String getTicketID() { return id; }
	
	private String sys_id;
	public void setSysID(String sys_id) { this.sys_id = sys_id; }
	public String getSysID() { return sys_id; }
	
	private String description;
	public void setDescription(String _d) { description = _d; }
	public String getDescription() { return description; }
	
	public class Comment {
		public String content;
		public String name;
		public Date time;
	}
	private ServiceNowAccessor accessor = null;
	public void loadComments() throws RemoteException {
		
		comments = new ArrayList<Comment>();
		
		//if the ticket was created via conversion, sometime past_history is empty (simply because the source ticket doesn't have any..)
		//if this occurs, then we can simply ignore this.
		if(accessor != null) {
			GetRecordsResult_type0 coms[] = accessor.getComments(sys_id);
			if(coms != null) {
				for(GetRecordsResult_type0 com : coms) {
					Comment entry = new Comment();
					entry.content = com.getValue();
					entry.name = com.getSys_created_by();
					try {
						entry.time = df.parse(com.getSys_created_on());
					} catch (ParseException e) {
						logger.error("ServiceNowTicket::loadComments: Couldn't parse date: " + com.getSys_created_on(), e);
					}
					
					//ignore status change only histories..
					if(entry.content.length() > 0) {
						comments.add(entry);
					}
				}
			} else {
				logger.debug("Failed to load comments - maybe there is none?");
			}
		}
	}
	private ArrayList<Comment> comments = null;
	public void addComment(Comment entry) throws RemoteException { 
		if(comments == null) {
			loadComments();
		}
		comments.add(entry); 
	}
	
	public ArrayList<Comment> getComments() throws RemoteException
	{
		if(comments == null) {
			loadComments();
		}
		return comments;
	}
	
	public String aggregateCommentsAfter(Date after) throws RemoteException 
	{ 
		if(comments == null) {
			loadComments();
		}
		
		String updates = "";
		for(Comment entry : comments) {
			if(after == null || entry.time.after(after)) {
				updates = entry.content + "\n" + " -- by " + entry.name + " at " + entry.time.toString() + "\n\n" + updates;
			}
		}
		return updates;
	}

	/*
	public Comment getLastComment() 
	{ 
		if(comments.size() > 0) {
			Comment last = comments.get(0);
			if(last.time.equals(updatetime)) {
				return comments.get(0);
			} else {
				return new Comment(); //no content was added during the last update
			}
		}
		//no comments - no last comment
		return new Comment();
	}
	*/
		
	private Date updatetime;
	@Override public Date getUpdatetime() { return updatetime; }
	public void setUpdatetime(Date updatetime) { this.updatetime = updatetime; }
	public void setUpdatetime(String updatetime) throws ParseException { this.updatetime = df.parse(updatetime); }
	
	private String origin_note;
	@Override public String getOriginNote() { return origin_note; }
	public void setOriginNote(String origin_note) { this.origin_note = origin_note; }
	
	private String short_description;
	public String getShortDescription() { return short_description; }
	public void setShortDescription(String short_description) { this.short_description = short_description; }
	
	private BigInteger urgency;
	public BigInteger getUrgency() { return urgency; }
	public void setUrgency(BigInteger urgency) { this.urgency = urgency; }
	
	private BigInteger incident_state;
	public BigInteger getIncidentState() { return incident_state; }
	public void setIncidentState(BigInteger state) { this.incident_state = state; }
	
	private String created_by;
	public String getCreatedBy() { return created_by; }
	public void setCreatedBy(String created_by) { this.created_by = created_by; }
	
	private Date created_on;
	public Date getCreatedOn() { return created_on; }
	public void setCreatedOn(Date created_on) { this.created_on = created_on; }
	public void setCreatedOn(String created_on) { 
		try {
			this.created_on = df.parse(created_on);
		} catch (ParseException e) {
			logger.error("Failed to parse opened_at: " + created_on, e);
		} 
	}
	
	private String assignment_group;
	public String getAssignmentGroup() { return assignment_group; }
	public void setAssignmentGroup(String assignment_group) { this.assignment_group = assignment_group; }
	
	///////////////////////////////////////////////////////////////////////////////////////////////
	//
	// fields that aren't exchanged but needs to be set (required)
	//
	private String category;
	public String getCategory() { return category; }
	public void setCategory(String category) { this.category = category; }
	
	private String sub_category;
	public String getSubcategory() { return sub_category; }
	public void setSubcategory(String sub_category) { this.sub_category = sub_category; }

	private String categorization;
	public String getCategorization() { return categorization; }
	public void setCategorization(String categorization) { this.categorization = categorization; }
	
	private String customer_categorization;
	public String getCustomerCategorization() { return customer_categorization; }
	public void setCustomerCategorization(String customer_categorization) { this.customer_categorization = customer_categorization; }
	
	private String item;
	public String getItem() { return item; }
	public void setItem(String item) { this.item = item; }
	
	private String operational_category;
	public String getOperationalCategory() { return operational_category; }
	public void setOperationalCategory(String operational_category) { this.operational_category = operational_category; }
	
	private String reported_source;
	public String getReportedSource() { return reported_source; }
	public void setReportedSource(String reported_source) { this.reported_source = reported_source; }
	
	private String service;
	public String getService() { return service; }
	public void setService(String service) { this.service = service; }

	private BigInteger impact;
	public BigInteger getImpact() { return impact; }
	public void setImpact(BigInteger impact) { this.impact = impact; }
	
	private String caller_id;
	private String caller_user_id;
	public String getCallerID() { return caller_id; }
	public String getCallerUserID() { return caller_user_id; }
	public void setCallerID(String caller_id, String caller_user_id) { 
		this.caller_id = caller_id; 
		this.caller_user_id = caller_user_id;
	}
	
	private String external_ticket_number;
	public String getExternalTicketNumber() { return external_ticket_number; }
	public void setExternalTicketNumber(String external_ticket_number) { this.external_ticket_number = external_ticket_number; }	
}
