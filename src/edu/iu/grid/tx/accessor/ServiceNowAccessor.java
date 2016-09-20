package edu.iu.grid.tx.accessor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.rmi.RemoteException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;

import org.apache.axiom.om.util.Base64;
import org.apache.axis2.AxisFault;
import org.apache.axis2.transport.http.HttpTransportProperties;
import org.apache.log4j.Logger;
import edu.iu.grid.tx.soap.servicenow.ServiceNow_ecc_queueStub;
import edu.iu.grid.tx.soap.servicenow.ServiceNow_sys_attachmentStub;
import edu.iu.grid.tx.soap.servicenow.ServiceNow_sys_journal_fieldStub.GetRecordsResponse;
import edu.iu.grid.tx.soap.servicenow.ServiceNow_sys_journal_fieldStub.GetRecordsResult_type0;
import edu.iu.grid.tx.soap.servicenow.ServiceNow_incidentStub;
import edu.iu.grid.tx.soap.servicenow.ServiceNow_sys_journal_fieldStub;
import edu.iu.grid.tx.soap.servicenow.ServiceNow_incidentStub.GetResponse;
import edu.iu.grid.tx.soap.servicenow.ServiceNow_incidentStub.GetKeysResponse;
import edu.iu.grid.tx.soap.servicenow.ServiceNow_incidentStub.Insert;
import edu.iu.grid.tx.soap.servicenow.ServiceNow_incidentStub.InsertResponse;
import edu.iu.grid.tx.soap.servicenow.ServiceNow_incidentStub.UpdateResponse;
import edu.iu.grid.tx.soap.servicenow.ServiceNow_sys_userStub;
import edu.iu.grid.tx.ticket.ServiceNowTicket;
import edu.iu.grid.tx.ticket.Ticket;

public class ServiceNowAccessor implements TicketAccessor {
	static Logger logger = Logger.getLogger(ServiceNowAccessor.class);
	
	private String endpoint;
	private HttpTransportProperties.Authenticator basicAuthentication;
	private String ticket_url;
	//private String caller;
	
	/*
	private String user;
	private String password;
	*/
	
	public ServiceNowAccessor(String _endpoint, String _user, String _password, String _ticket_url) throws AxisFault {
		endpoint = _endpoint;
		basicAuthentication = new HttpTransportProperties.Authenticator();
		basicAuthentication.setUsername(_user);
		basicAuthentication.setPassword(_password);
		
		/*
		user = _user;
		password = _password;
		*/
		
		//caller = _user;
		ticket_url = _ticket_url;
	}

	public String create(Ticket servicenowticket, String reverse_assignee) 
	{
		ServiceNowTicket ticket = (ServiceNowTicket)servicenowticket;
		
		try {
			ServiceNow_incidentStub stub = new ServiceNow_incidentStub(endpoint + "/incident.do?SOAP");
			stub._getServiceClient().getOptions().setProperty(org.apache.axis2.transport.http.HTTPConstants.AUTHENTICATE, basicAuthentication);
			stub._getServiceClient().getOptions().setProperty(org.apache.axis2.transport.http.HTTPConstants.CHUNKED, Boolean.FALSE);
			stub._getServiceClient().getOptions().setProperty(org.apache.axis2.transport.http.HTTPConstants.SO_TIMEOUT, new Integer(60*1000));
			stub._getServiceClient().getOptions().setProperty(org.apache.axis2.transport.http.HTTPConstants.CONNECTION_TIMEOUT, new Integer(60*1000));
			
			Insert insert = new Insert();

			//required fields
			insert.setShort_description(ticket.getShortDescription());
			insert.setCaller_id(reverse_assignee);
			insert.setUrgency(ticket.getUrgency());
			
			//required fields - with defaults
			//insert.setCategory("General");
			//insert.setSubcategory("GOC Ticket");//GOC Ticket
			//insert.setU_categorization("Other");//Other
			//insert.setU_customer_categorization("GOC");//GOC
			insert.setU_item("Other");
			insert.setU_operational_category("Information");
			insert.setU_reported_source("Direct Input");
			insert.setU_service("Other");
			insert.setImpact(new BigInteger("3"));
			
			//non-required fields
			insert.setIncident_state(ticket.getIncidentState());
			insert.setAssignment_group(ticket.getAssignmentGroup());
			insert.setU_external_ticket_number(ticket.getExternalTicketNumber());
			
			//now construct description
			String description = ticket.getDescription();
			description += "\n\n[Ticket Origin]\n" + ticket.getOriginNote();
			String comments = ticket.aggregateCommentsAfter(null);
			if(comments != null) {
				if(comments.length() > 0) {
					description += "\n\n[Past Comments]\n" + comments;
				}
			}
			insert.setDescription(description);

			InsertResponse resp = stub.insert(insert);
			//System.out.println("number: " + resp.getNumber());
			//System.out.println("sys_id: " + resp.getSys_id());
			return resp.getNumber();
		} catch (RemoteException e) {
			logger.error(e);
		}
		
		return null;
	}

	public void update(Ticket new_ticket, Date last_synctime, Ticket current_ticket) {		
		
		ServiceNowTicket new_servicenow_ticket = (ServiceNowTicket)new_ticket;
		ServiceNowTicket current_servicenow_ticket = (ServiceNowTicket)current_ticket;
		try {
			ServiceNow_incidentStub stub = new ServiceNow_incidentStub(endpoint + "/incident.do?SOAP");
			stub._getServiceClient().getOptions().setProperty(org.apache.axis2.transport.http.HTTPConstants.AUTHENTICATE, basicAuthentication);
			stub._getServiceClient().getOptions().setProperty(org.apache.axis2.transport.http.HTTPConstants.CHUNKED, Boolean.FALSE);
			stub._getServiceClient().getOptions().setProperty(org.apache.axis2.transport.http.HTTPConstants.SO_TIMEOUT, new Integer(60*1000));
			stub._getServiceClient().getOptions().setProperty(org.apache.axis2.transport.http.HTTPConstants.CONNECTION_TIMEOUT, new Integer(60*1000));

			ServiceNow_incidentStub.Update update = new ServiceNow_incidentStub.Update();
			update.setNumber(new_servicenow_ticket.getTicketID());
			String newcomment = new_servicenow_ticket.aggregateCommentsAfter(last_synctime).trim();
			if(newcomment.length() > 0) {
				update.setComments(newcomment);
			}

			//required fields (exchanged field - use new ticket)
			update.setShort_description(new_servicenow_ticket.getShortDescription());
			update.setUrgency(new_servicenow_ticket.getUrgency());
			update.setIncident_state(new_servicenow_ticket.getIncidentState());
			 
			//requird fields (non exchanged field - use value from current ticket)
			update.setSys_id(current_servicenow_ticket.getSysID());
			update.setImpact(current_servicenow_ticket.getImpact());			
			update.setCategory(current_servicenow_ticket.getCategory());
			update.setSubcategory(current_servicenow_ticket.getSubcategory());
			update.setU_categorization(current_servicenow_ticket.getCategorization());
			update.setU_customer_categorization(current_servicenow_ticket.getCustomerCategorization());
			update.setCaller_id(current_servicenow_ticket.getCallerID());
			update.setU_item(current_servicenow_ticket.getItem());
			update.setU_operational_category(current_servicenow_ticket.getOperationalCategory());
			update.setU_reported_source(current_servicenow_ticket.getReportedSource());
			update.setU_service(current_servicenow_ticket.getService());
			
			//non required field
			update.setAssignment_group(current_servicenow_ticket.getAssignmentGroup());
			update.setU_external_ticket_number(new_servicenow_ticket.getExternalTicketNumber());
			
			UpdateResponse resp = stub.update(update);
			//TODO - what can I do with resp?
		} catch (AxisFault e) {
			logger.error(e);
		} catch (RemoteException e) {
			logger.error(e);
		}			
	}

	public Ticket get(String ticket_id) {
		ServiceNowTicket ticket = new ServiceNowTicket(this);
		ticket.setTicketID(ticket_id);
		
		try {
			//ServiceNow_incidentStub stub = new ServiceNow_incidentStub(endpoint + "/incident.do?displayvalue=true&SOAP");
			ServiceNow_incidentStub stub = new ServiceNow_incidentStub(endpoint + "/incident.do?SOAP");
			stub._getServiceClient().getOptions().setProperty(org.apache.axis2.transport.http.HTTPConstants.AUTHENTICATE, basicAuthentication);
			stub._getServiceClient().getOptions().setProperty(org.apache.axis2.transport.http.HTTPConstants.CHUNKED, Boolean.FALSE);			
			stub._getServiceClient().getOptions().setProperty(org.apache.axis2.transport.http.HTTPConstants.SO_TIMEOUT, new Integer(60*1000));
			stub._getServiceClient().getOptions().setProperty(org.apache.axis2.transport.http.HTTPConstants.CONNECTION_TIMEOUT, new Integer(60*1000));

			ServiceNow_incidentStub.GetKeys getkey = new ServiceNow_incidentStub.GetKeys();
			getkey.setNumber(ticket_id);
			GetKeysResponse respkeys = stub.getKeys(getkey);
			String id = respkeys.getSys_id()[0];
			ticket.setSysID(id);
			
			ServiceNow_incidentStub.Get get = new ServiceNow_incidentStub.Get();
			get.setSys_id(id);
			
			GetResponse resp = stub.get(get);
			ticket.setShortDescription(resp.getShort_description());
			ticket.setDescription(resp.getDescription());
			ticket.setUpdatetime(resp.getSys_updated_on());
			ticket.setCreatedBy(resp.getSys_created_by());
			ticket.setCreatedOn(resp.getSys_created_on());
			ticket.setOriginNote(ticket_url+resp.getNumber());
			ticket.setCategory(resp.getCategory());
			ticket.setSubcategory(resp.getSubcategory());
			ticket.setCategorization(resp.getU_categorization());
			ticket.setCustomerCategorization(resp.getU_customer_categorization());
			ticket.setItem(resp.getU_item());
			ticket.setOperationalCategory(resp.getU_operational_category());
			ticket.setReportedSource(resp.getU_reported_source());
			ticket.setService(resp.getU_service());
			ticket.setImpact(resp.getImpact());
			ticket.setUrgency(resp.getUrgency());
			ticket.setIncidentState(resp.getIncident_state());
			ticket.setCreatedOn(resp.getSys_created_on());
			ticket.setCreatedBy(resp.getSys_created_by());
			ticket.setAssignmentGroup(resp.getAssignment_group());
			ticket.setExternalTicketNumber(resp.getU_external_ticket_number());
			
			if(resp.getClose_code() != null && !resp.getClose_code().isEmpty()) {
				ServiceNowTicket.CloseInfo close_info = ticket.new CloseInfo();
				close_info.note = resp.getClose_notes();
				close_info.code = resp.getClose_code();
				//at and by will be empty until ticket gets updated from resolved to closed (per Mike B.)
				close_info.at = resp.getClosed_at(); 
				close_info.by = resp.getClosed_by();
				ticket.setCloseInfo(close_info);
			}

			//lookup caller_user_id from caller_id (sys_id)
			ServiceNow_sys_userStub uStub = new ServiceNow_sys_userStub(endpoint + "/sys_user.do?SOAP");
			uStub._getServiceClient().getOptions().setProperty(org.apache.axis2.transport.http.HTTPConstants.AUTHENTICATE, basicAuthentication);
			uStub._getServiceClient().getOptions().setProperty(org.apache.axis2.transport.http.HTTPConstants.CHUNKED, Boolean.FALSE);			
			uStub._getServiceClient().getOptions().setProperty(org.apache.axis2.transport.http.HTTPConstants.SO_TIMEOUT, new Integer(60*1000));
			uStub._getServiceClient().getOptions().setProperty(org.apache.axis2.transport.http.HTTPConstants.CONNECTION_TIMEOUT, new Integer(60*1000));
			ServiceNow_sys_userStub.Get getuserkey = new ServiceNow_sys_userStub.Get();
			String caller_id = resp.getCaller_id();
			String caller_user_id = "";
			if(caller_id.length() > 0) {
				getuserkey.setSys_id(caller_id);
				ServiceNow_sys_userStub.GetResponse respUserKeys = uStub.get(getuserkey);
				caller_user_id = respUserKeys.getUser_name();
			}
			ticket.setCallerID(caller_id, caller_user_id);
			
		} catch (RemoteException e) {
			logger.error(e);
		} catch (ParseException e) {
			logger.error("probably failed to parse udpatetime:", e);
		}

		return ticket;
	}
	
	public GetRecordsResult_type0[] getComments(String id) throws RemoteException {	
	
		//http://wiki.service-now.com/index.php?title=Direct_Web_Services#getRecords
		//(see under "Retrieving Journal Entries")
		ServiceNow_sys_journal_fieldStub stub = new ServiceNow_sys_journal_fieldStub(endpoint + "/sys_journal_field.do?SOAP");
		stub._getServiceClient().getOptions().setProperty(org.apache.axis2.transport.http.HTTPConstants.AUTHENTICATE, basicAuthentication);
		stub._getServiceClient().getOptions().setProperty(org.apache.axis2.transport.http.HTTPConstants.CHUNKED, Boolean.FALSE);			
		stub._getServiceClient().getOptions().setProperty(org.apache.axis2.transport.http.HTTPConstants.SO_TIMEOUT, new Integer(60*1000));
		stub._getServiceClient().getOptions().setProperty(org.apache.axis2.transport.http.HTTPConstants.CONNECTION_TIMEOUT, new Integer(60*1000));

		ServiceNow_sys_journal_fieldStub.GetRecords get = new ServiceNow_sys_journal_fieldStub.GetRecords();
		get.setElement_id(id);
		
		//testing this again per Mike Baker's request
		get.setElement("comments"); //this is now broke on servicenow interface
		
		GetRecordsResponse resp = stub.getRecords(get);
		return resp.getGetRecordsResult();
	}
	
	public String parseTicketID(String subject) {
		//Parses : " Incident INC000000103312 opened -- Test Ticket"
		int pos = subject.indexOf("INC0");
		int pos2 = subject.indexOf(" ", pos+1);
		if(pos2 == -1) pos2 = subject.length();
		String id = subject.substring(pos, pos2);
		
		//remove trailing :
		if(id.charAt(id.length()-1) == ':') {
			id = id.substring(0, id.length() -1);
		}
		
		return id;
	}

	@Override
	public ArrayList<Attachment> getAttachments(String ticket_id)
			throws Exception {
		
		//first lookup sys_id from ticket_id
		ServiceNow_incidentStub ticket_stub = new ServiceNow_incidentStub(endpoint + "/incident.do?SOAP");
		ticket_stub._getServiceClient().getOptions().setProperty(org.apache.axis2.transport.http.HTTPConstants.AUTHENTICATE, basicAuthentication);
		ticket_stub._getServiceClient().getOptions().setProperty(org.apache.axis2.transport.http.HTTPConstants.CHUNKED, Boolean.FALSE);
		ticket_stub._getServiceClient().getOptions().setProperty(org.apache.axis2.transport.http.HTTPConstants.SO_TIMEOUT, new Integer(60*1000));
		ticket_stub._getServiceClient().getOptions().setProperty(org.apache.axis2.transport.http.HTTPConstants.CONNECTION_TIMEOUT, new Integer(60*1000));

		ServiceNow_incidentStub.GetKeys getkey = new ServiceNow_incidentStub.GetKeys();
		getkey.setNumber(ticket_id);
		GetKeysResponse respkeys = ticket_stub.getKeys(getkey);
		String sys_id = respkeys.getSys_id()[0];
		
		//then load the attatchment list
		ServiceNow_sys_attachmentStub stub = new ServiceNow_sys_attachmentStub(endpoint + "/sys_attachment.do?SOAP");
		stub._getServiceClient().getOptions().setProperty(org.apache.axis2.transport.http.HTTPConstants.AUTHENTICATE, basicAuthentication);
		stub._getServiceClient().getOptions().setProperty(org.apache.axis2.transport.http.HTTPConstants.CHUNKED, Boolean.FALSE);
		stub._getServiceClient().getOptions().setProperty(org.apache.axis2.transport.http.HTTPConstants.SO_TIMEOUT, new Integer(60*1000));
		stub._getServiceClient().getOptions().setProperty(org.apache.axis2.transport.http.HTTPConstants.CONNECTION_TIMEOUT, new Integer(60*1000));

		
		ServiceNow_sys_attachmentStub.GetRecords get = new ServiceNow_sys_attachmentStub.GetRecords();
		//get.setSys_id(sys_id);
		get.setTable_sys_id(sys_id);
		ServiceNow_sys_attachmentStub.GetRecordsResponse resp = stub.getRecords(get);
		
		ArrayList<Attachment> attachments = new ArrayList<Attachment>();
		ServiceNow_sys_attachmentStub.GetRecordsResult_type0 [] records = resp.getGetRecordsResult();
		if(records == null) {
			//we never had snow attachment working, so let's generate warning instead of error
			logger.warn("Failed to load ServiceNow attachments");
		} else {
			for(ServiceNow_sys_attachmentStub.GetRecordsResult_type0 record : records) {
				Attachment attachment = new Attachment();
				attachment.id = record.getSys_id();
				attachment.name = record.getFile_name();
				attachment.owner = record.getSys_updated_by();
				attachment.content_type = record.getContent_type();
				attachments.add(attachment);
			}
		}

		return attachments;		
	}

	//this hasn't worked yet.
	@Override
	public File downloadAttachment(String ticket_id_ignored, Attachment attachment)
			throws Exception {
	
		/*
		ServiceNow_ecc_queueStub stub = new ServiceNow_ecc_queueStub(endpoint + "/ecc_queue.do?SOAP");
		stub._getServiceClient().getOptions().setProperty(org.apache.axis2.transport.http.HTTPConstants.AUTHENTICATE, basicAuthentication);
		stub._getServiceClient().getOptions().setProperty(org.apache.axis2.transport.http.HTTPConstants.CHUNKED, Boolean.FALSE);	
		
		//download
		ServiceNow_ecc_queueStub.Get param = new ServiceNow_ecc_queueStub.Get();
		param.setSys_id(attachment.id);
		param.set__use_view("false");
		ServiceNow_ecc_queueStub.GetResponse res = stub.get(param);
	
		
		//save to file
		String base64 = res.getPayload();
		byte[] payload = Base64.decode(base64);
	    File tempfile = File.createTempFile("GOCTX.ServiceNow.", "."+attachment.name); //trimming newline char at the end
		FileOutputStream out = new FileOutputStream(tempfile.getAbsolutePath());
		out.write(payload);
		out.close();
		
		return tempfile;
		*/
		

		URL url = new URL(endpoint + "/sys_attachment.do?sys_id=" + attachment.id);
		HttpURLConnection  conn = (HttpURLConnection)url.openConnection();
		
		/*this doesn't work*/
		String username = basicAuthentication.getUsername();
		String password = basicAuthentication.getPassword();
		String input = username + ":" + password;
		String encoding = Base64.encode(input.getBytes());
		conn.setRequestProperty("Authorization", "Basic "+ encoding);
		
	    InputStream in = conn.getInputStream();
	    File tempfile = File.createTempFile("ServiceNow."+attachment.id, "."+attachment.name);
		FileOutputStream out = new FileOutputStream(tempfile.getAbsolutePath());
		byte[] buf = new byte[1024];
		while (true){
				int size = in.read(buf, 0, 1024);
				if(size == -1) break;
				out.write(buf, 0, size);
		}
		out.close();
		return tempfile;
	}

	@Override
	public String uploadAttachment(String ticket_id, final Attachment attachment)
			throws Exception {
		
		//first lookup sys_id from ticket_id
		ServiceNow_incidentStub ticket_stub = new ServiceNow_incidentStub(endpoint + "/incident.do?SOAP");
		ticket_stub._getServiceClient().getOptions().setProperty(org.apache.axis2.transport.http.HTTPConstants.AUTHENTICATE, basicAuthentication);
		ticket_stub._getServiceClient().getOptions().setProperty(org.apache.axis2.transport.http.HTTPConstants.CHUNKED, Boolean.FALSE);
		ticket_stub._getServiceClient().getOptions().setProperty(org.apache.axis2.transport.http.HTTPConstants.SO_TIMEOUT, new Integer(60*1000));
		ticket_stub._getServiceClient().getOptions().setProperty(org.apache.axis2.transport.http.HTTPConstants.CONNECTION_TIMEOUT, new Integer(60*1000));
		
		ServiceNow_incidentStub.GetKeys getkey = new ServiceNow_incidentStub.GetKeys();
		getkey.setNumber(ticket_id);
		GetKeysResponse respkeys = ticket_stub.getKeys(getkey);
		String sys_id = respkeys.getSys_id()[0];
		
		ServiceNow_ecc_queueStub stub = new ServiceNow_ecc_queueStub(endpoint + "/ecc_queue.do?SOAP");
		stub._getServiceClient().getOptions().setProperty(org.apache.axis2.transport.http.HTTPConstants.AUTHENTICATE, basicAuthentication);
		stub._getServiceClient().getOptions().setProperty(org.apache.axis2.transport.http.HTTPConstants.CHUNKED, Boolean.FALSE);	
		ticket_stub._getServiceClient().getOptions().setProperty(org.apache.axis2.transport.http.HTTPConstants.SO_TIMEOUT, new Integer(60*1000));
		ticket_stub._getServiceClient().getOptions().setProperty(org.apache.axis2.transport.http.HTTPConstants.CONNECTION_TIMEOUT, new Integer(60*1000));
				
		//load then entire thing into memory - bits at a time
		byte[] all = new byte[(int)attachment.file.length()];
  		byte[] bytebuf = new byte[10240];
  		FileInputStream fis = new FileInputStream(attachment.file);
		int bytesRead = 0;
		int total = 0;
		//StringBuffer buf = new StringBuffer();
		while((bytesRead = fis.read(bytebuf)) != -1)
		{  
			System.arraycopy(bytebuf, 0, all, total, bytesRead);
			total += bytesRead;
		}
		
		//create request
		ServiceNow_ecc_queueStub.Insert param = new ServiceNow_ecc_queueStub.Insert();
		param.setAgent("notused");
		param.setTopic("AttachmentCreator");
		param.setName(attachment.name + ":" + attachment.content_type);
		param.setSource("incident:"+sys_id); //doesn't work?
		param.setPayload(Base64.encode(all));
		
		//call, and pull result
		ServiceNow_ecc_queueStub.InsertResponse res = stub.insert(param);
		return res.getSys_id();
	}
	
	//override this if your RT is doing queue based exchange
	@Override
	public boolean isTicketExchanged(Ticket _ticket, String reverse_assignee) {
		
		ServiceNowTicket ticket = (ServiceNowTicket)_ticket;
		if(ticket.getCallerUserID() != null) {
			if(ticket.getCallerUserID().equals(reverse_assignee)) {
		
				return true;
			}
			logger.debug("caller_user_id:"+ticket.getCallerUserID());
		}
		return false;
	}

}
