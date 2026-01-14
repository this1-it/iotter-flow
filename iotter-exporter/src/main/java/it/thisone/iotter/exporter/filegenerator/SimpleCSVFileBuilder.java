package it.thisone.iotter.exporter.filegenerator;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

import it.thisone.iotter.cassandra.model.ExportRow;

public class SimpleCSVFileBuilder extends FileBuilder {
    private String delimiter = ",";
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
    private FileWriter writer;
    private int rowNr;
    private int colNr;

    public SimpleCSVFileBuilder(java.util.List<ExportRow> data, java.util.List<String> columnHeaders) {
        super(data, columnHeaders);
    }

    public SimpleCSVFileBuilder(java.util.List<ExportRow> data) {
        super(data, java.util.Collections.<String>emptyList());
    }

    public void setDelimiter(char delim) {
    	delimiter = new String( new char[] {delim});
    }
    
    
    @Override
    protected void resetContent() {
        try {
            colNr = 0;
            rowNr = 0;
            writer = new FileWriter(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void buildCell(Object value) {
        try {
        	if(value == null){
        		writer.append("");
        	}else if(value instanceof Calendar){
        		Calendar calendar = (Calendar) value;
        		writer.append(formatDate(calendar.getTime()));
        	}else if(value instanceof Date){
        		writer.append(formatDate((Date) value));
        	}else if(value instanceof Number){
        		writer.append(formatNumber((Number) value));
        	}else {
        		writer.append("\"").append(value.toString()).append("\"");
        	}
        } catch (IOException e) {
            logger.error("buildCell",e);
        }
    }

    @Override
    protected String getFileExtension() {
        return ".csv";
    }

    @Override
    protected void writeToFile() {
        try {
            writer.flush();
            writer.close();
        } catch (IOException e) {
            logger.error("writeToFile",e);
        }
    }

    @Override
    protected void onNewRow() {
        if (rowNr > 0) {
            try {
                writer.append("\n");
            } catch (IOException e) {
                logger.error("onNewRow",e);
            }
        }
        rowNr++;
        colNr = 0;
    }

    @Override
    protected void onNewCell() {
        if (colNr > 0 && colNr < getNumberofColumns()) {
            try {
                writer.append(delimiter);
            } catch (IOException e) {
                logger.error("onNewCell",e);
            }
        }
        colNr++;
    }

    @Override
    protected void buildColumnHeaderCell(String header) {
        try {
            writer.append("\"").append(header).append("\"");
        } catch (IOException e) {
            logger.error("buildColumnHeaderCell",e);
        }
    }

}
