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

/**
 * Generic service checking whether a URL is allowed according to a whitelist of URL patterns.
 * <p>
 * This is deliberately decoupled from any particular system setting: callers are responsible
 * for retrieving the whitelist value that applies to their use case (e.g. a dedicated setting
 * for thesaurus URL imports, or in the future a separate one for harvester source URLs) and
 * pass it in. This keeps the matching logic reusable across features without this service
 * needing to know about each of its callers.
 */
public interface UrlWhitelistService {

    /**
     * Checks if a URL is allowed according to a whitelist of URL patterns.
     *
     * @param url       the URL to check.
     * @param whitelist the whitelist patterns, one per line. Each pattern may use {@code *} as
     *                  a wildcard matching any sequence of characters (including none), e.g.
     *                  {@code https://example.org/*} or {@code https://*.example.org/vocab/*}.
     *                  Lines that are blank, or start with {@code #}, are ignored. A blank (or
     *                  {@code null}) whitelist allows any URL.
     * @return {@code true} if the whitelist is blank, or if the URL matches at least one
     * whitelist pattern; {@code false} otherwise.
     */
    boolean isUrlAllowed(String url, String whitelist);
}
