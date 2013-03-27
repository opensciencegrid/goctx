package edu.iu.grid.tx.accessor;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

import org.apache.log4j.Logger;

import com.mysql.jdbc.PreparedStatement;
import edu.iu.grid.tx.ticket.FPTicket;

public class FPMetadataAccessor  {

	private static final Logger logger = Logger.getLogger(FPMetadataAccessor.class);	
	protected static Connection db = null;
	
	int project_id;
	
	public FPMetadataAccessor(String url, String user, String pass, int project_id) {		
		this.project_id = project_id;
		
		try {
			db = DriverManager.getConnection(url, user, pass);
			logger.info("Connected to Metadata server: " + url);
		} catch (SQLException e) {
			logger.warn("Failed to connect to metadata host: " + url + "(metadata will not be used)", e);
		}
	}
	
	public void setMetadata(FPTicket ticket) {
		try {
			if(db != null) {
				db.setAutoCommit(false);
				
				int ticket_id = Integer.parseInt(ticket.getTicketID());
				
				//clear all previous values for this ticket(if any)			
				PreparedStatement st = (PreparedStatement) db.prepareStatement(
						"DELETE FROM metadata WHERE ticket_id = ? and project_id = ?");
				st.setInt(1, ticket_id);
				st.setInt(2, project_id);
				st.execute();
				st.close();
				
				//then populate all metadata key/value
				HashMap<String, String> metadata = ticket.getMetadata();
			    st = (PreparedStatement) db.prepareStatement(
			    		"INSERT INTO metadata (ticket_id, `key`, `value`, project_id) values (?,?,?,?)");
				for(String key : metadata.keySet()) {
					String value = metadata.get(key);
					
					st.setInt(1, ticket_id);
					st.setString(2, key);
					st.setString(3, value);
					st.setInt(4, project_id);
					st.addBatch();
					
					logger.debug("Metadata: " + key + " = " + value);
				}
				st.executeBatch();    
				
				db.commit();
				db.setAutoCommit(true);	
				

			}
		} catch (Exception e) {
			logger.warn("Failed to set metadata", e);
		}
	}

	public void getMetadata(FPTicket ticket) {
		try {
			if(db != null) {
				int ticket_id = Integer.parseInt(ticket.getTicketID());
				PreparedStatement st = (PreparedStatement) db.prepareStatement(
					"SELECT `key`, `value` FROM metadata WHERE ticket_id = ? and project_id = ?");
				st.setInt(1, ticket_id);
				st.setInt(2, project_id);
				ResultSet rs = st.executeQuery();
				while(rs.next()) {
					ticket.setMetadata(rs.getString(1), rs.getString(2));
				}
			}
		} catch (Exception e) {
			logger.warn("Failed to get metadata", e);
		}
	}
}
