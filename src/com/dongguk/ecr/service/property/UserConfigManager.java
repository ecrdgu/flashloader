package com.dongguk.ecr.service.property;

import java.util.Map;

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
import com.dongguk.ecr.service.config.OpenOcdConfiguration;

public class UserConfigManager  extends AbstractObserve {
	private IProperties prop = null;
	private final IServiceManager service;

	public UserConfigManager(IServiceManager service) {
		super(ModulesEnum.PROPERTY.code);

		try {
			prop = PropertiesFactory.createOrget(FlashLoaderService.APP_CONFIG);
		} catch (ConfigurationException e) {
			service.getLogger().println(e.getMessage());
		}

		this.service = service;
	}

	@Override
	public Object eventHandler(int id, Object t) {
		if (!(t instanceof Map<?,?>))
			return null;

		if (prop == null)
			return null;

		Map<?,?> map = (Map<?,?>)t;

		for(Object o : map.keySet()) {
			if (!(o instanceof String))
				continue;
			String key = (String) o;
			Object s = map.get(key);
			if (s == null)
				s = "";

			prop.set(key, s);
		}

		OpenOcdConfiguration openOcdCfg = OpenOcdConfiguration.getinstance();

		try {
			String base = prop.getString(PropertiesKeys.getPathCategory("base"));
			String binary = prop.getString(PropertiesKeys.getOCDCategory("binary"));
			String conf = prop.getString(PropertiesKeys.getOCDCategory("conf"));

			openOcdCfg.setBasePath(base);
			openOcdCfg.setOpenOcd(null, binary);
			openOcdCfg.setDeviceConfig(conf);
		}
		catch (ConfigurationException e) { }

		ServiceParams param = new ServiceParams();
		param.set(ParamKeysEnum.CMD.code, EventIds.UPDATE);

		service.sendEvent(ModulesEnum.COMMON.code, param);

		return true;
	}
}
