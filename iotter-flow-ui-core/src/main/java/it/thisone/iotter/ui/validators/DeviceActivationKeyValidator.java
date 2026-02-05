package it.thisone.iotter.ui.validators;

import com.vaadin.flow.data.binder.ValidationResult;
import com.vaadin.flow.data.binder.Validator;
import com.vaadin.flow.data.binder.ValueContext;

import it.thisone.iotter.persistence.model.Device;

public final class DeviceActivationKeyValidator implements Validator<String> {
    private static final long serialVersionUID = 1L;
    private String serial;


    public DeviceActivationKeyValidator(String serial ) {
        this.serial = serial;
    }

    @Override
    public ValidationResult apply(String value, ValueContext context) {
        String activationKey = value;
        
//        if (serial != null) {
//            Device device = UIUtils.getServiceFactory().getDeviceService().findBySerial(serial);
//            boolean found = device != null && device.isAvailableForActivation() && device.getActivationKey().equals(activationKey);
//            if (!found) {
//                return ValidationResult.error(getTranslation("validators.device_activation_not_valid"));
//            }
//        }
        return ValidationResult.ok();
    }
}