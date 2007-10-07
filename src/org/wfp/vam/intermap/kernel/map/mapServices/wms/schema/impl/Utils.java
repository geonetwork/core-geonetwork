/**
 * Utils.java
 *
 * @author ETj
 */

package org.wfp.vam.intermap.kernel.map.mapServices.wms.schema.impl;

/**
 * @author ETj
 */
public class Utils
{

	/**
	 * Method getBooleanAttrib
	 */
	public static boolean getBooleanAttrib(String attributeValue, boolean def)
	{
		if(attributeValue == null)
			return def;
		if("0".equals(attributeValue) || "false".equals(attributeValue))
			return false;
		if("1".equals(attributeValue) || "true".equals(attributeValue))
			return true;
		System.out.println("*** Error while parsing boolean attribute value '"+def+"'");
		return def;
	}
}

