package it.thisone.iotter.ui.common.fields;

import java.util.List;

import com.vaadin.flow.component.combobox.ComboBox;

import it.thisone.iotter.persistence.model.Role;

public class RoleSelect extends ComboBox<Role> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public RoleSelect(List<Role> roles) {
        super("", roles);
        this.setItemLabelGenerator(Role::getName);
    }

	
}
