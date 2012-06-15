package org.fao.geonet.kernel.search;

import bak.pcj.map.MapDefaults;
import bak.pcj.map.ObjectKeyByteChainedHashMap;
import bak.pcj.map.ObjectKeyByteMap;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.FieldCache;
import org.apache.lucene.search.FieldComparator;
import org.apache.lucene.search.FieldComparatorSource;
import org.apache.lucene.search.SortField;

import java.io.IOException;

public class LangSortField extends SortField {

    private static final String MD_DOC_LANG_FIELD = "_docLocale";
    public LangSortField( String currentLocale ) {
        super(MD_DOC_LANG_FIELD, new LangFieldComparatorSource(currentLocale));
    }

    private static final long serialVersionUID = 1L;

    static class LangFieldComparatorSource extends FieldComparatorSource {

        private static final long serialVersionUID = 1L;
        private String currentLocale;

        public LangFieldComparatorSource( String currentLocale ) {
            this.currentLocale = currentLocale;
        }

        @Override
        public FieldComparator newComparator( String fieldname, int numHits, int sortPos, boolean reversed )
                throws IOException {
            return new LangFieldComparator(currentLocale, numHits);
        }
    }

    static class LangFieldComparator extends FieldComparator {

        private ObjectKeyByteMap langCodeValue = new ObjectKeyByteChainedHashMap();
        private byte nextVal;

        public LangFieldComparator( String currentLocale, int numHits ) {
            this.values = new byte[numHits];
            langCodeValue.put(currentLocale, (byte) 1);
            langCodeValue.put(null, (byte) 255);
            nextVal = (byte) 2;
        }

        private byte[] values;
        private String[] currentReaderValues;
        // -2 indicates not set
        private int bottom = -2;

        @Override
        public int compare( int slot1, int slot2 ) {
            return values[slot1] - values[slot2];
        }

        @Override
        public int compareBottom( int doc ) {
            final String val2 = currentReaderValues[doc];
            if (bottom == -2) {
                if (val2 == null) {
                    return 0;
                }
                return 1;
            } else if (val2 == null) {
                return -1;
            }
            return bottom - intValue(val2);
        }

        @Override
        public void copy( int slot, int doc ) {
            values[slot] = intValue(currentReaderValues[doc]);
        }

        private byte intValue( String locale ) {
            byte val = langCodeValue.get(locale);
            if (val == MapDefaults.defaultByte()) {
                val = nextVal;
// Don't want to keep langs together in Geocat                nextVal++;
                langCodeValue.put(locale, val);
            }
            return val;
        }

        @Override
        public void setNextReader( IndexReader reader, int docBase ) throws IOException {
            currentReaderValues = FieldCache.DEFAULT.getStrings(reader, MD_DOC_LANG_FIELD);
        }

        @Override
        public void setBottom( final int bottom ) {
            this.bottom = values[bottom];
        }

        @SuppressWarnings("rawtypes")
        @Override
        public Comparable value( int slot ) {
            return values[slot];
        }
    }
}