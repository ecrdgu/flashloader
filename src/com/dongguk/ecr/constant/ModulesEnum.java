package com.dongguk.ecr.constant;

/**
 *
 * @author jhun.ahn
 *
 */
public enum ModulesEnum {
	COMMON(0),
	LOG_MSG(1),
	FUSING(2),
	STATUS(3),
	CONNECTION(4),
	CONFIG(5),
	PROPERTY(6);

	public int code;

	private ModulesEnum(int code) {
		this.code = code;
	}

	public static ModulesEnum get(int code) {
		for(ModulesEnum id : ModulesEnum.values())
			if(code == id.code)
				return id;
		return null;
	}
}
