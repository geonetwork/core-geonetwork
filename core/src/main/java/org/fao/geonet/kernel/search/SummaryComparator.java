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

import org.fao.geonet.kernel.LocaleUtil;
import org.jdom.Element;

import bak.pcj.map.ObjectKeyIntMapIterator;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class SummaryComparator implements Comparator<SummaryComparator.SummaryElement>, Serializable {
    private static final long serialVersionUID = -4668989929284491497L;
    private final Type _type;
    private final Element _configuration;

    // enum Aggregation {
    // COUNT, EQUAL_INTERVAL, QUANTILE, ANNUALLY, MONTHLY, DAILY
    // }
    private final Locale _locale;
    private SummaryComparator.SortOption _option;
    public SummaryComparator(SummaryComparator.SortOption option, SummaryComparator.Type type, String langCode, Element configuration) {
        this._option = option;
        this._type = type;
        this._configuration = configuration;
        _locale = LocaleUtil.toLocale(langCode);
    }

    public int compare(SummaryElement me1, SummaryElement me2) {
        String key1 = me1.name;
        String key2 = me2.name;
        int count1 = me1.count;
        int count2 = me2.count;
        switch (_option) {
            case NAME: {

                int cmp = compareKeys(key1, key2);
                if (cmp != 0)
                    return cmp;
                else
                    return compareCount(count1, count2);
            }
            case FREQUENCY: {
                return compareCount(count1, count2);
            }
            default:
                throw new AssertionError(_option + "is not handled by this method");
        }
    }

    private int compareCount(Integer count1, Integer count2) {
        int cmp = count2.compareTo(count1);
        if (cmp != 0)
            return cmp;
        else
            return -1;
    }

    @SuppressWarnings("unchecked")
    private int compareKeys(String key1, String key2) {
        @SuppressWarnings("rawtypes")
        Comparable value1 = _type.value(key1, _locale, _configuration);
        @SuppressWarnings("rawtypes")
        Comparable value2 = _type.value(key2, _locale, _configuration);
        return value1.compareTo(value2);
    }

    public enum Type {
        STRING {
            @Override
            public Comparable<LocalizedStringComparable> value(String string, Locale locale, Element configuration) {
                return new LocalizedStringComparable(string, locale);
            }

        },
        NUMBER {
            @Override
            public Comparable<Double> value(String string, Locale locale, Element configuration) {
                try {
                    return Double.valueOf(string.trim());
                } catch (NumberFormatException e) {
                    return Double.valueOf(Integer.MAX_VALUE); // Bottom of the list
                }
            }
        },
        SCALE {
            @Override
            public Comparable<Double> value(String string, Locale locale, Element configuration) {
                String scale = string;
                /**
                 * Check scaleDenominator value eg. 1:250000 or 1/2500000
                 * and extract only the denominator.
                 */
                if (string.contains("/")) {
                    String[] parts = string.split("/");
                    scale = parts[parts.length - 1];
                } else if (string.contains("\\")) {
                    String[] parts = string.split("\\\\");
                    scale = parts[parts.length - 1];
                } else if (string.contains(":")) {
                    String[] parts = string.split(":");
                    scale = parts[parts.length - 1];
                }
                try {
                    return Double.valueOf(scale.trim());
                } catch (NullPointerException e) {
                    return Double.valueOf(Integer.MAX_VALUE);    // if scale is not a number value - Bottom of the list
                }
            }
        },
        DATE {
            @Override
            public Comparable<java.util.Date> value(String string, Locale locale, Element configuration) {
                List<DateFormat> formats = new ArrayList<DateFormat>();
                for (Object child : configuration.getChildren("dateFormat")) {
                    Element elem = (Element) child;
                    synchronized (dateformats) {
                        DateFormat format = dateformats.get(elem.getValue());
                        if (format == null) {
                            dateformats.put(elem.getValue(), new SimpleDateFormat(elem.getValue()));
                        }
                        formats.add(format);
                    }
                }
                formats.add(dateformats.get(SimpleDateFormat.SHORT));
                formats.add(dateformats.get(SimpleDateFormat.MEDIUM));
                formats.add(dateformats.get(SimpleDateFormat.LONG));

                for (DateFormat dateFormat : formats) {
                    try {
                        java.util.Date date = dateFormat.parse(string);
                        return date;
                    } catch (ParseException e) {
                        // try next
                    }
                }
                throw new IllegalArgumentException(string
                    + " is not a recognized date pattern.  Add a dateFormat element to the configuration");
            }
        };

        private static Map<Object, DateFormat> dateformats = new HashMap<Object, DateFormat>();

        static {
            dateformats.put(SimpleDateFormat.SHORT, SimpleDateFormat.getDateInstance(SimpleDateFormat.SHORT));
            dateformats.put(SimpleDateFormat.MEDIUM, SimpleDateFormat.getDateInstance(SimpleDateFormat.MEDIUM));
            dateformats.put(SimpleDateFormat.LONG, SimpleDateFormat.getDateInstance(SimpleDateFormat.LONG));
        }

        public static Type parse(String type) {
            return valueOf(type.toUpperCase());
        }

        public abstract Comparable<? extends Object> value(String string, Locale locale, Element configuration);
    }

    public enum SortOption {
        NAME, FREQUENCY;

        public static SummaryComparator.SortOption parse(String order) {
            if (order.equals("freq") || order.equals("frequency"))
                return FREQUENCY;
            return valueOf(order.toUpperCase());
        }
    }

    public static class SummaryElement {
        public final String name;
        public final int count;

        public SummaryElement(ObjectKeyIntMapIterator next) {
            this.name = (String) next.getKey();
            this.count = next.getValue();
        }
    }

}
