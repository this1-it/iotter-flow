package it.thisone.iotter.exporter;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.DateFormatConverter;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import it.thisone.iotter.cassandra.model.CassandraExportContext;

import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * A simplified utility similar to CSVPrinter that creates a single-sheet Excel workbook.
 */
public class ExcelPrinter implements Flushable, Closeable, RecordPrinter {


    private final Workbook workbook;
    private final Sheet sheet;
    private int currentRow;
	private CellStyle dateCellStyle;
	private CellStyle numberCellStyle;

    private final OutputStream outputStream; // Optional output stream


    private static final Map<Locale, String> localePatterns = new HashMap<>();

    static {
        localePatterns.put(Locale.ENGLISH, "yyyy/MM/dd HH.mm.ss");
        localePatterns.put(Locale.GERMAN, "dd.MM.yyyy HH:mm:ss");
        localePatterns.put(Locale.FRENCH, "dd/MM/yyyy HH:mm:ss");
        localePatterns.put(new Locale("es", "ES"), "dd/MM/yyyy HH:mm:ss");
        localePatterns.put(Locale.ITALIAN, "dd/MM/yyyy HH:mm:ss");
    }

    
    /**
     * Constructor that accepts an OutputStream.

    /**
     * Constructor that accepts an OutputStream and headers to add as the first row.
     *
     * @param os the output stream to which the workbook can be written
     * @param headers an Iterable of header strings to be added as the first row in the sheet
     * @throws IOException if an I/O error occurs while writing the header row
     */
    public ExcelPrinter(OutputStream os,  Iterable<String> headers, Locale inputLocale,  NumberFormat nf) throws IOException {
        
        this.workbook = new XSSFWorkbook();
        this.sheet = workbook.createSheet("Sheet1");
        this.currentRow = 0;
        this.outputStream = os;
        
		CreationHelper createHelper = workbook.getCreationHelper();
		numberCellStyle = workbook.createCellStyle();
		numberCellStyle.setDataFormat(createHelper.createDataFormat()
				.getFormat(((DecimalFormat) nf).toLocalizedPattern()));

		dateCellStyle = workbook.createCellStyle();
		
        Locale locale = localePatterns.containsKey(inputLocale) ? inputLocale : Locale.ENGLISH;
        String pattern = localePatterns.get(locale);


		dateCellStyle.setDataFormat(createHelper.createDataFormat()
				.getFormat(DateFormatConverter.convert(locale, pattern)));

		
		
        if (headers != null) {
            Row headerRow = sheet.createRow(currentRow++);
            int cellIndex = 0;
            for (String header : headers) {
                Cell cell = headerRow.createCell(cellIndex++);
                cell.setCellValue(header);
            }
        }
    }
    

    /**
     * Appends a row to the Excel sheet using an Iterable of values.
     *
     * @param values an Iterable containing the cell values for the row
     * @throws IOException if an I/O error occurs
     */
    public void printRecord(Object... values) throws IOException {
        Row row = sheet.createRow(currentRow++);
        int cellIndex = 0;
        for (Object value : values) {
            Cell cell = row.createCell(cellIndex++);
            if (value == null) {
            	cell.setCellType(Cell.CELL_TYPE_BLANK);
            } else if (value instanceof Date) {
                cell.setCellValue((Date) value);
                cell.setCellStyle(dateCellStyle);
            } else if (value instanceof Double) {
                cell.setCellValue((Double) value);
                cell.setCellStyle(numberCellStyle);
            }  else {
                // Fallback: convert other types to string
                cell.setCellValue(value.toString());
                cell.setCellType(Cell.CELL_TYPE_STRING);
            }
        }
    }

    /**
     * Flushes the workbook to the internal OutputStream if it was provided.
     *
     * @throws IOException in case of I/O issues
     */
    public void flush() throws IOException {
        if (this.outputStream != null) {
            workbook.write(this.outputStream);
        }
    }

    /**
     * Closes the workbook and the OutputStream if it was provided.
     *
     * @throws IOException in case of I/O issues
     */
    public void close() throws IOException {
        try {
            if (this.outputStream != null) {
                workbook.write(this.outputStream);
            }
        } finally {
            workbook.close();
            if (this.outputStream != null) {
                this.outputStream.close();
            }
        }
    }
}