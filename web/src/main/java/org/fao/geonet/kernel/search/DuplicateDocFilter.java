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

package org.fao.geonet.kernel.search;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.AtomicReaderContext;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.DocIdSet;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Scorer;
import org.apache.lucene.util.Bits;
import org.apache.lucene.util.DocIdBitSet;

import java.io.IOException;
import java.util.BitSet;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * When there are multiple languages certain queries can match the same
 * "document" in each different language index. This filter allows the first
 * match but none of the later matche.
 *
 * @author jeichar
 */
public class DuplicateDocFilter extends Filter {

	public Query getQuery() {
		return _query;
	}

	public void setQuery(Query query) {
		this._query = query;
	}

	private Query _query;
	final Set<String> hits = new HashSet<String>();
	private int _maxResults;
    private Set<String> _fieldsToLoad;

	public DuplicateDocFilter(Query query, int maxResults) {
		this._query = query;
		this._maxResults = maxResults;
		_fieldsToLoad = Collections.singleton("_id");
	}

    @Override
    public DocIdSet getDocIdSet(AtomicReaderContext context, Bits acceptDocs) throws IOException {
		final BitSet bits = new BitSet(context.reader().maxDoc());

		new IndexSearcher(context.reader()).search(_query, new Collector() {

            private int docBase;
            private IndexReader reader;

            @Override
            public void setScorer(Scorer scorer) throws IOException {
            }
            
            @Override
            public void setNextReader(AtomicReaderContext context) throws IOException {
                this.docBase = context.docBase;
                this.reader = context.reader();
            }
            
            @Override
            public void collect(int doc) throws IOException {
                if (hits.size() <= _maxResults) {
                    Document document;
                    try {
                        document = reader.document(docBase + doc, _fieldsToLoad);
                        String id = document.get("_id");

                        if (!hits.contains(id)) {
                            bits.set(docBase + doc);
                            hits.add(id);
                        }
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            }
            
            @Override
            public boolean acceptsDocsOutOfOrder() {
                return false;
            }
        });

		return new DocIdBitSet(bits);
	}

}
