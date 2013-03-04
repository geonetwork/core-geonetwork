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

import java.util.WeakHashMap;

import jeeves.constants.ConfigFile;
import jeeves.exceptions.BadInputEx;
import jeeves.server.context.ServiceContext;
import jeeves.utils.Util;
import jeeves.utils.XmlFileCacher;

import org.jdom.Element;

//=============================================================================

/** Loads and returns an xml file
  */

public class XmlFile implements GuiService
{
	private final String  name;
	private final String  file;
	private final String  base;
	private final String  language;
	private final String  defaultLang;
	private final boolean localized;

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
	}

	//---------------------------------------------------------------------------
	//---
	//--- Exec
	//---
	//--------------------------------------------------------------------------

	public Element exec(Element response, ServiceContext context) throws Exception
	{

        String lang = context.getLanguage();

        String preferedLanguage = language;
        if(localized || preferedLanguage  == null) preferedLanguage = lang;
        if(preferedLanguage == null) preferedLanguage = defaultLang;

        Element element = context.getXmlCacheManager().get(context, localized, base, file, preferedLanguage, defaultLang);
        element.setName(name);
        return element;
	}
}

