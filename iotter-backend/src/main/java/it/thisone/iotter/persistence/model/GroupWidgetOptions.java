package it.thisone.iotter.persistence.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class GroupWidgetOptions implements Serializable {

    /**
	 * 
	 */
	private static final long serialVersionUID = -3384637807636294207L;


	public GroupWidgetOptions() {
		super();
		realTime = false;
		showControls = true;
	}
    
	@Column(name = "REAL_TIME")
	private boolean realTime;

	@Column(name = "SHOW_CONTROLS")
	private boolean showControls;


	public boolean isRealTime() {
		return realTime;
	}

	public boolean isShowControls() {
		return showControls;
	}

	public void setRealTime(boolean realTime) {
		this.realTime = realTime;
	}

	public void setShowControls(boolean showControls) {
		this.showControls = showControls;
	}
}
