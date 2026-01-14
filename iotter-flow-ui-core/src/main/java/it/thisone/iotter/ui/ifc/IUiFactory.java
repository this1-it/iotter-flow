package it.thisone.iotter.ui.ifc;

public interface IUiFactory {

	IDeviceUiFactory getDeviceUiFactory();
	
	IGroupWidgetUiFactory getGroupWidgetUiFactory();
}
