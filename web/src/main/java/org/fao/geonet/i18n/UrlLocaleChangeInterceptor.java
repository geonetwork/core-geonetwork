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

package org.fao.geonet.i18n;

import org.fao.geonet.NodeInfo;
import org.springframework.beans.propertyeditors.LocaleEditor;
import org.springframework.util.Assert;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import org.springframework.web.servlet.support.RequestContextUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Locale;

/**
 * Strongly based on LocaleChangeInterceptor from Spring
 *
 * @author delawen
 */
public class UrlLocaleChangeInterceptor extends HandlerInterceptorAdapter {
    public static final Integer DEFAULT_URL_POSITION = 2;
    /**
     * Indicates the position of the segment of the Url path that contains
     * the language removing the context path. For example, if the request URL is
     * /geonetwork/srv/eng/catalog.signin
     * removing the servlet context results in
     * /srv/eng/catalog.signin
     * and urlPosition should be 2 to match the position or eng.
     */
    private Integer urlPosition = DEFAULT_URL_POSITION;

    /**
     * Indicates the position of the segment of the Url path that contains
     * the language removing the context path. For example, if the request URL is
     * <code>/geonetwork/srv/eng/catalog.signin</code>
     * removing the servlet context results in
     * <code>/srv/eng/catalog.signin</code>
     * and urlPosition should be 2 to match the position or eng.
     *
     * @param p the position of the URL path segment after the servlet context that contains the language.
     */
    public void setUrlPosition(Integer p) {
        Assert.isTrue(p > 0, "urlPosition must be greater than 0");
        this.urlPosition = p;
    }

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response, Object handler) {

        String pathAfterContext = request.getRequestURI().substring(request.getContextPath().length());
        String[] path = pathAfterContext.split("/");
        // URL path contains the node id as the first part of the URL
        // eg. /srv/eng/catalogue.search or /srv/api/...
        if ((urlPosition >= path.length)
            || (path.length > 1 && NodeInfo.EXCLUDED_NODE_IDS.contains(path[1]))
            || path.length > 2 && "api".equals(path[2])) {
            // matches URLs like /catalog/... /
            return true;
        }


        String localeCode = path[urlPosition];
        if (localeCode != null) {
            LocaleResolver localeResolver = RequestContextUtils
                .getLocaleResolver(request);
            if (localeResolver == null) {
                throw new IllegalStateException(
                    "No LocaleResolver found: not in a DispatcherServlet request?");
            }
            LocaleEditor localeEditor = new LocaleEditor();
            localeEditor.setAsText(localeCode);
            localeResolver.setLocale(request, response,
                (Locale) localeEditor.getValue());
        }


        // Proceed in any case.
        return true;
    }

}
