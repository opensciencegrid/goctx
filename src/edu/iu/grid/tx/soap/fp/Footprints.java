package edu.iu.grid.tx.soap.fp;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

import javax.xml.soap.*;

import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.log4j.Logger;

import edu.iu.grid.tx.converter.GGUS2FPTicketConverter;
import edu.iu.grid.tx.ticket.FPTicket;
import edu.iu.grid.tx.soap.CleanXML;

public class Footprints
{		
	static Logger logger = Logger.getLogger(Footprints.class);
	
	String endpoint_uri;
	String endpoint_url;
	String projectid;
	String user;
	String password;
	
	//set to true during updateTicket if assignee notification is suppressed (used to manually send triggers)
	boolean bAssigneeNotificationSuppressed;
	public boolean isAssigneeNotificationSuppressed() {
		return bAssigneeNotificationSuppressed;
	}
	
	public Footprints(String _endpoint_uri, String _endpoint_url, String _projectid, String _user, String _password)
	{
		endpoint_uri = _endpoint_uri;
		endpoint_url = _endpoint_url;
		projectid = _projectid;
		user = _user;
		password = _password;
	}
	
	public SOAPMessage initMessage()
	{
		try {

	        MessageFactory msgFactory = MessageFactory.newInstance();
	        SOAPMessage msg = msgFactory.createMessage();
	        SOAPEnvelope env = msg.getSOAPPart().getEnvelope();
	        
	        //these 4 are absolutely necessary
	        env.addNamespaceDeclaration( "xsi", "http://www.w3.org/1999/XMLSchema-instance" );
	        env.addNamespaceDeclaration( "xsd", "http://www.w3.org/1999/XMLSchema" );
	        env.addNamespaceDeclaration( "namesp2", "http://xml.apache.org/xml-soap" );
	        env.addNamespaceDeclaration( "SOAP-ENC", "http://schemas.xmlsoap.org/soap/encoding/" );
	            
	        //env.setEncodingStyle("http://schemas.xmlsoap.org/soap/encoding/"); //doesn't seem to do anything
	        //msg.setProperty("CHARACTER_SET_ENCODING", "UTF-8");//does't work
	        //msg.setProperty(HTTPConstants.CHUNKED, "false");//doesn't do anything
			//msg.setProperty(org.apache.axis2.transport.http.HTTPConstants.CHUNKED, Boolean.FALSE);	//doesn't do anything either
	        
			return msg;
		} catch (UnsupportedOperationException e) {
			logger.error(e);
		} catch (SOAPException e) {
			logger.error(e);
		}
		
		return null;
	}

	//returns new ticket ID
	public String createNewTicket(FPTicket ticket, String description) throws Exception
	{
		SOAPMessage msg = initMessage();
		
		SOAPBody body = msg.getSOAPBody();
		SOAPEnvelope env = msg.getSOAPPart().getEnvelope();
		
        SOAPElement invoke = body.addChildElement( env.createName("MRWebServices__createIssue_goc", "namesp1", endpoint_uri));
        
        // root parameters user/pass/extra/args
        SOAPElement username = invoke.addChildElement( env.createName("user") );
        username.addAttribute( env.createName("type","xsi",""), "xsd:string" );
        username.addTextNode(user);
        
        SOAPElement pass = invoke.addChildElement( env.createName("password") );
        pass.addAttribute( env.createName("type","xsi",""), "xsd:string" );
        pass.addTextNode(password);
        
        SOAPElement extra_info = invoke.addChildElement( env.createName("extrainfo") );
        extra_info.addAttribute( env.createName("type","xsi",""), "xsd:string" );
        
        SOAPElement args = invoke.addChildElement( env.createName("args") );
        args.addAttribute( env.createName("type","xsi",""), "namesp2:SOAPStruct" );
        
        //Contat Information
        SOAPElement abfields = args.addChildElement( env.createName("abfields") );
        abfields.addAttribute( env.createName("type","xsi",""), "namesp2:SOAPStruct" );
        
        SOAPElement arg4_3_2 = abfields.addChildElement( env.createName("Last__bName") );
        arg4_3_2.addAttribute( env.createName("type","xsi",""), "xsd:string" );
        arg4_3_2.addTextNode(ticket.getSubmitterLastname());
        
        SOAPElement arg4_3_3 = abfields.addChildElement( env.createName("First__bName") );
        arg4_3_3.addAttribute( env.createName("type","xsi",""), "xsd:string" );
        arg4_3_3.addTextNode(ticket.getSubmitterFirstname());
                
        if(ticket.getPhone() != null) {
	        SOAPElement arg4_3_5 = abfields.addChildElement( env.createName("Office__bPhone") );
	        arg4_3_5.addAttribute( env.createName("type","xsi",""), "xsd:string" );
	        arg4_3_5.addTextNode(ticket.getPhone());
        }
        
        if(ticket.getEmail() != null) {
	        SOAPElement arg4_3_6 = abfields.addChildElement( env.createName("Email__baddress") );
	        arg4_3_6.addAttribute( env.createName("type","xsi",""), "xsd:string" );
	        arg4_3_6.addTextNode(ticket.getEmail());
        }
	        
        //Basic Information
        SOAPElement arg4_7 = args.addChildElement( env.createName("projectID") );
        arg4_7.addAttribute( env.createName("type","xsi",""), "xsd:int" );
        arg4_7.addTextNode(projectid);       
        SOAPElement arg4_9 = args.addChildElement( env.createName("submitter") );
        arg4_9.addAttribute( env.createName("type","xsi",""), "xsd:string" );
        arg4_9.addTextNode("OSG-GOC"); //only a valid FP user can be submitter..
        
        //can't pass empty title to FP
        String title = "(Untitled Ticket)";
        if(ticket.getTitle() != null && ticket.getTitle().trim().length() != 0) {
        	title = ticket.getTitle();
        }
        SOAPElement arg4_8 = args.addChildElement( env.createName("title") );
        arg4_8.addAttribute( env.createName("type","xsi",""), "xsd:string" );
        arg4_8.addTextNode(title);
        
        if(ticket.getPriority() == null) {
           	//priority can be null if source ticketing system doesn't want to synchronize it.. 
        	//even so, we need to set priority because FP requires it... let's set it to normal priority
        	ticket.setPriority("4");//normal
        }
    	SOAPElement arg4_1 = args.addChildElement( env.createName("priorityNumber") );
    	arg4_1.addAttribute( env.createName("type","xsi",""), "xsd:int" );
    	arg4_1.addTextNode(ticket.getPriority());
    	
        
        SOAPElement arg4_4 = args.addChildElement( env.createName("description") );
        arg4_4.addAttribute( env.createName("type","xsi",""), "xsd:string" );
        arg4_4.addTextNode(description);

        SOAPElement arg4_5 = args.addChildElement( env.createName("assignees") );
        arg4_5.addAttribute( env.createName("type","xsi",""), "SOAP-ENC:Array" );
        ArrayList<String> assignees = ticket.getAssignees();
        arg4_5.addAttribute( env.createName("arrayType","SOAP-ENC",""), "xsd:string["+assignees.size()+"]" );//CHANGE [1] to [n] based on the number of items
        for(String assignee : assignees) {
            SOAPElement arg4_5_1 = arg4_5.addChildElement( env.createName("item") );
            arg4_5_1.addAttribute( env.createName("type","xsi",""), "xsd:string" );
            arg4_5_1.addTextNode(assignee);
        }
        
        SOAPElement ccs_elem = args.addChildElement( env.createName("permanentCCs") );
        ccs_elem.addAttribute( env.createName("type","xsi",""), "SOAP-ENC:Array" );
        ArrayList<String> ccs = ticket.getCCs();
        ccs_elem.addAttribute( env.createName("arrayType","SOAP-ENC",""), "xsd:string["+ccs.size()+"]" );//CHANGE [1] to [n] based on the number of items
        for(String cc : ccs) {
            SOAPElement cc_elem = ccs_elem.addChildElement( env.createName("item") );
            cc_elem.addAttribute( env.createName("type","xsi",""), "xsd:string" );
            cc_elem.addTextNode(cc);
        }  
            
        //project fields
        SOAPElement projfields = args.addChildElement( env.createName("projfields") );
        projfields.addAttribute( env.createName("type","xsi",""), "namesp2:SOAPStruct" );
        
        SOAPElement orig_vo = projfields.addChildElement( env.createName("Originating__bVO__bSupport__bCenter") );
        orig_vo.addAttribute( env.createName("type","xsi",""), "xsd:string" );
        //orig_vo.addTextNode(ticket.getOriginatingVO());
        orig_vo.addTextNode("Ops");
        
        SOAPElement dest_vo = projfields.addChildElement( env.createName("Destination__bVO__bSupport__bCenter") );
        dest_vo.addAttribute( env.createName("type","xsi",""), "xsd:string" );
        //dest_vo.addTextNode(ticket.getDestinationVO());
        dest_vo.addTextNode("Ops");
        
        SOAPElement next_action = projfields.addChildElement( env.createName("ENG__bNext__bAction__bItem") );
        next_action.addAttribute( env.createName("type","xsi",""), "xsd:string" );
        next_action.addTextNode("TX Incoming");
        
        SOAPElement nad = projfields.addChildElement( env.createName("ENG__bNext__bAction__bDate__fTime__b__PUTC__p") );
        nad.addAttribute( env.createName("type","xsi",""), "xsd:string" );
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");  
        nad.addTextNode(format.format(new Date()));            
        
        if(ticket.getTicketType() != null) {
	        SOAPElement type = projfields.addChildElement( env.createName("Ticket__uType") );
	        type.addAttribute( env.createName("type","xsi",""), "xsd:string" );
	        type.addTextNode(ticket.getTicketType()); 
        }
        
        SOAPElement arg4_2 = args.addChildElement( env.createName("status") );
        arg4_2.addAttribute( env.createName("type","xsi",""), "xsd:string" );
        arg4_2.addTextNode(ticket.getStatus());
        msg.saveChanges();
        
        SOAPBody ret = call(msg);
        //DumpSOAPElement(ret, 0);
        
        //pull new ticket ID and return
        Iterator it = ret.getChildElements();
		Object obj = it.next();
		SOAPElement elem = (SOAPElement) obj;
		it = elem.getChildElements();
		
		//open "return"
		obj = it.next();
		elem = (SOAPElement) obj;
		it = elem.getChildElements();
		
		obj = it.next();
		Text text = (Text)obj;
        String id = text.getValue();
        
        //try parsing the ticket ID
        try {
        	Integer num = Integer.parseInt(id);
        } catch (NumberFormatException e) {
        	logger.error("Footprint::create() didn't return a new ticket ID");
        	throw new Exception(ret.getTextContent());
        }
        return id;
	}
	
	public void updateTicket(FPTicket ticket, Date last_synctime) throws SOAPException 
    {

		SOAPMessage msg = initMessage();

        // Compose SOAP body.
        SOAPBody body = msg.getSOAPBody();
		SOAPEnvelope env = msg.getSOAPPart().getEnvelope();
		
        SOAPElement invoke = body.addChildElement( 
        	env.createName("MRWebServices__editIssue_goc", "namesp1","MRWebServices") );

        // root parameters user/pass/extra/args
        SOAPElement username = invoke.addChildElement( env.createName("user") );
        username.addAttribute( env.createName("type","xsi",""), "xsd:string" );
        username.addTextNode(user);
        
        SOAPElement pass = invoke.addChildElement( env.createName("password") );
        pass.addAttribute( env.createName("type","xsi",""), "xsd:string" );
        pass.addTextNode(password);
       
	    SOAPElement extra_info = invoke.addChildElement( env.createName("extrainfo") );
	    extra_info.addAttribute( env.createName("type","xsi",""), "xsd:string" );
		
        SOAPElement args = invoke.addChildElement( env.createName("args") );
        args.addAttribute( env.createName("type","xsi",""), "namesp2:SOAPStruct" );
	    
		SOAPElement arg4 = args.addChildElement(env.createName("projectID"));
		arg4.addAttribute(env.createName("type", "xsi", ""), "xsd:int");
		arg4.addTextNode(projectid);	
		
		SOAPElement arg5 = args.addChildElement(env.createName("mrID"));
		arg5.addAttribute(env.createName("type", "xsi", ""), "xsd:int");
		arg5.addTextNode(ticket.getTicketID());
		
        //can't pass empty title to FP
        String title = "(Untitled Ticket)";
        if(ticket.getTitle() != null && ticket.getTitle().trim().length() != 0) {
        	title = ticket.getTitle();
        }
        SOAPElement arg4_8 = args.addChildElement( env.createName("title") );
        arg4_8.addAttribute( env.createName("type","xsi",""), "xsd:string" );
        arg4_8.addTextNode(title);
        
        if(ticket.getPriority() != null) {
	        SOAPElement arg4_1 = args.addChildElement( env.createName("priorityNumber") );
	        arg4_1.addAttribute( env.createName("type","xsi",""), "xsd:int" );
	        arg4_1.addTextNode(ticket.getPriority());
        }
        
        SOAPElement arg4_9 = args.addChildElement( env.createName("submitter") );
        arg4_9.addAttribute( env.createName("type","xsi",""), "xsd:string" );
        arg4_9.addTextNode("OSG-GOC"); //This has to be a valid FP usser
        
        String lastupdate = ticket.getDescriptionsAfter(last_synctime);
        if(lastupdate.length() > 0) {
        	lastupdate += "GOCTX Source: " + ticket.getOriginNote();
    	
        	//only update if there is actual any update
	        SOAPElement arg4_4 = args.addChildElement( env.createName("description") );
	        arg4_4.addAttribute( env.createName("type","xsi",""), "xsd:string" );
	        arg4_4.addTextNode(lastupdate);
	        logger.debug("description: " + lastupdate);
	        
	        bAssigneeNotificationSuppressed = false;
        } else {
	        logger.debug("description is empty - suppressing email notifications");
	        
            SOAPElement mail = args.addChildElement( env.createName("mail") );

            SOAPElement arg4_10_1 = mail.addChildElement( env.createName("assignees") );
            arg4_10_1.addAttribute( env.createName("type","xsi",""), "xsd:int" );
            arg4_10_1.addTextNode("0");
        
	        bAssigneeNotificationSuppressed = true;
            
            SOAPElement arg4_10_2 = mail.addChildElement( env.createName("contact") );
            arg4_10_2.addAttribute( env.createName("type","xsi",""), "xsd:int" );
            arg4_10_2.addTextNode("0");
            
            SOAPElement arg4_10_3 = mail.addChildElement( env.createName("permanentCCs") );
            arg4_10_3.addAttribute( env.createName("type","xsi",""), "xsd:int" );
            arg4_10_3.addTextNode("0");
        }
        
        SOAPElement arg4_2 = args.addChildElement( env.createName("status") );
        arg4_2.addAttribute( env.createName("type","xsi",""), "xsd:string" );
        arg4_2.addTextNode(ticket.getStatus());
        //msg.saveChanges();
        
        //project fields
        SOAPElement projfields = args.addChildElement( env.createName("projfields") );
        projfields.addAttribute( env.createName("type","xsi",""), "namesp2:SOAPStruct" );
        
        if(ticket.getTicketType() != null) {
        	SOAPElement type = projfields.addChildElement( env.createName("Ticket__uType") );
        	type.addAttribute( env.createName("type","xsi",""), "xsd:string" );
        	type.addTextNode(ticket.getTicketType());  
        }
        //Contat Information

        SOAPElement abfields = args.addChildElement( env.createName("abfields") );
        abfields.addAttribute( env.createName("type","xsi",""), "namesp2:SOAPStruct" );

        if(ticket.getPhone() != null) {
	        SOAPElement arg4_3_5 = abfields.addChildElement( env.createName("Office__bPhone") );
	        arg4_3_5.addAttribute( env.createName("type","xsi",""), "xsd:string" );
	        arg4_3_5.addTextNode(ticket.getPhone());
        }

        if(ticket.getEmail() != null) {
	        SOAPElement arg4_3_6 = abfields.addChildElement( env.createName("Email__baddress") );
	        arg4_3_6.addAttribute( env.createName("type","xsi",""), "xsd:string" );
	        arg4_3_6.addTextNode(ticket.getEmail());
        }
		
		//debug output
		logger.debug("status: " + ticket.getStatus());
		logger.debug("priority: " + ticket.getPriority());
		
        SOAPBody ret = call(msg);
        //DumpSOAPElement(ret, 0);
    }
	
	public SOAPBody getTicket(String id) throws SOAPException
	{
		SOAPMessage msg = initMessage();
		
		SOAPBody body = msg.getSOAPBody();
		SOAPEnvelope env = msg.getSOAPPart().getEnvelope();
		
		SOAPElement invoke = body.addChildElement(
			env.createName("MRWebServices__getIssueDetails_goc", "namesp1", "MRWebServices"));

		SOAPElement arg1 = invoke.addChildElement(env.createName("user"));
		arg1.addAttribute(env.createName("type", "xsi", ""), "xsd:string");
		arg1.addTextNode(user);

		SOAPElement arg2 = invoke.addChildElement(env.createName("password"));
		arg2.addAttribute(env.createName("type", "xsi", ""), "xsd:string");
		arg2.addTextNode(password);
		
		SOAPElement arg3 = invoke.addChildElement(env.createName("extrainfo"));
		arg3.addAttribute(env.createName("type", "xsi", ""), "xsd:string");

		SOAPElement arg4 = invoke.addChildElement(env.createName("projectnumber"));
		arg4.addAttribute(env.createName("type", "xsi", ""), "xsd:int");
		arg4.addTextNode(projectid);

		SOAPElement arg5 = invoke.addChildElement(env.createName("mrid"));
		arg5.addAttribute(env.createName("type", "xsi", ""), "xsd:int");
		arg5.addTextNode(id);
		msg.saveChanges();

		return call(msg);
	}
	
	SOAPBody call(SOAPMessage msg) throws SOAPException
	{
		SOAPConnection connection;
		SOAPConnectionFactory scf = SOAPConnectionFactory.newInstance();
        connection = scf.createConnection();
        
        SOAPMessage reply = connection.call(msg, endpoint_url);

        /*
        //debug .. dump soap
        try {
			reply.writeTo(System.out);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		*/
        
        connection.close();
        SOAPBody ret = reply.getSOAPPart().getEnvelope().getBody();
        if( ret.hasFault() ) {
            throw new SOAPException(ret.getFault().getFaultString());
        }
        
        return ret;
	}
	
	public static String GetIndent(int num) {

		String s = "";
		for (int i = 0; i < num; i++) {
			s = s + " ";
		}
		return s;
	}

	public static void DumpSOAPElement(SOAPElement el, int indent)
	{
		java.util.Iterator it = el.getChildElements();
		while (it.hasNext())
		{
			String indstr = GetIndent(indent);
			Object obj = it.next();
			if (obj instanceof SOAPElement)
			{
				SOAPElement ele = (SOAPElement) obj;
				System.out.println(indstr + "-----------------------------");
				System.out.println(indstr + ele.getElementName().getLocalName());
				System.out.println(indstr + "-----------------------------");
				DumpSOAPElement(ele, indent + 4);
			}
			else if (obj instanceof Text)
			{
				Text txt = (Text) obj;
				System.out.println(indstr + txt.getValue() + "\n");
			}
		}
	}

}
