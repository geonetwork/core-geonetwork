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
import org.fao.geonet.api.es.ObjectNodeUtils;
import org.fao.geonet.domain.ReservedOperation;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Processes an Elasticsearch response document to remove information about metadata privileges.
 */
@Component
public class EsDocumentRemovePrivilegesProcessor implements EsDocumentProcessor {
    @Override
    public void process(ObjectNode doc, ServiceContext context, Map<String, Object> parameters) throws Exception {
        removePrivileges(doc);
    }

    private void removePrivileges(ObjectNode doc) {
        // Remove fields with privileges info
        ObjectNode sourceNode = ObjectNodeUtils.getSourceNode(doc);
        if (sourceNode != null) {
            for (ReservedOperation o : ReservedOperation.values()) {
                sourceNode.remove("op" + o.getId());
            }
        }
    }
}
