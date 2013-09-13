package edu.iu.grid.tx.accessor;

import java.io.File;
import java.io.FileOutputStream;
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
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
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
			HttpClient cl = initHttpClient();
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
	public JiraTicket get(String key) {
		JiraTicket ticket = new JiraTicket();
	
		try {
			//make call
			GetMethod mPost = new GetMethod(baseuri + "/rest/api/2/issue/"+key);
			HttpClient cl = initHttpClient();
			cl.executeMethod(mPost);
			
			//receive reply
			InputStream response = mPost.getResponseBodyAsStream();
			String jsontxt = IOUtils.toString(response);
			JSONObject json = (JSONObject) JSONSerializer.toJSON( jsontxt );
			
			ticket.setKey(json.getString("key")); //this is what people refer to as ticket "name"
			ticket.setTicketID(json.getString("id"));
			
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
			logger.error("exception in JiraAccessor:get() for ticket " + key);
			logger.error(e);
		}

		return ticket;
	}
	
	@Override
	public ArrayList<Attachment> getAttachments(String key) throws Exception {
		ArrayList<Attachment> attachments = new ArrayList<Attachment>();
		try {
			//make call
			GetMethod mPost = new GetMethod(baseuri + "/rest/api/2/issue/"+key+ "?fields=attachment");
			HttpClient cl = initHttpClient();
			cl.executeMethod(mPost);
			
			//receive reply
			InputStream response = mPost.getResponseBodyAsStream();
			String jsontxt = IOUtils.toString(response);
			JSONObject json = (JSONObject) JSONSerializer.toJSON( jsontxt );
			
			JSONObject fields = json.getJSONObject("fields");
			JSONArray attachments_json = fields.getJSONArray("attachment");
			for(int i = 0; i < attachments_json.size(); ++i) {
				JSONObject attachment_json = attachments_json.getJSONObject(i);
				Attachment attachment = new Attachment();
				attachment.content_type = attachment_json.getString("mimeType");
				attachment.file = null; //TODO
				attachment.id = attachment_json.getString("id");
				attachment.name = attachment_json.getString("filename");
				JSONObject author_json = attachment_json.getJSONObject("author");
				attachment.owner = author_json.getString("name");
				attachments.add(attachment);
			}
					
		} catch (HttpException e) {
			logger.error(e);
		} catch (IOException e) {
			logger.error(e);
		} catch (Exception e) {
			logger.error("exception in JiraAccessor:get() for ticket " + key);
			logger.error(e);
		}
			
		return attachments;
	}
	
	
	@Override
	public File downloadAttachment(String ticket_id, Attachment attachment) throws Exception {
		GetMethod get = new GetMethod(baseuri + "/secure/attachment/"+attachment.id+"/"+attachment.name);
		HttpClient cl = initHttpClient();
		cl.executeMethod(get);
		InputStream response = get.getResponseBodyAsStream();
		
		//receive reply
	    File tempfile = File.createTempFile("JIRA."+attachment.id+".", attachment.name);
		FileOutputStream out = new FileOutputStream(tempfile.getAbsolutePath());
		byte[] buf = new byte[1024];
		while (true){
				int size = response.read(buf, 0, 1024);
				if(size == -1) break;
				out.write(buf, 0, size);
		}
		out.close();
		return tempfile;
	}
	
	@Override
	public String uploadAttachment(String ticket_id, Attachment attachment) throws Exception 
	{
		HttpClient cl = initHttpClient();
		PostMethod post = new PostMethod(baseuri + "/rest/api/2/issue/"+ticket_id+"/attachments");
		Part[] parts = new Part[] { new FilePart("file", attachment.file) };
		post.setRequestEntity(new MultipartRequestEntity(parts, post.getParams()));
		post.setRequestHeader("X-Atlassian-Token", "nocheck");
		//post.setRequestHeader("Content-Type", "multipart/form-data");
		cl.executeMethod(post);	
		
		//receive reply
		InputStream response = post.getResponseBodyAsStream();
		String jsontxt = IOUtils.toString(response);
		JSONArray json = (JSONArray) JSONSerializer.toJSON( jsontxt );
		JSONObject attachment_json = json.getJSONObject(0);//grab first one
		String id = attachment_json.getString("id");
		return id;
				
		/*
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
