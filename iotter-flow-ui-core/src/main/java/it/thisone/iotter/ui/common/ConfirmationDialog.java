package it.thisone.iotter.ui.common;


import com.vaadin.shared.ui.ContentMode;
import com.vaadin.flow.component.Alignment;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.Button.ClickEvent;
import com.vaadin.flow.component.button.Button.ClickListener;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.html.Span;
import org.vaadin.flow.components.PanelFlow;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.themes.ValoTheme;

public class ConfirmationDialog extends Dialog {

	 /**
     * Callback class for a {@link ConfirmationDialog}.
     */
    public static interface Callback {
        /**
         * Called upon pressing a button.
         * 
         * @param buttonName
         *            the name of the button that was clicked, never <code>null</code>.
         */
        void onDialogResult(boolean result);
    }
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Button confirm;
	private Button cancel;

	public ConfirmationDialog(String caption, String message, final Callback callback) {
		this(caption, new Span(message, ContentMode.HTML), callback);
	}

	public ConfirmationDialog(String caption, Component component, final Callback callback) {
		super();
		setHeaderTitle(caption);
		BaseComponent.makeResponsiveDialog(this, UIUtils.S_DIMENSION, UIUtils.S_WINDOW_STYLE);
		setDraggable(false);
		// setImmediate(true);

		VerticalLayout verticalLayout = new VerticalLayout();
		verticalLayout.setDefaultComponentAlignment(Alignment.MIDDLE_CENTER);
		verticalLayout.setSizeFull();
		verticalLayout.setSpacing(true);
		add(verticalLayout);

        PanelFlow panel = new PanelFlow();
		panel.setContent(component);
		panel.setSizeFull();
		
		HorizontalLayout content = new HorizontalLayout();
		content.setDefaultComponentAlignment(Alignment.MIDDLE_CENTER);
		content.setSizeFull();
		content.addComponent(panel);
		content.setPadding(true);
		verticalLayout.addComponent(content);
		verticalLayout.setExpandRatio(content, 1f);
		
		HorizontalLayout buttonLayout = new HorizontalLayout();
		buttonLayout.setSpacing(true);
		buttonLayout.setPadding(true);
		HorizontalLayout footer = new HorizontalLayout();
		footer.setWidth(100.0f, Unit.PERCENTAGE);
		footer.addClassName(ValoTheme.WINDOW_BOTTOM_TOOLBAR);
		footer.setDefaultComponentAlignment(Alignment.MIDDLE_CENTER);
		footer.addComponent(buttonLayout);
		
		if (callback != null) {
			confirm = new Button(UIUtils.localize("basic.editor.yes"));
			confirm.addClickListener(new ClickListener() {
				private static final long serialVersionUID = 1L;
				@Override
				public void buttonClick(ClickEvent event) {
					callback.onDialogResult(true);
					close();
				}
			});
			// confirm.setImmediate(true);
			confirm.addClassName(UIUtils.BUTTON_DEFAULT_STYLE);
			buttonLayout.addComponent(confirm);
		}

		cancel = new Button(UIUtils.localize("basic.editor.no"));
		cancel.addClickListener(new ClickListener() {
			private static final long serialVersionUID = 1L;
			@Override
			public void buttonClick(ClickEvent event) {
				close();
			}
		});

		// cancel.setImmediate(true);
		cancel.addClassName(UIUtils.BUTTON_DEFAULT_STYLE);

		buttonLayout.addComponent(cancel);


		verticalLayout.addComponent(footer);

	}

	public Button getConfirm() {
		return confirm;
	}

	public Button getCancel() {
		return cancel;
	}

	
}
