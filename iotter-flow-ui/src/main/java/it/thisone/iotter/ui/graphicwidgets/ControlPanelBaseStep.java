package it.thisone.iotter.ui.graphicwidgets;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import it.thisone.iotter.ui.wizards.WizardStep;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.binder.ValidationException;

import it.thisone.iotter.enums.ChartScaleType;
import it.thisone.iotter.enums.GraphicWidgetType;
import it.thisone.iotter.persistence.model.GraphicFeed;
import it.thisone.iotter.persistence.model.GraphicWidget;
import it.thisone.iotter.persistence.model.GroupWidget;
import it.thisone.iotter.persistence.model.ModbusProfile;
import it.thisone.iotter.persistence.model.ModbusRegister;
import it.thisone.iotter.persistence.service.GroupWidgetService;
import it.thisone.iotter.persistence.service.ModbusProfileService;
import it.thisone.iotter.ui.common.EditorConstraintException;
import it.thisone.iotter.ui.common.UIUtils;
import it.thisone.iotter.ui.common.charts.ChartUtils;
import it.thisone.iotter.ui.ifc.IProvisioningWizard;
import it.thisone.iotter.ui.main.UiConstants;
import it.thisone.iotter.ui.providers.ControlPanelBaseProvider;
import it.thisone.iotter.ui.provisioning.ModbusProfileChoiceDialog;
import it.thisone.iotter.ui.provisioning.ModbusProfileChoiceDialog.Callback;
import it.thisone.iotter.ui.provisioning.ProvisioningWizard;
import it.thisone.iotter.util.PopupNotification;
import it.thisone.iotter.util.PopupNotification.Type;

/**
 * 
 * @author bedinsky
 *
 */

public class ControlPanelBaseStep extends Composite<VerticalLayout> implements WizardStep, ControlPanelBaseConstants {

	public static Logger logger = LoggerFactory.getLogger(ControlPanelBaseStep.class);

	private ControlPanelBaseForm editor;
	private IProvisioningWizard wizard;

	private boolean committed;
	private Div content;
	private ComboBox<ModbusProfile> combo;
	private GroupWidgetService groupWidgetService = UIUtils.getServiceFactory().getGroupWidgetService();
	private ModbusProfileService modbusProfileService = UIUtils.getServiceFactory().getModbusProfileService();

	public ControlPanelBaseStep(IProvisioningWizard wizard) {
		this.wizard = wizard;
	}

	public Button createCopyButton() {
		Button button = new Button();
		button.setIcon(VaadinIcon.COPY.create());
		button.addClickListener(event -> copyProfile());
		return button;
	}
	
	private void copyProfile() {
		committed = false;
		commit();
		editor.reset();

		
		ModbusProfile source = wizard.getSelected();
		List<ModbusProfile> profiles = new ArrayList<ModbusProfile>();
		for (ModbusProfile profile : wizard.getProfiles()) {
			if (!profile.getId().equals(source.getId())
					&& profile.getDisplayName().equals(source.getDisplayName())) {
				profiles.add(profile);
			}
		}

		Callback callback = new Callback() {
			@Override
			public void onDialogResult(ModbusProfile target) {
				if (target != null) {
					GroupWidget sourceGroupWidget = wizard.getMapWidgets().get(source.getId());
					GroupWidget targetGroupWidget = wizard.getMapWidgets().get(target.getId());					
					GraphicWidget sourceGraphicWidget = sourceGroupWidget.getWidgets().get(0);
					GraphicWidget targetGraphicWidget = targetGroupWidget.getWidgets().get(0);					
					List<GraphicFeed> feeds = modbusProfileService.cloneFeeds(sourceGraphicWidget.getFeeds(), target);
					targetGraphicWidget.setFeeds(feeds);
				}
			}
		};

		String caption = getI18nLabel("copy_widget_settings");
		String sourceCaption = getI18nLabel("profile_source");
		String targetCaption = getI18nLabel("profile_target");
		String sourceValue = ProvisioningWizard.profileName(source);
		Dialog dialog = new ModbusProfileChoiceDialog(caption, sourceCaption, sourceValue, targetCaption, profiles, callback);
		dialog.open();

	}
	
	@SuppressWarnings({ "serial"})
	private ComboBox<ModbusProfile> createCombo(final List<ModbusProfile> profiles) {
		return createCombo(null, profiles);
	}

	private ComboBox<ModbusProfile> createCombo(String caption, final List<ModbusProfile> profiles) {
		ComboBox<ModbusProfile> combo = new ComboBox<>();
		combo.setItems(profiles);
		//combo.setItemLabelGenerator(ProvisioningWizard::profileName);
		if (caption != null) {
			combo.setLabel(caption);
		}
		combo.setClearButtonVisible(false);
		combo.setAllowCustomValue(false);
		combo.setSizeFull();
		combo.addValueChangeListener(event -> {
			ModbusProfile entity = event.getValue();
			if (entity == null) {
				return;
			}
			if (!commit()) {
				return;
			}

			wizard.setSelected(entity);
			GroupWidget groupWidget = wizard.getMapWidgets().get(entity.getId());
			GraphicWidget widget = groupWidget.getWidgets().get(0);

			content.removeAll();
			editor = new ControlPanelBaseForm(widget);
			content.add(editor.createProfileRegistersLayout());

			List<ModbusRegister> registers = entity.getRegisters().stream()
					.filter(s -> !s.getCrucial())
					.collect(Collectors.toList());
			editor.setRegisters(registers);
			editor.setRegisters(entity.getRegisters());

			editor.reset();
			committed = false;
		});
		return combo;
	}

	public String getI18nLabel(String key) {
		return getTranslation(UiConstants.PROVISIONING + "." + key);
	}

	@Override
	public String getCaption() {
		return getI18nLabel("aernet_pro");
	}

	
	public VerticalLayout getContent() {
		VerticalLayout mainLayout = new VerticalLayout();
		mainLayout.setSizeFull();
		mainLayout.setPadding(true);
		if (!wizard.getProfiles().isEmpty()) {
			setupGroupWidgets();
			combo = createCombo(wizard.getProfiles());
			
			Button copyButton = createCopyButton();
			HorizontalLayout buttonsLayout = new HorizontalLayout();
			buttonsLayout.addClassName(UIUtils.BUTTONS_STYLE);
			buttonsLayout.setSizeFull();
			buttonsLayout.setPadding(false);
			buttonsLayout.setSpacing(true);
			buttonsLayout.add(combo, copyButton);
			buttonsLayout.setFlexGrow(1f, combo);
			
			
			VerticalLayout header = new VerticalLayout();
			header.setPadding(true);
			header.setSpacing(true);
			header.setDefaultHorizontalComponentAlignment(Alignment.CENTER);
			header.add(buttonsLayout);
			mainLayout.add(header);
			
			content = new Div();
			content.setSizeFull();
			mainLayout.add(content);
			mainLayout.setFlexGrow(1f, content);
			combo.setValue(wizard.getSelected());
		} else {
			mainLayout.add(new Label(getI18nLabel("no_profiles")));
		}
		return mainLayout;
	}

	private void setupGroupWidgets() {
		for (ModbusProfile source : wizard.getProfiles()) {
			if (!wizard.getMapWidgets().containsKey(source.getId())) {
				//logger.debug("create GroupWidget SLAVE ID {}", source.getConfiguration().getSlaveID());
				String slaveSerial = wizard.getMapSlaves().get(source.getId());
				GroupWidget groupWidget = groupWidgetService.findByExternalId(source.getId(), slaveSerial);
				if (groupWidget == null) {
					groupWidget = createControlPanelGroupWidget(slaveSerial, source);
				} else {
					if (groupWidget.getWidgets().isEmpty()) {
						GraphicWidget graphWidget = createControlPanelWidget(source, groupWidget, slaveSerial);
						groupWidget.getWidgets().add(graphWidget);
					}
					else {
						GraphicWidget widget = groupWidget.getWidgets().get(0);
						if (widget.getFeeds().isEmpty()) {
							List<GraphicFeed> feeds = ControlPanelBaseConstants.createSectionFeeds(source.getRegisters());
							widget.setFeeds(feeds);
						}
						setWidgetPosition(widget);
					}
				}
				groupWidget.getOptions().setRealTime(true);
				wizard.getMapWidgets().put(source.getId(), groupWidget);
			}
		}
		
		// removes inactive feeds
		for (ModbusProfile source : wizard.getProfiles()) {
			String slaveSerial = wizard.getMapSlaves().get(source.getId());
			GroupWidget groupWidget = wizard.getMapWidgets().get(source.getId());

			if (groupWidget != null) {
				GraphicWidget widget = groupWidget.getWidgets().get(0);
				// source.getConfiguration().getSlaveID(),
				logger.debug("setup GroupWidget SLAVE {}, feeds {}", slaveSerial,  widget.getFeeds().size());
				Map<String, ModbusRegister> map = new HashMap<>();
				for (ModbusRegister register : source.getRegisters()) {
					if (register.getActive() && !register.getCrucial()) {
						map.put(register.getMetaIdentifier(), register);
					}
				}
				
				for (Iterator<GraphicFeed> iterator = widget.getFeeds().iterator(); iterator.hasNext();) {
					GraphicFeed feed = iterator.next();
				    if (!map.containsKey(feed.getMetaIdentifier())) {
				        iterator.remove();
				    }
				}
			}
		}
		
	}

	public GroupWidget createControlPanelGroupWidget(String slaveSerial, ModbusProfile source) {
		GroupWidget groupWidget = new GroupWidget();
		groupWidget.setName(slaveSerial);
		groupWidget.setOwner(source.getOwner());
		groupWidget.setDevice(slaveSerial);
		groupWidget.setExternalId(source.getId());
		GraphicWidget graphWidget = createControlPanelWidget(source, groupWidget, slaveSerial);
		groupWidget.getWidgets().add(graphWidget);
		return groupWidget;
	}

	private GraphicWidget createControlPanelWidget(ModbusProfile source, GroupWidget groupWidget, String slaveSerial) {
		GraphicWidget widget = new GraphicWidget();
		widget.setLabel(source.getDisplayName());
		widget.setType(GraphicWidgetType.CUSTOM);
		widget.setProvider(CONTROL_PANEL_WIDGET_PROVIDER);
		widget.getOptions().setScale(ChartScaleType.LINEAR);
		widget.setGroupWidget(groupWidget);
		widget.setDevice(slaveSerial);
		setWidgetPosition(widget);
		widget.getOptions().setFillColor(ChartUtils.quiteRandomHexColor());
		List<GraphicFeed> feeds = ControlPanelBaseConstants.createSectionFeeds(source.getRegisters());
		widget.setFeeds(feeds);
		return widget;
	}

	private void setWidgetPosition(GraphicWidget widget) {
		float aspectRatio = 4 / 3f;
		
		int canvasWidth = (int) ControlPanelBaseProvider.DEFAULT_SIZE[0];
		int canvasHeight = (int) (canvasWidth / aspectRatio);
		
		int width = (int) ControlPanelBaseProvider.DEFAULT_SIZE[0];
		int height = (int) ControlPanelBaseProvider.DEFAULT_SIZE[1];
		
		
		widget.setX(5 / (float) canvasWidth);
		widget.setY(5 / (float) canvasHeight);
		widget.setWidth(width / (float) canvasWidth);
		widget.setHeight(height / (float) canvasHeight);
		
		
		//Bug #2002
//		component.setWidth(98, Unit.PERCENTAGE);;
//		component.setHeight(65, Unit.PERCENTAGE);;
		
//		widget.setWidth(0.98f);
//		widget.setHeight(0.73f);
		
		
	}

	@Override
	public boolean onAdvance() {
		return commit();
	}

	@Override
	public boolean onBack() {
		return commit();
	}

	private boolean commit() {
		if (editor == null)
			return true;
		if (editor.getEntity().getLabel() == null)
			return true;
		if (committed)
			return true;
		try {
			editor.beforeCommit();
			editor.getBinder().writeBean(editor.getEntity());
			editor.afterCommit();
			GraphicWidget bean = editor.getEntity();
			GroupWidget groupWidget = wizard.getMapWidgets().get(bean.getGroupWidget().getExternalId());
			if (groupWidget != null) {
				// re-create widget
				GraphicWidget target = clone(bean);
				groupWidget.setWidgets(new ArrayList<GraphicWidget>());
				groupWidget.addGraphWidget(target);
			}
			logger.debug("commited SLAVE ID {}", this.wizard.getSelected().getConfiguration().getSlaveID());

		} catch (ValidationException e) {
			logger.debug("AernetProStep", e);
			PopupNotification.show(e.getMessage(), Type.ERROR);
			return false;
		} catch (EditorConstraintException e) {
			PopupNotification.show(e.getMessage(), Type.ERROR);
			return false;
		}
		
		committed = true;
		return true;
	}
	
	private GraphicWidget clone(GraphicWidget bean) {
		GraphicWidget target = new GraphicWidget();
		BeanUtils.copyProperties(bean, target, new String[] { "id" });
		List<GraphicFeed> feeds = new ArrayList<GraphicFeed>();
		for (GraphicFeed s : bean.getFeeds()) {
			GraphicFeed t = new GraphicFeed();
			BeanUtils.copyProperties(s, t, new String[] { "id" });
			feeds.add(t);
		}
		target.setFeeds(feeds);
		return target;
	}
	public boolean validateStep() {
		return commit();
	}
}
