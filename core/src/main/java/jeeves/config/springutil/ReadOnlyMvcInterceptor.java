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

import jeeves.services.ReadWriteController;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import static jeeves.config.springutil.JeevesDelegatingFilterProxy.getServletContext;

/**
 * This class intercepts MVC requests and verifies that: <ol> <li>The system is no in readonly
 * mode</li> <li>If the system is readonly then only allow tests <em>without</em> the {@link
 * jeeves.services.ReadWriteController} annotation</li> </ol>
 *
 * @author Jesse on 6/4/2014.
 */
public class ReadOnlyMvcInterceptor implements HandlerInterceptor {
    public static final String SERVLET_CONTEXT_ATTR_KEY = "readOnlyMode";

    @Autowired
    ServletContext context;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (handler instanceof HandlerMethod) {
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            Boolean isReadOnly = (Boolean) getServletContext(context).getAttribute(SERVLET_CONTEXT_ATTR_KEY);
            if (isReadOnly == null) {
                isReadOnly = false;
            }
            if (isReadOnly &&
                handlerMethod.getBean() != null &&
                handlerMethod.getBean().getClass().getAnnotation(ReadWriteController.class) != null) {
                throw new InReadOnlyModeException(request.getPathInfo());
            }
        }

        return true;
    }

    private static class InReadOnlyModeException extends RuntimeException {
        InReadOnlyModeException(String req) {
            super("Server is in Readonly mode, requested service is not allowed when in read only mode: " + req);
        }
    }
}
