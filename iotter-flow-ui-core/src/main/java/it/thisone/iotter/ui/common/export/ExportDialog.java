package it.thisone.iotter.ui.common.export;

import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

//import org.vaadin.gridutil.cell.CellFilterComponent;
//import org.vaadin.gridutil.cell.GridCellFilter;

import com.google.common.collect.Range;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationResult;
import com.vaadin.flow.data.binder.Validator;
import com.vaadin.flow.data.binder.ValueContext;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.validator.AbstractValidator;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.SelectionMode;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.menubar.*;
import org.vaadin.flow.components.PanelFlow;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.server.StreamResource;

import it.thisone.iotter.cassandra.model.CassandraExportFeed;
import it.thisone.iotter.cassandra.model.Interpolation;
import it.thisone.iotter.enums.ExportFileMode;
import it.thisone.iotter.enums.ExportFormat;
import it.thisone.iotter.enums.Order;
import it.thisone.iotter.exporter.ExportConfig;
import it.thisone.iotter.exporter.ExportGroupConfig;
import it.thisone.iotter.exporter.ExportProperties;
import it.thisone.iotter.exporter.IExportConfig;
import it.thisone.iotter.exporter.IExportProvider;
import it.thisone.iotter.persistence.model.Device;
import it.thisone.iotter.ui.common.BaseComponent;
import it.thisone.iotter.ui.common.UIUtils;
import it.thisone.iotter.ui.common.charts.TimeIntervalHelper;
import it.thisone.iotter.ui.eventbus.ExportStartEvent;
import it.thisone.iotter.ui.ifc.IGroupWidgetUiFactory;

import it.thisone.iotter.util.PopupNotification;

/*
 * Feature #167 Export data from visualizations
 */
public class ExportDialog extends Dialog {
	

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final String LABEL = "label";
	private static final String SELECTED = "selected";
	private static final String i18nkey = "export.dialog";
	private RadioButtonGroup<ExportFileMode> fileModeField;
	private RadioButtonGroup<Order> orderField;
	private TextField emailField;
	private RadioButtonGroup<ExportFormat> formatField;
	private ComboBox<String> decimalSeparatorField;
	private ComboBox<Interpolation> interpolationField;
	private ComboBox<String> columnSeparatorField;
	private TextField customSeparatorField;
	private Binder<ExportFormBean> binder;
	private TimeIntervalHelper helper;
	private DatePicker fromDateField;
	private DatePicker toDateField;

	private Date lowerBound;
	private Date upperBound;

	private final Grid<CassandraExportFeed> grid = new Grid<>();

	private Button export;
	private Button cancel;

	protected IGroupWidgetUiFactory config;
	private final Executor executor;

	@SuppressWarnings("serial")
	public ExportDialog(IExportConfig config, ExportProperties properties, Device device, Executor executor) {
		super();
		if (executor == null) {
			throw new IllegalArgumentException("Executor is required");
		}
		this.executor = executor;
		//setHeaderTitle(getI18nLabel("single_csv_export") + " " + config.getName());
		setDraggable(false);
		// setImmediate(true);

		lowerBound = config.getInterval().lowerEndpoint();
		upperBound = config.getInterval().upperEndpoint();

		initGrid(config);

		helper = new TimeIntervalHelper(properties.getTimeZone());

		fromDateField = helper.createDateField();
//		fromDateField.setRangeStart(helper.toLocalDate(config.getInterval().lowerEndpoint()));
//		fromDateField.setRangeEnd(helper.toLocalDate(config.getInterval().upperEndpoint()));
		// fromDateField.setDateOutOfRangeMessage(getI18nLabel("date_out_of_range")); // Not available in V8
		// fromDateField.setValidationVisible(true); // Not available in V8

		fromDateField.setValue(helper.toLocalDate(config.getInterval().lowerEndpoint()));
		fromDateField.setLabel(getI18nLabel("from_date"));

		toDateField = helper.createDateField();
		toDateField.setLabel(getI18nLabel("to_date"));

		// toDateField.setValidationVisible(true); // Not available in V8
		toDateField.setValue(helper.toLocalDate(config.getInterval().upperEndpoint()));
//		toDateField.setRangeStart(helper.toLocalDate(config.getInterval().lowerEndpoint()));
//		toDateField.setRangeEnd(helper.toLocalDate(config.getInterval().upperEndpoint()));
		// toDateField.setDateOutOfRangeMessage(getI18nLabel("date_out_of_range")); // Not available in V8

		String[] separators = new String[] { ",", ";", "\t" };
		String dec = new String(new char[] { DecimalFormatSymbols.getInstance().getDecimalSeparator() });

		columnSeparatorField = new ComboBox<>();
		columnSeparatorField.setItems(separators);
//		columnSeparatorField.setItemLabelGenerator(item -> {
//			switch (item) {
//				case ",": return ",";
//				case ";": return ";";
//				case "\t": return "tab";
//				default: return item;
//			}
//		});
		//columnSeparatorField.setEmptySelectionAllowed(false);
		columnSeparatorField.setValue(separators[0]);
		columnSeparatorField.setLabel(getI18nLabel("column_separator"));

		customSeparatorField = new TextField(getI18nLabel("custom_separator"));
		customSeparatorField.setMaxLength(1);

		String[] decimals = new String[] { ".", "," };
		decimalSeparatorField = new ComboBox<>();
		decimalSeparatorField.setItems(decimals);
		//decimalSeparatorField.setEmptySelectionAllowed(false);
		decimalSeparatorField.setValue(dec);
		decimalSeparatorField.setLabel(getI18nLabel("decimal_separator"));

		interpolationField = createInterpolationCombo(config.getInterval(), device);
		interpolationField.setLabel(getI18nLabel("interpolation"));

		fileModeField = createFileModeRadioButtonGroup();
//		fileModeField.setStyleName(ValoTheme.OPTIONGROUP_HORIZONTAL);
		fileModeField.setLabel(getI18nLabel("file_mode"));
		fileModeField.setValue(properties.getFileMode());
		// fileModeField.setVisible(false);

		orderField = createOrderRadioButtonGroup();
//		orderField.setStyleName(ValoTheme.OPTIONGROUP_HORIZONTAL);
		orderField.setLabel(getI18nLabel("export_order"));
		orderField.setValue(properties.getOrder());

		emailField = new TextField(getI18nLabel("export_email"));

		formatField = createFormatRadioButtonGroup();
		formatField.setLabel(getI18nLabel("format"));
		formatField.addValueChangeListener(event -> {
			ExportFormat value = event.getValue();
			boolean visible = value != null && value.equals(ExportFormat.CSV);
			columnSeparatorField.setVisible(visible);
			customSeparatorField.setVisible(visible);
			decimalSeparatorField.setVisible(visible);
		});
		formatField.setValue(properties.getFormat());

		ExportFormBean formBean = new ExportFormBean();
		formBean.setColumnSeparator(separators[0]);
		formBean.setCustomSeparator("");
		formBean.setDecimalSeparator(dec);
		formBean.setInterpolation(interpolationField.getValue());
		formBean.setFileMode(properties.getFileMode());
		formBean.setFormat(properties.getFormat());
		formBean.setExportEmail("");
		formBean.setExportOrder(properties.getOrder());
		formBean.setFromDate(helper.toLocalDate(config.getInterval().lowerEndpoint()));
		formBean.setToDate(helper.toLocalDate(config.getInterval().upperEndpoint()));

		binder = new Binder<>();
		binder.forField(columnSeparatorField).bind(ExportFormBean::getColumnSeparator, ExportFormBean::setColumnSeparator);
		binder.forField(customSeparatorField).bind(ExportFormBean::getCustomSeparator, ExportFormBean::setCustomSeparator);
		binder.forField(decimalSeparatorField).bind(ExportFormBean::getDecimalSeparator, ExportFormBean::setDecimalSeparator);
		binder.forField(interpolationField).bind(ExportFormBean::getInterpolation, ExportFormBean::setInterpolation);
		binder.forField(fileModeField).bind(ExportFormBean::getFileMode, ExportFormBean::setFileMode);
		binder.forField(emailField).bind(ExportFormBean::getExportEmail, ExportFormBean::setExportEmail);
		binder.forField(orderField).bind(ExportFormBean::getExportOrder, ExportFormBean::setExportOrder);
		binder.forField(formatField).bind(ExportFormBean::getFormat, ExportFormBean::setFormat);
		binder.forField(fromDateField).bind(ExportFormBean::getFromDate, ExportFormBean::setFromDate);
		binder.forField(toDateField).bind(ExportFormBean::getToDate, ExportFormBean::setToDate);
		binder.setBean(formBean);

		// columnSeparatorField.setImmediate(true);
		// customSeparatorField.setImmediate(true);
		// decimalSeparatorField.setImmediate(true);
		// interpolationField.setImmediate(true);
		// fileModeField.setImmediate(true);
		// emailField.setImmediate(true);
		// fromDateField.setImmediate(true);
		// toDateField.setImmediate(true);

		columnSeparatorField.setWidth(100, Unit.PERCENTAGE);
		customSeparatorField.setWidth(100, Unit.PERCENTAGE);
		decimalSeparatorField.setWidth(100, Unit.PERCENTAGE);
		emailField.setWidth(100, Unit.PERCENTAGE);
		interpolationField.setWidth(100, Unit.PERCENTAGE);
//		fileModeField.setWidth(100, Unit.PERCENTAGE);
//		formatField.setWidth(100, Unit.PERCENTAGE);
		fromDateField.setWidth(100, Unit.PERCENTAGE);
		toDateField.setWidth(100, Unit.PERCENTAGE);

		// Cross-validation: fromDate must be before toDate
		final Validator<LocalDate> fromDateValidator = new AbstractValidator<LocalDate>(getI18nLabel("invalid_date_range")) {
			@Override
			public ValidationResult apply(LocalDate fromDate, ValueContext context) {
				if (fromDate == null) {
					return ValidationResult.ok();
				}
				LocalDate toDate = toDateField.getValue();
				if (toDate != null && fromDate.isAfter(toDate)) {
					return ValidationResult.error(getI18nLabel("from_date_after_to_date"));
				}
				return ValidationResult.ok();
			}
		};

		final Validator<LocalDate> toDateValidator = new AbstractValidator<LocalDate>(getI18nLabel("invalid_date_range")) {
			@Override
			public ValidationResult apply(LocalDate toDate, ValueContext context) {
				if (toDate == null) {
					return ValidationResult.ok();
				}
				LocalDate fromDate = fromDateField.getValue();
				if (fromDate != null && fromDate.isAfter(toDate)) {
					return ValidationResult.error(getI18nLabel("to_date_before_from_date"));
				}
				return ValidationResult.ok();
			}
		};

		// Add cross-validation to binder
		binder.forField(fromDateField)
			.asRequired(getI18nLabel("from_date_required"))
			.withValidator(fromDateValidator)
			.bind(ExportFormBean::getFromDate, ExportFormBean::setFromDate);
		
		binder.forField(toDateField)
			.asRequired(getI18nLabel("to_date_required"))
			.withValidator(toDateValidator)
			.bind(ExportFormBean::getToDate, ExportFormBean::setToDate);

		// Add value change listeners for cross-validation
		fromDateField.addValueChangeListener(event -> {
			// Trigger validation on both fields when fromDate changes
			binder.validate();
		});

		toDateField.addValueChangeListener(event -> {
			// Trigger validation on both fields when toDate changes
			binder.validate();
		});
		
		// Note: Email validation will be added with V8 compatible validator if needed
		binder.forField(emailField)
			.bind(ExportFormBean::getExportEmail, ExportFormBean::setExportEmail);

		if (device != null) {
			emailField.setVisible(false);
			fileModeField.setVisible(false);
		} else {
			toDateField.setVisible(false);
			fromDateField.setVisible(false);
			grid.setVisible(false);
		}

		FormLayout fieldLayout = new FormLayout();


		if (config instanceof ExportGroupConfig) {
			fieldLayout.add(fileModeField);
		}

		fieldLayout.add(fromDateField);
		fieldLayout.add(toDateField);
		fieldLayout.add(interpolationField);

		fieldLayout.add(orderField);
		fieldLayout.add(formatField);
		fieldLayout.add(columnSeparatorField);
		fieldLayout.add(customSeparatorField);
		fieldLayout.add(decimalSeparatorField);
		fieldLayout.add(emailField);

        PanelFlow panel = new PanelFlow();
		panel.setContent(fieldLayout);
		panel.setSizeFull();

		HorizontalLayout content = new HorizontalLayout();
		content.setSizeFull();
		content.add(panel);
		content.setPadding(true);

//		exportListener = createExportListener(config, properties, helper);
//		export = new Button(getI18nLabel("export"));
//		export.setIcon(UIUtils.ICON_START);
//		export.addClickListener(exportListener);
//		enableExport(true);

		cancel = new Button(getI18nLabel("cancel"));
		cancel.addClickListener( event-> {
				close();
			}
		);



		HorizontalLayout buttonLayout = new HorizontalLayout();
		buttonLayout.setSpacing(true);
		buttonLayout.setPadding(true);
		buttonLayout.add(export);
		buttonLayout.add(cancel);

		HorizontalLayout footer = new HorizontalLayout();
		footer.setWidth(100.0f, Unit.PERCENTAGE);
//		footer.addClassName(ValoTheme.WINDOW_BOTTOM_TOOLBAR);
//		footer.setDefaultComponentAlignment(Alignment.MIDDLE_CENTER);
		footer.add(buttonLayout);

		VerticalLayout verticalLayout = new VerticalLayout();
//		verticalLayout.setDefaultComponentAlignment(Alignment.MIDDLE_CENTER);
		verticalLayout.setSizeFull();
		verticalLayout.setSpacing(true);
		verticalLayout.add(grid);
//		verticalLayout.setExpandRatio(grid, 1f);
		verticalLayout.add(fieldLayout);
//		verticalLayout.setExpandRatio(fieldLayout, 1f);
		verticalLayout.add(footer);
		add(verticalLayout);

	}

	private void initGrid(IExportConfig config) {
		grid.addClassName("smallgrid");
//		grid.setHeightMode(HeightMode.CSS);
		grid.setSelectionMode(SelectionMode.SINGLE);
		grid.setSizeFull();
		List<CassandraExportFeed> feeds = new ArrayList<>();
		if (config instanceof ExportGroupConfig) {
			for (ExportConfig cfg : ((ExportGroupConfig) config).getExportConfigs()) {
				feeds.addAll(cfg.getFeeds());
			}
		} else if (config instanceof ExportConfig) {
			feeds = ((ExportConfig) config).getFeeds();
		}
		ListDataProvider<CassandraExportFeed> dataProvider = new ListDataProvider<>(feeds);
		grid.setDataProvider(dataProvider);

//		grid.removeAllColumns();
//		grid.addComponentColumn(feed -> {
//			Checkbox checkBox = new Checkbox();
//			checkBox.setValue(feed.isSelected());
//			checkBox.addValueChangeListener(event -> {
//				feed.setSelected(event.getValue());
//				boolean enabled = false;
//				for (CassandraExportFeed f : dataProvider.getItems()) {
//					if (f.isSelected()) {
//						enabled = true;
//						break;
//					}
//				}
//				enableExport(enabled);
//			});
//			return checkBox;
//		}).setId(SELECTED).setLabel("").setWidth(100);
//
//		grid.addColumn(CassandraExportFeed::getLabel).setId(LABEL).setLabel(getI18nLabel("export_label")).setExpandRatio(1);
//
//		filter = new GridCellFilter<CassandraExportFeed>(grid);
//		boolean ignoreCase = true;
//		boolean onlyMatchPrefix = false;
//		CellFilterComponent<TextField> displayNameFilter = filter.setTextFilter(LABEL, ignoreCase, onlyMatchPrefix, "");
//		TextField displayName = displayNameFilter.getComponent();
//		displayName.setStyleName(ValoTheme.TEXTFIELD_TINY);
//		grid.getHeaderRow(0).getCell(SELECTED).setComponent(root);

		MenuBar root = filterSelectedMenuBar((ListDataProvider<CassandraExportFeed>) grid.getDataProvider());
		
	}

	@SuppressWarnings("serial")
	protected MenuBar filterSelectedMenuBar(ListDataProvider<CassandraExportFeed> dataProvider) {
		MenuBar root = new MenuBar();
		//root.addClassName(ValoTheme.BUTTON_BORDERLESS);
//		final MenuItem select = root.addItem("", VaadinIcon.EYE_SLASH, null);
//		String select_all = getI18nLabel("grid.select_all");
//		select.addItem(select_all, null, new Command() {
//			@Override
//			public void menuSelected(MenuItem selectedItem) {
//				dataProvider.clearFilters();
//				select.setIcon(VaadinIcon.CHECK_SQUARE_O);
//				for (CassandraExportFeed item : dataProvider.getItems()) {
//					item.setSelected(true);
//				}
//				grid.getDataProvider().refreshAll();
//				enableExport(dataProvider.getItems().size() > 0);
//			}
//		});
//
//		String select_none = getI18nLabel("grid.select_none");
//		select.addItem(select_none, null, new Command() {
//			@Override
//			public void menuSelected(MenuItem selectedItem) {
//				filter.clearAllFilters();
//				select.setIcon(VaadinIcon.CIRCLE_THIN);
//				dataProvider.clearFilters();
//				for (CassandraExportFeed item : dataProvider.getItems()) {
//					item.setSelected(false);
//				}
//				grid.getDataProvider().refreshAll();
//				enableExport(false);
//			}
//		});
//
//		String show_selected = getI18nLabel("grid.show_selected");
//		select.addItem(show_selected, null, new Command() {
//			@Override
//			public void menuSelected(MenuItem selectedItem) {
//				filter.clearAllFilters();
//				select.setIcon(VaadinIcon.EYE);
//				dataProvider.setFilter(feed -> feed.isSelected());
//			}
//		});
//
//		String show_all = getI18nLabel("grid.show_all");
//		select.addItem(show_all, null, new Command() {
//			@Override
//			public void menuSelected(MenuItem selectedItem) {
//				filter.clearAllFilters();
//				select.setIcon(VaadinIcon.EYE_SLASH);
//				dataProvider.clearFilters();
//			}
//		});
		return root;
	}

	private ComponentEventListener<ClickEvent<Button>> createExportListener(final IExportConfig config,
			final ExportProperties properties, final TimeIntervalHelper helper) {
		return event -> {
			try {
				if (binder.validate().isOk()) {
					ExportFormBean formBean = binder.getBean();
					properties.setFormat(formBean.getFormat());
					properties.setDecimalSeparator(getDecimalSeparator());
					properties.setColumnSeparator(getColumnSeparator());
					properties.setFileMode(formBean.getFileMode());
					properties.setOrder(formBean.getExportOrder());
					config.setInterpolation(formBean.getInterpolation());
					String email = formBean.getExportEmail();
					String owner = UIUtils.getUserDetails().getUsername();
					if (config.getLockId() == null) {
						config.setLockId(config.getName() + owner);
					}
					Date lower = helper.toDate(formBean.getFromDate());
					Date upper = helper.toDate(formBean.getToDate());

					Range<Date> interval = Range.closedOpen(lower, upper);
					config.setInterval(interval);

					ExportStartEvent start = new ExportStartEvent(owner, email, config, properties);
					UI ui = UI.getCurrent();
					Locale locale = ui == null ? UIUtils.getLocale() : ui.getLocale();
					if (ui == null) {
						PopupNotification.show(getI18nLabel("invalid_data"));
						return;
					}

					CompletableFuture
							.supplyAsync(() -> runExport(start, locale), executor)
							.whenComplete((result, ex) -> ui.access(() -> handleExportResult(result, ex, start)));

					PopupNotification.show(getI18nLabel("export_started"));
					close();

				} else {
					PopupNotification.show(getI18nLabel("invalid_data"));
				}
			} catch (Exception e) {
				PopupNotification.show(getI18nLabel("invalid_data"));
			}
		};
	}

	private char getColumnSeparator() {
		if (customSeparatorField.getValue() == null
				|| customSeparatorField.getValue().isEmpty()) {
			return columnSeparatorField.getValue().charAt(0);
		}
		return customSeparatorField.getValue().charAt(0);
	}

	private char getDecimalSeparator() {
		return decimalSeparatorField.getValue().charAt(0);
	}

	private ExportResult runExport(ExportStartEvent event, Locale locale) {
		boolean lockAcquired = false;
		try {
			if (event.getConfig().getLockId() != null) {
				if (!UIUtils.getCassandraService().getRollup()
						.lockSink(event.getConfig().getLockId(), 15 * 3600)) {
					return ExportResult.locked();
				}
				lockAcquired = true;
			}
			IExportProvider provider = UIUtils.getServiceFactory().getExportService();
			File exported = provider.createExportDataFile(event.getConfig(), event.getProperties());
			if (exported != null && event.getEmail() != null && !event.getEmail().trim().isEmpty()) {
				UIUtils.getServiceFactory().getNotificationService()
						.forwardVisualization(event.getEmail(), locale, exported, event.getConfig().getName());
			}
			return ExportResult.success(exported);
		} catch (Exception ex) {
			return ExportResult.failure();
		} finally {
			if (lockAcquired) {
				UIUtils.getCassandraService().getRollup().unlockSink(event.getConfig().getLockId());
			}
		}
	}

	private void handleExportResult(ExportResult result, Throwable ex, ExportStartEvent event) {
		if (ex != null || result == null) {
			PopupNotification.show(UIUtils.localize("export.failed_export") + " " + event.getConfig().getName(),
					PopupNotification.Type.ERROR);
			return;
		}
		if (result.alreadyLocked) {
			PopupNotification.show(UIUtils.localize("export.already_running_export") + " " + event.getConfig().getName(),
					PopupNotification.Type.ERROR);
			return;
		}
		if (result.exported == null) {
			PopupNotification.show(UIUtils.localize("export.failed_export") + " " + event.getConfig().getName(),
					PopupNotification.Type.ERROR);
			return;
		}

		showExportDialog(event, result.exported);
	}

	private void showExportDialog(ExportStartEvent event, File exported) {
		String fileName = event.getConfig().uniqueFileName(event.getProperties().getFileExtension());
		Button lnkFile = new Button(fileName);
		lnkFile.setIcon(VaadinIcon.DOWNLOAD.create());
		lnkFile.addClassName("link");

		StreamResource stream = createStreamResource(exported, fileName);
		EnhancedFileDownloader downloader = new EnhancedFileDownloader(stream);
		downloader.setOverrideContentType(true);
		downloader.extend(lnkFile);

		Dialog dialog = new Dialog();
		dialog.add(new Span(UIUtils.localize("export.export_finished")));
		dialog.add(lnkFile);
		dialog.setDraggable(false);
		dialog.open();
	}

	private StreamResource createStreamResource(final File file, String fileName) {
		return new StreamResource(fileName, () -> {
			try {
				return new FileInputStream(file);
			} catch (FileNotFoundException e) {
				return null;
			}
		});
	}

	private static final class ExportResult {
		private final File exported;
		private final boolean alreadyLocked;

		private ExportResult(File exported, boolean alreadyLocked) {
			this.exported = exported;
			this.alreadyLocked = alreadyLocked;
		}

		private static ExportResult success(File exported) {
			return new ExportResult(exported, false);
		}

		private static ExportResult locked() {
			return new ExportResult(null, true);
		}

		private static ExportResult failure() {
			return new ExportResult(null, false);
		}
	}

	private RadioButtonGroup<Order> createOrderRadioButtonGroup() {
		RadioButtonGroup<Order> optionGroup = new RadioButtonGroup<>();
		optionGroup.setItems(Order.values());
		//optionGroup.setItemLabelGenerator(literal -> UIUtils.localize(literal.getI18nKey()));

		return optionGroup;
	}

	private RadioButtonGroup<ExportFileMode> createFileModeRadioButtonGroup() {
		RadioButtonGroup<ExportFileMode> optionGroup = new RadioButtonGroup<>();
		optionGroup.setItems(ExportFileMode.values());
		//optionGroup.setItemLabelGenerator(literal -> UIUtils.localize(literal.getI18nKey()));
		return optionGroup;
	}

	private RadioButtonGroup<ExportFormat> createFormatRadioButtonGroup() {
		RadioButtonGroup<ExportFormat> optionGroup = new RadioButtonGroup<>();
		optionGroup.setItems(ExportFormat.values());
		//optionGroup.setItemLabelGenerator(literal -> literal.toString());

		return optionGroup;
	}

	private ComboBox<Interpolation> createInterpolationCombo(final Range<Date> interval, final Device device) {

		ComboBox<Interpolation> combo = new ComboBox<>();
		List<Interpolation> items = new ArrayList<>();

		if (device == null) {
			List<Interpolation> types = UIUtils.getCassandraService().getRollup().availableInterpolations(interval,
					null, null);
			for (Interpolation type : types) {
				if (type.getVirtual() == null) {
					items.add(type);
				}
			}

			if (items.size() == 0) {
				Interpolation type = Interpolation.H1;
				items.add(type);
			}
		} else {

			final Date sinceDate = sinceDate(device);

			List<Interpolation> types = new ArrayList<>();
			types.add(Interpolation.RAW);
			types.add(Interpolation.H1);
			items.addAll(types);

			combo.addValueChangeListener(event -> {
				Interpolation type = event.getValue();
				if (type != null) {
					if (type.equals(Interpolation.RAW)) {
						lowerBound = interval.lowerEndpoint();
						upperBound = interval.upperEndpoint();
					} else {
						Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
						calendar.add(Calendar.MONTH, -3);
						lowerBound = calendar.getTime().after(sinceDate) ? calendar.getTime() : sinceDate;
						upperBound = interval.upperEndpoint();
					}

					TimeIntervalHelper helperLocal = new TimeIntervalHelper(TimeZone.getTimeZone("UTC"));
//					fromDateField.setRangeStart(helperLocal.toLocalDate(lowerBound));
//					fromDateField.setRangeEnd(helperLocal.toLocalDate(upperBound));
//
//					toDateField.setRangeStart(helperLocal.toLocalDate(lowerBound));
//					toDateField.setRangeEnd(helperLocal.toLocalDate(upperBound));

					fromDateField.setValue(helperLocal.toLocalDate(lowerBound));
					toDateField.setValue(helperLocal.toLocalDate(upperBound));
				}
			});

		}

		combo.setItems(items);
//		combo.setItemLabelGenerator(type -> UIUtils.localize(type.getI18nKey()));
//		combo.setEmptySelectionAllowed(false);
		if (!items.isEmpty()) {
			combo.setValue(items.get(0));
		}
		return combo;
	}

	private Date sinceDate(Device device) {
		Date date = device.getActivationDate() != null ? device.getActivationDate() : device.getProductionDate();
		Date sinceDate = UIUtils.getCassandraService().getFeeds().getSince(device.getSerial());
		return sinceDate != null ? sinceDate : date;
	}

	private void enableExport(boolean enabled) {
		this.export.setEnabled(enabled);
	}

	public void resize(float[] dimension) {
//		int width = UI.getCurrent().getPage().getBrowserWindowWidth();
//		int height = UI.getCurrent().getPage().getBrowserWindowHeight();
//		setWidth(width * dimension[0], Unit.PIXELS);
//		setHeight(height * dimension[1], Unit.PIXELS);
	}

	public String getI18nLabel(String key) {
		return UIUtils.localize(getI18nKey() + "." + key);
	}

	public String getI18nKey() {
		return i18nkey;
	}

	public static class ExportFormBean {
		private String columnSeparator;
		private String customSeparator;
		private String decimalSeparator;
		private Interpolation interpolation;
		private ExportFileMode fileMode;
		private ExportFormat format;
		private String exportEmail;
		private Order exportOrder;
		private LocalDate fromDate;
		private LocalDate toDate;
		
		// Getters and setters
		public String getColumnSeparator() { return columnSeparator; }
		public void setColumnSeparator(String columnSeparator) { this.columnSeparator = columnSeparator; }
		
		public String getCustomSeparator() { return customSeparator; }
		public void setCustomSeparator(String customSeparator) { this.customSeparator = customSeparator; }
		
		public String getDecimalSeparator() { return decimalSeparator; }
		public void setDecimalSeparator(String decimalSeparator) { this.decimalSeparator = decimalSeparator; }
		
		public Interpolation getInterpolation() { return interpolation; }
		public void setInterpolation(Interpolation interpolation) { this.interpolation = interpolation; }
		
		public ExportFileMode getFileMode() { return fileMode; }
		public void setFileMode(ExportFileMode fileMode) { this.fileMode = fileMode; }
		
		public ExportFormat getFormat() { return format; }
		public void setFormat(ExportFormat format) { this.format = format; }
		
		public String getExportEmail() { return exportEmail; }
		public void setExportEmail(String exportEmail) { this.exportEmail = exportEmail; }
		
		public Order getExportOrder() { return exportOrder; }
		public void setExportOrder(Order exportOrder) { this.exportOrder = exportOrder; }
		
		public LocalDate getFromDate() { return fromDate; }
		public void setFromDate(LocalDate fromDate) { this.fromDate = fromDate; }
		
		public LocalDate getToDate() { return toDate; }
		public void setToDate(LocalDate toDate) { this.toDate = toDate; }
	}
	
}
