package com.dongguk.ecr.service;

import java.io.File;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import com.dongguk.ecr.common.event.Event;
import com.dongguk.ecr.common.event.EventBusFactory;
import com.dongguk.ecr.constant.ModulesEnum;
import com.dongguk.ecr.framework.common.event.IEventBus;
import com.dongguk.ecr.framework.service.IServiceManager;
import com.dongguk.ecr.framework.service.observe.AbstractObserve;
import com.dongguk.ecr.main.SystemTypeLoader;
import com.dongguk.ecr.main.SystemTypeLoader.SystemTypesEnum;
import com.dongguk.ecr.service.config.PartitionMapper;
import com.dongguk.ecr.service.connection.ConnectionManager;
import com.dongguk.ecr.service.logger.LogMessageManager;
import com.dongguk.ecr.service.option.FusingOptionManager;
import com.dongguk.ecr.service.property.UserConfigManager;
import com.dongguk.ecr.service.status.StatusManager;

/**
 * FlashLoaderService
 * @author jhun.ahn
 *
 */
public class FlashLoaderService implements IServiceManager {
	public static final String NAME = "ECR SW Downloader";
	public static final String APP_CONFIG = "ecr.flashloader.properties";

	private static FlashLoaderService instance;
	private IEventBus eventBus;

	private HashMap<Integer, AbstractObserve> mWatcherMap;

	private FlashLoaderService() {
		eventBus = EventBusFactory.createOrGet(NAME);
		mWatcherMap = new HashMap<Integer, AbstractObserve>();
	}

	public static FlashLoaderService getInstance() {
		if (instance == null)
			instance = new FlashLoaderService();

		return instance;
	}

	@Override
	public boolean initialize() {
		SystemTypesEnum osType = SystemTypeLoader.getOS();
		if (osType == SystemTypesEnum.UNSUPPORT) {
			System.err.println("This mechine is not support");
			System.exit(1);
		}

		externalLibraryLoader();

		List<AbstractObserve> moduleList = new ArrayList<AbstractObserve>();

		moduleList.add(new LogMessageManager(this));
		moduleList.add(new FusingOptionManager(this));
		moduleList.add(new PartitionMapper(this));
		moduleList.add(new StatusManager(this));
		moduleList.add(new ConnectionManager(this));
		moduleList.add(new UserConfigManager(this));

		for (AbstractObserve w : moduleList) {
			int id = w.getId();
			mWatcherMap.put(id, w);
			eventBus.addListener(id, w);

			eventBus.addListener(ModulesEnum.COMMON.code, w);
		}

		/* ... */
		eventBus.start();

		return true;
	}

	@Override
	public boolean addObserver(int id, Observer o) {
		Observable t = mWatcherMap.get(id);
		if (t == null) {
			return false;
		}

		t.addObserver(o);
		return true;
	}

	@Override
	public PrintStream getLogger() {
		AbstractObserve logger = mWatcherMap.get(ModulesEnum.LOG_MSG.code);
		return ((LogMessageManager) logger).getLogger();
	}

	@Override
	public void sendEvent(int id, Object arg) {
		eventBus.post(id, new Event(arg));
	}


	@Override
	public Object get(Object id) {
		return mWatcherMap.get(id);
	}

	private static final boolean externalLibraryLoader() {

		final char SEPERATOR = '/';
		final String libName = "usb4java-1.2.0";

		StringBuilder strBuilder = new StringBuilder(SystemTypeLoader.getClassPath());
		strBuilder.append("..");
		strBuilder.append(SEPERATOR);
		strBuilder.append("external");
		strBuilder.append(SEPERATOR);
		strBuilder.append(libName);
		strBuilder.append(SEPERATOR);
		strBuilder.append("lib"); strBuilder.append(libName);
		strBuilder.append('-');
		strBuilder.append(SystemTypeLoader.getOS().toString());
		strBuilder.append('-');
		strBuilder.append(SystemTypeLoader.getArch());
		strBuilder.append(".jar");

		File file = new File(strBuilder.toString());
		if (!file.exists())
			return false;

		try {
			URL url = file.toURI().toURL();
			URLClassLoader classLoader =
					(URLClassLoader)ClassLoader.getSystemClassLoader();
			Method method =
					URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
			method.setAccessible(true);
			method.invoke(classLoader, url);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}

		return true;
	}

}
