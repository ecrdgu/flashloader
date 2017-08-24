package com.dongguk.ecr.service.config;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import javax.naming.ConfigurationException;

import com.dongguk.ecr.common.config.PropertiesFactory;
import com.dongguk.ecr.common.payload.DelimiterBasedPayloadSpec;
import com.dongguk.ecr.common.payload.ParamTypesEnum;
import com.dongguk.ecr.common.payload.ServiceParams;
import com.dongguk.ecr.constant.EventIds;
import com.dongguk.ecr.constant.ModulesEnum;
import com.dongguk.ecr.constant.ParamKeysEnum;
import com.dongguk.ecr.constant.PayloadKeysEnum;
import com.dongguk.ecr.constant.PropertiesKeys;
import com.dongguk.ecr.framework.common.config.IProperties;
import com.dongguk.ecr.framework.service.IServiceManager;
import com.dongguk.ecr.framework.service.observe.AbstractObserve;
import com.dongguk.ecr.service.FlashLoaderService;

/**
 * PartitionMapper
 * @author jhun.ahn
 *
 */
public class PartitionMapper extends AbstractObserve {
	/**
     * Constant for the disabled list delimiter. This character is passed to the
     * list parsing methods if delimiter parsing is disabled. So this character
     * should not occur in string property values.
     */
	private static final String COMMENT_DELIMITER = "#";
	private static final String DELIMITER = " ";
	private static final char START_BRACE = '{';
	private static final char END_BRACE = '}';

	/** TODO : multiple data */
//	private PartitionCmdsEnum cmd;
//	private String name;

	/** FIXME!! for sorting... FIXME... */

	private final DelimiterBasedPayloadSpec payloadSpec =
			DelimiterBasedPayloadSpec.begin()
			.setDelimiter(DELIMITER)
			.addParam(PayloadKeysEnum.DESC.code, ParamTypesEnum.STRING)
			.addParam(PayloadKeysEnum.START_ADDRESS.code, ParamTypesEnum.INT_HEX)
			.addParam(PayloadKeysEnum.SECTION_SIZE.code, ParamTypesEnum.INT_HEX)
			.addParam(PayloadKeysEnum.READ_ONLY.code, ParamTypesEnum.INT)
			.end();

	private final IServiceManager service;
    public PartitionMapper(IServiceManager service) {
		super(ModulesEnum.CONFIG.code);

		this.service = service;
	}

	@Override
	public Object eventHandler(int id, Object o) {
		if (!(o instanceof ServiceParams))
			return null;

		ServiceParams param = (ServiceParams) o;
		o = param.getAsIs(ParamKeysEnum.CMD.code);
		if (!(o instanceof EventIds))
			return null;

		EventIds cmd = (EventIds) o;
		boolean retCode = false;

		o = param.getAsIs(ParamKeysEnum.PARAM.code);
		switch (cmd) {
		case CONFIG_PARTITION_SELECTED:
			if (o instanceof File)
				retCode = validCheck((File) o);

			param.set(ParamKeysEnum.RETCODE.code, retCode);
			return param;
		case CONFIG_FILE_SELECTED:
			OpenOcdConfiguration ocdConf = OpenOcdConfiguration.getinstance();
			File f = (File) o;
			if (f.exists()) {
				try {
					ocdConf.setDeviceConfig(f.getCanonicalPath());
					retCode = true;
				} catch (IOException e) { }
			}

			param.set(ParamKeysEnum.RETCODE.code, retCode);

			return param;
		case UPDATE:
			return param;
		default:
			return null;
		}

	}

	private boolean validCheck(File f) {
		List<PartitionInformation> configList = null;
		ServiceParams params = new ServiceParams();
		boolean retCode = true;
		try {
			configList = parseElement(f);
			if (configList == null || configList.isEmpty()) {
				service.getLogger().printf("%s: \n - %s\n",
						f.getPath(),
						"Illegal partition map file. should be check again");
				retCode = false;
			}
		} catch (IOException e) {
			service.getLogger().println(e.getMessage());
			retCode = false;
		}

		params.set(ParamKeysEnum.CMD.code,
				retCode ? EventIds.FUSING_INIT_MAP : EventIds.FUSING_ITEM_CLEAR);

		if (retCode) {
			IProperties prop;
			String category = null;
			try {
				prop = PropertiesFactory.createOrget(FlashLoaderService.APP_CONFIG);
				category = PropertiesKeys.getConfigCategory("partition");
				prop.set(category, f.getCanonicalPath());
			} catch (ConfigurationException e) {
				System.err.println(e.getMessage());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			params.set(ParamKeysEnum.PARAM.code, configList);
		}

		service.sendEvent(ModulesEnum.FUSING.code, params);

		return retCode;
	}

	@SuppressWarnings("resource")
	private List<PartitionInformation> parseElement(File f) throws IOException {
		BufferedReader bufferedReader = null;
		final List<PartitionInformation> configList
			= new ArrayList<PartitionInformation>();
		int step = 0;

		if (!f.exists() || !f.isFile() || !f.canRead())
			return null;
		/** */
//		cmd = null;
//		name = null;

		try {
			bufferedReader =
					new BufferedReader(new InputStreamReader(new FileInputStream(f)));
		} catch (FileNotFoundException e) {
			throw new IOException("file not found exception : " , e);
		}

		String line = null;
		String elem = null;
		while((line = bufferedReader.readLine()) != null) {

			if (line.startsWith(COMMENT_DELIMITER)) {
				continue;
			}

			line = line.replaceAll("\\s", DELIMITER);
			line = line.trim();

			StringTokenizer st = new StringTokenizer(line, DELIMITER);
			while(st.hasMoreTokens()) {
				String str = st.nextToken();
				if (str == null) {
					continue;
				}

				if (str.charAt(0) == START_BRACE) {
					step++;
					continue;
				}

				if (str.charAt(0) == END_BRACE) {
					step--;
					continue;
				}

				if (step == 0) {
//					/** get cmd & name */
//					if (this.cmd != null) {
//						name = str;
//					} else {
//						PartitionCmdsEnum c = PartitionCmdsEnum.get(str);
//						if (c == null)
//							return false;
//						cmd = c;
//					}
				} else if (step == 1) {
					elem = str;
				} else if (step == 2) {
					while(st.hasMoreTokens()) {
						String s = st.nextToken();
						if (s.charAt(0) == END_BRACE)
							break;

						str = str + DELIMITER + s;
					}

					ServiceParams param = payloadSpec.convertIntoServiceParams(str);
					if (param == null)
						return null;

					try {
						String desc = param.getAsString(PayloadKeysEnum.DESC.code);
						int startAddr = param.getAsInt(PayloadKeysEnum.START_ADDRESS.code);
						int size = param.getAsInt(PayloadKeysEnum.SECTION_SIZE.code);
						int ro = param.getAsInt(PayloadKeysEnum.READ_ONLY.code);

						configList.add(new PartitionInformation(elem, desc, startAddr, size, ro));
					} catch (RuntimeException e) {
						return null;
					}

					step = 1;
				}

			}
		}

		return configList;
	}

}
