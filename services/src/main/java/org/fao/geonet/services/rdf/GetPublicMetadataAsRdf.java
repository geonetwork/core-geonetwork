/*
 * Copyright (C) 2001-2016 Food and Agriculture Organization of the
 * United Nations (FAO-UN), United Nations World Food Programme (WFP)
 * and United Nations Environment Programme (UNEP)
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or (at
 * your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
 *
 * Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
 * Rome - Italy. email: geonetwork@osgeo.org
 */

package org.fao.geonet.services.rdf;

import jeeves.interfaces.Service;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;

import org.fao.geonet.api.records.rdf.RdfOutputManager;
import org.fao.geonet.api.records.rdf.RdfSearcher;
import org.fao.geonet.services.thesaurus.GetList;
import org.fao.geonet.utils.BinaryFile;
import org.jdom.Element;

import java.io.File;
import java.nio.file.Path;

@Deprecated
public class GetPublicMetadataAsRdf implements Service {
    public void init(Path appPath, ServiceConfig params) throws Exception {
    }

    //--------------------------------------------------------------------------
    //---
    //--- Service
    //---
    //--------------------------------------------------------------------------

    public Element exec(Element params, ServiceContext context) throws Exception {
        Element thesaurusEl = new GetList().exec(params, context);

        RdfOutputManager manager = new RdfOutputManager((Element) thesaurusEl.getChild("thesauri").detach());

        RdfSearcher rdfHarvestSearcher = new RdfSearcher(params, context);
        File rdfFile = manager.createRdfFile(context, rdfHarvestSearcher);

        return BinaryFile.encode(200, rdfFile.toPath().toAbsolutePath().normalize(), true).getElement();
    }

}
