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

package org.fao.geonet.services.metadata;


import java.nio.file.Path;
import java.util.List;

import javax.annotation.Nonnull;

import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.datamanager.IMetadataManager;
import org.fao.geonet.repository.Updater;
import org.fao.geonet.services.NotInReadOnlyModeService;
import org.fao.geonet.services.Utils;
import org.jdom.Element;

import jeeves.constants.Jeeves;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;

/**
 * Stores all operations allowed for a metadata. Called by the metadata.admin service.
 */
@Deprecated
public class UpdateCategories extends NotInReadOnlyModeService {

    /**
     *
     * @param appPath
     * @param params
     * @throws Exception
     */
    public void init(Path appPath, ServiceConfig params) throws Exception {
    }

    /**
     *
     * @param params
     * @param context
     * @return
     * @throws Exception
     */
    public Element serviceSpecificExec(Element params, ServiceContext context) throws Exception {
        GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);

        DataManager dataMan = gc.getBean(DataManager.class);
        String id = Utils.getIdentifierFromParameters(params, context);

        //--- check access
        int iLocalId = Integer.parseInt(id);
        if (!dataMan.existsMetadata(iLocalId))
            throw new IllegalArgumentException("Metadata not found --> " + id);

        //--- remove old operations
        context.getBean(IMetadataManager.class).update(iLocalId, new Updater<Metadata>() {
            @Override
            public void apply(@Nonnull Metadata entity) {
                entity.getMetadataCategories().clear();
            }
        });

        //--- set new ones
        @SuppressWarnings("unchecked")
        List<Element> list = params.getChildren();

        for (Element el : list) {
            String name = el.getName();

            if (name.startsWith("_"))
                dataMan.setCategory(context, id, name.substring(1));
        }

        //--- index metadata
        dataMan.indexMetadata(id, true, null);

        //--- return id for showing
        return new Element(Jeeves.Elem.RESPONSE).addContent(new Element(Geonet.Elem.ID).setText(id));
    }
}
