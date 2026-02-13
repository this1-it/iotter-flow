package it.thisone.iotter.ui.provisioning;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.button.ButtonVariant;

import it.thisone.iotter.config.Constants.Provisioning;
import it.thisone.iotter.integration.SubscriptionService;
import it.thisone.iotter.persistence.model.Device;
import it.thisone.iotter.persistence.model.GraphicWidget;
import it.thisone.iotter.persistence.model.GroupWidget;
import it.thisone.iotter.persistence.model.ModbusProfile;
import it.thisone.iotter.persistence.service.DeviceService;
import it.thisone.iotter.persistence.service.ModbusProfileService;
import it.thisone.iotter.provisioning.ProvisionedEvent;
import it.thisone.iotter.provisioning.ProvisioningEvent;
import it.thisone.iotter.ui.common.EditorSavedEvent;
import it.thisone.iotter.ui.common.EditorSavedListener;
import it.thisone.iotter.ui.common.UIUtils;
import it.thisone.iotter.ui.graphicwidgets.ControlPanelBaseStep;
import it.thisone.iotter.ui.ifc.IProvisioningWizard;
import it.thisone.iotter.ui.main.UiConstants;
import it.thisone.iotter.ui.wizards.Wizard;
import it.thisone.iotter.ui.wizards.event.WizardCancelledEvent;
import it.thisone.iotter.ui.wizards.event.WizardCompletedEvent;
import it.thisone.iotter.ui.wizards.event.WizardProgressListener;
import it.thisone.iotter.ui.wizards.event.WizardStepActivationEvent;
import it.thisone.iotter.ui.wizards.event.WizardStepSetChangedEvent;
import it.thisone.iotter.util.PopupNotification;
import it.thisone.iotter.util.PopupNotification.Type;

public class ProvisioningWizard extends Composite<VerticalLayout> implements
		WizardProgressListener, IProvisioningWizard {
	
	private static final int MAX_TOTAL_REGISTRIES = 800;
	
	public static Logger logger = LoggerFactory
			.getLogger(ProvisioningWizard.class);

	private Device master;
	//private String checksum;

	private List<ModbusProfile> profiles = new ArrayList<>();
	private List<String> originalProfiles = new ArrayList<>();
	private Map<String, String> mapSlaves = new HashMap<>();
	private Map<String, GroupWidget> mapWidgets = new HashMap<>();
	private Map<String, GraphicWidget> mapGraphics = new HashMap<>();
	private ModbusProfile selected;

	private ModbusProfileService profileService = UIUtils.getServiceFactory()
			.getModbusProfileService();
	private DeviceService deviceService = UIUtils.getServiceFactory()
			.getDeviceService();
	private SubscriptionService subscriptionService = UIUtils
			.getServiceFactory().getSubscriptionService();

	/**
	 * 
	 */
	private static final long serialVersionUID = 4337523807788095820L;

	private ModbusTemplatesStep templatesStep;
	private ModbusProfilesStep profilesStep;
	private ControlPanelBaseStep aernetProStep;
	private final List<EditorSavedListener> editorSavedListeners = new ArrayList<>();


	@SuppressWarnings("serial")
	public ProvisioningWizard(String masterSerial) {
		this.master = deviceService.findBySerial(masterSerial);
		init();
		
		Wizard wizard = new Wizard();
		templatesStep = new ModbusTemplatesStep(this);
		profilesStep = new ModbusProfilesStep(this);
		aernetProStep = new ControlPanelBaseStep(this);
		
		wizard.addStep(templatesStep, "templates");
		wizard.addStep(profilesStep, "profiles");
		wizard.addStep(aernetProStep, "aernet_pro");
		wizard.addListener(this);

		// wizard.setUriFragmentEnabled(true);

		wizard.getCancelButton().addClickListener(event -> fireEditorSaved(null));

		wizard.getFinishButton().addClickListener(event -> {
			if (aernetProStep.validateStep() ) {
				commit();
				fireEditorSaved(getMaster());
			}
		});

		wizard.getCancelButton().addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		wizard.getFinishButton().addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		wizard.getNextButton().addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		wizard.getBackButton().addThemeVariants(ButtonVariant.LUMO_PRIMARY);

		wizard.getCancelButton().setText(getI18nLabel("cancel"));
		wizard.getFinishButton().setText(getI18nLabel("finish"));
		wizard.getNextButton().setText(getI18nLabel("next"));
		wizard.getBackButton().setText(getI18nLabel("back"));
		wizard.getFinishButton().setDisableOnClick(true);
		
		VerticalLayout content = getContent();
		content.setSizeFull();
		content.setPadding(true);

		content.add(wizard);
		// setImmediate(true);

	}

	private void init() {
		profiles = new ArrayList<>();
		originalProfiles = new ArrayList<>();
		mapSlaves = new HashMap<>();
		mapWidgets = new HashMap<>();
		List<Device> slaves = deviceService.findSlaves(master);
		for (Device slave : slaves) {
			for (ModbusProfile source : slave.getProfiles()) {
				mapSlaves.put(source.getId(), slave.getSerial());
				ModbusProfile target = profileService.copyBuffered(source);
				profiles.add(target);
				originalProfiles.add(source.getId());
			}
		}
	}

	public String getWindowStyle() {
		return UiConstants.PROVISIONING;
	}

	public float[] getWindowDimension() {
		return UIUtils.XL_DIMENSION;
	}

	public void addListener(EditorSavedListener listener) {
		if (listener != null) {
			editorSavedListeners.add(listener);
		}
	}

	private void fireEditorSaved(Device savedItem) {
		EditorSavedEvent<Device> event = new EditorSavedEvent<>(this, savedItem);
		for (EditorSavedListener listener : editorSavedListeners) {
			listener.editorSaved(event);
		}
	}

	public Device getMaster() {
		return master;
	}

	@Override
	public void activeStepChanged(WizardStepActivationEvent event) {

	}

	@Override
	public void stepSetChanged(WizardStepSetChangedEvent event) {

	}

	@Override
	public void wizardCompleted(WizardCompletedEvent event) {
	}

	@Override
	public void wizardCancelled(WizardCancelledEvent event) {
	}

	public String getI18nLabel(String key) {
		return getTranslation(getI18nKey() + "." + key);
	}

	public String getI18nKey() {
		return UiConstants.PROVISIONING;
	}

	private void commit() {
		ProvisioningEvent provisioning = new ProvisioningEvent(master, getProfiles(),
				getOriginalProfiles(), getMapSlaves(), getMapWidgets());
		
		try {
			subscriptionService.provisioning(provisioning);
		} catch (Throwable e) {
			logger.error("on provisioning",e);
			PopupNotification.show(getI18nLabel("changes_cannot_be_committed"), Type.ERROR);
			return;
		}
		
		String checksum = deviceService.provisioningChecksum(master);
		ProvisionedEvent provisioned = new ProvisionedEvent(master, checksum);
		try {
			subscriptionService.provisioned(provisioned);
		} catch (Throwable e) {
			for (ModbusProfile profile : getProfiles()) {
				profileService.changeChecksum(profile);
			}
			logger.error("on provisioned", e);
			PopupNotification.show(getI18nLabel("changes_cannot_be_propagated"), Type.ERROR);
		}
		
		this.clear();
	}

	@Override
	public ModbusProfile getSelected() {
		return selected;
	}

	@Override
	public void setSelected(ModbusProfile selected) {
		this.selected = selected;
	}

	@Override
	public List<ModbusProfile> getProfiles() {
		return profiles;
	}

	@Override
	public List<String> getOriginalProfiles() {
		return originalProfiles;
	}

	@Override
	public Map<String, String> getMapSlaves() {
		return mapSlaves;
	}

	@Override
	public Map<String, GroupWidget> getMapWidgets() {
		return mapWidgets;
	}

	@Override
	public Map<String, GraphicWidget> getMapGraphics() {
		return mapGraphics;
	}

	@Override
	public int getMaxTotalRegisters() {
		//return MAX_TOTAL_REGISTRIES;
		return MAX_TOTAL_REGISTRIES;
	}

	@Override
	public int getMaxAllowedBandwidthRatio() {
		return Provisioning.MAX_ALLOWED_BANDWIDTH_RATIO;
	}

	@Override
	public int getMaxProfiles() {
		return Provisioning.MAX_PROFILES;
	}


	public static String profileName(ModbusProfile profile) {
		String name = String.format("%s - %s rev. %s", profile.getConfiguration().getSlaveName(),
				profile.getDisplayName(), profile.getRevision());
		return name;
	}


	@Override
	public void clear() {
		//
		templatesStep.clear();
		profilesStep.clear();
		//
		profiles.clear();
		originalProfiles.clear();
		mapSlaves.clear();
		mapWidgets.clear();
		mapGraphics.clear();	

	}


}
