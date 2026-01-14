package it.thisone.iotter.ui.eventbus;
/*
 * used to open a visualization,  clicking on a link in a infowindow
 */
public class DeviceGroupWidgetEvent {
    private final String device;
    private final String widget;
    public DeviceGroupWidgetEvent(String device, String widget) {
        this.device = device;
        this.widget = widget;
    }
    public String getDevice() {
        return device;
    }
    public String getWidget() {
        return widget;
    }
}
