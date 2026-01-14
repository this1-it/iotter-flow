package it.thisone.iotter.exporter.filegenerator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ZipFileBuilder implements Serializable {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private File file;
	private File[] entries;
	private String[] names;
	private String fileName = "tmp";
	private static Logger logger = LoggerFactory.getLogger(ZipFileBuilder.class);
	
    public ZipFileBuilder(File[] entries, String[] names ) {
    	this.entries = entries;
    	this.names = names;
    }
	
    public ZipFileBuilder(File... entries) {
    	this.entries = entries;
    	this.names = new String[entries.length];
    	for (int i = 0; i < entries.length; i++) {
    		this.names[i] = entries[i].getName();
		}
    }


    public File getFile() {
        try {
            initTempFile();
            writeToFile();
        } catch (Exception e) {
            logger.error("getFile",e);
        }
        return file;
    }

    private void initTempFile() throws IOException {
        if (file != null) {
            file.delete();
        }
        file = createTempFile();
    }


	protected String getFileExtension() {
		return ".zip";
	}

	public void setFileName(String fileName) {
		this.fileName = fileName.replaceAll(getFileExtension(), "");
	}

	public String getFileName() {
		return fileName;
	}

    protected File createTempFile() throws IOException {
        return File.createTempFile(getFileName(), getFileExtension());
    }

    protected void writeToFile(){
    	if (entries == null) {
    		return;
    	}
    	FileOutputStream fos = null;
    	ZipOutputStream zos = null;
		try {
			fos = new FileOutputStream(file);
			zos = new ZipOutputStream(fos);
			for (int i = 0; i < entries.length; i++) {
				addToZipFile(entries[i],names[i],zos);
			}
		} catch (Exception e) {
            logger.error("writeToFile",e);
		}
		finally {
			try {
				if (zos != null) {
					zos.close();
				}
			} catch (IOException e) {
			}
			try {
				if (fos != null) {
					fos.close();
				}
			} catch (IOException e) {
			}
		}

    }

    
	private void addToZipFile(File file,String name, ZipOutputStream zos) throws FileNotFoundException, IOException {
		FileInputStream fis = new FileInputStream(file);
		

		ZipEntry zipEntry = new ZipEntry(name);
		
		zos.putNextEntry(zipEntry);
		byte[] bytes = new byte[1024];
		int length;
		while ((length = fis.read(bytes)) >= 0) {
			zos.write(bytes, 0, length);
		}
		zos.closeEntry();
		fis.close();
	}
    
    
}
