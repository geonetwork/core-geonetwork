package org.fao.geonet.kernel.search;

import java.io.IOException;
import java.text.Collator;

import org.apache.lucene.index.AtomicReaderContext;
import org.apache.lucene.index.SortedDocValues;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.search.FieldCache;
import org.apache.lucene.search.FieldComparator;
import org.apache.lucene.search.FieldComparatorSource;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.packed.PackedInts.Reader;

/**
 * TODO: it may be relevant to use
 * http://lucene.apache.org/core/4_9_0/analyzers-common/org/apache/lucene/collation/CollationKeyAnalyzer.html
 * instead ?
 */
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

        private static final SortedDocValues EMPTY_TERMS = new SortedDocValues() {
            @Override
            public int getOrd(int docID) {
                return 0;
            }

            @Override
            public BytesRef lookupOrd(int ord) {
                return null;
            }

            @Override
            public int getValueCount() {
                return 0;
            }
        };
        private String[]     values;
        private SortedDocValues currentReaderValues;
        private final String field;
        private String       bottom;
        private String searchLang;
        private Collator collator;
        private SortedDocValues shadowValues;

        CaseInsensitiveFieldComparator(int numHits, String searchLang, String field) {
            values = new String[numHits];
            this.field = field;
            this.searchLang = searchLang;
            this.collator = Collator.getInstance();
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
            
            return this.collator.compare(val1, val2);
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
            String term = null;

            int ord = shadowValues.getOrd(doc);
            if(ord != -1) {
                term = shadowValues.lookupOrd(ord).utf8ToString().trim();
            }

            if(term == null || term.isEmpty()) {
                ord = currentReaderValues.getOrd(doc);
                if (ord != -1) {
                    term = currentReaderValues.lookupOrd(ord).utf8ToString().trim();
                } else {
                    return null;
                }
            }
            return term;
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
          currentReaderValues = FieldCache.DEFAULT.getTermsIndex(context.reader(), field);

          if(searchLang != null) {
              this.shadowValues = FieldCache.DEFAULT.getTermsIndex(context.reader(), LuceneConfig.multilingualSortFieldName(field, searchLang));
          } else {
              this.shadowValues = EMPTY_TERMS;
          }
            return this;
        }

        @Override
        public String value(int slot) {
            return values[slot];
        }
    }
    public static CaseInsensitiveFieldComparatorSource languageInsensitiveInstance() {
        return languageInsensitiveInstance;
    }
}
