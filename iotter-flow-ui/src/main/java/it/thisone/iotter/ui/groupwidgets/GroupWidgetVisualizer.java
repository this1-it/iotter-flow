package it.thisone.iotter.ui.groupwidgets;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.Subscribe;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import it.thisone.iotter.ui.common.BaseComponent;
import it.thisone.iotter.ui.eventbus.WidgetRefreshEvent;

/**
 * Flow-migrated placeholder visualizer.
 *
 * TODO: Restore full widget rendering and controls with explicit service wiring
 * (GroupWidgetService, UIEventBus, and chart/widget factories) instead of legacy
 * UIUtils/IMainUI access.
 */
public class GroupWidgetVisualizer extends BaseComponent {

	public static final Logger logger = LoggerFactory.getLogger(GroupWidgetVisualizer.class);
	private static final long serialVersionUID = -6776667672616201904L;

	private final String entityId;
	private final boolean tabContext;
	private final Span status = new Span();

	public GroupWidgetVisualizer(String entityId, boolean isTab) {
		super("groupwidget.visualizer");
		this.entityId = entityId;
		this.tabContext = isTab;
		setId(entityId);
		buildLayout();
	}

	private void buildLayout() {
		VerticalLayout root = new VerticalLayout();
		root.setSizeFull();
		root.setSpacing(false);
		root.setPadding(false);

		HorizontalLayout toolbar = new HorizontalLayout();
		toolbar.setWidthFull();
		toolbar.setSpacing(true);
		toolbar.setPadding(true);
		toolbar.addClassName("timecontrols");

		Button exportButton = new Button(VaadinIcon.DOWNLOAD.create());
		exportButton.getElement().setProperty("title", getI18nLabel("export_data"));
		exportButton.setEnabled(false);

		status.setText(String.format("GroupWidget %s", entityId));
		toolbar.add(status, exportButton);
		toolbar.setFlexGrow(1f, status);

		Div canvas = new Div();
		canvas.setSizeFull();
		canvas.addClassName("groupwidget-canvas");
		canvas.setText(getTranslation("basic.editor.pending_changes"));

		root.add(toolbar, canvas);
		root.setFlexGrow(1f, canvas);

		getContent().removeAll();
		getContent().add(root);
	}

	public void removeListeners() {
		// No-op in Flow placeholder implementation.
	}

	@Subscribe
	public void refreshWithUiAccess(WidgetRefreshEvent event) {
		status.setText(String.format("GroupWidget %s - refresh %tT", entityId, new Date()));
	}

	public String getEntityId() {
		return entityId;
	}

	public boolean isTabContext() {
		return tabContext;
	}
}
