package it.polimi.tiw.util;

import java.nio.charset.StandardCharsets;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;


public class StringUtility {

    /**
     * Determines if at least one of the parameters string is null or empty
     * @param strings strings to check
     * @return true if the array contains an empty string or a null value
     */
    public static boolean isNullOrEmpty(String... strings){
        for(String string : strings)
            if(string == null || string.isEmpty()) return true;
        return false;
    }

    /**
     * Convert to UTF-8
     * @param str string to convert
     * @return UTF-8 formatted string
     */
    public static String getUnicode(String str){
        return new String (str.getBytes (StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
    }
    
    public static String sanitizeString(String s) {
		String res = null;
		res = StringUtils.normalizeSpace(s);
		res = StringEscapeUtils.escapeJava(res);
		
		return res;
	}
}