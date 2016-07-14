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

package org.fao.geonet.services.category;

import jeeves.constants.Jeeves;
import jeeves.interfaces.Service;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;

import org.fao.geonet.constants.Params;
import org.fao.geonet.domain.MetadataCategory;
import org.fao.geonet.repository.MetadataCategoryRepository;
import org.jdom.Element;

import java.nio.file.Path;

//=============================================================================

/**
 * Returns a specific category given its id
 */
@Deprecated
public class Get implements Service {
    public void init(Path appPath, ServiceConfig params) throws Exception {
    }

    //--------------------------------------------------------------------------
    //---
    //--- Service
    //---
    //--------------------------------------------------------------------------

    public Element exec(Element params, ServiceContext context) throws Exception {
        String id = params.getChildText(Params.ID);

        if (id == null) {
            return new Element(Jeeves.Elem.RESPONSE);
        }

        final MetadataCategory category = context.getBean(MetadataCategoryRepository.class).findOne(Integer.valueOf(id));

        if (category == null) {
            return new Element(Jeeves.Elem.RESPONSE);
        }
        return new Element(Jeeves.Elem.RESPONSE).addContent(category.asXml());
    }
}

//=============================================================================

