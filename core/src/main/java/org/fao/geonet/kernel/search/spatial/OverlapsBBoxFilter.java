//===	Copyright (C) 2001-2007 Food and Agriculture Organization of the
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

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.index.ItemVisitor;
import com.vividsolutions.jts.index.SpatialIndex;
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
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class OverlapsBBoxFilter extends SpatialFilter {

    private Set<String> intersected;

    public OverlapsBBoxFilter(Query query, int numHits, Geometry geom, Pair<FeatureSource<SimpleFeatureType, SimpleFeature>, SpatialIndex> sourceAccessor) throws IOException {
        super(query, numHits, geom, sourceAccessor);
    }

    public DocIdSet getDocIdSet(AtomicReaderContext context, Bits acceptDocs) throws IOException {
        final OpenBitSet bits = new OpenBitSet(context.reader().maxDoc());
        final Set<String> intersected = intersected();

        if (intersected.isEmpty() || _hits >= _numHits) return bits;

        new IndexSearcher(context.reader()).search(_query, new Collector() {
            private int docBase;
            private Document document;
            private AtomicReader reader;

            // ignore scorer
            public void setScorer(Scorer scorer) {
            }

            // accept docs out of order (for a BitSet it doesn't matter)
            public boolean acceptsDocsOutOfOrder() {
                return true;
            }

            public void collect(int doc) {
                doc = doc + docBase;
                try {
                    document = reader.document(doc, _fieldsToLoad);
                    String key = document.get("_id");
                    if (intersected.contains(key) && _hits < _numHits) {
                        _hits++;
                        bits.set(doc + docBase);

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

    protected synchronized Set<String> intersected() {
        if (intersected == null) {
            intersected = new HashSet<>();
            SpatialIndex spatialIndex = sourceAccessor.two();
            Envelope env = _geom.getEnvelopeInternal();

            spatialIndex.query(env, new ItemVisitor() {
                @Override
                public void visitItem(Object o) {
                    SpatialIndexWriter.Data data = (SpatialIndexWriter.Data) o;
                    intersected.add(data.getMetadataId());
                }
            });
        }
        return intersected;
    }

    protected org.opengis.filter.Filter createFilter(FeatureSource<SimpleFeatureType, SimpleFeature> source) {
        return null;
    }

}
