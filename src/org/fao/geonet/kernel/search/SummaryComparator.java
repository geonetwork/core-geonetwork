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

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jdom.Element;

public class SummaryComparator implements Comparator<Map.Entry<String, Integer>>
{

    public enum Type {
        STRING{
            @Override
            public Comparable value(String string, Element configuration)
            {
                return string;
            }
        }, 
        NUMBER{
            @Override
            public Comparable value(String string, Element configuration)
            {
                return Double.valueOf(string);
            }
        }, 
        DATE{
            @Override
            public Comparable value(String string, Element configuration)
            {
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
        
        public abstract Comparable value(String string, Element configuration);

        private static Map<Object, DateFormat> dateformats = new HashMap<Object, DateFormat>();
        static {
            dateformats.put(SimpleDateFormat.SHORT, SimpleDateFormat.getDateInstance(SimpleDateFormat.SHORT));
            dateformats.put(SimpleDateFormat.MEDIUM, SimpleDateFormat.getDateInstance(SimpleDateFormat.MEDIUM));
            dateformats.put(SimpleDateFormat.LONG, SimpleDateFormat.getDateInstance(SimpleDateFormat.LONG));
        }
        
        public static Type parse(String type)
        {
            return valueOf(type.toUpperCase());
        } 
    }
    
//    enum Aggregation {
//        COUNT, EQUAL_INTERVAL, QUANTILE, ANNUALLY, MONTHLY, DAILY
//    }
    
    public enum SortOption
    {
        NAME, FREQUENCY;

        public static SummaryComparator.SortOption parse(String order)
        {
            if (order.equals("freq"))
                return FREQUENCY;
            return valueOf(order.toUpperCase());
        }
    }

    
    private SummaryComparator.SortOption _option;
    private Type _type;
    private Element _configuration;

    public SummaryComparator(SummaryComparator.SortOption option, SummaryComparator.Type type, Element configuration)
    {
        this._option = option;
        this._type = type;
        this._configuration=configuration;
    }

    public int compare(Map.Entry<String, Integer> me1, Map.Entry<String, Integer> me2)
    {
        String key1 = (String) me1.getKey();
        String key2 = (String) me2.getKey();
        Integer count1 = (Integer) me1.getValue();
        Integer count2 = (Integer) me2.getValue();
        switch (_option)
        {
        case NAME:
        {
            
            int cmp = compareKeys(key1, key2);
            if (cmp != 0)
                return cmp;
            else
                return compareCount(count1,count2);
        }
        case FREQUENCY:
        {
        	return compareCount(count1, count2);
        }
        default:
            throw new AssertionError(_option + "is not handled by this method");
        }
    }
    
    private int compareCount(Integer count1, Integer count2)
    {
        int cmp = count2.compareTo(count1);
        if (cmp != 0)
            return cmp;
        else
            return -1;
    }

    private int compareKeys(String key1, String key2)
    {
        Comparable value1 = _type.value(key1, _configuration);
        Comparable value2 = _type.value(key2,_configuration);
        return value1.compareTo(value2);
    }
}