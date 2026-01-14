package it.thisone.iotter.persistence.service;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import it.thisone.iotter.config.Constants;
import it.thisone.iotter.enums.TracingAction;
import it.thisone.iotter.persistence.dao.TracingErrorDao;
import it.thisone.iotter.persistence.ifc.ITracingDao;
import it.thisone.iotter.persistence.model.Tracing;
import it.thisone.iotter.persistence.model.TracingCriteria;
import it.thisone.iotter.util.EncryptUtils;

@Service
public class TracingService {
	private static Logger logger = LoggerFactory.getLogger(Constants.REST.LOG4J_CATEGORY);

	@Autowired
	private ITracingDao dao;

	@Autowired
	private TracingErrorDao error;
	
	
	public TracingService() {
		super();
	}

	// API

	public long count(TracingCriteria criteria) {
		return dao.count(criteria);
	}

	public List<Tracing> search(TracingCriteria criteria, int offset, int limit) {
		return dao.search(criteria, offset, limit);
	}

	@Transactional
	public void create(Tracing entity) {
		dao.create(entity);
	}

	@Transactional
	public void update(Tracing entity) {
		dao.update(entity);
	}

	public Tracing findOne(String id) {
		return dao.findOne(id);
	}

	public List<Tracing> findAll() {
		return dao.findAll();
	}

	public List<Tracing> findByOwner(String owner) {
		return dao.findByOwner(owner);
	}

	@Transactional
	public void deleteById(String entityId) {
		dao.deleteById(entityId);
	}

	@Transactional
	public void trace(TracingAction action, String username, String administrator, String network, String device,
			String message) {
		if (username == null) {
			username = Constants.SYSTEM;
		}
		if (administrator == null) {
			administrator = Constants.SYSTEM;
		}
		Tracing entity = new Tracing(action, username, administrator, network, device, message);
		dao.create(entity);
	}

	@Transactional
	public void traceAlarm(Date timeStamp, String administrator, String network, String device, String message) {
		Tracing entity = new Tracing(TracingAction.ALARM, Constants.SYSTEM, administrator, network, device, message);
		entity.setTimeStamp(timeStamp);
		dao.create(entity);
	}

	//@Transactional
	public boolean traceRestError(String errorId, String administrator, String network, String device, String message, String json) {
		if (administrator == null) {
			administrator = Constants.SYSTEM;
		}
		String host = dao.getHostAddress();
		if (host != null && !host.equals("localhost")) {
			administrator = String.format("%s@%s", administrator, host);
		}

		Tracing entity = new Tracing(TracingAction.ERROR_REST, Constants.SYSTEM, administrator, network, device,
				message);

		Calendar calendar = Calendar.getInstance();
		int start = calendar.get(Calendar.MINUTE) / 15;
		calendar.set(Calendar.MINUTE, start * 15);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		String id = String.format("%d%s%s", calendar.getTimeInMillis(), device, EncryptUtils.digest(errorId));
		entity.setId(id);

		try {
			error.insert(entity);
		} catch (RuntimeException e) {
			return false;
		}
		if (json != null) {
			logger.error("{} {}", errorId, StringUtils.replace(json, "\n", ""));
		}
		
		return true;
	}

}
