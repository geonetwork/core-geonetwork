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

package jeeves.services.session;

import jeeves.constants.Jeeves;
import jeeves.interfaces.Service;
import jeeves.server.ServiceConfig;
import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;

import org.jdom.Element;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;

//=============================================================================

/**
 * Stores input fields in the session
 */

public class Put implements Service {
    String groupName;
    HashSet<String> inFields;

    //--------------------------------------------------------------------------
    //---
    //--- Init
    //---
    //--------------------------------------------------------------------------

    public void init(Path appPath, ServiceConfig params) throws Exception {
        groupName = params.getMandatoryValue(Jeeves.Config.GROUP);
        List<Element> l = params.getChildren(Jeeves.Config.IN_FIELDS, Jeeves.Config.FIELD);
        if (l != null) {
            inFields = new HashSet<String>();
            for (Element field : l) {
                inFields.add(field.getName());
            }
        }
    }

    //--------------------------------------------------------------------------
    //---
    //--- Service
    //---
    //--------------------------------------------------------------------------

    @SuppressWarnings("unchecked")
    public Element exec(Element params, ServiceContext context) throws Exception {
        UserSession session = context.getUserSession();

        Hashtable<String, Element> group = (Hashtable<String, Element>) session.getProperty(groupName);
        if (group == null) {
            group = new Hashtable<String, Element>();
        }
        for (Element child : (List<Element>) params.getChildren()) {
            if (inFields == null || inFields.contains(child.getName())) {
                group.put(child.getName(), child);
            }
        }
        session.setProperty(groupName, group);

        return params;
    }
}

//=============================================================================

