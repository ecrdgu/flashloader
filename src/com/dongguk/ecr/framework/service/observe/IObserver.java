package com.dongguk.ecr.framework.service.observe;

import java.util.Observer;

/**
 * IObserver
 * @author jhun.ahn
 *
 */
public interface IObserver extends Observer {

	/**
	 *
	 * @param id
	 */
	public boolean connect(int id);

	/**
	 *
	 * @return
	 */
	public int getId();

	/**
	 *
	 * @param o
	 * @return
	 */
	public boolean signal(Object o);
}
