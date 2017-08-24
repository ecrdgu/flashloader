package com.dongguk.ecr.common.payload;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author jhun.ahn
 *
 */
public class ServiceParams {
	private Map<String, Object> valueMap = new HashMap<String, Object>();

	public ServiceParams clear() {
		valueMap.clear();
		return this;
	}

	public ServiceParams duplicate() {
		ServiceParams params = new ServiceParams();
		params.valueMap = new HashMap<String, Object>(this.valueMap);
		return params;
	}

	/**
	 *
	 * @param name
	 * @param value
	 * @return
	 */
	public ServiceParams set(String name, Object value) {
		valueMap.put(name, value);
		return this;
	}

	public String getAsString(String name) {
		Object val = valueMap.get(name);
		if (val instanceof String) {
			return (String) val;
		} else {
			/** FIXME jhun.ahn : null check!! */
			if (val != null) {
				return val.toString();
			} else {
				return null;
			}
		}
	}

	public boolean getAsBoolean(String name) {
		Object val = valueMap.get(name);
		if (val instanceof Boolean) {
			return ((Boolean) val).booleanValue();
		} else if (val instanceof String) {
			return Boolean.parseBoolean((String) val);
		} else {
			// TODO Define new exception type.
			throw new RuntimeException(
					String.format("Can not convert data of (name=[%s], type=[%s], value=[%s]) into int type.", name,
							val.getClass().toString(), val));
		}
	}

	public byte getAsByte(String name) {
		Object val = valueMap.get(name);
		if (val instanceof Byte) {
			return ((Byte) val).byteValue();
		} else if (val instanceof String) {
			return Byte.parseByte((String) val);
		} else {
			// TODO Define new exception type.
			throw new RuntimeException(
					String.format("Can not convert data of (name=[%s], type=[%s], value=[%s]) into int type.", name,
							val.getClass().toString(), val));
		}
	}

	public byte[] getAsByteArray(String name) {
		Object val = valueMap.get(name);
		if (val == null) {
			return null;
		}

		if (val instanceof byte[]) {
			return (byte[]) val;
		} else {
			// TODO Define new exception type.
			throw new RuntimeException(
					String.format("Can not convert data of (name=[%s], type=[%s], value=[%s]) into int type.", name,
							val.getClass().toString(), val));
		}
	}

	public short getAsShort(String name) {
		Object val = valueMap.get(name);
		if (val instanceof Short) {
			return ((Short) val).shortValue();
		} else if (val instanceof Byte) {
			return ((Byte) val).shortValue();
		} else if (val instanceof String) {
			return Short.parseShort((String) val);
		} else {
			// TODO Define new exception type.
			throw new RuntimeException(
					String.format("Can not convert data of (name=[%s], type=[%s], value=[%s]) into int type.", name,
							val.getClass().toString(), val));
		}
	}

	public int getAsInt(String name) {
		Object val = valueMap.get(name);
		if (val instanceof Integer) {
			return (Integer) val;
		} else if (val instanceof Short) {
			return ((Short) val).intValue();
		} else if (val instanceof Byte) {
			return ((Byte) val).intValue();
		} else if (val instanceof String) {
			return Integer.parseInt((String) val);
		} else if (val instanceof Double) {
			double d = (Double) val;
			return (int) d;
		} else {
			// TODO Define new exception type.
			throw new RuntimeException(
					String.format("Can not convert data of (name=[%s], type=[%s], value=[%s]) into int type.", name,
							val.getClass().toString(), val));
		}
	}

	public float getAsFloat(String name) {
		Object val = valueMap.get(name);
		if (val instanceof Float) {
			return (Float) val;
		} else if (val instanceof String) {
			return Float.parseFloat((String) val);
		} else {
			// TODO Define new exception type.
			throw new RuntimeException(
					String.format("Can not convert data of (name=[%s], type=[%s], value=[%s]) into float type.", name,
							val.getClass().toString(), val));
		}
	}

	public double getAsDouble(String name) {
		Object val = valueMap.get(name);
		if (val instanceof Double) {
			return (Double) val;
		} else if (val instanceof Float) {
			return ((Float) val).doubleValue();
		} else if (val instanceof String) {
			return Double.parseDouble((String) val);
		} else if (val instanceof Integer) {
			double d = (Double) val;
			return d;
		} else {
			// TODO Define new exception type.
			throw new RuntimeException(
					String.format("Can not convert data of (name=[%s], type=[%s], value=[%s]) into double type.", name,
							val.getClass().toString(), val));
		}
	}

	/**
	 * @param name
	 * @return
	 */
	public Object getAsIs(String name) {
		return valueMap.get(name);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("ServiceParams[");
		Set<String> keySet = valueMap.keySet();

		int idx = 0;

		for (String name : keySet) {
			Object value = valueMap.get(name);

			if (idx > 0)
				sb.append(", ");

			sb.append(name).append("=[").append(value).append("]");

			idx++;
		}
		sb.deleteCharAt(sb.length() - 1);
		sb.append("]");
		return sb.toString();
	}
}
