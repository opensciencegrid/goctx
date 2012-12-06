package edu.iu.grid.tx;

import java.sql.SQLException;

import org.apache.log4j.Logger;

public class TestTicketExchanger extends TicketExchanger {
	static Logger logger = Logger.getLogger(TestTicketExchanger.class);
	
	public TestTicketExchanger() throws SQLException {
		super(null);
	}
	void run() {
		logger.info("Test TX Received.");
	}
}
