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

import org.fao.geonet.NodeInfo;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Set;

/**
 * This class intercepts MVC requests and, when the system is in readonly mode, rejects requests
 * other than GET, HEAD and OPTIONS to controllers with the {@link
 * jeeves.services.ReadWriteController} annotation.
 *
 * @author Jesse on 6/4/2014.
 */
public class ReadOnlyMvcInterceptor implements HandlerInterceptor {
    private static final Set<String> READ_METHODS = Set.of("GET", "HEAD", "OPTIONS");

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (handler instanceof HandlerMethod) {
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            // Controllers mix read and write operations, only block requests that may write.
            // getBeanType() returns the user class, the bean may be a security/AOP proxy.
            if (NodeInfo.isReadOnly() &&
                !READ_METHODS.contains(request.getMethod()) &&
                handlerMethod.getBeanType().isAnnotationPresent(ReadWriteController.class)) {
                throw new InReadOnlyModeException(request.getPathInfo());
            }
        }

        return true;
    }

    public static class InReadOnlyModeException extends RuntimeException {
        InReadOnlyModeException(String req) {
            super("Server is in Readonly mode, requested service is not allowed when in read only mode: " + req);
        }
    }
}
