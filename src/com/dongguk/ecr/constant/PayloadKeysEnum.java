package com.dongguk.ecr.constant;

/**
 *
 * @author jhun.ahn
 *
 */
public enum PayloadKeysEnum {
	NAME("name"),
	DESC("desc"),
	START_ADDRESS("startAddr"),
	SECTION_SIZE("size"),
	READ_ONLY("ro");

	public final String code;

	private PayloadKeysEnum(String code) {
		this.code = code;
	}
}
