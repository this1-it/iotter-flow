package it.thisone.iotter.ui.validators;

import java.util.Set;

import com.vaadin.flow.data.binder.ValidationResult;
import com.vaadin.flow.data.binder.Validator;
import com.vaadin.flow.data.binder.ValueContext;

import it.thisone.iotter.persistence.model.Network;
import it.thisone.iotter.persistence.model.NetworkGroup;
import it.thisone.iotter.ui.common.UIUtils;

public final class NetworkGroupsMembershipValidator implements Validator<Object> {
    /** The default serial version UID. */
    private static final long serialVersionUID = 1L;
    private boolean mandatory;
    
    public NetworkGroupsMembershipValidator(boolean mandatory) {
    	this.mandatory = mandatory;
    }

	@Override
	public ValidationResult apply(Object value, ValueContext context) {
        if(mandatory && value == null) {
        	return ValidationResult.error(UIUtils.localize("validators.group_is_mandatory_for_network_membership"));
        }
    	if (value == null){
        	return ValidationResult.ok();
        }
    	// TODO debug
        if (value instanceof NetworkGroup) {
        	return ValidationResult.ok();
        }
        
        @SuppressWarnings("unchecked")
        Set<NetworkGroup> groups = (Set<NetworkGroup>) value;
        if (groups.isEmpty() && mandatory) {
        	return ValidationResult.error(UIUtils.localize("validators.group_is_mandatory_for_network_membership"));
        }

        if (!groups.isEmpty()) {
            Network network = groups.iterator().next().getNetwork();
            
            for (NetworkGroup group : groups) {
    			if (!group.getNetwork().equals(network)) {
    		         return ValidationResult.error(UIUtils.localize("validators.groups_belongs_to_different_networks"));
    			}
    		}
        }
    	return ValidationResult.ok();
	}
}