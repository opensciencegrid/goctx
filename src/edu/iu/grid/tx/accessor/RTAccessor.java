package edu.iu.grid.tx.accessor;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.CharBuffer;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Vector;

import org.apache.axis2.AxisFault;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;

import edu.iu.grid.tx.soap.CleanXML;
import edu.iu.grid.tx.ticket.RTTicket;
import edu.iu.grid.tx.ticket.Ticket;
import edu.iu.grid.tx.ticket.RTTicket.PastHistoryEntry;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.StringPart;

//http://wiki.bestpractical.com/view/REST

public class RTAccessor implements TicketAccessor {
	static Logger logger = Logger.getLogger(RTAccessor.class);
	
	private String baseuri;
	private String user;
	private String password;
	private String default_queue;
	private boolean basic_auth;
	
	ArrayList<SimpleDateFormat> dfs = new ArrayList<SimpleDateFormat>();
	
	public RTAccessor(String _baseuri, String _user, String _password, String _default_queue, boolean basic_auth) throws AxisFault {
		this.baseuri = _baseuri;
		this.user = _user;
		this.password = _password;
		this.default_queue = _default_queue;
		this.basic_auth = basic_auth;
		
		//date format to try parsing with
		//https://java.sun.com/j2se/1.4.2/docs/api/java/text/SimpleDateFormat.html
		dfs.add(new SimpleDateFormat("EEE MMM d HH:mm:ss yyyy"));
		dfs.add(new SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'"));
		dfs.add(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
		dfs.add(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z"));
		dfs.add(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
	}
	
	public StringBuffer tabAndWrapContent(String input) {
		StringBuffer output = new StringBuffer();
		
		for(String raw : input.split("\n")) {
			String [] wrapped = wrapText(raw, 72);
			for(String line : wrapped) {
				output.append(line);
				output.append("\n ");//space is left intentionally for the indentation
			}
		}
		return output;
	}
	
	String [] wrapText (String text, int len)
	{
	  // return empty array for null text
	  if (text == null)
	  return new String [] {};

	  // return text if len is zero or less
	  if (len <= 0)
	  return new String [] {text};

	  // return text if less than length
	  if (text.length() <= len)
	  return new String [] {text};

	  char [] chars = text.toCharArray();
	  Vector<String> lines = new Vector<String>();
	  StringBuffer line = new StringBuffer();
	  StringBuffer word = new StringBuffer();

	  for (int i = 0; i < chars.length; i++) {
	    word.append(chars[i]);

	    if (chars[i] == ' ') {
	      if ((line.length() + word.length()) > len) {
	        lines.add(line.toString());
	        line.delete(0, line.length());
	      }

	      line.append(word);
	      word.delete(0, word.length());
	    }
	  }

	  // handle any extra chars in current word
	  if (word.length() > 0) {
	    if ((line.length() + word.length()) > len) {
	      lines.add(line.toString());
	      line.delete(0, line.length());
	    }
	    line.append(word);
	  }

	  // handle extra line
	  if (line.length() > 0) {
	    lines.add(line.toString());
	  }

	  String [] ret = new String[lines.size()];
	  int c = 0; // counter
	  for (Enumeration<String> e = lines.elements(); e.hasMoreElements(); c++) {
	    ret[c] = e.nextElement();
	  }

	  return ret;
	}

	public String create(Ticket rtticket, String reverse_assignee) 
	{
		RTTicket ticket = (RTTicket)rtticket;
		
		HttpClient cl = new HttpClient();
		cl.getParams().setParameter("http.protocol.single-cookie-header", true);
		cl.getParams().setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);
		
		PostMethod mPost;
		if(basic_auth) {
			mPost = new PostMethod(baseuri + "/ticket/new");
			cl.getParams().setAuthenticationPreemptive(true);
			Credentials defaultcreds = new UsernamePasswordCredentials(user, password);
			cl.getState().setCredentials(AuthScope.ANY, defaultcreds);
		} else {
			mPost = new PostMethod(baseuri + "/ticket/new?user="+user+"&pass="+password);
		}
		
		ticket.setText(ticket.getText());
		ticket.setRequestor(reverse_assignee); //TODO - instead of overriding it, we need to "add" CC... but how?
		
		StringBuffer text = new StringBuffer(ticket.getText());
		text.append("\n\n[Ticket Origin]\n");
		text.append(rtticket.getOriginNote());

		String history = ticket.getPastHistoryAfter(null);
		if(history.trim().length() > 0) {
			text.append("\n\n[Ticket History]\n");
			text.append(history);
		}
		
		StringBuffer content = new StringBuffer();
		content.append("Subject: ");
		content.append(ticket.getSubject());
		content.append("\n");
		
		//TODO - why would I have to update CC!?
		if(ticket.getCC() != null) {
			content.append("CC: ");
			content.append(ticket.getCC());
			content.append("\n");
		}
		
		content.append("Status: ");
		//Jason from BNL has requsted that when a new RT ticket is created, he wants this status to be new no matter what
		//content.append(ticket.getStatus());
		content.append("new");
		content.append("\n");
		
		if(ticket.getPriority() != null) {
			content.append("Priority: ");
			content.append(ticket.getPriority());
			content.append("\n");
		}
		
		content.append("Text: ");
		content.append(tabAndWrapContent(text.toString()));
		content.append("\n");
		
		//queue is needed by create()
		content.append("Queue: ");
		if(ticket.getQueue() != null) {
			content.append(ticket.getQueue());
		} else {
			//set it to default queue
			content.append(default_queue);
		}
		content.append("\n"); 
		
		content.append("Requestor: ");
		content.append(ticket.getRequestor());
		content.append("\n");
		
		content.append(getCustomFields(ticket));
		
		logger.debug("Sending Content:\n" + content);
		
		Part[] parts = { new StringPart("content", content.toString()) };
		mPost.setRequestEntity(new MultipartRequestEntity(parts, mPost.getParams()));
		try {
			cl.executeMethod(mPost);
	
			//parse out created ticket ID
			BufferedReader in = checkStatusAndGetBody(mPost);
			String ret = in.readLine();
			logger.debug("parsing id from token[2] of :" + ret);
			String tokens[] = ret.split(" ");
			
			//dump other output if any
			do {
				logger.debug(ret);
				ret = in.readLine();
			} while(ret != null);
			
			//try parsing it as numeric id
			Integer id = Integer.parseInt(tokens[2]);
			return id.toString();
			
		} catch (HttpException e) {
			logger.error(e);
		} catch (IOException e) {
			logger.error(e);
		} catch (Exception e) {
			logger.error("exception in RTAccessor::create()");
			logger.error(e);
		}

		return null;
	}

	public void update(Ticket rtticket, Date last_synctime, Ticket current_ticket) {		
		RTTicket ticket = (RTTicket)rtticket;
		HttpClient cl = new HttpClient();
		cl.getParams().setParameter("http.protocol.single-cookie-header", true);
		cl.getParams().setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);
		cl.getHttpConnectionManager().getParams().setConnectionTimeout(1000*10);
		
		PostMethod mPost;
		if(basic_auth) {
			mPost = new PostMethod(baseuri + "/ticket/"+ticket.getTicketID()+"/edit");
			cl.getParams().setAuthenticationPreemptive(true);
			Credentials defaultcreds = new UsernamePasswordCredentials(user, password);
			cl.getState().setCredentials(AuthScope.ANY, defaultcreds);
		} else {
			mPost = new PostMethod(baseuri + "/ticket/"+ticket.getTicketID()+"/edit?user="+user+"&pass="+password);
		}
		
		StringBuffer content = new StringBuffer();
		content.append("Subject: ");
		content.append(ticket.getSubject());
		content.append("\n");
		//TODO - why would I have to update CC!?
		if(ticket.getCC() != null) {
			content.append("CC: ");
			content.append(ticket.getCC());
			content.append("\n");
		}
		content.append("Status: ");
		content.append(ticket.getStatus());
		content.append("\n");
		if(ticket.getPriority() != null) {
			content.append("Priority: ");
			content.append(ticket.getPriority());
			content.append("\n");
		}
		content.append(getCustomFields(ticket));
		logger.debug(content);
		Part[] parts = { new StringPart("content", content.toString()) };
		mPost.setRequestEntity(new MultipartRequestEntity(parts, mPost.getParams()));
		try {
			cl.executeMethod(mPost);
			checkStatusAndGetBody(mPost);	
		} catch (HttpException e) {
			logger.error(e);
		} catch (IOException e) {
			logger.error(e);
		} catch (Exception e) {
			logger.error(e);
		}
		
		//add correspondece / comment
		String newcomment = ticket.getPastHistoryAfter(last_synctime).trim();
		if(newcomment.length() > 0) {
			//mPost = new PostMethod(baseuri + "/ticket/"+ticket.getTicketID()+"/comment?user="+user+"&pass="+password);	
			if(basic_auth) {
				mPost = new PostMethod(baseuri + "/ticket/"+ticket.getTicketID()+"/comment");
			} else {
				mPost = new PostMethod(baseuri + "/ticket/"+ticket.getTicketID()+"/comment?user="+user+"&pass="+password);	
			}
			
			//avoid RT to re-open ticket due to ticket update when it's in resolved status
			String action = "correspond";
			if(ticket.getStatus().equals("resolved")) {
				action = "comment";
			}
			Part[] parts2 = { new StringPart("content", "Action: "+action+"\nText: " + tabAndWrapContent(newcomment)) };
			mPost.setRequestEntity(new MultipartRequestEntity(parts2, mPost.getParams()));
			try {
				cl.executeMethod(mPost);
				checkStatusAndGetBody(mPost);		
				//logger.debug("Action: " + action + "\nText: " + tabAndWrapContent(newcomment));
			} catch (HttpException e) {
				logger.error(e);
			} catch (IOException e) {
				logger.error(e);
			} catch (Exception e) {
				logger.error(e);
			}
		}
	}
	
	class KeyValue {
		String key;
		String value;
	}
	public ArrayList<KeyValue> parseKeyValues(BufferedReader in) throws Exception {
		ArrayList<KeyValue> kvs = new ArrayList<KeyValue>();
		KeyValue current = null;
		String line = in.readLine();
		StringBuffer value = null;
		while(true) {	
			//read next line
			line = in.readLine();
			if(line == null) break;

			//section delimiter?
			if(line.equals("--")) break;
			
			if(current == null) {
				//new k/v
				int delim = line.indexOf(":");
				if(delim == -1) continue;
				
				current = new KeyValue();
				current.key = line.substring(0, delim);
				value = new StringBuffer(line.substring(delim+1).trim());
			} else {
				//end of multi-line kv?
				if(line.length() == 0) {
					current.value = value.toString();
					kvs.add(current);
					current = null;
				}
				//new kv?
				else if(line.charAt(0) != ' ') {
					current.value = value.toString();
					kvs.add(current);
					
					int delim = line.indexOf(":");
					if(delim == -1) continue;
					
					current = new KeyValue();
					current.key = line.substring(0, delim);
					value = new StringBuffer(line.substring(delim+1).trim());
				}
				//then must be another line of the previous kv
				else {
					value.append("\n");
					value.append(line.trim());
				}
			}
		}
		
		return kvs;
	}
	
	public String searchValueByKey(ArrayList<KeyValue> kvs, String key) {
		for(KeyValue kv : kvs) {
			if(kv.key.equals(key)) {
				return kv.value;
			}
		}
		return null;
	}

	public Date parseDateTime(String input) throws ParseException {
		for(SimpleDateFormat df : dfs) {
			try {
				return df.parse(input);
			} catch (ParseException e) {
				//keep trying 
			}
		}
		throw new ParseException("Couldn't parse " + input + " any of the formats that I know", 0);
	}
	
	public RTTicket get(String id) {
		RTTicket ticket = new RTTicket();
		ticket.setTicketID(id);
	
		HttpClient cl = new HttpClient();
		cl.getParams().setParameter("http.protocol.single-cookie-header", true);
		cl.getParams().setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);
		cl.getHttpConnectionManager().getParams().setConnectionTimeout(1000*10);//10 seconds good?
		
		try {
			///////////////////////////////////////////////////////////////////////////////////////
			//get basic ticket information
			PostMethod mPost;
			if(basic_auth) {
				mPost = new PostMethod(baseuri + "/ticket/"+ticket.getTicketID()+"/show");
				cl.getParams().setAuthenticationPreemptive(true);
				Credentials defaultcreds = new UsernamePasswordCredentials(user, password);
				cl.getState().setCredentials(AuthScope.ANY, defaultcreds);
			} else {
				mPost = new PostMethod(baseuri + "/ticket/"+ticket.getTicketID()+"/show?user="+user+"&pass="+password);
			}
			
			cl.executeMethod(mPost);
			BufferedReader in = checkStatusAndGetBody(mPost);
			for(KeyValue kv : parseKeyValues(in)) {
				if(kv.key.equals("Queue")) {
					ticket.setQueue(kv.value);
				} else if(kv.key.equals("Owner")) {
					ticket.setOwner(kv.value);
				} else if(kv.key.equals("Creator")) {
					ticket.setCreator(kv.value);
				} else if(kv.key.equals("Subject")) {
					ticket.setSubject(kv.value);
				} else if(kv.key.equals("Status")) {
					ticket.setStatus(kv.value);
				} else if(kv.key.equals("Priority")) {
					ticket.setPriority(Integer.parseInt(kv.value));
				} else if(kv.key.equals("CC")) {
					ticket.setCC(kv.value);
				} else if(kv.key.equals("Requestors")) {
					ticket.setRequestor(kv.value);
				} else if(kv.key.equals("Created")) {
					try {
						ticket.setCreated(parseDateTime(kv.value));
					} catch (ParseException e) {
						logger.error("Couldn't parse Created field on ticket detail", e);
					}
				} else if(kv.key.equals("LastUpdated")) {
					try {
						ticket.setLastUpdated(parseDateTime(kv.value));
					} catch (ParseException e) {
						logger.error("Couldn't parse LastUpdated field on ticket detail", e);
					}		
				}	
			}
			
			//this should be overridden by custom accessor
			ticket.setOriginNote("RT(Generic) Ticket: Queue=" + ticket.getQueue() + " ID=" + ticket.getTicketID());
			
			///////////////////////////////////////////////////////////////////////////////////////
			//get a ticket history (long format)
			//mPost = new PostMethod(baseuri + "/ticket/"+id+"/history?format=l&user="+user+"&pass="+password);
			if(basic_auth) {
				mPost = new PostMethod(baseuri + "/ticket/"+ticket.getTicketID()+"/history?format=l");
			} else {
				mPost = new PostMethod(baseuri + "/ticket/"+ticket.getTicketID()+"/history?format=l&user="+user+"&pass="+password);
			}
			
			
			cl.executeMethod(mPost);
			//parse body into separate history objects
			in = checkStatusAndGetBody(mPost);
			while(true) {
				ArrayList<KeyValue> kvs = parseKeyValues(in);
				if(kvs.size() == 0) break;
				String creator = searchValueByKey(kvs, "Creator");
				String type = searchValueByKey(kvs, "Type");
				if(type.equals("Create")) {
					ticket.setText(searchValueByKey(kvs, "Content"));
				} else if(type.equals("Comment") || type.equals("Correspond")) {					
					PastHistoryEntry entry = ticket.new PastHistoryEntry();
					//RT can allow entering of invalid XML chars.. I need to quranteen here
					//because rest of GOC-TX, and other ticketing system as far as I know aren't
					//resilient to such characters
					entry.content = CleanXML.removeBadXMLChars(searchValueByKey(kvs, "Content"));
					entry.name = searchValueByKey(kvs, "Creator");
					entry.time = parseDateTime(searchValueByKey(kvs, "Created"));
					ticket.addPastHistory(entry);
				} else {
					logger.debug("Ignoring unknown history type: " + type);
				}
			}			
		} catch (HttpException e) {
			logger.error(e);
		} catch (IOException e) {
			logger.error(e);
		} catch (Exception e) {
			logger.error("exception in RTAccessor:get() for ticket " + id);
			logger.error(e);
		}

		return ticket;
	}
	
	@Override
	public ArrayList<Attachment> getAttachments(String id) throws Exception {
		
		ArrayList<Attachment> attachments = new ArrayList<Attachment>();
		
		HttpClient cl = new HttpClient();
		cl.getParams().setParameter("http.protocol.single-cookie-header", true);
		cl.getParams().setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);
		cl.getHttpConnectionManager().getParams().setConnectionTimeout(1000*10);//10 seconds good?
		
		//PostMethod mPost = new PostMethod(baseuri + "/ticket/"+id+"/attachments?user="+user+"&pass="+password);
		PostMethod mPost;
		if(basic_auth) {
			mPost = new PostMethod(baseuri + "/ticket/"+id+"/attachments");
			cl.getParams().setAuthenticationPreemptive(true);
			Credentials defaultcreds = new UsernamePasswordCredentials(user, password);
			cl.getState().setCredentials(AuthScope.ANY, defaultcreds);
		} else {
			mPost = new PostMethod(baseuri + "/ticket/"+id+"/attachments?user="+user+"&pass="+password);
		}
		
		cl.executeMethod(mPost);
		//parse body into separate attachment objects
		BufferedReader in = checkStatusAndGetBody(mPost);
		while(true) {
			ArrayList<KeyValue> kvs = parseKeyValues(in);
			if(kvs.size() == 0) break;
			
			String attachments_raw = searchValueByKey(kvs, "Attachments");
			for(String attachment_raw : attachments_raw.split(",\n")) {
				Attachment attachment = new Attachment();
				int pos = attachment_raw.indexOf(':');
				int pos1 = attachment_raw.lastIndexOf("(");
				int pos2 = attachment_raw.indexOf(" ", pos1+2);
				attachment.id = attachment_raw.substring(0, pos);
				attachment.name = attachment_raw.substring(pos+2, pos1-1);
				attachment.owner = "RT via GOC-TX"; //TODO - I should find out who the creator was by querying the ticket history..
				attachment.content_type = attachment_raw.substring(pos1+1, pos2);
				if(!attachment.name.equals("(Unnamed)")) {
					attachments.add(attachment);
				}
			}
		}
		
		return attachments;
	}
	
	private BufferedReader checkStatusAndGetBody(PostMethod post) throws Exception 
	{
		InputStreamReader inr = new InputStreamReader(post.getResponseBodyAsStream());
		BufferedReader in = new BufferedReader(inr);
		String status = in.readLine();
		if(status == null) throw new Exception("RT returned null status.\n" + post.getResponseBodyAsString());
		//logger.debug("status: " + status);
		
		//skip the empty line after status
		in.readLine();
		
		//analyze status
		String tokens[] = status.split(" ");
		if(tokens.length < 3) throw new Exception("RT returned status in unexpected format : " + status);
		if(!tokens[2].equals("Ok")) {
			//consume the rest of the message
			StringBuffer message = new StringBuffer();
			while(in.ready()) {
				message.append(in.readLine());
			}
			throw new Exception("RT returned non-OK stauts code : " + status + " for request path : " + post.getPath() + "\n" + message.toString());
		}

		return in;
	}
	
	public String parseTicketID(String subject) {
		//Parses : "[triage #6] Ticket title"
		int start = subject.indexOf("#")+1;
		int end = subject.indexOf("]");
		String id = subject.substring(start, end);
		return id;
	}
	
	public StringBuffer getCustomFields(RTTicket ticket) {
		StringBuffer content = new StringBuffer();
		HashMap<String, String> custom_fields = ticket.getCustomFields();
		for(String key : custom_fields.keySet()) {
			String value = custom_fields.get(key);
			content.append(key);
			content.append(": ");
			content.append(value);
			content.append("\n");
		}
		return content;
	}
	
	@Override
	public File downloadAttachment(String ticket_id, Attachment attachment) throws Exception {
		HttpClient cl = new HttpClient();		
		cl.getParams().setParameter("http.protocol.single-cookie-header", true);
		cl.getParams().setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);
		cl.getHttpConnectionManager().getParams().setConnectionTimeout(1000*30);
		
		PostMethod mPost;
		//= new PostMethod(baseuri + "/ticket/"+ticket_id+"/attachments/"+attachment.id+"/content?user="+user+"&pass="+password);
		if(basic_auth) {
			mPost = new PostMethod(baseuri + "/ticket/"+ticket_id+"/attachments/"+attachment.id+"/content");
			cl.getParams().setAuthenticationPreemptive(true);
			Credentials defaultcreds = new UsernamePasswordCredentials(user, password);
			cl.getState().setCredentials(AuthScope.ANY, defaultcreds);
		} else {
			mPost = new PostMethod(baseuri + "/ticket/"+ticket_id+"/attachments/"+attachment.id+"/content?user="+user+"&pass="+password);
		}
		
		cl.executeMethod(mPost);
		InputStream in = mPost.getResponseBodyAsStream();
		//skip first 2 lines
		int lines = 0;
		while(true) {
			int ch = in.read();
			if(ch == '\n') lines++;
			if(lines == 2) break;
		}
		
		//save to temp file
	    File tempfile = File.createTempFile("GOCTX.RT.", "."+attachment.name);
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
	public String uploadAttachment(String ticket_id, Attachment attachment) throws Exception 
	{
		
		HttpClient cl = new HttpClient();
		cl.getParams().setParameter("http.protocol.single-cookie-header", true);
		cl.getParams().setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);
		cl.getHttpConnectionManager().getParams().setConnectionTimeout(1000*30);
		
		PostMethod mPost;
		if(basic_auth) {
			mPost = new PostMethod(baseuri + "/ticket/"+ticket_id+"/comment");	
			cl.getParams().setAuthenticationPreemptive(true);
			Credentials defaultcreds = new UsernamePasswordCredentials(user, password);
			cl.getState().setCredentials(AuthScope.ANY, defaultcreds);
		} else {
			mPost = new PostMethod(baseuri + "/ticket/"+ticket_id+"/comment?user="+user+"&pass="+password);	
		}
		//PostMethod mPost = new PostMethod(baseuri + "/ticket/"+ticket_id+"/comment?user="+user+"&pass="+password);	

		String action = "correspond";
		/*
		//avoid RT to re-open ticket due to ticket update when it's in resolved status
		if(ticket.getStatus().equals("resolved")) {
			action = "comment";
		}
		*/

		FilePart attachment_part = new FilePart("attachment_1", attachment.file);
		attachment_part.setContentType(attachment.content_type);//default -- application/octet-stream 
		//TODO - how can I set Content-Disposition: inline; filename="PHP.gif"
		    
		Part[] parts = { 
			new StringPart("content", "id: "+ticket_id+"\nAction: "+action+"\nAttachment: " + attachment.name),
			attachment_part
		};
		mPost.setRequestEntity(new MultipartRequestEntity(parts, mPost.getParams()));

		cl.executeMethod(mPost);
		checkStatusAndGetBody(mPost);		

		//find the attachment ID that we just inserted (assume new attachment will be the last element)
		ArrayList<Attachment> attachments = getAttachments(ticket_id);
		Attachment a = attachments.get(attachments.size()-1);
		return a.id;
	}

	//override this if your RT is doing queue based exchange
	@Override
	public boolean isTicketExchanged(Ticket _ticket, String reverse_assignee) {
		RTTicket ticket = (RTTicket)_ticket;
		logger.debug("the ticket's requester:"+ticket.getRequestor()+" the reverse assignee:" + reverse_assignee);
		logger.debug("the ticket's owner:"+ticket.getOwner()+" the tx user:" + user);
		if(ticket.getRequestor() != null && ticket.getRequestor().equals(reverse_assignee)) {
			return true;
		}
		if(ticket.getOwner().equals(user)) {
			return true;
		}
		return false;
	}
	
}
