package com.dongguk.ecr.service.config;

import java.io.File;

/**
 * PartitionInformation
 * @author jhun.ahn
 *
 */
public class PartitionInformation {

	private final String name;
	private final String desc;

	private final int startAddr;
	private final int size;
	private final int ro;

	private File file;

	public PartitionInformation(String name, String desc, int startAddr, int size, int ro) {
		this.name = name;
		this.desc = desc;
		this.startAddr = startAddr;
		this.size = size;
		this.ro = ro;
	}

	public String getName() {
		return name;
	}

	public String getDesc() {
		return desc;
	}

	public int getStartAddr() {
		return startAddr;
	}

	public int getSize() {
		return size;
	}

	public int getRo() {
		return ro;
	}

	public void setBinary(String path) {
		this.file = new File(path);
	}
	public void setBinary(File f) {
		this.file = f;
	}

	public File getBinary() {
		return this.file;
	}

	@Override
	public String toString() {
		return "FusingParameter [name=" + name +
				", desc=" + desc +
				", startAddr=" + Integer.toHexString(startAddr) +
				", size=" + Integer.toHexString(size) +
				", ro=" + ro + "]";
	}

}
