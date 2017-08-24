package com.dongguk.ecr.framework.service.proc;

import com.dongguk.ecr.service.config.PartitionInformation;

/**
 * IProcessRunner
 * @author jhun.ahn
 *
 */
public interface IProcessRunner {

	/**
	 *
	 */
	public boolean start();

	/**
	 *
	 */
	public Object execute();

	/**
	 *
	 */
	public Object terminate();

	/**
	 *
	 * @param partition
	 * @return
	 */
	public boolean add(PartitionInformation partition);

	/**
	 *
	 * @param listener
	 */
	public void setResultListener(IProcResultListener listener);


}
