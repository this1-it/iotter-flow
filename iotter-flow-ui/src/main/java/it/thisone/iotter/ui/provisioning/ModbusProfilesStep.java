package it.thisone.iotter.ui.provisioning;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import it.thisone.iotter.ui.wizards.WizardStep;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import it.thisone.iotter.config.Constants;
import it.thisone.iotter.persistence.model.ModbusProfile;
import it.thisone.iotter.persistence.service.ModbusProfileService;
import it.thisone.iotter.ui.common.UIUtils;
import it.thisone.iotter.ui.ifc.IProvisioningWizard;
import it.thisone.iotter.ui.main.UiConstants;
import it.thisone.iotter.ui.provisioning.ModbusProfileChoiceDialog.Callback;
import it.thisone.iotter.util.PopupNotification;
import it.thisone.iotter.util.PopupNotification.Type;

/**
 * "^[a-zA-Z0-9_]+$"
 * 
 * @author bedinsky
 *
 */

public class ModbusProfilesStep extends Composite<VerticalLayout> implements WizardStep, Constants {
	public static Logger logger = LoggerFactory.getLogger(ModbusProfilesStep.class);

	private ComboBox<ModbusProfile> profiles;
	private ModbusProfileSimpleForm form;
	private IProvisioningWizard wizard;
	private ModbusRegistersField registers;
	private ModbusProfileService modbusProfileService = UIUtils.getServiceFactory().getModbusProfileService();

	public ModbusProfilesStep(IProvisioningWizard wizard) {
		this.form = new ModbusProfileSimpleForm();
		this.wizard = wizard;
		this.registers = new ModbusRegistersField();
	}

	@SuppressWarnings({ "serial" })
	private ComboBox<ModbusProfile> createCombo(final List<ModbusProfile> profiles) {
		ComboBox<ModbusProfile> combo = new ComboBox<>();
		combo.setItems(profiles);
		//combo.setItemLabelGenerator(ProvisioningWizard::profileName);
		combo.setClearButtonVisible(false);
		combo.setAllowCustomValue(false);
		combo.addValueChangeListener(event -> {
			ModbusProfile entity = event.getValue();
			if (entity == null) {
				return;
			}
			if (!commit()) {
				return;
			}
			wizard.setSelected(entity);
			List<String> others = new ArrayList<>();
			List<Integer> otherIds = new ArrayList<>();
			for (ModbusProfile profile : profiles) {
				if (!profile.equals(entity)) {
					others.add(profile.getConfiguration().getSlaveName());
					otherIds.add(profile.getConfiguration().getSlaveID());
				}
			}
			form.setValidations(others, otherIds);
			form.setEntity(entity);
			registers.setRegisters(entity.getRegisters());
			form.getSlaveName().setReadOnly(true);
		});
		return combo;
	}




	public String getI18nLabel(String key) {
		return getTranslation(UiConstants.PROVISIONING + "." + key);
	}

	public String getI18nLabel(String key, Object[] args) {
		return getTranslation(UiConstants.PROVISIONING + "." + key, args, null);
	}

	@Override
	public String getCaption() {
		return getI18nLabel("profiles_step");
	}

	private void copyProfile() {
		ModbusProfile selected = wizard.getSelected();
		List<ModbusProfile> profiles = new ArrayList<ModbusProfile>();
		for (ModbusProfile profile : wizard.getProfiles()) {
			if (!profile.getId().equals(selected.getId())
					&& profile.getDisplayName().equals(selected.getDisplayName())) {
				profiles.add(profile);
			}
		}

		Callback callback = new Callback() {
			@Override
			public void onDialogResult(ModbusProfile target) {
				modbusProfileService.copyCompatibleRegisters(selected, target);
				
//				if (target != null) {
//					for (ModbusRegister tr : target.getRegisters()) {
//						Optional<ModbusRegister> match = selected.getRegisters().stream().filter(ModbusRegister.IS_EQUAL(tr)).findFirst();
//						if (match.isPresent()) {
//							tr.setActive(match.get().getActive());
//						}
//					}
//				}
			}
		};

		String caption = getI18nLabel("copy_active_flags");
		String sourceCaption = getI18nLabel("profile_source");
		String targetCaption = getI18nLabel("profile_target");
		String sourceValue = wizard.getSelected().getId();
		Dialog dialog = new ModbusProfileChoiceDialog(caption, sourceCaption, sourceValue, targetCaption, profiles, callback);
		dialog.open();

	}

	public Button createCopyButton() {
		Button button = new Button();
		button.setIcon(VaadinIcon.COPY.create());
		button.addClickListener(event -> copyProfile());
		return button;
	}

	@Override
	public VerticalLayout getContent() {
		VerticalLayout mainLayout = new VerticalLayout();
//        mainLayout.setHeightUndefined();
//        content.setHeightFull();
		if (!wizard.getProfiles().isEmpty()) {
			profiles = createCombo(wizard.getProfiles());
			if (wizard.getSelected() == null) {
				wizard.setSelected(wizard.getProfiles().get(0));
			}
			profiles.setValue(wizard.getSelected());
			profiles.setWidthFull();

			Button copyButton = createCopyButton();
			HorizontalLayout buttonsLayout = new HorizontalLayout();
			buttonsLayout.addClassName(UIUtils.BUTTONS_STYLE);
			buttonsLayout.setSizeFull();
			buttonsLayout.setPadding(false);
			buttonsLayout.setSpacing(true);
			buttonsLayout.add(profiles, copyButton);
			buttonsLayout.setFlexGrow(1f, profiles);

			VerticalLayout header = new VerticalLayout();
			header.setPadding(true);
			header.setSpacing(true);
			header.setDefaultHorizontalComponentAlignment(Alignment.CENTER);
			header.add(buttonsLayout);
			header.add(form.getFlowLayout());

			VerticalLayout topLayout = new VerticalLayout();
			topLayout.setSpacing(true);
			topLayout.setPadding(false);
			topLayout.setWidthFull();
			topLayout.add(header);

			mainLayout.setSizeFull();
			mainLayout.setPadding(false);

			mainLayout.add(topLayout);
			//registers.setSizeFull();
			mainLayout.add(registers);
			mainLayout.setFlexGrow(1f, registers);

		} else {
			mainLayout.add(new Label(getI18nLabel("no_profiles")));
		}
		return mainLayout;
	}

	@Override
	public boolean onAdvance() {
		return validateStep();
	}

	@Override
	public boolean onBack() {
		return true;
	}

	private boolean commit() {
		if (!form.commit()) {
			return false;
		}
		this.wizard.getSelected().setRegisters(registers.getRegisters());
		return true;
	}

	protected boolean validateStep() {
		if (!commit())
			return false;

		int total = 0;
		double sampleRate = 0;
		for (ModbusProfile profile : wizard.getProfiles()) {
			total = total + profile.countActiveRegisters();
			sampleRate = sampleRate + profile.getConfiguration().getSampleRate();
		}

		if (total > wizard.getMaxTotalRegisters()) {
			PopupNotification.show(getI18nLabel("too_many_registries", new Object[] { wizard.getMaxTotalRegisters() }),
					Type.ERROR);
			return false;
		}

		double bandwidthRatio = total / (sampleRate / wizard.getProfiles().size());
		// logger.error("total:{}, rate: {}, mean:{}, bandwidth:{}", total ,sampleRate,
		// sampleRate / wizard.getProfiles().size(), bandwidthRatio );

		if (bandwidthRatio > wizard.getMaxAllowedBandwidthRatio()) {
			PopupNotification.show(getI18nLabel("sample_rate_must_be_modified",
					new Object[] { total / wizard.getMaxAllowedBandwidthRatio() }), Type.ERROR);
			return false;
		}

		return true;
	}

	public void clear() {
		registers.clear();
	}
	
}
