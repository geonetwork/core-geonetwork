/**
 * WMSMetadataURLImpl.java
 *
 */

package org.wfp.vam.intermap.kernel.map.mapServices.wms.schema.impl;

import org.jdom.Element;
import org.wfp.vam.intermap.kernel.map.mapServices.wms.schema.type.WMSMetadataURL;

/**
 * @author ETj
 */
public class WMSMetadataURLImpl extends WMSURLAbs implements WMSMetadataURL
{
	private MDTYPE _type;

	private WMSMetadataURLImpl()
	{}

	public static WMSMetadataURL newInstance()
	{
		return new WMSMetadataURLImpl();
	}

	public static WMSMetadataURL parse(Element emdu)
	{
		WMSMetadataURLImpl metadataUrl = new WMSMetadataURLImpl();

		WMSURLAbs.parse(metadataUrl, emdu);

		metadataUrl.setType(parseType(emdu.getAttributeValue("type")));

		return metadataUrl;
	}

	/**
	 * Method parseType
	 */
	private static WMSMetadataURLImpl.MDTYPE parseType(String type)
	{
		if("FGDC".equals(type)) return MDTYPE.FGDC; // wms1.1.1 FGDC CSDGM
		if("TC211".equals(type)) return MDTYPE.ISO19115; // wms1.1.1 ISO TC211 19115
		if("FGDC:1998".equals(type)) return MDTYPE.FGDC; 	// wms1.3.0
		if("ISO 19115:2003".equals(type)) return MDTYPE.ISO19115; // wms1.3.0

		System.out.println("WMSMetadataURL: ERROR: unknown metadata type '"+type+"'");

		return null;
	}

	/**
	 * Sets Type
	 */
	public void setType(MDTYPE type)
	{
		_type = type;
	}

	/**
	 * Returns Type
	 */
	public MDTYPE getType()
	{
		return _type;
	}
}

