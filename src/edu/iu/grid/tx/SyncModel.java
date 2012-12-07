package edu.iu.grid.tx;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;

import org.apache.log4j.Logger;

public class SyncModel {
	static Logger logger = Logger.getLogger(SyncModel.class);
    Connection con = null;
    
	public SyncModel() throws SQLException
	{
		String url = TicketExchanger.getConf().getProperty("txdb_url");
		logger.debug("Attempting to connect to Sync DB: " + url);
		con = DriverManager.getConnection(url,
				TicketExchanger.getConf().getProperty("txdb_user"), 
				TicketExchanger.getConf().getProperty("txdb_pass"));
		logger.debug("Successfully connected");
	}
	public void close()
	{
		try {
			con.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public String getDestIDBySourceID(String source_id, String tx_id) throws SQLException
	{
		String sql = "select dest_id from sync where source_id = ? and tx_id = ?";
		PreparedStatement stmt = con.prepareStatement(sql);		
	    stmt.setString(1, source_id);
	    stmt.setString(2, tx_id);
	    ResultSet rs = stmt.executeQuery();
	    if(rs.next()) {
	    	return rs.getString(1);
	    }
	    return null;
	}
	
	public boolean hasAttachmentRecord(String tx_id, String source_id, String dest_id, String source_attachment_id) throws SQLException {
		String sql = "select * from attachment where tx_id = ? and source_id = ? and dest_id = ? and source_attachment_id = ?";
		PreparedStatement stmt = con.prepareStatement(sql);		
	    stmt.setString(1, tx_id);
	    stmt.setString(2, source_id);
	    stmt.setString(3, dest_id);
	    stmt.setString(4, source_attachment_id);
	    ResultSet rs = stmt.executeQuery();
	    if(rs.next()) {
	    	return true;
	    }
	    return false;
	}
	public void insertAttachmentRecord(String tx_id, String source_id, String dest_id, String source_attachment_id) throws SQLException
	{
		String sql = "INSERT INTO attachment VALUES (?,?,?,?)";
		PreparedStatement stmt = con.prepareStatement(sql);		
	    stmt.setString(1, tx_id);
		stmt.setString(2, source_id);
	    stmt.setString(3, dest_id);
	    stmt.setString(4, source_attachment_id);
	    stmt.execute();
	}
	
	public Timestamp getSourceTimestamp(String source_id, String tx_id) throws SQLException
	{
		String sql = "select source_timestamp from sync where source_id = ? and tx_id = ?";
		PreparedStatement stmt = con.prepareStatement(sql);		
	    stmt.setString(1, source_id);
	    stmt.setString(2, tx_id);
	    ResultSet rs = stmt.executeQuery();
	    if(rs.next()) {
	    	return rs.getTimestamp(1);
	    }
	    return null;
	}
	
	public void insert(String source_id, String dest_id, String tx_id, Date timestamp) throws SQLException
	{
		String sql = "INSERT INTO sync VALUES (?,?,?,?)";
		PreparedStatement stmt = con.prepareStatement(sql);		
	    stmt.setString(1, source_id);
	    stmt.setString(2, dest_id);
	    stmt.setString(3, tx_id);
	    stmt.setTimestamp(4, new Timestamp(timestamp.getTime()));
	    stmt.execute();
	}
	public void updateSourceTimestamp(String source_id, String tx_id, Date timestamp) throws SQLException
	{
		String sql = "UPDATE sync SET source_timestamp = ? WHERE source_id = ? AND tx_id = ?";
		PreparedStatement stmt = con.prepareStatement(sql);		
	    stmt.setTimestamp(1, new Timestamp(timestamp.getTime()));
		stmt.setString(2, source_id);
		stmt.setString(3, tx_id);
	    stmt.execute();
	}
	
	public class SyncRecord
	{
		String source_id;
		String dest_id;
		public SyncRecord(ResultSet res) throws SQLException
		{
			source_id = res.getString(1);
			dest_id = res.getString(2);
		}
	}
}
