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

package org.fao.geonet.services.search;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import org.fao.geonet.kernel.search.LuceneConfig;
import org.fao.geonet.kernel.search.facet.Dimension;
import org.fao.geonet.kernel.search.facet.ItemConfig;
import org.fao.geonet.kernel.search.facet.SummaryType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

/**
 * Get the facet configuration.  Needed for the facet/refine search directive.
 *
 * @author Jesse on 1/26/2015.
 */
@Controller("search/facet/config")
public class FacetsService {
    @Autowired
    private LuceneConfig luceneConfig;

    @RequestMapping(value = "/{portal}/{lang:[a-z]{3}}/search/facet/config", produces = {MediaType.APPLICATION_JSON_VALUE})
    @ResponseBody
    public Map<String, List<Facet>> getConfig() {
        Map<String, List<Facet>> results = Maps.newLinkedHashMap();
        for (SummaryType summaryType : luceneConfig.getSummaryTypes().getSummaryTypes()) {
            List<Facet> facets = Lists.newArrayList();
            for (ItemConfig itemConfig : summaryType.getItems()) {
                facets.add(new Facet(itemConfig));
            }
            results.put(summaryType.getName(), facets);
        }

        return results;
    }

    /**
     * Equivalent to the {@link org.fao.geonet.kernel.search.facet.SummaryType} but only has the
     * essential information so that the output json is clean and only has the desired information.
     */
    public static class FacetConfig {
        final String id;
        final List<Facet> items = Lists.newArrayList();

        public FacetConfig(String id) {
            this.id = id;
        }

        public String getId() {
            return id;
        }

        public List<Facet> getItems() {
            return items;
        }
    }

    public static class Facet {
        private final String key;
        private final String name;
        private final String label;
        private final Integer pageSize;

        public Facet(ItemConfig itemConfig) {
            final Dimension dimension = itemConfig.getDimension();
            this.key = dimension.getIndexKey();
            this.label = dimension.getLabel();
            this.name = dimension.getName();
            this.pageSize = itemConfig.getPageSize();
        }

        public String getKey() {
            return key;
        }

        public String getName() {
            return name;
        }

        public String getLabel() {
            return label;
        }

        public Integer getPageSize() {
            return pageSize;
        }
    }
}
