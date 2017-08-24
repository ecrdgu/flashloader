package com.dongguk.ecr.service.downloader;

import java.io.File;
import java.io.IOException;

import org.apache.commons.exec.CommandLine;

/**
 * CommandBuilder
 * @author jhun.ahn
 *
 */
public class CommandBuilder {
	private static final String cmd = "flash write_image";
	private static final String erase = "erase";
	private static final String exit = "exit";
	private static final char delimiter = ' ';
	private static final char quote = '\'';
	private final CommandLine commandLine;

	private enum OpenOCDOptions {
		CONFIG('f'),
		COMMAND('c'),
		SCRIPT('s');

		public final String cmd;

		private OpenOCDOptions(char c) {
			this.cmd = "" + '-' + c;
		}
	}

	private CommandBuilder(File f){
		commandLine = new CommandLine(f);
	}

	private CommandBuilder(CommandLine cmdLine){
		commandLine = cmdLine;
	}


	public static Constructor begin(File f) {
		return new Constructor(new CommandBuilder(f));
	}

	public static Constructor begin(CommandLine cmdLine) {
		return new Constructor(new CommandBuilder(cmdLine));
	}

	/**
	 * Constructor class of CommandBuilder
	 *
	 * @author jhun.ahn
	 */
	public static final class Constructor {
		private final CommandBuilder THIS;

		private Constructor(CommandBuilder obj) {
			THIS = obj;
		}

		public Constructor addConfig(String path) throws IOException {
			THIS.commandLine.addArgument(OpenOCDOptions.CONFIG.cmd);
			THIS.commandLine.addArgument(path);

			return this;
		}

		public Constructor addConfig(File file) throws IOException {
			return addConfig(file.getCanonicalPath());
		}

		public Constructor addCommand(String... cmds ) throws IOException {
			StringBuffer buf = new StringBuffer();
			THIS.commandLine.addArgument(OpenOCDOptions.COMMAND.cmd);

			buf.append(quote);
			{
				buf.append("init");

				for (String cmd : cmds) {
					buf.append(';');
					buf.append(delimiter);
					buf.append(cmd);
				}
			}
			buf.append(quote);

			THIS.commandLine.addArguments(buf.toString(), false);

			return this;
		}

		public Constructor addBinary(File file, int addr, int size, boolean bExit) throws IOException {
			THIS.commandLine.addArgument(OpenOCDOptions.COMMAND.cmd);

			StringBuffer buf = new StringBuffer();
			buf.append(quote);
			buf.append(cmd); buf.append(delimiter);
			buf.append(erase); buf.append(delimiter);
			buf.append(file.getCanonicalPath().replace("\\", "\\\\"));
			buf.append(delimiter);
			buf.append("0x"); buf.append(Integer.toHexString(addr));
			buf.append(delimiter);
			buf.append("0x"); buf.append(Integer.toHexString(size));

			if (bExit) {
				buf.append(';');
				buf.append(delimiter);
				buf.append(exit);
			}
			buf.append(quote);
			THIS.commandLine.addArguments(buf.toString(), false);

			return this;
		}

		public Constructor addScript(File file) throws IOException {
			THIS.commandLine.addArgument(OpenOCDOptions.SCRIPT.cmd);
			THIS.commandLine.addArgument(file.getCanonicalPath());
			return this;
		}

		public CommandLine end() {
			return THIS.commandLine;
		}
	}
}
