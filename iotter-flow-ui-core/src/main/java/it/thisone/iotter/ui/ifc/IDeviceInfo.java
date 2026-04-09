package it.thisone.iotter.ui.ifc;

import java.util.Collection;

import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.shared.Registration;
import it.thisone.iotter.persistence.model.Device;
import it.thisone.iotter.persistence.model.GroupWidget;
import it.thisone.iotter.ui.common.GroupWidgetSelectedEvent;

public interface IDeviceInfo {

	void setContent(Device device, Collection<GroupWidget> widgets);

	Registration addGroupWidgetSelectedListener(ComponentEventListener<GroupWidgetSelectedEvent> listener);
}