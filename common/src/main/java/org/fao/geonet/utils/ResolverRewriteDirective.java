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

package org.fao.geonet.utils;

/**
 * Represents a strategy for rewriting the href of a XSLT or XML import.
 *
 * @author Jesse on 11/28/2014.
 */
public interface ResolverRewriteDirective {
    /**
     * Test if this should be applied to the href.
     */
    boolean appliesTo(String href);

    /**
     * Modify the url for locating the referenced stylesheet.
     */
    String rewrite(String href);

    /**
     * Each directive must have a key that identifies the directive so that it is possible to find
     * and replace the directive if (for example) the resource moves.  An example of this is in
     * testing.  Each test might need the value to be different.
     */
    Object getKey();
}
