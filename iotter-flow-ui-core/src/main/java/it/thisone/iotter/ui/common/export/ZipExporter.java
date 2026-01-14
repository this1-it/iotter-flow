package it.thisone.iotter.ui.common.export;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.server.StreamResource;
import com.vaadin.server.StreamResource.StreamSource;
import com.vaadin.flow.component.button.Button;

import it.thisone.iotter.exporter.filegenerator.ZipFileBuilder;

public class ZipExporter extends Button implements StreamSource {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected ZipFileBuilder fileBuilder;
	private EnhancedFileDownloader fileDownloader;
	protected String downloadFileName;
	private static Logger logger = LoggerFactory.getLogger(ZipExporter.class);

	public ZipExporter() {
		fileDownloader = new EnhancedFileDownloader(new StreamResource(this, getDownloadFileName()));
		fileDownloader.extend(this);
		fileBuilder = new ZipFileBuilder();

	}

	protected  String getDownloadFileName() {
    	if(downloadFileName == null){
    		return "exported-zip.zip";
        }
    	if(downloadFileName.endsWith(".zip")){
    		return downloadFileName;
    	}else{
    		return downloadFileName + ".zip";
    	}
	}

	public void setZipEntries (File... entries) {
		fileBuilder = new ZipFileBuilder(entries);
	}
	
	public void setDownloadFileName(String fileName) {
		downloadFileName = fileName;
		((StreamResource) fileDownloader.getFileDownloadResource()).setFilename(getDownloadFileName());
	}

	@Override
	public InputStream getStream() {
		try {
			return new FileInputStream(fileBuilder.getFile());
		} catch (FileNotFoundException e) {
			logger.error(getDownloadFileName(),e);
		}
		return null;
	}

	public EnhancedFileDownloader getFileDownloader() {
		return fileDownloader;
	}
}
