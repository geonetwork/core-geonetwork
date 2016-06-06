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

package org.fao.geonet.kernel.search;

import org.apache.commons.httpclient.util.URIUtil;
import org.fao.geonet.domain.ReservedOperation;
import org.fao.geonet.kernel.AccessManager;

import java.util.Set;
import java.util.stream.Collectors;

import jeeves.server.context.ServiceContext;

public class SolrAuth {
    public static String addPermissions(String queryString) throws Exception {
        return queryString + "&fq=" + URIUtil.encodeQuery(getPermissions());
    }

    public static String getPermissions() throws Exception {
        ServiceContext context = ServiceContext.get();
        AccessManager accessManager = context.getBean(AccessManager.class);
        Set<Integer> groups = accessManager.getUserGroups(context.getUserSession(), context.getIpAddress(), false);
        final int viewId = ReservedOperation.view.getId();
        final String ids = groups.stream().map(Object::toString)
            .collect(Collectors.joining("\" \"", "(\"", "\")"));
        return String.format(
            "(+docType:metadata +_op%d:%s) " +
                "or (+docType:feature) " +
                "or (+docType:harvesterReport)", viewId, ids);
    }
}
