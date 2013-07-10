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

        // pull from config file
        String tacc_url = tx.getConf().getProperty("tacc_accessor_url");
        String tacc_username = tx.getConf().getProperty("tacc_accessor_username");
        String tacc_password = tx.getConf().getProperty("tacc_accessor_password");

        String xsede_url = tx.getConf().getProperty("xsede_accessor_url");
        String xsede_username = tx.getConf().getProperty("xsede_accessor_username");
        String xsede_password = tx.getConf().getProperty("xsede_accessor_password");

        String nics_url = tx.getConf().getProperty("nics_accessor_url");
        String nics_username = tx.getConf().getProperty("nics_accessor_username");
        String nics_password = tx.getConf().getProperty("nics_accessor_password");

		String goctx_fqdn = tx.getConf().getProperty("goctx_fqdn");

        logger.debug("tx_key: " + tx_key);
        logger.debug("mikedebug090: " + tx_key.getClass().getName());

        // TODO move connection details (user/pass/url) to [Site]Accessor class
        if ( tx_key.equals("tacc_xsede") ) {
            logger.debug("mikedebug100: ");
            source = new taccAccessor(tacc_url, tacc_username, tacc_password, "Support", false);
            destination = new xsedeAccessor(xsede_url, xsede_username, xsede_password, "0-Help", true);
		    reverse_assignee = "tx+xsede_tacc@" + goctx_fqdn;
        } else if ( tx_key.equals("xsede_tacc") ) {
            source = new xsedeAccessor(xsede_url, xsede_username, xsede_password, "0-Help", true);
            destination = new taccAccessor(tacc_url, tacc_username, tacc_password, "Support", false);
		    reverse_assignee = "tx+tacc_xsede@" + goctx_fqdn;
        } else if ( tx_key.equals("xsede_nics") ) {
            source = new xsedeAccessor(xsede_url, xsede_username, xsede_password, "0-Help", true);
            destination = new nicsAccessor(nics_url, nics_username, nics_password, "0-Help", true);
		    reverse_assignee = "tx+nics_xsede@" + goctx_fqdn;
        } else if ( tx_key.equals("nics_xsede") ) {
            source = new nicsAccessor(nics_url, nics_username, nics_password, "0-Help", true);
            destination = new xsedeAccessor(xsede_url, xsede_username, xsede_password, "0-Help", true);
		    reverse_assignee = "tx+xsede_nics@" + goctx_fqdn;
        } else {
            source = null;
            destination = null;
        }

        logger.debug("source: " + source);
        logger.debug("dest: " + destination);
        logger.debug("rev: " + reverse_assignee);

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
	    	
        try { 
            if(get instanceof taccAccessor && set instanceof xsedeAccessor) {
                return new RT2RTTicketConverter();
            }
            if(get instanceof xsedeAccessor && set instanceof taccAccessor) {
                return new RT2RTTicketConverter();
            }

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
            /*
            if(get instanceof GOCAccessor && set instanceof ServiceNowAccessor) {
                return new GOC2FNALTicketConverter();
            }
            */
            return null;
        } catch ( Exception e ) {
			logger.error("Don't know how to convert from " + get.getClass().getName() + " to " + set.getClass().getName());
            return null;
        }
		//throw new Exception("Don't know how to convert from " + get.getClass().getName() + " to " + get.getClass().setName());
	}
    

}
