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

import com.google.common.annotations.VisibleForTesting;
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.domain.User;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.DelegatingFilterProxy;
import org.springframework.web.filter.GenericFilterBean;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA. User: Jesse Date: 11/13/13 Time: 5:15 PM
 */
public class JeevesDelegatingFilterProxy extends GenericFilterBean {
    private final static InheritableThreadLocal<String> applicationContextAttributeKey = new InheritableThreadLocal<>();
    private final Map<String, Filter> nodeIdToFilterMap = new HashMap<>();

    public static ServletContext getServletContext(ServletContext fallback) {
        if (ApplicationContextHolder.get() != null) {
            return ApplicationContextHolder.get().getBean(ServletContext.class);
        } else {
            return fallback;
        }
    }

    public static ConfigurableApplicationContext getApplicationContextFromServletContext(ServletContext servletContext) {
        final Object applicationContext = servletContext.getAttribute(applicationContextAttributeKey.get());
        return (ConfigurableApplicationContext) applicationContext;
    }

    @VisibleForTesting
    public static void setApplicationContextAttributeKey(String key) {
        applicationContextAttributeKey.set(key);
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            if (request instanceof HttpServletRequest) {
                String nodeId = User.NODE_APPLICATION_CONTEXT_KEY;

                applicationContextAttributeKey.set(nodeId);
                final ConfigurableApplicationContext applicationContext = getApplicationContextFromServletContext(getServletContext());
                ApplicationContextHolder.set(applicationContext);
                getDelegateFilter(nodeId, (WebApplicationContext) applicationContext).doFilter(request, response, filterChain);
            } else {
                response.getWriter().write(request.getClass().getName() + " is not a supported type of request");
            }
        } finally {
            ApplicationContextHolder.clear();
        }
    }


    private synchronized Filter getDelegateFilter(String nodeId, WebApplicationContext context) {
        Filter filter = this.nodeIdToFilterMap.get(nodeId);

        if (filter == null) {
            filter = new DelegatingFilterProxy(getFilterName(), context);
            this.nodeIdToFilterMap.put(nodeId, filter);
        }

        return filter;
    }

}
