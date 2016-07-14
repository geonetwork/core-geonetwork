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

package org.fao.geonet.services.banner;

import jeeves.constants.Jeeves;
import jeeves.interfaces.Service;
import jeeves.server.ServiceConfig;
import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;

import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.setting.SettingManager;
import org.jdom.Element;

import java.nio.file.Path;

//=============================================================================

/**
 * This service returns all information needed to build the banner with XSL
 */
@Deprecated
public class Get implements Service {
    public void init(Path appPath, ServiceConfig params) throws Exception {
    }

    //--------------------------------------------------------------------------
    //---
    //--- Exec
    //---
    //--------------------------------------------------------------------------

    public Element exec(Element params, ServiceContext context) throws Exception {
        Element res = new Element(Jeeves.Elem.RESPONSE)
            .addContent(getStack(context))
            .addContent(getUserInfo(context))
            .addContent(getShibUse(context));
        //--- add the invert tag if the banner must be flipped
        //--- used for the arabic language

        if (getInvertValue(context.getLanguage()))
            res.addContent(new Element("invert"));

        return res;
    }

    //--------------------------------------------------------------------------
    //--- Stack building
    //--------------------------------------------------------------------------

    private Element getStack(ServiceContext srvContext) {
        Element stackElem = new Element("stack");
        String service = srvContext.getService();
//		Element mainSearchElem = (Element) srvContext.getUserSession().getProperty(Geonet.Session.MAIN_SEARCH);

        //-----------------------------------------------------------------------
        //--- build stack according with current service

        //--- we are in the main.search service

        stackElem.addContent(new Element("current").addContent(service));
        stackElem.addContent(new Element("language").addContent(srvContext.getLanguage()));

		/* RGFIX: should check session and modality (local/remote)
        if (mainSearchElem != null && !mainSearchElem.getChildText(Geonet.SRV_MAIN_RESULT_TEXT).equals(""))
			stackElem.addContent(new Element("result"));
		*/
        return stackElem;
    }

    //--------------------------------------------------------------------------
    //--- Buttons building
    //--------------------------------------------------------------------------

    private Element getUserInfo(ServiceContext srvContext) {
        UserSession session = srvContext.getUserSession();

        return new Element("user")
            .addContent(new Element("username").setText(session.getUsername()))
            .addContent(new Element("name").setText(session.getName()))
            .addContent(new Element("surname").setText(session.getSurname()));
    }

    /**
     * Create an element "shib/use" describing whether shibboleth login is being used.
     *
     * @param srvContext The Jeeves service context.
     * @return Shib use element.
     */
    private Element getShibUse(ServiceContext srvContext) {
        GeonetContext gc = (GeonetContext) srvContext.getHandlerContext(Geonet.CONTEXT_NAME);
        SettingManager sm = gc.getBean(SettingManager.class);
        String prefix = "system/shib";

        String use = sm.getValue(prefix + "/use");

        return new Element("shib")
            .addContent(new Element("use").setText(use));
    }

    //--------------------------------------------------------------------------

    private boolean getInvertValue(String lang) {
        return lang.equals("ar");
    }
}

//=============================================================================

