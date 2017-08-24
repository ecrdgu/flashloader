package com.dongguk.ecr.service.connection;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.naming.ConfigurationException;
import javax.usb.UsbDevice;
import javax.usb.UsbDeviceDescriptor;
import javax.usb.UsbDisconnectedException;
import javax.usb.UsbException;
import javax.usb.UsbHostManager;
import javax.usb.UsbHub;
import javax.usb.UsbServices;
import javax.usb.event.UsbDeviceDataEvent;
import javax.usb.event.UsbDeviceErrorEvent;
import javax.usb.event.UsbDeviceEvent;
import javax.usb.event.UsbDeviceListener;

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
 * ConnectionManager
 * @author jhun.ahn
 *
 */
public class ConnectionManager extends AbstractObserve {
	private short vid;
	private short pid;
	private final IServiceManager service;

	private HashMap<String, String> mDeviceMap = new HashMap<>();

	public ConnectionManager(IServiceManager service) {
		super(ModulesEnum.CONNECTION.code);
		this.service = service;

		load();
	}

	private void load() {
		IProperties prop;
		try {
			prop = PropertiesFactory.createOrget(FlashLoaderService.APP_CONFIG);
			vid = prop.getShort(PropertiesKeys.
					getDeviceCategory("vender_id"));
			pid = prop.getShort(PropertiesKeys.
					getDeviceCategory("product_id"));
		} catch (ConfigurationException e) {
			vid = -1;
			pid = -1;
			return;
		}
	}

	@Override
	public Object eventHandler(int id, Object o) {
		ServiceParams param = null;
		EventIds eventId = null;
		if (o instanceof ServiceParams) {
			param = (ServiceParams) o;
			eventId = (EventIds) param.getAsIs(ParamKeysEnum.CMD.code);
		} else if (o instanceof EventIds) {
			eventId = (EventIds) o;
		}

		if (eventId == null)
			return null;

		switch (eventId) {
		case UPDATE:
			load();
			return eventId;
		case CONNCTION_DEVICE_SEARCH:
			return getDeviceNameList(vid, pid);

		case CONNCTION_CLEAR_DEVICE:
			service.sendEvent(ModulesEnum.STATUS.code, EventIds.STATUS_DEVICE_CLEAR);
			break;

		case CONNCTION_ADD_DEVICE:
			param.set(ParamKeysEnum.CMD.code, EventIds.STATUS_DEVICE_ADDED);
			service.sendEvent(ModulesEnum.STATUS.code, param);
			break;

		case CONNCTION_DELETE_DEVICE:
			ServiceParams p = param.duplicate();
			p.set(ParamKeysEnum.CMD.code, EventIds.STATUS_DEVICE_DELETED);
			service.sendEvent(ModulesEnum.STATUS.code, p);
			return param;

		default:
			break;
		}

		return null;
	}

	private List<UsbDevice> findDevice(UsbHub hub, short vendorId, short productId) {

		if (hub == null)
			return null;

		List<?> perepheriques = hub.getAttachedUsbDevices();
		List<UsbDevice> list = new ArrayList<UsbDevice>();

		for (Object o : perepheriques) {
			if (!(o instanceof UsbDevice)) {
				return null;
			}

			UsbDevice device = (UsbDevice) o;

			if (device.isUsbHub()) {
				list.addAll(findDevice((UsbHub) device, vendorId, productId));
			} else {
				boolean islistUp = false;
				UsbDeviceDescriptor desc = device.getUsbDeviceDescriptor();

				islistUp = (vendorId < 0 && productId < 0);
				islistUp |= (desc.idVendor() == vendorId && desc.idProduct() == productId);

				if (islistUp) {
					device.addUsbDeviceListener(new innerListner());
					list.add(device);
				}
			}

		}

		return list;
	}

	private List<UsbDevice> getDeviceList(short vender, short product) {

		try {
			UsbServices usbService = UsbHostManager.getUsbServices();
			UsbHub root = usbService.getRootUsbHub();
			return findDevice(root, vender, product);
		} catch (SecurityException | UsbException e) {
			System.err.println("usb device error: " + e.getMessage());
		}

		return null;

	}

	private List<String> getDeviceNameList(short vender, short product) {
		List<UsbDevice> usbList = getDeviceList(vender, product);
		if (usbList == null | usbList.isEmpty())
			return null;

		List<String> nameList = new ArrayList<>();

		for (UsbDevice usb : usbList) {
			String name = getUsbDeviceName(usb);

			nameList.add(name);
			mDeviceMap.put(usb.toString(), name);
		}

		return nameList;
	}

	private String getUsbDeviceName(UsbDevice device) {
		String strName;
		UsbDeviceDescriptor desc =
				device.getUsbDeviceDescriptor();

		try {
			byte man = desc.iManufacturer();
			byte prod = desc.iProduct();

			strName = String.format("%s %s", device.getString(man),
					device.getString(prod));

		} catch (UnsupportedEncodingException | UsbDisconnectedException | UsbException e) {
			System.err.println(e.getMessage());

			strName = String.format("%X:%X", desc.idVendor(), desc.idProduct());
		}

		return strName;
	}

	private class innerListner implements UsbDeviceListener {

		@Override
		public void dataEventOccurred(UsbDeviceDataEvent arg0) {
			UsbDevice device = arg0.getUsbDevice();
			System.out.println(" - dataEventOccurred " + device);
		}

		@Override
		public void errorEventOccurred(UsbDeviceErrorEvent arg0) {
			UsbDevice device = arg0.getUsbDevice();
			System.err.println(" - errorEventOccurred " + device);
		}

		@Override
		public void usbDeviceDetached(UsbDeviceEvent arg0) {
			UsbDevice usb = arg0.getUsbDevice();
			ServiceParams param = new ServiceParams();

			String name = usb.toString();

			param.set(ParamKeysEnum.CMD.code, EventIds.CONNCTION_DELETE_DEVICE);
			param.set(ParamKeysEnum.PARAM.code, mDeviceMap.get(name));
			service.sendEvent(ModulesEnum.CONNECTION.code, param);
			mDeviceMap.remove(usb.toString());

			System.out.println(" - usbDeviceDetached " + usb);
		}
	}
}
