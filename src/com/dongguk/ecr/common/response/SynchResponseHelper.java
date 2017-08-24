package com.dongguk.ecr.common.response;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * SynchResponseHelper
 * @author jhun.ahn
 *
 */
public class SynchResponseHelper {
	private List<IResponseHolder> mResponseHolders;

	private static interface IResponseHolder {
		void setResponse(Object data);

		Object getReponse();

		boolean isResponsed(long timeoutMillis);
	}

	public SynchResponseHelper() {
		this.mResponseHolders = new ArrayList<IResponseHolder>();
	}

	/**
     * Abstract class to receive response in synchronous way.
     *
     */
    public static abstract class ResponseHolder implements IResponseHolder {
        private Object data = null;
        private Object monitor = new Object();

        public final void setResponse(Object data) {
            synchronized (monitor) {
                this.data = data;
                monitor.notifyAll();
            }
        }

        public final boolean isResponsed(long timeoutMillis) {
            synchronized (monitor) {
                if (data != null)
                    return true;

                try { monitor.wait(timeoutMillis); } catch (InterruptedException e) {}

                return data != null;
            }
        }

        public Object getReponse() {
            return data;
        }

        public abstract boolean isMatch(Object data);
    }

    /* package */
    void addResponseHolder(IResponseHolder holder) {
        synchronized (mResponseHolders) {
            mResponseHolders.add(holder);
        }
    }

    /* package */
    void removeResponseHolder(IResponseHolder holder) {
        synchronized (mResponseHolders) {
            mResponseHolders.remove(holder);
        }
    }

    /* package */
    boolean notifyResponse(Object data) {
        synchronized (mResponseHolders) {
            Iterator<IResponseHolder> it = mResponseHolders.iterator();
            IResponseHolder holder;
            while (it.hasNext()) {
                holder = it.next();
//                data.rewind();
                if (((ResponseHolder)holder).isMatch(data)) {
                    it.remove();
                    holder.setResponse(data);
//                    data.rewind();
                    return true;
                }
            }
        }

//        data.rewind();

        return false;
    }

    /* package */void flushResponseWaiting() {
        synchronized (mResponseHolders) {
            Iterator<IResponseHolder> it = mResponseHolders.iterator();
            IResponseHolder holder;
            while (it.hasNext()) {
                holder = it.next();
                it.remove();
                holder.setResponse(null);
            }
        }
    }
}
