package it.thisone.iotter.util;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class BacNet {
	

	private static Map<String,Integer> map;
	
	public static void main(String[] args) {
		propertiesFile();
	}

	public static int lookUpCode(String unit) {
		if (map == null) {
			initialize();
		}
		for (String key : map.keySet()) {
			if (key.equalsIgnoreCase(unit)) {
				return map.get(key);
			}
		}
		
		return -1;
	}
	
	private static void initialize() {
		map = new HashMap<String, Integer>();
		for (int i = 0; i < 255; i++) {
			String unit = lookUp((short)i);
			if (unit != null) {
				map.put(unit, i);
			}
		}
	}

	public static void propertiesFile() {
		Properties props = new Properties();
		for (int i = 0; i < 255; i++) {
			String unit = lookUp((short)i);
			if (unit != null) {
				props.put(String.format("%03d",i), unit);
			}
		}
		props.put(String.format("%03d",NOT_DEF), "not def");
		try {
			OutputStream output = new FileOutputStream("bacnet.properties");
			OutputStreamWriter writer = new OutputStreamWriter(output, StandardCharsets.UTF_8);
			props.store(writer, null);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	public static final int NOT_DEF = 255;
	public static final int SQUARE_METERS = 0;
	public static final int SQUARE_FEET = 1;
	public static final int MILLIAMPERES = 2;
	public static final int AMPERES = 3;
	public static final int OHMS = 4;
	public static final int VOLTS = 5;
	public static final int KILOVOLTS = 6;
	public static final int MEGAVOLTS = 7;
	public static final int VOLT_AMPERES = 8;
	public static final int KILOVOLT_AMPERES = 9;
	public static final int MEGAVOLT_AMPERES = 10;
	public static final int VOLT_AMPERES_REACTIVE = 11;
	public static final int KILOVOLT_AMPERES_REACTIVE = 12;
	public static final int MEGAVOLT_AMPERES_REACTIVE = 13;
	public static final int DEGREES_PHASE = 14;
	public static final int POWER_FACTOR = 15;
	public static final int JOULES = 16;
	public static final int KILOJOULES = 17;
	public static final int WATT_HOURS = 18;
	public static final int KILOWATT_HOURS = 19;
	public static final int BTUS = 20;
	public static final int THERMS = 21;
	public static final int TON_HOURS = 22;
	public static final int JOULES_PER_KILOGRAM_DRY_AIR = 23;
	public static final int BTUS_PER_POUND_DRY_AIR = 24;
	public static final int CYCLES_PER_HOUR = 25;
	public static final int CYCLES_PER_MINUTE = 26;
	public static final int HERTZ = 27;
	public static final int GRAMS_OF_WATER_PER_KILOGRAM_DRY_AIR = 28;
	public static final int PERCENT_RELATIVE_HUMIDITY = 29;
	public static final int MILLIMETERS = 30;
	public static final int METERS = 31;
	public static final int INCHES = 32;
	public static final int FEET = 33;
	public static final int WATTS_PER_SQUARE_FOOT = 34;
	public static final int WATTS_PER_SQUARE_METER = 35;
	public static final int LUMENS = 36;
	public static final int LUX = 37;
	public static final int FOOT_CANDLES = 38;
	public static final int KILOGRAMS = 39;
	public static final int POUNDS_MASS = 40;
	public static final int TONS = 41;
	public static final int KILOGRAMS_PER_SECOND = 42;
	public static final int KILOGRAMS_PER_MINUTE = 43;
	public static final int KILOGRAMS_PER_HOUR = 44;
	public static final int POUNDS_MASS_PER_MINUTE = 45;
	public static final int POUNDS_MASS_PER_HOUR = 46;
	public static final int WATTS = 47;
	public static final int KILOWATTS = 48;
	public static final int MEGAWATTS = 49;
	public static final int BTUS_PER_HOUR = 50;
	public static final int HORSEPOWER = 51;
	public static final int TONS_REFRIGERATION = 52;
	public static final int PASCALS = 53;
	public static final int KILOPASCALS = 54;
	public static final int BARS = 55;
	public static final int POUNDS_FORCE_PER_SQUARE_INCH = 56;
	public static final int CENTIMETERS_OF_WATER = 57;
	public static final int INCHES_OF_WATER = 58;
	public static final int MILLIMETERS_OF_MERCURY = 59;
	public static final int CENTIMETERS_OF_MERCURY = 60;
	public static final int INCHES_OF_MERCURY = 61;
	public static final int DEGREES_CELSIUS = 62;
	public static final int DEGREES_KELVIN = 63;
	public static final int DEGREES_FAHRENHEIT = 64;
	public static final int DEGREE_DAYS_CELSIUS = 65;
	public static final int DEGREE_DAYS_FAHRENHEIT = 66;
	public static final int YEARS = 67;
	public static final int MONTHS = 68;
	public static final int WEEKS = 69;
	public static final int DAYS = 70;
	public static final int HOURS = 71;
	public static final int MINUTES = 72;
	public static final int SECONDS = 73;
	public static final int METERS_PER_SECOND = 74;
	public static final int KILOMETERS_PER_HOUR = 75;
	public static final int FEET_PER_SECOND = 76;
	public static final int FEET_PER_MINUTE = 77;
	public static final int MILES_PER_HOUR = 78;
	public static final int CUBIC_FEET = 79;
	public static final int CUBIC_METERS = 80;
	public static final int IMPERIAL_GALLONS = 81;
	public static final int LITERS = 82;
	public static final int US_GALLONS = 83;
	public static final int CUBIC_FEET_PER_MINUTE = 84;
	public static final int CUBIC_METERS_PER_SECOND = 85;
	public static final int IMPERIAL_GALLONS_PER_MINUTE = 86;
	public static final int LITERS_PER_SECOND = 87;
	public static final int LITERS_PER_MINUTE = 88;
	public static final int US_GALLONS_PER_MINUTE = 89;
	public static final int DEGREES_ANGULAR = 90;
	public static final int DEGREES_CELSIUS_PER_HOUR = 91;
	public static final int DEGREES_CELSIUS_PER_MINUTE = 92;
	public static final int DEGREES_FAHRENHEIT_PER_HOUR = 93;
	public static final int DEGREES_FAHRENHEIT_PER_MINUTE = 94;
	public static final int ADIM = 95;
	public static final int PARTS_PER_MILLION = 96;
	public static final int PARTS_PER_BILLION = 97;
	public static final int PERCENT = 98;
	public static final int PERCENT_PER_SECOND = 99;
	public static final int PER_MINUTE = 100;
	public static final int PER_SECOND = 101;
	public static final int PSI_PER_DEGREE_FAHRENHEIT = 102;
	public static final int RADIANS = 103;
	public static final int REVOLUTIONS_PER_MINUTE = 104;
	public static final int CURRENCY1 = 105;
	public static final int CURRENCY2 = 106;
	public static final int CURRENCY3 = 107;
	public static final int CURRENCY4 = 108;
	public static final int CURRENCY5 = 109;
	public static final int CURRENCY6 = 110;
	public static final int CURRENCY7 = 111;
	public static final int CURRENCY8 = 112;
	public static final int CURRENCY9 = 113;
	public static final int CURRENCY10 = 114;
	public static final int SQUARE_INCHES = 115;
	public static final int SQUARE_CENTIMETERS = 116;
	public static final int BTUS_PER_POUND = 117;
	public static final int CENTIMETERS = 118;
	public static final int POUNDS_MASS_PER_SECOND = 119;
	public static final int DELTA_DEGREES_FAHRENHEIT = 120;
	public static final int DELTA_DEGREES_KELVIN = 121;
	public static final int KILOHMS = 122;
	public static final int MEGOHMS = 123;
	public static final int MILLIVOLTS = 124;
	public static final int KILOJOULES_PER_KILOGRAM = 125;
	public static final int MEGAJOULES = 126;
	public static final int JOULES_PER_DEGREE_KELVIN = 127;
	public static final int JOULES_PER_KILOGRAM_DEGREE_KELVIN = 128;
	public static final int KILOHERTZ = 129;
	public static final int MEGAHERTZ = 130;
	public static final int PER_HOUR = 131;
	public static final int MILLIWATTS = 132;
	public static final int HECTOPASCALS = 133;
	public static final int MILLIBARS = 134;
	public static final int CUBIC_METERS_PER_HOUR = 135;
	public static final int LITERS_PER_HOUR = 136;
	public static final int KILOWATT_HOURS_PER_SQUARE_METER = 137;
	public static final int KILOWATT_HOURS_PER_SQUARE_FOOT = 138;
	public static final int MEGAJOULES_PER_SQUARE_METER = 139;
	public static final int MEGAJOULES_PER_SQUARE_FOOT = 140;
	public static final int WATTS_PER_SQUARE_METER_DEGREE_KELVIN = 141;
	public static final int CUBIC_FEET_PER_SECOND = 142;
	public static final int PERCENT_OBSCURATION_PER_FOOT = 143;
	public static final int PERCENT_OBSCURATION_PER_METER = 144;
	public static final int MILLIOHMS = 145;
	public static final int MEGAWATT_HOURS = 146;
	public static final int KILO_BTUS = 147;
	public static final int MEGA_BTUS = 148;
	public static final int KILOJOULES_PER_KILOGRAM_DRY_AIR = 149;
	public static final int MEGAJOULES_PER_KILOGRAM_DRY_AIR = 150;
	public static final int KILOJOULES_PER_DEGREE_KELVIN = 151;
	public static final int MEGAJOULES_PER_DEGREE_KELVIN = 152;
	public static final int NEWTONS = 153;
	public static final int GRAMS_PER_SECOND = 154;
	public static final int GRAMS_PER_MINUTE = 155;
	public static final int TONS_PER_HOUR = 156;
	public static final int KILO_BTUS_PER_HOUR = 157;
	public static final int HUNDREDTHS_SECONDS = 158;
	public static final int MILLISECONDS = 159;
	public static final int NEWTON_METERS = 160;
	public static final int MILLIMETERS_PER_SECOND = 161;
	public static final int MILLIMETERS_PER_MINUTE = 162;
	public static final int METERS_PER_MINUTE = 163;
	public static final int METERS_PER_HOUR = 164;
	public static final int CUBIC_METERS_PER_MINUTE = 165;
	public static final int METERS_PER_SECOND_PER_SECOND = 166;
	public static final int AMPERES_PER_METER = 167;
	public static final int AMPERES_PER_SQUARE_METER = 168;
	public static final int AMPERE_SQUARE_METERS = 169;
	public static final int FARADS = 170;
	public static final int HENRYS = 171;
	public static final int OHM_METERS = 172;
	public static final int SIEMENS = 173;
	public static final int SIEMENS_PER_METER = 174;
	public static final int TESLAS = 175;
	public static final int VOLTS_PER_DEGREE_KELVIN = 176;
	public static final int VOLTS_PER_METER = 177;
	public static final int WEBERS = 178;
	public static final int CANDELAS = 179;
	public static final int CANDELAS_PER_SQUARE_METER = 180;
	public static final int DEGREES_KELVIN_PER_HOUR = 181;
	public static final int DEGREES_KELVIN_PER_MINUTE = 182;
	public static final int JOULE_SECONDS = 183;
	public static final int RADIAN_PER_SECOND = 184;
	public static final int SQUARE_METERS_PER_NEWTON = 185;
	public static final int KILOGRAMS_PER_CUBIC_METER = 186;
	public static final int NEWTON_SECONDS = 187;
	public static final int NEWTONS_PER_METER = 188;
	public static final int WATTS_PER_METER_PER_DEGREE_KELVIN = 189;
   	public static final int MICRO_SIEMENS = 190;
   	public static final int CUBIC_FEET_PER_HOUR = 191;
	public static final int US_GALLONS_PER_HOUR = 192;
	public static final int KILOMETERS = 193;
	public static final int MICROMETERS = 194;
	public static final int GRAMS = 195;
	public static final int MILLIGRAMS = 196;
	public static final int MILLILITERS = 197;
	public static final int MILLILITERS_PER_SECOND = 198;
	public static final int DECIBELS = 199;
	public static final int DECIBELS_MILLIVOLT = 200;
	public static final int DECIBELS_VOLT = 201;
	public static final int MILLISIEMENS = 202;
	public static final int WATT_HOURS_REACTIVE = 203;
	public static final int KILOWATT_HOURS_REACTIVE = 204;
	public static final int MEGAWATT_HOURS_REACTIVE = 205;
	public static final int MILLIMETERS_OF_WATER = 206;
   	public static final int PER_MILLE = 207;
   	public static final int GRAMS_PER_GRAM = 208;
	public static final int KILOGRAMS_PER_KILOGRAM = 209;
	public static final int GRAMS_PER_KILOGRAM = 210;
	
	public static String lookUp(int unit){
		String s= new String();
		switch(unit){
        case DEGREES_CELSIUS:
        	s="\u00B0" + "C";
       	 	break;
        case DEGREES_KELVIN:
        	s="\u00B0" + "K";
       	 	break;
        case DEGREES_FAHRENHEIT:
        	s="\u00B0" + "F";
       	 	break;
        case SQUARE_METERS:
        	s="m2";
       	 	break;
        case SQUARE_FEET:
        	s="ft2";
       	 	break;
        case MILLIAMPERES:
        	s="mA";
       	 	break;
        case AMPERES:
        	s="A";
       	 	break;
        case OHMS:
        	s="Ohm";
       	 	break;
        case VOLTS:
        	s="V";
       	 	break;
        case KILOVOLTS:
        	s="kV";
       	 	break;
        case MEGAVOLTS:
        	s="MV";
       	 	break;
        case VOLT_AMPERES:
        	s="VA";
       	 	break;
        case KILOVOLT_AMPERES:
        	s="kVA";
       	 	break;
        case MEGAVOLT_AMPERES:
        	s="MVA";
       	 	break;
        case VOLT_AMPERES_REACTIVE:
        	s="VAR";
       	 	break;
        case KILOVOLT_AMPERES_REACTIVE:
        	s="KVAR";
       	 	break;
        case MEGAVOLT_AMPERES_REACTIVE:
        	s="MVAR";
       	 	break;
        case DEGREES_PHASE:
        	s="Ø°";
       	 	break;
        case POWER_FACTOR:
        	s="PF";
       	 	break;
        case JOULES:
        	s="J";
       	 	break;
        case KILOJOULES:
        	s="kJ";
       	 	break;
        case WATT_HOURS:
        	s="Wh";
       	 	break;
        case KILOWATT_HOURS:
        	s="kWh";
       	 	break;
        case BTUS:
        	s="Btu";
       	 	break;
        case THERMS:
        	s="thm";
       	 	break;
        case TON_HOURS:
        	s="ton"+"\u00B0" + ""+"h";
       	 	break;
        case JOULES_PER_KILOGRAM_DRY_AIR:
        	s="J/Kg";
       	 	break;
        case BTUS_PER_POUND_DRY_AIR:
        	s="Btu/lb";
       	 	break;
        case CYCLES_PER_HOUR:
        	s="c/h";
       	 	break;
        case CYCLES_PER_MINUTE:
        	s="c/min";
       	 	break;
        case HERTZ:
        	s="Hz";
       	 	break;
        case GRAMS_OF_WATER_PER_KILOGRAM_DRY_AIR:
        	s="gH2O/Kg";
       	 	break;
        case PERCENT_RELATIVE_HUMIDITY:
        	s="%RH";
       	 	break;
        case MILLIMETERS:
        	s="mm";
       	 	break;
        case METERS:
        	s="m";
       	 	break;
        case INCHES:
        	s="in";
       	 	break;
        case FEET:
        	s="ft";
       	 	break;
        case WATTS_PER_SQUARE_FOOT:
        	s="W/ft2";
       	 	break;
        case WATTS_PER_SQUARE_METER:
        	s="W/m2";
       	 	break;
        case LUMENS:
        	s="lm";
       	 	break;
        case LUX:
        	s="lx";
       	 	break;
        case FOOT_CANDLES:
        	s="fc";
       	 	break;
        case KILOGRAMS:
        	s="kg";
       	 	break;
        case POUNDS_MASS:
        	s="lb";
       	 	break;
        case TONS:
        	s="t";
       	 	break;
        case KILOGRAMS_PER_SECOND:
        	s="kg/s";
       	 	break;
        case KILOGRAMS_PER_MINUTE:
        	s="kg/min";
       	 	break;
        case KILOGRAMS_PER_HOUR:
        	s="kg/hour";
       	 	break;
        case POUNDS_MASS_PER_MINUTE:
        	s="lb/min";
       	 	break;
        case POUNDS_MASS_PER_HOUR:
        	s="lb/hour";
       	 	break;
        case WATTS:
        	s="W";
       	 	break;
        case KILOWATTS:
        	s="kW";
       	 	break;
        case MEGAWATTS:
        	s="MW";
       	 	break;
        case BTUS_PER_HOUR:
        	s="btu/h";
       	 	break;
        case HORSEPOWER:
        	s="hp";
       	 	break;
        case TONS_REFRIGERATION:
        	s="TR";
       	 	break;
        case PASCALS:
        	s="Pa";
       	 	break;
        case KILOPASCALS:
        	s="kPa";
       	 	break;
        case BARS:
        	s="bar";
       	 	break;
        case POUNDS_FORCE_PER_SQUARE_INCH:
        	s="psi";
       	 	break;
        case CENTIMETERS_OF_WATER:
        	s="cmH2O";
       	 	break;
        case INCHES_OF_WATER:
        	s="inH2O";
       	 	break;
        case MILLIMETERS_OF_MERCURY:
        	s="mmHg";
       	 	break;
        case CENTIMETERS_OF_MERCURY:
        	s="cmHg";
       	 	break;
        case INCHES_OF_MERCURY:
        	s="inHg";
       	 	break;
        case DEGREE_DAYS_CELSIUS:
        	s="DDc";
        	break;
        case DEGREE_DAYS_FAHRENHEIT:
        	s="DDf";
        	break;
        case YEARS:
        	s="y";
        	break;
        case MONTHS:
        	s="months";
        	break;
        case WEEKS:
        	s="wk";
        	break;
        case DAYS:
        	s="d";
        	break;
        case HOURS:
        	s="hr";
        	break;
        case MINUTES:
        	s="min";
        	break;
        case SECONDS:
        	s="s";
        	break;
        case METERS_PER_SECOND :
        	s="m/s";
        	break;
        case KILOMETERS_PER_HOUR:
        	s="Km/h";
        	break;
        case FEET_PER_SECOND:
        	s="ft/s";
        	break;
        case FEET_PER_MINUTE:
        	s="ft/min";
        	break;
        case MILES_PER_HOUR:
        	s="mph";
        	break;
        case CUBIC_FEET:
        	s="ft3";
        	break;
        case CUBIC_METERS:
        	s="m3";
        	break;
        case IMPERIAL_GALLONS:
        	s="ig";
        	break;
        case LITERS:
        	s="l";
        	break;
        case US_GALLONS:
        	s="gal";
        	break;
        case CUBIC_FEET_PER_MINUTE:
        	s="ft3/min";
        	break;
        case CUBIC_METERS_PER_SECOND:
        	s="m3/s";
        	break;
        case IMPERIAL_GALLONS_PER_MINUTE:
        	s="ig/min";
        	break;
        case LITERS_PER_SECOND :
        	s="l/s";
        	break;
        case LITERS_PER_MINUTE:
        	s="l/min";
        	break;
        case US_GALLONS_PER_MINUTE:
        	s="gal/min";
        	break;
        case DEGREES_ANGULAR:
        	s="\u00B0" + "";
        	break;
        case DEGREES_CELSIUS_PER_HOUR :
        	s="\u00B0" + "C"+"/h";
        	break;
        case DEGREES_CELSIUS_PER_MINUTE:
        	s="\u00B0" + "C"+"/min";
        	break;
        case DEGREES_FAHRENHEIT_PER_HOUR :
        	s="\u00B0" + "F"+"/h";
        	break;
        case DEGREES_FAHRENHEIT_PER_MINUTE:
        	s="\u00B0" + "F"+"/min";
        	break;
        case ADIM:
        	s="adim";
        	break;
        case PARTS_PER_MILLION:
        	s="ppm";
        	break;
        case PARTS_PER_BILLION:
        	s="ppb";
        	break;
        case PERCENT:
        	s="%";
        	break;
        case PERCENT_PER_SECOND:
        	s="%/s";
        	break;
        case PER_MINUTE:
        	s="1/m";
        	break;
        case PER_SECOND:
        	s="1/s";
        	break;
        case PSI_PER_DEGREE_FAHRENHEIT :
        	s="psi/"+"\u00B0" + "F";
        	break;
        case RADIANS:
        	s="rad";
        	break;
        case REVOLUTIONS_PER_MINUTE:
        	s="rpm";
        	break;
        case CURRENCY1:
        	s="curr1";
        	break;
        case CURRENCY2:
        	s="curr2";
        	break;
        case CURRENCY3:
        	s="curr3";
        	break;
        case CURRENCY4:
        	s="curr4";
        	break;
        case CURRENCY5:
        	s="curr5";
        	break;
        case CURRENCY6:
        	s="curr6";
        	break;
        case CURRENCY7:
        	s="curr7";
        	break;
        case CURRENCY8:
        	s="curr8";
        	break;
        case CURRENCY9:
        	s="curr9";
        	break;
        case CURRENCY10:
        	s="curr10";
        	break;
        case SQUARE_INCHES:
        	s="in2";
        	break;
        case SQUARE_CENTIMETERS:
        	s="cm2";
        	break;
        case BTUS_PER_POUND :
        	s="btu/lb";
        	break;
        case CENTIMETERS:
        	s="cm";
        	break;
        case POUNDS_MASS_PER_SECOND:
        	s="lb/s";
        	break;
        case DELTA_DEGREES_FAHRENHEIT:
        	s="Δ"+"\u00B0" + "F";
        	break;
        case DELTA_DEGREES_KELVIN:
        	s="Δ"+"K";
        	break;
        case KILOHMS:
        	s="K"+"Ohm";
        	break;
        case MEGOHMS:
        	s="M"+"Ohm";
        	break;
        case MILLIVOLTS:
        	s="mV";
        	break;
        case KILOJOULES_PER_KILOGRAM:
        	s="kJ/kg";
        	break;
        case MEGAJOULES:
        	s="MJ";
        	break;
        case JOULES_PER_DEGREE_KELVIN :
        	s="J/K";
        	break;
        case JOULES_PER_KILOGRAM_DEGREE_KELVIN:
        	s="J/Kg"+"\u00B0" + ""+"K";
        	break;
        case KILOHERTZ:
        	s="kHz";
        	break;
        case MEGAHERTZ:
        	s="MHz";
        	break;
        case PER_HOUR:
        	s="1/h";
        	break;
        case MILLIWATTS:
        	s="mW";
        	break;
        case HECTOPASCALS:
        	s="hPa";
        	break;
        case MILLIBARS:
        	s="mbar";
        	break;
        case CUBIC_METERS_PER_HOUR:
        	s="m3/h";
        	break;
        case LITERS_PER_HOUR:
        	s="l/h";
        	break;
        case KILOWATT_HOURS_PER_SQUARE_METER:
        	s="kW h/m2";
        	break;
        case KILOWATT_HOURS_PER_SQUARE_FOOT:
        	s="kW h/ft2";
        	break;
        case MEGAJOULES_PER_SQUARE_METER:
        	s="MJ/m2";
        	break;
        case MEGAJOULES_PER_SQUARE_FOOT:
        	s="MJ/ft2";
        	break;
        case WATTS_PER_SQUARE_METER_DEGREE_KELVIN:
        	s="W/m"+"\u00B0" + ""+"K";
        	break;
        case CUBIC_FEET_PER_SECOND:
        	s="ft3/s";
        	break;
        case PERCENT_OBSCURATION_PER_FOOT:
        	s="%ft";
        	break;
        case PERCENT_OBSCURATION_PER_METER:
        	s="%m";
        	break;
        case MILLIOHMS:
        	s="m"+"Ohm";
        	break;
        case MEGAWATT_HOURS:
        	s="MW/h";
        	break;
        case KILO_BTUS:
        	s="kBtu";
        	break;
        case MEGA_BTUS:
        	s="MBtu";
        	break;
        case KILOJOULES_PER_KILOGRAM_DRY_AIR:
        	s="kJ/Kg";
        	break;
        case MEGAJOULES_PER_KILOGRAM_DRY_AIR:
        	s="MJ/Kg";
        	break;
        case KILOJOULES_PER_DEGREE_KELVIN:
        	s="kJ/K";
        	break;
        case MEGAJOULES_PER_DEGREE_KELVIN:
        	s="MJ/K";
        	break;
        case NEWTONS:
        	s="N";
        	break;
        case GRAMS_PER_SECOND:
        	s="g/s";
        	break;
        case GRAMS_PER_MINUTE:
        	s="g/min";
        	break;
        case TONS_PER_HOUR:
        	s="t/h";
        	break;
        case KILO_BTUS_PER_HOUR:
        	s="kBtu/h";
        	break;
        case HUNDREDTHS_SECONDS:
        	s="(1/100)*s";
        	break;
        case MILLISECONDS:
        	s="ms";
        	break;
        case NEWTON_METERS:
        	s="N/m";
        	break;
        case MILLIMETERS_PER_SECOND:
        	s="mm/s";
        	break;
        case MILLIMETERS_PER_MINUTE:
        	s="mm/min";
        	break;
        case METERS_PER_MINUTE:
        	s="m/min";
        	break;
        case METERS_PER_HOUR:
        	s="m/h";
        	break;
        case CUBIC_METERS_PER_MINUTE:
        	s="m3/min";
        	break;
        case METERS_PER_SECOND_PER_SECOND:
        	s="m/s2";
        	break;
        case AMPERES_PER_METER :
        	s="A/m";
        	break;
        case AMPERES_PER_SQUARE_METER:
        	s="A/m2";
        	break;
        case AMPERE_SQUARE_METERS:
        	s="A"+"\u00B0" + ""+"m2";
        	break;
        case FARADS :
        	s="F";
        	break;
        case HENRYS:
        	s="H";
        	break;
        case OHM_METERS:
        	s="Ohm"+"\u00B0" + ""+"m";
        	break;
        case SIEMENS:
        	s="S";
        	break;
        case SIEMENS_PER_METER:
        	s="S/m";
        	break;
        case TESLAS:
        	s="T";
        	break;
        case VOLTS_PER_DEGREE_KELVIN:
        	s="V/K";
        	break;
        case VOLTS_PER_METER:
        	s="V/m";
        	break;
        case WEBERS:
        	s="Wb";
        	break;
        case CANDELAS:
        	s="cd";
        	break;
        case CANDELAS_PER_SQUARE_METER :
        	s="cd/m2";
        	break;
        case DEGREES_KELVIN_PER_HOUR:
        	s="K/h";
        	break;
        case DEGREES_KELVIN_PER_MINUTE:
        	s="K/min";
        	break;
        case JOULE_SECONDS :
        	s="J"+"\u00B0" +"s";
        	break;
        case RADIAN_PER_SECOND:
        	s="rad/s";
        	break;
        case SQUARE_METERS_PER_NEWTON:
        	s="m2/N";
        	break;
        case KILOGRAMS_PER_CUBIC_METER:
        	s="Kg/m3";
        	break;
        case NEWTON_SECONDS:
        	s="N"+"\u00B0" + ""+"s";
        	break;
        case NEWTONS_PER_METER:
        	s="N/m";
        	break;
        case WATTS_PER_METER_PER_DEGREE_KELVIN:
        	s="W/m-K";
        	break;
        case MICRO_SIEMENS:
        	s="µ"+"S";
        	break;
        case CUBIC_FEET_PER_HOUR:
        	s="ft3/h";
        	break;
        case US_GALLONS_PER_HOUR:
        	s="gal/h";
        	break;
        case KILOMETERS:
        	s="Km";
        	break;
        case MICROMETERS:
        	s="µ"+"m";
        	break;
        case GRAMS:
        	s="g";
        	break;
        case MILLIGRAMS :
        	s="mg";
        	break;
        case MILLILITERS:
        	s="mm";
        	break;
        case MILLILITERS_PER_SECOND:
        	s="mm/s";
        	break;
        case DECIBELS:
        	s="dB";
        	break;
        case DECIBELS_MILLIVOLT:
        	s="dBmV";
        	break;
        case DECIBELS_VOLT:
        	s="dBV";
        	break;
        case MILLISIEMENS:
        	s="mS";
        	break;
        case WATT_HOURS_REACTIVE:
        	s="Wrh";
        	break;
        case KILOWATT_HOURS_REACTIVE :
        	s="kWrh";
        	break;
        case MEGAWATT_HOURS_REACTIVE:
        	s="MWrh";
        	break;
        case MILLIMETERS_OF_WATER:
        	s="mmH2O";
        	break;
        case PER_MILLE:
        	s="‰";
        	break;
        case GRAMS_PER_GRAM:
        	s="g/g";
        	break;
        case KILOGRAMS_PER_KILOGRAM :
        	s="Kg/Kg";
        	break;
        case GRAMS_PER_KILOGRAM :
        	s="g/Kg";
        	break;
        default:
        	s = null;
        	break;
		}
		return s;
	}
	

}