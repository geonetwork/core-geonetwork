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

package jeeves.config.springutil;

import com.google.common.escape.Escaper;
import com.google.common.net.UrlEscapers;

import org.apache.commons.lang.StringUtils;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.WebAttributes;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * Handles Access Denied exceptions during spring security.
 *
 * Created by Jesse on 12/5/13.
 */
public class JeevesAccessDeniedHandler implements AccessDeniedHandler {

    private String _errorPage;
    private Escaper _escaper = UrlEscapers.urlPathSegmentEscaper();
    private AntPathRequestMatcher matcher;


    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws
        IOException, ServletException {
        if (!response.isCommitted()) {
            if (matcher != null && matcher.matches(request)) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, accessDeniedException.getMessage());
            }
            if (_errorPage != null) {
                request.setAttribute(WebAttributes.ACCESS_DENIED_403, accessDeniedException);
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                final String referer = _escaper.escape(request.getRequestURI());
                RequestDispatcher dispatcher = request.getRequestDispatcher(_errorPage + "?referer=" + referer);
                dispatcher.forward(request, response);
            } else {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, accessDeniedException.getMessage());
            }
        }
    }

    public void setErrorPage(String errorPage) {
        this._errorPage = errorPage;
    }

    /**
     * The URLs matching this pattern will only receive the HTTP error code and an empty body, even if the errorPage is
     * set.
     * @param pattern
     */
    public void setOnlyStatusResponsePages(String pattern) {
        if (StringUtils.isNotBlank(pattern)) {
            this.matcher = new AntPathRequestMatcher(pattern);
        }
    }
}
