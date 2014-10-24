package org.fao.geonet.kernel.search;

import bak.pcj.map.MapDefaults;
import bak.pcj.map.ObjectKeyByteChainedHashMap;
import bak.pcj.map.ObjectKeyByteMap;
import bak.pcj.map.ObjectKeyByteMapIterator;

import org.apache.lucene.index.AtomicReaderContext;
import org.apache.lucene.index.SortedDocValues;
import org.apache.lucene.search.FieldCache;
import org.apache.lucene.search.FieldComparator;
import org.apache.lucene.search.FieldComparatorSource;
import org.apache.lucene.search.SortField;
import org.apache.lucene.util.BytesRef;

import java.io.IOException;

public class LangSortField extends SortField {

    private static final String MD_DOC_LANG_FIELD = "_docLocale";
    public LangSortField( String currentLocale ) {
        super(MD_DOC_LANG_FIELD, new LangFieldComparatorSource(currentLocale));
    }

    static class LangFieldComparatorSource extends FieldComparatorSource {

        private String currentLocale;

        public LangFieldComparatorSource( String currentLocale ) {
            this.currentLocale = currentLocale;
        }

        @Override
        public FieldComparator<String> newComparator( String fieldname, int numHits, int sortPos, boolean reversed )
                throws IOException {
            return new LangFieldComparator(currentLocale, numHits);
        }
    }

    static class LangFieldComparator extends FieldComparator<String> {

        private ObjectKeyByteMap langCodeValue = new ObjectKeyByteChainedHashMap();
        private byte nextVal;
        private String defaultLocale;

        public LangFieldComparator( String currentLocale, int numHits ) {
            this.values = new byte[numHits];
            langCodeValue.put(currentLocale, (byte) 1);
            langCodeValue.put(null, (byte) 255);
            nextVal = (byte) 2;
            this.defaultLocale = currentLocale;
        }

        private byte[] values;
        private SortedDocValues currentReaderValues;
        // -2 indicates not set
        private int bottom = -2;

        @Override
        public int compare( int slot1, int slot2 ) {
            return values[slot1] - values[slot2];
        }

        @Override
        public int compareBottom( int doc ) {
            final String val2 = readerValue(doc);
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

        public void setTopValue(String value) {
            // LUCENE49FIX
            // Used for deep paging we don't use.

        }
        public int compareTop(int doc) throws IOException {
            // LUCENE49FIX
            // Used for deep paging we don't use.
            return -1;
        }

        @Override
        public void copy( int slot, int doc ) {
            String locale = readerValue(doc);
            values[slot] = intValue(locale);
        }

        private String readerValue(int docID) {
            int ord = currentReaderValues.getOrd(docID);
            //if ord < 0 then it is a missing value
            if(ord < 0) {
            	//Using default 0 instead of throwing an error
            	ord = 0;
            }
            
            System.out.println(ord + " -> " + currentReaderValues.lookupOrd(ord).utf8ToString());
            
            return currentReaderValues.lookupOrd(ord).utf8ToString();
        }

        private byte intValue( String locale ) {
            byte val = langCodeValue.get(locale);
            if (val == MapDefaults.defaultByte()) {
                val = nextVal;
                nextVal++;
                langCodeValue.put(locale, val);
            }
            return val;
        }

        @Override
        public FieldComparator<String> setNextReader(AtomicReaderContext context) throws IOException {
            currentReaderValues = FieldCache.DEFAULT.getTermsIndex(context.reader(), MD_DOC_LANG_FIELD);
            return this;
        }

        @Override
        public void setBottom( final int bottom ) {
            this.bottom = values[bottom];
        }

        @Override
        public String value(int slot) {
            String lang = this.defaultLocale;
            byte val = values[slot];
            ObjectKeyByteMapIterator iter = langCodeValue.entries();
            while(iter.hasNext()) {
                iter.next();
                if(iter.getValue() == val) {
                    if(iter.getKey() != null) {
                        lang = (String) iter.getKey();
                    }
                    break;
                }
            }
            return lang;
        }
    }
}