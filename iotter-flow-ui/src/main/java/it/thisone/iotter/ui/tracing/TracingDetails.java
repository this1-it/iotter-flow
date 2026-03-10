package it.thisone.iotter.ui.tracing;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;

import it.thisone.iotter.persistence.model.Tracing;

public class TracingDetails extends Composite<VerticalLayout> {

	private static final long serialVersionUID = 1L;
	private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

	public TracingDetails(Tracing item) {
		super();
		getContent().add(buildContent(item));
	}

	private Component buildContent(Tracing item) {
		VerticalLayout mainLayout = new VerticalLayout();
		mainLayout.setSpacing(true);
		mainLayout.setPadding(true);
		mainLayout.setSizeFull();

		FormLayout formLayout = new FormLayout();
		formLayout.setWidthFull();

		formLayout.add(readOnlyField("action", item != null && item.getAction() != null ? item.getAction().name() : ""));
		formLayout.add(readOnlyField("owner", item != null ? item.getOwner() : ""));
		formLayout.add(readOnlyField("administrator", item != null ? item.getAdministrator() : ""));
		formLayout.add(readOnlyField("network", item != null ? item.getNetwork() : ""));
		formLayout.add(readOnlyField("device", item != null ? item.getDevice() : ""));
		formLayout.add(readOnlyField("timeStamp", formatTimestamp(item)));

		TextArea description = new TextArea(getI18nLabel("description"));
		description.setReadOnly(true);
		description.setWidthFull();
		description.setMinHeight("10em");
		description.setValue(item != null && item.getDescription() != null ? item.getDescription() : "");
		formLayout.add(description);

		mainLayout.add(formLayout);
		return mainLayout;
	}

	private TextField readOnlyField(String key, String value) {
		TextField field = new TextField(getI18nLabel(key));
		field.setReadOnly(true);
		field.setWidthFull();
		field.setValue(value != null ? value : "");
		return field;
	}

	private String formatTimestamp(Tracing item) {
		if (item == null || item.getTimeStamp() == null) {
			return "";
		}
		return item.getTimeStamp().toInstant().atZone(ZoneId.systemDefault()).format(TIMESTAMP_FORMATTER);
	}

    public String getI18nLabel(String key) {
        return getTranslation("tracing.view" + "." + key);
    }
}
