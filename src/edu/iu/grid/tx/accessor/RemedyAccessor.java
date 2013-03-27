package edu.iu.grid.tx.accessor;


import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import org.apache.axis2.AxisFault;
import org.apache.log4j.Logger;

import edu.iu.grid.tx.soap.remedy.HPD_IncidentInterface_WSServiceStub;
import edu.iu.grid.tx.soap.remedy.HPD_IncidentInterface_WSServiceStub.GetInputMap;
import edu.iu.grid.tx.soap.remedy.HPD_IncidentInterface_WSServiceStub.GetOutputMap;
import edu.iu.grid.tx.soap.remedy.HPD_IncidentInterface_WSServiceStub.HelpDesk_Query_Service;
import edu.iu.grid.tx.soap.remedy.HPD_IncidentInterface_WSServiceStub.HelpDesk_Query_ServiceResponse;
import edu.iu.grid.tx.ticket.RemedyTicket;
import edu.iu.grid.tx.ticket.Ticket;

public class RemedyAccessor implements TicketAccessor {
	static Logger logger = Logger.getLogger(RemedyAccessor.class);
	
	private String baseuri;
	private String user;
	private String password;
	
	
	public RemedyAccessor(String _baseuri, String _user, String _password) throws AxisFault {
		baseuri = _baseuri;
		user = _user;
		password = _password;		
	}

	public String create(Ticket remedyticket, String reverse_assignee) 
	{
		RemedyTicket ticket = (RemedyTicket)remedyticket;
	
		//TODO
		
		return null;
	}

	public void update(Ticket remedyticket, Date last_synctime, Ticket current_ticket) {		
		RemedyTicket ticket = (RemedyTicket)remedyticket;

		//TODO
	}
	
	public Ticket get(String id) {
		RemedyTicket ticket = new RemedyTicket();
		ticket.setTicketID(id);
		
		HPD_IncidentInterface_WSServiceStub stub;
		try {
			stub = new HPD_IncidentInterface_WSServiceStub(baseuri + "&webService=HPD_IncidentInterface_WS");
			stub._getServiceClient().getOptions().setProperty(org.apache.axis2.transport.http.HTTPConstants.CHUNKED,Boolean.FALSE);
			
			//stub._getServiceClient().getOptions().setProperty(org.apache.axis2.transport.http.HTTPConstants.HTTP_PROTOCOL_VERSION, org.apache.axis2.transport.http.HTTPConstants.HEADER_PROTOCOL_10);
			
			//set user/pass
			HPD_IncidentInterface_WSServiceStub.AuthenticationInfoE authe = new HPD_IncidentInterface_WSServiceStub.AuthenticationInfoE();
			HPD_IncidentInterface_WSServiceStub.AuthenticationInfo auth = new HPD_IncidentInterface_WSServiceStub.AuthenticationInfo();
			auth.setUserName(user);
			auth.setPassword(password);
			authe.setAuthenticationInfo(auth);	
			
			//set incident number
			HelpDesk_Query_Service get = new HPD_IncidentInterface_WSServiceStub.HelpDesk_Query_Service();
			GetInputMap param = new GetInputMap();
			param.setIncident_Number(id);
			get.setHelpDesk_Query_Service(param);
			
			//call the servivce
			HelpDesk_Query_ServiceResponse res = stub.helpDesk_Query_Service(get, authe);
			GetOutputMap ret = res.getHelpDesk_Query_ServiceResponse();
			
			logger.info("Summary: " + ret.getSummary());
			
			//pull result
			//TODO
			
		} catch (AxisFault e1) {
			logger.error(e1);
		} catch (java.rmi.RemoteException e2) {
			logger.error(e2);
		}
		
		return ticket;
	}
	
	public String parseTicketID(String subject) {
		//Parses : "[triage #6] Ticket title"
		int start = subject.indexOf("#")+1;
		int end = subject.indexOf("]");
		String id = subject.substring(start, end);
		return id;
	}

	@Override
	public ArrayList<Attachment> getAttachments(String ticket_id)
			throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public File downloadAttachment(String ticket_id, Attachment attachment)
			throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String uploadAttachment(String ticket_id, Attachment attachment)
			throws Exception {
		// TODO Auto-generated method stub
		return null;
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
