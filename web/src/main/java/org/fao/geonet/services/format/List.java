//=============================================================================
//===	Copyright (C) 2008 Swisstopo
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

package org.fao.geonet.services.format;

import jeeves.interfaces.Service;
import jeeves.resources.dbms.Dbms;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.constants.Params;
import org.jdom.Content;
import org.jdom.Element;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

//=============================================================================

/**
 * Retrieves all format in the system if no name parameter
 * provided.
 *
 * @author fxprunayre
 * @see jeeves.interfaces.Service
 */

public class List implements Service {
    private static final Comparator<Content> COMPARATOR = new FormatsComparator();

    public void init(String appPath, ServiceConfig params) throws Exception {
    }


    public Element exec(Element params, ServiceContext context) throws Exception {
        Dbms dbms = (Dbms) context.getResourceManager().open(Geonet.Res.MAIN_DB);

        String name = params.getChildText(Params.NAME);
        Element el = null;

        if (name == null)
            el = dbms.select("SELECT * FROM Formats ORDER BY name, version");
        else {
            el = dbms.select("SELECT * FROM Formats WHERE "
                    + "name ilike '%" + name + "%' "
                    + "ORDER BY name, version");
        }

        if("validated".equalsIgnoreCase(params.getChildTextNormalize("order"))) {
            java.util.List<Content> children = new ArrayList<Content>(el.getChildren());

            for (Content child : children) {
                child.detach();
            }

            Collections.sort(children, COMPARATOR);
            el.addContent(children);
        }
        return el;
    }

    private static class FormatsComparator implements Comparator<Content> {

        public int compare(Content o1, Content o2) {
            if (!(o1 instanceof Element && o2 instanceof Element)) return 0;

            Element e1 = (Element) o1;
            Element e2 = (Element) o2;

            int points1 = 0;
            int points2 = 0;
            if ("n".equals(e1.getChildTextNormalize("validated"))) {
                points1 -= 10000;
            }
            if ("n".equals(e2.getChildTextNormalize("validated"))) {
                points2 -= 10000;
            }

            return points2 - points1;
        }
    }
}

//=============================================================================

