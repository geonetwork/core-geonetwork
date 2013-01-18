package org.fao.geonet.kernel.search;

import org.apache.lucene.facet.taxonomy.TaxonomyReader;
import org.fao.geonet.kernel.search.index.GeonetworkMultiReader;

public class IndexAndTaxonomy {
    public final TaxonomyReader taxonomyReader;
    public final GeonetworkMultiReader indexReader;
    public final long version;

    public IndexAndTaxonomy(long version, GeonetworkMultiReader indexReader, TaxonomyReader taxonomyReader) {
        super();
        this.taxonomyReader = taxonomyReader;
        this.indexReader = indexReader;
        this.version = version;
    }
}
