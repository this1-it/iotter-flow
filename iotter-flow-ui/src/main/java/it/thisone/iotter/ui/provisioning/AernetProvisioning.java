package it.thisone.iotter.provisioning;

import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.text.ChoiceFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.UUID;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.POIXMLException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;

import it.thisone.iotter.config.Constants;
import it.thisone.iotter.enums.Priority;
import it.thisone.iotter.enums.modbus.Format;
import it.thisone.iotter.enums.modbus.FunctionCode;
import it.thisone.iotter.enums.modbus.Permission;
import it.thisone.iotter.enums.modbus.Signed;
import it.thisone.iotter.enums.modbus.TypeRead;
import it.thisone.iotter.enums.modbus.TypeVar;
import it.thisone.iotter.persistence.model.MessageBundle;
import it.thisone.iotter.persistence.model.ModbusConfiguration;
import it.thisone.iotter.persistence.model.ModbusProfile;
import it.thisone.iotter.persistence.model.ModbusRegister;
import it.thisone.iotter.persistence.model.ResourceData;
import it.thisone.iotter.util.BacNet;

public class AernetProvisioning implements IProvisioningProvider, AernetXLSXParserConstants {

	private static final Logger logger = LoggerFactory.getLogger(AernetProvisioning.class);
	private static final String CONTROLPANEL_ASCII = "ascii";
	private static final String CONTROLPANEL_RESET = "reset";
	private static final int CONTROLPANEL_ASCII_POS = 16;
	
	@Override
	public List<ModbusProfile> availableProfiles() {
		String[] files = new String[] { //
				// "Template-E5.xlsx", //
				//"Template-AerNet---NRL-pCO5.xlsx", //
				//"Template-AerNet---NRP.xlsx", "Template-AerNet---TBX_01.xlsx", //
				//"Template-AerNet---TBX.xlsx", "Template-AerNet---ANKi.xlsx", //
				//"Template-AerNet---MC_EVO.xlsx" //
		};

		// String[] files = new String[] {
		// "Template-AerNet---NRP.xlsx"
		// };

		List<ModbusProfile> profiles = new ArrayList<ModbusProfile>();
		InputStream is = null;
		for (int i = 0; i < files.length; i++) {
			String resourceName = files[i];
			try {
//				OutputStream output = new FileOutputStream(resourceName+".log");
//				OutputStreamWriter writer = new OutputStreamWriter(output, StandardCharsets.UTF_8);
				ClassPathResource resource = new ClassPathResource(files[i]);
				is = resource.getInputStream();
				ModbusProfile profile = readProfileFromExcel(files[i], is);
				if (profile.getId() != null) {
					ResourceData data = new ResourceData();
					data.setFilename(resourceName);
					data.setData(IOUtils.toByteArray(resource.getInputStream()));
					data.setMimetype("application/vnd.ms-excel");
					data.setOwner(profile.getOwner());
					profile.setData(data);
					profiles.add(profile);
				}
			} catch (IOException e) {
				logger.error(resourceName, e);
			} finally {
				try {
					if (is != null)
						is.close();
				} catch (IOException e) {
				}
			}

		}
		// propertiesFile(profiles);
		return profiles;
	}
	
	@Override
	public ModbusProfile readProfileFromExcel(String resourceName, byte[] source, StringWriter writer) {
		ModbusProfile profile = null;
		ByteArrayInputStream is = null;
		if (writer != null) {
			writer.getBuffer().setLength(0);
		}
		try {
			is = new ByteArrayInputStream(source);
			profile = readProfileFromExcel(resourceName, is);
			validateAdditionalProperties(profile);
			if (profile.getId() != null) {
				ResourceData data = new ResourceData();
				data.setFilename(resourceName);
				data.setData(source);
				data.setMimetype("application/vnd.ms-excel");
				data.setOwner(profile.getOwner());
				profile.setData(data);
			}
		} catch (NullPointerException | IOException e) {
			logger.error("Unable to parse file: " + resourceName, e);
			if (writer != null) {
				writer.append("ERROR Unable to parse file: ").append(resourceName).append('\n');
				writer.append(e.toString()).append('\n');
			}
		}
		finally {
			try {
				if (is != null)
					is.close();
			} catch (IOException e) {
			}
		}
		return profile;
	}

	



	private void validateAdditionalProperties(ModbusProfile profile) {
		List<ModbusRegister> models = new ArrayList<>();
		for (ModbusRegister register : profile.getRegisters()) {
			String sectionId = register.getAdditionalProperties().get(CONTROL_PANEL_NAME);
			if (sectionId != null) {
				if (sectionId.contains(CONTROLPANEL_ASCII)) {
					if (register.getTypeVar().equals(TypeVar.INTEGER)) {
						models.add(register);
					}
					else {
						logger.error(String.format("Model parameter %s has wrong type %s", register.getDisplayName(), register.getTypeVar().toString()));
					}					
				}
				if (sectionId.contains(CONTROLPANEL_RESET)) {
					register.setPermission(Permission.WRITE);
				}
			}
		}
		if (models.size() > 0 && models.size() < CONTROLPANEL_ASCII_POS) {
			logger.error(String.format("Model parameters have wrong size %d", models.size()));
		} else if (models.size() > CONTROLPANEL_ASCII_POS) {
			logger.error(String.format("Model parameters have wrong size %d", models.size()));
		}
		
	}

	public void propertiesFile(List<ModbusProfile> profiles) {
		Properties props = new Properties();
		for (ModbusProfile profile : profiles) {
			for (ModbusRegister register : profile.getRegisters()) {
				String unit = BacNet.lookUp(register.getMeasureUnit());
				if (unit != null) {
					props.put(String.format("%03d", register.getMeasureUnit()), unit);
				}
			}
		}
		props.put(String.format("%03d", 255), "not def");
		try {
			OutputStream output = new FileOutputStream("bacnet.properties");
			OutputStreamWriter writer = new OutputStreamWriter(output, StandardCharsets.UTF_8);
			props.store(writer, null);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public ModbusProfile readProfileFromExcel(String resourceName, InputStream is) throws IOException {
		ModbusProfile profile = new ModbusProfile();
		profile.setOwner("supervisor");
		// Create Workbook instance holding reference to .xlsx file
		XSSFWorkbook workbook = null;

		try {
			workbook = new XSSFWorkbook(is);
		} catch (POIXMLException t) {
			throw new IOException(t);
		}
		
		// Get first/desired sheet from the workbook
		XSSFSheet sheet = workbook.getSheetAt(0);
		profile.setResource(resourceName);

		logger.info(String.format("Processing %s ", resourceName));
		
		try {
			String template = readTemplateName(sheet);
			if (template.isEmpty()) {
				logger.error(String.format("%s Missing template name", resourceName));
				template = resourceName;
			}
			profile.setTemplate(template);

			String revision = readTemplateRevision(sheet);
			if (revision.isEmpty()) {
				logger.error(String.format("%s Missing template revision", resourceName));
				revision = "";
			}
			
			profile.setRevision(revision);
			profile.setConfiguration(readTemplateConfiguration(sheet));
			String profileId = String.format("%s-%s", profile.getRevision(), profile.getTemplate());
			String displayName = String.format("%s-%s", profile.getTemplate(), profile.getRevision());
			
			profile.setId(profileId);		
			profile.setDisplayName(displayName);
			profile.setCreationDate(new Date());
			
			Locale extraLocale = readExtraLocale(sheet);

			// Iterate through each rows one by one
			Iterator<Row> rowIterator = sheet.iterator();
			int cnt = 0;
			int rowId = 0;
			while (rowIterator.hasNext()) {
				Row row = rowIterator.next();
				rowId++;
				List<String> content = new ArrayList<String>();
				for (int i = 0; i < COLS_NUM; i++) {
					content.add(cellToString(row.getCell(i, Row.RETURN_NULL_AND_BLANK)));
				}
				ModbusRegister register = readRegisterTemplate(rowId, content, profileId, cnt, extraLocale);
				if (register != null) {
					profile.addRegister(register);
					cnt++;
				}
			}
		} catch (Throwable e) {
			logger.error(resourceName,e);
		}
		finally {
			try {
				workbook.close();
			} catch (Exception e) {
			}
		}
		return profile;
	}

	private String readTemplateName(XSSFSheet sheet) {
		return readCellContent(sheet, TEMPLATE);
	}

	private String readTemplateRevision(XSSFSheet sheet) {
		return readCellContent(sheet, REVISION);
	}

	private Locale readExtraLocale(XSSFSheet sheet) {
		String str = readCellContent(sheet, EXTRA_LOCALE);
		if (str.trim().length() > 2) {
			str = str.substring(str.length() - 2).toLowerCase();
			if (Arrays.asList(Locale.getISOLanguages()).contains(str)) {
				return new Locale(str);
			}
		}
		return null;
	}

	
	private ModbusConfiguration readTemplateConfiguration(XSSFSheet sheet) {
		ModbusConfiguration configuration = new ModbusConfiguration();
		String speed = String.valueOf(parseInteger(readCellContent(sheet, SPEED), 19200));
		String parity = readCellContent(sheet, PARITY);
		if (!parity.isEmpty()) {
			parity = parity.substring(0, 1).toUpperCase();
		}
		String dataBits = String.valueOf(parseInteger(readCellContent(sheet, DATA_BITS), 8));
		double stopBits = parseDouble(readCellContent(sheet, STOP_BITS), 2d);
		DecimalFormat decimalFormat = (DecimalFormat) NumberFormat.getNumberInstance(Locale.US);
		decimalFormat.applyPattern("#.#");

		configuration.setDataBits(dataBits);
		configuration.setParity(parity);
		configuration.setSpeed(speed);
		configuration.setStopBits(decimalFormat.format(stopBits));
		configuration.setSampleRate(10);
		return configuration;
	}

	private String readCellContent(XSSFSheet sheet, int[] coord) {
		Cell cell = sheet.getRow(coord[0]).getCell(coord[1]);
		return cellToString(cell);
	}

	private String cellToString(Cell cell) {
		String s = "";
		if (cell == null)
			return s;
		// Check the cell type and format accordingly
		NumberFormat format = NumberFormat.getInstance(Locale.US);
		switch (cell.getCellType()) {
		case Cell.CELL_TYPE_NUMERIC:
			s = format.format(cell.getNumericCellValue());
			break;
		case Cell.CELL_TYPE_STRING:
			s = cell.getStringCellValue().trim();
			break;
		}
		return s;
	}

	private ModbusRegister readRegisterTemplate(int rowid,List<String> content, String profileid, int cnt, Locale extraLocale) {

		Integer address = parseInteger(readIndex(content, ADDRESS), null);
		if (address == null) {
			logger.debug(String.format("%s skipped register %s", profileid, StringUtils.join(content.iterator(), ",")));
			return null;
		}

		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < content.size(); i++) {
			sb.append(String.format("[%d] = %s,", i, content.get(i)));
		}

		logger.debug(String.format("%s parsing register %s size: %s content: %s", profileid, address, content.size(),
				sb.toString()));
		TypeRead typeRead = parseTypeRead(rowid, TYPE_READ, content);
		ModbusRegister register = new ModbusRegister();
		//String registerId = UUID.randomUUID().toString();
		String registerId = String.format("%s-%04d", profileid, cnt);

		
		register.setId(registerId);
		register.setActive(true);
		register.setCrucial(false);
		register.setAddress(address);
		register.setTypeRead(typeRead);
		
		register.setTypeVar(parseTypeVar(rowid,TYPE_VAR,content));
		register.setBitmask(readIndex(content, BIT_POSITION));
		register.setDisplayName(readIndex(content, LABEL_EN));

		if (register.getDisplayName().isEmpty()) {
			logger.error(String.format("[%d, %d] missing default label en", rowid, LABEL_EN));
			register.setDisplayName(String.format("%04d", cnt));
		}
		
		register.setDecimalDigits(parseInteger(rowid,  DECIMAL_DIGITS, content, 0));
		register.setDeltaLogging(parseDouble( rowid, DELTA_LOGGING, content,  0d));
		register.setFunctionCode(parseFunctionCode(rowid, FUNCTION_CODE, content));
		register.setMax(parseDouble(rowid, MAXIMUM, content, +32767d));
		register.setMin(parseDouble(rowid, MINIMUM, content, -32768d));
		register.setOffset(parseDouble(rowid, OFFSET,content, 0d));
		register.setPermission(parsePermission(rowid, PERMISSION,content));
		register.setPriority(parsePriority(rowid, PRIORITY,content));
		register.setScaleMultiplier(parseDouble(rowid, SCALE,content, 1d));
		
		register.setFormat(parseFormat(rowid, FORMAT,register.getTypeRead(), content));
		register.setSigned(parseSigned(rowid, SIGNED, register.getTypeRead(), content));
		register.setMeasureUnit(parseMeasureUnit(rowid, UOM, register.getTypeVar(), content));
		

		List<MessageBundle> labels = readLabels(rowid, content, register.getId(), extraLocale);
		List<MessageBundle> enums = readEnums(rowid, content, register.getId(), extraLocale);


		switch (register.getTypeVar()) {
		case ALARM:
			if (register.getPriority() == null) {
				register.setPriority(Priority.LOW);
			}
			register.setMeasureUnit(BacNet.ADIM);
			register.setMin(0d);
			register.setMax(1d);
			break;
		case DIGITAL:
			if (register.getMeasureUnit() == BacNet.NOT_DEF)
				register.setMeasureUnit(BacNet.ADIM);
			register.setMin(0d);
			register.setMax(1d);
			break;
		// case INTEGER:
		// if (register.getMeasureUnit() == BacNet.NOT_DEF)
		// register.setMeasureUnit(BacNet.ADIM);
		// break;

		default:
			break;
		}
		
		String aernetPro = readIndex(content, AERNETPRO);
		if (aernetPro != null && !aernetPro.isEmpty()) {
			register.getAdditionalProperties().put(CONTROL_PANEL_NAME, aernetPro.toLowerCase());
			logger.debug(String.format("[%d, %d] additional property %s value %s", rowid, AERNETPRO, CONTROL_PANEL_NAME, aernetPro));
			String iconSet = readIndex(content, ICONSET);
			if (iconSet != null && !iconSet.isEmpty()) {
				register.getAdditionalProperties().put(ICONSET_NAME, iconSet.toLowerCase());
				logger.debug(String.format("[%d, %d] additional property %s value %s", rowid,ICONSET, ICONSET_NAME, iconSet));
			}
		}
		
		String metaData = ModbusRegister.buildMetadata(cnt, register);
		register.setMetaData(metaData);
		register.setOwner(null);

		if (!labels.isEmpty()) {
			register.getMessages().addAll(labels);
		}
		if (!enums.isEmpty()) {
			register.getMessages().addAll(enums);
		}
		logger.debug(String.format("%s finished register %s", profileid, register.toString()));

		return register;
	}

	private String readIndex(List<String> content, int index) {
		if (index >= content.size())
			return "";
		return content.get(index);
	}

	private List<MessageBundle> readLabels(int address, List<String> content, String code, Locale extra) {
		List<MessageBundle> messages = new ArrayList<MessageBundle>();
		int[] positions = new int[] { LABEL_EN, LABEL_DE, LABEL_IT, LABEL_FR, LABEL_RU, LABEL_XX };
		Locale[] locales = new Locale[] { Locale.ENGLISH, Locale.GERMAN, Locale.ITALIAN, Locale.FRENCH,
				LOCALE_RU, extra };
		List<String> missing = new ArrayList<>();
		int size = positions.length;
		if (extra == null) size --;

		for (int i = 0; i < positions.length; i++) {
			String label = readIndex(content, positions[i]);
			if (label != null && !label.isEmpty() && locales[i] != null) {
				messages.add(new MessageBundle(code, "", label, locales[i]));
			}
			if (label.isEmpty() && locales[i] != null){
				missing.add(locales[i].toString());
			}
		}
		
		if (missing.size() == size) missing.clear();
		
		if (!missing.isEmpty()) {
			logger.warn(String.format("[%d] missing labels %s ", address, StringUtils.join(missing) ));
		}
		return messages;
	}

	private List<MessageBundle> readEnums(int address, List<String> content, String code, Locale extra ) {
		List<MessageBundle> messages = new ArrayList<MessageBundle>();
		int[] positions = new int[] { ENUMERATION_EN, ENUMERATION_DE, ENUMERATION_IT, ENUMERATION_FR, ENUMERATION_RU,
				ENUMERATION_XX };
		Locale[] locales = new Locale[] { Locale.ENGLISH, Locale.GERMAN, Locale.ITALIAN, Locale.FRENCH,
				LOCALE_RU, extra };
		
		List<String> missing = new ArrayList<>();
		int size = positions.length;
		if (extra == null) size --;
		
		for (int i = 0; i < positions.length; i++) {
			String enumString = readIndex(content, positions[i]);
			if (enumString != null && !enumString.trim().isEmpty() && locales[i] != null) {
				logger.debug(String.format("[%d] parsing enum '%s'", address, enumString));
				
				enumString = enumString.replaceAll("<", "");
				enumString = enumString.replaceAll("#", "");
				enumString = enumString.replaceAll("\u2264", "");
				
				try {
					String pattern = enumString;
					
					String[] parts = StringUtils.split(pattern, ";");
					for (int j = 0; j < parts.length; j++) {
						int count = StringUtils.countMatches(parts[j], "=");
						if (count != 1) {
							throw new IllegalArgumentException("multiple '=' found");
						}
					}
					
					pattern = enumString.replaceAll("=", "#");
					pattern = enumString.replaceAll(";", "|");
					ChoiceFormat renderer = new ChoiceFormat(pattern);
					for (int j = 0; j < renderer.getLimits().length; j++) {
						renderer.format(renderer.getLimits()[j]);
					}
					
				} catch (Throwable e) {
					logger.error(String.format("[%d] invalid enum '%s' %s", address, enumString, e.getMessage()));
				}
				messages.add(new MessageBundle(code, Constants.Provisioning.META_ENUM, enumString, locales[i]));
			}
			if (enumString.isEmpty() && locales[i] != null){
				missing.add(locales[i].toString());
			}
		}
		
		if (missing.size() == size) missing.clear();
		
		if (!missing.isEmpty()) {
			logger.warn(String.format("[%d] missing enums %s ", address, StringUtils.join(missing) ));
		}
		
		return messages;
	}

	private int parseMeasureUnit(int address, int index, TypeVar typevar, List<String> content) {
		String s = readIndex(content, index);
		if (s == null)
			s = "";
		int code = BacNet.lookUpCode(s);
		
		switch (typevar) {
		case DIGITAL:
		case ALARM:
			code = BacNet.ADIM;
			break;
		default:
			break;
		}

		
		if (code < 0) {
			code = BacNet.NOT_DEF;
			logger.error(String.format("[%d, %d] unrecognized unit of measure '%s' default to %d NOT_DEF for typevar %s ",
					address, index, s, code, typevar.toString()));
		}

		return code;
	}

	private TypeVar parseTypeVar(int address, int index, List<String> content) {
		String s = readIndex(content, index);
		if (s == null)
			s = "";
		for (TypeVar value : TypeVar.values()) {
			if (value.getDisplayName().equalsIgnoreCase(s)) {
				return value;
			}
		}
		logger.error(
				String.format("[%d, %d] unrecognized typevar '%s' default to %s", address, index, s, TypeVar.ANALOG));
		return TypeVar.ANALOG;
	}

	private TypeRead parseTypeRead(int address, int index, List<String> content) {
		String s = readIndex(content, index);
		if (s == null)
			s = "";
		for (TypeRead value : TypeRead.values()) {
			if (value.getDisplayName().equalsIgnoreCase(s)) {
				return value;
			}
		}
		logger.error(String.format("[%d, %d] unrecognized typeread '%s' default to %s", address, index, s,
				TypeRead.HOLDING));
		return TypeRead.HOLDING;
	}


	private Priority parsePriority(int address, int index, List<String> content) {
		String s = readIndex(content, index);
		if (s == null)
			s = "";
		for (Priority value : Priority.values()) {
			if (value.getDisplayName().equalsIgnoreCase(s)) {
				return value;
			}
		}
		logger.warn(
				String.format("[%d, %d] unrecognized priority '%s' default to %s", address, index, s, Priority.LOW));
		return Priority.LOW;
	}

	private Permission parsePermission(int address, int index, List<String> content) {
		String s = readIndex(content, index);
		if (s == null)
			s = "";
		for (Permission value : Permission.values()) {
			if (value.getDisplayName().equalsIgnoreCase(s)) {
				return value;
			}
		}
		logger.error(String.format("[%d, %d] unrecognized permission '%s' default to %s", address, index, s,
				Permission.READ));
		return Permission.READ;
	}

	private FunctionCode parseFunctionCode(int address, int index, List<String> content) {
		String s = readIndex(content, index);
		if (s == null)
			s = "";
		for (FunctionCode value : FunctionCode.values()) {
			if (value.getDisplayName().equalsIgnoreCase(s) || s.contains(value.getDisplayName())) {
				return value;
			}
		}
		logger.warn(String.format("[%d, %d] unrecognized function code '%s' default to %s", address, index, s,
				FunctionCode.MULTIPLE));
		return FunctionCode.MULTIPLE;
	}


	private Signed parseSigned(int address, int index, TypeRead typeread,List<String> content) {
		switch (typeread) {
		case COIL:
		case DISCRETE_INPUT:
			return null;
		default:
			break;
		}		
		String s = readIndex(content, index);
		if (s == null)
			s = "";
		for (Signed value : Signed.values()) {
			if (value.getDisplayName().equalsIgnoreCase(s)) {
				return value;
			}
		}
		logger.error(String.format("[%d, %d] unrecognized signed '%s' default to %s", address, index, s, Signed.YES));
		return Signed.YES;
	}
	
	private Format parseFormat(int address, int index, TypeRead typeread, List<String> content) {
		switch (typeread) {
		case COIL:
		case DISCRETE_INPUT:
			return null;
		default:
			break;
		}		

		String s = readIndex(content, index);
		if (s == null)
			s = "";
		s = s.replace("bits", "bit");
		for (Format value : Format.values()) {
			if (value.getDisplayName().equalsIgnoreCase(s)) {
				return value;
			}
		}
		logger.error(String.format("[%d, %d] unrecognized format '%s' default to %s", address, index, s, Format.BIT16));
		return Format.BIT16;
	}
	
	

	private Double parseDouble(int address, int index, List<String> content, Double value) {
		String s = readIndex(content, index);
		if (s == null)
			s = "";
		if (s.trim().isEmpty())
			return value;
		Double _value = parseDouble(s, null);
		if (_value != null) return _value;
		logger.error(String.format("[%d, %d] cannot parse double '%s' default to %f", address, index, s, value));
		return value;
	}

	private Integer parseInteger(int address, int index, List<String> content, Integer value) {
		String s = readIndex(content, index);
		if (s == null)
			s = "";
		if (s.trim().isEmpty())
			return value;
		Integer _value = parseInteger(s, null);
		if (_value != null) return _value;
		logger.error(String.format("[%d, %d] cannot parse integer '%s' default to %d", address, index, s, value));
		return value;
	}

	
	private Double parseDouble(String s, Double value) {
		try {
			NumberFormat format = NumberFormat.getInstance(Locale.US);
			return format.parse(s).doubleValue();
		} catch (NumberFormatException | ParseException e) {
		}
		return value;
	}

	private Integer parseInteger(String s, Integer value) {
		try {
			NumberFormat format = NumberFormat.getInstance(Locale.US);
			return format.parse(s).intValue();
		} catch (NumberFormatException | ParseException e) {
		}
		return value;
	}


	
}
