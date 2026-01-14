package it.thisone.iotter.persistence.service;

import java.text.MessageFormat;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.AbstractMessageSource;
import org.springframework.stereotype.Component;

import it.thisone.iotter.persistence.ifc.IMessageBundleDao;
import it.thisone.iotter.persistence.model.MessageBundle;

//@Component
public class DatabaseMessageSourceContext extends AbstractMessageSource {
	private static Logger logger = LoggerFactory.getLogger(DatabaseMessageSourceContext.class);

	@Autowired
	private IMessageBundleDao dao;

	private Messages messages;

	private Set<String> templates;

	public String getDatabaseMessage(String code, String defaultMessage, Locale locale) {
		int index = code.lastIndexOf("-");
		if (index > 0) {
			String template = code.substring(0, index);
			if (!templates.contains(template)) {
				try {
					List<MessageBundle> bundles = dao.findByTemplate(template);
					logger.debug("template library {} found messages {}", template, bundles.size());
					templates.add(template);
					if (bundles.size() > 0) {
						addMessages(bundles);
					}
					else {
						logger.error("template library may have been deleted, no messages found with code {}% ", template);
					}

				} catch (Throwable e) {
					logger.error("getDatabaseMessage code:" + code, e);
				}
			}			
		}
		return super.getMessage(code, null, defaultMessage, locale);
	}

	public String getDatabaseMessage(String code, Locale locale) {
		String defaultMessage = null;
		return this.getDatabaseMessage(code, defaultMessage, locale);
	}

	public void reloadTemplate(String template) {
		this.templates.remove(template);
		this.messages.removeTemplate(template);
	}

	@Override
	protected MessageFormat resolveCode(String code, Locale locale) {
		String msg = messages.getMessage(code, locale);
		return createMessageFormat(msg, locale);
	}

	public void addMessages(Collection<MessageBundle> bundles) {
		for (MessageBundle bundle : bundles) {
			String code = bundle.getCode() + bundle.getType();
			messages.addMessage(code, bundle.getLocale(), bundle.getMessage());
		}
	}

	public void removeMessages(Collection<MessageBundle> bundles) {
		for (MessageBundle bundle : bundles) {
			String code = bundle.getCode() + bundle.getType();
			messages.removeBundle(code);
		}
	}

	@PostConstruct
	public void init() {
		messages = new Messages();
		templates = new HashSet<String>();
//		try {
//			List<MessageBundle> bundles = dao.findAll();
//			addMessages(bundles);
//		} catch (Throwable e) {
//			logger.error("init",e);
//		}
	}
	
	public int clear() {
		templates.clear();
		int size = messages.clear();
		return size;
	}

	@PreDestroy
	public void destroy() {
		logger.debug("Callback triggered - @PreDestroy.");
	}

	/**
	 * 
	 * Messages bundle
	 */
	protected static final class Messages {

		public Messages() {
			super();
			messages = new HashMap<String, Map<Locale, String>>();
		}

		public void removeTemplate(String template) {
			if (messages == null)
				messages = new HashMap<String, Map<Locale, String>>();
			Set<String> codes = messages.keySet();
			for (String code : codes) {
				if (code.startsWith(template)) {
					messages.remove(code);
				}
			}
		}

		public void removeBundle(String code) {
			if (messages == null)
				messages = new HashMap<String, Map<Locale, String>>();
			messages.remove(code);
		}

		/* <code, <locale, message>> */
		private Map<String, Map<Locale, String>> messages;

		public void addMessage(String code, Locale locale, String msg) {
			if (messages == null)
				messages = new HashMap<String, Map<Locale, String>>();
			Map<Locale, String> data = messages.get(code);
			if (data == null) {
				data = new HashMap<Locale, String>();
				messages.put(code, data);
			}
			data.put(locale, msg);
		}

		public String getMessage(String code, Locale locale) {
			Map<Locale, String> data = messages.get(code);
			if (data == null)
				return null;
			Locale language = new Locale(locale.getLanguage());
			String message = data.get(language);
			return message != null ? message : data.get(Locale.ENGLISH);
		}
		
		public int clear() {
			int size = messages.size();
			messages.clear();
			return size;
		}
		
	}

}