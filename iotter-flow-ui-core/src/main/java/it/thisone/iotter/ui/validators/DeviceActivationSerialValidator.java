package it.thisone.iotter.ui.validators;

import com.vaadin.flow.data.binder.ValidationResult;
import com.vaadin.flow.data.binder.Validator;
import com.vaadin.flow.data.binder.ValueContext;

public final class DeviceActivationSerialValidator implements Validator<String> {
    public static String CATCH_ALL_SERIAL = "TISONE";
	
	/** The default serial version UID. */
    private static final long serialVersionUID = 1L;

    /**
     * The activation property ID used to trigger revalidation of
     * that property when device serial property changes.
     */
    private String deviceActivationPropertyId;
    
    public DeviceActivationSerialValidator(String deviceActivationPropertyId) {

        this.deviceActivationPropertyId = deviceActivationPropertyId;
    }



	@Override
	public ValidationResult apply(String value, ValueContext context) {
        String serial = value;
        
//        if (serial != null && serial.equals(CATCH_ALL_SERIAL)) {
//        	return ValidationResult.ok();
//        }
//        
//        editor.validateField(deviceActivationPropertyId);
//		if(serial != null && !serial.trim().isEmpty()) {
//			Device device = UIUtils.getServiceFactory().getDeviceService().findBySerial(serial.trim());
//			boolean found = (device != null && device.isAvailableForActivation());
//			if (!found) {
//		         return ValidationResult.error(UIUtils.localize("validators.device_serial_not_found"));
//			}
//		}
        return ValidationResult.ok();
	}
}