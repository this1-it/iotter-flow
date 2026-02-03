package it.thisone.iotter.ui.common;

import java.util.function.Consumer;

import org.apache.commons.lang3.builder.EqualsBuilder;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.icon.VaadinIcon;

import it.thisone.iotter.persistence.model.BaseEntity;
import it.thisone.iotter.ui.common.ConfirmationDialog.Callback;
import it.thisone.iotter.ui.eventbus.PendingChangesEvent;


/**
 * Custom component with a tool bar to be used as tab content
 * @author tisone
 *
 */
public abstract class BaseEditor<T extends BaseEntity> extends BaseComponent {

	public static final String AN_EDIT_CONFLICT_OCCURRED = "An edit conflict occurred and you are going to have to redo changes";
	private static final String AN_UNEXPECTED_ERROR_OCCURRED = "An unexpected error occurred and you are going to have to redo changes";

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private T item;

	private Button saveButton;
	private Button cancelButton;

	private boolean pendingChanges;
	private Consumer<Object> eventPoster;

	/**
	 * Set the event poster for publishing UI events (e.g., PendingChangesEvent).
	 * This should be called by subclasses that inject UIEventBus.
	 *
	 * @param eventPoster a consumer that posts events to the UI event bus
	 */
	public void setEventPoster(Consumer<Object> eventPoster) {
		this.eventPoster = eventPoster;
	}
	

	public BaseEditor(String name) {
		super(name);
	}

	public BaseEditor(String i18nkey, String id){
		super(i18nkey,id);
	}
	
	protected T getItem() {
		return item;
	}
	
	protected void setItem(T data) {
		item = data;
	}
	
	protected abstract void onSave();

	protected void onCancel() {

	}
	
	protected HorizontalLayout createToolbar() {
		HorizontalLayout toolbar = new HorizontalLayout();
		toolbar.setWidth("100%");
		toolbar.setSpacing(true);
		toolbar.setPadding(true);
		toolbar.addClassName(UIUtils.TOOLBAR_STYLE);
		HorizontalLayout buttonbar = createButtonbar();
		toolbar.add(buttonbar);
		//toolbar.setComponentAlignment(buttonbar, Alignment.MIDDLE_RIGHT);
		return toolbar;
	}

	protected HorizontalLayout createButtonbar() {
		HorizontalLayout buttonbar = new HorizontalLayout();
		//buttonbar.setStyleName(UIUtils.BUTTONS_STYLE);
		buttonbar.setSpacing(true);
		saveButton = createSaveButton();
		buttonbar.add(saveButton);
		//buttonbar.setExpandRatio(saveButton, 1);
		//buttonbar.setComponentAlignment(saveButton, Alignment.MIDDLE_RIGHT);
		cancelButton = createCancelButton();
		buttonbar.add(cancelButton);
//		buttonbar.setExpandRatio(cancelButton, 1);
//		buttonbar.setComponentAlignment(cancelButton, Alignment.MIDDLE_RIGHT);
		return buttonbar;
	}	
	
	
	

	protected Button createSaveButton() {
		//Button button = new Button(getI18nLabel("save_button"));
		Button button = new Button();
		button.setIcon(VaadinIcon.FILE_TEXT.create());
		//button.setStyleName(UIUtils.ICON_SAVE_STYLE);
		button.getElement()
	      .setProperty("title", getI18nLabel("save_button"));
		button.addClickListener(event -> {
			onSave();
			if (eventPoster != null) {
				eventPoster.accept(new PendingChangesEvent());
			}
		});
		return button;
	}


	protected Button createCancelButton() {
		//Button button = new Button(getI18nLabel("cancel_button"));
		Button button = new Button();
		button.setIcon(VaadinIcon.CLOSE.create());
		button.getElement()
	      .setProperty("title", getI18nLabel("cancel_button"));
		
		button.addClickListener(event -> {
				final EditorSavedEvent evt = new EditorSavedEvent(BaseEditor.this, null);
				if (pendingChanges) {
					String caption = getTranslation("basic.editor.forget_changes");
					String message = getTranslation("basic.editor.pending_changes");
					Callback callback = new Callback() {
						@Override
						public void onDialogResult(boolean result) {
							if (result) {
								onCancel();
								fireEvent(evt);
							}
						}
					};
					Dialog dialog = new ConfirmationDialog(caption, message, callback);
					dialog.open();
				}
				else {
					onCancel();
					fireEvent(evt);
				}
			
		});
		return button;
	}

	
	public void addListener(EditorSavedListener listener) {
//		try {
//			Method method = EditorSavedListener.class.getDeclaredMethod(EditorSavedListener.EDITOR_SAVED, new Class[] { EditorSavedEvent.class });
//			addListener(EditorSavedEvent.class, listener, method);
//		} catch (final java.lang.NoSuchMethodException e) {
//			throw new java.lang.RuntimeException("Internal error, editor saved method not found");
//		}
	}

	public void removeListener(EditorSavedListener listener) {
		//removeListener(EditorSavedEvent.class, listener);
	}
	
	

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof BaseEditor == false) {
			return false;
		}
		if (this == obj) {
			return true;
		}
		final BaseEditor otherObject = (BaseEditor) obj;
		return new EqualsBuilder().append(this.getId(), otherObject.getId()).isEquals();
	}

	public Button getSaveButton() {
		return saveButton;
	}

	public Button getCancelButton() {
		return cancelButton;
	}
	
	public void pendingChanges(){
		if (!isPendingChanges()) {
			setPendingChanges(true);
//			UI.getCurrent().access(new Runnable() {
//			    @Override
//			    public void run() {
//					getSaveButton().setStyleName("pending-changes");
//					getSaveButton().markAsDirty();
//					UIUtils.push();
//			    }
//			});	
		}
	}
	
	public boolean isPendingChanges() {
		return pendingChanges;
	}

	public void setPendingChanges(boolean pendingChanges) {
		this.pendingChanges = pendingChanges;
	}

}
