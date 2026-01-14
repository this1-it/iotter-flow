package it.thisone.iotter.ui.eventbus;

import java.io.Serializable;

import it.thisone.iotter.exporter.IExportConfig;
import it.thisone.iotter.exporter.IExportProperties;


public class ExportStartEvent implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -6190272978945468886L;

	private final String owner;

	private final String email;
	
	private final IExportConfig config;
	
	private final IExportProperties properties;
	
	public ExportStartEvent(String owner, String email, IExportConfig config, IExportProperties properties) {
		super();
		this.owner = owner;
		this.email = email;
		this.config = config;
		this.properties = properties;
	}

	public String getOwner() {
		return owner;
	}

	public String getEmail() {
		return email;
	}
	
	public IExportConfig getConfig() {
		return config;
	}

	public IExportProperties getProperties() {
		return properties;
	}

}
