package it.thisone.iotter.security;

import java.io.Serializable;

public class Permissions implements Serializable {
	private static final long serialVersionUID = -2216614611599049665L;
	
	private boolean viewAllMode = false;
	private boolean viewMode = false;
	private boolean modifyMode = false;
	private boolean removeMode = false;
	private boolean createMode = false;
	private boolean activateMode = false;
	
	public Permissions() {
		super();
	}

	public Permissions(boolean value) {
		super();
		viewAllMode = value;
		viewMode = value;
		modifyMode = value;
		removeMode = value;
		createMode = value;
		activateMode = value;
	}
	
	
	public boolean isViewAllMode() {
		return viewAllMode;
	}
	public void setViewAllMode(boolean viewAllMode) {
		this.viewAllMode = viewAllMode;
	}
	public boolean isViewMode() {
		return viewMode;
	}
	public void setViewMode(boolean viewMode) {
		this.viewMode = viewMode;
	}
	public boolean isModifyMode() {
		return modifyMode;
	}
	public void setModifyMode(boolean modifyMode) {
		this.modifyMode = modifyMode;
	}
	public boolean isRemoveMode() {
		return removeMode;
	}
	public void setRemoveMode(boolean removeMode) {
		this.removeMode = removeMode;
	}
	public boolean isCreateMode() {
		return createMode;
	}
	public void setCreateMode(boolean createMode) {
		this.createMode = createMode;
	}
	public boolean isActivateMode() {
		return activateMode;
	}
	public void setActivateMode(boolean activateMode) {
		this.activateMode = activateMode;
	}
	

}
