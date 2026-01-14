package it.thisone.iotter.persistence.proftpd;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

@SuppressWarnings("rawtypes")
public class FtpUserRowMapper implements RowMapper
{
	@Override
	public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
		FtpUser user = new FtpUser();
		user.setAccessed(rs.getDate("accessed"));
		user.setCount(rs.getInt("count"));
		user.setGid(rs.getInt("gid"));
		user.setHomedir(rs.getString("homedir"));
		user.setId(rs.getInt("id"));
		user.setModified(rs.getDate("modified"));
		user.setPasswd(rs.getString("passwd"));
		user.setShell(rs.getString("shell"));
		user.setUid(rs.getInt("uid"));
		user.setUserid(rs.getString("userid"));
		return user;
	}
 
}