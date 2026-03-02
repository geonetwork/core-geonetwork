/*
 * Copyright (C) 2001-2025 Food and Agriculture Organization of the
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

package org.fao.geonet.api.es;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.SchemaManager;
import org.fao.geonet.kernel.schema.MetadataSchema;
import org.fao.geonet.kernel.schema.MetadataSchemaOperationFilter;
import org.fao.geonet.repository.UserGroupRepository;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.data.jpa.domain.Specification;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

public class EsHTTPProxyTest {

    @Mock
    private SchemaManager schemaManager;

    @Mock
    private ConfigurableApplicationContext applicationContext;

    @Mock
    private UserGroupRepository userGroupRepository;

    @InjectMocks
    private EsHTTPProxy esHTTPProxy = new EsHTTPProxy();

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        ApplicationContextHolder.set(applicationContext);
        when(applicationContext.getBean(UserGroupRepository.class)).thenReturn(userGroupRepository);
    }

    @Test
    public void testProcessMetadataSchemaFiltersGroupOwner() throws Exception {
        // 1. Setup
        ServiceContext context = new ServiceContext("default", applicationContext, new HashMap<>(), null);
        UserSession userSession = spy(new UserSession());
        when(userSession.isAuthenticated()).thenReturn(true);
        when(userSession.getUserIdAsInt()).thenReturn(42);

        context.setUserSession(userSession);

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode doc = mapper.createObjectNode();
        ObjectNode source = mapper.createObjectNode();
        source.put(Geonet.IndexFieldNames.GROUP_OWNER, 1);
        source.put("someField", "someValue");
        doc.set("_source", source);
        doc.put("edit", false);
        doc.put("download", false);
        doc.put("dynamic", false);

        // Mock MetadataSchema
        MetadataSchema mds = mock(MetadataSchema.class);
        MetadataSchemaOperationFilter groupOwnerFilter = new MetadataSchemaOperationFilter(null, "$.someField", null);
        when(mds.getOperationFilter("groupOwner")).thenReturn(groupOwnerFilter);

        // Mock AccessManager.getGroups via UserGroupRepository
        // When user is in group 1
        when(userGroupRepository.findGroupIds(any(Specification.class))).thenReturn(Arrays.asList(1));

        // 2. Call private method using reflection
        Method method = EsHTTPProxy.class.getDeclaredMethod("processMetadataSchemaFilters", ServiceContext.class, MetadataSchema.class, ObjectNode.class);
        method.setAccessible(true);
        method.invoke(esHTTPProxy, context, mds, doc);

        // 3. Assertions for user in group
        assertTrue("someField should exist when user is in groupOwner", doc.get("_source").has("someField"));

        // --- Test case where user is NOT in group ---
        // When user is NOT in group 1 (e.g. in group 2)
        when(userGroupRepository.findGroupIds(any(Specification.class))).thenReturn(Arrays.asList(2));

        // re-create doc
        doc = mapper.createObjectNode();
        source = mapper.createObjectNode();
        source.put(Geonet.IndexFieldNames.GROUP_OWNER, 1);
        source.put("someField", "someValue");
        doc.set("_source", source);
        doc.put("edit", false);
        doc.put("download", false);
        doc.put("dynamic", false);

        method.invoke(esHTTPProxy, context, mds, doc);

        assertFalse("someField should be filtered when user is not in groupOwner", doc.get("_source").has("someField"));
    }
}
