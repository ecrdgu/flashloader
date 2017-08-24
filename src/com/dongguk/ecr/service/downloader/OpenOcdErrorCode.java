package com.dongguk.ecr.service.downloader;

/**
 * OpenOcdErrorCode
 * @author jhun.ahn
 *
 */
public enum OpenOcdErrorCode {
	NO_ERROR("fatal exceptions"),
	FILE_NOT_FOUND("file not found"),
	PROC_INTERRUPTED("process interrupted"),
	FATAL_ERROR("fatal exceptions");

	public final String desc;

	private OpenOcdErrorCode(String desc) {
		this.desc = desc;
	}
}
