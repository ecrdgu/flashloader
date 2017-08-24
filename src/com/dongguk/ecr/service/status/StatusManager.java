package com.dongguk.ecr.service.status;

import java.io.PrintStream;
import java.util.ArrayList;
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
import com.dongguk.ecr.service.FlashLoaderService;

/**
 * StatusManager
 * @author jhun.ahn
 *
 */
public class StatusManager extends AbstractObserve {
	public static final String MAX_ELEMENT = "max_element";
	private final IServiceManager service;
	private final List<String> deviceList;
	private int selectIdx = -1;
	private boolean bMultiDevice = false;

	public StatusManager(IServiceManager service) {
		super(ModulesEnum.STATUS.code);
		this.service = service;

		deviceList = new ArrayList<String>();
	}

	private void load() {
		IProperties prop;
		try {
			prop = PropertiesFactory.createOrget(FlashLoaderService.APP_CONFIG);
			bMultiDevice = prop.getBoolean(PropertiesKeys.
					getDeviceCategory("multi_device"));

		} catch (ConfigurationException e) {
			return;
		}
	}

	@Override
	public Object eventHandler(int id, Object o) {
		ServiceParams param = null;
		EventIds eventId = null;
		boolean retCode = false;

		PrintStream logger = service.getLogger();

		if (o instanceof ServiceParams) {
			param = (ServiceParams) o;
			eventId = (EventIds) param.getAsIs(ParamKeysEnum.CMD.code);
		} else if (o instanceof EventIds) {
			eventId = (EventIds) o;
		}

		if (eventId == null)
			return null;

		Iterator<String> it = deviceList.iterator();
		switch (eventId) {
		case UPDATE:
			load();
			break;

		case STATUS_DEVICE_ADDED:
			if (!bMultiDevice && deviceList.size() > 0)
				break;

			if (param != null) {
				String device = param.getAsString(ParamKeysEnum.PARAM.code);
				if (device != null && !deviceList.contains(device))
					retCode = deviceList.add(device);

				param.set(ParamKeysEnum.RETCODE.code, retCode);

				if (!retCode)
					logger.println("failed to added device: " + device);

				return param;
			}
			break;
		case STATUS_DEVICE_CLEAR:
			while(it.hasNext()) {
				it.next();
				it.remove();
			}

			param = new ServiceParams();
			param.set(ParamKeysEnum.CMD.code, eventId);
			param.set(ParamKeysEnum.RETCODE.code, true);
			return param;

		case STATUS_DEVICE_DELETED:
			if (param != null) {
				retCode = true;
				String device = param.getAsString(ParamKeysEnum.PARAM.code);
				if (device != null && deviceList.contains(device)) {

					//TODO: firmware download check..!!
					while(it.hasNext()) {
						String str = it.next();
						if (str.equals(device)) {
							retCode = deviceList.remove(str);
							break;
						}
					}
				}

				param.set(ParamKeysEnum.RETCODE.code, retCode);
				if (!retCode)
					logger.println("failed to remove device: " + device);

				return param;
			}
			break;
		case STATUS_DEVICE_SELECT:
			if (param != null) {
				selectIdx = param.getAsInt(ParamKeysEnum.PARAM.code);
				if (selectIdx >= 0)
					logger.println("device select: " + deviceList.get(selectIdx));
				else
					logger.println("clear device list");
			}
			break;
		case STATUS_ITEM_CLEAR:
			if (param != null) {
				String name = param.getAsString(ParamKeysEnum.DEVICE.code);
				if (name == null)
					break;
				return param;
			}
			break;
		case STATUS_ITEM_ADDED:
			if (param != null) {
				o = param.getAsIs(ParamKeysEnum.PARAM.code);
				if (!(o instanceof List<?>))
					break;

				List<?> paramList = (List<?>)o;
				if (paramList.size() <= 0)
					break;

				String name = param.getAsString(ParamKeysEnum.DEVICE.code);
				if (name == null)
					break;
				return param;
			}
			break;

		case STATUS_ITEM_SUCCESS:
			break;
		case STATUS_ITEM_FAILURE:
			break;
		case STATUS_ITEM_DELETED:
			break;

		default:
			break;
		}

		return null;
	}

	public List<String> getDeviceList() {
		return this.deviceList;
	}

	public int getSelectIdx() {
		return this.selectIdx;
	}

	public String getSelectDevice() {
		if (selectIdx < 0)
			return null;

		return deviceList.get(selectIdx);
	}

}
