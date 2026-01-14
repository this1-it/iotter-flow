package it.thisone.iotter.persistence.service;

import java.util.Locale;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import it.thisone.iotter.config.Constants;
import it.thisone.iotter.persistence.ifc.IMessageBundleDao;
import it.thisone.iotter.persistence.model.MessageBundle;

//@Component
public class DatabaseMessageSourceCacheable {
	private static Logger logger = LoggerFactory.getLogger(DatabaseMessageSourceCacheable.class);

//	@Resource
//	private DatabaseMessageSourceCacheable self;

	@Autowired
	private IMessageBundleDao dao;

	public String getDatabaseMessage(String code, String defaultMessage, Locale locale) {
		String type = "";
		int index = code.lastIndexOf("|enum");
		if (index > 0) {
			code = code.substring(0, index);
			type = "enum";
		}
		return getMessage(code, type, locale.getLanguage());
	}

	public String getDatabaseMessage(String code, Locale locale) {
		String defaultMessage = null;
		return this.getDatabaseMessage(code, defaultMessage, locale);
	}

	@PostConstruct
	public void init() {
		logger.debug("Callback triggered - @PostConstruct.");
	}

	@PreDestroy
	public void destroy() {
		logger.debug("Callback triggered - @PreDestroy.");
	}

	public final String getMessage(String code, String type, String language) {
		MessageBundle bundle = dao.find(code, type, language);
		if (bundle == null) {
			return null;
		}
		return bundle.getMessage();
	}

}