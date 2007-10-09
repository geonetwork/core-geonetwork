//=============================================================================
//===	Copyright (C) 2001-2007 Food and Agriculture Organization of the
//===	United Nations (FAO-UN), United Nations World Food Programme (WFP)
//===	and United Nations Environment Programme (UNEP)
//===
//===	This program is free software; you can redistribute it and/or modify
//===	it under the terms of the GNU General Public License as published by
//===	the Free Software Foundation; either version 2 of the License, or (at
//===	your option) any later version.
//===
//===	This program is distributed in the hope that it will be useful, but
//===	WITHOUT ANY WARRANTY; without even the implied warranty of
//===	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
//===	General Public License for more details.
//===
//===	You should have received a copy of the GNU General Public License
//===	along with this program; if not, write to the Free Software
//===	Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
//===
//===	Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
//===	Rome - Italy. email: geonetwork@osgeo.org
//==============================================================================

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

