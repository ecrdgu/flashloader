package com.dongguk.ecr.framework.common.config;

import java.util.Set;

import javax.naming.ConfigurationException;

/**
 *
 * @author jhun.ahn
 *
 */
public interface IProperties {

	/**
	 *
	 * @return
	 */
	public Set<?> getProperties();

	/**
	 *
	 * @return
	 * @throws ConfigurationException
	 */
	public boolean getBoolean(String key) throws ConfigurationException;

	/**
	 *
	 * @return
	 * @throws ConfigurationException
	 */
	public byte getByte(String key) throws ConfigurationException;

	/**
	 *
	 * @return
	 * @throws ConfigurationException
	 */
	public int getInteger(String key) throws ConfigurationException;

	/**
	 *
	 * @return
	 * @throws ConfigurationException
	 */
	public short getShort(String key) throws ConfigurationException;

	/**
	 *
	 * @return
	 * @throws ConfigurationException
	 */
	public long getLong(String key) throws ConfigurationException;

	/**
	 *
	 * @return
	 * @throws ConfigurationException
	 */
	public double getDouble(String key) throws ConfigurationException;

	/**
	 *
	 * @return
	 * @throws ConfigurationException
	 */
	public String getString(String key) throws ConfigurationException;

	/**
	 *
	 * @param key
	 * @param o
	 * @return
	 * @throws ConfigurationException
	 */
	public boolean set(String key, Object o);

	/**
	 *
	 * @throws ConfigurationException
	 */
	public void save() throws ConfigurationException;
}