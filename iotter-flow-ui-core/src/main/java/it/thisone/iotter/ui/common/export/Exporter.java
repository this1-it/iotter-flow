package it.thisone.iotter.ui.common.export;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.server.StreamResource;
import com.vaadin.server.StreamResource.StreamSource;
import com.vaadin.flow.component.button.Button;

import it.thisone.iotter.cassandra.model.ExportRow;
import it.thisone.iotter.exporter.filegenerator.FileBuilder;

public abstract class Exporter extends Button implements StreamSource {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected FileBuilder fileBuilder;
	private EnhancedFileDownloader fileDownloader;
	private Locale locale;
	protected String downloadFileName;
	private static Logger logger = LoggerFactory.getLogger(Exporter.class);

	public Exporter() {
		fileDownloader = new EnhancedFileDownloader(new StreamResource(this, getDownloadFileName()));
		fileDownloader.extend(this);
	}

	public Exporter(List<ExportRow> data, List<String> columnHeaders) {
		this();
		setCaption("Exporter");
		setDataToBeExported(data, columnHeaders);
	}

	public void setDataToBeExported(List<ExportRow> data, List<String> columnHeaders) {
		fileBuilder = createFileBuilder(data, columnHeaders);
		if (locale != null) {
			fileBuilder.setLocale(locale);
		}
	}

	public void setColumnHeaders(List<String> headers) {
		fileBuilder.setColumnHeaders(headers);
	}

	public void setHeader(String header) {
		fileBuilder.setHeader(header);
	}

	@Override
	public void setLocale(Locale locale) {
		this.locale = locale;
	}

	public void setTimestampHeader(String header) {
		fileBuilder.setTimestampHeader(header);
	}

	public void setIncludeTimestamp(boolean includeTimestamp) {
		fileBuilder.setIncludeTimestamp(includeTimestamp);
	}

	protected abstract FileBuilder createFileBuilder(List<ExportRow> data, List<String> columnHeaders);

	protected abstract String getDownloadFileName();

	public void setDownloadFileName(String fileName) {
		downloadFileName = fileName;
		((StreamResource) fileDownloader.getFileDownloadResource()).setFilename(getDownloadFileName());
	}

	@Override
	public InputStream getStream() {
		try {
			return new FileInputStream(fileBuilder.getFile());
		} catch (FileNotFoundException e) {
			logger.error(getDownloadFileName(), e);
		}
		return null;
	}

	public EnhancedFileDownloader getFileDownloader() {
		return fileDownloader;
	}
}
