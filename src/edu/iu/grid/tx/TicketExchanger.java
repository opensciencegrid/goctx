package edu.iu.grid.tx;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Properties;

import javax.activation.MimetypesFileTypeMap;

import org.apache.log4j.Logger;

import edu.iu.grid.tx.accessor.Attachment;
import edu.iu.grid.tx.accessor.TicketAccessor;
import edu.iu.grid.tx.converter.TicketConverter;
import edu.iu.grid.tx.ticket.Ticket;

public class TicketExchanger {
	
	static String version = "1.29";
	
	static Logger logger = Logger.getLogger(TicketExchanger.class);
	
	private String tx_id;
	public String getTXID() { return tx_id; }
	private IFactory factory;
	
	private static Properties conf = null;
	public static Properties getConf() {
		if(conf != null) return conf;
		
		try {
			conf = new Properties();
			conf.load(new FileInputStream("goctx.conf"));
			return conf;
		} catch (Exception e1) {
			logger.error("Failed to load goctx.conf: ", e1);
			System.exit(1);
		}
		return null;
	}
	
	private String source_ticket_id;
	public void setTicketID(String id) { source_ticket_id = id; }
	public void setTicketIDFromSubject(String subject) {
		source_ticket_id = parseSourceTicketID(subject);
	}
	
	SyncModel model;
	
	public TicketExchanger(String tx_id, IFactory factory) throws SQLException {
		this.tx_id = tx_id;
		this.factory = factory;
		
		this.model = new SyncModel();
		
		logger.debug("GOCTX TicketExchanger Version " + version);
	}
	
	static public TicketExchanger createInstanceFromEmail(BufferedReader reader, IFactory factory) throws Exception {
		String line;
		HashMap<String, String> headers = new HashMap<String, String>();
		
		//Parse email
		Boolean header = true;
		try {
			line = reader.readLine(); //ignore the first line
			line = reader.readLine();
			while ( line!=null ) {
				line = line.trim();
				logger.debug("\t"+line);
				if(header) {
					  //detect header end
					  if(line.length() == 0) {
						  header = false;
						  continue;
					  }
					  //process header line
					  int pos = line.indexOf(':');
					  if(pos != -1) {
						  headers.put(line.substring(0, pos), line.substring(pos+1,line.length()).trim());
					  }
				}
				line = reader.readLine();
			}
		} catch (IOException e) {
			logger.error("Failed to parse email", e);
		}
		
		//Get instance key from Delivered-To: 
		String to = headers.get("Delivered-To");
		if(to == null) {
			throw new Exception("Couldn't find Delivered-To field.");
		}
		String address = to.split("@")[0];
		String []extension = address.split("\\+");
		String tx_key = extension[1];
		if(extension.length == 1) {
			throw new Exception("Delivered-To address [" + to + "] does not contain extension.");
		} else {
			//create instance and set ticket id
			TicketExchanger tx = factory.createInstance(tx_key);
			String subject = headers.get("Subject");
			tx.setTicketIDFromSubject(subject);
			return tx;
		}
	}
	
	private TicketAccessor source;
	public void setSourceAccessor(TicketAccessor s) { source = s; };
	public TicketAccessor getSource() { return source; }
	
	private TicketAccessor destination;
	public void setDestinationAccessor(TicketAccessor s) { destination = s; };	
	public TicketAccessor getDestination() { return destination; }
	
	private TicketConverter converter;
	public void setConverter(TicketConverter c) { converter = c; };
	
	private String reverse_assignee;
	public void setReverseAssignee(String id) { reverse_assignee = id; }
	
	String getReverseTXID() {
		String insts[] = tx_id.split("_");
		//String reverse;
		if(insts.length == 2) {
			return insts[1] + "_" + insts[0];
		} 
		return  null;
	}
	
	public String parseSourceTicketID(String subject) {
		logger.debug("Parsing Source Ticket ID from Email Header: " + subject);
		if(source != null) {
			return source.parseTicketID(subject);
		} 
		logger.debug("source ticketing system is not set.. this is probably a test");
		return null;
	}
	
	public void run()
	{	
		if(tx_id.equals("test")) {
			logger.debug("test tx_id received");

			//anything nice to test?
			return;
		}
		
		logger.debug("Start Processing TX: " + tx_id);
		logger.debug("Source Accessor: " + source.getClass().getName());
		logger.debug("Destination Accessor: " + destination.getClass().getName());
		logger.debug("Source Ticket ID: " + source_ticket_id);

		Ticket source_ticket;
		Ticket dest_ticket;
		try {
			logger.debug("Retrieving source ticket");
			source_ticket = source.get(source_ticket_id);
			source_ticket_id = source_ticket.getTicketID(); //some accessor reset ticket id (like jira) from "key" to the real internal ticket id
			
			logger.debug("Converting to the destination ticket using " +converter.getClass().getName());
			dest_ticket = converter.convert(source_ticket);

			String dest_id;
			try {
				dest_id = model.getDestIDBySourceID(source_ticket_id, tx_id);
				if(dest_id == null) {
					processNewTicket(source_ticket_id, source_ticket, dest_ticket);
				} else {
					processUpdate(source_ticket_id, dest_id, source_ticket, dest_ticket);
				}				
			} catch (SQLException e) {
				logger.error("Failed to find the source ticket id from sync table: " + source_ticket_id, e);
			}
			
		} catch (Exception e) {
			logger.error("Failed to get / convert source ticket :" + source_ticket_id, e);
		}
	}
	
	private void processNewTicket(String source_ticket_id, Ticket source_ticket, Ticket dest_ticket)
	{
		logger.debug("This is a new TX - create destination ticket and setup syn");
		
		String dest_id;
		try {
			
			logger.debug("Creating new ticket");
			dest_id = destination.create(dest_ticket, reverse_assignee);
			logger.debug("Created Destination Ticket with ID: " + dest_id);
			
			try {
				//synchronize attachments from source to destination
				processAttachments(tx_id, getReverseTXID(), source, source_ticket_id, destination, dest_id);
			} catch(Exception e) {
				logger.error("failed to synchronize attachments from source to dest");
			}
						
			Date ticket_timestamp = new Date();
			try {
				ticket_timestamp = destination.get(dest_id).getUpdatetime();
				logger.debug("Ticket " + dest_id + " has timestap of " + ticket_timestamp.toString());
			} catch(Exception e) {
				logger.error("Failed to obtain timestamp from the new ticket - using server local timestamp instead",e);
			}
						
			//I need to try inserting syn entry - with or without the correct ticket timestamp
			//or we will run into infinite ticket creation issue..
			try {
				logger.debug("Inserting ticket sync records");	
				Date source_ticket_timestamp = source_ticket.getUpdatetime();
				model.insert(source_ticket_id, dest_id, tx_id, source_ticket_timestamp);
				//on reverse TX, set it's source timestamp to be the one that I just created and looked up.
				model.insert(dest_id, source_ticket_id, getReverseTXID(), ticket_timestamp);
			} catch(SQLException e) {
				logger.error("Failed to insert syn information for the new ticket ID: " + dest_id, e);
			}
		
			
		} catch (Exception e) {
			logger.error("Failed to create new ticket..",e);
		}
	}
	
	private void processUpdate(String source_ticket_id, String dest_id, Ticket source_ticket, Ticket dest_ticket)
	{
		logger.debug("This is an existing TX - preparing to update dest ticket");
		
		//we should only do update if source ticket is *actually* newer than last recorded TX time.
		//if it's same timestamp, that probably means that this is just a notification loop
		dest_ticket.setTicketID(dest_id);
		Date ticket_timestamp = source_ticket.getUpdatetime();
		try {
			Date sync_timestamp = model.getSourceTimestamp(source_ticket_id, tx_id);
			logger.debug("Current Source Ticket Timestamp:" + ticket_timestamp.toGMTString());
			logger.debug("Last Sync Timestamp:" + sync_timestamp.toGMTString());
			if(sync_timestamp.compareTo(ticket_timestamp) < 0) {
				logger.debug("Source Ticket timestamp is later than the sync timestamp - proceeding");
				
				Date dest_sync_timestamp = model.getSourceTimestamp(dest_id, getReverseTXID());
				Ticket dest_old_ticket = destination.get(dest_id);
				
				//make sure destination still has reverse assignee assigned to it (JIRA GOCTX-26)
				if(destination.isTicketExchanged(dest_old_ticket, reverse_assignee)) {
				
					//make sure destination ticket hasn't been updated since last sync time
					if(dest_sync_timestamp.compareTo(dest_old_ticket.getUpdatetime()) < 0) {
						logger.debug("Destination timestamp is later than sync timestamp - we need to update the source ticket as well as dest");
						
						TicketConverter reverse_converter = factory.chooseAndInstantiateConverter(destination, source);
						Ticket source_new_ticket = reverse_converter.convert(dest_old_ticket);
						source_new_ticket.setTicketID(source_ticket_id);
						
						try {
							//synchronize attachments from dest to source (reverse)
							processAttachments(getReverseTXID(), tx_id, destination, dest_id, source, source_ticket_id);
						} catch(Exception e) {
							logger.error("failed to synchronize attachments from dest to source (reverse)");
							//TODO - somehow add note to the source ticket
						}
						
						//this update is mainly to update 
						source.update(source_new_ticket, dest_sync_timestamp, source_ticket);
						logger.debug("Updateted source ticket..");
						
						ticket_timestamp = source.get(source_ticket_id).getUpdatetime();
						logger.debug("source ticket's timestamp is now " + ticket_timestamp.toGMTString());
						
						//we might have just re-done the status updates.. we can solve this by simply doing the update again
						//with no description update by using the current timestamp with original source_ticket
						//TODO - I am not sure if I can really clear out ...
						source.update(source_ticket, ticket_timestamp, source_ticket);
						
					}
					
					try {
						//synchronize attachments from source to destination
						processAttachments(tx_id, getReverseTXID(), source, source_ticket_id, destination, dest_id);
					} catch(Exception e) {
						logger.error("failed to synchronize attachments from source to dest");
						//TODO - somehow add note to the destination ticket
					}
					
					//update, then get new ticket update timestamp and update sync table
					destination.update(dest_ticket, sync_timestamp, dest_old_ticket);
					logger.debug("updated the destination ticket ID: " + dest_id);
					
					model.updateSourceTimestamp(source_ticket_id, tx_id, ticket_timestamp);
					logger.debug("updated source ticket sync time stamp :" + ticket_timestamp.toGMTString());
					
					Date dest_ticket_timestamp = destination.get(dest_id).getUpdatetime();
					model.updateSourceTimestamp(dest_id, getReverseTXID(), dest_ticket_timestamp);
					logger.debug("retreived and updated destination ticket sync time stamp :" + dest_ticket_timestamp.toGMTString());
				} else {
					logger.error("on TX_ID:"+tx_id+" .. Can't exchange source ticket " +source_ticket_id+" to the destination ticket "+dest_id+" which is no longer exchanged to source.. aborting exchange. reverse_assignee:"+reverse_assignee);
				}
			
			} else {
				logger.info("Source timestamp hasn't been updated.. This is most likely just a update notification - ignoring");
			}
		} catch (Exception e) {
			logger.error("Failed to update ticket",e);
		}
	}
	
	//synchronize all attachments in "from" accessor to "to" accessor
	private void processAttachments(String txid, String rev_txid, TicketAccessor from, String from_id, TicketAccessor to, String to_id) {
		try {
			ArrayList<Attachment> attachments = from.getAttachments(from_id);			
			logger.debug("processing attachment. count:" + attachments.size() + " from:" + from_id + " to:" + to_id);
			for(Attachment attachment : attachments) {
				if(!model.hasAttachmentRecord(txid, from_id, to_id, attachment.id)) {
					
					attachment.file = from.downloadAttachment(from_id, attachment);
					if(attachment.content_type == null) {
						//guess content from the file
						attachment.content_type = new MimetypesFileTypeMap().getContentType(attachment.file);
					}
					String new_attachment_id = to.uploadAttachment(to_id, attachment);
					
					//if upload success, or non-IO-error, then insert 2 new records (both direction)
					//(if IO error, then don't insert them - and let it retry)
					if(new_attachment_id != null) {
						logger.info("Uploaded attachment ID: " + attachment.id + "(" + attachment.name + ") as " + new_attachment_id);
						model.insertAttachmentRecord(txid, from_id, to_id, attachment.id);
						model.insertAttachmentRecord(rev_txid, to_id, from_id, new_attachment_id);
					}
				}
			}
		} catch (Exception e) {
			//IO-error, let's try again sometime later.
			logger.error("Failed to process attachments: tx_id:"+txid+" from:"+from_id+" to:"+to_id, e);
		}
	}
	
}
