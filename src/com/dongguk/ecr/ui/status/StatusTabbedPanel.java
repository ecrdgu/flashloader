package com.dongguk.ecr.ui.status;

import java.awt.GridBagConstraints;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Observable;

import javax.naming.ConfigurationException;
import javax.swing.BorderFactory;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.dongguk.ecr.common.config.PropertiesFactory;
import com.dongguk.ecr.common.payload.ServiceParams;
import com.dongguk.ecr.constant.EventIds;
import com.dongguk.ecr.constant.ParamKeysEnum;
import com.dongguk.ecr.constant.PropertiesKeys;
import com.dongguk.ecr.framework.common.config.IProperties;
import com.dongguk.ecr.framework.service.IServiceManager;
import com.dongguk.ecr.framework.service.observe.IObserver;
import com.dongguk.ecr.framework.ui.GraphicPalette;
import com.dongguk.ecr.service.FlashLoaderService;
import com.dongguk.ecr.service.config.PartitionInformation;
import com.dongguk.ecr.service.status.StatusEnum;
import com.dongguk.ecr.service.status.StatusManager;

/**
 * StatusTabbedPanel
 * @author jhun.ahn
 *
 */
public class StatusTabbedPanel  extends GraphicPalette implements IObserver {
	private int NUMBER_OF_BUTTONS = 10;

	private final JTabbedPane tab;
	private final HashMap<String, GraphicStatusPanel> mDeviceMap;

	private final IServiceManager service;
	private Integer id;

	public StatusTabbedPanel(IServiceManager service) {
		this.service = service;

		setLayout(new GridLayout(0, 1, 0, 0));

		final IProperties prop;

		try {
			prop = PropertiesFactory.createOrget(FlashLoaderService.APP_CONFIG);
			NUMBER_OF_BUTTONS = prop.getInteger(PropertiesKeys.getServiceCategory(StatusManager.MAX_ELEMENT));
		} catch (ConfigurationException e) {
			System.err.println(e.getMessage());
		}

		tab = new JTabbedPane(JTabbedPane.TOP);
		tab.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent arg0) {
				Object o = arg0.getSource();
				if (o instanceof JTabbedPane) {
					JTabbedPane t = (JTabbedPane) o;

					ServiceParams param = new ServiceParams();
					param.set(ParamKeysEnum.CMD.code, EventIds.STATUS_DEVICE_SELECT);
					param.set(ParamKeysEnum.PARAM.code, t.getSelectedIndex());

					signal(param);
				}
			}
		});

		tab.setBorder(BorderFactory.createEmptyBorder(0,5,5,5));
		addComponent(tab, 0, 0, 1, 1, 1.0, 1.0, noInsets,
                GridBagConstraints.LINE_START, GridBagConstraints.BOTH);

		mDeviceMap = new HashMap<String, GraphicStatusPanel>();
	}

	@Override
	public void update(Observable o, Object data) {
		if (!(data instanceof ServiceParams))
			return;
		ServiceParams param = (ServiceParams) data;

		Object obj = param.getAsIs(ParamKeysEnum.CMD.code);
		if (!(obj instanceof EventIds))
			return;

		EventIds eventId = (EventIds) obj;

		String name;
		boolean retCode = false;
		switch(eventId) {
		case STATUS_DEVICE_ADDED:
			name = param.getAsString(ParamKeysEnum.PARAM.code);
			if (name == null)
				return;

			if (param.getAsBoolean(ParamKeysEnum.RETCODE.code))
				addDevice(name);

			break;
		case STATUS_DEVICE_CLEAR:
			retCode = param.getAsBoolean(ParamKeysEnum.RETCODE.code);
			if (retCode) {
				Iterator<String> it = mDeviceMap.keySet().iterator();
				while(it.hasNext()) {
					remove(it.next());
				}
			}
			break;
		case STATUS_DEVICE_DELETED:
			name = param.getAsString(ParamKeysEnum.PARAM.code);
			retCode = param.getAsBoolean(ParamKeysEnum.RETCODE.code);
			if (retCode && !mDeviceMap.containsKey(name))
				break;

			remove(name);

			break;
		case STATUS_ITEM_ADDED:
			List<?> paramList = (List<?>)param.getAsIs(ParamKeysEnum.PARAM.code);
			name = param.getAsString(ParamKeysEnum.DEVICE.code);
			if (!mDeviceMap.containsKey(name))
				break;
			GraphicStatusPanel gSp = mDeviceMap.get(name);
			gSp.removeAll();

			@SuppressWarnings("unchecked")
			Iterator<PartitionInformation> it =
					(Iterator<PartitionInformation>) paramList.iterator();
			while(it.hasNext()) {
				PartitionInformation pI = it.next();
				gSp.add(pI.getDesc(), StatusEnum.DEFAULT);
			}

			break;
		case STATUS_ITEM_CLEAR:
			name = param.getAsString(ParamKeysEnum.PARAM.code);
			if (name == null)
				return;
			name = param.getAsString(ParamKeysEnum.DEVICE.code);
			if (!mDeviceMap.containsKey(name))
				break;

			mDeviceMap.get(name).removeAll();
			break;

		case STATUS_ITEM_SUCCESS:
			break;
		case STATUS_ITEM_FAILURE:
			break;
		default:
			break;
		}
	}

	private void remove(String name) {
		GraphicStatusPanel gSp = mDeviceMap.get(name);
		gSp.removeAll();
		tab.remove(gSp.getPalette());

		mDeviceMap.remove(name);
	}

	@Override
	public boolean connect(int id) {
		this.id = id;
		if (service == null)
			return false;

		service.addObserver(id, this);
		return true;
	}

	@Override
	public int getId() {
		return this.id;
	}

	@Override
	public boolean signal(Object o) {
		if (this.id == null) {
			return false;
		}

		service.sendEvent(getId(), o);
		return true;
	}

	private void addDevice(final String name) {
		int tabSize = tab.getTabCount();

		GraphicStatusPanel gP = new GraphicStatusPanel(NUMBER_OF_BUTTONS);
		gP.setName(name);
		mDeviceMap.put(name, gP);
		tab.add(name, gP.getPalette());

		final ButtonTabComponent bTc = new ButtonTabComponent(tab, true);
		bTc.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				int idx = tab.indexOfTabComponent(bTc);
				if (idx < 0)
					return;

				ServiceParams param = new ServiceParams();
				param.set(ParamKeysEnum.CMD.code, EventIds.STATUS_DEVICE_DELETED);
				param.set(ParamKeysEnum.PARAM.code, tab.getTitleAt(idx));

				signal(param);
			}
		});

		tab.setTabComponentAt(tabSize, bTc);
		tab.setSelectedComponent(gP.getPalette());
	}

}
