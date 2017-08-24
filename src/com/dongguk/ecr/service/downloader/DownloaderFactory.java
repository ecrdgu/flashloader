package com.dongguk.ecr.service.downloader;

import java.util.HashMap;

import com.dongguk.ecr.framework.service.proc.IProcessRunner;

/**
 * DownloaderFactory
 * @author jhun.ahn
 *
 */
public class DownloaderFactory {
	private static final HashMap<String, IProcessRunner>sInstanceMap =
			new HashMap<String, IProcessRunner>();

	public static IProcessRunner createOrGet(String device) {
		IProcessRunner proc = null;
		synchronized (sInstanceMap) {
			if (sInstanceMap.containsKey(device))
				return sInstanceMap.get(device);

			proc = new OpenOcdDownloader(device);

			sInstanceMap.put(device, proc);
		}

		return proc;
	}

	public static boolean remove(String device) {
		IProcessRunner proc = null;
		synchronized (sInstanceMap) {
			proc = sInstanceMap.get(device);
			if(proc == null)
				return false;

			sInstanceMap.remove(device);

			return true;
		}
	}
}
