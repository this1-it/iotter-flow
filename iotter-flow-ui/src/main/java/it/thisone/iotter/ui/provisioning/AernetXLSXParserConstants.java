package it.thisone.iotter.provisioning;

import java.util.Arrays;
import java.util.Locale;

import it.thisone.iotter.persistence.model.GraphicFeed;
import it.thisone.iotter.persistence.model.ModbusRegister;

/*
 1 1 Template //
 4 1 Version //

 6 1 Speed //
 7 1 Bit //
 8 1 Parity //
 9 1 Stop Bits //

 14 //

 0 Address //
 1 Label IT //
 2 Label EN //
 3 Label FR //
 4 Label DE //
 5 Label ES //
 6 Label RU //
 7 Label XX //
 8 Type Var //
 9 Type Read //
 10 Format //
 11 Bit Position //
 12 Signed //
 13 Permission //
 14 Function Code//
 15 UoM //
 16 Scale //
 17 Offset //
 18 Decimal digits //
 19 Delta Logging //
 20 Minimum //
 21 Maximum //
 22 Priority//
 23 Enumeration IT //
 24 Enumeration EN //
 25 Enumeration FR //
 26 Enumeration DE //
 27 Enumeration ES //
 28 Enumeration RU //
 29 Enumeration XX //


 */

public interface AernetXLSXParserConstants {
	public static final String CONTROLPANEL = "AernetPro";
	
	public static final String CONTROL_PANEL_NAME = "aernetpro";
	
	public static final String ICONSET_NAME = "iconset";

    public static final Locale LOCALE_RU = new Locale("ru");
	public static final int[] TEMPLATE = new int[] { 1, 2 };
	public static final int[] REVISION = new int[] { 4, 2 };

	public static final int[] SPEED = new int[] { 6, 2 };
	public static final int[] DATA_BITS = new int[] { 7, 2 };
	public static final int[] PARITY = new int[] { 8, 2 };
	public static final int[] STOP_BITS = new int[] { 9, 2 };
	public static final int[] EXTRA_LOCALE = new int[] { 12, 8 };

	
	public static final int START = 14;
	public static final int COLS_NUM = 32;
	
	public static final int ADDRESS = 1;
	public static final int LABEL_IT = 2;
	public static final int LABEL_EN = 3;
	public static final int LABEL_FR = 4;
	public static final int LABEL_DE = 5;
	public static final int LABEL_ES = 6;
	public static final int LABEL_RU = 7;
	public static final int LABEL_XX = 8;
	public static final int TYPE_VAR = 9;
	public static final int TYPE_READ = 10;
	public static final int FORMAT = 11;
	public static final int BIT_POSITION = 12;
	public static final int SIGNED = 13;
	public static final int PERMISSION = 14;
	public static final int FUNCTION_CODE = 15;
	public static final int UOM = 16;
	public static final int SCALE = 17;
	public static final int OFFSET = 18;
	public static final int DECIMAL_DIGITS = 19;
	public static final int DELTA_LOGGING = 20;
	public static final int MINIMUM = 21;
	public static final int MAXIMUM = 22;
	public static final int PRIORITY = 23;
	public static final int AERNETPRO = 24;
	public static final int ICONSET = 25;
	public static final int ENUMERATION_IT = 26;
	public static final int ENUMERATION_EN = 27;
	public static final int ENUMERATION_FR = 28;
	public static final int ENUMERATION_DE = 29;
	public static final int ENUMERATION_ES = 30;
	public static final int ENUMERATION_RU = 31;
	public static final int ENUMERATION_XX = 32;
	

	
}
