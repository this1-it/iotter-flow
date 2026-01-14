package it.thisone.iotter.ui.common.export;

import java.util.List;

import it.thisone.iotter.cassandra.model.ExportRow;
import it.thisone.iotter.exporter.filegenerator.CSVFileBuilder;
import it.thisone.iotter.exporter.filegenerator.FileBuilder;

public class CSVExporter extends Exporter {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public CSVExporter() {
        super();
    }

    public CSVExporter(List<ExportRow> data, List<String> columnHeaders) {
        super(data, columnHeaders);
    }

    public void setDelimiter(char delimiter) {
    	((CSVFileBuilder)fileBuilder).setDelimiter(delimiter);
    }
    
    @Override
    protected FileBuilder createFileBuilder(List<ExportRow> data, List<String> columnHeaders) {
        return new CSVFileBuilder(data, columnHeaders);
    }

    @Override
    protected String getDownloadFileName() {
    	if(downloadFileName == null){
    		return "exported-csv.csv";
        }
    	if(downloadFileName.endsWith(".csv")){
    		return downloadFileName;
    	}else{
    		return downloadFileName + ".csv";
    	}
    }

}
