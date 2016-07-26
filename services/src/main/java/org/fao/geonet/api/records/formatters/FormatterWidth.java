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

package org.fao.geonet.api.records.formatters;

/**
 * An indicator of how wide the html container which will contain the formatted metadata with
 * respect to the full width of the screen.  This is required because when embedded, the media
 * queries aren't useful for determining the which css to apply. For example bootstrap grid is
 * broken when embedded.
 *
 * @author Jesse on 3/12/2015.
 */
public enum FormatterWidth {
    /**
     * Indicates the element that the formatter will be embedded in is approximately 25% of the
     * width of the screen.
     */
    _25,
    /**
     * Indicates the element that the formatter will be embedded in is approximately 50% of the
     * width of the screen.
     */
    _50,
    /**
     * Indicates the element that the formatter will be embedded in is approximately 75% of the
     * width of the screen.
     */
    _75,
    /**
     * Indicates the element that the formatter will be embedded in is approximately 100% of the
     * width of the screen.
     */
    _100
}
