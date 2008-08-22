package org.fao.geonet.csw.common;

import org.fao.geonet.csw.common.exceptions.InvalidParameterValueEx;
import org.fao.geonet.csw.common.exceptions.MissingParameterValueEx;

public enum ConstraintLanguage
{
	CQL("CQL_TEXT"), FILTER("FILTER");

	//------------------------------------------------------------------------

	private ConstraintLanguage(String language) { this.language = language;}

	//------------------------------------------------------------------------

	public String toString() { return language; }

	//------------------------------------------------------------------------

	public static ConstraintLanguage parse(String language) throws MissingParameterValueEx,
																						InvalidParameterValueEx
	{
		if (language == null)
			throw new MissingParameterValueEx("constraintLanguage");

		if (language.equals(CQL.toString()))		return CQL;
		if (language.equals(FILTER.toString()))	return FILTER;

		throw new InvalidParameterValueEx("constraintLanguage", language);
	}

	//------------------------------------------------------------------------

	private String language;
}
