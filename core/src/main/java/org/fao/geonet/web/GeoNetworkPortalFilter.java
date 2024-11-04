/*
 * Copyright (C) 2001-2023 Food and Agriculture Organization of the
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

package org.fao.geonet.web;

import org.apache.commons.lang3.StringUtils;
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.NodeInfo;
import org.fao.geonet.domain.SourceType;
import org.fao.geonet.repository.SourceRepository;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Filter to check that the portal requested is a valid one, otherwise redirects to the default portal:
 * <p>
 * - Default srv portal.
 * - Valid portal defined in the sources table
 */
public class GeoNetworkPortalFilter implements javax.servlet.Filter {
    private static final String EXCLUDED_URL_PATHS = "excludedPaths";

    private static final String URL_PATH_SEPARATOR = "/";

    /**
     * Ignored application paths.
     **/
    private List<AntPathRequestMatcher> excludedPathsMatchers = new ArrayList<>();


    @Override
    public void init(FilterConfig config) {
        String excludedPathsValue = config.getInitParameter(EXCLUDED_URL_PATHS);
        if (StringUtils.isNotEmpty(excludedPathsValue)) {
            excludedPathsMatchers = Arrays.stream(excludedPathsValue.split(","))
                .map(StringUtils::trimToEmpty)
                .filter(StringUtils::isNotEmpty)
                .map(AntPathRequestMatcher::new)
                .collect(Collectors.toList());
        }
    }

    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain filterChain) throws IOException, ServletException {
        HttpServletResponse httpResp = (HttpServletResponse) resp;
        HttpServletRequest httpReq = (HttpServletRequest) req;

        if (!ignoreUrl(httpReq)) {
            String portal = "";

            // Check the url format: /portal/lang/service/..., /portal/api/service/...
            if (httpReq.getPathInfo().split(URL_PATH_SEPARATOR).length > 3) {
                portal = httpReq.getPathInfo().split(URL_PATH_SEPARATOR)[1];
            } else {
                portal = NodeInfo.DEFAULT_NODE;
            }

            if (!NodeInfo.DEFAULT_NODE.equals(portal)) {
                SourceRepository sourceRepository = ApplicationContextHolder.get().getBean(SourceRepository.class);
                // Check the portal exists and it's of type subportal, otherwise redirect to the default portal
                boolean redirectToDefaultPortal = !sourceRepository.existsByUuidAndType(portal, SourceType.subportal);

                if (redirectToDefaultPortal) {
                    String newPath = httpReq.getPathInfo().replace(URL_PATH_SEPARATOR + portal + URL_PATH_SEPARATOR,
                        URL_PATH_SEPARATOR + NodeInfo.DEFAULT_NODE + URL_PATH_SEPARATOR);
                    if (httpReq.getQueryString() != null) {
                        newPath = newPath + "?" + httpReq.getQueryString();
                    }

                    httpResp.sendRedirect(httpReq.getContextPath() + newPath);
                    return;
                }
            }
        }

        filterChain.doFilter(req, resp);
    }

    @Override
    public void destroy() {
        // No cleanup required
    }

    private boolean ignoreUrl(HttpServletRequest request) {
        if (StringUtils.isEmpty(request.getPathInfo())) {
            return true;
        } else {
            return excludedPathsMatchers.stream()
                .anyMatch(matcher -> matcher.matches(request));
        }
    }
}
