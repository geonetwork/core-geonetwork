package org.fao.geonet.csw.common;

import org.fao.geonet.csw.common.exceptions.InvalidParameterValueEx;

/**
 * TODO javadoc.
 */
public enum ElementSetName {
	BRIEF("brief"), SUMMARY("summary"), FULL("full");

	//------------------------------------------------------------------------

	private ElementSetName(String setName) { this.setName = setName;}

	//------------------------------------------------------------------------

	public String toString() { return setName; }

	//------------------------------------------------------------------------

	public static ElementSetName parse(String setName) throws InvalidParameterValueEx
	{
		if (setName == null)
		    return SUMMARY; // required by CSW 2.0.2
		if (setName.equals(BRIEF  .toString()))	return BRIEF;
		if (setName.equals(SUMMARY.toString())) 	return SUMMARY;
		if (setName.equals(FULL   .toString()))	return FULL;

		throw new InvalidParameterValueEx("elementSetName", setName);
	}

	//------------------------------------------------------------------------

	private String setName;
}