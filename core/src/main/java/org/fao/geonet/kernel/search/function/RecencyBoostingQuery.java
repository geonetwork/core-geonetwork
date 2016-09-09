//=============================================================================
//===	Copyright (C) 2010 Food and Agriculture Organization of the
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

package org.fao.geonet.kernel.search.function;

import org.apache.lucene.index.AtomicReaderContext;
import org.apache.lucene.index.SortedDocValues;
import org.apache.lucene.queries.CustomScoreProvider;
import org.apache.lucene.queries.CustomScoreQuery;
import org.apache.lucene.search.FieldCache;
import org.apache.lucene.search.Query;
import org.apache.lucene.util.BytesRef;
import org.fao.geonet.domain.ISODate;

import java.io.IOException;

/**
 * Boost recently modified document. (adapted from LuceneInAction)
 *
 * TODO : Store TimeStamp as numeric for faster processing ? which could also allow to remove @see
 * {@link ISODate} dependency.
 *
 * @author fxprunayre
 */
public class RecencyBoostingQuery extends CustomScoreQuery {
    private static int SEC_PER_DAY = 3600 * 24;
    private double multiplier;
    private ISODate today;
    private int maxDaysAgo;
    private String dayField;

    /**
     * The class requires you to specify the name of a numeric field that contains the timestamp of
     * each document that you’d like to use for boosting.
     *
     * @param q          The query
     * @param multiplier Boosting factor used to compute simple linear boost
     * @param maxDaysAgo Number of days from when document must be skipped. eg. 2*365 will skipped
     *                   all documents which are more than 2 years old.
     * @param dayField   Timestamp field in Lucene index. eg. _changeDate
     */
    public RecencyBoostingQuery(Query q, double multiplier, int maxDaysAgo,
                                String dayField) {
        super(q);
        today = new ISODate();
        this.multiplier = multiplier;
        this.maxDaysAgo = maxDaysAgo;
        this.dayField = dayField;
    }

    public CustomScoreProvider getCustomScoreProvider(AtomicReaderContext r)
        throws IOException {
        return new RecencyBooster(r);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((dayField == null) ? 0 : dayField.hashCode());
        result = prime * result + maxDaysAgo;
        long temp;
        temp = Double.doubleToLongBits(multiplier);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        result = prime * result + ((today == null) ? 0 : today.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        RecencyBoostingQuery other = (RecencyBoostingQuery) obj;
        if (dayField == null) {
            if (other.dayField != null)
                return false;
        } else if (!dayField.equals(other.dayField))
            return false;
        if (maxDaysAgo != other.maxDaysAgo)
            return false;
        if (Double.doubleToLongBits(multiplier) != Double.doubleToLongBits(other.multiplier))
            return false;
        if (today == null) {
            if (other.today != null)
                return false;
        } else if (!today.equals(other.today))
            return false;
        return true;
    }

    private class RecencyBooster extends CustomScoreProvider {
        final SortedDocValues publishDay;

        public RecencyBooster(AtomicReaderContext r) throws IOException {
            super(r);

            publishDay = FieldCache.DEFAULT.getTermsIndex(r.reader(), dayField);
        }

        public float customScore(int doc, float subQueryScore, float valSrcScore) {
            BytesRef ret = new BytesRef();
            publishDay.get(doc);
            ISODate d = new ISODate(ret.utf8ToString());
            long daysAgo = today.timeDifferenceInSeconds(d) / SEC_PER_DAY;
            if (daysAgo < maxDaysAgo) {    // skip old document
                float boost = (float) (multiplier * (maxDaysAgo - daysAgo) / maxDaysAgo);
                return (float) (subQueryScore * (1.0 + boost));
            } else {
                return subQueryScore;
            }
        }
    }
}
