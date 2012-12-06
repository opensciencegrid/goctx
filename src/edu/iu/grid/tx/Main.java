package edu.iu.grid.tx;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.channels.FileLock;
import java.util.HashMap;
import java.util.Properties;

import org.apache.log4j.Logger;

import edu.iu.grid.tx.custom.Factory;

public class Main {
	
	static String version = "1.29";
	static Logger logger = Logger.getLogger(Main.class);

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
	
	public static void main(String[] args) 
	{
		System.setProperty("javax.net.ssl.trustStore", "jssecacerts");
		System.setProperty("javax.net.ssl.trustStorePassword", "changeit");
		
		//obtain file lock
		String lock_filename = getConf().getProperty("goctx_filelock_path");
		FileLock fl = null;
		try {
			FileOutputStream fos = new FileOutputStream(lock_filename);
			do {
				fl = fos.getChannel().tryLock();
				if(fl == null) {
					logger.info("Failed to obtain file lock -- waiting for 5 seconds");
					Thread.sleep(5000);
				}
			} while(fl == null);
			
		} catch (Exception e) {
			logger.error("Failed to obtain lock: ", e);
			System.exit(1);
		}
		
		//ok.. Go!
		try {
			logger.debug("Starting GOCTX - version " + version);
			Main main = new Main();			
			main.run(args);
		} catch (Exception e) {
			logger.error("Uncaught exception: ", e);
		}
		
		//release the file lock
		try {
			fl.release();
		} catch (IOException e) {
			logger.error("Failed to release lock: ", e);
		}
	}

	void run(String[] args) throws Exception 
	{
		//Load email content from 
		InputStreamReader isr;
		TicketExchanger tx;

		if(args.length == 1)  {
			//Run it form the command line
			String command = args[0];
			if(command.equals("override")) {
				logger.info("Overriding");
				
				//override for testing
				tx = Factory.createInstance("fp114_rtgocdev");
				tx.setTicketID("80");
				tx.run();
			}
		} else {
			//Email trigger
			isr = new InputStreamReader(System.in);
			BufferedReader reader = new BufferedReader(isr);
			tx = constructTXFromEmail(reader);
			tx.run();
		}
	}
	
	private TicketExchanger constructTXFromEmail(BufferedReader reader) throws Exception
	{
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
			if(tx_key.equals("test")) {
				//"test" is a special tx that doesn't do anything - but just to make sure that TX is running
				return new TestTicketExchanger();
			} else {
				TicketExchanger tx = Factory.createInstance(tx_key);
				String subject = headers.get("Subject");
				tx.setTicketIDFromSubject(subject);
				return tx;
			}
		}
	}
}
