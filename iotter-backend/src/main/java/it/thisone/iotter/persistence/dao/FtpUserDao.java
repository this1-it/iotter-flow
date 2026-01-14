package it.thisone.iotter.persistence.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.stereotype.Repository;

import it.thisone.iotter.persistence.proftpd.FtpUser;
import it.thisone.iotter.persistence.proftpd.FtpUserRowMapper;

//@Repository
public class FtpUserDao extends JdbcDaoSupport {
	private final static String DEFAULT_FTP_HOMEDIR_PATH="/srv/ftp/users/";
	
	private final static String SELECT_USER_SQL = "SELECT `id` FROM `ftpuser` WHERE `userid`=?;";

	private final static String CREATE_USER_SQL = "INSERT INTO `ftpuser` (`userid`, `passwd`, `uid`, `gid`,`homedir`, `shell`, `count`, `accessed`, `modified`) " +
			"VALUES (?, ?, ?, ?, ?,'/sbin/nologin', 0, '0000-00-00 00:00:00', '0000-00-00 00:00:00');";
	private final static String CREATE_GROUP_SQL = "INSERT INTO `ftpgroup` (`groupname`, `gid`, `members`) "+
			"VALUES (?, ?, ?);";
	private final static String CREATE_QUOTA_SQL = "INSERT INTO `ftpquotalimits` (`name`,`quota_type`,`per_session`,`limit_type`,`bytes_in_avail`, `bytes_out_avail`, `bytes_xfer_avail`, `files_in_avail`, `files_out_avail`,`files_xfer_avail`) "+
			"VALUES (?, 'user', 'true', 'hard', ?, 0, 0, 0, 0, 0);";

	private final static String UPDATE_USER_SQL = "UPDATE `ftpuser` SET `passwd`=? WHERE `ftpuser`=?;";
	private final static String UPDATE_QUOTA_SQL = "UPDATE `ftpquotalimits` SET `bytes_in_avail`=? WHERE `name`=?;";
	
	private final static String DELETE_USER_SQL = "DELETE FROM `ftpuser` WHERE `userid`=?;";
	private final static String DELETE_QUOTA_SQL = "DELETE FROM `ftpquotalimits` WHERE `name`=?;";
	private final static String DELETE_GROUP_SQL = "DELETE FROM `ftpgroup` WHERE `members`=?;";

//	@Autowired
//	@Qualifier("Ftp")
	private DataSource ftpDataSource;

	//@PostConstruct
	void init() {
		setDataSource(ftpDataSource);
	}

	public String findUserNameById(int id){
		String sql = "SELECT userid FROM ftpuser WHERE id=?;";
		String name = getJdbcTemplate().queryForObject(
				sql, new Object[] { id }, String.class);
		return name;
	}

	@SuppressWarnings("unchecked")
	public FtpUser findById(int id){
		String sql = "SELECT * FROM ftpuser WHERE id=?;";
		FtpUser user = (FtpUser)getJdbcTemplate().queryForObject(
				sql, new Object[] { id }, new FtpUserRowMapper());
		return user;
	}
	
	public void createOrUpdateUser(String userName, String password, int quota) {
		Connection conn = null;
		PreparedStatement userSelectStat = null;
		PreparedStatement userUpdateStat = null;
		PreparedStatement userInsertStat = null;
		PreparedStatement groupInsertStat = null;
		PreparedStatement quotaInsertStat = null;
		PreparedStatement quotaUpdateStat = null;

		try {
			conn = ftpDataSource.getConnection();			
			conn.setAutoCommit(false);
			userSelectStat = conn.prepareStatement(SELECT_USER_SQL);
			userSelectStat.setString(1, userName);
			ResultSet set = userSelectStat.executeQuery();
			if(set.next()) {
				userUpdateStat = conn.prepareStatement(UPDATE_USER_SQL);
				userUpdateStat.setString(1, userName);
				userUpdateStat.setString(2, password);
				userUpdateStat.executeUpdate();
				
				quotaUpdateStat = conn.prepareStatement(UPDATE_QUOTA_SQL);
				quotaUpdateStat.setInt(1, quota);
				quotaUpdateStat.setString(2, userName);
				quotaUpdateStat.executeUpdate();
			}
			else {
				
//				String groupname = properties.getProperty("ftp.groupname", "nobody");
//				String root = properties.getProperty("ftp.root", DEFAULT_FTP_HOMEDIR_PATH);
//				int uid = Integer.parseInt(properties.getProperty("ftp.uid", "109"));
//				int gid = Integer.parseInt(properties.getProperty("ftp.gid", "32767"));

				String groupname = "nobody";
				String root = DEFAULT_FTP_HOMEDIR_PATH;
				int uid = 99;
				int gid = 99;
				
				groupInsertStat = conn.prepareStatement(CREATE_GROUP_SQL);
				groupInsertStat.setString(1, groupname);
				groupInsertStat.setInt(2, gid);
				groupInsertStat.setString(3, userName);
				groupInsertStat.executeUpdate();
				
				userInsertStat = conn.prepareStatement(CREATE_USER_SQL);
				userInsertStat.setString(1, userName);
				userInsertStat.setString(2, password);
				userInsertStat.setInt(3, uid);
				userInsertStat.setInt(4, gid);
				userInsertStat.setString(5, root + userName);
				userInsertStat.executeUpdate();
				
				quotaInsertStat = conn.prepareStatement(CREATE_QUOTA_SQL);
				quotaInsertStat.setString(1, userName);
				quotaInsertStat.setInt(2, quota);				
				quotaInsertStat.executeUpdate();
			}
			
			conn.commit();
			
		} catch (SQLException e) {
			logger.error("createOrUpdateUser", e);
			try {
				if(conn != null) conn.rollback();
			} catch (SQLException e1) {
			}
		}
		finally {
			try {
				if(userSelectStat != null) userSelectStat.close();
				if(userInsertStat != null) userInsertStat.close();
				if(userUpdateStat != null) userUpdateStat.close();
				if(groupInsertStat != null) groupInsertStat.close();
				if(quotaInsertStat != null) quotaInsertStat.close();
				if(quotaUpdateStat != null) quotaUpdateStat.close();
			}
			catch(Exception e) {}
			try {
				if(conn != null) conn.close();
			}
			catch(Exception e) {}
		}
	}
	
	public void deleteUser(String userName) {
		Connection conn = null;
		PreparedStatement userSelectStat = null;
		PreparedStatement userDeleteStat = null;
		PreparedStatement groupDeleteStat = null;
		PreparedStatement quotaDeleteStat = null;
		
		try {
			conn = ftpDataSource.getConnection();
			conn.setAutoCommit(false);
			userSelectStat = conn.prepareStatement(SELECT_USER_SQL);
			userSelectStat.setString(1, userName);
			ResultSet set = userSelectStat.executeQuery();
			if(set.next()) {
				userDeleteStat = conn.prepareStatement(DELETE_USER_SQL);
				userDeleteStat.setString(1, userName);
				userDeleteStat.executeUpdate();
			
				groupDeleteStat = conn.prepareStatement(DELETE_GROUP_SQL);
				groupDeleteStat.setString(1, userName);
				groupDeleteStat.executeUpdate();
				
				quotaDeleteStat = conn.prepareStatement(DELETE_QUOTA_SQL);
				quotaDeleteStat.setString(1, userName);
				quotaDeleteStat.executeUpdate();
				
				conn.commit();				
			}
		}
		catch (SQLException e) {
			logger.error("deleteUser", e);
			try {
				if(conn != null) conn.rollback();
			} catch (SQLException e1) {
			}
		}
		finally {
			try {
				if(userSelectStat != null) userSelectStat.close();
				if(userDeleteStat != null) userDeleteStat.close();
				if(groupDeleteStat != null) groupDeleteStat.close();
				if(quotaDeleteStat != null) quotaDeleteStat.close();
			}
			catch(Exception e) {}
			try {
				if(conn != null) conn.close();
			}
			catch(Exception e) {}
		}
	}
	
	
}
