package edu.iu.grid.tx.accessor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.rmi.RemoteException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;

import javax.activation.DataHandler;
import javax.activation.DataSource;

import org.apache.axis2.AxisFault;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Base64OutputStream;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import edu.iu.grid.tx.soap.ggus.GGUSServiceStub;
import edu.iu.grid.tx.soap.ggus.GGUSServiceStub.GetInputMap;
import edu.iu.grid.tx.soap.ggus.GGUSServiceStub.GetOutputMap;
import edu.iu.grid.tx.soap.ggus.GGUSServiceStub.InputMapping4;
import edu.iu.grid.tx.soap.ggus.GGUSServiceStub.OpCreateResponse;
import edu.iu.grid.tx.soap.ggus.GGUSServiceStub.OutputMapping4;
import edu.iu.grid.tx.soap.ggus.GGUSServiceStub.SetInputMap;
import edu.iu.grid.tx.soap.ggus.GGUSServiceStub.SetOutputMap;
import edu.iu.grid.tx.soap.ggus.GGUSServiceStub.TicketGetResponse;
import edu.iu.grid.tx.soap.ggus.GGUSServiceStub.TicketModifyResponse;
import edu.iu.grid.tx.soap.ggus.GGUS_ATTACHServiceStub;
import edu.iu.grid.tx.soap.ggus.GGUS_ATTACHServiceStub.AddAttachment;
import edu.iu.grid.tx.soap.ggus.GGUS_ATTACHServiceStub.AddAttachmentResponse;
import edu.iu.grid.tx.soap.ggus.GGUS_ATTACHServiceStub.GetAttachIDs;
import edu.iu.grid.tx.soap.ggus.GGUS_ATTACHServiceStub.GetAttachIDsResponse;
import edu.iu.grid.tx.soap.ggus.GGUS_ATTACHServiceStub.GetOneAttachmentResponse;
import edu.iu.grid.tx.soap.ggus.GGUS_ATTACHServiceStub.InputMapping2;
import edu.iu.grid.tx.soap.ggus.GGUS_ATTACHServiceStub.OutputMapping2;
import edu.iu.grid.tx.soap.ggus.GGUS_HISTORYServiceStub;
import edu.iu.grid.tx.soap.ggus.GGUS_HISTORYServiceStub.GetListValues_type2;
import edu.iu.grid.tx.soap.ggus.GGUS_HISTORYServiceStub.InputMapping3;
import edu.iu.grid.tx.soap.ggus.GGUS_HISTORYServiceStub.OutputMapping3;
import edu.iu.grid.tx.soap.ggus.Grid_AttachmentServiceStub;
import edu.iu.grid.tx.soap.ggus.Grid_AttachmentServiceStub.GetListOutputMap;
import edu.iu.grid.tx.ticket.GGUSTicket;
import edu.iu.grid.tx.ticket.Ticket;

public class GGUSSOAPAccessor implements TicketAccessor {
	static Logger logger = Logger.getLogger(GGUSSOAPAccessor.class);
	
	private String endpoint;
	private String user;
	private String password;
	private String ticket_url;
	
	//private GGUSServiceStub stub = null;
	
	public GGUSSOAPAccessor(String _endpoint, String _user, String _password, String _ticket_url) throws AxisFault {
		endpoint = _endpoint;
		user = _user;
		password = _password;
		ticket_url = _ticket_url;

		//stub = new GGUSServiceStub(endpoint);
	}
	
	public String getUser() { return user; };

	public String create(Ticket ggusticket, String reverse_assignee) 
	{
		GGUSTicket ticket = (GGUSTicket)ggusticket;
		//ticket.setPastHistoryAccessor(this);
		//pull the detail via SOAP
		try {
			GGUSServiceStub stub = new GGUSServiceStub(endpoint + "&webService=GGUS");

			GGUSServiceStub.AuthenticationInfoE authe = new GGUSServiceStub.AuthenticationInfoE();
			GGUSServiceStub.AuthenticationInfo auth = new GGUSServiceStub.AuthenticationInfo();
			auth.setUserName(user);
			auth.setPassword(password);
			authe.setAuthenticationInfo(auth);
			
			GGUSServiceStub.OpCreate create = new GGUSServiceStub.OpCreate();
			InputMapping4 param = new InputMapping4();
			
			//don't create a new GGUS ticket with "in progress" (Guenter's request)
			if(ticket.getStatus().equals("in progress")) {
				ticket.setStatus("new");
			}	
			param.setGHD_Status(ticket.getStatus());
			param.setGHD_Last_Modifier(ticket.getLastModifier());
			param.setGHD_Last_Login(user);
			param.setGHD_Priority(ticket.getPriority());
			
			//this field is currently not converted by any converter that I know of.
			//param.setGHD_Affected_VO(ticket.getConvernedVO());
			
			String description = ticket.getDescription();
			description += "\n\n[Ticket Origin]\n" + ticket.getOriginNote();

			String history = ticket.getPastHistoryAfter(null);
			if(history != null) {
				if(history.length() > 0) {
					//if history is too large, grab the last 3000 bytes
					if(history.length() > 3000) {
						history = "(history too large - truncated)\n" + history.substring(history.length() - 3000);
					}
					description += "\n\n[Ticket History]\n" + history;
				}
			}
		
			//truncate the description to be less than 4000 chars
			if(description.length() > 4000) {
				//grab last 4000 chars
				description = description.substring(description.length()-4000+200);//200 is to give some buffer
				description += "\n\n(Text truncated due to GGUS 4000 char limit - please view origin ticket)";
			}
			param.setGHD_Description(description);
			
			String diary = ticket.getDiary();
			if(diary != null) {
				if(diary.length() > 4000) {
					//grab last 4000 chars
					diary = diary.substring(diary.length()-4000+200);//200 is to give some buffer
					diary += "\n\n(Text truncated due to GGUS 4000 char limit - please view origin ticket)";	
				}
			}
			param.setGHD_Internal_Diary(diary);

			param.setGHD_Short_Description(ticket.getShortDescription()); //this can't be null
			//param.setGHD_XML_Short_Description(StringEscapeUtils.escapeXml(ticket.getShortDescription())); //this can't be null
			param.setGHD_Name(ticket.getSubmitterName());
			param.setGHD_Affected_VO("none");
			//param.setGHD_Share_With(reverse_email);
			param.setGHD_Phone(ticket.getPhone());
	
			//TODO - not sure if I need to check for null
			//TODO - Loginname is not the submitter DN!!!!!!!!
			if(ticket.getSubmitterDN() != null) {
				param.setGHD_Loginname(ticket.getSubmitterDN());
				logger.debug("Setting loginname to " + ticket.getSubmitterDN());
			} else {
				logger.warn("Submitter DN information (in source ticket) is not available.. not setting loginname");
			}
			
			/*
			//Setting this will cause GGUS to initiate the old GGUS-TX transaction
			param.setGHD_Origin_SG("OSG");
			param.setGHD_Responsible_Unit(reverse_assignee); 
			*/
			param.setGHD_Responsible_Unit("TPM"); 
			param.setGHD_Origin_SG(reverse_assignee);
			
			param.setGHD_EMail(ticket.getEmail());
			param.setGHD_Origin_ID(ticket.getOriginID());
			/*
    'GHD_Affected_Site' => "",
    'GHD_Loginname' => "/O=GermanGrid/OU=FZK/CN=Guenter Grein",
			 */
			create.setOpCreate(param);
			OpCreateResponse res = stub.opCreate(create, authe);	
			OutputMapping4 out = res.getOpCreateResponse();
			return out.getGHD_Request_ID();
		} catch (AxisFault e1) {
			logger.error(e1);
		} catch (java.rmi.RemoteException e2) {
			logger.error(e2);
		}
		return null;
	}

	public void update(Ticket ggusticket, Date last_synctime, Ticket current_ticket) {		
		GGUSTicket ticket = (GGUSTicket)ggusticket;
		//pull the detail via SOAP
		try {
			GGUSServiceStub stub = new GGUSServiceStub(endpoint + "&webService=GGUS");

			GGUSServiceStub.AuthenticationInfoE authe = new GGUSServiceStub.AuthenticationInfoE();
			GGUSServiceStub.AuthenticationInfo auth = new GGUSServiceStub.AuthenticationInfo();
			auth.setUserName(user);
			auth.setPassword(password);
			authe.setAuthenticationInfo(auth);	
			
			GGUSServiceStub.TicketModify modify = new GGUSServiceStub.TicketModify();
			SetInputMap param = new SetInputMap();
			param.setGHD_Request_ID(ticket.getTicketID());
			param.setGHD_Last_Modifier(ticket.getLastModifier());
			
			//find *current* ggus ticket status
			GGUSTicket current = (GGUSTicket)get(ticket.getTicketID());
			String current_status = current.getStatus();

			//If current GGUS status is new/assigned/on hold, etc.. (that are in open status), and we are trying to reset to "in progess", leave it current (Guenter's request)
			if(current_status.equals("new") ||
				current_status.equals("assigned") ||
				//current_status.equals("in progress") ||
				current_status.equals("waiting for reply") ||
				current_status.equals("on hold") ||
				current_status.equals("reopened")) {
				if(ticket.getStatus().equals("in progress")) { //FP2GGUS set to in progress
					ticket.setStatus(current_status);
				}	
			}
			
			//if current GGUS status is verified, and if we are trying to reset it to solved, keep it in current status
			if(current_status.equals("verified") || 
				current_status.equals("closed") ||
				//current_status.equals("solved") ||
				current_status.equals("unsolved")) {
				if(ticket.getStatus().equals("solved")/* && ticket.getStatus().equals("unsolved")*/) { //FP2GGUs set to solved
					ticket.setStatus(current_status);
				}	
			}
			
			param.setGHD_Status(ticket.getStatus());
			
			//we don't convert converned VO right now.. can't set it.
			//param.setGHD_Affected_VO(ticket.getConvernedVO());
			
			String diary = ticket.getPastHistoryAfter(last_synctime);
			if(ticket.getDiary() != null) {
				diary += "\n" + ticket.getDiary();
			}
			if(diary.length() > 4000) {
				logger.warn("Diary sent to GGUS (ID:"+ticket.getTicketID()+") is too large ("+diary.length()+" chars). GGUS only allows up to 4000 chars. Truncating.. Please see "+ticket.getOriginNote()+" for origin.");
				diary = diary.substring(0, 3800);
				diary += "\n\n[Text truncated due to GGUS 4000 char limit]\nPlease access " + 
					ticket.getOriginNote()+ " for full text.";
			}
			if(diary.trim().length() > 0) {
				param.setGHD_Public_Diary(diary);
			} else {
				logger.debug("dairy is empty - not setting GHD_Public_Diary");
			}
			
			param.setGHD_Last_Login(user);
			param.setGHD_Priority(ticket.getPriority());
			if(ticket.getDetailedSolution() != null) {
				param.setGHD_Detailed_Solution(ticket.getDetailedSolution());
			}
			//param.setGHD_XML_Short_Solution(StringEscapeUtils.escapeXml(ticket.getShortDescription()));//what is this again?
			param.setGHD_Short_Description(ticket.getShortDescription());
			
			//debug output
			logger.debug("public diary: " + param.getGHD_Public_Diary());
			logger.debug("Internal diary: " + param.getGHD_Internal_Diary());
			logger.debug("detailed solution: " + param.getGHD_Detailed_Solution());
			logger.debug("status: " + param.getGHD_Status());
			logger.debug("priority: " + param.getGHD_Priority());
			
			modify.setTicketModify(param);
			TicketModifyResponse res = stub.ticketModify(modify, authe);	
			SetOutputMap out = res.getTicketModifyResponse();
			System.out.println("Updated GGUS Ticket: " + ticket.getTicketID() + " -- Request ID: " + out.getGHD_Request_ID());
		} catch (AxisFault e1) {
			logger.error(e1);
		} catch (java.rmi.RemoteException e2) {
			logger.error(e2);
		}
	}

	public Ticket get(String id) {
		GGUSTicket ticket = new GGUSTicket(this);
		ticket.setTicketID(id);
	
		try {
			GGUSServiceStub stub = new GGUSServiceStub(endpoint + "&webService=GGUS");

			GGUSServiceStub.AuthenticationInfoE authe = new GGUSServiceStub.AuthenticationInfoE();
			GGUSServiceStub.AuthenticationInfo auth = new GGUSServiceStub.AuthenticationInfo();
			auth.setUserName(user);
			auth.setPassword(password);
			authe.setAuthenticationInfo(auth);	
			
			GGUSServiceStub.TicketGet get = new GGUSServiceStub.TicketGet();
			GetInputMap param = new GetInputMap();
			param.setGHD_Request_ID(id);
			get.setTicketGet(param);
			TicketGetResponse res = stub.ticketGet(get, authe);
			
			GetOutputMap ret = res.getTicketGetResponse();
			
			ticket.setShortDescription(ret.getGHD_Short_Description());
			ticket.setDescription(ret.getGHD_Description());
			ticket.setSubmitterName(ret.getGHD_Name());
			ticket.setTypeOfProblem(ret.getGHD_Type_Of_Problem());
			ticket.setPriority(ret.getGHD_Priority());
			ticket.setStatus(ret.getGHD_Status());
			ticket.setLastModifier(ret.getGHD_Last_Modifier());
			ticket.setEmail(ret.getGHD_EMail());
			ticket.setAffectedSite(ret.getGHD_Affected_Site());
			ticket.setConcernedVO(ret.getGHD_Affected_VO());
			ticket.setTicketType(ret.getGHD_Ticket_Type());
			
			//for detecting if the ggus is exchange to destination
			ticket.setOriginSG(ret.getGHD_Origin_SG());
			ticket.setResponsibleUnit(ret.getGHD_Responsible_Unit());
			
			String diary = "";
			if(ret.getGHD_Internal_Diary() != null) {
				diary += ret.getGHD_Internal_Diary();
			}
			if(ret.getGHD_Public_Diary() != null) {
				diary += ret.getGHD_Public_Diary();
			}
			ticket.setDiary(diary);
			
			ticket.setDateOfCreation(ret.getGHD_Date_Of_Creation());
			ticket.setDetailedSolution(ret.getGHD_Detailed_Solution());
			ticket.setPhone(ret.getGHD_Phone());
			
			Date date = ret.getGHD_Date_Of_Change().getTime();
			ticket.setUpdatetime(new Timestamp(date.getTime()));
			
			//TODO - no getGHD_SubmitterDN?
			if(ret.getGHD_Loginname() != null) {
				ticket.setSubmitterDN(ret.getGHD_Loginname());
			}
			
			ticket.setOriginNote(ticket_url + "?ticket=" + id);
		} catch (AxisFault e1) {
			logger.error(e1);
		} catch (java.rmi.RemoteException e2) {
			logger.error(e2);
		}
		
		return ticket;
	}
	
	public GetListValues_type2[] getHistory(String id) throws RemoteException {	
		GGUS_HISTORYServiceStub stub = new GGUS_HISTORYServiceStub(endpoint + "&webService=GGUS_HISTORY");
		
		GGUS_HISTORYServiceStub.AuthenticationInfo auth = new GGUS_HISTORYServiceStub.AuthenticationInfo();
		auth.setUserName(user);
		auth.setPassword(password);
		
		GGUS_HISTORYServiceStub.AuthenticationInfoE authe = new GGUS_HISTORYServiceStub.AuthenticationInfoE();
		authe.setAuthenticationInfo(auth);	
		
		InputMapping3 param = new InputMapping3();
		//param.setGHI_Ticket_ID(id);
		param.setGHD_Request_ID(id);
		//param.setQualification(id); //no longer exists?
		param.setStartRecord("");
		param.setMaxLimit("");

		GGUS_HISTORYServiceStub.OpGetTicketHist get = new GGUS_HISTORYServiceStub.OpGetTicketHist();
		get.setOpGetTicketHist(param);
		
		GGUS_HISTORYServiceStub.OpGetTicketHistResponse res = stub.opGetTicketHist(get, authe);
		
		OutputMapping3 ret = res.getOpGetTicketHistResponse();
		GetListValues_type2[] values = ret.getGetListValues();
	
		return values;

	}
	/*
	public class GGUSAttachment extends Attachment {
		//nothing special to handle..
	}
	*/

	
	public String parseTicketID(String subject) {
		//Parses : " NOTIFICATION of GGUS-Ticket-ID: #40637    Test Ticket"
		int pos = subject.indexOf("#");
		int pos2 = subject.indexOf(" ", pos+1);
		if(pos2 == -1) pos2 = subject.length();
		String id = subject.substring(pos+1, pos2);
		return id;
	}

	@Override
	public ArrayList<Attachment> getAttachments(String id) throws Exception {
		//String debug_endoint = "";//https://train-ars.ggus.eu/arsys/services/ARService?server=train-ars
		GGUS_ATTACHServiceStub stub = new GGUS_ATTACHServiceStub(endpoint + "&webService=GGUS_ATTACH");
		
		//create authe
		GGUS_ATTACHServiceStub.AuthenticationInfo auth = new GGUS_ATTACHServiceStub.AuthenticationInfo();
		auth.setUserName(user);
		auth.setPassword(password);
		GGUS_ATTACHServiceStub.AuthenticationInfoE authe = new GGUS_ATTACHServiceStub.AuthenticationInfoE();
		authe.setAuthenticationInfo(auth);	
		
		//create request
		InputMapping2 param = new GGUS_ATTACHServiceStub.InputMapping2();
		param.setGAT_Request_ID(id);
		GetAttachIDs request = new GGUS_ATTACHServiceStub.GetAttachIDs();
		request.setGetAttachIDs(param);
		
		ArrayList<Attachment> attachments = new ArrayList<Attachment>();
		
		//call, and pull result
		try {
			GetAttachIDsResponse res = stub.getAttachIDs(request, authe);
			OutputMapping2 ret = res.getGetAttachIDsResponse();
			GGUS_ATTACHServiceStub.GetListValues_type0[] values = ret.getGetListValues();
			
			for(GGUS_ATTACHServiceStub.GetListValues_type0 value : values) {
				Attachment attachment = new Attachment();
				attachment.id = value.getGAT_AttachmentID(); //value.getGAT_Attachment_ID();
				attachment.name = value.getGAT_Attachment_attachmentName(); //getGAT_Attachment_Name().trim(); ////ggus still returns newlinea at the end of the name
				attachment.owner = value.getGAT_Last_Modifier();
				attachments.add(attachment);
			}
		} catch(AxisFault e) {
			String message = e.getLocalizedMessage();
			if(message.equals("ERROR (302): Entry does not exist in database; ")) {
				logger.debug("GGUS returns AxisFault with 'Entry does not exist' which means the attachment list is empty.. ignoring this exception");
			} else {
				//rethrow everything else
				throw e;
			}
		}
		
		return attachments;		
	}
	
	/*
	//deprecated.. remove this!
	public ArrayList<Attachment> getAttachments_old(String id) throws Exception {
		//String debug_endoint = "";//https://train-ars.ggus.eu/arsys/services/ARService?server=train-ars
		Grid_AttachmentServiceStub stub = new Grid_AttachmentServiceStub(endpoint + "&webService=Grid_Attachment");

		//create authe
		Grid_AttachmentServiceStub.AuthenticationInfo auth = new Grid_AttachmentServiceStub.AuthenticationInfo();
		auth.setUserName(user);
		auth.setPassword(password);
		Grid_AttachmentServiceStub.AuthenticationInfoE authe = new Grid_AttachmentServiceStub.AuthenticationInfoE();
		authe.setAuthenticationInfo(auth);	

		//create request
		Grid_AttachmentServiceStub.GetListInputMap param = new Grid_AttachmentServiceStub.GetListInputMap();
		param.setGAT_RequestID(id);
		Grid_AttachmentServiceStub.GetAllAttachments request = new Grid_AttachmentServiceStub.GetAllAttachments();
		request.setGetAllAttachments(param);

		ArrayList<Attachment> attachments = new ArrayList<Attachment>();

		//call, and pull result
		try {
			Grid_AttachmentServiceStub.GetAllAttachmentsResponse res = stub.getAllAttachments(request, authe);
			GetListOutputMap ret = res.getGetAllAttachmentsResponse();
			Grid_AttachmentServiceStub.GetListValues_type0[] values = ret.getGetListValues();

			for(Grid_AttachmentServiceStub.GetListValues_type0 value : values) {
				Attachment attachment = new Attachment();
				attachment.id = value.getGAT_AttachmentID();
				attachment.name = value.getGAT_Attachment_Name().trim(); ////ggus still returns newlinea at the end of the name
				attachment.owner = value.getGAT_Last_Modifier();
				attachments.add(attachment);
			}
		} catch(AxisFault e) {
			String message = e.getLocalizedMessage();
			if(message.equals("ERROR (302): Entry does not exist in database; ")) {
				logger.debug("GGUS returns AxisFault with 'Entry does not exist' which means the attachment list is empty.. ignoring this exception");
			} else {
				//rethrow everything else
				throw e;
			}
		}

		return attachments;		
	}
	*/
	
	@Override
	public File downloadAttachment(String ticket_id_notused, Attachment attachment) throws IOException {
		GGUS_ATTACHServiceStub stub = new GGUS_ATTACHServiceStub(endpoint + "&webService=GGUS_ATTACH");

		//create authe
		GGUS_ATTACHServiceStub.AuthenticationInfo auth = new GGUS_ATTACHServiceStub.AuthenticationInfo();
		auth.setUserName(user);
		auth.setPassword(password);
		GGUS_ATTACHServiceStub.AuthenticationInfoE authe = new GGUS_ATTACHServiceStub.AuthenticationInfoE();
		authe.setAuthenticationInfo(auth);	
		
		//create request
		edu.iu.grid.tx.soap.ggus.GGUS_ATTACHServiceStub.InputMapping3 param = new edu.iu.grid.tx.soap.ggus.GGUS_ATTACHServiceStub.InputMapping3();
		param.setGAT_Attachment_ID(attachment.id);
		GGUS_ATTACHServiceStub.GetOneAttachment request = new GGUS_ATTACHServiceStub.GetOneAttachment();
		request.setGetOneAttachment(param);
		
		//call, and pull result
		GetOneAttachmentResponse res = stub.getOneAttachment(request, authe);
		edu.iu.grid.tx.soap.ggus.GGUS_ATTACHServiceStub.OutputMapping3 ret = res.getGetOneAttachmentResponse();
	    InputStream in = ret.getGAT_Attachment_attachmentData().getInputStream();
	    
		File tempfile = File.createTempFile("GOCTX.GGUS.", "."+attachment.name); //trimming newline char at the end
		FileOutputStream out = new FileOutputStream(tempfile.getAbsolutePath());
		
		//why am I decoding the content *sometimes*? See https://jira.opensciencegrid.org/browse/GOCTX-29
	    //load to byte array..
	    byte[] bytes = IOUtils.toByteArray(in);
	    if(Base64.isArrayByteBase64(bytes)) {
	    	Base64OutputStream bout = new Base64OutputStream(out, false);
	    	IOUtils.write(bytes, bout);
	    	bout.close();
	    } else {
	    	IOUtils.write(bytes, out);
	    	out.close();
	    }
		/*
		Base64InputStream b64in = new Base64InputStream(in);
		byte[] buf = new byte[1024];
		while (true){
				int size = b64in.read(buf, 0, 1024);
				if(size == -1) break;
				out.write(buf, 0, size);
		}
	    */
		return tempfile;
	}

	@Override
	public String uploadAttachment(String ticket_id, final Attachment attachment) throws RemoteException {
		GGUS_ATTACHServiceStub stub = new GGUS_ATTACHServiceStub(endpoint + "&webService=GGUS_ATTACH");

		//create authe
		GGUS_ATTACHServiceStub.AuthenticationInfo auth = new GGUS_ATTACHServiceStub.AuthenticationInfo();
		auth.setUserName(user);
		auth.setPassword(password);
		GGUS_ATTACHServiceStub.AuthenticationInfoE authe = new GGUS_ATTACHServiceStub.AuthenticationInfoE();
		authe.setAuthenticationInfo(auth);	
		
		//create datahandler for attachment
		DataHandler attachment_handler = new DataHandler(new DataSource() {
			@Override
			public String getContentType() {
				return attachment.content_type;
			}

			@Override
			public InputStream getInputStream() throws IOException {
				return new FileInputStream(attachment.file);
				//return new Base64InputStream(new FileInputStream(attachment.file), true); //this doesn't make any differnce
			}

			@Override
			public String getName() {
				// TODO Auto-generated method stub
				return attachment.name;
			}

			@Override
			public OutputStream getOutputStream() throws IOException {
				// TODO Auto-generated method stub
				return null;
			}
			
		});
		
		//create request
		GGUS_ATTACHServiceStub.CreateInputMap param = new GGUS_ATTACHServiceStub.CreateInputMap();
		param.setGAT_Request_ID(ticket_id);
		param.setGAT_Attachment_Name(attachment.name);
		param.setGAT_Attachment_Data(attachment_handler);
		param.setGAT_Attachment_Orig_Size((int)attachment.file.length());
		param.setGAT_Last_Modifier(attachment.owner);//required
		param.setGAT_Last_Login("n/a");//required
		//param.setGAT_Origin_SG("n/a");//required
		AddAttachment request = new AddAttachment();
		request.setAddAttachment(param);
		
		//call, and pull result
		AddAttachmentResponse res = stub.addAttachment(request, authe);
		GGUS_ATTACHServiceStub.CreateOutputMap ret = res.getAddAttachmentResponse();
		return ret.getGAT_Attachment_ID();
	}
	
	//override this if your RT is doing queue based exchange
	@Override
	public boolean isTicketExchanged(Ticket _ticket, String reverse_assignee) {
		/*
		RTTicket ticket = (RTTicket)_ticket;
		if(ticket.getRequestor().equals(reverse_assignee)) {
			return true;
		}
		if(ticket.getOwner().equals(user)) {
			return true;
		}
		return false;
		*/
		
		//TODO implement this
		return true;
	}
}
