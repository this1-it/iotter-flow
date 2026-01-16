package it.thisone.iotter.ui.common.export;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.server.StreamResource;

import it.thisone.iotter.cassandra.model.ExportRow;
import it.thisone.iotter.exporter.filegenerator.FileBuilder;

public abstract class Exporter extends Button {
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
		fileDownloader = new EnhancedFileDownloader(createStreamResource());
		fileDownloader.extend(this);
	}

	public Exporter(List<ExportRow> data, List<String> columnHeaders) {
		this();
		setText("Exporter");
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
		fileDownloader.setFileDownloadResource(createStreamResource());
	}

	
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

	private StreamResource createStreamResource() {
		return new StreamResource(getDownloadFileName(), this::getStream);
	}
}
