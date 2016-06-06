//==============================================================================
//===	Copyright (C) 2001-2008 Food and Agriculture Organization of the
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

package org.fao.geonet.kernel.search.spatial;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.AtomicReader;
import org.apache.lucene.index.AtomicReaderContext;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.DocIdSet;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Scorer;
import org.apache.lucene.util.Bits;
import org.apache.lucene.util.OpenBitSet;
import org.fao.geonet.domain.Pair;
import org.geotools.data.FeatureSource;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.Name;
import org.opengis.filter.FilterFactory2;
import org.opengis.filter.expression.Literal;
import org.opengis.filter.expression.PropertyName;
import org.opengis.filter.spatial.SpatialOperator;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.index.SpatialIndex;

/**
 * This filter filters out all documents that do not intersect the requested geometry.
 *
 * @author jeichar
 */
public class FullScanFilter extends SpatialFilter {

    private Set<String> _matches;

    public FullScanFilter(Query query, int numHits, Geometry geom,
                          Pair<FeatureSource<SimpleFeatureType, SimpleFeature>, SpatialIndex> sourceAccessor) throws IOException {
        super(query, numHits, geom, sourceAccessor);
    }

    protected FullScanFilter(Query query, int numHits, Envelope bounds,
                             Pair<FeatureSource<SimpleFeatureType, SimpleFeature>, SpatialIndex> sourceAccessor) throws IOException {
        super(query, numHits, bounds, sourceAccessor);
    }

    public DocIdSet getDocIdSet(AtomicReaderContext context, Bits acceptDocs) throws IOException {
        final OpenBitSet bits = new OpenBitSet(context.reader().maxDoc());

        final Set<String> matches = loadMatches();

        new IndexSearcher(context.reader()).search(_query, new Collector() {
            private int docBase;
            private AtomicReader reader;

            // ignore scorer
            public void setScorer(Scorer scorer) {
            }

            // accept docs out of order (for a BitSet it doesn't matter)
            public boolean acceptsDocsOutOfOrder() {
                return true;
            }

            public final void collect(int doc) {
                Document document;
                try {
                    document = reader.document(doc, _fieldsToLoad);
                    if (matches.contains(document.get("_id"))) {
                        bits.set(docBase + doc);
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public void setNextReader(AtomicReaderContext context) throws IOException {
                this.docBase = context.docBase;
                this.reader = context.reader();
            }
        });
        return bits;
    }

    /**
     * Default for testing purposes
     *
     * @return the set of ids of the metadata objects that match the geometry of this filter
     * @see #createFilter(FeatureSource)
     */
    protected synchronized Set<String> loadMatches() throws IOException {
        if (_matches == null) {

            FeatureSource<SimpleFeatureType, SimpleFeature> _featureSource = sourceAccessor.one();
            FeatureCollection<SimpleFeatureType, SimpleFeature> features = _featureSource
                .getFeatures(createFilter(_featureSource));
            FeatureIterator<SimpleFeature> iterator = features.features();

            HashSet<String> matches = new HashSet<String>();
            try {
                final Name idColumn = SpatialIndexWriter.findIdColumn(_featureSource);
                while (iterator.hasNext()) {
                    SimpleFeature feature = iterator.next();
                    matches
                        .add((String) feature
                            .getAttribute(idColumn));
                }
            } finally {
                iterator.close();
            }
            _matches = matches;
        }
        return _matches;
    }

    public SpatialOperator createGeomFilter(FilterFactory2 filterFactory,
                                            PropertyName geomPropertyName, Literal geomExpression) {
        return filterFactory.disjoint(geomPropertyName, geomExpression);
    }

}
