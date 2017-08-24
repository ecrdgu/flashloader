package com.dongguk.ecr.common.config;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.util.Properties;
import java.util.Set;

import javax.naming.ConfigurationException;

import com.dongguk.ecr.constant.PropertiesKeys;
import com.dongguk.ecr.framework.common.config.IProperties;
import com.dongguk.ecr.main.SystemTypeLoader;
import com.dongguk.ecr.main.SystemTypeLoader.SystemTypesEnum;

/**
 * DefaultProperties
 *
 * @author jhun.ahn
 *
 */
class DefaultProperties implements IProperties {
	private final Properties prop;
	private final File THIS;
	private static final String SEPARATOR = File.separator;

	/* package */
	DefaultProperties(String propFileName) throws ConfigurationException {
		String path = "";
		try {
			path = SystemTypeLoader.getClassPath() + ".." + SEPARATOR + "config";
			File configFile = new File(path + DefaultProperties.SEPARATOR + propFileName);
			if (!configFile.canRead()) {
				throw new ConfigurationException(
						"failed open configuration file: " + path + DefaultProperties.SEPARATOR + propFileName);
			}

			THIS = configFile;

			this.prop = new Properties();
			Reader reader = preparePropertyFile(configFile);
			prop.load(reader);

			reader.close();
		} catch (FileNotFoundException e) {
			throw new ConfigurationException(
					"failed open configuration file: " + path + DefaultProperties.SEPARATOR + propFileName);
		} catch (IOException e) {
			throw new ConfigurationException(
					"failed read configuration file: " + path + DefaultProperties.SEPARATOR + propFileName);
		}
	}

	@Override
	public Set<?> getProperties() {
		return this.prop.keySet();
	}

	@Override
	public boolean getBoolean(String key) throws ConfigurationException {
		synchronized (prop) {
			String str = this.prop.getProperty(key);
			if (str == null)
				throw new ConfigurationException("failed get property in file: " + key);
			return Boolean.parseBoolean(str);
		}
	}

	@Override
	public byte getByte(String key) throws ConfigurationException {
		int radix;
		synchronized (prop) {
			try {
				String str = this.prop.getProperty(key);
				radix = getRadixOfNumericString(str);
				return Byte.parseByte(excludeRadixPrefix(str, radix), radix);
			} catch (NumberFormatException e) {
				throw new ConfigurationException("failed get property in file: " + key);
			}
		}
	}

	private int getRadixOfNumericString(String numericString) {
		if ("0".equals(numericString)) {
			return 10;
		} else if (numericString.isEmpty()) {
			return 0;
		} else if (numericString.startsWith("0x")) {
			return 16;
		} else if (numericString.startsWith("0")) {
			return 8;
		} else if ((numericString.charAt(0) >= '1' && numericString.charAt(0) <= '9')
				|| (numericString.charAt(0) == '-')) {
			return 10;
		} else {
			throw new RuntimeException("Invalid numeric string: " + numericString);
		}
	}

	private String excludeRadixPrefix(String numericString, int radix) {
		if (numericString.isEmpty())
			return "0";

		switch (radix) {
		case 10:
			return numericString;
		case 16:
			return numericString.substring(2);
		case 8:
			return numericString.substring(1);
		default:
			throw new RuntimeException("Invalid radix: " + radix);
		}
	}

	@Override
	public short getShort(String key) throws ConfigurationException {
		int radix;
		synchronized (prop) {
			try {
				String str = this.prop.getProperty(key);
				radix = getRadixOfNumericString(str);
				return Short.parseShort(excludeRadixPrefix(str, radix), radix);
			} catch (NumberFormatException e) {
				throw new ConfigurationException("failed get property in file: " + key);
			}
		}
	}

	@Override
	public int getInteger(String key) throws ConfigurationException {
		int radix;
		synchronized (prop) {
			try {
				String str = this.prop.getProperty(key);
				radix = getRadixOfNumericString(str);
				return Integer.parseInt(excludeRadixPrefix(str, radix), radix);

			} catch (NumberFormatException e) {
				throw new ConfigurationException("failed get property in file: " + key);
			}
		}
	}

	@Override
	public long getLong(String key) throws ConfigurationException {
		int radix;
		synchronized (prop) {
			try {
				String str = this.prop.getProperty(key);
				radix = getRadixOfNumericString(str);
				return Long.parseLong(excludeRadixPrefix(str, radix), radix);
			} catch (NumberFormatException e) {
				throw new ConfigurationException("failed get property in file: " + key);
			}
		}
	}

	@Override
	public double getDouble(String key) throws ConfigurationException {
		synchronized (prop) {
			try {
				return Double.parseDouble(this.prop.getProperty(key));
			} catch (NumberFormatException e) {
				throw new ConfigurationException("failed get property in file: " + key);
			}
		}
	}

	@Override
	public String getString(String key) throws ConfigurationException {
		synchronized (prop) {
			if (!prop.containsKey(key))
				throw new ConfigurationException("failed get property in file: " + key);

			return this.prop.getProperty(key);
		}
	}

	@Override
	public boolean set(String key, Object o) {
		if (o instanceof String) {
			synchronized (prop) {
				prop.setProperty(key, (String) o);
			}

			return false;
		}

		return true;
	}

	@Override
	public void save() throws ConfigurationException {

		if (SystemTypeLoader.getOS() == SystemTypesEnum.UNSUPPORT)
			return;

		try {
			OutputStream out = new FileOutputStream(THIS) {
				@Override
				public void write(byte[] arg0, int arg1, int arg2) throws IOException {
					String str = new String(arg0, arg1, arg2);
					str = str.replace("\\\\", "\\").replace("\\:", ":");
					super.write(str.getBytes());
				}
			};
			synchronized (prop) {
				prop.store(out, "FILE NAME: " + THIS.getName());
			}

		} catch (FileNotFoundException e) {
			new ConfigurationException(e.getMessage());
		} catch (IOException e) {
			new ConfigurationException(e.getMessage());
		}
	}

	private static Reader preparePropertyFile(File file) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(file));
		StringBuilder result = new StringBuilder();

		String line = null;
		boolean endingBackslash = false;

		while ((line = reader.readLine()) != null) {
			line = line.trim();

			if (line.endsWith(PropertiesKeys.SINGLE_BACKLASH)) {
				endingBackslash = true;
				line = line.substring(0, line.length() - 1).trim();
			} else {
				endingBackslash = false;
			}

			result.append(line.replace(PropertiesKeys.SINGLE_BACKLASH, PropertiesKeys.BACKLASH));
			if (!endingBackslash)
				result.append('\n');
		}

		reader.close();

		if (endingBackslash)
			result.append(PropertiesKeys.BACKLASH);

		return new StringReader(result.toString());
	}
}
