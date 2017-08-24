package com.dongguk.ecr.constant;

/**
 *
 * @author jhun.ahn
 *
 */
public final class PropertiesKeys {
	public static final String COMMENT = "#";
	public static final String BACKLASH = "\\\\";
	public static final String SINGLE_BACKLASH = "\\";

	private static final String SEPARATOR = ".";

	private static final String PREFIX = "ecr";

	private static final String PATH_CATEGORY = PREFIX + SEPARATOR + "path";
	private static final String SERVICE_CATEGORY = PREFIX + SEPARATOR + "service";
	private static final String DEVICE_CATEGORY = PREFIX + SEPARATOR + "device";
	private static final String CONFIG_CATEGORY = PREFIX + SEPARATOR + "config";

	private static final String OCD_CATEGORY = PREFIX + SEPARATOR + "ocd";

	public static String getOCDCategory(String name) {
		return OCD_CATEGORY + SEPARATOR + name;
	}

	public static String getDeviceCategory(String name) {
		return DEVICE_CATEGORY + SEPARATOR + name;
	}

	public static String getConfigCategory(String name) {
		return CONFIG_CATEGORY + SEPARATOR + name;
	}

	public static String getServiceCategory(String name) {
		return SERVICE_CATEGORY + SEPARATOR + name;
	}

	public static String getPathCategory(String name) {
		return PATH_CATEGORY + SEPARATOR + name;
	}
}
