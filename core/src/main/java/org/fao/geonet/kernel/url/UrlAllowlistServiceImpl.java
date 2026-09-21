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

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Default implementation of {@link UrlAllowlistService}.
 * <p>
 * The allowlist is a plain multi-line string (as configured by an administrator in a system
 * setting). Each non-blank, non-comment line is treated as a wildcard pattern (using {@code *}
 * to match any sequence of characters).
 * <p>
 * Matching is <b>host-anchored</b>: a pattern that contains a scheme ({@code ://}) is split into
 * its scheme, host (with optional port) and path components, and each component is matched
 * separately. Because the scheme and host never contain {@code /}, a {@code *} in the host part
 * cannot spill over into the path. This prevents bypasses such as
 * {@code https://evil.org/.example.org/} matching {@code https://*.example.org/*}. The host is
 * taken from the parsed URL (not the raw authority), so userinfo tricks like
 * {@code https://trusted.example.org@evil.org/} are matched on their real host ({@code evil.org}).
 * Within the path component {@code *} still matches any sequence of characters, including
 * {@code /}, so {@code https://example.org/*} matches arbitrarily deep paths.
 * <p>
 * A scheme-less pattern (one without {@code ://}, e.g. a bare {@code *}) is matched against the
 * whole URL, where {@code *} may span {@code /}. This is kept so a bare {@code *} means
 * "allow any URL"; such patterns are not host-anchored and should be avoided when restricting
 * to specific hosts.
 */
public class UrlAllowlistServiceImpl implements UrlAllowlistService {

    private static final String COMMENT_PREFIX = "#";
    private static final String SCHEME_SEPARATOR = "://";

    @Override
    public boolean isUrlAllowed(String url, String allowlist) {
        if (StringUtils.isBlank(allowlist)) {
            // No allowlist configured: allow any URL.
            return true;
        }
        if (StringUtils.isBlank(url)) {
            return false;
        }

        // Parse the target URL once, using the parsed host (not the raw authority) so that
        // userinfo tricks are matched on their real host. A URL that cannot be parsed as an
        // absolute URL with a host cannot be safely matched against a host-anchored pattern,
        // so it is rejected (fail closed).
        URI uri;
        try {
            uri = new URI(url.trim());
        } catch (URISyntaxException e) {
            return false;
        }
        String scheme = uri.getScheme();
        String host = uri.getHost();
        if (scheme == null || host == null) {
            return false;
        }
        String hostAndPort = uri.getPort() == -1 ? host : host + ":" + uri.getPort();
        String pathPart = buildPathPart(uri);

        for (String line : allowlist.split("\\r?\\n")) {
            String pattern = line.trim();
            if (pattern.isEmpty() || pattern.startsWith(COMMENT_PREFIX)) {
                continue;
            }
            if (matchesPattern(scheme, hostAndPort, pathPart, url.trim(), pattern)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Reconstructs the part of the URL after the authority (path, plus optional query and
     * fragment) so it can be matched against the path portion of a pattern. The raw (still
     * encoded) forms are used so matching is performed against the URL as written.
     */
    private String buildPathPart(URI uri) {
        StringBuilder pathPart = new StringBuilder();
        if (uri.getRawPath() != null) {
            pathPart.append(uri.getRawPath());
        }
        if (uri.getRawQuery() != null) {
            pathPart.append('?').append(uri.getRawQuery());
        }
        if (uri.getRawFragment() != null) {
            pathPart.append('#').append(uri.getRawFragment());
        }
        return pathPart.toString();
    }

    private boolean matchesPattern(String scheme, String hostAndPort, String pathPart,
                                   String fullUrl, String pattern) {
        int schemeSep = pattern.indexOf(SCHEME_SEPARATOR);
        if (schemeSep < 0) {
            // Scheme-less pattern (e.g. a bare "*"): match against the whole URL, where '*' may
            // span '/'. Not host-anchored; kept for backward compatibility / "allow any URL".
            return FilenameUtils.wildcardMatch(fullUrl, pattern, IOCase.INSENSITIVE);
        }

        String schemePattern = pattern.substring(0, schemeSep);
        String rest = pattern.substring(schemeSep + SCHEME_SEPARATOR.length());
        int pathSep = rest.indexOf('/');
        String hostPattern;
        String pathPattern;
        if (pathSep < 0) {
            // No path in the pattern (e.g. "https://vocabs.example.org"): allow any path on the
            // matched host.
            hostPattern = rest;
            pathPattern = "*";
        } else {
            hostPattern = rest.substring(0, pathSep);
            pathPattern = rest.substring(pathSep);
        }

        // Scheme and host are matched in isolation. As neither contains '/', a '*' in the host
        // pattern cannot reach into the path, which is what anchors the match to the host.
        if (!FilenameUtils.wildcardMatch(scheme, schemePattern, IOCase.INSENSITIVE)) {
            return false;
        }
        if (!FilenameUtils.wildcardMatch(hostAndPort, hostPattern, IOCase.INSENSITIVE)) {
            return false;
        }
        // Within the path, '*' is allowed to span '/' so deep paths can be matched.
        return FilenameUtils.wildcardMatch(pathPart, pathPattern, IOCase.INSENSITIVE);
    }
}
