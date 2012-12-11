package org.fao.geonet.kernel.search;

import org.apache.lucene.index.AtomicReaderContext;
import org.apache.lucene.search.FieldCache;
import org.apache.lucene.search.FieldCache.DocTerms;
import org.apache.lucene.search.FieldComparator;
import org.apache.lucene.search.FieldComparatorSource;
import org.apache.lucene.util.BytesRef;

import java.io.IOException;

public class CaseInsensitiveFieldComparatorSource extends FieldComparatorSource {

    private static final CaseInsensitiveFieldComparatorSource languageInsensitiveInstance         = new CaseInsensitiveFieldComparatorSource(null);
    private String searchLang;

    /**
     * @param searchLang if non-null then it will be attempted to translate each field before sorting
     */
    public CaseInsensitiveFieldComparatorSource(String searchLang) {
        this.searchLang = searchLang;
    }
    @Override
    public FieldComparator<String> newComparator(String fieldname, int numHits, int sortPos, boolean reversed)
            throws IOException {

        return new CaseInsensitiveFieldComparator(numHits, searchLang, fieldname);
    }

    public static final class CaseInsensitiveFieldComparator extends FieldComparator<String> {

        private static final DocTerms EMPTY_TERMS = new DocTerms(){

            @Override
            public BytesRef getTerm(int docID, BytesRef ret) {
                return null;
            }

            @Override
            public boolean exists(int docID) {
                return false;
            }

            @Override
            public int size() {
                return 0;
            }
            
        };
        private String[]     values;
        private DocTerms     currentReaderValues;
        private final String field;
        private String       bottom;
        private String searchLang;
        private DocTerms shadowValues;

        CaseInsensitiveFieldComparator(int numHits, String searchLang, String field) {
            values = new String[numHits];
            this.field = field;
            this.searchLang = searchLang;
        }

        @Override
        public int compare(int slot1, int slot2) {
            final String val1 = values[slot1];
            final String val2 = values[slot2];
            return doCompare(val1, val2);
        }

        private int doCompare(final String val1, final String val2) {
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
            final String val2 = readerValue(doc);
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

        private String readerValue(int doc) {
            BytesRef ref = new BytesRef();
            BytesRef term;
            if(shadowValues.exists(doc)) {
                term = shadowValues.getTerm(doc, ref);
            } else if (currentReaderValues.exists(doc)){
                term = currentReaderValues.getTerm(doc, ref);
            } else {
                return null;
            }
            return term.utf8ToString().trim();
        }

        @Override
        public void copy(int slot, int doc) {
            String val = readerValue(doc);
            if(val != null) {
                values[slot] = val;
            }
        }

        @Override
        public void setBottom(final int bottom) {
            this.bottom = values[bottom];
        }

        @Override
        public FieldComparator<String> setNextReader(AtomicReaderContext context) throws IOException {
           currentReaderValues = FieldCache.DEFAULT.getTerms(context.reader(), field);
          if(searchLang != null) {
              this.shadowValues = FieldCache.DEFAULT.getTerms(context.reader(), LuceneConfig.multilingualSortFieldName(field, searchLang));
          } else {
              this.shadowValues = EMPTY_TERMS;
          }
            return this;
        }

        @Override
        public String value(int slot) {
            return values[slot];
        }

        @Override
        public int compareDocToValue(int doc, String value) throws IOException {
            return doCompare(readerValue(doc), value);
        }
    }
    public static CaseInsensitiveFieldComparatorSource languageInsensitiveInstance() {
        return languageInsensitiveInstance;
    }
}
