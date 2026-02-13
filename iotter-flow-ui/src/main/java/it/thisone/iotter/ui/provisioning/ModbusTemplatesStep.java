package it.thisone.iotter.ui.provisioning;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import it.thisone.iotter.ui.wizards.WizardStep;
import org.vaadin.firitin.form.AbstractForm;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.value.ValueChangeMode;

import it.thisone.iotter.config.Constants;
import it.thisone.iotter.enums.modbus.TemplateState;
import it.thisone.iotter.persistence.model.ModbusProfile;
import it.thisone.iotter.persistence.service.ModbusProfileService;
import it.thisone.iotter.ui.common.ConfirmationDialog;
import it.thisone.iotter.ui.common.ConfirmationDialog.Callback;
import it.thisone.iotter.ui.common.UIUtils;
import it.thisone.iotter.ui.ifc.IProvisioningWizard;
import it.thisone.iotter.ui.main.UiConstants;
import it.thisone.iotter.util.PopupNotification;
import it.thisone.iotter.util.PopupNotification.Type;

public class ModbusTemplatesStep extends Composite<HorizontalLayout> implements WizardStep, Constants {

	private static final String SERIAL = "SERIAL";
	private ModbusProfileService service = UIUtils.getServiceFactory().getModbusProfileService();
	private IProvisioningWizard wizard;

	public ModbusTemplatesStep(IProvisioningWizard wizard) {
		this.wizard = wizard;
	}

	public String getI18nLabel(String key) {
		return getTranslation(UiConstants.PROVISIONING + "." + key);
	}

	@Override
	public String getCaption() {
		return getI18nLabel("templates_step");
	}

	@Override
	public HorizontalLayout getContent() {
		return buildContent();
	}

	@Override
	public boolean onAdvance() {
		return validateStep();
	}

	@Override
	public boolean onBack() {
		return true;
	}

	private Grid<ModbusProfile> leftGrid;
	private Grid<ModbusProfile> rightGrid;
	private ListDataProvider<ModbusProfile> leftProvider;
	private ListDataProvider<ModbusProfile> rightProvider;
	private List<ModbusProfile> lastTemplates;
	private List<ModbusProfile> allTemplates;
	private TextField filterField;

	private HorizontalLayout buildContent() {
		HorizontalLayout content = new HorizontalLayout();
		content.setPadding(true);
		content.setSizeFull();

		VerticalLayout left = new VerticalLayout();
		left.setDefaultHorizontalComponentAlignment(Alignment.START);
		left.setSizeFull();

		VerticalLayout right = new VerticalLayout();
		right.setDefaultHorizontalComponentAlignment(Alignment.START);
		right.setSizeFull();

		VerticalLayout center = new VerticalLayout();
		center.setSizeFull();
		center.setDefaultHorizontalComponentAlignment(Alignment.CENTER);

		final Button moveRightButton = new Button();
		moveRightButton.setIcon(VaadinIcon.ARROW_CIRCLE_RIGHT.create());
		moveRightButton.addClassName("icon-only");
		moveRightButton.setEnabled(false);

		final Button moveLeftButton = new Button();
		moveLeftButton.setIcon(VaadinIcon.ARROW_CIRCLE_LEFT.create());
		moveLeftButton.addClassName("icon-only");
		moveLeftButton.setEnabled(false);
		moveLeftButton.setVisible(false);
		

		VerticalLayout buttons = new VerticalLayout();
		buttons.setDefaultHorizontalComponentAlignment(Alignment.CENTER);
		buttons.addClassName(UIUtils.BUTTONS_STYLE);
		buttons.add(moveRightButton);
		buttons.add(moveLeftButton);
		center.add(buttons);

		content.add(left, center, right);
		content.setVerticalComponentAlignment(Alignment.START, left, center, right);

		content.setFlexGrow(0.5f, left);
		content.setFlexGrow(0.05f, center);
		content.setFlexGrow(0.5f, right);
		String tenant = UIUtils.getUserDetails().getTenant();

		boolean supervisor = UIUtils.getUserDetails().hasRole(Constants.ROLE_SUPERVISOR);
		lastTemplates = filterTemplates(service.findLastTemplates(supervisor), tenant);
		allTemplates = filterTemplates(service.findTemplates(), tenant);

		leftProvider = new ListDataProvider<>(new ArrayList<>(lastTemplates));
		leftGrid = createLeftGrid(leftProvider);

		Checkbox checkbox = new Checkbox(getI18nLabel("show_all_templates"));
		checkbox.addValueChangeListener(event -> {
			boolean value = event.getValue() != null && event.getValue();
			leftProvider.getItems().clear();
			if (value) {
				leftProvider.getItems().addAll(allTemplates);
			} else {
				leftProvider.getItems().addAll(lastTemplates);
			}
			leftProvider.refreshAll();
			applyFilter(filterField.getValue());
		});

		filterField = new TextField();
		filterField.setPlaceholder(getI18nLabel("filter_templates"));
		filterField.setValueChangeMode(ValueChangeMode.LAZY);
		filterField.addValueChangeListener(event -> applyFilter(event.getValue()));		
		
		HorizontalLayout topLeft = new HorizontalLayout();
		topLeft.setWidthFull();
		topLeft.add(new Label(getI18nLabel("available_templates")), checkbox, filterField);
		topLeft.setVerticalComponentAlignment(Alignment.CENTER, checkbox, filterField);


		left.add(topLeft, leftGrid);
		left.setFlexGrow(1f, leftGrid);
		left.setSpacing(true);

		rightProvider = new ListDataProvider<>(wizard.getProfiles());
		rightGrid = createRightGrid(rightProvider);

		HorizontalLayout buttonsLayout = new HorizontalLayout();
		buttonsLayout.addClassName(UIUtils.BUTTONS_STYLE);
		buttonsLayout.setSpacing(true);
		buttonsLayout.setDefaultVerticalComponentAlignment(Alignment.CENTER);
		Button removeButton = createRemoveButton();
		buttonsLayout.add(removeButton);

		Button copyButton = createCopyButton();
//		buttonsLayout.addComponent(copyButton);
		Button editButton = createEditButton();
		buttonsLayout.add(editButton);
		
		HorizontalLayout topRight = new HorizontalLayout();
		topRight.setWidthFull();
		topRight.add(new Label(getI18nLabel("configured_profiles")), buttonsLayout);
		topRight.setVerticalComponentAlignment(Alignment.CENTER, buttonsLayout);
		
		
		right.add(topRight, rightGrid);
		right.setFlexGrow(1f, rightGrid);
		right.setSpacing(true);

		leftGrid.addSelectionListener(event -> {
			boolean enabled = event.getFirstSelectedItem().isPresent()
					&& rightProvider.getItems().size() < wizard.getMaxProfiles();
			moveRightButton.setEnabled(enabled);
		});

		rightGrid.addSelectionListener(event -> {
			ModbusProfile profile = event.getFirstSelectedItem().orElse(null);
			removeButton.setEnabled(profile != null);
			editButton.setEnabled(profile != null);
			copyButton.setEnabled(profile != null && profile.getCreationDate() != null);
			wizard.setSelected(profile);
		});

//		moveLeftButton.addClickListener(new ClickListener() {
//			/**
//			 * 
//			 */
//			private static final long serialVersionUID = 1L;
//
//			@Override
//			public void buttonClick(ClickEvent event) {
//				removeProfile();
//			}
//		});

		moveRightButton.addClickListener(event -> addProfile());

		rightGrid.select(wizard.getSelected());

		return content;
	}
	
	public Button createRemoveButton() {
		Button button = new Button();
		button.setIcon(VaadinIcon.TRASH.create());
		button.addClickListener(event -> removeProfile());
		button.setEnabled(false);
		return button;
	}

	public Button createCopyButton() {
		Button button = new Button();
		button.setIcon(VaadinIcon.COPY.create());
		button.addClickListener(event -> copyProfile());
		button.setEnabled(false);
		return button;
	}
	
	public Button createEditButton() {
		Button button = new Button();
		button.setIcon(VaadinIcon.EDIT.create());
		button.addClickListener(event -> editProfile());
		button.setEnabled(false);
		return button;
	}
	
	

	@SuppressWarnings("serial")
	private void addProfile() {
		ModbusProfile selected = leftGrid.asSingleSelect().getValue();
		if (selected == null) {
			return;
		}
		rightGrid.deselectAll();

		ModbusProfile itemId = service.cloneProfile(selected);
		itemId.setCreationDate(null);
		itemId.getConfiguration().setSlaveID(null);
		itemId.getConfiguration().setSlaveName(null);

		if (itemId.getConfiguration().getProtocol() == null) {
			itemId.getConfiguration().setProtocol(SERIAL);
		}

		List<String> others = new ArrayList<>();
		List<Integer> otherIds = new ArrayList<>();

		for (ModbusProfile profile : wizard.getProfiles()) {
			others.add(profile.getConfiguration().getSlaveName());
			otherIds.add(profile.getConfiguration().getSlaveID());
		}

		final ModbusProfileSimpleForm form = new ModbusProfileSimpleForm();
		form.getSaveButton().setText(getTranslation("basic.editor.confirm"));
		form.getResetButton().setText(getTranslation("basic.editor.cancel"));

		form.setValidations(others, otherIds);
		form.setEntity(itemId);
		form.setSavedHandler(new AbstractForm.SavedHandler<ModbusProfile>() {
			@Override
			public void onSave(ModbusProfile entity) {
				rightProvider.getItems().add(entity);
				rightProvider.refreshAll();
				form.getPopup().close();
			}
		});
		form.setResetHandler(new AbstractForm.ResetHandler<ModbusProfile>() {
			@Override
			public void onReset(ModbusProfile entity) {
				form.getPopup().close();
			}
		});

		form.setModalWindowTitle(getI18nLabel("create_profile"));
		final Dialog dialog = form.openInModalPopup();
		dialog.setResizable(false);

	}
	
	@SuppressWarnings("serial")
	private void copyProfile() {
		ModbusProfile selected = rightGrid.asSingleSelect().getValue();
		if (selected == null) {
			return;
		}


		ModbusProfile itemId = service.cloneProfile(selected);
		itemId.setCreationDate(null);
		itemId.getConfiguration().setSlaveID(null);
		itemId.getConfiguration().setSlaveName(null);

		if (itemId.getConfiguration().getProtocol() == null) {
			itemId.getConfiguration().setProtocol(SERIAL);
		}

		List<String> others = new ArrayList<>();
		List<Integer> otherIds = new ArrayList<>();

		for (ModbusProfile profile : wizard.getProfiles()) {
			others.add(profile.getConfiguration().getSlaveName());
			otherIds.add(profile.getConfiguration().getSlaveID());
		}

		final ModbusProfileSimpleForm form = new ModbusProfileSimpleForm();

		form.getSaveButton().setText(getTranslation("basic.editor.confirm"));
		form.getResetButton().setText(getTranslation("basic.editor.cancel"));

		form.setValidations(others, otherIds);
		form.setEntity(itemId);
		form.setSavedHandler(new AbstractForm.SavedHandler<ModbusProfile>() {
			@Override
			public void onSave(ModbusProfile entity) {
				rightProvider.getItems().add(entity);
				rightProvider.refreshAll();
				form.getPopup().close();
			}
		});
		form.setResetHandler(new AbstractForm.ResetHandler<ModbusProfile>() {
			@Override
			public void onReset(ModbusProfile entity) {
				form.getPopup().close();
			}
		});

		form.setModalWindowTitle(getI18nLabel("copy_profile"));
		final Dialog dialog = form.openInModalPopup();
		dialog.setResizable(false);

	}	
	

	private void removeProfile() {
		final ModbusProfile profile = rightGrid.asSingleSelect().getValue();
		if (profile == null) {
			return;
		}
		if (profile.isNew()) {
			rightProvider.getItems().remove(profile);
			rightProvider.refreshAll();
		} else {
			Callback callback = new Callback() {
				@Override
				public void onDialogResult(boolean result) {
					if (result) {
						rightProvider.getItems().remove(profile);
						rightProvider.refreshAll();
					}
				}
			};
			String caption = getI18nLabel("profile_removal");
			String message = getI18nLabel("confirm_profile_removal");
			Dialog dialog = new ConfirmationDialog(caption, message, callback);
			dialog.open();
		}

	}
	
	
	private void editProfile() {
		final ModbusProfile selected = rightGrid.asSingleSelect().getValue();
		if (selected == null) {
			return;
		}
		wizard.getMapSlaves();
		List<String> others = new ArrayList<>();
		List<Integer> otherIds = new ArrayList<>();

		for (ModbusProfile profile : wizard.getProfiles()) {
			if (!selected.getId().equals(profile.getId())) {
				others.add(profile.getConfiguration().getSlaveName());
				otherIds.add(profile.getConfiguration().getSlaveID());
			}
		}

		final ModbusProfileSimpleForm form = new ModbusProfileSimpleForm();
		form.getSaveButton().setText(getTranslation("basic.editor.confirm"));
		form.getResetButton().setText(getTranslation("basic.editor.cancel"));

		form.setValidations(others, otherIds);
		form.setEntity(selected);
		
		form.setSavedHandler(new AbstractForm.SavedHandler<ModbusProfile>() {
			@Override
			public void onSave(ModbusProfile entity) {
				rightProvider.refreshItem(entity);
				form.getPopup().close();
			}
		});
		form.setResetHandler(new AbstractForm.ResetHandler<ModbusProfile>() {
			@Override
			public void onReset(ModbusProfile entity) {
				form.getPopup().close();
			}
		});

		form.setModalWindowTitle(getI18nLabel("edit_profile"));
		final Dialog dialog = form.openInModalPopup();
		dialog.setResizable(false);
	}

	

	private Grid<ModbusProfile> createLeftGrid(ListDataProvider<ModbusProfile> provider) {
		Grid<ModbusProfile> grid = new Grid<>();
		grid.setDataProvider(provider);
		grid.setSelectionMode(Grid.SelectionMode.SINGLE);
		grid.setSizeFull();
		//grid.addClassName(UIUtils.TABLE_STYLE);
		grid.addColumn(ModbusProfile::getDisplayName).setKey("displayName").setHeader(getI18nLabel("displayName"));
		grid.addColumn(ModbusProfile::getRevision).setKey("revision").setHeader(getI18nLabel("revision"));
		grid.addColumn(ModbusProfile::getTemplate).setKey("template").setHeader(getI18nLabel("template"));
		return grid;
	}

	private Grid<ModbusProfile> createRightGrid(ListDataProvider<ModbusProfile> provider) {
		Grid<ModbusProfile> grid = new Grid<>();
		grid.setDataProvider(provider);
		grid.setSelectionMode(Grid.SelectionMode.SINGLE);
		grid.setSizeFull();
		//grid.addClassName(UIUtils.TABLE_STYLE);
		grid.addColumn(profile -> profile.getConfiguration().getSlaveID())
			.setKey("configuration.slaveID")
			.setHeader(getI18nLabel("configuration.slaveID"));
		grid.addColumn(profile -> profile.getConfiguration().getSlaveName())
			.setKey("configuration.slaveName")
			.setHeader(getI18nLabel("configuration.slaveName"));
		grid.addColumn(ModbusProfile::getDisplayName).setKey("displayName").setHeader(getI18nLabel("displayName"));
		grid.addColumn(ModbusProfile::getRevision).setKey("revision").setHeader(getI18nLabel("revision"));
		return grid;
	}

	private void applyFilter(String text) {
		if (leftProvider == null) {
			return;
		}
		String needle = text == null ? "" : text.trim().toLowerCase();
		if (needle.isEmpty()) {
			leftProvider.clearFilters();
			return;
		}
		leftProvider.setFilter(profile -> {
			String value = profile.getDisplayName() == null ? "" : profile.getDisplayName();
			return value.toLowerCase().contains(needle);
		});
	}
	
	protected boolean validateStep() {
		if (wizard.getProfiles().size() > wizard.getMaxProfiles()) {
			PopupNotification.show(getI18nLabel("too_many_profiles", new Object[] { wizard.getMaxProfiles() }),
					Type.ERROR);
			return false;
		}
		return true;
	}

	public String getI18nLabel(String key, Object[] args) {
		return getTranslation(UiConstants.PROVISIONING + "." + key, args, null);
	}
	
	
	private List<ModbusProfile> filterTemplates(List<ModbusProfile> templates, String owner) {
		for (Iterator<ModbusProfile> iterator = templates.iterator(); iterator.hasNext();) {
			ModbusProfile template = iterator.next();
			TemplateState state = template.getState() != null ? template.getState() : TemplateState.PUBLIC;
			switch (state) {
			case DISABLED:
				iterator.remove();
				break;
			case DRAFT:
				if (!template.getOwner().equals(owner)) {
					iterator.remove();
				}
				break;

			default:
				break;
			}
		}
		templates.forEach(f -> f.setData(null));
		return templates;
	}	
	
	
	public void clear() {
		if (leftProvider != null) {
			leftProvider.getItems().clear();
			leftProvider.refreshAll();
		}
		if (rightProvider != null) {
			rightProvider.getItems().clear();
			rightProvider.refreshAll();
		}
	}

}
