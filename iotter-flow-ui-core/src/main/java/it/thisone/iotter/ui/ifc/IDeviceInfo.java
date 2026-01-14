package it.thisone.iotter.ui.ifc;

import java.util.Collection;

import com.vaadin.flow.component.Component;

import it.thisone.iotter.persistence.model.Device;
import it.thisone.iotter.persistence.model.GroupWidget;
import it.thisone.iotter.ui.common.ItemSelectedListener;

public interface IDeviceInfo extends Component {

	void setContent(Device device, Collection<GroupWidget> widgets);
	
	void addListener(ItemSelectedListener listener);

	void removeListener(ItemSelectedListener listener);


}