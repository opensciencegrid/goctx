package edu.iu.grid.tx.accessor;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;

import org.apache.axiom.om.util.Base64;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.w3c.dom.NodeList;

import edu.iu.grid.tx.soap.fp.Footprints;
import edu.iu.grid.tx.ticket.FPTicket;
import edu.iu.grid.tx.ticket.Ticket;
import edu.iu.grid.tx.ticket.FPTicket.DescriptionEntry;

public abstract class FPWebAPIAccessor implements TicketAccessor {
	static Logger logger = Logger.getLogger(FPWebAPIAccessor.class);
	protected Footprints footprints;
	protected String projectid;
	
	FPMetadataAccessor metadata_accessor;
	
	public FPWebAPIAccessor(String _uri, String _url, String _projectid, String _user, String _password, 
			FPMetadataAccessor metadata_accessor) {
		footprints = new Footprints(_uri, _url, _projectid, _user, _password);

		this.metadata_accessor = metadata_accessor;
		projectid = _projectid;
	}

	public String create(Ticket _ticket, String reverse_assignee) throws Exception {
		FPTicket ticket = (FPTicket)_ticket;
		ticket.addAssignee(reverse_assignee);
		
		String description;
		DescriptionEntry description_entry = ticket.getFirstDescription();
		description = description_entry.content;
		
		description += "\n\n[Ticket Origin]\n" + ticket.getOriginNote();
		
		String history = ticket.getPastHistory();
		if(history.trim().length() > 0) {
			description += "\n\n[Ticket History]\n" + history;
		}
		
		customCreateProcess(ticket);
		
		String new_id = footprints.createNewTicket(ticket, description);
		ticket.setTicketID(new_id);
		
		metadata_accessor.setMetadata(ticket);
		
		return new_id;
	}
	
	public void customCreateProcess(FPTicket ticket) {
		ticket.addAssignee("OSG__bGOC__bSupport__bTeam");
	}

	public void update(Ticket _ticket, Date last_synctime, Ticket _current_ticket) throws SOAPException {
		FPTicket ticket = (FPTicket)_ticket;
		footprints.updateTicket(ticket, last_synctime);
		
		//revert any change made on permanent metadata fields
		FPTicket current_ticket = (FPTicket)_current_ticket;
		ticket.setMetadata("SUBMITTER_NAME", current_ticket.getMetadata("SUBMITTER_NAME"));
		ticket.setMetadata("SUBMITTED_VIA", current_ticket.getMetadata("SUBMITTED_VIA"));	
		
		//also preserve any metadata that doesn't exist in the new ticket
		HashMap<String, String> current_meta = current_ticket.getMetadata();
		for(String key: current_meta.keySet()) {
			if(ticket.getMetadata(key) == null) {
				ticket.setMetadata(key, current_meta.get(key));
			}
		}
		
		metadata_accessor.setMetadata(ticket);
	}
	
	public FPTicket get(String id) 
	{
		FPTicket ticket = new FPTicket();
		ticket.setTicketID(id);
		ticket.setProjectID(projectid);
		
		metadata_accessor.getMetadata(ticket);
		
		try {
			//retreive and populate ticket detail
			SOAPBody ret = footprints.getTicket(id);
					
			NodeList nodelist = ret.getChildNodes();
			Iterator it = ret.getChildElements();
			Object obj = it.next();
			SOAPElement elem = (SOAPElement) obj;
			it = elem.getChildElements();

			obj = it.next();
			elem = (SOAPElement) obj;
			it = elem.getChildElements();
			
			String lastdate = null;
			String lasttime = null;
			
			while (it.hasNext())
			{
				obj = it.next();
				if (obj instanceof SOAPElement)
				{
					elem = (SOAPElement) obj;
					String name = elem.getLocalName();
					if(name.equals("title")) {
						ticket.setTitle(getValue(elem));
					} else if (name.equals("priority")) {
						ticket.setPriority(getValue(elem));
					} else if (name.equals("Ticket__uType")) {
						ticket.setTicketType(getValue(elem));
					} else if (name.equals("status")) {
						ticket.setStatus(getValue(elem));
					} else if (name.equals("Last__bName")) {
						ticket.setSubmitterLastname(getValue(elem));
					} else if (name.equals("First__bName")) {
						ticket.setSubmitterFirstname(getValue(elem));
					} else if (name.equals("Office__bPhone")) {
						ticket.setPhone(getValue(elem));
					} else if (name.equals("Email__baddress")) {
						ticket.setEmail(getValue(elem));
					} else if (name.equals("assignees")) {
						String assignees = getValue(elem);
						if(assignees != null) {
							for(String assignee : assignees.split(" ")) {
								if(assignee.startsWith("CC:")) {
									ticket.addCC(assignee.substring(3));
								} else {
									ticket.addAssignee(assignee);
								}
							}
						}
					} else if(name.equals("lasttime")) {
						lasttime = getValue(elem);
					} else if(name.equals("lastdate")) {
						lastdate = getValue(elem);
					} else if(name.equals("allDescriptions")) {
						//TODO - assume the description is entered newone first in FP (meaning - I don't have to sort by timestamp)
						Iterator<SOAPElement> items = elem.getChildElements();
						while (items.hasNext())
						{
							SOAPElement item = items.next();
							FPTicket.DescriptionEntry entry = ticket.new DescriptionEntry();
							parseTicketUpdate(entry, item.getChildElements());
							ticket.addDescription(entry);
						}
		
					}
				}
			}
			
			if(lastdate != null && lasttime != null) {
				SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");
				try {
					Date date = df.parse(lastdate + " " + lasttime + " UTC");
					ticket.setUpdatetime(date);
				} catch (ParseException e) {
					logger.error(e);
					ticket.setUpdatetime(new Date());
				}
			} else {
				logger.warn("Returned ticket doesn't contain last update information - maybe failed to find the ticket");
			}
				
			return ticket;
			
		} catch (SOAPException e) {
			logger.error(e);
		}
		
		return null;
	}	
	
	private String getValue(SOAPElement elem) {
		String encoding = elem.getAttribute("xsi:type");
		String raw = elem.getValue();
		if(encoding != null) {
			if(encoding.equals("SOAP-ENC:base64")) {
				try {
					return new String(Base64.decode(raw), "UTF-8");
				} catch (UnsupportedEncodingException e) {
					logger.error(e);
				}
			} else if(encoding.equals("xsd:string")) {
				return StringEscapeUtils.unescapeXml(raw);
			}
 		}
		
		//I don't know what this is, but just return the raw content
		return elem.getValue();
	}
	
	private void parseTicketUpdate(FPTicket.DescriptionEntry entry, Iterator<SOAPElement> item)
	{
		SimpleDateFormat df = new SimpleDateFormat("MM/dd/yyyy 'at' HH:mm:ss z");
		
		while (item.hasNext())
		{
			SOAPElement elem = item.next();
			String name = elem.getLocalName();
			if(name.equals("data")) {
				entry.content = StringEscapeUtils.unescapeHtml(getValue(elem));
			} else if(name.equals("stamp")) {
				String stamp = getValue(elem);
				int pos = stamp.indexOf(" by ");
				String modifier = stamp.substring(pos+4, stamp.length()-1);
				entry.name = modifier;
				
				pos = stamp.indexOf(" on ");
				String time_s = stamp.substring(pos+4, pos + 30);
				try {
					entry.time = df.parse(time_s);
				} catch (ParseException e) {
					logger.error("Failed to parse the FP date format " + time_s + " (using today's date instead)");
					entry.time = new Date();
				}
			}
		}
	}
	
	//parse GGUS Ticket ID From subject
	public String parseTicketID(String subject) {
		//Parses : "Test Ticket ISSUE=49 PROJ=114"
		int pos = subject.indexOf("ISSUE=");
		if(pos == -1) return null;
		String issue = subject.substring(pos+6, subject.length());
		pos = issue.indexOf(" ");
		return issue.substring(0, pos);
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
