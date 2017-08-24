package com.dongguk.ecr.constant;

/**
 *
 * @author jhun.ahn
 *
 */
public enum PartitionCmdsEnum {
	/** TODO: append here.. */
	GET ("get"),
	SET ("set");

	public String cmd;

	private PartitionCmdsEnum(String cmd) {
		this.cmd = cmd;
	}

	public static PartitionCmdsEnum get(String cmd) {
		for (PartitionCmdsEnum c : PartitionCmdsEnum.values())
			if (c.cmd.equals(cmd))
				return c;
		return null;
	}
}
