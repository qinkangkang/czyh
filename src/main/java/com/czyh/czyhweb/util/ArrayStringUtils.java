package com.czyh.czyhweb.util;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ArrayStringUtils {

	private static Logger logger = LoggerFactory.getLogger(ArrayStringUtils.class);

	public static final String Separator = ";";

	public static String arrayToString(String[] ids, String separator) {
		if (ArrayUtils.isEmpty(ids)) {
			return StringUtils.EMPTY;
		}
		StringBuilder ret = new StringBuilder();
		for (String id : ids) {
			ret.append(id).append(separator);
		}
		return ret.deleteCharAt(ret.length() - 1).toString();
	}

	public static String[] stringToArray(String ids, String separator) {
		if (StringUtils.isBlank(ids)) {
			return ArrayUtils.EMPTY_STRING_ARRAY;
		}
		String[] ret = StringUtils.split(ids, separator);
		return ret;
	}
}