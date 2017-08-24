package com.dongguk.ecr.framework.service.proc;

/**
 * The listener interface for termination hook from {@link IProcessRunner}
 * @author jhun.ahn
 *
 */
public interface IProcResultListener {

	/**
	 *
	 * @param o
	 * @param msg
	 */
	public void actionPerformed(Object o, String msg);
}
