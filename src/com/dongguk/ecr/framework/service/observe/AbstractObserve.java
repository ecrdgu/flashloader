package com.dongguk.ecr.framework.service.observe;

import java.util.Observable;

import com.dongguk.ecr.common.event.Event;
import com.dongguk.ecr.framework.common.event.IEventBusListener;

/**
 * AbstractObserve
 * @author jhun.ahn
 *
 */
public abstract class AbstractObserve extends Observable implements IEventBusListener {
	private final int id;

	public AbstractObserve(int id) {
		this.id = id;
	}

	abstract public Object eventHandler(int id, Object t);

	@Override
	public void notifyObservers(Object arg0) {
		setChanged();
		if (arg0 == null) {
			super.notifyObservers();
		} else {
			super.notifyObservers(arg0);
		}
	}

	@Override
	public void handleEvent(int id, Event t) {
		Object o = eventHandler(id, t.source());
		if (o != null) {
			notifyObservers(o);
		}
	}

	public int getId() {
		return id;
	}

}
