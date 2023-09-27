//=============================================================================
//===	Copyright (C) 2001-2023 Food and Agriculture Organization of the
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

package org.fao.geonet.kernel.search.index;

import org.apache.commons.lang.StringUtils;
import org.elasticsearch.action.search.SearchResponse;
import org.fao.geonet.kernel.search.EsSearchManager;
import org.fao.geonet.util.XslUtil;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * An operation which takes some time to add information
 * into the index. This action can be run in the background (TODO)
 */
public class OverviewIndexFieldUpdater {
    private Integer imageSize = 140;

    @Autowired
    EsSearchManager searchManager;

    public void process(String uuid) {
        processOverview(uuid);
        processOverview(uuid + "-draft");
    }

    private ArrayList<HashMap<String, String>> getHitOverviews(Map<String, Object> fields) {
        Object overviews = fields.get("overview");
        if (overviews != null) {
            return ((ArrayList) overviews);
        }
        return new ArrayList<>();
    }

    private void processOverview(String id) {
        Set<String> source = new HashSet<>();
        source.add("overview");
        SearchResponse response = null;
        try {
            response = searchManager.query(String.format(
                "+id:\"%s\" _exists_:overview.url -_exists_:overview.data",
                id), null, source, 0, 1);
            response.getHits().forEach(hit -> {
                AtomicBoolean updates = new AtomicBoolean(false);
                Map<String, Object> fields = hit.getSourceAsMap();
                getHitOverviews(fields)
                    .stream()
                    .forEach(overview -> {
                        String url = (String) ((Map) overview).get("url");
                        if (StringUtils.isNotEmpty(url)) {
                            String data = XslUtil.buildDataUrl(url, imageSize);
                            ((Map) overview).put("data", data);
                            updates.set(true);
                        }
                    });
                if (updates.get()) {
                    try {
                        searchManager.updateFields(id, fields, source);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
