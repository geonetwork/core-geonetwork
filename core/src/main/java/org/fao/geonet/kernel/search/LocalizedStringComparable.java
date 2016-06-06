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

package org.fao.geonet.kernel.search;

import java.text.Collator;
import java.util.Locale;

class LocalizedStringComparable implements Comparable<LocalizedStringComparable> {
    public final String _wrapped;
    private final Collator _comparator;

    public LocalizedStringComparable(String wrapped, Locale locale) {
        this._wrapped = wrapped;
        _comparator = java.text.Collator.getInstance(locale);
    }

    public int compareTo(LocalizedStringComparable anotherString) {
        return _comparator.compare(_wrapped, anotherString._wrapped);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((_comparator == null) ? 0 : _comparator.hashCode());
        result = prime * result + ((_wrapped == null) ? 0 : _wrapped.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        LocalizedStringComparable other = (LocalizedStringComparable) obj;

        return compareTo(other) == 0;
    }


}
