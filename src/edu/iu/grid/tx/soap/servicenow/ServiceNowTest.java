package edu.iu.grid.tx.soap.servicenow;

import java.math.BigInteger;
import java.rmi.RemoteException;

import edu.iu.grid.tx.soap.servicenow.ServiceNow_incidentStub.Insert;
import edu.iu.grid.tx.soap.servicenow.ServiceNow_incidentStub.InsertResponse;
import edu.iu.grid.tx.soap.servicenow.ServiceNow_incidentStub.GetResponse;
import edu.iu.grid.tx.soap.servicenow.ServiceNow_incidentStub.UpdateResponse;
import edu.iu.grid.tx.soap.servicenow.ServiceNow_sys_journal_fieldStub.GetRecordsResult_type0;

import edu.iu.grid.tx.soap.servicenow.ServiceNow_sys_journal_fieldStub.GetRecordsResponse;

import org.apache.axis2.AxisFault;
import org.apache.axis2.transport.http.HttpTransportProperties;

class Impact {
	static public BigInteger critical = new BigInteger("1");
	static public BigInteger high = new BigInteger("2");
	static public BigInteger medium = new BigInteger("3");
	static public BigInteger low = new BigInteger("4");
}

public class ServiceNowTest {


	public void insert() {
		ServiceNow_incidentStub stub;
		
		try {
			
			//stub = new ServiceNow_incidentStub("https://fermidev.service-now.com/incident.do?SOAP");
			
			stub = new ServiceNow_incidentStub("https://fermidev.service-now.com/incident.do?SOAP");
			HttpTransportProperties.Authenticator basicAuthentication = new HttpTransportProperties.Authenticator();
			basicAuthentication.setUsername("cd-srv-goc-ops");
			basicAuthentication.setPassword("QF4WRqlaq8u79X2bo26Z#fuoq");
			stub._getServiceClient().getOptions().setProperty(org.apache.axis2.transport.http.HTTPConstants.AUTHENTICATE, basicAuthentication);
			stub._getServiceClient().getOptions().setProperty(org.apache.axis2.transport.http.HTTPConstants.CHUNKED, Boolean.FALSE);
			
			Insert insert = new Insert();

			//required fields
			insert.setShort_description("my short description");
			insert.setCategory("General");
			insert.setSubcategory("my sub category");
			insert.setU_customer_categorization("tester");
			insert.setCaller_id("Soichi Hayashi");
			insert.setU_item("what item?");
			insert.setU_operational_category("Information");
			insert.setU_reported_source("Other");
			insert.setU_service("Other");
			insert.setImpact(Impact.low);
			insert.setUrgency(new BigInteger("0"));
			
			//not required fields
			//insert.setPriority(BigInteger.ONE);
			insert.setDescription("is this summary?");

			/*
			ServiceNow_incidentCallbackHandler callback = new ServiceNow_incidentCallbackHandler() {
		           public void receiveResultinsert(ServiceNow_incidentStub.InsertResponse result) {
		        	   System.out.println(result.getSys_id());
		           }
			};
			*/
			InsertResponse resp = stub.insert(insert);
			System.out.println("number: " + resp.getNumber());
			System.out.println("sys_id: " + resp.getSys_id());
			//TODO - check the response
		} catch (AxisFault e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void get() {
		//String id = "INC000000103211";

		//String number = "INC000000103212";
		String id = "5fe9339829451c008638a6dc41528b27";
		
		ServiceNow_incidentStub stub;
		try {
			stub = new ServiceNow_incidentStub("https://fermidev.service-now.com/incident.do?SOAP");
			HttpTransportProperties.Authenticator basicAuthentication = new HttpTransportProperties.Authenticator();
			basicAuthentication.setUsername("cd-srv-goc-ops");
			basicAuthentication.setPassword("QF4WRqlaq8u79X2bo26Z#fuoq");
			stub._getServiceClient().getOptions().setProperty(org.apache.axis2.transport.http.HTTPConstants.AUTHENTICATE, basicAuthentication);
			stub._getServiceClient().getOptions().setProperty(org.apache.axis2.transport.http.HTTPConstants.CHUNKED, Boolean.FALSE);
			
			ServiceNow_incidentStub.Get get = new ServiceNow_incidentStub.Get();
			get.setSys_id(id);
			
			GetResponse resp = stub.get(get);
			System.out.println("short desc(title?)" + resp.getShort_description());
			System.out.println("desc(first desc?)" + resp.getDescription());
			System.out.println("state(status)" + resp.getState());
			System.out.println("incident state " + resp.getIncident_state());
			System.out.println("update time" + resp.getSys_updated_on());
		} catch (AxisFault e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void get_comments() {
		//String number = "INC000000103212";
		String id = "5fe9339829451c008638a6dc41528b27";
		
		ServiceNow_sys_journal_fieldStub stub;
		try {
			stub = new ServiceNow_sys_journal_fieldStub("https://fermidev.service-now.com/sys_journal_field.do?SOAP");
			HttpTransportProperties.Authenticator basicAuthentication = new HttpTransportProperties.Authenticator();
			basicAuthentication.setUsername("cd-srv-goc-ops");
			basicAuthentication.setPassword("QF4WRqlaq8u79X2bo26Z#fuoq");
			stub._getServiceClient().getOptions().setProperty(org.apache.axis2.transport.http.HTTPConstants.AUTHENTICATE, basicAuthentication);
			stub._getServiceClient().getOptions().setProperty(org.apache.axis2.transport.http.HTTPConstants.CHUNKED, Boolean.FALSE);
			
			ServiceNow_sys_journal_fieldStub.GetRecords get = new ServiceNow_sys_journal_fieldStub.GetRecords();
			get.setElement_id(id);
			get.setElement("comments");
			
			GetRecordsResponse resp = stub.getRecords(get);
			for(GetRecordsResult_type0 rec : resp.getGetRecordsResult()) {
				System.out.println(rec.getValue());
				System.out.println("by: " + rec.getSys_created_by());
				System.out.println("on: " + rec.getSys_created_on());
				System.out.println(rec.getName());
			}
				
		} catch (AxisFault e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
	
	public void update() {
		String id = "5fe9339829451c008638a6dc41528b27"; //INC000000103212
		
		ServiceNow_incidentStub stub;
		try {
			stub = new ServiceNow_incidentStub("https://fermidev.service-now.com/incident.do?SOAP");
			HttpTransportProperties.Authenticator basicAuthentication = new HttpTransportProperties.Authenticator();
			basicAuthentication.setUsername("cd-srv-goc-ops");
			basicAuthentication.setPassword("QF4WRqlaq8u79X2bo26Z#fuoq");
			stub._getServiceClient().getOptions().setProperty(org.apache.axis2.transport.http.HTTPConstants.AUTHENTICATE, basicAuthentication);
			stub._getServiceClient().getOptions().setProperty(org.apache.axis2.transport.http.HTTPConstants.CHUNKED, Boolean.FALSE);
			
			ServiceNow_incidentStub.Update update = new ServiceNow_incidentStub.Update();
			update.setSys_id(id);
			update.setComments("here is my description");
			//update.setAssignment_group("CMS-Grid-Services");
			update.setAssignment_group("CMS-Tier1-LPC");

			//required fields (I am guess I need to first "get" ticket, then pass the same values?)
			update.setShort_description("my short description");
			update.setCategory("General");
			update.setSubcategory("my sub category");
			update.setU_customer_categorization("tester");
			update.setCaller_id("Soichi Hayashi");
			update.setU_item("what item?");
			update.setU_operational_category("Information");
			update.setU_reported_source("Other");
			update.setU_service("Other");
			update.setImpact(Impact.low);
			update.setUrgency(new BigInteger("0"));
			
			UpdateResponse resp = stub.update(update);
			//TODO - what can I do with resp?
		} catch (AxisFault e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
	}
	
	/*
	public void getkey() {
		try {
			ServiceNow_incidentStub stub = new ServiceNow_incidentStub();
			ServiceNowStub.GetKeys getInc = new ServiceNowStub.GetKeys();
			ServiceNowStub.GetKeysResponse resp = new ServiceNowStub.GetKeysResponse();
			
			getInc.setActive(true);
			getInc.setCategory("hardware");
			
			stub._getServiceClient().getOptions().setProperty(org.apache.axis2.transport.http.HTTPConstants.CHUNKED, Boolean.FALSE);
			
			resp = proxy.getKeys(getInc);
			
			String[] keys = resp.getSys_id();
			
			System.out.println("Key: " + keys[0]);
		} catch (Exception e) {
			System.out.println(e.toString());
		}
	}
	*/

}
