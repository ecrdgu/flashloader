package com.dongguk.ecr.common.config;

import java.util.HashMap;
import java.util.Set;

import javax.naming.ConfigurationException;

import com.dongguk.ecr.framework.common.config.IProperties;

/**
 * A factory class to create Properties instances.
 * @author jhun.ahn
 *
 */
public class PropertiesFactory {
	private static final HashMap<String, IProperties> mPropMap =
			new HashMap<String, IProperties>();

	public static IProperties createOrget(String propFileName)
			throws ConfigurationException {
		IProperties prop = null;
		synchronized (mPropMap) {
			if (mPropMap.containsKey(propFileName))
				return mPropMap.get(propFileName);
			prop = new DefaultProperties(propFileName);
			mPropMap.put(propFileName, prop);
		}

		return prop;
	}

	public static Set<String> getKeys() {
		return mPropMap.keySet();
	}
}
