package it.thisone.iotter.exporter.filegenerator;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.thisone.iotter.cassandra.model.ExportRow;

public abstract class FileBuilder implements Serializable {
	/**
	 * 
	 */
	public static final String NUMBER_PREFIX = "%d";
	public static final String EMPTY_VALUE = "---";
	private static final long serialVersionUID = 1L;
	protected File file;
	public List<ExportRow> data;
	private List<String> columnHeaders = new ArrayList<String>();
	private String timestampHeader = "Timestamp";
	private boolean includeTimestamp = true;
	private String header;
	private Locale locale = Locale.getDefault();
	private NumberFormat numberFormat = DecimalFormat.getInstance();
	private DateFormat dateFormat = DateFormat.getDateInstance();
	protected static Logger logger = LoggerFactory.getLogger(FileBuilder.class);

	private String fileName = "tmp";
	private int batchSize = 100;
	

	public FileBuilder() {
	}

	public FileBuilder(List<ExportRow> data, List<String> columnHeaders) {
		setData(data);
		setColumnHeaders(columnHeaders);
	}

	public void setData(List<ExportRow> data) {
		this.data = data;
	}

	public void setColumnHeaders(List<String> headers) {
		if (headers == null) {
			this.columnHeaders = new ArrayList<String>();
			return;
		}
		this.columnHeaders = new ArrayList<String>(headers);
	}

	public File getFile() {
		try {
			Path path = getFilePath();
			if (path.toFile().exists()) {
				return path.toFile();
			}
			initTempFile();
			resetContent();
			buildFileContent();
			writeToFile();
		} catch (Exception e) {
			logger.error(getFileName(), e);
		}
		return file;
	}

	protected void initTempFile() throws IOException {
		if (file != null) {
			file.delete();
		}
		file = createTempFile();
	}

	protected void buildFileContent() {
		buildHeader();
		buildColumnHeaders();
		buildRows();
		buildFooter();
	}

	protected void resetContent() {

	}

	protected void buildColumnHeaders() {
		if (columnHeaders.isEmpty() && !includeTimestamp) {
			return;
		}
		onHeader();
		if (includeTimestamp) {
			onNewCell();
			buildColumnHeaderCell(timestampHeader);
		}
		for (String header : columnHeaders) {
			onNewCell();
			buildColumnHeaderCell(header);
		}
	}

	protected void onHeader() {
		onNewRow();
	}

	protected void buildColumnHeaderCell(String header) {

	}

	protected void buildHeader() {
		// TODO Auto-generated method stub

	}

	private void buildRows() {
		if (data == null || data.isEmpty()) {
			return;
		}
		List<ExportRow> rows = data;
		int size = rows.size();
		int startIndex = 0;
		int numberOfItems = getBatchSize();
		if (!columnHeaders.isEmpty()) {
			int valuesSize = rows.get(0).getValues().size();
			if (valuesSize != columnHeaders.size()) {
				logger.warn("column header count {} does not match values size {}", columnHeaders.size(), valuesSize);
			}
		}
		while (startIndex < size) {
			int endIndex = Math.min(startIndex + numberOfItems, size);
			for (int i = startIndex; i < endIndex; i++) {
				onNewRow();
				buildRow(rows.get(i));
			}
			startIndex = endIndex;
		}
	}

	private void buildRow(ExportRow row) {
		if (row == null) {
			return;
		}
		if (includeTimestamp) {
			onNewCell();
			buildCell(row.getTimestamp());
		}
		List<Float> values = row.getValues();
		int valueColumns = columnHeaders.isEmpty() ? values.size() : columnHeaders.size();
		for (int i = 0; i < valueColumns; i++) {
			Float value = i < values.size() ? values.get(i) : null;
			onNewCell();
			buildCell(value);
		}
	}

	protected void onNewRow() {

	}

	protected void onNewCell() {

	}

	protected abstract void buildCell(Object value);

	protected void buildFooter() {
		// TODO Auto-generated method stub

	}

	protected abstract String getFileExtension();
	
	protected Path getFilePath() {
		return Paths.get(System.getProperty("java.io.tmpdir"),String.format("%s%s", getFileName(), getFileExtension()) );
	}


	protected File createTempFile() throws IOException {
		//return File.createTempFile(getFileName(), getFileExtension());
		Path path = Files.createFile(getFilePath());
		return path.toFile();
	}

	protected abstract void writeToFile();

	public String getHeader() {
		return header;
	}

	public void setHeader(String header) {
		this.header = header;
	}

	protected int getNumberofColumns() {
		return columnHeaders.size() + (includeTimestamp ? 1 : 0);
	}

	public void setLocale(Locale locale) {
		this.locale = locale;
	}

	public void setDateFormat(DateFormat dateFormat) {
		this.dateFormat = dateFormat;
	}

	protected DateFormat getDateFormat() {
		return dateFormat;
	}

	protected String formatDate(Date date) {
		if (date == null)
			return EMPTY_VALUE;
		return dateFormat.format(date);
	}

	protected String formatNumber(Number value) {
		if (value == null)
			return EMPTY_VALUE;
		return numberFormat.format(value.doubleValue());
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getFileName() {
		return fileName;
	}

	public NumberFormat getNumberFormat() {
		return numberFormat;
	}

	public void setNumberFormat(NumberFormat numberFormat) {
		this.numberFormat = numberFormat;
	}

	public Locale getLocale() {
		return locale;
	}

	public void setTimestampHeader(String timestampHeader) {
		this.timestampHeader = timestampHeader == null ? "Timestamp" : timestampHeader;
	}

	public void setIncludeTimestamp(boolean includeTimestamp) {
		this.includeTimestamp = includeTimestamp;
	}

	public int getBatchSize() {
		return batchSize;
	}


	public void setBatchSize(int batchSize) {
		this.batchSize = batchSize;
	}


}
