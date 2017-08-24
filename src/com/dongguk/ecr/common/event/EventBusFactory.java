package com.dongguk.ecr.common.event;

import java.util.HashMap;

import com.dongguk.ecr.framework.common.event.IEventBus;

/**
 * EventBusFactory: A factory class to create EventBus instances.
 * @author jhun.ahn
 *
 */
public class EventBusFactory {
	private static final HashMap<String, IEventBus> sInstanceMap = new HashMap<String, IEventBus>();

	/**
	 * Creates(or get) an EventBus instance for general usage.
	 *
	 * @param busName
	 * @return
	 */
	public static IEventBus createOrGet(String busName) {
		IEventBus bus = null;
		synchronized (sInstanceMap) {
			if (!sInstanceMap.containsKey(busName)) {
				bus = new EventBus(busName);
				bus.start();
				sInstanceMap.put(busName, bus);
				/* TODO: print to logger */
			} else {
				bus = sInstanceMap.get(busName);
			}
		}
		return bus;
	}
}
