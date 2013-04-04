package edu.iu.grid.tx.custom;

//we need factory of factory design pattern.. maybe?
import org.apache.log4j.Logger;

import edu.iu.grid.tx.IFactory;
import edu.iu.grid.tx.TicketExchanger;
import edu.iu.grid.tx.accessor.*;
import edu.iu.grid.tx.converter.*;
import goctx.Main;

public class Factory implements IFactory {	
	Logger logger = Logger.getLogger(Factory.class);
		
	@Override
	public TicketExchanger createInstance(String tx_key) throws Exception
	{	
		TicketExchanger tx = new TicketExchanger(tx_key, this);
		
		TicketAccessor source = null;
		TicketAccessor destination = null;
		String reverse_assignee = null;
		
		//Test tx_key just to make sure GOC-TX is running
		if(tx_key.equals("test")) {
			logger.debug("test tx_key received.");
			return tx;
		}
				
		//XSEDE RT FP Production  ////////////////////////////////////////////////////////////////////////
		else if(tx_key.equals("fp71_xsede")) {
			source = new XSEDEAccessor("https://newtickets.xsede.org/REST/1.0/", "goc", "password", "GOC Incoming");
			destination = new XSEDEAccessor("https://newtickets.xsede.org/REST/1.0/", "goc", "password", "GOC Incoming");
			reverse_assignee = "tx+xsede_fp71@tx.grid.iu.edu";
		} else if(tx_key.equals("xsede_fp71")) {
			source = new XSEDEAccessor("https://newtickets.xsede.org/REST/1.0/", "goc", "password", "GOC Incoming");
			destination = new XSEDEAccessor("https://newtickets.xsede.org/REST/1.0/", "goc", "password", "GOC Incoming");
			reverse_assignee = "TX_FP71_XSEDE";
		}
		
		if(source == null || destination == null || reverse_assignee == null) {
			logger.error("Failed to initialize for TX_KEY: " + tx_key + " in Factory");
		}
		
		tx.setSourceAccessor(source);
		tx.setDestinationAccessor(destination);
		tx.setConverter(chooseAndInstantiateConverter(source, destination));
		tx.setReverseAssignee(reverse_assignee);
		
		return tx;
	}
	
	@Override
	public TicketConverter chooseAndInstantiateConverter(TicketAccessor get, TicketAccessor set) throws Exception {
		///////////////////////////////////////////////////////////////////////////////////////////
		//must try custom ones first - or generics one will match
		/*
		if(get instanceof GOCAccessor && set instanceof BNLAccessor) {
			return new GOC2BNLTicketConverter();
		}
		if(get instanceof GGUSSOAPAccessor && set instanceof GOCAccessor) {
			return new GGUS2GOCTicketConverter();
		}
		if(get instanceof VDTAccessor && set instanceof GOCAccessor) {
			return new VDT2GOCTicketConverter();
		}
		if(get instanceof GOCAccessor && set instanceof VDTAccessor) {
			return new GOC2VDTTicketConverter();
		}
		*/
		
		///////////////////////////////////////////////////////////////////////////////////////////
		//generics
		/*
		if(get instanceof GGUSSOAPAccessor && set instanceof FPWebAPIAccessor) {
			return new GGUS2FPTicketConverter();
		}
		if(get instanceof FPWebAPIAccessor && set instanceof GGUSSOAPAccessor) {
			return new FP2GGUSTicketConverter();
		}
		
		if(get instanceof RTAccessor && set instanceof FPWebAPIAccessor) {
			return new RT2FPTicketConverter();
		}
		if(get instanceof FPWebAPIAccessor && set instanceof RTAccessor) {
			return new FP2RTTicketConverter();
		}
		
		if(get instanceof ServiceNowAccessor && set instanceof FPWebAPIAccessor) {
			return new ServiceNow2FPTicketConverter();
		}
		if(get instanceof GOCAccessor && set instanceof ServiceNowAccessor) {
			return new GOC2FNALTicketConverter();
		}
		*/
		
		throw new Exception("Don't know how to convert from " + get.getClass().getName() + " to " + get.getClass().getName());
	}
}
