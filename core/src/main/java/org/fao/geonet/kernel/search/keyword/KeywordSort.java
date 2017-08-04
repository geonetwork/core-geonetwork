/*
 * Copyright (C) 2001-2016 Food and Agriculture Organization of the
 * United Nations (FAO-UN), United Nations World Food Programme (WFP)
 * and United Nations Environment Programme (UNEP)
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or (at
 * your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
 *
 * Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
 * Rome - Italy. email: geonetwork@osgeo.org
 */

package org.fao.geonet.kernel.search.keyword;

import org.fao.geonet.kernel.KeywordBean;

import java.util.Comparator;

/**
 * Contains factory methods for creating comparators for sorting collections of {@link KeywordBean}
 * objects
 *
 * @author jeichar
 */
public final class KeywordSort {
    private KeywordSort() {
    }

    /**
     * Sort keywords based on the default label of the keywords
     *
     * @param direction if DESC then sort a-z otherwise z-a
     * @return a comparator for sorting by label
     */
    public static Comparator<KeywordBean> defaultLabelSorter(final SortDirection direction) {
        return new Comparator<KeywordBean>() {
            public int compare(final KeywordBean kw1, final KeywordBean kw2) {
                int comp = normalizeDesc(kw1.getDefaultValue()).compareTo(normalizeDesc(kw2.getDefaultValue()));
                if(comp == 0) {
                    comp = kw1.getUriCode().compareTo(kw2.getUriCode());
                }
                return direction.multiplier * comp;
            }

            @Override
            public String toString() {
                return "Sort by Value " + direction;
            }
        };
    }

    /**
     * Sort keywords based on the default definition of the keywords
     *
     * @param direction if DESC then sort a-z otherwise z-a
     * @return a comparator for sorting by definition
     */
    public static Comparator<KeywordBean> defaultDefinitionSorter(final SortDirection direction) {
        return new Comparator<KeywordBean>() {
            public int compare(final KeywordBean kw1, final KeywordBean kw2) {
                return direction.multiplier * normalizeDesc(kw1.getDefaultDefinition()).compareToIgnoreCase(
                    normalizeDesc(kw2.getDefaultDefinition()));
            }

            @Override
            public String toString() {
                return "Sort by Definition " + direction;
            }
        };
    }

    public static Comparator<KeywordBean> searchResultsSorter(final String searchTerm, final SortDirection direction) {
        final Comparator<KeywordBean> defaultSorter = defaultLabelSorter(direction);
        final String normSearchTerm = normalizeDesc(searchTerm);
        return new Comparator<KeywordBean>() {
            public int compare(final KeywordBean kw1, final KeywordBean kw2) {
                String defValue1 = normalizeDesc(kw1.getDefaultValue());
                String defValue2 = normalizeDesc(kw2.getDefaultValue());

                int sim1 = calcSim(defValue1, normSearchTerm);
                int sim2 = calcSim(defValue2, normSearchTerm);

                if (sim1 != sim2) {
                    sim1 = sim1 * (6 - Math.min(5, defValue1.length() - normSearchTerm.length()));
                    sim2 = sim2 * (6 - Math.min(5, defValue2.length() - normSearchTerm.length()));
                    return sim2 - sim1;
                }
                return defaultSorter.compare(kw1, kw2);
            }

            @Override
            public String toString() {
                return "Prioritize: " + normSearchTerm + ", other order: " + direction;
            }
        };

    }

    private static int calcSim(String val, String normSearchTerm) {
        if (val.equals(normSearchTerm)) {
            return 100;
        }
        if (val.startsWith(normSearchTerm)) {
            return 1;
        } else {
            return 0;
        }
    }

    public static String normalizeDesc(String rawDesc) {
        if (rawDesc == null) {
            return "";
        }
        String lowercase = rawDesc.toLowerCase();
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < lowercase.length(); i++) {
            char currChar = lowercase.charAt(i);
            switch (currChar) {
                case '\u00f6':// o diacritic
                case '\u00f2':// o accent
                case '\u00f4':// o circumflex
                    builder.append('o');
                    break;
                case '\u00fc':// u diacritic
                case '\u00f9':// u accent
                case '\u00fb':// u circumflex
                    builder.append('u');
                    break;
                case '\u00e9':// e accent
                case '\u00ea':// e circumflex
                case '\u00e8':// e accent
                    builder.append('e');
                    break;
                case '\u00e4':// a diacritic
                case '\u00e0':// a accent
                case '\u00e2':// a circumflex
                case '\u00e1':// a accent
                    builder.append('a');
                    break;
                case '\u00e7':// c cedilla
                    builder.append('c');
                    break;
                case '\u00ec':// i accent
                case '\u00ee':// i circumflex
                    builder.append('i');
                    break;
                default:
                    if (Character.isAlphabetic(currChar) || Character.isDigit(currChar)) {
                        builder.append(currChar);
                    }
                    break;
            }
        }
        return builder.toString().trim();
    }
}
