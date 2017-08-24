package com.dongguk.ecr.framework.common.payload;

import com.dongguk.ecr.common.payload.ServiceParams;

/**
 * IPayloadSpec
 * @author jhun.ahn
 *
 */
public interface IPayloadSpec {

	/**
	 * Convert the given payload into ServiceParams with this specification.
	 *
	 * @param payload
	 * @return ServiceParams
	 */
	public ServiceParams convertIntoServiceParams(Object payload);

	/**
	 * Convert the given ServiceParams into payload with this specification.
	 *
	 * @param params
	 * @return payload
	 */
	public Object convertIntoPayload(ServiceParams params);
}
