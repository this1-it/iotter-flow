package it.thisone.iotter.ui.common;

public class UAgent {
	
	private final String header;
	public UAgent(String header) {
		super();
		this.header = header;
	}
	private String family;
	private String version;
	private boolean mobile;
	
	public String getFamily() {
		return family;
	}

	public void setFamily(String family) {
		this.family = family;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public boolean isMobile() {
		return mobile;
	}

	public void setMobile(boolean mobile) {
		this.mobile = mobile;
	}


	
	public String getHeader() {
		return header;
	}
	

}
