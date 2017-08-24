package com.dongguk.ecr.ui.connection;

import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Observable;

import javax.swing.JButton;
import javax.swing.JComboBox;

import com.dongguk.ecr.common.payload.ServiceParams;
import com.dongguk.ecr.constant.EventIds;
import com.dongguk.ecr.constant.ParamKeysEnum;
import com.dongguk.ecr.framework.service.IServiceManager;
import com.dongguk.ecr.framework.service.observe.IObserver;
import com.dongguk.ecr.framework.ui.GraphicPalette;

/**
 * ConnectionPanel
 * @author jhun.ahn
 *
 */
public class ConnectionPanel extends GraphicPalette implements IObserver {
	private JComboBox<String> comboBox = new JComboBox<String>();
	private final IServiceManager service;
	private Integer id;

	public ConnectionPanel(IServiceManager service) {
		this.service = service;
		setLayout();

		create();

		load();
	}

	private void load() { }

	private void add(String device) {
		ServiceParams param = new ServiceParams();
		param.set(ParamKeysEnum.CMD.code, EventIds.CONNCTION_ADD_DEVICE);
		param.set(ParamKeysEnum.PARAM.code, device);
		signal(param);
	}

	private void create() {
		JButton btnAddItem = new JButton("add");
		btnAddItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				String str = (String)comboBox.getSelectedItem();
				if (str != null)
					add(str);
			}
		});

		JButton btnSearch = new JButton("refresh");
		btnSearch.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				signal(EventIds.CONNCTION_CLEAR_DEVICE);
				signal(EventIds.CONNCTION_DEVICE_SEARCH);
			}
		});

		addComponent(comboBox, gridx++, gridy, 1, 1, 1.0, 1.0, GraphicPalette.wideInsets, GridBagConstraints.FIRST_LINE_START,
				GridBagConstraints.HORIZONTAL);

		addComponent(btnAddItem, gridx++, gridy, 1, 1, 0.0, 0.0, GraphicPalette.wideInsets,
				GridBagConstraints.PAGE_START, GridBagConstraints.NONE);

		addComponent(btnSearch, gridx++, gridy, 1, 1, 0.0, 0.0, GraphicPalette.wideInsets,
				GridBagConstraints.FIRST_LINE_END, GridBagConstraints.NONE);
	}

	@Override
	public void update(Observable arg0, Object arg1) {
		EventIds eventId = null;
		ServiceParams param = null;

		if (arg1 instanceof List<?>)
			eventId = EventIds.CONNCTION_DEVICE_SEARCH;
		else if (arg1 instanceof EventIds)
			eventId = (EventIds) arg1;
		else if (arg1 instanceof ServiceParams) {
			param = (ServiceParams) arg1;
			eventId = (EventIds)param.getAsIs(ParamKeysEnum.CMD.code);
		}

		switch (eventId) {
		case UPDATE:
			load();
			break;
		case CONNCTION_DEVICE_SEARCH:
			comboBox.removeAllItems();

			@SuppressWarnings("unchecked")
			List<String> list = (List<String>) arg1;

			for (String usbDevName : list) {
				comboBox.addItem(usbDevName);
			}

			break;
		case CONNCTION_CLEAR_DEVICE:
			comboBox.removeAllItems();
			break;

		case CONNCTION_DELETE_DEVICE:
			String name = param.getAsString(ParamKeysEnum.PARAM.code);
			if (name == null)
				return;

			int size = comboBox.getItemCount();

			for (int i = 0; i < size; i++) {
				String str = comboBox.getItemAt(i);
				System.out.println(str);
				if (name.equals(str)) {
					comboBox.removeItem(str);
					break;
				}
			}

			display();
			break;
		case CONNCTION_ADD_DEVICE:
		default:
			return;
		}
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


}
