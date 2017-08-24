package com.dongguk.ecr.service.option;

import java.io.File;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.naming.ConfigurationException;

import com.dongguk.ecr.common.config.PropertiesFactory;
import com.dongguk.ecr.common.payload.ServiceParams;
import com.dongguk.ecr.constant.EventIds;
import com.dongguk.ecr.constant.ModulesEnum;
import com.dongguk.ecr.constant.ParamKeysEnum;
import com.dongguk.ecr.constant.PropertiesKeys;
import com.dongguk.ecr.framework.common.config.IProperties;
import com.dongguk.ecr.framework.service.IServiceManager;
import com.dongguk.ecr.framework.service.observe.AbstractObserve;
import com.dongguk.ecr.framework.service.proc.IProcResultListener;
import com.dongguk.ecr.framework.service.proc.IProcessRunner;
import com.dongguk.ecr.service.FlashLoaderService;
import com.dongguk.ecr.service.config.PartitionInformation;
import com.dongguk.ecr.service.downloader.DownloaderFactory;
import com.dongguk.ecr.service.status.StatusManager;

/**
 * FusingOptionManager
 * @author jhun.ahn
 *
 */
public class FusingOptionManager extends  AbstractObserve {
	private final IServiceManager service;

	/** <device, Fusing parameters> */

	private final HashMap<String, List<PartitionInformation>>mWorkMap;
	private int elemSize = -1;

	public FusingOptionManager(IServiceManager service) {
		super(ModulesEnum.FUSING.code);
		this.service = service;
		this.mWorkMap = new HashMap<String, List<PartitionInformation>>();

		final IProperties prop;
		try {
			prop = PropertiesFactory.createOrget(FlashLoaderService.APP_CONFIG);
			elemSize = prop.getInteger(
					PropertiesKeys.getServiceCategory(StatusManager.MAX_ELEMENT));

		} catch (ConfigurationException e) {
			System.err.println(e.getMessage());
		}
	}

	@Override
	public Object eventHandler(int id, Object o) {
		PrintStream logger = service.getLogger();
		EventIds cmd = null;
		ServiceParams params = null;

		if (o instanceof ServiceParams) {
			params = (ServiceParams) o;
			cmd = (EventIds) params.getAsIs(ParamKeysEnum.CMD.code);
		} else if (o instanceof EventIds) {
			cmd = (EventIds) o;
		}

		if (cmd == null)
			return null;

		boolean retCode = false;
		switch (cmd)
		{
		case UPDATE:
			return cmd;
		case FUSING_ITEM_ADDED :
			retCode = startParamCheck(params);
			ServiceParams evt = params.duplicate();
			evt.set(ParamKeysEnum.CMD.code, !retCode?
					EventIds.STATUS_ITEM_CLEAR : EventIds.STATUS_ITEM_ADDED);
			service.sendEvent(ModulesEnum.STATUS.code, evt);

			break;

		case FUSING_INIT_MAP:
			retCode = initParamCheck(params);
			break;

		case FUSING_PROCESS_START:
			//TODO : Firmware download start
			retCode = download();
			if (retCode)
				logger.println("start to firmware download");
			break;
		case FUSING_ITEM_CLEAR:
			break;
		case FUSING_PROCESS_STOP:
			//TODO : terminate..
			retCode = true;
			break;
		case FUSING_PROCESS_TERMINATE:
			logger.println("try to process terminate");
			terminate();
			retCode = false;
			break;
		default:
			return null;
		}

		params.set(ParamKeysEnum.RETCODE.code, retCode);
		return params;
	}

	private void terminate() {
		Object m = service.get(ModulesEnum.STATUS.code);
		if (!(m instanceof StatusManager) || mWorkMap.size() == 0)
			return;

		StatusManager sM = (StatusManager) m;
		Iterator<String> itDevice = sM.getDeviceList().iterator();
		while(itDevice.hasNext()) {
			String device = itDevice.next();
			if (!mWorkMap.containsKey(device))
				continue;

			IProcessRunner downloader = DownloaderFactory.createOrGet(device);
			downloader.terminate();
		}
	}

	private boolean download() {
		PrintStream logger = service.getLogger();

		Object m = service.get(ModulesEnum.STATUS.code);
		if (!(m instanceof StatusManager))
			return false;
		if (mWorkMap.size() == 0) {
			logger.println("failed to download. should be add to entry");
			return false;
		}

		StatusManager statusManager = (StatusManager) m;
		Iterator<String> itDevice = statusManager.getDeviceList().iterator();
		while(itDevice.hasNext()) {
			String device = itDevice.next();
			if (!mWorkMap.containsKey(device))
				continue;

			List<PartitionInformation> piList = mWorkMap.get(device);
			Iterator<PartitionInformation> it = piList.iterator();
			IProcessRunner downloader;

			try {
				downloader = DownloaderFactory.createOrGet(device);
			} catch (RuntimeException e) {
				logger.println(e.getMessage());
				return false;
			}

			while(it.hasNext()) {
				if (!downloader.add(it.next()))
					return false;
			}

			/* TODO */
			downloader.setResultListener(new IProcResultListener() {

				@Override
				public void actionPerformed(Object o, String msg) {
					EventIds cmd;

					if (o instanceof Integer) {
						int retCode = (Integer)o;
						cmd = retCode == 0 ? EventIds.STATUS_ITEM_SUCCESS : EventIds.STATUS_ITEM_FAILURE;
						ServiceParams param = new ServiceParams();
						param.set(ParamKeysEnum.CMD.code, cmd);
						service.sendEvent(ModulesEnum.STATUS.code, param);

						if (msg != null)
							service.getLogger().printf("Process exit(%d): %s\n", retCode, msg);
					}

					ServiceParams param = new ServiceParams();
					param.set(ParamKeysEnum.CMD.code, EventIds.FUSING_PROCESS_STOP);
					service.sendEvent(ModulesEnum.FUSING.code, param);
				}
			});
			boolean b = downloader.start();
			if (!b)
				return false;
		}

		return true;
	}

	private boolean startParamCheck(ServiceParams param) {
		PrintStream logger = service.getLogger();
		String name = null;

		if (param == null) {
			logger.println("illegal argument. failed parse argument");
			return false;
		}

		Object o = param.getAsIs(ParamKeysEnum.PARAM.code);
		if (!(o instanceof List<?>)) {
			logger.println("illegal argument. failed parse paramList");
			return false;
		}

		//TODO : get Selected Device
		Object m = service.get(ModulesEnum.STATUS.code);
		if (m instanceof StatusManager) {
			StatusManager sM = (StatusManager) m;
			name = sM.getSelectDevice();
		}

		if (name == null) {
			logger.println("unselected device");
			return false;
		}

		param.set(ParamKeysEnum.DEVICE.code, name);

		/** argument check **/
		@SuppressWarnings("unchecked")
		List<PartitionInformation> paramList = (List<PartitionInformation>)o;
		if (paramList.size() <= 0) {
			logger.println("empty parameter");
			return false;
		}

		if (paramList.size() > elemSize) {
			logger.println("too many parameter. ( > " + elemSize + ")");
			return false;
		}

		Iterator<?> it = paramList.iterator();
		while(it.hasNext()) {
			PartitionInformation pi = (PartitionInformation) it.next();
			File f = pi.getBinary();
			if (f == null || !f.exists()) {
				logger.println("file not exist");
				return false;
			}

			if (!f.isFile() || !f.canRead()) {
				logger.println("can't read the file: " + f.getAbsolutePath());
				return false;
			}
		}

		mWorkMap.put(name, paramList);

		return true;
	}

	private boolean initParamCheck(ServiceParams param) {
		PrintStream logger = service.getLogger();

		if (param == null) {
			logger.println("illegal argument. failed parse argument");
			return false;
		}

		Object o = param.getAsIs(ParamKeysEnum.PARAM.code);
		if (!(o instanceof List<?>)) {
			logger.println("illegal argument. failed parse paramList");
			return false;
		}

		/** argument check **/
		List<?> paramList = (List<?>)o;
		if (paramList.size() <= 0) {
			logger.println("empty parameter");
			return false;
		}

		Iterator<?> it = paramList.iterator();
		while(it.hasNext()) {
			o = it.next();
			if (!(o instanceof PartitionInformation)) {
				logger.println("illegal argument. failed to read partition");
				return false;
			}

			PartitionInformation part = (PartitionInformation) o;
			if (part.getName() == null) {
				logger.println("illegal argument. failed to get partition name");
				return false;
			}
		}

		return true;
	}

}
