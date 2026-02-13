package it.thisone.iotter.ui.provisioning;

import java.util.List;
import java.util.function.Consumer;

import org.vaadin.firitin.form.AbstractForm;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.ValidationResult;
import com.vaadin.flow.data.binder.ValueContext;

import it.thisone.iotter.persistence.model.ModbusProfile;
import it.thisone.iotter.ui.common.UIUtils;

// Feature #2029
public class ModbusProfileChoiceDialog extends Dialog {

	/**
	 * Callback class for a {@link ModbusProfileChoiceDialog}.
	 */
	public static interface Callback {
		/**
		 * Called upon pressing a button.
		 * 
		 * @param buttonName the name of the button that was clicked, never
		 *                   <code>null</code>.
		 */
		void onDialogResult(ModbusProfile target);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final ModbusProfileChoiceForm form;

	public ModbusProfileChoiceDialog(String caption, String sourceCaption, String sourceValue,
			String targetCaption, List<ModbusProfile> targets, final Callback callback) {
		super();
		//setHeaderTitle(caption);
		setDraggable(false);
		setCloseOnEsc(true);
		setCloseOnOutsideClick(false);
		setResizable(false);
		form = new ModbusProfileChoiceForm(sourceCaption, targetCaption);
		form.setTargets(targets);
		form.setSourceValue(sourceValue);
		form.setSubmitHandler(data -> {
			callback.onDialogResult(data.getTarget());
			close();
		});
		form.setCancelHandler(event -> close());
		add(form);
	}

	private static class ModbusProfileChoiceData {
		private String source;
		private ModbusProfile target;

		public String getSource() {
			return source;
		}

		public void setSource(String source) {
			this.source = source;
		}

		public ModbusProfile getTarget() {
			return target;
		}

		public void setTarget(ModbusProfile target) {
			this.target = target;
		}
	}

	private static class ModbusProfileChoiceForm extends AbstractForm<ModbusProfileChoiceData> {
		private static final long serialVersionUID = 1L;
		private final TextField source;
		private final ComboBox<ModbusProfile> targets;
		private final Button cancelButton;
		private Consumer<ModbusProfileChoiceData> submitHandler;

		ModbusProfileChoiceForm(String sourceCaption, String targetCaption) {
			super(ModbusProfileChoiceData.class);
			source = new TextField(sourceCaption);
			source.setReadOnly(true);
			source.setSizeFull();

			targets = new ComboBox<>(targetCaption);
			targets.setSizeFull();
			targets.setClearButtonVisible(false);
			targets.setAllowCustomValue(false);
			//targets.setItemLabelGenerator(ProvisioningWizard::profileName);

			cancelButton = new Button(getTranslation("basic.editor.no"));
			//cancelButton.addClassName(UIUtils.BUTTON_DEFAULT_STYLE);

			setEntity(new ModbusProfileChoiceData());
			setupBinder();
			setupHandlers();
		}

		private void setupBinder() {
			getBinder().forField(source)
				.bind(ModbusProfileChoiceData::getSource, ModbusProfileChoiceData::setSource);
			getBinder().forField(targets)
				.asRequired(getTranslation("validators.fieldgroup_errors"))
				.withValidator(this::validateTarget)
				.bind(ModbusProfileChoiceData::getTarget, ModbusProfileChoiceData::setTarget);
			getBinder().addStatusChangeListener(event -> getSaveButton().setEnabled(event.getBinder().isValid()));
		}

		private ValidationResult validateTarget(ModbusProfile value, ValueContext context) {
			return value == null ? ValidationResult.error(getTranslation("validators.fieldgroup_errors"))
					: ValidationResult.ok();
		}

		private void setupHandlers() {
			setSavedHandler(entity -> {
				if (submitHandler != null) {
					submitHandler.accept(entity);
				}
			});
		}

		void setTargets(List<ModbusProfile> profiles) {
			targets.setItems(profiles);
		}

		void setSourceValue(String value) {
			source.setReadOnly(false);
			source.setValue(value == null ? "" : value);
			source.setReadOnly(true);
			getEntity().setSource(value);
		}

		void setSubmitHandler(Consumer<ModbusProfileChoiceData> submitHandler) {
			this.submitHandler = submitHandler;
		}

		void setCancelHandler(ComponentEventListener<ClickEvent<Button>> listener) {
			cancelButton.addClickListener(listener);
		}

		@Override
		protected Component createContent() {
			getSaveButton().setText(getTranslation("basic.editor.yes"));
			//getSaveButton().addClassName(UIUtils.BUTTON_DEFAULT_STYLE);
			getSaveButton().setEnabled(false);
			getResetButton().setVisible(false);
			getDeleteButton().setVisible(false);

			FormLayout formLayout = new FormLayout();
			formLayout.setWidthFull();
			formLayout.getStyle().set("padding", "var(--lumo-space-m)");
			formLayout.add(source, targets);

			HorizontalLayout content = new HorizontalLayout();
			content.setSizeFull();
			content.setAlignItems(FlexComponent.Alignment.CENTER);
			content.add(formLayout);
			content.setPadding(true);

			HorizontalLayout buttonLayout = new HorizontalLayout();
			buttonLayout.setSpacing(true);
			buttonLayout.setPadding(true);
			buttonLayout.add(getSaveButton(), cancelButton);

			HorizontalLayout footer = new HorizontalLayout();
			footer.setWidthFull();
			footer.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
			footer.add(buttonLayout);

			VerticalLayout verticalLayout = new VerticalLayout();
			verticalLayout.setSizeFull();
			verticalLayout.setSpacing(true);
			verticalLayout.setAlignItems(FlexComponent.Alignment.CENTER);
			verticalLayout.add(content, footer);
			verticalLayout.setFlexGrow(1f, content);
			return verticalLayout;
		}
	}

}
