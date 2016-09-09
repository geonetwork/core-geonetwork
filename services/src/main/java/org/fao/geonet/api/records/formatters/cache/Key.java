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

package org.fao.geonet.api.records.formatters.cache;

import org.fao.geonet.api.records.formatters.FormatType;
import org.fao.geonet.api.records.formatters.FormatterWidth;

/**
 * A key for storing a value in the cache.
 *
 * @author Jesse on 3/5/2015.
 */
public class Key {
    public final int mdId;
    public final String lang;
    public final FormatType formatType;
    public final String formatterId;
    public final boolean hideWithheld;
    public final FormatterWidth width;

    /**
     * Constructor.
     *
     * @param mdId         the id of the metadata
     * @param lang         the current ui language
     * @param formatType   the content type of the output
     * @param formatterId  the formatter used to create the output
     * @param hideWithheld if true then elements in the metadata with the attribute
     *                     gco:nilreason="withheld" are being hidden
     */
    public Key(int mdId, String lang, FormatType formatType, String formatterId, boolean hideWithheld, FormatterWidth width) {
        this.mdId = mdId;
        this.lang = lang;
        this.formatType = formatType;
        this.formatterId = formatterId;
        this.hideWithheld = hideWithheld;
        this.width = width;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Key key = (Key) o;

        if (hideWithheld != key.hideWithheld) return false;
        if (mdId != key.mdId) return false;
        if (formatType != key.formatType) return false;
        if (width != key.width) return false;
        if (!formatterId.equals(key.formatterId)) return false;
        if (!lang.equals(key.lang)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = mdId;
        result = 31 * result + lang.hashCode();
        result = 31 * result + formatType.ordinal();
        result = 31 * result + width.ordinal();
        result = 31 * result + formatterId.hashCode();
        result = 31 * result + (hideWithheld ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Key{" +
            "mdId=" + mdId +
            ", lang='" + lang + '\'' +
            ", width=" + width +
            ", formatType=" + formatType +
            ", formatterId='" + formatterId + '\'' +
            ", hideWithheld=" + hideWithheld +
            '}';
    }
}
