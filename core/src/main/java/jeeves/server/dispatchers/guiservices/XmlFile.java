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
import jeeves.server.context.ServiceContext;

import org.fao.geonet.Util;
import org.fao.geonet.exceptions.BadInputEx;
import org.fao.geonet.utils.IO;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.springframework.context.ApplicationContext;

import java.net.URI;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;

//=============================================================================

/**
 * Loads and returns an xml file
 */

public class XmlFile implements GuiService {
    private final String name;
    private final String file;
    private final Path base;
    private final String language;
    private final String defaultLang;
    private final boolean localized;

    //---------------------------------------------------------------------------
    //---
    //--- Init
    //---
    //--------------------------------------------------------------------------

    public XmlFile(Element config, String defaultLanguage, boolean defaultLocalized) throws BadInputEx {
        defaultLang = defaultLanguage;

        name = Util.getAttrib(config, ConfigFile.Xml.Attr.NAME);
        file = Util.getAttrib(config, ConfigFile.Xml.Attr.FILE);

        final String loc = Util.getAttrib(config, ConfigFile.Xml.Attr.BASE, "loc");
        Path basePath;
        try {
            basePath = IO.toPath(new URI(loc));
        } catch (URISyntaxException | IllegalArgumentException e) {
            basePath = IO.toPath(loc);
        }
        base = basePath;

        language = config.getAttributeValue(ConfigFile.Xml.Attr.LANGUAGE);

        //--- handle localized attrib

        String local = config.getAttributeValue(ConfigFile.Xml.Attr.LOCALIZED);

        if (local == null) localized = defaultLocalized;
        else localized = local.equals("true");
    }

    //---------------------------------------------------------------------------
    //---
    //--- Exec
    //---
    //--------------------------------------------------------------------------

    public Element exec(Element response, ServiceContext context) throws Exception {
        String lang = context.getLanguage();

        return getXml(context.getApplicationContext(), lang, true);
    }

    public Element getXml(ApplicationContext context, String lang, boolean makeCopy) throws JDOMException, IOException {
        String preferedLanguage = language;
        if (localized || preferedLanguage == null) preferedLanguage = lang;
        if (preferedLanguage == null) preferedLanguage = defaultLang;

        Element element = context.getBean(XmlCacheManager.class).get(context, localized, base, file, preferedLanguage, defaultLang, makeCopy);
        element.setName(name);
        return element;
    }
}

