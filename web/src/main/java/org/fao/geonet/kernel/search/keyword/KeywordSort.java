package org.fao.geonet.kernel.search.keyword;

import java.util.Comparator;

import org.fao.geonet.kernel.KeywordBean;

/**
 * Contains factory methods for creating comparators for sorting collections of {@link KeywordBean} objects
 * @author jeichar
 */
public final class KeywordSort {
    private KeywordSort() { }
    
    /**
     * Sort keywords based on the default label of the keywords
     * 
     * @param direction if DESC then sort a-z otherwise z-a
     * 
     * @return a comparator for sorting by label
     */
    public static Comparator<KeywordBean> defaultLabelSorter(final SortDirection direction) {
        return new Comparator<KeywordBean>() {
            public int compare(final KeywordBean kw1, final KeywordBean kw2) {
                return direction.multiplier * kw1.getDefaultValue().toLowerCase().compareToIgnoreCase(kw2.getDefaultValue().toLowerCase());
            }
        };
    }
    /**
     * Sort keywords based on the default definition of the keywords
     * 
     * @param direction if DESC then sort a-z otherwise z-a
     * 
     * @return a comparator for sorting by definition
     */
    public static Comparator<KeywordBean> defaultDefinitionSorter(final SortDirection direction) {
        return new Comparator<KeywordBean>() {
            public int compare(final KeywordBean kw1, final KeywordBean kw2) {
                return direction.multiplier * kw1.getDefaultDefinition().toLowerCase().compareToIgnoreCase(kw2.getDefaultDefinition().toLowerCase());
            }
        };
    }
    
}
