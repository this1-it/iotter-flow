package it.thisone.iotter.ui.devices;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vaadin.gridutil.cell.GridCellFilter;
import org.vaadin.viritin.button.PrimaryButton;
import org.vaadin.viritin.form.AbstractForm;
import org.vaadin.viritin.layouts.MFormLayout;

import com.vaadin.annotations.PropertyId;
import com.vaadin.data.HasValue;
import com.vaadin.data.provider.ListDataProvider;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.server.Page;
import com.vaadin.server.StreamResource;
import com.vaadin.server.StreamResource.StreamSource;
import com.vaadin.shared.ui.grid.HeightMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.DateField;
import com.vaadin.ui.Grid;
import com.vaadin.ui.Grid.SelectionMode;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.MenuBar.Command;
import com.vaadin.ui.MenuBar.MenuItem;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;


import it.thisone.iotter.config.Constants;
import it.thisone.iotter.enums.DeviceStatus;
import it.thisone.iotter.enums.TracingAction;
import it.thisone.iotter.persistence.model.Device;
import it.thisone.iotter.persistence.model.DeviceCriteria;
import it.thisone.iotter.persistence.model.DeviceModel;
import it.thisone.iotter.persistence.model.ResourceData;
import it.thisone.iotter.persistence.model.User;
import it.thisone.iotter.ui.common.BaseComponent;
import it.thisone.iotter.ui.common.EditorSelectedEvent;
import it.thisone.iotter.ui.common.EditorSelectedListener;
import it.thisone.iotter.ui.common.UIUtils;
import it.thisone.iotter.ui.common.fields.DeviceModelSelect;
import it.thisone.iotter.ui.common.fields.EditableResourceData;
import it.thisone.iotter.util.EncryptUtils;
import it.thisone.iotter.util.PopupNotification;

public class DeviceProductionImporter extends BaseComponent {
	private static final String STICKY = "sticky";
	/**
	 * 
	 */
	private static final long serialVersionUID = 4337523807788095820L;
	private DeviceProductionForm form;
	private Grid<Device> grid;
	private ListDataProvider<Device> dataProvider;
	private HasValue.ValueChangeListener<ResourceData> resourceValueChangeListener;
	private EditableResourceData upload;
	private static Logger logger = LoggerFactory.getLogger(DeviceProductionImporter.class);


	public DeviceProductionImporter() {
		super(DeviceForm.NAME, null);
		setCompositionRoot(buildLayout());
	}

	private HasValue.ValueChangeListener<ResourceData> resourceValueChangeListener(final EditableResourceData field) {
		return new HasValue.ValueChangeListener<ResourceData>() {
			private static final long serialVersionUID = -2226780514986839696L;

			@Override
			public void valueChange(HasValue.ValueChangeEvent<ResourceData> event) {
				importResource(field.getValue());
			}
		};
	}

	protected void importResource(ResourceData resource) {
		List<Device> items = readItems(resource.getData());
		if (!items.isEmpty()) {
			dataProvider.getItems().clear();
			dataProvider.getItems().addAll(items);
			dataProvider.refreshAll();
			String msg = UIUtils.localize(getI18nKey() + ".import_production.devices", new Object[] { items.size() },
					null);
			PopupNotification.show(msg, Type.HUMANIZED_MESSAGE);
		} else {
			PopupNotification.show(getI18nLabel("import_production.not_found"), Type.ERROR_MESSAGE);
		}
		upload.setReadOnly(true);
	}

	private List<Device> readItems(byte[] data) {
		List<Device> items = new ArrayList<>();
		CSVParser csvFileParser = null;
		try {
			BufferedReader is = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(data), "UTF8"));
			csvFileParser = new CSVParser(is, CSVFormat.DEFAULT);
			for (CSVRecord record : csvFileParser.getRecords()) {
				if (record.size() >= 2 && record.get(0).length() >= Constants.Validators.MIN_SERIAL_NUMBER_LENGTH) {
					String serial = record.get(0);
					String activationKey = record.get(1);
					String writeApikey = EncryptUtils.createWriteApiKey(serial);
					Device item = null;
					item = UIUtils.getServiceFactory().getDeviceService().findBySerial(serial);
					if (item != null) {
						logger.error("{} serial already present", serial);
						continue;
					}
					item = UIUtils.getServiceFactory().getDeviceService().findByActivationKey(activationKey);
					if (item != null) {
						logger.error("{} activationKey already present", activationKey);
						continue;
					}
						
					item = new Device();
					item.setSticky(true);
					item.setSerial(serial);
					item.setLabel(serial);
					item.setActivationKey(activationKey);
					item.setWriteApikey(writeApikey);
					item.setOwner(Constants.ROLE_PRODUCTION.toLowerCase());
					items.add(item);
				}
				else {
					logger.error("{} not importable", record.get(0));
				}
			}

		} catch (NullPointerException | IOException e) {
		} finally {
			try {
				if (csvFileParser != null)
					csvFileParser.close();
			} catch (IOException e) {
			}
		}
		return items;
	}

	@SuppressWarnings({ "serial" })
	protected MenuBar filterStickyMenuBar() {
		MenuBar root = new MenuBar();
		root.addStyleName(ValoTheme.BUTTON_BORDERLESS);
		final MenuItem select = root.addItem("", VaadinIcons.EYE_SLASH, null);
		select.addItem(getI18nLabel("import_production.select_all"), null, new Command() {
			@Override
			public void menuSelected(MenuItem selectedItem) {
				dataProvider.clearFilters();
				select.setIcon(VaadinIcons.CHECK_SQUARE_O);
				for (Device item : dataProvider.getItems()) {
					item.setSticky(true);
				}
				dataProvider.refreshAll();
				grid.clearSortOrder();
			}
		});
		select.addItem(getI18nLabel("import_production.select_none"), null, new Command() {
			@Override
			public void menuSelected(MenuItem selectedItem) {
				select.setIcon(VaadinIcons.THIN_SQUARE);
				dataProvider.clearFilters();
				for (Device item : dataProvider.getItems()) {
					item.setSticky(false);
				}
				dataProvider.refreshAll();
				grid.clearSortOrder();
			}
		});

		return root;
	}

	public String getI18nLabel(String key) {
		return UIUtils.localize(getI18nKey() + "." + key);
	}

	public void addListener(EditorSelectedListener listener) {
		try {
			Method method = EditorSelectedListener.class.getDeclaredMethod(EditorSelectedListener.EDITOR_SELECTED,
					new Class[] { EditorSelectedEvent.class });
			addListener(EditorSelectedEvent.class, listener, method);
		} catch (final java.lang.NoSuchMethodException e) {
			throw new java.lang.RuntimeException("Internal error, editor selected method not found");
		}
	}

	public void removeListener(EditorSelectedListener listener) {
		removeListener(EditorSelectedEvent.class, listener);
	}

	public String getWindowStyle() {
		return getI18nKey();
	}

	public float[] getWindowDimension() {
		return UIUtils.L_DIMENSION;
	}

	@SuppressWarnings("serial")
	private Component buildLayout() {

		grid = new Grid<>(Device.class);
		grid.addStyleName("smallgrid");
		grid.setHeightMode(HeightMode.CSS);
		grid.setSelectionMode(SelectionMode.SINGLE);
		grid.setSizeFull();
		
		dataProvider = new ListDataProvider<>(new ArrayList<>());
		grid.setDataProvider(dataProvider);
		
		// Remove default columns and add custom ones
		grid.removeAllColumns();
		
		// Add checkbox component column
		grid.addComponentColumn(device -> {
			CheckBox checkBox = new CheckBox();
			checkBox.setValue(device.isSticky());
			checkBox.addValueChangeListener(event -> {
				device.setSticky(event.getValue());
				dataProvider.refreshItem(device);
			});
			return checkBox;
		}).setId(STICKY).setCaption("").setWidth(100);
		
		// Add regular columns
		grid.addColumn(Device::getSerial).setId("serial")
			.setCaption(getI18nLabel("serial")).setExpandRatio(1);
		grid.addColumn(Device::getActivationKey).setId("activationKey")
			.setCaption(getI18nLabel("activationKey")).setExpandRatio(1);

		MenuBar root = filterStickyMenuBar();
		GridCellFilter<Device> filter = new GridCellFilter<>(grid);
		filter.getFilterRow().getCell(STICKY).setComponent(root);

		boolean ignoreCase = true;
		boolean onlyMatchPrefix = false;
		org.vaadin.gridutil.cell.CellFilterComponent<TextField> serialFilter = 
			filter.setTextFilter("serial", ignoreCase, onlyMatchPrefix, "");
		TextField serial = serialFilter.getComponent();
		serial.setStyleName(ValoTheme.TEXTFIELD_TINY);
		grid.setSizeFull();

		HorizontalLayout header = new HorizontalLayout();
		header.setMargin(true);
		header.setDefaultComponentAlignment(Alignment.MIDDLE_LEFT);
		header.setWidth(100.0f, Unit.PERCENTAGE);

		upload = new EditableResourceData();
		upload.getUploadField().setAcceptFilter(".csv");
		upload.getResourceLabel().setCaption(getI18nLabel("import_production.upload"));
		resourceValueChangeListener = resourceValueChangeListener(upload);
		upload.addValueChangeListener(resourceValueChangeListener);
		header.addComponent(upload);

		Button download = new Button();
		download.setIcon(VaadinIcons.DOWNLOAD);
		download.addClickListener(new Button.ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				downloadActivated();
			}
		});

		header.addComponent(download);
		header.setComponentAlignment(download, Alignment.MIDDLE_RIGHT);

		form = new DeviceProductionForm();
		form.setCaption(getI18nLabel("import_production.defaults"));
		form.getSaveButton().setCaption(UIUtils.localize("basic.editor.confirm"));
		form.setResetButton(new PrimaryButton(UIUtils.localize("basic.editor.cancel")).withVisible(false));
		Device entity = new Device();
		form.setEntity(entity);
		form.setSavedHandler(new AbstractForm.SavedHandler<Device>() {
			@Override
			public void onSave(Device entity) {
				List<Device> items = new ArrayList<>();
				for (Device item : dataProvider.getItems()) {
					if (item.isSticky()) {
						item.setModel(form.getEntity().getModel());
						item.setFirmwareVersion(form.getEntity().getFirmwareVersion());
						item.setProductionDate(form.getEntity().getProductionDate());
						item.setInactivityMinutes(form.getEntity().getInactivityMinutes());
						item.setOwner(form.getEntity().getOwner());
						if (!form.getEntity().getOwner().equals(Constants.ROLE_PRODUCTION.toLowerCase())) {
							item.setStatus(DeviceStatus.ACTIVATED);
						}
						UIUtils.getServiceFactory().getDeviceService().trace(item, TracingAction.DEVICE_CREATION,
								item.toString(), UIUtils.getUserDetails().getName(), null);

						items.add(item);
					}
				}
				if (!items.isEmpty()) {
					fireEvent(new EditorSelectedEvent(DeviceProductionImporter.this, items));
				} else {
					PopupNotification.show(getI18nLabel("import_production.not_selected"), Type.ERROR_MESSAGE);
					form.model.setValue((DeviceModel) null);
				}
			}
		});

		form.setResetHandler(new AbstractForm.ResetHandler<Device>() {
			@Override
			public void onReset(Device entity) {
				List<Device> items = null;
				fireEvent(new EditorSelectedEvent(DeviceProductionImporter.this, items));
			}
		});

		VerticalLayout layout = new VerticalLayout();
		layout.setMargin(true);
		layout.addComponent(form);
		form.productionDate.setValue(LocalDate.now());

		VerticalLayout verticalLayout = new VerticalLayout();
		verticalLayout.setSizeFull();
		verticalLayout.addComponent(header);
		verticalLayout.addComponent(grid);
		verticalLayout.setExpandRatio(grid, 1f);
		verticalLayout.addComponent(layout);

		HorizontalLayout buttonLayout = new HorizontalLayout();
		buttonLayout.setSpacing(true);
		buttonLayout.addComponent(form.getSaveButton());
		buttonLayout.addComponent(form.getResetButton());
		buttonLayout.addComponent(form.getDeleteButton());

		HorizontalLayout footer = new HorizontalLayout();
		footer.addStyleName(ValoTheme.WINDOW_BOTTOM_TOOLBAR);
		footer.setWidth(100.0f, Unit.PERCENTAGE);
		footer.setDefaultComponentAlignment(Alignment.MIDDLE_CENTER);
		footer.addComponent(buttonLayout);
		footer.setExpandRatio(buttonLayout, 1f);
		verticalLayout.addComponent(footer);

		return verticalLayout;
	}

	protected void downloadActivated() {
		String fn = String.format("activated-%tF.csv", new Date());
		Page.getCurrent().open(createStreamResource(fn), null, false);

		// Button lnkFile = new Button("activated devices");
		// lnkFile.setIcon(VaadinIcons.FILE);
		// lnkFile.setStyleName("link");
		// StreamResource stream = createStreamResource();
		// FileDownloader fileDownloader = new FileDownloader(stream);
		// fileDownloader.extend(lnkFile);
		// Window dialog = new Window("Download CSV");
		// dialog.setContent(lnkFile);
		// dialog.center();
		// dialog.setResizable(false);
		// dialog.setDraggable(false);
		// dialog.setClosable(true);
		// dialog.setImmediate(true);
		// UI.getCurrent().addWindow(dialog);
		// lnkFile.addClickListener(new Button.ClickListener() {
		// @Override
		// public void buttonClick(ClickEvent event) {
		// dialog.close();
		// }
		// });

	}

	private StreamResource createStreamResource(String fn) {
		StreamResource stream = new StreamResource(new StreamSource() {
			private static final long serialVersionUID = 3825453932246713909L;

			@Override
			public InputStream getStream() {
				DeviceCriteria criteria = new DeviceCriteria();
				//criteria.setActivated(true);
				List<Device> devices = UIUtils.getServiceFactory().getDeviceService().search(criteria, 0, 10000);
				StringWriter sw = new StringWriter();
				sw.append("serial,production date,activation date,firmware,model,owner,status\n");
				for (Device device : devices) {
					DeviceStatus status = device.getStatus();
					String owner = device.getOwner();
//					if (status.equals(DeviceStatus.PRODUCED) && device.getHistory().getOwner() != null) {
//						owner =  owner + "/" +device.getHistory().getOwner();
//					}
					sw.append(String.format("%s,%tF,%tF,%s,%s,%s,%s\n", 
							device.getSerial(), //
							device.getProductionDate(), //
							device.getActivationDate(), //
							device.getFirmwareVersion(), //
							device.getModel().getName(), //
							owner, //
							status.name() //
							));
				}
				return new ByteArrayInputStream(sw.toString().getBytes());
			}
		}, fn);
		return stream;
	}

	public class DeviceProductionForm extends AbstractForm<Device> {

		@PropertyId("model")
		private DeviceModelSelect model;

		@PropertyId("firmwareVersion")
		private TextField firmwareVersion;

		@PropertyId("productionDate")
		private DateField productionDate;

		@PropertyId("inactivityMinutes")
		private DeviceInactivityOptionGroup inactivityMinutes;

		@PropertyId("owner")
		private ComboBox<String> owner;

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public DeviceProductionForm() {
			super(Device.class);

			Collection<DeviceModel> models = UIUtils.getServiceFactory().getDeviceService().getDeviceModels();
			model = new DeviceModelSelect(models);
			model.setSizeFull();
			model.setRequiredIndicatorVisible(true);
			model.setCaption(getI18nLabel("model"));

			firmwareVersion = new TextField();
			firmwareVersion.setSizeFull();
			firmwareVersion.setRequiredIndicatorVisible(true);
			firmwareVersion.setCaption(getI18nLabel("firmwareVersion"));

			productionDate = new DateField();
			productionDate.setSizeFull();
			productionDate.setRequiredIndicatorVisible(true);
			productionDate.setCaption(getI18nLabel("productionDate"));

			inactivityMinutes = new DeviceInactivityOptionGroup();
			inactivityMinutes.setSizeFull();
			inactivityMinutes.setRequiredIndicatorVisible(true);
			inactivityMinutes.setCaption(getI18nLabel("inactivityMinutes"));

			owner = new ComboBox<>(getI18nLabel("owner"));
			owner.setEmptySelectionAllowed(false);
			owner.setTextInputAllowed(false);
			owner.setSizeFull();
			owner.setRequiredIndicatorVisible(true);

			List<String> owners = new ArrayList<>();
			owners.add(Constants.ROLE_PRODUCTION.toLowerCase());

			List<User> users = UIUtils.getServiceFactory().getUserService().findByRole(Constants.ROLE_ADMINISTRATOR);
			for (User user : users) {
				owners.add(user.getUsername());
			}

			owner.setItems(owners);
			owner.setStyleName(ValoTheme.COMBOBOX_SMALL);
			owner.setValue(Constants.ROLE_PRODUCTION.toLowerCase());

		}

		@Override
		protected Component createContent() {
			return new MFormLayout(model, firmwareVersion, productionDate, inactivityMinutes, owner).withMargin(false)
					.withWidth(100, Unit.PERCENTAGE);
		}

	}

}
