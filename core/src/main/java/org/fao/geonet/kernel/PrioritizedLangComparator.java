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

package org.fao.geonet.kernel;

import java.io.Serializable;
import java.util.Comparator;
import java.util.List;

class PrioritizedLangComparator implements Comparator<String>, Serializable {
    private static final long serialVersionUID = 3482464035641314990L;
    private String defaultLang;
    private List<String> prioritizedList;

    public PrioritizedLangComparator(String defaultLang, List<String> prioritizedList) {
        this.defaultLang = defaultLang;
        this.prioritizedList = prioritizedList;
    }

    public int compare(String lang1, String lang2) {
        int value = val(lang2) - val(lang1);
        if (value == 0) {
            return lang1.compareToIgnoreCase(lang2);
        }
        return value;
    }

    private int val(String lang) {
        if (lang.equalsIgnoreCase(defaultLang)) {
            return 1000;
        }

        int index = prioritizedList.indexOf(lang);
        if (index > 0) {
            return prioritizedList.size() - index;
        }
        return -1;
    }
}
