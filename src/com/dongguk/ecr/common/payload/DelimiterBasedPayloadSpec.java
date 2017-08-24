package com.dongguk.ecr.common.payload;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;

import com.dongguk.ecr.framework.common.payload.IPayloadSpec;

/**
 *
 * @author jhun.ahn
 *
 */
public class DelimiterBasedPayloadSpec implements IPayloadSpec {

	private String delimiter = " ";
	private String suffix = "\r\n";

	private String stringForNullValue = " ";

	private List<DelimiterBasedParamSpec> paramSpecList =
			Collections.synchronizedList(new ArrayList<DelimiterBasedParamSpec>());

	private DelimiterBasedPayloadSpec() { }

	public static Constructor begin() {
		return new Constructor(new DelimiterBasedPayloadSpec());
	}

	/**
	 * Constructor class of DelimiterBasedPayloadSpec
	 *
	 * @author jhun.ahn
	 */
	public static final class Constructor {
		private final DelimiterBasedPayloadSpec THIS;

		private Constructor(DelimiterBasedPayloadSpec obj) {
			THIS = obj;
		}

		public Constructor setNullDelimiter(String delimiter) {
			THIS.stringForNullValue = delimiter;
			return this;
		}

		/**
		 * delimiter.
		 * default: " "
		 *
		 * ex) " ", "\t"
		 *
		 * @param delimiter
		 * @return
		 */
		public Constructor setDelimiter(String delimiter) {
			THIS.delimiter = delimiter;
			return this;
		}

		/**
		 * suffix
		 * default: "\r\n"
		 *
		 * ex) "\n", "\r\n"
		 *
		 * @param suffix
		 * @return
		 */
		public Constructor setSuffix(String suffix) {
			THIS.suffix = suffix;
			return this;
		}

		/**
		 * This field is append string if value is null, when created payload
		 * default: " "
		 *
		 * example) " ", "NULL"
		 *
		 * @param stringForNullValue
		 * @return
		 */
		public Constructor setStringForNullValue(String stringForNullValue) {
			THIS.stringForNullValue = stringForNullValue;
			return this;
		}

		/**
		 * {@link DelimiterBasedStringParamSpec} add parameter of 'type'
		 *
		 * @param name
		 * @param type
		 * @return
		 */
		public Constructor addParam(String name, ParamTypesEnum type) {
			THIS.paramSpecList.add(
					new DelimiterBasedParamSpec(name, type));
			return this;
		}

		/**
		 * Builder to PayloadSpec {@link DelimiterBasedStringPayloadSpec}
		 *
		 * @return
		 */
		public DelimiterBasedPayloadSpec end() {
			return THIS;
		}
	}

	/**
	 * @param numericString
	 * @return	radix
	 */
	private int getRadixOfNumericString(String numericString) {
		if ("0".equals(numericString)) {
			return 10;
		}
		else if (numericString.isEmpty())	{
			return 0;
		}
		else if (numericString.startsWith("0x")) {
			return 16;
		}
		else if (numericString.startsWith("0")) {
			return 8;
		}
		else if ( (numericString.charAt(0) >= '1' && numericString.charAt(0) <= '9')
				|| (numericString.charAt(0) == '-') ){
			return 10;
		}
		else {
			throw new RuntimeException("Invalid numeric string: " + numericString);
		}
	}

	/**
	 * check prefix("0x", "0") and remove prefix
	 *
	 * @param numericString
	 * @param radix
	 * @return
	 */
	private String excludeRadixPrefix(String numericString, int radix) {
		if (numericString.isEmpty())
			return "0";

		switch (radix) {
		case 10:
			/** do nothing */
			return numericString;
		case 16:
			/** remove "0x" */
			return numericString.substring(2);
		case 8:
			/** remove "0" */
			return numericString.substring(1);
		default:
			throw new RuntimeException("Invalid radix: " + radix);
		}
	}

	/**
	 * Configuration exception maker
	 * @param paramName
	 * @param paramType
	 */
	private void throwConfigurationError(String paramName, ParamTypesEnum paramType) {
		String msg = String.format("paramName=%s, type=%s", paramName, paramType.toString());
		throw new RuntimeException(msg);
	}

	@Override
	public ServiceParams convertIntoServiceParams(Object payload) {
		if (!(payload instanceof String)) {
			return null;
		}

		String data = (String) payload;
		ServiceParams params = new ServiceParams();
		StringTokenizer st = new StringTokenizer(data, this.delimiter + this.suffix);
		int specListIndex = 0;

		while(st.hasMoreTokens()) {
			try {
				DelimiterBasedParamSpec paramSpec = paramSpecList.get(specListIndex);
				String paramName = paramSpec.getName();
				ParamTypesEnum paramType = paramSpec.getType();
				String str = st.nextToken();

				Object value = null;

				if (str != null) {
					if (str.trim().isEmpty() &&
							(paramType != ParamTypesEnum.STRING)) {
						value = 0;
					} else {
						/* TODO : add to trimmer */
						str = str.trim();

						int radix;
						switch (paramType) {
						case BYTE:
							radix = getRadixOfNumericString(str);
							value = Byte.parseByte(excludeRadixPrefix(str, radix), radix);
							break;
						case SHORT:
							radix = getRadixOfNumericString(str);
							value = Short.parseShort(excludeRadixPrefix(str, radix), radix);
							break;
						case INT:
						case INT_HEX:
						case INT_OCTAL:
							radix = getRadixOfNumericString(str);
							Long convValue = Long.parseLong(excludeRadixPrefix(str, radix), radix);
							value = convValue.intValue();
							break;
						case LONG:
						case LONG_HEX:
						case LONG_OCTAL:
							radix = getRadixOfNumericString(str);
							value = Long.parseLong(excludeRadixPrefix(str, radix), radix);
							break;
						case FLOAT:
							value = Float.parseFloat(str);
							break;
						case DOUBLE:
							value = Double.parseDouble(str);
							break;
						case STRING:
							/** if value is string_mixture */
							int quotes = 0;
							while(st.hasMoreTokens()) {

								if (quotes == 0 && str.startsWith("\"")) {
									str = str.replaceFirst("\"", "");
									quotes = 1;
								}

								if (quotes == 1 && str.endsWith("\"")) {
									str = str.replaceFirst("\"", "");
									quotes = 2;
								}

								if (quotes == 2) {
									quotes = 0;
									value = str;
									break;
								} else {
									str = str + this.delimiter + st.nextToken();
								}
							}
							break;
						default:
							throwConfigurationError(paramName, paramType);
						}
					}
				}

				params.set(paramName, value);
				specListIndex++;

			} catch (RuntimeException e) {
				break;
			}
		}

		return params;
	}

	/**
	 * parse the String into StringBuilder
	 * @param sb
	 * @param i
	 * @param str
	 */
	private void appendStringAndDelimiterToStringBuilder(
			StringBuilder sb, int i, String str) {
		sb.append(str);
		if (i < paramSpecList.size() - 1) {
			sb.append(this.delimiter);
		}
		else {
			sb.append(this.suffix);
		}
	}

	@Override
	public Object convertIntoPayload(ServiceParams params) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < paramSpecList.size(); i++) {
			DelimiterBasedParamSpec paramSpec = paramSpecList.get(i);
			String paramName = paramSpec.getName();
			ParamTypesEnum paramType = paramSpec.getType();
			Object paramValue = params.getAsIs(paramName);

			if (paramValue == null) {
				appendStringAndDelimiterToStringBuilder(sb,  i, stringForNullValue);
			}
			else
			{
				switch (paramType) {
				case BYTE:
				case SHORT:
				case INT:
				case LONG:
				case FLOAT:
				case DOUBLE:
				case STRING:
					appendStringAndDelimiterToStringBuilder(sb, i, paramValue.toString());
					break;
				case BYTE_HEX:
				case INT_HEX:
					sb.append("0x");
					appendStringAndDelimiterToStringBuilder(
							sb, i,
							Integer.toHexString((Integer)paramValue));
					break;
				case LONG_HEX:
					sb.append("0x");
					appendStringAndDelimiterToStringBuilder(
							sb, i,
							Long.toHexString((Integer)paramValue));
				case INT_OCTAL:
					sb.append("0");
					appendStringAndDelimiterToStringBuilder(
							sb, i,
							Integer.toOctalString((Integer)paramValue));
					break;
				case LONG_OCTAL:
					sb.append("0");
					appendStringAndDelimiterToStringBuilder(
							sb, i,
							Long.toOctalString((Integer)paramValue));
					break;
				}
			}
		}

		return sb.toString();
	}

}
