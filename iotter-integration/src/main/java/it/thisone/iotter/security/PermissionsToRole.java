package it.thisone.iotter.security;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import it.thisone.iotter.config.Constants;


/**
 * assign permission on entities based on role
 * 
 * @author tisone
 *
 */
public final class PermissionsToRole implements Serializable {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	
	/**
     * Flag reflecting whether initialization of permission has been done
     */
    private static boolean initialized = false;

    /**
     * Map of role name / permission.
     */
    private static Map<String, List<EntityPermission>> permissions = new HashMap<String, List<EntityPermission>>();

    /**
     * Adds a permission for given role name.
     * @param roleName
     * @param permission
     */
    public static void add(final String roleName, final EntityPermission permission) {
        if (!permissions.containsKey(roleName)) {
        	permissions.put(roleName, new ArrayList<EntityPermission>());
        }
        permissions.get(roleName).add(permission);
    }
    
    public static void remove(final String roleName, final EntityPermission permission) {
        if (permissions.containsKey(roleName)) {
        	permissions.get(roleName).remove(permission);
        }
    	
    }

    
    /**
     * Gets permission for given role name.
     * @param roleName The role name.
     * @return an unmodifiable list of permission.
     */
    public static List<EntityPermission> getPermissions(String roleName) {
        if (!permissions.containsKey(roleName)) {
            return new ArrayList<EntityPermission>();
        }
        return Collections.unmodifiableList(permissions.get(roleName));
    }
    
    /**
     * Initialize permission if not done yet.
     * Each role can be associated to a list of permissions
     */
    public static synchronized void initialize() {
        if (initialized) {
            return;
        }
        initialized = true;
        
        /* supervisor start */
        PermissionsToRole.add(Constants.ROLE_SUPERVISOR, EntityPermission.MODBUS_PROFILE.CREATE);
        PermissionsToRole.add(Constants.ROLE_SUPERVISOR, EntityPermission.MODBUS_PROFILE.MODIFY);
        PermissionsToRole.add(Constants.ROLE_SUPERVISOR, EntityPermission.MODBUS_PROFILE.VIEW);
        PermissionsToRole.add(Constants.ROLE_SUPERVISOR, EntityPermission.MODBUS_PROFILE.VIEWALL);
        PermissionsToRole.add(Constants.ROLE_SUPERVISOR, EntityPermission.MODBUS_PROFILE.REMOVE);
        
        PermissionsToRole.add(Constants.ROLE_SUPERVISOR, EntityPermission.USER.CREATE);
        PermissionsToRole.add(Constants.ROLE_SUPERVISOR, EntityPermission.USER.MODIFY);
        PermissionsToRole.add(Constants.ROLE_SUPERVISOR, EntityPermission.USER.VIEW);
        PermissionsToRole.add(Constants.ROLE_SUPERVISOR, EntityPermission.USER.VIEWALL);
        PermissionsToRole.add(Constants.ROLE_SUPERVISOR, EntityPermission.USER.REMOVE);

        PermissionsToRole.add(Constants.ROLE_SUPERVISOR, EntityPermission.DEVICE.CREATE);
        PermissionsToRole.add(Constants.ROLE_SUPERVISOR, EntityPermission.DEVICE.MODIFY);
        PermissionsToRole.add(Constants.ROLE_SUPERVISOR, EntityPermission.DEVICE.VIEW);
        PermissionsToRole.add(Constants.ROLE_SUPERVISOR, EntityPermission.DEVICE.VIEWALL);
        PermissionsToRole.add(Constants.ROLE_SUPERVISOR, EntityPermission.DEVICE.REMOVE);
        PermissionsToRole.add(Constants.ROLE_SUPERVISOR, EntityPermission.DEVICE.RESET);
        PermissionsToRole.add(Constants.ROLE_SUPERVISOR, EntityPermission.DEVICE.MIGRATE);
        PermissionsToRole.add(Constants.ROLE_SUPERVISOR, EntityPermission.DEVICE.EXPORT_DATA);

        PermissionsToRole.add(Constants.ROLE_SUPERVISOR, EntityPermission.DEVICE_MODEL.CREATE);
        PermissionsToRole.add(Constants.ROLE_SUPERVISOR, EntityPermission.DEVICE_MODEL.MODIFY);
        PermissionsToRole.add(Constants.ROLE_SUPERVISOR, EntityPermission.DEVICE_MODEL.VIEW);
        PermissionsToRole.add(Constants.ROLE_SUPERVISOR, EntityPermission.DEVICE_MODEL.VIEWALL);
        PermissionsToRole.add(Constants.ROLE_SUPERVISOR, EntityPermission.DEVICE_MODEL.REMOVE);

        PermissionsToRole.add(Constants.ROLE_SUPERVISOR, EntityPermission.MEASURE_UNIT_TYPE.CREATE);
        PermissionsToRole.add(Constants.ROLE_SUPERVISOR, EntityPermission.MEASURE_UNIT_TYPE.MODIFY);
        PermissionsToRole.add(Constants.ROLE_SUPERVISOR, EntityPermission.MEASURE_UNIT_TYPE.VIEW);
        PermissionsToRole.add(Constants.ROLE_SUPERVISOR, EntityPermission.MEASURE_UNIT_TYPE.VIEWALL);
        PermissionsToRole.add(Constants.ROLE_SUPERVISOR, EntityPermission.MEASURE_UNIT_TYPE.REMOVE);
        
        PermissionsToRole.add(Constants.ROLE_SUPERVISOR, EntityPermission.TRACING.VIEWALL);
        PermissionsToRole.add(Constants.ROLE_SUPERVISOR, EntityPermission.NETWORK.VIEWALL);
        PermissionsToRole.add(Constants.ROLE_SUPERVISOR, EntityPermission.GROUP_WIDGET.VIEWALL);
        PermissionsToRole.add(Constants.ROLE_SUPERVISOR, EntityPermission.GROUP_WIDGET.MODIFY);
        PermissionsToRole.add(Constants.ROLE_SUPERVISOR, EntityPermission.NETWORK.MODIFY);
        PermissionsToRole.add(Constants.ROLE_SUPERVISOR, EntityPermission.NETWORK.REMOVE);
        PermissionsToRole.add(Constants.ROLE_SUPERVISOR, EntityPermission.GROUP_WIDGET.REMOVE);
        /* supervisor end */

        /* production start */
        PermissionsToRole.add(Constants.ROLE_PRODUCTION, EntityPermission.DEVICE.CREATE);
        PermissionsToRole.add(Constants.ROLE_PRODUCTION, EntityPermission.DEVICE.VIEW);
        PermissionsToRole.add(Constants.ROLE_PRODUCTION, EntityPermission.DEVICE.REMOVE);
        PermissionsToRole.add(Constants.ROLE_PRODUCTION, EntityPermission.DEVICE.MODIFY);
        PermissionsToRole.add(Constants.ROLE_PRODUCTION, EntityPermission.DEVICE.IMPORT);
        PermissionsToRole.add(Constants.ROLE_PRODUCTION, EntityPermission.DEVICE.EXPORT);
        //PermissionsToRole.add(Constants.ROLE_PRODUCTION, EntityPermission.DEVICE.RESET);
        //PermissionsToRole.add(Constants.ROLE_PRODUCTION, EntityPermission.DEVICE.VIEWALL);
        
        PermissionsToRole.add(Constants.ROLE_PRODUCTION, EntityPermission.DEVICE_MODEL.CREATE);
        PermissionsToRole.add(Constants.ROLE_PRODUCTION, EntityPermission.DEVICE_MODEL.MODIFY);
        PermissionsToRole.add(Constants.ROLE_PRODUCTION, EntityPermission.DEVICE_MODEL.VIEW);
        PermissionsToRole.add(Constants.ROLE_PRODUCTION, EntityPermission.DEVICE_MODEL.VIEWALL);
        PermissionsToRole.add(Constants.ROLE_PRODUCTION, EntityPermission.DEVICE_MODEL.REMOVE);
 
        PermissionsToRole.add(Constants.ROLE_PRODUCTION, EntityPermission.MEASURE_UNIT_TYPE.CREATE);
        PermissionsToRole.add(Constants.ROLE_PRODUCTION, EntityPermission.MEASURE_UNIT_TYPE.MODIFY);
        PermissionsToRole.add(Constants.ROLE_PRODUCTION, EntityPermission.MEASURE_UNIT_TYPE.VIEW);
        PermissionsToRole.add(Constants.ROLE_PRODUCTION, EntityPermission.MEASURE_UNIT_TYPE.VIEWALL);
        PermissionsToRole.add(Constants.ROLE_PRODUCTION, EntityPermission.MEASURE_UNIT_TYPE.REMOVE);
        
        /* production end */
       
        /* administrator start */
        PermissionsToRole.add(Constants.ROLE_ADMINISTRATOR, EntityPermission.USER.CREATE);
        PermissionsToRole.add(Constants.ROLE_ADMINISTRATOR, EntityPermission.USER.MODIFY);
        PermissionsToRole.add(Constants.ROLE_ADMINISTRATOR, EntityPermission.USER.VIEW);
        PermissionsToRole.add(Constants.ROLE_ADMINISTRATOR, EntityPermission.USER.REMOVE);

        PermissionsToRole.add(Constants.ROLE_ADMINISTRATOR, EntityPermission.DEVICE.MODIFY);
        PermissionsToRole.add(Constants.ROLE_ADMINISTRATOR, EntityPermission.DEVICE.VIEW);
        PermissionsToRole.add(Constants.ROLE_ADMINISTRATOR, EntityPermission.DEVICE.ACTIVATE);
        PermissionsToRole.add(Constants.ROLE_ADMINISTRATOR, EntityPermission.DEVICE.MIGRATE);
        PermissionsToRole.add(Constants.ROLE_ADMINISTRATOR, EntityPermission.DEVICE.RESET);
        PermissionsToRole.add(Constants.ROLE_ADMINISTRATOR, EntityPermission.DEVICE.EXPORT_DATA);
        //PermissionsToRole.add(Constants.ROLE_ADMINISTRATOR, EntityPermission.DEVICE.REMOVE);

        PermissionsToRole.add(Constants.ROLE_ADMINISTRATOR, EntityPermission.NETWORK.VIEW);
        PermissionsToRole.add(Constants.ROLE_ADMINISTRATOR, EntityPermission.NETWORK.MODIFY);
        PermissionsToRole.add(Constants.ROLE_ADMINISTRATOR, EntityPermission.NETWORK.CREATE);
        PermissionsToRole.add(Constants.ROLE_ADMINISTRATOR, EntityPermission.NETWORK.REMOVE);

        PermissionsToRole.add(Constants.ROLE_ADMINISTRATOR, EntityPermission.NETWORK_GROUP.VIEW);
        PermissionsToRole.add(Constants.ROLE_ADMINISTRATOR, EntityPermission.NETWORK_GROUP.MODIFY);
        PermissionsToRole.add(Constants.ROLE_ADMINISTRATOR, EntityPermission.NETWORK_GROUP.CREATE);
        PermissionsToRole.add(Constants.ROLE_ADMINISTRATOR, EntityPermission.NETWORK_GROUP.REMOVE);
        
        PermissionsToRole.add(Constants.ROLE_ADMINISTRATOR, EntityPermission.GROUP_WIDGET.VIEW);
        PermissionsToRole.add(Constants.ROLE_ADMINISTRATOR, EntityPermission.GROUP_WIDGET.MODIFY);
        PermissionsToRole.add(Constants.ROLE_ADMINISTRATOR, EntityPermission.GROUP_WIDGET.CREATE);
        PermissionsToRole.add(Constants.ROLE_ADMINISTRATOR, EntityPermission.GROUP_WIDGET.REMOVE);
        
        /* administrator end */
      
        PermissionsToRole.add(Constants.ROLE_SUPERUSER, EntityPermission.USER.VIEW);
        PermissionsToRole.add(Constants.ROLE_SUPERUSER, EntityPermission.DEVICE.MODIFY);
        PermissionsToRole.add(Constants.ROLE_SUPERUSER, EntityPermission.DEVICE.VIEW);
        PermissionsToRole.add(Constants.ROLE_SUPERUSER, EntityPermission.DEVICE.EXPORT_DATA);
        PermissionsToRole.add(Constants.ROLE_SUPERUSER, EntityPermission.GROUP_WIDGET.VIEW);
        PermissionsToRole.add(Constants.ROLE_SUPERUSER, EntityPermission.GROUP_WIDGET.MODIFY);
        PermissionsToRole.add(Constants.ROLE_SUPERUSER, EntityPermission.GROUP_WIDGET.CREATE);
        PermissionsToRole.add(Constants.ROLE_SUPERUSER, EntityPermission.GROUP_WIDGET.REMOVE);
 
        
        PermissionsToRole.add(Constants.ROLE_USER, EntityPermission.GROUP_WIDGET.VIEW);
        
    }
}
