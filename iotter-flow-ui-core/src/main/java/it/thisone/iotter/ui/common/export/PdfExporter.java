package it.thisone.iotter.ui.common.export;

import java.util.List;

import it.thisone.iotter.cassandra.model.ExportRow;
import it.thisone.iotter.exporter.filegenerator.FileBuilder;
import it.thisone.iotter.exporter.filegenerator.PdfFileBuilder;

public class PdfExporter extends Exporter {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
    public PdfExporter() {
        super();
    }

    public PdfExporter(List<ExportRow> data, List<String> columnHeaders) {
        super(data, columnHeaders);
    }

    @Override
    protected FileBuilder createFileBuilder(List<ExportRow> data, List<String> columnHeaders) {
        return new PdfFileBuilder(data, columnHeaders);
    }

    @Override
    protected String getDownloadFileName() {
    	if(downloadFileName == null){
    		return "exported-pdf.pdf";
        }
    	if(downloadFileName.endsWith(".pdf")){
    		return downloadFileName;
    	}else{
    		return downloadFileName + ".pdf";
    	}
    }

    public void setWithBorder(boolean withBorder) {
        ((PdfFileBuilder) fileBuilder).setWithBorder(withBorder);
    }
    
}
