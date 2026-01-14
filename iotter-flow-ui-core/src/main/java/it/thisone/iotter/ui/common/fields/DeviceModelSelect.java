package it.thisone.iotter.ui.common.fields;

import java.util.Collection;

import com.vaadin.flow.component.combobox.ComboBox;

import it.thisone.iotter.persistence.model.DeviceModel;

public class DeviceModelSelect extends ComboBox<DeviceModel> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

    /**
     * Default constructor which populates the select with existing device models.
     */
    public DeviceModelSelect(Collection<DeviceModel> options) {
        super("", options);
        this.setLabelCaptionGenerator(DeviceModel::getName);
    }
    
  	
}
