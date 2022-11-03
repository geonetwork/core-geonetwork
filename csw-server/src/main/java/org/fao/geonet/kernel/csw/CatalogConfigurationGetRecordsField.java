package org.fao.geonet.kernel.csw;

import java.util.HashMap;
import java.util.Map;

public class CatalogConfigurationGetRecordsField {
    private String name;
    private String type;
    private boolean range;
    private String luceneField;
    private String luceneSortField;
    private Map<String, String> xpaths = new HashMap();

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public boolean isRange() {
        return range;
    }

    public String getLuceneField() {
        return luceneField;
    }

    public String getLuceneSortField() {
        return luceneSortField;
    }

    public Map<String, String> getXpaths() {
        return xpaths;
    }

    public CatalogConfigurationGetRecordsField(String name, String type, boolean range, String luceneField,
                                               String luceneSortField, Map<String, String> xpaths) {
        this.name = name;
        this.type = type;
        this.range = range;
        this.luceneField = luceneField;
        this.luceneSortField = luceneSortField;
        this.xpaths = xpaths;
    }
}
