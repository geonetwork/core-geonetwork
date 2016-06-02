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

import java.util.Locale;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.propertyeditors.LocaleEditor;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import org.springframework.web.servlet.support.RequestContextUtils;

/**
 * Strongly based on LocaleChangeInterceptor from Spring
 *
 * @author delawen
 */
public class UrlLocaleChangeInterceptor extends HandlerInterceptorAdapter {
    public static final Integer DEFAULT_URL_POSITION = 0;
    private Integer urlPosition = DEFAULT_URL_POSITION;

    public void setUrlPosition(Integer p) {
        this.urlPosition = p;
    }

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response, Object handler)
        throws ServletException {

        String url = request.getRequestURI();
        String[] path = url.split("/");
        String newLocale = null;
        Integer position = urlPosition;

        if (path.length >= position) {
            newLocale = path[position];
            if (newLocale != null) {
                LocaleResolver localeResolver = RequestContextUtils
                    .getLocaleResolver(request);
                if (localeResolver == null) {
                    throw new IllegalStateException(
                        "No LocaleResolver found: not in a DispatcherServlet request?");
                }
                LocaleEditor localeEditor = new LocaleEditor();
                localeEditor.setAsText(newLocale);
                localeResolver.setLocale(request, response,
                    (Locale) localeEditor.getValue());
            }
        }

        // Proceed in any case.
        return true;
    }

}
