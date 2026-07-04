/*
 * Copyright (C) 2001-2026 Food and Agriculture Organization of the
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

package org.fao.geonet.kernel.url;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.lang.StringUtils;

/**
 * Default implementation of {@link UrlWhitelistService}.
 * <p>
 * The whitelist is a plain multi-line string (as configured by an administrator in a system
 * setting). Each non-blank, non-comment line is treated as a wildcard pattern (using {@code *}
 * to match any sequence of characters) and matched against the whole URL.
 */
public class UrlWhitelistServiceImpl implements UrlWhitelistService {

    private static final String COMMENT_PREFIX = "#";

    @Override
    public boolean isUrlAllowed(String url, String whitelist) {
        if (StringUtils.isBlank(whitelist)) {
            // No whitelist configured: allow any URL.
            return true;
        }
        if (StringUtils.isBlank(url)) {
            return false;
        }

        for (String line : whitelist.split("\\r?\\n")) {
            String pattern = line.trim();
            if (pattern.isEmpty() || pattern.startsWith(COMMENT_PREFIX)) {
                continue;
            }
            if (FilenameUtils.wildcardMatch(url, pattern, IOCase.INSENSITIVE)) {
                return true;
            }
        }
        return false;
    }
}
