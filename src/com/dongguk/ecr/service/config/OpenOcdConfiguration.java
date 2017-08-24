package com.dongguk.ecr.service.config;

import java.io.File;

import javax.naming.ConfigurationException;

import com.dongguk.ecr.common.config.PropertiesFactory;
import com.dongguk.ecr.constant.PropertiesKeys;
import com.dongguk.ecr.framework.common.config.IProperties;
import com.dongguk.ecr.main.SystemTypeLoader;
import com.dongguk.ecr.main.SystemTypeLoader.SystemTypesEnum;
import com.dongguk.ecr.service.FlashLoaderService;

/**
 * OpenOcdConfiguration
 * @author jhun.ahn
 *
 */
public final class OpenOcdConfiguration {
	private File base;
	private File openocd;
	private File config;

	private static OpenOcdConfiguration instance;

	public static OpenOcdConfiguration getinstance() {
		if(instance == null)
			instance = new OpenOcdConfiguration();
		return instance;
	}

	public OpenOcdConfiguration() {
		IProperties prop = null;
		String base = null;
		String conf = null;
		String binary = null;

		try {
			prop = PropertiesFactory.createOrget(FlashLoaderService.APP_CONFIG);

			base = prop.getString(PropertiesKeys.getPathCategory("base"));
			binary = prop.getString(PropertiesKeys.getOCDCategory("binary"));
			conf = prop.getString(PropertiesKeys.getOCDCategory("conf"));
		} catch (ConfigurationException e) {
			return;
		}

		setBasePath(base);
		setOpenOcd(null, binary);
		setDeviceConfig(conf);
	}

	public void setOpenOcd(File f, String filePath) {
		File openocd = validFileCheck(f, filePath);

		if (openocd == null) {
			SystemTypesEnum type = SystemTypeLoader.getOS();
			String ext = "";
			switch (type) {
			case OS_UNIX:
				filePath = type.toString();
				break;
			case OS_WINDOWS:
				filePath = "win";
				ext = ".exe";
			default:
				break;
			}

			filePath += SystemTypeLoader.getArch().indexOf("64") > 0?
					64 : 32;
			filePath += File.separatorChar + "openocd" + ext;
			System.out.println("base: " + this.base);
			System.out.println("openocd: " + filePath);
			openocd = validFileCheck(this.base, filePath);
		}

		if (openocd != null && openocd.exists() && openocd.canExecute())
			this.openocd = openocd;
	}

	public File getOpenOcd() { return openocd; }

	public void setBasePath(String path) {
		File base = validPathCheck(path);
		this.base = (base == null)? new File(SystemTypeLoader.getClassPath()) : base;
	}

	public File getBasePath() { return base; }

	public boolean setDeviceConfig(String conf) {
		File config = validFileCheck(null, conf);
		if (config == null)
			return false;

		this.config = config;

		return true;
	}

	public File getDeviceConfig() { return config; }

	/**-------------------------------------------------------------**/

	private File validPathCheck(String path) {
		File file = new File(path);
		if (!file.exists() || !file.isDirectory())
			return null;
		return file;
	}

	private File validFileCheck(File f, String path) {
		File file = f == null? new File(path) : new File(f, path);

		if (!file.exists() || !file.isFile())
			return null;
		return file;
	}


}
