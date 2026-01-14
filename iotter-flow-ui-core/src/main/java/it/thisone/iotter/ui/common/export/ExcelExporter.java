package it.thisone.iotter.ui.common.export;

import java.util.List;

import it.thisone.iotter.cassandra.model.ExportRow;
import it.thisone.iotter.exporter.filegenerator.ExcelFileBuilder;
import it.thisone.iotter.exporter.filegenerator.FileBuilder;

public class ExcelExporter extends Exporter {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
    public ExcelExporter() {
        super();
    }

    public ExcelExporter(List<ExportRow> data, List<String> columnHeaders) {
        super(data, columnHeaders);
    }

    @Override
    protected FileBuilder createFileBuilder(List<ExportRow> data, List<String> columnHeaders) {
        return new ExcelFileBuilder(data, columnHeaders);
    }

    @Override
    protected String getDownloadFileName() {
    	if(downloadFileName == null){
    		return "exported-excel.xls";
        }
    	if(downloadFileName.endsWith(".xls")){
    		return downloadFileName;
    	}else{
    		return downloadFileName + ".xls";
    	}
    }
}
