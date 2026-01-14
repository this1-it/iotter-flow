package it.thisone.iotter.persistence.model;

import java.util.Locale;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.eclipse.persistence.annotations.Index;
import org.eclipse.persistence.annotations.Indexes;

@Cacheable(false)
@Entity
@Table(name = "I18N")
@Indexes({ @Index(name = "I18N_LOOKUP_INDEX", columnNames = { "CODE", "TYPE", "LANGUAGE" })})
public class MessageBundle extends BaseEntity {

	public MessageBundle() {
		super();
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -5300417025647548988L;

	public MessageBundle(String code, String type, String message,  Locale locale) {
		super();
		this.code = code;
		this.message = message;
		this.type = type;
		this.language = locale.getLanguage();
	}

	@Lob
	@Column(name = "MESSAGE")
	private String message;
	
	@Column(name = "LANGUAGE")
	private String language;

	@Column(name = "CODE")
	private String code;

	@Column(name = "TYPE")
	private String type;

	
	@Transient
	public Locale getLocale() {
		return new Locale(language);
	}
	
	
	public String getMessage() {
		return message;
	}

	public String getLanguage() {
		return language;
	}

	public String getCode() {
		return code;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public void setCode(String code) {
		this.code = code;
	}


	public String getType() {
		return type;
	}


	public void setType(String type) {
		this.type = type;
	}

}
