package it.thisone.iotter.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Properties;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import it.thisone.iotter.config.Constants;

public class Utils implements Constants {

	public static String stackTrace(Throwable throwable) {
		StringWriter errorStackTrace = new StringWriter();
		String message = "";
		try {
			message = throwable.getMessage();
			throwable.printStackTrace(new PrintWriter(errorStackTrace));
			message = errorStackTrace.toString().substring(0, 1024);
			errorStackTrace.close();
		} catch (Throwable e) {
		}
		finally {
		}
		return message;
	}
	
	public static String logStackTrace(Throwable throwable) {
		return stackTrace(throwable).replaceAll(System.lineSeparator(), " ");
	}
	
	
	
	public static Properties loadProperties(Manifest manifest) {
		Properties properties = new Properties();
		Attributes attributes = manifest.getMainAttributes();
        try {
			properties.put(PropertyName.IMPLEMENTATION_TITLE, attributes.getValue(PropertyName.IMPLEMENTATION_TITLE));
			properties.put(PropertyName.IMPLEMENTATION_VERSION, attributes.getValue(PropertyName.IMPLEMENTATION_VERSION));
			properties.put(PropertyName.BUILD_NUMBER, attributes.getValue(PropertyName.BUILD_NUMBER));
			properties.put(PropertyName.BUILD_DATE_TIME, attributes.getValue(PropertyName.BUILD_DATE_TIME));
			properties.put(PropertyName.BUILD_SCM_BRANCH, attributes.getValue(PropertyName.BUILD_SCM_BRANCH));
		} catch (NullPointerException e) {
		}
        return properties;
	}

	public static boolean isTypeAlarm(String metaData) {
		return metaData != null && metaData.toLowerCase().contains(Constants.Provisioning.META_ALARM);
	}

	
	public static String messageBundleId(String metadata) {
		if (metadata != null && metadata.contains("|")) {
			int beginIndex = metadata.lastIndexOf("|");
			if (beginIndex > 0) {
				return metadata.substring(beginIndex+1);
			}
		}
		return null;
	}

	public static boolean isValidEmailAddress(String email) {
		String ePattern = "^([a-zA-Z0-9_\\.\\-+])+@(([a-zA-Z0-9-])+\\.)+([a-zA-Z0-9]{2,4})+$";
		//String ePattern = "^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\])|(([a-zA-Z\\-0-9]+\\.)+[a-zA-Z]{2,}))$";
		java.util.regex.Pattern p = java.util.regex.Pattern.compile(ePattern);
		java.util.regex.Matcher m = p.matcher(email);
		return m.matches();
	}

}
