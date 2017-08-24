package com.dongguk.ecr.main;

import java.io.File;
import java.text.SimpleDateFormat;

/**
 * SystemTypeLoader
 * @author jhun.ahn
 *
 */
public final class SystemTypeLoader {

	public enum SystemTypesEnum {
		/** Constant for Windows operating system. */
		OS_WINDOWS("windows"),
		/** Constant for Unix operating system. */
		OS_UNIX("linux"),
		/** Constant for SunOS operating system. */
		OS_SOLARIS("sunos"),
		/** Constant for OS X operating system. */
		OS_OSX("osx"),
		/** Constant for FreeBSD operating system. */
		OS_FREEBSD(""),

		UNSUPPORT("UNSUPPORT");

		private final String name;

		private SystemTypesEnum(String name) {
			this.name = name;
		}

		@Override
		public String toString() {
			return this.name;
		}

		public static SystemTypesEnum getType(String type) {
			if (type != null) {
				for (SystemTypesEnum t : values())
					if(type.indexOf(t.name) >= 0)
						return t;
//				if (type.indexOf("mac") >= 0)
//					return SystemTypesEnum.OS_OSX;
				if (type.indexOf("nix") >= 0 || type.indexOf("nux") >= 0 || type.indexOf("aix") > 0)
					return SystemTypesEnum.OS_UNIX;
			}
			return SystemTypesEnum.UNSUPPORT;
		}
	}

	/** Constant for i386 architecture. */
	private static final String ARCH_I386 = "i386";

	/** Constant for x86 architecture. */
	public static final String ARCH_X86 = "x86";

	/** Constant for x86_64 architecture. */
	public static final String ARCH_X86_64 = "x86_64";

	/** Constant for amd64 architecture. */
	private static final String ARCH_AMD64 = "amd64";

	public static SystemTypesEnum getOS() {
		final String os = System.getProperty("os.name").toLowerCase().replace(" ", "");
		return SystemTypesEnum.getType(os);
	}

	public static String getArch() {
		final String arch = System.getProperty("os.arch").toLowerCase().replace(" ", "");
		if (arch.equals(ARCH_I386))
			return ARCH_X86;

		if (arch.equals(ARCH_AMD64))
			return ARCH_X86_64;

		return arch;
	}

	public static String getClassPath() {
		char sep = File.separatorChar;
		if (sep == '\0')
			return null;

		return SystemTypeLoader.class.getResource("" + sep).getPath();
	}


	public static String getSystemLog() {
		StringBuffer buffer = new StringBuffer();
		buffer.append(new SimpleDateFormat("yyyy. MM. dd HH:mm:ss").format(System.currentTimeMillis()));
		buffer.append('\n');
		buffer.append("System type : " + getOS().name + " " + getArch());
		buffer.append('\n');

		return buffer.toString();
	}

}
