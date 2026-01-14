package it.thisone.iotter.ui.common;

import java.io.Serializable;

import it.thisone.iotter.security.EntityPermission;
import it.thisone.iotter.security.Permissions;

/**
 * return permissions for each entity listing
 * 
 * @author tisone
 *
 */
public class PermissionsUtils implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 8740526545489741855L;

	public static Permissions getPermissionsForDeviceEntity() {
		Permissions permissions = new Permissions();
		permissions.setActivateMode(UIUtils.hasPermission(EntityPermission.DEVICE.ACTIVATE));
		permissions.setCreateMode(UIUtils.hasPermission(EntityPermission.DEVICE.CREATE));
		permissions.setModifyMode(UIUtils.hasPermission(EntityPermission.DEVICE.MODIFY));
		permissions.setRemoveMode(UIUtils.hasPermission(EntityPermission.DEVICE.REMOVE));
		permissions.setViewAllMode(UIUtils.hasPermission(EntityPermission.DEVICE.VIEWALL));
		permissions.setViewMode(UIUtils.hasPermission(EntityPermission.DEVICE.VIEW));
		
		return permissions;
	}
	
	
	public static Permissions getPermissionsForNetworkEntity() {
		Permissions permissions = new Permissions();
		permissions.setCreateMode(UIUtils.hasPermission(EntityPermission.NETWORK.CREATE));
		permissions.setModifyMode(UIUtils.hasPermission(EntityPermission.NETWORK.MODIFY));
		permissions.setRemoveMode(UIUtils.hasPermission(EntityPermission.NETWORK.REMOVE));
		permissions.setViewAllMode(UIUtils.hasPermission(EntityPermission.NETWORK.VIEWALL));
		permissions.setViewMode(UIUtils.hasPermission(EntityPermission.NETWORK.VIEW));
		return permissions;
	}
	
	public static Permissions getPermissionsForNetworkGroupEntity() {
		Permissions permissions = new Permissions();
		permissions.setCreateMode(UIUtils.hasPermission(EntityPermission.NETWORK_GROUP.CREATE));
		permissions.setModifyMode(UIUtils.hasPermission(EntityPermission.NETWORK_GROUP.MODIFY));
		permissions.setRemoveMode(UIUtils.hasPermission(EntityPermission.NETWORK_GROUP.REMOVE));
		permissions.setViewAllMode(UIUtils.hasPermission(EntityPermission.NETWORK_GROUP.VIEWALL));
		permissions.setViewMode(UIUtils.hasPermission(EntityPermission.NETWORK_GROUP.VIEW));
		return permissions;
	}

	

	public static Permissions getPermissionsForUserEntity() {
		Permissions permissions = new Permissions();
		permissions.setCreateMode(UIUtils.hasPermission(EntityPermission.USER.CREATE));
		permissions.setModifyMode(UIUtils.hasPermission(EntityPermission.USER.MODIFY));
		permissions.setRemoveMode(UIUtils.hasPermission(EntityPermission.USER.REMOVE));
		permissions.setViewAllMode(UIUtils.hasPermission(EntityPermission.USER.VIEWALL));
		permissions.setViewMode(UIUtils.hasPermission(EntityPermission.USER.VIEW));
		return permissions;
	}
	
	public static Permissions getPermissionsForTracingEntity() {
		Permissions permissions = new Permissions();
		permissions.setCreateMode(false);
		permissions.setModifyMode(false);
		permissions.setRemoveMode(false);
		permissions.setViewAllMode(UIUtils.hasPermission(EntityPermission.TRACING.VIEWALL));
		permissions.setViewMode(UIUtils.hasPermission(EntityPermission.TRACING.VIEW));
		return permissions;
	}

	public static Permissions getPermissionsForGroupWidgetEntity() {
		Permissions permissions = new Permissions();
		permissions.setCreateMode(UIUtils.hasPermission(EntityPermission.GROUP_WIDGET.CREATE));
		permissions.setModifyMode(UIUtils.hasPermission(EntityPermission.GROUP_WIDGET.MODIFY));
		permissions.setRemoveMode(UIUtils.hasPermission(EntityPermission.GROUP_WIDGET.REMOVE));
		permissions.setViewAllMode(UIUtils.hasPermission(EntityPermission.GROUP_WIDGET.VIEWALL));
		permissions.setViewMode(UIUtils.hasPermission(EntityPermission.GROUP_WIDGET.VIEW));
		return permissions;
	}


	public static Permissions getPermissionsForDeviceModelEntity() {
		Permissions permissions = new Permissions();
		permissions.setCreateMode(UIUtils.hasPermission(EntityPermission.DEVICE_MODEL.CREATE));
		permissions.setModifyMode(UIUtils.hasPermission(EntityPermission.DEVICE_MODEL.MODIFY));
		permissions.setRemoveMode(UIUtils.hasPermission(EntityPermission.DEVICE_MODEL.REMOVE));
		permissions.setViewAllMode(UIUtils.hasPermission(EntityPermission.DEVICE_MODEL.VIEWALL));
		permissions.setViewMode(UIUtils.hasPermission(EntityPermission.DEVICE_MODEL.VIEW));
		return permissions;
	}


	public static Permissions getPermissionsForMeasureUnitTypeEntity() {
		Permissions permissions = new Permissions();
		permissions.setCreateMode(UIUtils.hasPermission(EntityPermission.MEASURE_UNIT_TYPE.CREATE));
		permissions.setModifyMode(UIUtils.hasPermission(EntityPermission.MEASURE_UNIT_TYPE.MODIFY));
		permissions.setRemoveMode(UIUtils.hasPermission(EntityPermission.MEASURE_UNIT_TYPE.REMOVE));
		permissions.setViewAllMode(UIUtils.hasPermission(EntityPermission.MEASURE_UNIT_TYPE.VIEWALL));
		permissions.setViewMode(UIUtils.hasPermission(EntityPermission.MEASURE_UNIT_TYPE.VIEW));
		return permissions;
	}


	public static Permissions getPermissionsForModbusProfileEntity() {
		Permissions permissions = new Permissions();
		permissions.setCreateMode(UIUtils.hasPermission(EntityPermission.MODBUS_PROFILE.CREATE));
		permissions.setModifyMode(UIUtils.hasPermission(EntityPermission.MODBUS_PROFILE.MODIFY));
		permissions.setRemoveMode(UIUtils.hasPermission(EntityPermission.MODBUS_PROFILE.REMOVE));
		//permissions.setViewAllMode(UIUtils.hasPermission(EntityPermission.MODBUS_PROFILE.VIEWALL));
		permissions.setViewMode(UIUtils.hasPermission(EntityPermission.MODBUS_PROFILE.VIEW));
		return permissions;
	}
	
	
	
	
}
