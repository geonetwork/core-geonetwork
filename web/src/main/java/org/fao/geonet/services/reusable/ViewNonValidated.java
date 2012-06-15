//==============================================================================
//===	Copyright (C) 2001-2008 Food and Agriculture Organization of the
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

package org.fao.geonet.services.reusable;

import jeeves.interfaces.Service;
import jeeves.resources.dbms.Dbms;
import jeeves.server.ServiceConfig;
import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;
import jeeves.utils.Util;

import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.reusable.*;
import org.jdom.Element;

/**
 * Makes a list of all the non-validated elements
 *
 * @author jeichar
 */
public class ViewNonValidated implements Service
{

    public Element exec(Element params, ServiceContext context) throws Exception
    {
        String type = Util.getParam(params, "type");

        GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
        Dbms dbms = (Dbms) context.getResourceManager().open(Geonet.Res.MAIN_DB);
        UserSession session = context.getUserSession();
        String appPath = context.getAppPath();
        String baseUrl = Utils.mkBaseURL(context.getBaseUrl(), gc.getSettingManager());
        String language = context.getLanguage();

        if (type.equals("deleted")) {
            return DeletedObjects.list(dbms);
        }

        ReplacementStrategy strategy;
        switch (ReusableTypes.valueOf(type))
        {
        case contacts:
            strategy = new ContactsStrategy(dbms, appPath, baseUrl, language, context.getSerialFactory());
            break;
        case extents:
            strategy = new ExtentsStrategy(baseUrl, appPath, gc.getExtentManager(), language);
            break;
        case formats:
            strategy = new FormatsStrategy(dbms, appPath, baseUrl, language, context.getSerialFactory());
            break;
        case keywords:
            strategy = new KeywordsStrategy(gc.getThesaurusManager(), appPath, baseUrl, language);
            break;

        default:
            throw new IllegalArgumentException(type + " is not a reusable object type");
        }

        return strategy.findNonValidated(session);
    }

    public void init(String appPath, ServiceConfig params) throws Exception
    {
    }

}
