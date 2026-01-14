package it.thisone.iotter.util;

import com.vaadin.server.Page;
import com.vaadin.flow.component.notification.Notification;

public class PopupNotification {
	
	public static void show(String message, Notification.Type type) {		
		message = String.format("%s&nbsp;&nbsp;&nbsp;&times;", message);		
		Notification notification = new Notification(message, type);
		notification.setHtmlContentAllowed(true);
		notification.show(Page.getCurrent());
	}
	
	public static void show(String message) {
		show(message, Notification.Type.HUMANIZED_MESSAGE);
	}
}
