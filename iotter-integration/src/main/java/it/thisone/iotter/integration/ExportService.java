package it.thisone.iotter.integration;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;
import java.util.stream.Collectors;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Range;
import com.google.common.io.BaseEncoding;
import com.vaadin.flow.data.provider.Query;

import it.thisone.iotter.cassandra.CassandraMeasures;
import it.thisone.iotter.cassandra.InterpolationUtils;
import it.thisone.iotter.cassandra.model.CassandraExportContext;
import it.thisone.iotter.cassandra.model.CassandraExportFeed;
import it.thisone.iotter.cassandra.model.IMeasureExporter;
import it.thisone.iotter.cassandra.model.Interpolation;
import it.thisone.iotter.cassandra.model.ExportRow;
import it.thisone.iotter.config.Constants;
import it.thisone.iotter.enums.AccountStatus;
import it.thisone.iotter.enums.ExportFileMode;
import it.thisone.iotter.enums.ExportFormat;
import it.thisone.iotter.enums.Order;
import it.thisone.iotter.exporter.CSVPrinterAdapter;
import it.thisone.iotter.exporter.DataFormat;
import it.thisone.iotter.exporter.ExcelPrinter;
import it.thisone.iotter.exporter.ExportConfig;
import it.thisone.iotter.exporter.ExportGroupConfig;
import it.thisone.iotter.exporter.ExportProperties;
import it.thisone.iotter.exporter.IExportConfig;
import it.thisone.iotter.exporter.IExportProperties;
import it.thisone.iotter.exporter.IExportProvider;
import it.thisone.iotter.exporter.RecordPrinter;
import it.thisone.iotter.exporter.cassandra.CassandraExportDataProvider;
import it.thisone.iotter.exporter.filegenerator.CSVFileBuilder;
import it.thisone.iotter.exporter.filegenerator.ExcelFileBuilder;
import it.thisone.iotter.exporter.filegenerator.FileBuilder;
import it.thisone.iotter.exporter.filegenerator.ZipFileBuilder;
import it.thisone.iotter.integration.ExportMessage.ExportItem;
import it.thisone.iotter.persistence.model.Channel;
import it.thisone.iotter.persistence.model.Device;
import it.thisone.iotter.persistence.model.ExportingConfig;
import it.thisone.iotter.persistence.model.MeasureUnit;
import it.thisone.iotter.persistence.model.NetworkGroup;
import it.thisone.iotter.persistence.model.User;
import it.thisone.iotter.persistence.model.UserCriteria;
import it.thisone.iotter.persistence.service.DeviceService;
import it.thisone.iotter.persistence.service.UserService;
import it.thisone.iotter.security.EncryptionInitializationException;

@Service
public class ExportService implements IExportProvider {
	@Autowired
	private UserService userService;
	
	@Autowired
	private IMeasureExporter exporter;

	@Autowired
	private CassandraMeasures measureService;
	@Autowired
	private DeviceService deviceService;

	public static String dateFormatPattern = "yyyy/MM/dd HH.mm.ss ZZZ";

	 public static Logger logger =
	 LoggerFactory.getLogger(Constants.Exporter.LOG4J_CATEGORY);

//	public static Logger logger = LoggerFactory.getLogger(ExportService.class);

	public CassandraExportFeed createExportFeed(Channel channel, String label) {
		if (channel == null)
			return null;
		String key = channel.getKey();
		String number = channel.getNumber();
		String serial = channel.getDevice().getSerial();
		String device = channel.getDevice().getLabel();
		boolean active = channel.getConfiguration().isActive();
		if (label == null) {
			label = channel.getConfiguration().getLabel();
		}
		MeasureUnit unit = channel.getDefaultMeasure();
		List<Range<Date>> validities = channel.getValidityRanges();
		Float measureScale = unit.getScale();
		Float measureOffset = unit.getOffset();
		String measureFormat = unit.normalizedFormat();
		int measureDecimals = unit.getDecimals();
		return new CassandraExportFeed(number, key, label, serial, device, active, validities, measureScale,
				measureOffset, measureFormat, measureDecimals);
	}

	@Override
	public File createExportDataFile(IExportConfig config, IExportProperties properties) {
		File exported = null;
		config = reconfigure(config, (ExportProperties) properties);
		boolean raw = config.getInterpolation().equals(Interpolation.RAW);
		if (raw && !properties.isLegacy()) {
			exported = exportMeasureSet(config, (ExportProperties) properties);
		} else if (config instanceof ExportGroupConfig) {
			exported = exportDataProviderGroup((ExportGroupConfig) config, (ExportProperties) properties);
		} else if (config instanceof ExportConfig) {
			exported = exportDataProviderFile((ExportConfig) config, (ExportProperties) properties);
		}
		return exported;
	}

	private File exportDataProviderFile(ExportConfig config, ExportProperties properties) {
		File exported = null;
		logger.info("start ExportDataFile {}", config.toString());
		boolean ascending = properties.getOrder().equals(Order.ASCENDING);
		DataFormat dataFormat = new DataFormat(properties.getLocale(), properties.getTimeZone());
		if (properties.getFormat().equals(ExportFormat.CSV)) {
			dataFormat.getDecimalFormatSymbols().setDecimalSeparator(properties.getDecimalSeparator());
		} else {
			dataFormat.setDecimalFormatSymbols(null);
		}

		StringBuilder sb = new StringBuilder();
		sb.append(config.getInterpolation());
		sb.append(properties.getLocale());
		int days = (int) (System.currentTimeMillis() / (1000 * 60 * 60 * 24));
		sb.append(days);
		List<CassandraExportFeed> feeds = new ArrayList<>();
		for (CassandraExportFeed feed : config.getFeeds()) {
			if (feed.isSelected()) {
				feeds.add(feed);
				sb.append(feed.getKey());
			}
		}
		if (feeds.isEmpty()) {
			return exported;
		}

		String digest = digest(sb.toString());
		int batchSize = IMeasureExporter.BATCH_ITEMS;

		CassandraExportDataProvider container = new CassandraExportDataProvider(exporter, feeds, dataFormat, batchSize,
				ascending);
		container.getQueryDefinition().setInterval(config.getInterval());
		container.getQueryDefinition().setInterpolation(config.getInterpolation());
		container.getQueryDefinition().setExporting(true);

		DateFormat dateFormat = new SimpleDateFormat(dateFormatPattern, properties.getLocale());
		dateFormat.setTimeZone(properties.getTimeZone());

		try {
			FileBuilder fileBuilder = null;
			DecimalFormat numberFormat = null;
			List<ExportRow> data = loadExportRows(container, batchSize);
			if (properties.getFormat().equals(ExportFormat.CSV)) {
				fileBuilder = new CSVFileBuilder(data, container.getValueHeaders());
				fileBuilder.setTimestampHeader(container.getTimestampHeader());
				DecimalFormatSymbols symbols = new DecimalFormatSymbols(properties.getLocale());
				symbols.setDecimalSeparator(dataFormat.getDecimalFormatSymbols().getDecimalSeparator());
				numberFormat = new DecimalFormat("##0.#######", symbols);
				numberFormat.setGroupingUsed(false);
				((CSVFileBuilder) fileBuilder).setDelimiter(properties.getColumnSeparator());
			} else {
				fileBuilder = new ExcelFileBuilder(data, container.getValueHeaders());
				fileBuilder.setTimestampHeader(container.getTimestampHeader());
				DecimalFormatSymbols symbols = new DecimalFormatSymbols(properties.getLocale());
				numberFormat = new DecimalFormat("##0.#######", symbols);
				numberFormat.setGroupingUsed(false);
			}
			fileBuilder.setFileName(uniqueExportName(config.getName(),
					interval(config.getInterval(), properties.getTimeZone()), digest));
			fileBuilder.setLocale(properties.getLocale());
			fileBuilder.setDateFormat(dateFormat);
			fileBuilder.setNumberFormat(numberFormat);
			fileBuilder.setBatchSize(batchSize);
			exported = fileBuilder.getFile();
		} catch (Throwable t) {
			exported = null;
			logger.error(config.toString(), t);
		} finally {
//			container.removeAllItems();
//			container.getQueryView().refresh();
		}
		logger.info("end ExportDataFile {}", config.toString());
		return exported;
	}

	private File exportDataProviderGroup(ExportGroupConfig config, ExportProperties properties) {
		List<File> files = new ArrayList<File>();
		List<String> names = new ArrayList<String>();
		if (properties.getFileMode().equals(ExportFileMode.SINGLE)) {
			ExportConfig cfg = new ExportConfig();
			cfg.setName(config.getName());
			cfg.setInterpolation(config.getInterpolation());
			cfg.setInterval(config.getInterval());
			for (ExportConfig chart : config.getExportConfigs()) {
				cfg.getFeeds().addAll(chart.getFeeds());
			}
			File exported = exportDataProviderFile(cfg, properties);
			if (exported != null) {
				files.add(exported);
				String dates = interval(cfg.getInterval(), properties.getTimeZone());
				names.add(String.format("%s %s.%s", cfg.getName(), dates, properties.getFileExtension()));
			}
		} else {
			for (ExportConfig cfg : config.getExportConfigs()) {
				cfg.setInterpolation(config.getInterpolation());
				File exported = exportDataProviderFile(cfg, properties);
				if (exported != null) {
					files.add(exported);
					String dates = interval(cfg.getInterval(), properties.getTimeZone());
					String ext = FilenameUtils.getExtension(exported.getName());
					names.add(String.format("%s %s.%s", cfg.getName(), dates, ext));
				}
			}
		}
		ZipFileBuilder zipBuilder = new ZipFileBuilder(files.toArray(new File[0]), names.toArray(new String[0]));
		String dates = interval(config.getInterval(), properties.getTimeZone());
		config.setName(String.format("%s %s", config.getName(), dates));
		zipBuilder.setFileName(config.uniqueFileName("zip"));
		File exported = zipBuilder.getFile();
		return exported;
	}

	private List<ExportRow> loadExportRows(CassandraExportDataProvider container, int batchSize) {
		List<ExportRow> rows = new ArrayList<>();
		int offset = 0;
		while (true) {
			Query<ExportRow, Date> query = new Query<ExportRow, Date>(offset, batchSize, Collections.emptyList(), null,
					null);
			List<ExportRow> batch = container.fetch(query).collect(Collectors.toList());
			if (batch.isEmpty()) {
				break;
			}
			rows.addAll(batch);
			offset += batch.size();
			if (batch.size() < batchSize) {
				break;
			}
		}
		return rows;
	}

	private String uniqueExportName(String name, String interval, String digest) {
		String label = String.format("%s %s", name, interval);
		label = label.replaceAll("[^a-zA-Z0-9.-]", "_");
		return label + digest;
	}

	private IExportConfig reconfigure(IExportConfig orig, ExportProperties properties) {
		if (!orig.getInterpolation().equals(Interpolation.RAW)) {
			return orig;
		}
		long seconds = (orig.getInterval().upperEndpoint().getTime() - orig.getInterval().lowerEndpoint().getTime())/ 1000;
		if (seconds <= Interpolation.D1.getSeconds()) {
			if (orig instanceof ExportGroupConfig) {
				((ExportProperties) properties).setFileMode(ExportFileMode.MULTI);
				//return orig;
			}
//			ExportGroupConfig config = new ExportGroupConfig();
//			config.setInterval(orig.getInterval());
//			config.setInterpolation(orig.getInterpolation());
//			config.setName(orig.getName());
//			config.getExportConfigs().add((ExportConfig)orig);
//			((ExportProperties) properties).setFileMode(ExportFileMode.MULTI);
			return orig;
			
		}
		return splitExportConfig(orig, properties);
	}

	public ExportGroupConfig splitExportConfig(IExportConfig orig, ExportProperties properties) {
		ExportGroupConfig config = new ExportGroupConfig();
		((ExportProperties) properties).setFileMode(ExportFileMode.MULTI);
		config.setInterpolation(Interpolation.RAW);
		config.setInterval(orig.getInterval());
		config.setName(orig.getName());
		if (orig instanceof ExportGroupConfig) {
			for (ExportConfig cfg : ((ExportGroupConfig) orig).getExportConfigs()) {
				config.getExportConfigs().addAll(splitRawDays(cfg, (ExportProperties) properties));
			}
		} else if (orig instanceof ExportConfig) {
			config.getExportConfigs().addAll(splitRawDays((ExportConfig) orig, (ExportProperties) properties));
		}
		return config;
	}

	private List<ExportConfig> splitRawDays(ExportConfig orig, ExportProperties properties) {
		List<ExportConfig> list = new ArrayList<>();
		Date lower = orig.getInterval().lowerEndpoint();
		Date upper = orig.getInterval().upperEndpoint();
		Calendar calendar = Calendar.getInstance(properties.getTimeZone());
		calendar.setTime(lower);
		//logger.debug("splitRawDays {}", orig.toString());
		while (calendar.getTime().before(upper)) {
			ExportConfig cfg = new ExportConfig();
			cfg.setFeeds(orig.getFeeds());
			cfg.setInterpolation(Interpolation.RAW);
			Date start = calendar.getTime();
			calendar.set(Calendar.HOUR_OF_DAY, 23);
			calendar.set(Calendar.MINUTE, 59);
			calendar.set(Calendar.SECOND, 59);
			calendar.set(Calendar.MILLISECOND, 0);
			Date end = calendar.getTime();
			if (end.after(upper)) {
				end = upper;
			}
			calendar.add(Calendar.SECOND, 1);
			Range<Date> range = Range.closedOpen(start, end);
			cfg.setInterval(range);
			cfg.setName(orig.getName());
			// logger.debug("splitted {}", cfg.toString());
			list.add(cfg);
		}

		return list;
	}

	private String interval(Range<Date> range, TimeZone tz) {
		DateFormat dateFormat = new SimpleDateFormat("dd-MM-yy HH.mm.ss");
		dateFormat.setTimeZone(tz);
		Date start = range.lowerEndpoint();
		Date end = range.upperEndpoint();
		return String.format("%s %s ", dateFormat.format(start), dateFormat.format(end));
	}

	private String digest(String key) {
		try {
			byte[] message = key.getBytes("UTF-8");
			MessageDigest md = MessageDigest.getInstance("MD5");
			md.update(message);
			byte[] digest = md.digest(message);
			StringBuilder sb = new StringBuilder(BaseEncoding.base32Hex().encode(digest));
			return sb.reverse().toString();
		} catch (Exception e) {
			throw new EncryptionInitializationException(e);
		}
	}

	// Feature #2162
	public ExportMessage exportData(Device master) throws IOException {
		List<File> attachments = new ArrayList<File>();
		ExportingConfig exportingConfig = master.getExportingConfig();
		if (exportingConfig == null) {
			exportingConfig = new ExportingConfig();
		}
		String columnSeparator = exportingConfig.getColumnSeparator() != null && !exportingConfig.getColumnSeparator().isEmpty() ? exportingConfig.getColumnSeparator() : ";";
		if (exportingConfig.getCustomSeparator() != null && !exportingConfig.getCustomSeparator().isEmpty()) {
			columnSeparator = exportingConfig.getCustomSeparator();
		}
		String decimalSeparator = exportingConfig.getDecimalSeparator() != null && !exportingConfig.getDecimalSeparator().isEmpty()  ? exportingConfig.getDecimalSeparator(): ",";
		TimeZone timeZone = exportingConfig.getTimeZone() != null ? TimeZone.getTimeZone( exportingConfig.getTimeZone()) : TimeZone.getDefault();
		Locale locale = exportingConfig.getLocale() != null ? Locale.forLanguageTag(exportingConfig.getLocale()): Locale.getDefault();
		
		ExportProperties properties = new ExportProperties();
		properties.setTimeZone(timeZone);
		properties.setFileMode(ExportFileMode.MULTI);
		properties.setFormat(ExportFormat.CSV);
		properties.setLocale(locale);
		properties.setColumnSeparator(columnSeparator.charAt(0));
		properties.setDecimalSeparator(decimalSeparator.charAt(0));
		properties.setLegacy(false);

		DecimalFormatSymbols symbols = new DecimalFormatSymbols(properties.getLocale());
		symbols.setDecimalSeparator(properties.getDecimalSeparator());
		DecimalFormat nf = new DecimalFormat("##0.#######", symbols);
		nf.setGroupingUsed(false);

		DateFormat df = new SimpleDateFormat(dateFormatPattern, properties.getLocale());
		df.setTimeZone(properties.getTimeZone());
		
		Range<Date> result = null;
		
		ExportMessage message = new ExportMessage(); 
		message.setNetwork(master.getNetwork().getName());
		message.setOwner(master.getOwner());
		message.setSerial(master.getSerial());
		message.setLocale(locale);
		
		for ( Device slave: deviceService.findSlaves(master)) {
			String serial = slave.getSerial();
			Range<Date> interval = measureService.getMeasuresSetRange(serial);
			if (interval == null) {
				logger.info("data to be exported not available {} ", serial);
				continue;
			}
			
			if (result == null) {
				result = interval;
			}
			else {
				result = result.span(interval);
			}
			
			ExportConfig config = new ExportConfig();
			config.setInterpolation(Interpolation.RAW);
			config.setInterval(interval);
			config.setName(String.format("%s %s", slave.getLabel(), slave.getSerial()));
			for (Channel channel : slave.getChannels()) {
				if (channel.getConfiguration().isActive()) {
					config.getFeeds().add( this.createExportFeed(channel, null));
				}
			}
			ExportGroupConfig grouped = splitExportConfig(config, properties);
			File zipFile = this.exportMeasureSet(grouped, properties);	
			if (zipFile == null) {
				logger.info("exported zip not available {} ", serial);
				continue;
			}
			attachments.add(zipFile);
			message.getSlaves().add(new ExportMessage.ExportItem(slave.getLabel(), interval(interval, timeZone)));
			
		}
		
		if (attachments.isEmpty()) {
			return null;
		}


		message.setNetwork(master.getNetwork().getName());
		message.setOwner(master.getOwner());
		message.setSerial(master.getSerial());
		message.setAttachments(attachments.toArray(new File[0]));
		message.setMaster(new ExportMessage.ExportItem(master.getLabel(), interval(result, timeZone)));
		message.setEmails(subscribers(master));
		
		return message;
	}

	private File exportMeasureSet(IExportConfig orig, ExportProperties properties) {
		ExportGroupConfig grouped = new ExportGroupConfig();
		if (orig instanceof ExportGroupConfig) {
			grouped = (ExportGroupConfig) orig;
		}
		else {
			grouped.setInterval(orig.getInterval());
			grouped.setInterpolation(orig.getInterpolation());
			grouped.setName(orig.getName());
			grouped.getExportConfigs().add((ExportConfig)orig);
			((ExportProperties) properties).setFileMode(ExportFileMode.MULTI);
		}
		
		List<File> files = new ArrayList<File>();
		List<String> names = new ArrayList<String>();
		DecimalFormatSymbols symbols = new DecimalFormatSymbols(properties.getLocale());
		symbols.setDecimalSeparator(properties.getDecimalSeparator());
		DecimalFormat nf = new DecimalFormat("##0.#######", symbols);
		nf.setGroupingUsed(false);
		DateFormat df = new SimpleDateFormat(dateFormatPattern, properties.getLocale());
		df.setTimeZone(properties.getTimeZone());
		StringBuilder sb = new StringBuilder();
		sb.append(grouped.getInterpolation());
		int days = (int) (System.currentTimeMillis() / (1000 * 60 * 60 * 24));
		sb.append(days);
		for (ExportConfig cfg : grouped.getExportConfigs()) {
			cfg.setInterpolation(grouped.getInterpolation());
			CassandraExportContext context = new CassandraExportContext(cfg.getFeeds(), cfg.getInterval(),
					nf, df, properties.getOrder().equals(Order.ASCENDING),
					properties.getFormat().equals(ExportFormat.EXCEL));
			try {
				String fileName = uniqueExportName(cfg.getName(), interval(cfg.getInterval(), properties.getTimeZone()), digest(sb.toString()));
				String extension = properties.getFormat().equals(ExportFormat.CSV) ? "csv" : "xls";
				Path path = Paths.get(System.getProperty("java.io.tmpdir"), String.format("%s.%s", fileName, extension));
				if (path.toFile().exists()) {
					path.toFile().delete();
				}
				File file = path.toFile();
				path = Files.createFile(path);
				RecordPrinter adapter = null;
				if (properties.getFormat().equals(ExportFormat.CSV)) {
					CSVPrinter printer = new CSVPrinter(new FileWriter(file),
							CSVFormat.DEFAULT.withHeader(context.getLabels().toArray(new String[context.getLabels().size()])));
					adapter = new CSVPrinterAdapter(printer);
				} else {
					OutputStream os = new FileOutputStream(file);
					adapter = new ExcelPrinter(os, context.getLabels(), properties.getLocale(), nf);
				}
				measureService.exportMeasuresSet(context, adapter);
				files.add(file);
				String dates = interval(cfg.getInterval(), properties.getTimeZone());
				String ext = FilenameUtils.getExtension(file.getName());
				names.add(String.format("%s %s.%s", cfg.getName(), dates, ext));

			} catch (IOException e) {
				logger.error(grouped.getName(), e);
			}
		}
		
		if (files.isEmpty()) {
			return null;
		}
		

		ZipFileBuilder zipBuilder = new ZipFileBuilder(files.toArray(new File[0]), names.toArray(new String[0]));
		String dates = interval(grouped.getInterval(), properties.getTimeZone());
		grouped.setName(String.format("%s %s", grouped.getName(), dates));
		zipBuilder.setFileName(grouped.uniqueFileName("zip"));
		File exported = zipBuilder.getFile();
		
		// Remove all temporary files after zip creation
		for (File file : files) {
			if (file.exists()) {
				file.delete();
			}
		}
		
		return exported;

	}
	
	

	private String[] subscribers(Device device) {
		Set<String> emails = new HashSet<>();
		if (device.getNetwork() != null) {
			NetworkGroup defaultGroup = device.getNetwork().getDefaultGroup();
			UserCriteria criteria = new UserCriteria();
			criteria.setRole(Constants.ROLE_SUPERUSER);
			criteria.setGroup(defaultGroup.getId());
			criteria.setStatus(AccountStatus.ACTIVE);
			for (User user : userService.search(criteria, 0, 100)) {
				emails.add(user.getEmail());
			}
			emails.add(userService.findByName(device.getOwner()).getEmail());
		}
		NetworkGroup group = deviceService.getDeviceExportGroup(device);
		if (group != null) {
			List<User> users = userService.findByGroup(group);
			for (User user : users) {
				if (user.getAccountStatus().equals(AccountStatus.ACTIVE) ||user.getAccountStatus().equals(AccountStatus.HIDDEN)) {
					emails.add(user.getEmail());
				}
			}
		}
		
		return emails.toArray(new String[0]);
	}
	

}
