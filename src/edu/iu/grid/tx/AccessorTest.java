package edu.iu.grid.tx;

import java.io.File;
import java.util.ArrayList;

import org.apache.axis2.AxisFault;
import org.apache.log4j.Logger;

import edu.iu.grid.tx.accessor.Attachment;
import edu.iu.grid.tx.accessor.GGUSSOAPAccessor;
import edu.iu.grid.tx.accessor.RTAccessor;
import edu.iu.grid.tx.accessor.ServiceNowAccessor;
import edu.iu.grid.tx.accessor.TicketAccessor;
import edu.iu.grid.tx.converter.TicketConverter;
import edu.iu.grid.tx.custom.Factory;
import edu.iu.grid.tx.custom.GOCAccessor;
import edu.iu.grid.tx.ticket.FPTicket;
import edu.iu.grid.tx.ticket.GGUSTicket;
import edu.iu.grid.tx.ticket.RTTicket;
import edu.iu.grid.tx.ticket.RemedyTicket;
import edu.iu.grid.tx.ticket.FPTicket.DescriptionEntry;
import edu.iu.grid.tx.ticket.GGUSTicket.PastHistoryEntry;
import edu.iu.grid.tx.ticket.ServiceNowTicket;

public class AccessorTest {
	
	static Logger logger = Logger.getLogger(AccessorTest.class);

	public static void main(String[] args) {
		System.setProperty("javax.net.ssl.trustStore", "jssecacerts");
		System.setProperty("javax.net.ssl.trustStorePassword", "changeit");

		//RT();
		//RT_Attachment();
		//GGUS();
		//GGUS_Attachment();
		//FP();
		//REMEDY();
		//VDT();
		ServiceNow();
		//XSEDE();

	}
	
	//RT restful interface -- http://wiki.bestpractical.com/view/REST
	public static void RT() {

		try {
			TicketExchanger tx = Factory.createInstance("fp114_rtgocdev");//ggus test
			//GGUSTicket ggus = (RTTicket) tx.getDestination().get("2");
			RTAccessor access = (RTAccessor)tx.getDestination();
			RTTicket ticket = access.get("2");
			
			////////////////////////////////////////////////////////////////////////////////////
			//test listing 
			ArrayList<Attachment> attachments = access.getAttachments("2");
			for(Attachment attachment : attachments) {
				System.out.println("attachment: " + attachment.name + " id: " + attachment.id);
			}
			
			////////////////////////////////////////////////////////////////////////////////////
			//download
			Attachment a = attachments.get(0);
			File file = access.downloadAttachment("2", a);
			System.out.println("downloaded: " + file.getAbsolutePath());
			
			////////////////////////////////////////////////////////////////////////////////////
			//upload
			Attachment newa = new Attachment();
			newa.name = "test.png";
			newa.file = new File("C:/Users/soichi/tmp/test.png");
			String newid = access.uploadAttachment("2", newa);
			System.out.println("inserted attachment id: " + newid);
			
		} catch (AxisFault e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void XSEDE() {

		try {
			TicketExchanger tx = Factory.createInstance("fp114_xsedetest");
			RTAccessor access = (RTAccessor)tx.getDestination();
			RTTicket ticket = access.get("2");
			
			/*
			////////////////////////////////////////////////////////////////////////////////////
			//test listing 
			ArrayList<Attachment> attachments = access.getAttachments("2");
			for(Attachment attachment : attachments) {
				System.out.println("attachment: " + attachment.name + " id: " + attachment.id);
			}
			
			////////////////////////////////////////////////////////////////////////////////////
			//download
			Attachment a = attachments.get(0);
			File file = access.downloadAttachment("2", a);
			System.out.println("downloaded: " + file.getAbsolutePath());
			
			////////////////////////////////////////////////////////////////////////////////////
			//upload
			Attachment newa = new Attachment();
			newa.name = "test.png";
			newa.file = new File("C:/Users/soichi/tmp/test.png");
			String newid = access.uploadAttachment("2", newa);
			System.out.println("inserted attachment id: " + newid);
			*/
			
			System.out.println(ticket.getSubject());
		} catch (AxisFault e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	public static void RT_Attachment() {
		try {
			TicketExchanger tx = Factory.createInstance("fp114_rtbnlprod");//ggus test
			//GGUSTicket ggus = (RTTicket) tx.getDestination().get("2");
			RTAccessor access = (RTAccessor)tx.getDestination();
			RTTicket ticket = access.get("2");
			
			//list attachments
			ArrayList<Attachment> attachments = access.getAttachments("21658");
			for(Attachment attachment : attachments) {
				System.out.println("attachment: " + attachment.name + " id: " + attachment.id);
			}
			
			//upload
			Attachment newa = new Attachment();
			newa.name = "test.png";
			newa.file = new File("C:/trash/test.png");
			String newid = access.uploadAttachment("21658", newa);
			System.out.println("inserted attachment id: " + newid);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
	
	public static void VDT() {
		try {
			TicketExchanger tx = Factory.createInstance("fp114_rtvdttest");
			TicketAccessor rt = tx.getDestination();
			RTTicket ticket = (RTTicket) rt.get("7856");
			System.out.println(ticket.getSubject());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
	}
	
	public static void GGUS() {
		
		try {
			
			System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.SimpleLog");
			System.setProperty("org.apache.commons.logging.simplelog.showdatetime", "true");
			System.setProperty("org.apache.commons.logging.simplelog.log.httpclient.wire.header", "debug");
			System.setProperty("org.apache.commons.logging.simplelog.log.org.apache.commons.httpclient", "debug");
			
			
			TicketExchanger tx = Factory.createInstance("fp114_ggus1");
			String id = "49616"; //ggus test ticket
			GGUSTicket ggus = (GGUSTicket) tx.getDestination().get(id);
			ArrayList<PastHistoryEntry> history = ggus.getPastHistory();
			
			/*
			FPTicket ts = (FPTicket)tx.getSource().get("428");
			TicketConverter tc = Factory.chooseAndInstantiateConverter(tx.getSource(), tx.getDestination());
			GGUSTicket td = (GGUSTicket) tc.convert(ts);
			System.out.println(ts.getMetadata("SUBMITTER_DN"));
			System.out.println(td.getSubmitterDN());
			*/

			TicketConverter tc = Factory.chooseAndInstantiateConverter(tx.getDestination(), tx.getSource());
			FPTicket fp = (FPTicket) tc.convert(ggus);
			System.out.println(fp.getMetadata().toString());
			
			/*
			TicketExchanger tx = Factory.createInstance("fp114_ggus1");//ggus test
  			String id = "48798";//ggus test ticket	
			GGUSTicket ggus = (GGUSTicket) tx.getDestination().get(id);
			ArrayList<PastHistoryEntry> history = ggus.getPastHistory();
			*/
			
			/*
			GGUSSOAPAccessor access = (GGUSSOAPAccessor)tx.getDestination();
			///////////////////////////////////////////////////////////////////////////////////////
			// enumerate attachments
			ArrayList<Attachment> attachments = access.getAttachments(id);
			for(Attachment attachment : attachments) {
				System.out.println("attachment: " + attachment.name);
				System.out.println("id: " + attachment.id);
			}
			attachments.get(2).file = access.downloadAttachment("49052", attachments.get(2));
			GOCAccessor gaccess = (GOCAccessor)tx.getSource();
			gaccess.uploadAttachment("521", attachments.get(2));

			
			 
			///////////////////////////////////////////////////////////////////////////////////////
			// get attachments (dump to file)
			// -- localGAT_Attachment_Data is set to null
			Attachment aget = new Attachment();
			aget.id = "ATT46594";
			aget.name = "something.pdf";
			File file = access.downloadAttachment(null, aget);
			System.out.println("downloaded to : "+ file.getAbsolutePath());
			
			///////////////////////////////////////////////////////////////////////////////////////
			// add attachments
			Attachment attachment = new Attachment();
			attachment.name = "test.png";
			attachment.file = new File("C:/trash/test.png");
			attachment.owner = "Soichi Hayashi";
			String aid = access.uploadAttachment(id, attachment);
			System.out.println("added attachment id: " + aid);
			
			*/
			
			//System.out.println(es.size());
			//System.out.println(ts.getTicketID());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
	
	public static void GGUS_Attachment() {
		try {
			System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.SimpleLog");
			System.setProperty("org.apache.commons.logging.simplelog.showdatetime", "true");
			System.setProperty("org.apache.commons.logging.simplelog.log.httpclient.wire.header", "debug");
			System.setProperty("org.apache.commons.logging.simplelog.log.org.apache.commons.httpclient", "debug");
			
			
			TicketExchanger tx = Factory.createInstance("fp114_ggus2");//ggus prod
			String id = "49052"; //ggus test ticket
			GGUSTicket ggus = (GGUSTicket) tx.getDestination().get(id);
			
			//list attachments
			GGUSSOAPAccessor access = (GGUSSOAPAccessor)tx.getDestination();
			ArrayList<Attachment> attachments = access.getAttachments(id);
			for(Attachment attachment : attachments) {
				System.out.println("attachment: " + attachment.name);
				System.out.println("id: " + attachment.id);
			}
			
			//add attachments
			Attachment attachment = new Attachment();
			attachment.name = "test.png";
			attachment.file = new File("C:/trash/test.png");
			attachment.owner = "Soichi Hayashi";
			String aid = access.uploadAttachment(id, attachment);
			System.out.println("added attachment id: " + aid);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
	
	public static void FP() {
		try {
			
			//TicketExchanger tx = Factory.createInstance("fpsoichi_rtgocdev");
			//TicketExchanger tx = Factory.createInstance("fp71_ggus2"); String id = "11713"
			TicketExchanger tx = Factory.createInstance("fp114_ggus1"); String id = "479";
			GOCAccessor access = (GOCAccessor)tx.getSource();
			
			FPTicket ticket = (FPTicket) tx.getSource().get(id);
			
			///////////////////////////////////////////////////////////////////////////////////////
			// list attachment
			ArrayList<Attachment> attachments = access.getAttachments(id);
			for(Attachment attachment : attachments) {
				System.out.println("attachment id: " + attachment.id);
			}
			
			///////////////////////////////////////////////////////////////////////////////////////
			// get attachments (just download from the public url)
			Attachment aget = attachments.get(3);
			File file = access.downloadAttachment(id, aget);
			System.out.println("downloaded file: " + file.getAbsolutePath());

			///////////////////////////////////////////////////////////////////////////////////////
			// add attachments
			Attachment aput = new Attachment();
			aput.file = new File("C:/Users/soichi/tmp/test.gif");
			String aid = access.uploadAttachment(ticket.getTicketID(), aput);
			System.out.println("new att_id: " + aid);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
	}
	public static void REMEDY() {
		try {
			System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.SimpleLog");
			System.setProperty("org.apache.commons.logging.simplelog.showdatetime", "true");
			System.setProperty("org.apache.commons.logging.simplelog.log.httpclient.wire.header", "debug");
			System.setProperty("org.apache.commons.logging.simplelog.log.org.apache.commons.httpclient", "debug");
			
			
			TicketExchanger tx = Factory.createInstance("fp114_remedyfnaldev");
			RemedyTicket ticket = (RemedyTicket) tx.getDestination().get("INC000000026425");

			//do something with ticket...
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
	}

	public static String sn_parseTicketID(String subject) {
		//Parses : " Incident INC000000103312 opened -- Test Ticket"
		int pos = subject.indexOf("INC0");
		int pos2 = subject.indexOf(" ", pos+1);
		if(pos2 == -1) pos2 = subject.length();
		String id = subject.substring(pos, pos2);
		return id;
	}
	
	public static void ServiceNow() {
		//ServiceNowTest test = new ServiceNowTest();
		//test.insert();
		//test.get();
		//test.update();
		//test.get_comments();
		//System.out.println(sn_parseTicketID("Incident INC000000103312 opened -- Test Ticket"));
		
		try {

			TicketExchanger tx = Factory.createInstance("fermitest_fp114");
			ServiceNowAccessor access = (ServiceNowAccessor)tx.getSource();
						
			String ticket_id = access.parseTicketID("Incident INC000000164835: opened -- Test Ticket");	
		
			//Convert ServiceNow to FP
			//get servicenow ticket
			ServiceNowTicket ticket = (ServiceNowTicket) tx.getSource().get(ticket_id);
			System.out.println("short desc: " + ticket.getShortDescription());
			System.out.println("update time: " + ticket.getUpdatetime());
			System.out.println("comments: " + ticket.aggregateCommentsAfter(null));
			TicketConverter tc = Factory.chooseAndInstantiateConverter(tx.getSource(), tx.getDestination());
			FPTicket td = (FPTicket) tc.convert(ticket);
			System.out.println("first desc: " + td.getFirstDescription());
			for(DescriptionEntry desc : td.getDescriptions()) {
				System.out.println(desc.content);
				System.out.println(desc.name);
				System.out.println(desc.time.toLocaleString());
			}
			
			
			/*
			//Convert FP to ServiceNow
			TicketExchanger tx = Factory.createInstance("fp71_fermi");
			FPTicket ticket = (FPTicket) tx.getSource().get("11534"); 
			TicketConverter tc = Factory.chooseAndInstantiateConverter(tx.getSource(), tx.getDestination());
			ServiceNowTicket st = (ServiceNowTicket) tc.convert(ticket);
			System.out.println(st.getAssignmentGroup());
			*/
			
			/*
			//Convert FP to ServiceNow
			TicketConverter tc = Factory.chooseAndInstantiateConverter(tx.getDestination(), tx.getSource());
			FPTicket ticket = (FPTicket) tx.getDestination().get("438");
			ServiceNowTicket td = (ServiceNowTicket) tc.convert(ticket);
			System.out.println("short description" + td.getShortDescription());
			*/
			
			
			///////////////////////////////////////////////////////////////////////////////////////
			// enumerate attachments
			//733e0f1c7002100086389504e7a14074
			ArrayList<Attachment> attachments = access.getAttachments(ticket_id);
			for(Attachment attachment : attachments) {
				System.out.println("attachment: " + attachment.name);
				System.out.println("id: " + attachment.id);
			}
			
			
			///////////////////////////////////////////////////////////////////////////////////////
			// get attachments (just download from the public url)
			Attachment aget = attachments.get(0);
			File file = access.downloadAttachment(null, aget);
			System.out.println("downloaded file: " + file.getAbsolutePath());
			
			//TicketExchanger exchanger = new TicketExchanger("fermitest_fp114");
			//exchanger.processAttachments(tx.getSource(), "INC000000216219", tx.getDestination(), "479");
			
			/*
			///////////////////////////////////////////////////////////////////////////////////////
			// add attachments
			Attachment aput = new Attachment();
			aput.name = "test.png";
			aput.content_type = "image/png";
			aput.file = new File("C:/trash/test.png");
			String id = access.uploadAttachment(ticket_id, aput);
			System.out.println("new id: " + id);
			*/
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
	}

}
