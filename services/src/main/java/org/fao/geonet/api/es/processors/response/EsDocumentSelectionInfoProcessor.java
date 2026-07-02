/*
 * Copyright (C) 2001-2026 Food and Agriculture Organization of the
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

package org.fao.geonet.api.es.processors.response;

import com.fasterxml.jackson.databind.node.ObjectNode;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.api.ApiUtils;
import org.fao.geonet.api.es.ObjectNodeUtils;
import org.fao.geonet.constants.Edit;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.SelectionManager;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Processes an Elasticsearch response document to add information about the metadata been selected.
 */
@Component
public class EsDocumentSelectionInfoProcessor implements EsDocumentProcessor {
    @Override
    public void process(ObjectNode doc, ServiceContext context, Map<String, Object> parameters) throws Exception {

        final Set<String> selections = (Set<String>) parameters.get("selections");

        if (selections != null) {
            addSelectionInfo(doc, selections);
        }
    }

    private void addSelectionInfo(ObjectNode doc, Set<String> selections) {
        final String uuid = ObjectNodeUtils.getSourceString(doc, Geonet.IndexFieldNames.UUID);
        doc.put(Edit.Info.Elem.SELECTED, selections.contains(uuid));
    }
}
