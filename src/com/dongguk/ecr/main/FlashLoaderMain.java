package com.dongguk.ecr.main;

import java.awt.EventQueue;
import java.io.File;

import javax.naming.ConfigurationException;

import com.dongguk.ecr.common.config.PropertiesFactory;
import com.dongguk.ecr.constant.EventIds;
import com.dongguk.ecr.constant.ModulesEnum;
import com.dongguk.ecr.service.FlashLoaderService;
import com.dongguk.ecr.service.config.OpenOcdConfiguration;
import com.dongguk.ecr.ui.FlashLoaderGuiManager;

/**
 * FlashLoaderMain
 * @author jhun.ahn
 *
 */
public class FlashLoaderMain {
	private static Thread shutdown = new Thread() {
		@Override
		public void run() {
			for (String key : PropertiesFactory.getKeys()) {
				try {
					PropertiesFactory.createOrget(key).save();
				} catch (ConfigurationException e) {
					e.printStackTrace();
				}
			}
		};
	};

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		Runtime.getRuntime().addShutdownHook(shutdown);

		EventQueue.invokeLater(new Runnable() {
			public void run() {
				final FlashLoaderService service =
						FlashLoaderService.getInstance();
				try {
					if (!service.initialize()) {
						System.err.println("service init fail");
						return;
					}

					FlashLoaderGuiManager.setFontStyle();
					FlashLoaderGuiManager.setStype();

					FlashLoaderGuiManager flashLoaderGui = FlashLoaderGuiManager.getinstance();
					if (!flashLoaderGui.initialize()) {
						System.err.println("service init fail");
						return;
					}

					flashLoaderGui.start();
				} catch (Exception e) {
					e.printStackTrace();
				}

				setDefaultValue();

				service.getLogger().print(getInitialLog());
			}
		});
	}

	private static String getInitialLog() {
		StringBuffer buf = new StringBuffer();
		OpenOcdConfiguration openOcdCfg = OpenOcdConfiguration.getinstance();

		final File openOcd = openOcdCfg.getOpenOcd();
		final File config = openOcdCfg.getDeviceConfig();
		final File script = openOcdCfg.getBasePath();

		buf.append("---------------------------------------\n");
		buf.append(SystemTypeLoader.getSystemLog());
		buf.append(" - openocd : ");
		buf.append(openOcd == null ?
				"not exist" : openOcd.getName());
		buf.append('\n');

		buf.append(" - config : ");
		buf.append(config == null ?
				"not exist" : config.getName());
		buf.append('\n');

		buf.append(" - script : ");
		buf.append(script == null ?
				"not exist" : script.getName() + "/");
		buf.append('\n');
		buf.append("---------------------------------------\n");
		return buf.toString();
	}

	private static void setDefaultValue() {
		final FlashLoaderService service =
				FlashLoaderService.getInstance();

		service.sendEvent(ModulesEnum.CONNECTION.code,
				EventIds.CONNCTION_DEVICE_SEARCH);
	}
}
