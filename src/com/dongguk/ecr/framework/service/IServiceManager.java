package com.dongguk.ecr.framework.service;

import java.io.PrintStream;
import java.util.Observer;

/**
 * IServiceManager
 * @author jhun.ahn
 *
 */
public interface IServiceManager {


	/**
	 *
	 * @return
	 */
	public boolean initialize();

	/**
	 *
	 * @param id
	 * @param o
	 * @return
	 */
	public boolean addObserver(int id, Observer o);

	/**
	 *
	 * @return
	 */
	public PrintStream getLogger();

	/**
	 *
	 * @param id
	 * @return
	 */
	public Object get(Object id);

	/**
	 *
	 * @param id
	 * @param arg
	 */
	public void sendEvent(int id, Object arg);
}
