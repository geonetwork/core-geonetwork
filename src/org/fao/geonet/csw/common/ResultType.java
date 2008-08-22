package org.fao.geonet.csw.common;

import org.fao.geonet.csw.common.exceptions.InvalidParameterValueEx;

public enum ResultType {
	HITS("hits"), RESULTS("results"), VALIDATE("validate");

	//------------------------------------------------------------------------

	private ResultType(String type) { this.type = type;}

	//------------------------------------------------------------------------

	public String toString() { return type; }

	//------------------------------------------------------------------------

	public static ResultType parse(String type) throws InvalidParameterValueEx
	{
		if (type == null)						return HITS;
		if (type.equals(HITS.toString()))		return HITS;
		if (type.equals(RESULTS.toString())) 	return RESULTS;
		if (type.equals(VALIDATE.toString()))	return VALIDATE;

		throw new InvalidParameterValueEx("resultType", type);
	}

	//------------------------------------------------------------------------

	private String type;
}

