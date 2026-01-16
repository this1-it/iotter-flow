package it.thisone.iotter.util;

import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;

public class PopupNotification {

    public enum Type {
        HUMANIZED,
        WARNING,
        ERROR,
        SUCCESS
    }

    public static void show(String message, Type type) {

        Html content = new Html(
            "<span>" + message + "&nbsp;&nbsp;&nbsp;&times;</span>"
        );

        Notification notification = new Notification(content);
        notification.setDuration(3000);
        notification.setPosition(Notification.Position.MIDDLE);

        switch (type) {
            case WARNING:
                notification.addThemeVariants(NotificationVariant.LUMO_CONTRAST);
                break;
            case ERROR:
                notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
                break;
            case SUCCESS:
                notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                break;
            case HUMANIZED:
            default:
                // default theme
                break;
        }

        notification.open();
    }

    public static void show(String message) {
        show(message, Type.HUMANIZED);
    }
}
