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
import jeeves.utils.Log;
import jeeves.utils.Util;
import jeeves.utils.XmlFileCacher;
import org.jdom.Element;

import javax.servlet.ServletContext;
import java.io.File;
import java.util.WeakHashMap;

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
	private WeakHashMap<String,XmlFileCacher> xmlCaches = new WeakHashMap<String, XmlFileCacher>(4);

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

	public synchronized Element exec(Element response, ServiceContext context) throws Exception
	{
		String lang = context.getLanguage();

        if(localized || language == null) language = lang;
        if(language == null) language = defaultLang;

		String appPath = context.getAppPath();
		String xmlFilePath;

        boolean isBaseAbsolutePath = (new File(base)).isAbsolute();
        String rootPath = (isBaseAbsolutePath) ? base : appPath + base;

		if (localized) xmlFilePath = rootPath + File.separator + lang +File.separator + file;
		else xmlFilePath = appPath + file;

        ServletContext servletContext = null;
        if(context.getServlet() != null) {
            servletContext = context.getServlet().getServletContext();
        }

        XmlFileCacher xmlCache = xmlCaches.get(language);
		if (xmlCache == null){
            xmlCache = new XmlFileCacher(new File(xmlFilePath),servletContext,appPath);
            xmlCaches.put(language, xmlCache);
        }

		Element result;
		try {
			result = (Element)xmlCache.get().clone();
		} catch (Exception e) {
            Log.error(Log.RESOURCES, "Error cloning the cached data");
			String xmlDefaultLangFilePath = rootPath + File.separator + defaultLang + File.separator + file;
			xmlCache = new XmlFileCacher(new File(xmlDefaultLangFilePath),servletContext, appPath);
            xmlCaches.put(language, xmlCache);
            result = (Element)xmlCache.get().clone();
		}
		return result.setName(name);
	}
}

