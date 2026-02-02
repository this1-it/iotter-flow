package it.thisone.iotter.ui.common;

import java.io.Serializable;

import org.springframework.security.core.GrantedAuthority;

import it.thisone.iotter.security.EntityPermission;
import it.thisone.iotter.security.Permissions;
import it.thisone.iotter.security.UserDetailsAdapter;

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

	public static Permissions getPermissionsForDeviceEntity(UserDetailsAdapter currentUser) {
		Permissions permissions = new Permissions();
		permissions.setActivateMode(hasPermission(currentUser,EntityPermission.DEVICE.ACTIVATE));
		permissions.setCreateMode(hasPermission(currentUser,EntityPermission.DEVICE.CREATE));
		permissions.setModifyMode(hasPermission(currentUser,EntityPermission.DEVICE.MODIFY));
		permissions.setRemoveMode(hasPermission(currentUser,EntityPermission.DEVICE.REMOVE));
		permissions.setViewAllMode(hasPermission(currentUser,EntityPermission.DEVICE.VIEWALL));
		permissions.setViewMode(hasPermission(currentUser,EntityPermission.DEVICE.VIEW));
		
		return permissions;
	}
	
	


	public static Permissions getPermissionsForNetworkEntity(UserDetailsAdapter currentUser) {
		Permissions permissions = new Permissions();
		permissions.setCreateMode(hasPermission(currentUser,EntityPermission.NETWORK.CREATE));
		permissions.setModifyMode(hasPermission(currentUser,EntityPermission.NETWORK.MODIFY));
		permissions.setRemoveMode(hasPermission(currentUser,EntityPermission.NETWORK.REMOVE));
		permissions.setViewAllMode(hasPermission(currentUser,EntityPermission.NETWORK.VIEWALL));
		permissions.setViewMode(hasPermission(currentUser,EntityPermission.NETWORK.VIEW));
		return permissions;
	}
	
	public static Permissions getPermissionsForNetworkGroupEntity(UserDetailsAdapter currentUser) {
		Permissions permissions = new Permissions();
		permissions.setCreateMode(hasPermission(currentUser,EntityPermission.NETWORK_GROUP.CREATE));
		permissions.setModifyMode(hasPermission(currentUser,EntityPermission.NETWORK_GROUP.MODIFY));
		permissions.setRemoveMode(hasPermission(currentUser,EntityPermission.NETWORK_GROUP.REMOVE));
		permissions.setViewAllMode(hasPermission(currentUser,EntityPermission.NETWORK_GROUP.VIEWALL));
		permissions.setViewMode(hasPermission(currentUser,EntityPermission.NETWORK_GROUP.VIEW));
		return permissions;
	}

	

	public static Permissions getPermissionsForUserEntity(UserDetailsAdapter currentUser) {
		Permissions permissions = new Permissions();
		permissions.setCreateMode(hasPermission(currentUser,EntityPermission.USER.CREATE));
		permissions.setModifyMode(hasPermission(currentUser,EntityPermission.USER.MODIFY));
		permissions.setRemoveMode(hasPermission(currentUser,EntityPermission.USER.REMOVE));
		permissions.setViewAllMode(hasPermission(currentUser,EntityPermission.USER.VIEWALL));
		permissions.setViewMode(hasPermission(currentUser,EntityPermission.USER.VIEW));
		return permissions;
	}
	
	public static Permissions getPermissionsForTracingEntity(UserDetailsAdapter currentUser) {
		Permissions permissions = new Permissions();
		permissions.setCreateMode(false);
		permissions.setModifyMode(false);
		permissions.setRemoveMode(false);
		permissions.setViewAllMode(hasPermission(currentUser,EntityPermission.TRACING.VIEWALL));
		permissions.setViewMode(hasPermission(currentUser,EntityPermission.TRACING.VIEW));
		return permissions;
	}

	public static Permissions getPermissionsForGroupWidgetEntity(UserDetailsAdapter currentUser) {
		Permissions permissions = new Permissions();
		permissions.setCreateMode(hasPermission(currentUser,EntityPermission.GROUP_WIDGET.CREATE));
		permissions.setModifyMode(hasPermission(currentUser,EntityPermission.GROUP_WIDGET.MODIFY));
		permissions.setRemoveMode(hasPermission(currentUser,EntityPermission.GROUP_WIDGET.REMOVE));
		permissions.setViewAllMode(hasPermission(currentUser,EntityPermission.GROUP_WIDGET.VIEWALL));
		permissions.setViewMode(hasPermission(currentUser,EntityPermission.GROUP_WIDGET.VIEW));
		return permissions;
	}


	public static Permissions getPermissionsForDeviceModelEntity(UserDetailsAdapter currentUser) {
		Permissions permissions = new Permissions();
		permissions.setCreateMode(hasPermission(currentUser,EntityPermission.DEVICE_MODEL.CREATE));
		permissions.setModifyMode(hasPermission(currentUser,EntityPermission.DEVICE_MODEL.MODIFY));
		permissions.setRemoveMode(hasPermission(currentUser,EntityPermission.DEVICE_MODEL.REMOVE));
		permissions.setViewAllMode(hasPermission(currentUser,EntityPermission.DEVICE_MODEL.VIEWALL));
		permissions.setViewMode(hasPermission(currentUser,EntityPermission.DEVICE_MODEL.VIEW));
		return permissions;
	}


	public static Permissions getPermissionsForMeasureUnitTypeEntity(UserDetailsAdapter currentUser) {
		Permissions permissions = new Permissions();
		permissions.setCreateMode(hasPermission(currentUser,EntityPermission.MEASURE_UNIT_TYPE.CREATE));
		permissions.setModifyMode(hasPermission(currentUser,EntityPermission.MEASURE_UNIT_TYPE.MODIFY));
		permissions.setRemoveMode(hasPermission(currentUser,EntityPermission.MEASURE_UNIT_TYPE.REMOVE));
		permissions.setViewAllMode(hasPermission(currentUser,EntityPermission.MEASURE_UNIT_TYPE.VIEWALL));
		permissions.setViewMode(hasPermission(currentUser,EntityPermission.MEASURE_UNIT_TYPE.VIEW));
		return permissions;
	}


	public static Permissions getPermissionsForModbusProfileEntity(UserDetailsAdapter currentUser) {
		Permissions permissions = new Permissions();
		permissions.setCreateMode(hasPermission(currentUser,EntityPermission.MODBUS_PROFILE.CREATE));
		permissions.setModifyMode(hasPermission(currentUser,EntityPermission.MODBUS_PROFILE.MODIFY));
		permissions.setRemoveMode(hasPermission(currentUser,EntityPermission.MODBUS_PROFILE.REMOVE));
		//permissions.setViewAllMode(hasPermission(currentUser,EntityPermission.MODBUS_PROFILE.VIEWALL));
		permissions.setViewMode(hasPermission(currentUser,EntityPermission.MODBUS_PROFILE.VIEW));
		return permissions;
	}
	
	
	private static boolean hasPermission(UserDetailsAdapter currentUser, EntityPermission permission) {
		for (GrantedAuthority ga : currentUser.getAuthorities()) {
			if (ga.getAuthority().equals(permission.toString())) {
				return true;
			}
		}

		return false;
	}

	
}
