package it.thisone.iotter.ui.maps;

import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;

import it.thisone.iotter.enums.AlarmStatus;

public class DeviceAlarmedLabel extends Span {

    private static final long serialVersionUID = -6930068764985328608L;

    private String serial;

    public DeviceAlarmedLabel(String content) {
        setText(content != null ? content : "");
    }

    public void change(AlarmStatus status) {
        AlarmStatus resolved = status != null ? status : AlarmStatus.UNDEFINED;

        removeAll();
        removeClassName("active-alarm");
        removeClassName("no-alarm");

        switch (resolved) {
            case ON:
                add(VaadinIcon.BELL.create());
                addClassName("active-alarm");
                break;
            case OFF:
                add(VaadinIcon.BELL.create());
                addClassName("no-alarm");
                break;
            case OFFLINE:
                add(VaadinIcon.PLUG.create());
                addClassName("active-alarm");
                break;
            default:
                setText("");
                addClassName("no-alarm");
                break;
        }
    }

    public void setDeviceStatus(String serial, AlarmStatus status) {
        this.serial = serial;
        change(status);
    }

    public String getSerial() {
        return serial;
    }
}
