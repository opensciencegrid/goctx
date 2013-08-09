package edu.iu.grid.tx.accessor;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.apache.axis2.AxisFault;
import org.apache.log4j.Logger;

import edu.iu.grid.tx.ticket.JiraTicket;
import edu.iu.grid.tx.ticket.Ticket;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.io.IOUtils;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;

//http://wiki.bestpractical.com/view/REST

public class JiraAccessor implements TicketAccessor {
	static Logger logger = Logger.getLogger(JiraAccessor.class);
	
	private String baseuri;
	private String user;
	private String password;
	private String default_projectkey; //project key to create new tickets in
	
	public JiraAccessor(String _baseuri, String _user, String _password, String default_projectkey) throws AxisFault {
		this.baseuri = _baseuri;
		this.user = _user;
		this.password = _password;
		this.default_projectkey = default_projectkey;	
	}
	
	private HttpClient initHttpClient() {
		//init httpclient
		HttpClient cl = new HttpClient();
		cl.getParams().setParameter("http.protocol.single-cookie-header", true);
		cl.getParams().setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);
		cl.getHttpConnectionManager().getParams().setConnectionTimeout(1000*10);//10 seconds good?
		
		//set basic auth
		cl.getParams().setAuthenticationPreemptive(true);
		Credentials defaultcreds = new UsernamePasswordCredentials(user, password);
		cl.getState().setCredentials(AuthScope.ANY, defaultcreds);
				
		return cl;
	}
	
	public String create(Ticket jiraticket, String reverse_assignee) 
	{
		JiraTicket ticket = (JiraTicket)jiraticket;
		HttpClient cl = initHttpClient();
		
		try {
			JSONObject fields =  new JSONObject();
			setJsonAs(fields, "project", "key", default_projectkey);
			fields.put("summary", ticket.getSummary());
			setJsonAs(fields, "reporter", "name", reverse_assignee);
			setJsonAs(fields, "priority", "name", ticket.getPriority());
			fields.put("description", ticket.getDescription());
			setJsonAs(fields, "issuetype", "name", ticket.getIssueType());
			
			//TODO - what should I do with status? Maybe transition after it's opened?
			
			JSONObject root = new JSONObject();
			root.put("fields", fields);
			
			PostMethod post = new PostMethod(baseuri + "/rest/api/2/issue");
			StringRequestEntity  body = new StringRequestEntity (root.toString(), "application/json", null);
			post.setRequestEntity(body);
			cl.executeMethod(post);
			
			int code = post.getStatusCode();
			if(code == 201) {
				logger.info("ticket creation successful");
				
				//debug output response -- receive reply
				InputStream response = post.getResponseBodyAsStream();
				if(response == null) {
					logger.error("null response");
				} else {
					String reply = IOUtils.toString(response);
					logger.info(reply);
					JSONObject json = (JSONObject) JSONSerializer.toJSON(reply);
					String key = json.getString("key");
					String id = json.getString("id");
					ticket.setTicketID(id);
					ticket.setKey(key);
					
					update(ticket, null, ticket);
					
					return id; //All good
				}
				
			} else {
				logger.error("ticket creation failed: code:" + code);
				
				//debug output response -- receive reply
				InputStream response = post.getResponseBodyAsStream();
				if(response == null) {
					logger.error("null response");
				} else {
					String reply = IOUtils.toString(response);
					logger.error(reply);
				}
			}
			
		} catch (Exception e) {
			logger.error("exception in JiraAccessor:update() for ticket " + ticket.getTicketID());
			logger.error(e);
		}							
		return null;
	}

	//https://docs.atlassian.com/jira/REST/latest/#idp1362256
	public void update(Ticket _ticket, Date last_synctime, Ticket _old_ticket) {
		
		JiraTicket ticket = (JiraTicket)_ticket;
		JiraTicket old_ticket = (JiraTicket)_old_ticket;
		HttpClient cl = initHttpClient();
				
		//update fields
		try {
			JSONObject update =  new JSONObject();
			setJson(update, "summary", ticket.getSummary());
			setJson(update, "description", ticket.getDescription());
			setJson(update, "priority", "name", ticket.getPriority());
			setJson(update, "issuetype", "name", ticket.getIssueType());
			
			//add comments
			JSONArray comment_array =  new JSONArray();
			JSONObject comment_body = new JSONObject();
			comment_body.put("body", ticket.getCommentsAfter(last_synctime));
			JSONObject comment_add = new JSONObject();
			comment_add.put("add", comment_body);
			comment_array.add(comment_add);
			update.put("comment", comment_array);
			
			JSONObject root = new JSONObject();
			root.put("update", update);
			
			//make update request
			PutMethod put = new PutMethod(baseuri + "/rest/api/2/issue/"+ticket.getTicketID());
			StringRequestEntity  body = new StringRequestEntity (root.toString(), "application/json", null);
			put.setRequestEntity(body);
			cl.executeMethod(put);
			
			int code = put.getStatusCode();
			if(code == 204) {
				logger.debug("ticket update successful");
			} else {
				logger.error("ticket update failed: code:" + code);
				
				//debug output response -- receive reply
				InputStream response = put.getResponseBodyAsStream();
				if(response == null) {
					logger.error("null response");
				} else {
					String reply = IOUtils.toString(response);
					logger.error(reply);
				}
			}
			
		} catch (Exception e) {
			logger.error("exception in JiraAccessor:update() for ticket " + ticket.getTicketID());
			logger.error(e);
		}
		
		//transition to specified state
		massageTransition(old_ticket, ticket);
		if(!old_ticket.getStatus().equals(ticket.getStatus())) {
			logger.debug("Processing state transition from " + old_ticket.getStatus() + " to " + ticket.getStatus());
			
			String transition_id = null;
			try {
				//load transition list
				GetMethod get = new GetMethod(baseuri + "/rest/api/2/issue/"+ticket.getTicketID()+"/transitions?expand=transitions.fields");
				cl.executeMethod(get);
				InputStream response = get.getResponseBodyAsStream();
				String jsontxt = IOUtils.toString(response);
				JSONObject json = (JSONObject) JSONSerializer.toJSON( jsontxt );
				
				//find state that user wants to move to
				JSONArray transitions = json.getJSONArray("transitions");
				for(int i = 0; i < transitions.size(); ++i) {
					JSONObject transition = transitions.getJSONObject(i);
					String transition_name = transition.getString("name");
					String _transition_id = transition.getString("id");
					JSONObject to = transition.getJSONObject("to");
					String to_state_name = to.getString("name");
					if(to_state_name.equals(ticket.getStatus())) {
						logger.debug("Found transition: " + transition_name);
						transition_id = _transition_id;
						break;
					}
				}		
			} catch (Exception e) {
				logger.error("exception in JiraAccessor:update() for ticket " + ticket.getTicketID());
				logger.error(e);
			}
			
			if(transition_id == null) {
				logger.error("Failed to find transition.. skipping state change");
			} else {
				//post state transition request
				try {
					JSONObject json_transition_id =  new JSONObject();
					json_transition_id.put("id", transition_id);		
					JSONObject root = new JSONObject();
					root.put("transition", json_transition_id);
					
					//make update request
					PostMethod post = new PostMethod(baseuri + "/rest/api/2/issue/"+ticket.getTicketID()+"/transitions");
					StringRequestEntity  body = new StringRequestEntity (root.toString(), "application/json", null);
					post.setRequestEntity(body);
					cl.executeMethod(post);
					
					int code = post.getStatusCode();
					if(code == 204) {
						logger.debug("status change successful");
					} else {
						logger.error("status change failed: code:" + code);
						
						//debug output response -- receive reply
						InputStream response = post.getResponseBodyAsStream();
						if(response == null) {
							logger.error("null response");
						} else {
							String reply = IOUtils.toString(response);
							logger.error(reply);
						}
					}
					
				} catch (Exception e) {
					logger.error("exception in JiraAccessor:update() for ticket " + ticket.getTicketID());
					logger.error(e);
				}			
			}
		}
	}
	
	//override this to implement your own transition rule
	private void massageTransition(JiraTicket current_ticket, JiraTicket ticket) {
		//massage state transition a bit..
		if(current_ticket.getStatus().equals("Resolved") || current_ticket.getStatus().equals("Closed")) {
			if(ticket.getStatus().equals("Open")) {
				logger.info("Changing from "+current_ticket.getStatus()+" to "+ticket.getStatus()+": Changing new status to Reopened.");
				ticket.setStatus("Reopened");
			}
		}
		
		if(current_ticket.getStatus().equals("Reopened")) {
			if(ticket.getStatus().equals("Open")) {
				logger.info("Changing from "+current_ticket.getStatus()+" to "+ticket.getStatus()+": Changing new status to Reopened (to keep it Reopened).");
				ticket.setStatus("Reopened");
			}
		}
	}

	private void setJsonAs(JSONObject obj, String key, String as, String value) {
		JSONObject item = new JSONObject();
		item.put(as, value);
		obj.put(key, item);
	}
	
	private void setJson(JSONObject json, String key, String value) {
		JSONObject set = new JSONObject();
		set.put("set", value);
		JSONArray vset = new JSONArray();
		vset.add(set);
		json.put(key, vset);
	}
	
	private void setJson(JSONObject json, String key, String value_type, String value) {
		JSONObject field = new JSONObject();
		field.put(value_type, value);
		JSONObject set = new JSONObject();
		set.put("set", field);
		JSONArray vset = new JSONArray();
		vset.add(set);
		json.put(key, vset);
	}

	//see json sample -- 
	//https://jira.grid.iu.edu/rest/api/2/issue/OO-1
	//https://jira.opensciencegrid.org/rest/api/2/issue/PROJECTTEST-11
	
	//with auth 
	//curl --insecure -D- -u hayashis:mygocpass -X GET -H "Content-Type: application/json" "https://jira.grid.iu.edu/rest/api/2/issue/OO-1"
	public JiraTicket get(String id) {
		JiraTicket ticket = new JiraTicket();
		ticket.setTicketID(id);
	
		HttpClient cl = initHttpClient();
		
		try {
			//make call
			GetMethod mPost = new GetMethod(baseuri + "/rest/api/2/issue/"+ticket.getTicketID());
			cl.executeMethod(mPost);
			
			//receive reply
			InputStream response = mPost.getResponseBodyAsStream();
			String jsontxt = IOUtils.toString(response);
			JSONObject json = (JSONObject) JSONSerializer.toJSON( jsontxt );
			
			ticket.setKey(json.getString("key")); //this is what people refer to as ticket "name"
			
			JSONObject fields = json.getJSONObject("fields");
			
			//load ticket metadata
			ticket.setSummary(fields.getString("summary")); //title
			ticket.setDescription(fields.getString("description")); //ticket description
			
			JSONObject priority = fields.getJSONObject("priority");
			ticket.setPriority(priority.getString("name"));
			
			JSONObject status = fields.getJSONObject("status");
			ticket.setStatus(status.getString("name"));
			
			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
			ticket.setCreated(df.parse(fields.getString("created")));
			ticket.setUpdatetime(df.parse(fields.getString("updated")));
			
			JSONObject reporter = fields.getJSONObject("reporter");
			ticket.setReporterID(reporter.getString("name"));
			
			JSONObject assignee = fields.getJSONObject("assignee");
			ticket.setAssigneeID(assignee.getString("name"));
			
			JSONObject issuetype = fields.getJSONObject("issuetype");
			ticket.setIssueType(issuetype.getString("name"));
			
			//this should be overridden by custom accessor
			ticket.setOriginNote("Jira(Generic) " + ticket.getKey());
			
			//load ticket comments
			JSONObject comment_obj = fields.getJSONObject("comment");
			JSONArray comments = comment_obj.getJSONArray("comments");
			for(int i = 0; i < comments.size(); ++i) {
				JSONObject comment = comments.getJSONObject(i);
				JiraTicket.Comment com = ticket.new Comment();
				JSONObject author = comment.getJSONObject("author");
				com.author_name = author.getString("name");
				com.author_email = author.getString("emailAddress");
				com.body = comment.getString("body");
				com.time = df.parse(comment.getString("created"));//TODO - should I use created, or updated?
				ticket.comments.add(com);
			}
					
		} catch (HttpException e) {
			logger.error(e);
		} catch (IOException e) {
			logger.error(e);
		} catch (Exception e) {
			logger.error("exception in JiraAccessor:get() for ticket " + id);
			logger.error(e);
		}

		return ticket;
	}
	
	@Override
	public ArrayList<Attachment> getAttachments(String id) throws Exception {
		
		ArrayList<Attachment> attachments = new ArrayList<Attachment>();
		/* TODO
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
		*/
		
		return attachments;
	}
	
	/*
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
	*/
	
	@Override
	public File downloadAttachment(String ticket_id, Attachment attachment) throws Exception {
		return null; //TODO
		/*
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
		*/
	}
	
	@Override
	public String uploadAttachment(String ticket_id, Attachment attachment) throws Exception 
	{
		return null; //TODO
		/*
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
		*/
	}

	//override this if your Jira is doing queue based exchange
	@Override
	public boolean isTicketExchanged(Ticket _ticket, String reverse_assignee) {
		JiraTicket ticket = (JiraTicket)_ticket;
		logger.debug("the ticket's reporter:"+ticket.getReporterID()+" the reverse assignee:" + reverse_assignee);
		logger.debug("the ticket's assignee:"+ticket.getAssigneeID()+" the tx user:" + user);
		if(ticket.getReporterID() != null && ticket.getReporterID().equals(reverse_assignee)) {
			return true;
		}
		if(ticket.getAssigneeID().equals(user)) {
			return true;
		}
		return false;
	}

	@Override
	public String parseTicketID(String subject) {
		//Parses : "[JIRA] (OSGPKI-387) OSG/PKI currently does not provide an easy way to update user certificate stored in browser"
		int start = subject.indexOf("(")+1;
		int end = subject.indexOf(")");
		String id = subject.substring(start, end);
		return id;
	}
}
