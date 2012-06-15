package org.fao.geonet.kernel.search;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.FieldCache;
import org.apache.lucene.search.FieldComparator;
import org.apache.lucene.search.FieldComparatorSource;

import java.io.IOException;

public class CaseInsensitiveFieldComparatorSource extends FieldComparatorSource {

    private static final long                                 serialVersionUID = 1L;
    private static final CaseInsensitiveFieldComparatorSource instance         = new CaseInsensitiveFieldComparatorSource();

    @Override
    public FieldComparator newComparator(String fieldname, int numHits, int sortPos, boolean reversed)
            throws IOException {

        return new CaseInsensitiveFieldComparator(numHits, fieldname);
    }

    public static final class CaseInsensitiveFieldComparator extends FieldComparator {

        private String[]     values;
        private String[]     currentReaderValues;
        private final String field;
        private String       bottom;

        CaseInsensitiveFieldComparator(int numHits, String field) {
            values = new String[numHits];
            this.field = field;
        }

        @Override
        public int compare(int slot1, int slot2) {
            final String val1 = values[slot1];
            final String val2 = values[slot2];
            if (val1 == null) {
                if (val2 == null) {
                    return 0;
                }
                return 1;
            } else if (val2 == null) {
                return -1;
            }

            return val1.compareToIgnoreCase(val2);
        }

        @Override
        public int compareBottom(int doc) {
            final String val2 = currentReaderValues[doc];
            if (bottom == null) {
                if (val2 == null) {
                    return 0;
                }
                return -1;
            } else if (val2 == null) {
                return 1;
            }
            return bottom.compareTo(val2);
        }

        @Override
        public void copy(int slot, int doc) {
            if(currentReaderValues[doc] != null) {
                values[slot] = currentReaderValues[doc].trim();
            }
        }

        @Override
        public void setNextReader(IndexReader reader, int docBase) throws IOException {
            currentReaderValues = FieldCache.DEFAULT.getStrings(reader, field);
        }

        @Override
        public void setBottom(final int bottom) {
            this.bottom = values[bottom];
        }

        @Override
        public Comparable value(int slot) {
            return values[slot];
        }
    }
    public static CaseInsensitiveFieldComparatorSource instance() {
        return instance;
    }
}
