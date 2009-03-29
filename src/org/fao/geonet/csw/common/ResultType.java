package org.fao.geonet.csw.common;

import org.fao.geonet.csw.common.exceptions.InvalidParameterValueEx;

public enum ResultType {
	HITS("hits"), RESULTS("results"), RESULTS_WITH_SUMMARY("results_with_summary"), VALIDATE("validate");

	//------------------------------------------------------------------------

	private ResultType(String type) { this.type = type;}

	//------------------------------------------------------------------------

	public String toString() { return type; }

	//------------------------------------------------------------------------

	public static ResultType parse(String type) throws InvalidParameterValueEx
	{
		if (type == null)						return HITS;
		for (ResultType rtype : ResultType.values()) {
			if (type.equals(rtype.toString()))
				return rtype;
		}

		throw new InvalidParameterValueEx("resultType", type);
	}

	//------------------------------------------------------------------------

	private String type;
}

