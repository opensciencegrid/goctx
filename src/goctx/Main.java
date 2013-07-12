package goctx;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.channels.FileLock;
import java.util.Properties;

import org.apache.log4j.Logger;

import edu.iu.grid.tx.TicketExchanger;
import edu.iu.grid.tx.custom.Factory;

public class Main {
	
	static Logger logger = Logger.getLogger(Main.class);
	static String version = "1.31";
	
	public static void main(String[] args) 
	{

        // Load up properties file
        String properties_file= System.getProperty("config", "goctx.default.properties");

        Properties properties = new Properties();
        try {
            properties.load(new FileInputStream(properties_file));
        } catch (Exception e) {
            logger.debug("Could not load properties file: " + properties_file);
        }

        // Print out properties
        for(String key : properties.stringPropertyNames()) {
            String value = properties.getProperty(key);
            logger.debug(key + " => " + value);
        }

		logger.debug("GOCTX Version " + version);

        // TODO put somewhere else?
		//System.setProperty("javax.net.ssl.trustStore", "jssecacerts");
		//System.setProperty("javax.net.ssl.trustStorePassword", "changeit");

		FileLock fl = lock();

		try {
			//Email trigger
			InputStreamReader isr = new InputStreamReader(System.in);
			BufferedReader reader = new BufferedReader(isr);
			TicketExchanger tx = TicketExchanger.createInstanceFromEmail(reader, new Factory());
			
			/* debug
			tx = factory.createInstance("fp114_rtgocdev");
			tx.setTicketID("80");
			tx.run();
			*/
			
			tx.run();
		} catch (Exception e) {
			logger.error("Uncaught exception: ", e);
		} finally {
			unlock(fl);		
		}
	}
	
	static private FileLock lock() {
		//obtain file lock
		String lock_filename = TicketExchanger.getConf().getProperty("goctx_filelock_path");
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
		return fl;
	}
	
	static private void unlock(FileLock fl) {
		//release the file lock
		try {
			fl.release();
		} catch (IOException e) {
			logger.error("Failed to release lock: ", e);
		}
	}
}
