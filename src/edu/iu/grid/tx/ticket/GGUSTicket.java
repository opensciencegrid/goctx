package edu.iu.grid.tx.ticket;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import org.apache.log4j.Logger;

import edu.iu.grid.tx.accessor.GGUSSOAPAccessor;
import edu.iu.grid.tx.accessor.RTAccessor;
import edu.iu.grid.tx.soap.ggus.GGUSServiceStub.GHD_PriorityType;
import edu.iu.grid.tx.soap.ggus.GGUSServiceStub.GHD_Ticket_TypeType;
import edu.iu.grid.tx.soap.ggus.GGUS_HISTORYServiceStub.GetListValues_type2;
import edu.iu.grid.tx.soap.ggus.Grid_HistoryServiceStub.GetListValues_type0;

public class GGUSTicket implements Ticket {
	static Logger logger = Logger.getLogger(RTAccessor.class);
	
	private GGUSSOAPAccessor accessor = null;
	public GGUSTicket(GGUSSOAPAccessor accessor) {
		this.accessor = accessor;
	}
	
	private String id;
	public void setTicketID(String _id) { id = _id; }
	public String getTicketID() { return id; }
	
	private String description;
	public void setDescription(String _d) { description = _d; }
	public String getDescription() { return description; }
	
	public class PastHistoryEntry {
		public String content;
		public String name;
		public Date time;
	}
	public void loadPastHistory() throws RemoteException {
		pasthistories = new ArrayList<PastHistoryEntry>();
		
		//if the ticket was created via conversion, sometime past_history is empty (simply because the source ticket doesn't have any..)
		//if this occurs, then we can simply ignore this.
		if(accessor != null) {
			GetListValues_type2[] history_list = accessor.getHistory(id);
			for(GetListValues_type2 history : history_list) {
				PastHistoryEntry entry = new PastHistoryEntry();
				
				entry.content = "";
				if(history.getGHD_Internal_Diary() != null) {
					entry.content += history.getGHD_Internal_Diary();
				}
				if(history.getGHD_Public_Diary() != null) {
					entry.content += history.getGHD_Public_Diary();
				}
				if(history.getGHD_Detailed_Solution() != null) {
					entry.content += history.getGHD_Detailed_Solution();
				} 
				if(history.getGHD_Diary_Of_Steps() != null) {
					entry.content += history.getGHD_Diary_Of_Steps();
				} 
				
				entry.name = history.getGHD_Last_Modifier();
				entry.time = history.getGHD_Creation_Date().getTime();
				
				/* -- this doesn't work since the name of attachment uploader is *usually* the name of some user on the other ticketing system
				//ignore comments added by the rest user - most likely due to TX (such as attachment)
				if(entry.name.equals(accessor.getUser())) {
					logger.warn("Ignoring GGUS history entry made by " + accessor.getUser());
					continue;
				}
				*/
				
				//ignore status change only histories..
				if(entry.content.length() > 0) {
					pasthistories.add(entry);
				}
			}
		}
	}
	private ArrayList<PastHistoryEntry> pasthistories = null;
	public void addPastHistory(PastHistoryEntry entry) throws RemoteException { 
		if(pasthistories == null) {
			loadPastHistory();
		}
		pasthistories.add(entry); 
	}
	public ArrayList<PastHistoryEntry> getPastHistory() throws RemoteException
	{
		if(pasthistories == null) {
			loadPastHistory();
		}
		return pasthistories;
	}
	
	public String getPastHistoryAfter(Date after) throws RemoteException 
	{ 
		if(pasthistories == null) {
			loadPastHistory();
		}
		
		String updates = "";
		for(PastHistoryEntry entry : pasthistories) {
			if(after == null || entry.time.after(after)) {
				updates = entry.content + "\n" + " -- by " + entry.name + " at " + entry.time.toString() + "\n\n" + updates;
			}
		}
		return updates;
	}
		
	private String submitter_name; //submitter
	public void setSubmitterName(String _name) { submitter_name = _name; }
	public String getSubmitterName() { return submitter_name; }

	private String submitter_dn; //submitter
	public void setSubmitterDN(String submitter_dn) { this.submitter_dn = submitter_dn; }
	public String getSubmitterDN() { return submitter_dn; }
	
	private String type_of_problem;
	public void setTypeOfProblem(String _type_of_problem) { type_of_problem = _type_of_problem; }
	public String getTypeOfProblem() { return type_of_problem; }
	
	private GHD_PriorityType priority;
	public void setPriority(GHD_PriorityType _priority) { priority = _priority; }
	public GHD_PriorityType getPriority() { return priority; }
	
	private String status;
	public void setStatus(String _status) { status = _status; }
	public String getStatus() { return status; }
	
	private String short_description;
	public void setShortDescription(String _d) { short_description = _d; }
	public String getShortDescription() { return short_description; }
	
	private Date update_time;
	public void setUpdatetime(Date time) { update_time = time; }
	public Date getUpdatetime() { return update_time; }
	
	private String last_modifier;
	public void setLastModifier(String time) { last_modifier = time; }
	public String getLastModifier() { return last_modifier; }
	
	//diary is the "description" that should be added to next ticket update.
	private String diary;
	public void setDiary(String _d) { diary = _d; }
	public String getDiary() { return diary; }
	
	private Calendar date_of_creation;
	public void setDateOfCreation(Calendar _d) { date_of_creation = _d; }
	public Calendar getDateOfCreation() { return date_of_creation; }
	
	private String detailed_solution;
	public void setDetailedSolution(String it) { detailed_solution = it; }
	public String getDetailedSolution() { return detailed_solution; }
	
	private GHD_Ticket_TypeType ticket_type;
	/* [Guenter Grein]
GHD_Ticket_Type can have these values: USER, ALARM, TEAM, CIC, SAVANNAH and LHCOPN.
LHCOPN is more or less a theoretical value for you as LHCOPN tickets are not routed to any other system outside GGUS.
The default value for GHD_Ticket_Type is USER. Hence this field won't be set to null.
	 */
	public void setTicketType(GHD_Ticket_TypeType it) { ticket_type = it; }
	public GHD_Ticket_TypeType getTicketType() { return ticket_type; }
	
	private String phone;
	public void setPhone(String it) { phone = it; }
	public String getPhone() { return phone; }
	
	private String email;
	public void setEmail(String it) { email = it; }
	public String getEmail() { return email; }
	
	private String affected_site; 
	public void setAffectedSite(String it) { affected_site = it; }
	public String getAffectedSite() { return affected_site; }
	
	private String concerned_vo; 
	public void setConcernedVO(String it) { concerned_vo = it; }
	public String getConcernedVO() { return concerned_vo; }

	private String origin_id; //originating ticket ID
	public void setOriginID(String it) { origin_id = it; }
	public String getOriginID() { return origin_id; }	
	
	private String origin_note;
	public String getOriginNote() { return origin_note; }
	public void setOriginNote(String origin_note) { this.origin_note = origin_note; }
	
	private String origin_sg;
	public String getOriginSG() { return origin_sg; }
	public void setOriginSG(String origin_sg) { this.origin_sg = origin_sg; }
	
	private String responsible_unit;
	public String getResponsibleUnit() { return responsible_unit; }
	public void setResponsibleUnit(String responsible_unit) { this.responsible_unit = responsible_unit; }
}
