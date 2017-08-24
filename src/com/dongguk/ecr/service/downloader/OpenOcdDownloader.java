package com.dongguk.ecr.service.downloader;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecuteResultHandler;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.LogOutputStream;
import org.apache.commons.exec.ProcessDestroyer;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.commons.exec.ShutdownHookProcessDestroyer;

import com.dongguk.ecr.framework.service.IServiceManager;
import com.dongguk.ecr.framework.service.proc.IProcResultListener;
import com.dongguk.ecr.framework.service.proc.IProcessRunner;
import com.dongguk.ecr.service.FlashLoaderService;
import com.dongguk.ecr.service.config.OpenOcdConfiguration;
import com.dongguk.ecr.service.config.PartitionInformation;

public class OpenOcdDownloader implements IProcessRunner {
	private enum Status { START, STOP }
	private final String device;

	private final DefaultExecutor executor;
	private CommandLine cmdLine;

	private final List<PartitionInformation> mCmdList;

	private IProcResultListener listener;

	private static final boolean bPrintCmd = true;

	private final DefaultExecuteResultHandler resultListener = new DefaultExecuteResultHandler() {

		@Override
		public void onProcessComplete(int exitValue) {
			super.onProcessComplete(exitValue);
			onProcessNotifier(exitValue, "Download Complete");
		};

		@Override
		public void onProcessFailed(ExecuteException e) {
			super.onProcessFailed(e);
			onProcessNotifier(e.getExitValue(), e.getMessage());
		};

		private void onProcessNotifier(int retCode, String msg) {
			DownloaderFactory.remove(device);
			if(listener == null) return;

			synchronized (listener) {
				listener.actionPerformed(retCode, msg);
			}
		}
	};

	private final ProcessDestroyer destoryListener = new ShutdownHookProcessDestroyer() {
		@Override
		public boolean remove(Process arg0) {
			setStat(Status.STOP);
			return super.remove(arg0);
		}

		@Override
		public boolean add(Process arg0) {
			setStat(Status.START);
			return super.add(arg0);
		}
	};

	public OpenOcdDownloader(String device) {
		this.device = device;

		mCmdList = new ArrayList<>();

		OpenOcdConfiguration openOcdCfg = OpenOcdConfiguration.getinstance();

		final File openOcd = openOcdCfg.getOpenOcd();
		final File config = openOcdCfg.getDeviceConfig();
		final File script = openOcdCfg.getBasePath();

		if (openOcd == null)
			throw new RuntimeException("openOCD: not exist");

		if (config == null)
			throw new RuntimeException("board config file: not exist");

		if (script == null)
			throw new RuntimeException("script path: not exist");

		try {
			cmdLine = CommandBuilder.begin(openOcd)
					.addConfig(config)
					.addScript(script)
					.addCommand("reset halt")
					.end();
		} catch (IOException e) {
			throw new RuntimeException("failed to cmdLine make");
		}

		executor = new DefaultExecutor();

		executor.setWatchdog(new ExecuteWatchdog(Integer.MAX_VALUE));
		executor.setWorkingDirectory(script);
		executor.setExitValue(0);
		executor.setProcessDestroyer(destoryListener);
		executor.setStreamHandler(new PumpStreamHandler(new LogOutputStream() {

			@Override
			protected void processLine(String arg0, int arg1) {
				final IServiceManager service = FlashLoaderService.getInstance();
				service.getLogger().println(arg0);
			}
		}));

	}

	private Status stat = Status.STOP;

	public Status getStat() {
		return stat;
	}

	public void setStat(Status stat) {
		this.stat = stat;
	}

	@Override
	public boolean start() {
		if (getStat() != Status.STOP)
			return false;

		try {
			Iterator<PartitionInformation>it = null;
			it = mCmdList.iterator();

			synchronized (cmdLine) {
				while(it.hasNext()) {
					PartitionInformation item = it.next();

					try {
						cmdLine = CommandBuilder.begin(cmdLine)
							.addBinary(item.getBinary(), item.getStartAddr(), item.getSize(), !it.hasNext())
							.end();
					} catch (IOException e) {
						return false;
					}

					it.remove();
				}
			}

			if (bPrintCmd)
				printCmdList();

			synchronized (executor) {
				executor.execute(cmdLine, resultListener);
			}

			return true;

		} catch (IOException e) {
			return false;
		}
	}

	private void printCmdList() {
		PrintStream ps = System.out;
		StringBuffer buf = new StringBuffer();

		synchronized (cmdLine) {
			buf.append(cmdLine.getExecutable());
			buf.append(' ');

			for (String str : cmdLine.getArguments()) {
				buf.append('\'' + str + '\'');
				buf.append(' ');
			}
		}

		ps.println(buf.toString());
	}

	@Override
	public Object execute() {
		boolean retCode = this.start();
		if (!retCode)
			return false;

		try {
			resultListener.waitFor();
		} catch (InterruptedException e) {
			return false;
		}

		return resultListener.getExitValue();
	}

	@Override
	public Object terminate() {
		if (getStat() == Status.STOP)
			return true;

		ExecuteWatchdog watchdog = null;
		synchronized (executor) {
			watchdog = executor.getWatchdog();
			watchdog.destroyProcess();
		}

		return true;

	}

	@Override
	public void setResultListener(IProcResultListener listener) {
		this.listener = listener;
	}

	@Override
	public boolean add(PartitionInformation partition) {
		mCmdList.add(partition);
		return true;
	}
}
