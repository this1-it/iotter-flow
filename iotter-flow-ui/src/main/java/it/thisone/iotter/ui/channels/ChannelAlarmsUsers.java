package it.thisone.iotter.ui.channels;

import org.vaadin.flow.components.TabSheet;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import it.thisone.iotter.persistence.model.NetworkGroup;
import it.thisone.iotter.ui.common.BaseEditor;
import it.thisone.iotter.ui.common.UIUtils;

public class ChannelAlarmsUsers extends BaseEditor<NetworkGroup> {

	private static final long serialVersionUID = 1L;

	public ChannelAlarmsUsers(NetworkGroup group) {
		super("networkgroup.bindings", "alarm.notification");
		setItem(group);

		VerticalLayout mainLayout = new VerticalLayout();
		mainLayout.setSizeFull();
		mainLayout.setSpacing(true);

		Component content = buildContent();
		mainLayout.add(content);
		mainLayout.setFlexGrow(1f, content);
		mainLayout.add(buildFooter());

		getContent().removeAll();
		getContent().add(mainLayout);
	}

	private Component buildContent() {
		TabSheet tabSheet = new TabSheet();
		tabSheet.setSizeFull();
		tabSheet.addTab(getI18nLabel("users_tab"), new Span(getTranslation("basic.editor.pending_changes")));
		return tabSheet;
	}

	private Component buildFooter() {
		HorizontalLayout footer = new HorizontalLayout();
		footer.setWidthFull();
		footer.setSpacing(true);

		Button saveButton = createSaveButton();
		saveButton.setText(getI18nLabel("save_button"));
		saveButton.setIcon(null);

		Button cancelButton = createCancelButton();
		cancelButton.setText(getI18nLabel("cancel_button"));
		cancelButton.setIcon(null);

		footer.add(saveButton, cancelButton);
		return footer;
	}

	@Override
	protected void onSave() {
	}

	@Override
	protected void onCancel() {
	}

	public String getWindowStyle() {
		return "networkgroup-members-editor";
	}

	public float[] getWindowDimension() {
		return UIUtils.XL_DIMENSION;
	}
}
