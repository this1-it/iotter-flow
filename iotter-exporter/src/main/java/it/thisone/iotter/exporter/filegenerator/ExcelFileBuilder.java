package it.thisone.iotter.exporter.filegenerator;

import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.DateFormatConverter;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

import it.thisone.iotter.cassandra.model.ExportRow;

public class ExcelFileBuilder extends FileBuilder {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final String DATE_CELL_STYLE = "m/d/yy HH:mm:ss";
	private Workbook workbook;
	private Sheet sheet;
	private int rowNr;
	private int colNr;
	private Row row;
	private Cell cell;
	private CellStyle dateCellStyle;
	private CellStyle numberCellStyle;
	private CellStyle boldStyle;
	

	public ExcelFileBuilder(java.util.List<ExportRow> data, java.util.List<String> columnHeaders) {
		super(data, columnHeaders);
	}

	public ExcelFileBuilder(java.util.List<ExportRow> data) {
		super(data, java.util.Collections.<String>emptyList());
		
	}

	public void setDateCellStyle(String style) {
		CreationHelper createHelper = workbook.getCreationHelper();
		dateCellStyle.setDataFormat(createHelper.createDataFormat().getFormat(style));
	}

	@Override
	public String getFileExtension() {
		return ".xls";
	}

	@Override
	protected void writeToFile() {
		FileOutputStream out = null;
		try {
			out = new FileOutputStream(file);
			workbook.write(out);
			((SXSSFWorkbook) workbook).dispose();
		} catch (IOException e) {
			logger.error("writeToFile", e);
		}
		finally {
			try {
				if (out != null)
					out.close();
			} catch (IOException e) {
			}			
			try {
				workbook.close();
			} catch (IOException e) {
			}			
		}
	}


	@Override
	protected void onNewRow() {
		row = sheet.createRow(rowNr);
		rowNr++;
		colNr = 0;
	}

	@Override
	protected void onNewCell() {
		cell = row.createCell(colNr);
		colNr++;
	}

	@Override
	protected void buildCell(Object value) {
		if (value == null) {
			cell.setCellType(Cell.CELL_TYPE_BLANK);
		} else if (value instanceof Boolean) {
			cell.setCellValue((Boolean) value);
			cell.setCellType(Cell.CELL_TYPE_BOOLEAN);
		} else if (value instanceof Date) {
			cell.setCellValue(formatDate((Date) value));
			cell.setCellType(Cell.CELL_TYPE_STRING);
//			cell.setCellValue((Date) value);
//			cell.setCellStyle(getDateCellStyle());
		} else if (value instanceof Calendar) {
			Calendar calendar = (Calendar) value;
			cell.setCellValue(calendar.getTime());
			cell.setCellType(Cell.CELL_TYPE_STRING);
		} else if (value instanceof Number) {
			cell.setCellValue(((Number) value).doubleValue());
			cell.setCellType(Cell.CELL_TYPE_NUMERIC);
		} else {
			if (value.toString().startsWith(NUMBER_PREFIX)) {
				try {
					String _value = value.toString().replaceAll(NUMBER_PREFIX, "");
					Number number = getNumberFormat().parse(_value);
					cell.setCellType(Cell.CELL_TYPE_NUMERIC);
					cell.setCellValue(number.doubleValue());
					// do not format integers
					if(number.doubleValue() % 1 != 0) {
						cell.setCellStyle(getNumberCellStyle());
					}
				} catch (ParseException e) {
					cell.setCellType(Cell.CELL_TYPE_BLANK);
				}
			} else {
				cell.setCellValue(value.toString());
				cell.setCellType(Cell.CELL_TYPE_STRING);
			}
		}
	}

	@Override
	protected void buildColumnHeaderCell(String header) {
		buildCell(header);
		cell.setCellStyle(getBoldStyle());
	}

	public CellStyle getNumberCellStyle() {
		if (numberCellStyle == null) {
			CreationHelper createHelper = workbook.getCreationHelper();
			numberCellStyle = workbook.createCellStyle();
			numberCellStyle.setDataFormat(createHelper.createDataFormat()
					.getFormat(((DecimalFormat) getNumberFormat()).toLocalizedPattern()));
		}
		return numberCellStyle;
	}

	public CellStyle getDateCellStyle() {
		if (dateCellStyle == null) {
			CreationHelper createHelper = workbook.getCreationHelper();
			dateCellStyle = workbook.createCellStyle();
			dateCellStyle.setDataFormat(createHelper.createDataFormat()
					.getFormat(DateFormatConverter.convert(getLocale(), "yyyy/MM/dd HH:mm:ss")));
		}
		return dateCellStyle;
	}

	private CellStyle getBoldStyle() {
		if (boldStyle == null) {
			Font bold = workbook.createFont();
			bold.setBoldweight(Font.BOLDWEIGHT_BOLD);
			boldStyle = workbook.createCellStyle();
			boldStyle.setFont(bold);
		}
		return boldStyle;
	}

	@Override
	protected void buildHeader() {
		if (getHeader() == null) {
			return;
		}
		onNewRow();
		onNewCell();
		cell.setCellValue(getHeader());
		Font headerFont = workbook.createFont();
		headerFont.setBoldweight(Font.BOLDWEIGHT_BOLD);
		headerFont.setFontHeightInPoints((short) 15);
		CellStyle headerStyle = workbook.createCellStyle();
		headerStyle.setFont(headerFont);
		headerStyle.setAlignment(CellStyle.ALIGN_CENTER);
		cell.setCellStyle(headerStyle);
		sheet.addMergedRegion(new CellRangeAddress(0, 1, 0, getNumberofColumns() - 1));
		onNewRow();
	}

	@Override
	protected void buildFooter() {
		for (int i = 0; i < getNumberofColumns(); i++) {
			sheet.autoSizeColumn(i);
		}
	}

	@Override
	protected void resetContent() {
		SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy HH.mm.ss");
		
		if (getDateFormat() != null) {
			df.setTimeZone(getDateFormat().getTimeZone());
		}
		
		setDateFormat(df);

		
//		workbook = new HSSFWorkbook();
//		sheet = workbook.createSheet();
		
		workbook = new SXSSFWorkbook(null,10);
		((SXSSFWorkbook)workbook).setCompressTempFiles(false);
		sheet = (SXSSFSheet) workbook.createSheet();

		
		colNr = 0;
		rowNr = 0;
		row = null;
		cell = null;
		dateCellStyle = null;
		boldStyle = null;
	}
}
