package com.dongguk.ecr.common.payload;

/**
 *
 * @author jhun.ahn
 *
 */
public class DelimiterBasedParamSpec {
	protected final String name;
	protected final ParamTypesEnum type;

	public DelimiterBasedParamSpec(String name, ParamTypesEnum type) {
		this.name = name;
		this.type = type;
	}

	public String getName() {
		return name;
	}

	public ParamTypesEnum getType() {
		return type;
	}

}
