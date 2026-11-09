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
package jeeves.config.springutil;

import jeeves.services.ReadWriteController;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.NodeInfo;
import org.junit.After;
import org.junit.Test;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.method.HandlerMethod;

import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public class ReadOnlyMvcInterceptorTest {

    @ReadWriteController
    public static class WriteController {
        public void save() {
        }
    }

    public static class ReadController {
        public void get() {
        }
    }

    private final ReadOnlyMvcInterceptor interceptor = new ReadOnlyMvcInterceptor();

    @After
    public void resetReadOnly() {
        NodeInfo.setReadOnly(false);
    }

    private boolean preHandle(String httpMethod, Object controller, String method) throws Exception {
        HandlerMethod handler = new HandlerMethod(controller, controller.getClass().getMethod(method));
        return interceptor.preHandle(new MockHttpServletRequest(httpMethod, "/srv/api/test"), new MockHttpServletResponse(), handler);
    }

    @Test
    public void allowsEverythingWhenReadWrite() throws Exception {
        assertTrue(preHandle("POST", new WriteController(), "save"));
        assertTrue(preHandle("POST", new ReadController(), "get"));
    }

    @Test
    public void blocksReadWriteControllersWhenReadOnly() throws Exception {
        // Switch mode through GeonetContext like the DB heartbeat does, to check both share the same flag.
        new GeonetContext(mock(ApplicationContext.class), false).setReadOnly(true);
        assertTrue(NodeInfo.isReadOnly());

        assertTrue(preHandle("POST", new ReadController(), "get"));
        assertTrue(preHandle("GET", new WriteController(), "save"));
        for (String httpMethod : new String[]{"POST", "PUT", "PATCH", "DELETE"}) {
            assertThrows(ReadOnlyMvcInterceptor.InReadOnlyModeException.class,
                () -> preHandle(httpMethod, new WriteController(), "save"));
        }

        // Controllers secured with @PreAuthorize are CGLIB proxies.
        ProxyFactory proxyFactory = new ProxyFactory(new WriteController());
        proxyFactory.setProxyTargetClass(true);
        Object proxy = proxyFactory.getProxy();
        assertThrows(ReadOnlyMvcInterceptor.InReadOnlyModeException.class,
            () -> preHandle("POST", proxy, "save"));
    }
}
