package it.thisone.iotter.persistence.dao;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.stereotype.Repository;

import it.thisone.iotter.persistence.model.Tracing;

@Repository
public class TracingErrorDao extends JdbcDaoSupport {
//	private static Logger logger = LoggerFactory.getLogger(TracingErrorDao.class);

	@Autowired
	@Qualifier("JPA")
	private DataSource dataSource;

	private static final String SQL = "INSERT INTO TRACING (ID, ACTION, DESCRIPTION, ADMINISTRATOR, DEVICE, TIMESTAMP, VERSION ) VALUES (?, ?, ?, ?, ?, ?, ?)";

	@PostConstruct
	void init() {
		setDataSource(dataSource);
	}

	public void insert(Tracing entity) {
		getJdbcTemplate().update(SQL, new Object[] { entity.getId(), entity.getAction().name(), entity.getDescription(),
				entity.getAdministrator(), entity.getDevice(), entity.getTimeStamp(), 0l });
	}

//	public boolean _insert(Tracing entity) {
//		boolean done = false;
//		Connection conn = null;
//		PreparedStatement stmt = null;
//		try {
//			conn = dataSource.getConnection();
//			conn.setAutoCommit(true);
//			stmt = conn.prepareStatement(SQL);
//			stmt.setString(1, entity.getId());
//			stmt.setString(2, entity.getAction().name());
//			stmt.setString(3, entity.getDescription());
//			stmt.setString(4, entity.getAdministrator());
//			stmt.setString(5, entity.getDevice());
//			stmt.setDate(6, new java.sql.Date(entity.getTimeStamp().getTime()));
//			stmt.setLong(7, 0l);
//			stmt.executeUpdate();
//			conn.commit();
//			done = true;
//			logger.error("traceErrorDao {}", entity.getId());
//
//		} catch (SQLException e) {
//			logger.error("traceErrorDao", e);
//		} finally {
//			try {
//				if (stmt != null)
//					stmt.close();
//			} catch (Exception e) {
//			}
//			try {
//				if (conn != null)
//					conn.close();
//			} catch (Exception e) {
//			}
//		}
//		return done;
//	}

}
