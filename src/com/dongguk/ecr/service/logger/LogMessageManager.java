package com.dongguk.ecr.service.logger;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import com.dongguk.ecr.common.payload.ServiceParams;
import com.dongguk.ecr.constant.EventIds;
import com.dongguk.ecr.constant.ModulesEnum;
import com.dongguk.ecr.constant.ParamKeysEnum;
import com.dongguk.ecr.framework.service.IServiceManager;
import com.dongguk.ecr.framework.service.observe.AbstractObserve;

/**
 * LogMessageManager
 * @author jhun.ahn
 *
 */
public class LogMessageManager extends AbstractObserve {
	private static final int MAX_PACKET_SIZE = 1024;
	private final ByteArrayOutputStream boas;
	private final PrintStream stream;

	public LogMessageManager(final IServiceManager service) {
		super(ModulesEnum.LOG_MSG.code);
		if (service == null)
			throw new RuntimeException("Service not initialize");

		boas = new ByteArrayOutputStream(MAX_PACKET_SIZE);

		stream = new PrintStream(boas) {
			@Override
			public void write(int arg0) {
				super.write(arg0);
				send();
			}

			@Override
			public void write(byte[] arg0, int arg1, int arg2) {
				super.write(arg0, arg1, arg2);
				send();
			}

			private void send() {
				String str = null;
				try {
					out.flush();

					str = out.toString();
					boas.reset();

				} catch (IOException e) {
					e.printStackTrace();
				}

				ServiceParams params = new ServiceParams();

				params.set(ParamKeysEnum.CMD.code, EventIds.MSG_APPEND);
				params.set(ParamKeysEnum.PARAM.code, str);

				service.sendEvent(ModulesEnum.LOG_MSG.code, params);
			}
		};

	}

	@Override
	public Object eventHandler(int id, Object o) {
		return o;
	}

	public PrintStream printf(String format, Object... o) {
		return stream.printf(format + "\n", o);
	}

	public PrintStream getLogger() {
		return stream;
	}
}
