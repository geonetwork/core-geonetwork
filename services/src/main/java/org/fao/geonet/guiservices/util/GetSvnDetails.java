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

package org.fao.geonet.guiservices.util;

import jeeves.interfaces.Service;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;

import org.fao.geonet.utils.Log;

import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.SvnManager;
import org.jdom.Element;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;

import java.nio.file.Path;

//=============================================================================

/**
 * This service returns useful information about the subversion manager
 */
public class GetSvnDetails implements Service {
    public void init(Path appPath, ServiceConfig params) throws Exception {
    }

    // --------------------------------------------------------------------------
    // ---
    // --- Service
    // ---
    // --------------------------------------------------------------------------

    public Element exec(Element params, ServiceContext context) throws Exception {
        GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
        boolean svnManagerFound = false;
        Element root = new Element("a");

        try {
            SvnManager svnMan = gc.getBean(SvnManager.class);
            if (svnMan != null) {
                svnManagerFound = true;
            }
        } catch (NoSuchBeanDefinitionException e) {
            // No SVN manager found - SVN is not activated
            Log.debug(Geonet.SVN_MANAGER, "NoSuchBeanDefinitionException: No SVN manager found - SVN is not activated");
        }

        root.addContent(new Element(Geonet.Elem.ENABLED).setText(Boolean.toString(svnManagerFound)));

        return root;
    }
}
