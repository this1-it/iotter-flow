package it.thisone.iotter.ui.common.fields;

import java.util.ArrayList;
import java.util.List;

import com.vaadin.flow.component.combobox.ComboBox;

import it.thisone.iotter.config.Constants;
import it.thisone.iotter.persistence.model.Role;
import it.thisone.iotter.ui.common.UIUtils;

public class RoleSelect extends ComboBox<Role> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

    /**
     * Default constructor which populates the select with existing roles.
     */
    public RoleSelect() {
        super("", retrieveAllRoles());
        this.setItemLabelGenerator(Role::getName);
    }
    
    public RoleSelect(List<Role> roles) {
        super("", roles);
        this.setItemLabelGenerator(Role::getName);
    }

	public void setOptionsForCreation() {
        setItems( retrieveRolesForCreation());
    }

	public static List<Role> retrieveAllRoles() {
		List<Role> roles = new ArrayList<Role>();
		if (UIUtils.hasRole(Constants.ROLE_SUPERVISOR)) {
			roles = UIUtils.getServiceFactory().getRoleService().findAll();
		} else {
			roles.add(UIUtils.getServiceFactory().getRoleService().findByName(Constants.ROLE_ADMINISTRATOR));
			roles.add(UIUtils.getServiceFactory().getRoleService().findByName(Constants.ROLE_SUPERUSER));
			roles.add(UIUtils.getServiceFactory().getRoleService().findByName(Constants.ROLE_USER));
		}
		return roles;
	}

	public static List<Role> retrieveRolesForCreation() {
		List<Role> roles = new ArrayList<Role>();
		if (UIUtils.hasRole(Constants.ROLE_SUPERVISOR)) {
			roles.add(UIUtils.getServiceFactory().getRoleService().findByName(Constants.ROLE_ADMINISTRATOR));
			roles.add(UIUtils.getServiceFactory().getRoleService().findByName(Constants.ROLE_PRODUCTION));
			roles.add(UIUtils.getServiceFactory().getRoleService().findByName(Constants.ROLE_FINANCE));
		} else {
			roles.add(UIUtils.getServiceFactory().getRoleService().findByName(Constants.ROLE_SUPERUSER));
			roles.add(UIUtils.getServiceFactory().getRoleService().findByName(Constants.ROLE_USER));
		}
		return roles;
	}
	
	
}
