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

import java.util.Objects;

/**
 * if the url starts with the provided text then write the url with a new prefix.
 *
 * @author Jesse on 11/28/2014.
 */
public class PrefixUrlRewrite implements ResolverRewriteDirective {
    private final String prefix;
    private final String replacement;

    public PrefixUrlRewrite(String prefix, String replacement) {
        Objects.requireNonNull(prefix);
        Objects.requireNonNull(replacement);
        this.prefix = prefix;
        this.replacement = replacement;
    }

    @Override
    public boolean appliesTo(String href) {
        return href.startsWith(prefix);
    }

    @Override
    public String rewrite(String href) {
        return replacement + href.substring(prefix.length());
    }

    @Override
    public Object getKey() {
        return prefix;
    }
}
