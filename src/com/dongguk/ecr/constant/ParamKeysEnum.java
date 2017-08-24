package com.dongguk.ecr.constant;

/**
 *
 * @author jhun.ahn
 *
 */
public enum ParamKeysEnum {
	CMD("cmd"),

	DEVICE("device"),

	PARAM("params"),
	RETCODE("retCode"),

	MESSAGE("message");

	public String code;

	private ParamKeysEnum(String code) {
		this.code = code;
	}

}
