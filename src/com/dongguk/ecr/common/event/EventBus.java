package com.dongguk.ecr.common.event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;

import com.dongguk.ecr.framework.common.event.IEventBus;
import com.dongguk.ecr.framework.common.event.IEventBusListener;

/**
 * A skeleton implementation of EventBus.
 * @author jhun.ahn
 *
 */
/* package */
class EventBus implements IEventBus {
	private final String mBusName;
	private List<IEventBusListener> mListenerList = new ArrayList<IEventBusListener>();
	private HashMap<Integer, List<IEventBusListener>> mListenerMap = new HashMap<Integer, List<IEventBusListener>>();
	private AsyncNotifierRunnable mAsyncRunnable = null;

	/* package */
	EventBus(String busName) {
		mBusName = busName;
	}

	@Override
	public synchronized void start() {
		if (mAsyncRunnable != null)
			return;

		mAsyncRunnable = new AsyncNotifierRunnable();
		new Thread(mAsyncRunnable, "EventBus." + name()).start();
	}

	@Override
	public boolean isStarted() {
		return (mAsyncRunnable != null);
	}

	@Override
	public synchronized void terminate() {
		mAsyncRunnable.terminate();
		mAsyncRunnable = null;
	}

	@Override
	public boolean addListener(int id, IEventBusListener listener) {
		List<IEventBusListener> listeners = null;
		synchronized (mListenerMap) {
			if (!mListenerMap.containsKey(id)) {
				listeners = new ArrayList<IEventBusListener>();
				mListenerMap.put(id, listeners);
			} else {
				listeners = mListenerMap.get(id);
			}
			return listeners.add(listener);
		}
	}

	@Override
	public boolean addListener(IEventBusListener listener) {
		synchronized (mListenerList) {
			return mListenerList.add(listener);
		}
	}

	@Override
	public boolean removeListener(int id, IEventBusListener listener) {
		List<IEventBusListener> listeners = null;
		synchronized (mListenerMap) {
			if (!mListenerMap.containsKey(id)) {
				return false;
			} else {
				listeners = mListenerMap.get(id);
			}
			return listeners.remove(listener);
		}
	}

	@Override
	public boolean removeListener(IEventBusListener listener) {
		synchronized (mListenerList) {
			return mListenerList.remove(listener);
		}
	}

	@Override
	public String name() {
		return mBusName;
	}

	@Override
	public void send(int id, Event event) {
		synchronized (mListenerList) {
			for (IEventBusListener l : mListenerList) {
				try {
					l.handleEvent(id, event);
				} catch (Exception e) {
					/* TODO : print to logger */
					System.err.println("exception in listener\n" + e);
				}
			}
		}

		List<IEventBusListener> listeners = null;
		synchronized (mListenerMap) {
			if (!mListenerMap.containsKey(id)) {
				return;
			} else {
				listeners = mListenerMap.get(id);
			}

			for (IEventBusListener l : listeners) {
				try {
					l.handleEvent(id, event);
				} catch (Exception e) {
					/* TODO : print to logger */
					System.err.println("exception in listener\n" + e);
				}
			}
		}
	}

	@Override
	public void send(int id) {
		send(id, null);
	}

	@Override
	public void post(int id, Event event) {
		mAsyncRunnable.reqNotify(id, event);
	}

	/**
	 * A task to deliver event in asynchronously.
	 *
	 */
	private final class AsyncNotifierRunnable implements Runnable {

		private ArrayBlockingQueue<AsynReq> mQueue = new ArrayBlockingQueue<AsynReq>(64);

		private void reqNotify(int id, Event event) {
			mQueue.offer(new AsynReq(id, event));
		}

		private void terminate() {
			mQueue.offer(POISON);
		}

		@Override
		public void run() {
			while (true) {
				AsynReq req = null;
				try {
					req = mQueue.take();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				if (req == POISON) {
					return;
				}

				send(req.id, req.event);
			}
		}
	}

	private static final AsynReq POISON = new AsynReq(-1, null);

	private static final class AsynReq {

		int id;
		Event event;

		AsynReq(int id, Event event) {
			this.id = id;
			this.event = event;
		}
	}
}
