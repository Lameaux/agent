package utils;

import java.util.Properties;

public class StringUtils {

	public static boolean nullOrEmpty(String value) {
		return value == null || value.trim().isEmpty();
	}	
	
	public static String printProperties(Properties p, String prefix) {
		StringBuffer sb = new StringBuffer();
		for(String propertyName : p.stringPropertyNames()) {
			if (prefix != null && !propertyName.startsWith(prefix)) {
				continue;
			}
			String value = p.getProperty(propertyName);
			sb.append(propertyName).append("=").append(value).append("\r\n");
		}
		return sb.toString();
	}
}
