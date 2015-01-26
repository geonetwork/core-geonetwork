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

    @RequestMapping(value = "/{lang}/search/facet/config", produces = {MediaType.APPLICATION_JSON_VALUE})
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
     * Equivalent to the {@link org.fao.geonet.kernel.search.facet.SummaryType} but only has the essential information
     * so that the output json is clean and only has the desired information.
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

        public Facet(ItemConfig itemConfig) {
            final Dimension dimension = itemConfig.getDimension();
            this.key = dimension.getIndexKey();
            this.label = dimension.getLabel();
            this.name = dimension.getName();
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
    }
}
