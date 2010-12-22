//=============================================================================
//===	Copyright (C) 2001-2005 Food and Agriculture Organization of the
//===	United Nations (FAO-UN), United Nations World Food Programme (WFP)
//===	and United Nations Environment Programme (UNEP)
//===
//===	This library is free software; you can redistribute it and/or
//===	modify it under the terms of the GNU Lesser General Public
//===	License as published by the Free Software Foundation; either
//===	version 2.1 of the License, or (at your option) any later version.
//===
//===	This library is distributed in the hope that it will be useful,
//===	but WITHOUT ANY WARRANTY; without even the implied warranty of
//===	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
//===	Lesser General Public License for more details.
//===
//===	You should have received a copy of the GNU Lesser General Public
//===	License along with this library; if not, write to the Free Software
//===	Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
//===
//===	Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
//===	Rome - Italy. email: GeoNetwork@fao.org
//==============================================================================

package jeeves.server.dispatchers.guiservices;

import jeeves.constants.ConfigFile;
import jeeves.exceptions.BadInputEx;
import jeeves.server.context.ServiceContext;
import jeeves.utils.Util;
import jeeves.utils.XmlFileCacher;
import org.jdom.Element;

import java.io.File;

//=============================================================================

/** Loads and returns an xml file
  */

public class XmlFile implements GuiService
{
	private String  name;
	private String  file;
	private String  base;
	private String  language;
	private String  defaultLang;
	private boolean localized;
	private XmlFileCacher xmlCache;

	//---------------------------------------------------------------------------
	//---
	//--- Init
	//---
	//--------------------------------------------------------------------------

	public XmlFile(Element config, String defaultLanguage, boolean defaultLocalized) throws BadInputEx
	{
		defaultLang = defaultLanguage;

		name = Util.getAttrib(config, ConfigFile.Xml.Attr.NAME);
		file = Util.getAttrib(config, ConfigFile.Xml.Attr.FILE);
		base = Util.getAttrib(config, ConfigFile.Xml.Attr.BASE, "loc");

		language = config.getAttributeValue(ConfigFile.Xml.Attr.LANGUAGE);

		//--- handle localized attrib

		String local = config.getAttributeValue(ConfigFile.Xml.Attr.LOCALIZED);

		if (local == null)	localized = defaultLocalized;
		else localized = local.equals("true");

		xmlCache = null;
	}

	//---------------------------------------------------------------------------
	//---
	//--- Exec
	//---
	//--------------------------------------------------------------------------

	public Element exec(Element response, ServiceContext context) throws Exception
	{
		String lang = context.getLanguage();
		if (localized && !lang.equals(language)) xmlCache = null;
		language = lang;
		
		String path = context.getAppPath();
		String xmlFilePath;

		if (localized) xmlFilePath = path + base +"/"+ lang +"/"+ file;
		else xmlFilePath = path + file;

		if (xmlCache == null) xmlCache = new XmlFileCacher(new File(xmlFilePath));

		Element result = null;
		try {
			result = (Element)xmlCache.get().clone();
		} catch (Exception e) {
			e.printStackTrace();
			String xmlDefaultLangFilePath = path + base +"/"+ defaultLang +"/"+ file;
			xmlCache = new XmlFileCacher(new File(xmlDefaultLangFilePath));
			result = (Element)xmlCache.get().clone();
		}
		return result.setName(name);
	}

	//--------------------------------------------------------------------------

}

//=============================================================================

