package it.thisone.iotter.exporter.filegenerator;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.Calendar;
import java.util.Date;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import it.thisone.iotter.cassandra.model.ExportRow;
/*
 * Feature #167 Export data from visualizations
 */

public class CSVFileBuilder extends FileBuilder {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
    private CSVPrinter printer;
    private CSVFormat format;
    
    private int rowNr;
    private int colNr;

    public CSVFileBuilder(java.util.List<ExportRow> data, java.util.List<String> columnHeaders) {
        super(data, columnHeaders);
        format = CSVFormat.EXCEL;
    }

    public CSVFileBuilder(java.util.List<ExportRow> data) {
        super(data, java.util.Collections.<String>emptyList());
        format = CSVFormat.EXCEL;
    }

    public void setDelimiter(char delimiter) {
    	format = CSVFormat.EXCEL.withDelimiter(delimiter);
    }
    
    
    @Override
    protected void resetContent() {
        try {
            colNr = 0;
            rowNr = 0;
            //Appendable out = new FileWriter(file);
            // http://stackoverflow.com/questions/9852978/write-a-file-in-utf-8-using-filewriter-java
            OutputStreamWriter char_output = new OutputStreamWriter(
            	     new FileOutputStream(file),
            	     Charset.forName("UTF-8").newEncoder()
            	 );
            
            Appendable out = char_output;
            printer = new CSVPrinter(out, format);
        } catch (IOException e) {
            logger.error("resetContent", e);
        }
    }

    @Override
    protected void buildCell(Object value) {
        try {
        	if(value == null){
        		printer.print("");
        	}else if(value instanceof Calendar){
        		Calendar calendar = (Calendar) value;
        		printer.print(formatDate(calendar.getTime()));
        	} else if(value instanceof Date){
        		printer.print(formatDate((Date) value));
        	}else if(value instanceof Number){
        		printer.print(formatNumber((Number) value));
        	} else {
        		printer.print(value.toString().replaceAll(NUMBER_PREFIX, ""));
        	}
        } catch (IOException e) {
            logger.error("buildCell", e);
        }
    }


	@Override
    protected String getFileExtension() {
        return ".csv";
    }

    @Override
    protected void writeToFile() {
        try {
        	printer.flush();
            printer.close();
        } catch (IOException e) {
            logger.error("writeToFile", e);
        }
    }

    @Override
    protected void onNewRow() {
        if (rowNr > 0) {
            try {
                printer.println();
            } catch (IOException e) {
            	logger.error("onNewRow", e);
            }
        }
        rowNr++;
        colNr = 0;
    }

    @Override
    protected void onNewCell() {
        if (colNr > 0 && colNr < getNumberofColumns()) {
        }
        colNr++;
    }

    @Override
    protected void buildColumnHeaderCell(String header) {
        try {
            printer.print(header);
        } catch (IOException e) {
        	logger.error("buildColumnHeaderCell", e);
        }
    }

}
