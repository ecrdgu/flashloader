package com.dongguk.ecr.ui.config;

import java.awt.GridBagConstraints;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Observable;

import javax.naming.ConfigurationException;
import javax.swing.filechooser.FileFilter;

import com.dongguk.ecr.common.config.PropertiesFactory;
import com.dongguk.ecr.common.payload.ServiceParams;
import com.dongguk.ecr.constant.EventIds;
import com.dongguk.ecr.constant.ParamKeysEnum;
import com.dongguk.ecr.constant.PropertiesKeys;
import com.dongguk.ecr.framework.common.config.IProperties;
import com.dongguk.ecr.framework.service.IServiceManager;
import com.dongguk.ecr.framework.service.observe.IObserver;
import com.dongguk.ecr.framework.ui.FileSelectablePanel;
import com.dongguk.ecr.framework.ui.GraphicPalette;
import com.dongguk.ecr.framework.ui.IFileBrowser;
import com.dongguk.ecr.service.FlashLoaderService;

/**
 * PartitionLoadPanel
 * @author jhun.ahn
 *
 */
public class ConfigLoadPanel extends GraphicPalette implements IObserver {
	private static final String ext = "cfg";
	private final IServiceManager service;

	private HashMap<EventIds, FileSelector> panelMap;

	private Integer id;

	private class FileSelector extends FileSelectablePanel {
		private String name;

		public FileSelector(String name) {

			this.name = name;

			setFileSelectionMode(0);
			setFilter(new FileFilter() {

				@Override
				public String getDescription() {
					return String.format("%s file data(*.%s)", name, ext);
				}

				@Override
				public boolean accept(File arg0) {
					if (arg0.isDirectory()) {
						return true;
					} else {
						return arg0.getName().toLowerCase().endsWith("." + ext);
					}
				}
			});

		}

		public void setText(String str) {
			super.setText(name, str);
		}

		public void addListener(IFileBrowser listener) {
			addItem(name, listener);
		}
	}

	/**
	 * Create the panel.
	 */
	public ConfigLoadPanel(IServiceManager service) {
		this.service = service;
		FileSelector filePanel;

		setLayout();

		panelMap = new HashMap<>();

		filePanel = new FileSelector("Partition Map");
		filePanel.addListener(new FileSelectListener(EventIds.CONFIG_PARTITION_SELECTED));

		addComponent(filePanel.getPalette(), 0, gridy++, 1, 1, 1.0, 1.0, GraphicPalette.noInsets,
				GridBagConstraints.LINE_START, GridBagConstraints.BOTH);

		panelMap.put(EventIds.CONFIG_PARTITION_SELECTED, filePanel);

		filePanel = new FileSelector("Configuration");
		filePanel.addListener(new FileSelectListener(EventIds.CONFIG_FILE_SELECTED));

		addComponent(filePanel.getPalette(), 0, gridy++, 1, 1, 1.0, 1.0, GraphicPalette.noInsets,
				GridBagConstraints.LINE_START, GridBagConstraints.BOTH);

		panelMap.put(EventIds.CONFIG_FILE_SELECTED, filePanel);

		load();
	}

	private void load() {
		final IProperties prop;

		String path = null;
		try {
			prop = PropertiesFactory.createOrget(FlashLoaderService.APP_CONFIG);
			path = prop.getString(PropertiesKeys.getPathCategory("base"));
		} catch (ConfigurationException e) {
			System.err.println(e.getMessage());
		}

		Iterator<EventIds> it = panelMap.keySet().iterator();
		while(it.hasNext()) {
			panelMap.get(it.next()).setCurrentDirectory(new File(path));
		}
	}

	private class FileSelectListener implements IFileBrowser {
		private final EventIds id;
		public FileSelectListener(EventIds id) {
			this.id = id;
		}

		@Override
		public void actionPerformed(Object o) {
			if (!(o instanceof File))
				return;

			ServiceParams param = new ServiceParams();
			param.set(ParamKeysEnum.CMD.code, this.id);
			param.set(ParamKeysEnum.PARAM.code, o);
			signal(param);
		}
	}

	@Override
	public void update(Observable arg0, Object arg1) {
		if (!(arg1 instanceof ServiceParams))
			return;

		ServiceParams param = (ServiceParams) arg1;
		Object o = param.getAsIs(ParamKeysEnum.CMD.code);
		if (!(o instanceof EventIds))
			return;
		EventIds cmd = (EventIds) o;
		switch (cmd) {
		case CONFIG_FILE_SELECTED:
		case CONFIG_PARTITION_SELECTED:
			break;
		case UPDATE:
			load();
			return;
		default:
			return;
		}

		try {
			boolean retCode = (Boolean)param.getAsIs(ParamKeysEnum.RETCODE.code);
			if (retCode) {
				File f = (File)param.getAsIs(ParamKeysEnum.PARAM.code);
				panelMap.get(cmd).setText(f.getCanonicalPath());
			}
		} catch (ClassCastException | IOException e) {
			service.getLogger().println(e.getMessage());
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
