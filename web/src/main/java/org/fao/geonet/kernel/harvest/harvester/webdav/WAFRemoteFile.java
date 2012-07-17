//=============================================================================
//===	Copyright (C) 2001-2012 Food and Agriculture Organization of the
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

package org.fao.geonet.kernel.harvest.harvester.webdav;

import jeeves.utils.PasswordUtil;
import jeeves.utils.Xml;
import org.apache.commons.httpclient.HttpException;
import org.apache.webdav.lib.WebdavResource;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.SchemaManager;
import org.fao.geonet.util.Sha1Encoder;
import org.jdom.Element;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

//=============================================================================

class WAFRemoteFile implements RemoteFile {
	
	public class WXS{
		public static final String WMS = "SERVICE=WMS";
		public static final String WCS = "SERVICE=WCS";
		public static final String WFS = "SERVICE=WFS";
		public static final String WPS = "SERVICE=WPS";
	}
	
	//---------------------------------------------------------------------------
	//---
	//--- Constructor
	//---
	//---------------------------------------------------------------------------

	public WAFRemoteFile(String path) {
		this.path = path;
	}

	//---------------------------------------------------------------------------
	//---
	//--- RemoteFile interface
	//---
	//---------------------------------------------------------------------------

	public Element getMetadata(SchemaManager  schemaMan) throws Exception {
		try {	
			String type = WAFRetriever.getFileType(this.path);
			if(type.equals(WAFRetriever.type_GetCapabilities))
				return getMdFromService(path, schemaMan);
			else if(type.equals(WAFRetriever.type_xml))
				return Xml.loadFile(new URL(path));		
			else 
				return null;
		}
		catch (HttpException x) {
			throw new Exception("HTTPException : " + x.getMessage());
		}
	}


	
	//---------------------------------------------------------------------------
	//---
	//--- Get Metadata from GetCapabilities file (Service)
	//---
	//---------------------------------------------------------------------------	
	
	private Element getMdFromService(String url, SchemaManager  schemaMan) throws Exception
	{
		Element el = null;
		String styleSheet = getStyleSheet(url, schemaMan);
		if(styleSheet == null)
			return null;
        try {
			Element xml = Xml.loadFile(new URL(url));
			
			// md5 the full capabilities URL
			String uuid = Sha1Encoder.encodeString (url); // is the service identifier
			
			Map<String, String> param = new HashMap<String, String>();
			param.put("uuid", uuid);
			
			el = Xml.transform (xml, styleSheet, param);		
		} catch (HttpException x) {
			// TODO Auto-generated catch block
			throw new Exception("HTTPException : " + x.getMessage());
		} 
		return el;
	}
	
	private String getStyleSheet(String url, SchemaManager  schemaMan)
	{
		String serviceType = getServiceType(url);
		if(serviceType == null)
			return null;
		String styleSheet = schemaMan.getSchemaDir(outputSchema) + 
			Geonet.Path.CONVERT_STYLESHEETS
			+ "/OGCWxSGetCapabilitiesto19119/" 
			+ "/OGC"
			+ serviceType
			+ "GetCapabilities-to-ISO19119_ISO19139.xsl";
		return styleSheet;
	}
	
	//---------------------------------------------------------------------------
	//---
	//--- Get Service Type
	//---
	//---------------------------------------------------------------------------
	
	private String getServiceType(String url)
	{
		if(url.toUpperCase().contains(WXS.WMS))
			return "WMS";
		else if(url.toUpperCase().contains(WXS.WCS))
			return "WCS";
		else if(url.toUpperCase().contains(WXS.WFS))
			return "WFS";
		else if(url.toUpperCase().contains(WXS.WPS))
			return "WPS";
		else 
			return null;
	}
	
	//---------------------------------------------------------------------------

	public boolean isMoreRecentThan(String localChangeDate) {
		return true;
	}

	//---------------------------------------------------------------------------
	//---
	//--- Variables
	//---
	//---------------------------------------------------------------------------
	
	private String path;
	private static String outputSchema = "iso19139";
	
	public String getPath() {
		return path;
	}

	
	public String getChangeDate() {
		return null;
	}
	
}

//=============================================================================
