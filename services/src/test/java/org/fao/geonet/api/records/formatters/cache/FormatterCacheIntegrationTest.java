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

package org.fao.geonet.api.records.formatters.cache;

import jeeves.server.context.ServiceContext;
import org.fao.geonet.Constants;
import org.fao.geonet.SystemInfo;
import org.fao.geonet.api.records.formatters.FormatType;
import org.fao.geonet.api.records.formatters.FormatterWidth;
import org.fao.geonet.domain.OperationAllowed;
import org.fao.geonet.domain.ReservedOperation;
import org.fao.geonet.kernel.datamanager.IMetadataManager;
import org.fao.geonet.kernel.mef.MEFLibIntegrationTest.ImportMetadata;
import org.fao.geonet.kernel.search.submission.DirectDeletionSubmittor;
import org.fao.geonet.repository.OperationAllowedRepository;
import org.fao.geonet.repository.specification.OperationAllowedSpecs;
import org.fao.geonet.services.AbstractServiceIntegrationTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.ContextConfiguration;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.springframework.data.jpa.domain.Specification.where;

@ContextConfiguration(inheritLocations = true, locations = "classpath:formatter-cache-test-context.xml")
public class FormatterCacheIntegrationTest extends AbstractServiceIntegrationTest {

    public static final MockHttpServletRequest SERVLET_REQUEST = new MockHttpServletRequest("GET", "requesturi");
    @PersistenceContext
    EntityManager entityManager;
    @Autowired
    private OperationAllowedRepository operationAllowedRepository;
    @Autowired
    private IMetadataManager metadataManager;

    @Autowired
    private SystemInfo systemInfo;
    private FilesystemStore fsStore;
    private FormatterCache formatterCache;

    private String metadataId;
    private String stagingProfile;

    @Before
    public void setUp() throws Exception {
        ServiceContext context = createServiceContext();
        ImportMetadata importer = new ImportMetadata(this, context);
        importer.invoke();

        this.metadataId = importer.getMetadataIds().get(0);

        this.fsStore = _applicationContext.getBean(FilesystemStore.class);
        this.formatterCache = _applicationContext.getBean(FormatterCache.class);
        this.stagingProfile = systemInfo.getStagingProfile();
        systemInfo.setStagingProfile(SystemInfo.STAGE_PRODUCTION);
    }

    @After
    public void resetStagingProfile() throws Exception {
        systemInfo.setStagingProfile(stagingProfile);
    }

    @Test
    public void testUpdatesAfterMetadataUnpublished() throws Exception {
        final Specification<OperationAllowed> isPublished = OperationAllowedSpecs.isPublic(ReservedOperation.view);
        final Specification<OperationAllowed> hasMdId = OperationAllowedSpecs.hasMetadataId(metadataId);
        final OperationAllowed one = operationAllowedRepository.findOne(where(hasMdId).and(isPublished)).get();
        final long changeDate = new Date().getTime();
        final Key key = new Key(Integer.parseInt(metadataId), "eng", FormatType.html, "full_view", true, FormatterWidth._100);
        formatterCache.get(key, new ChangeDateValidator(changeDate), new TestLoader("result", changeDate, one != null), true);

        // TODOES Move to the API instead of Jeeves services
//        if (one != null) {
//            SERVLET_REQUEST.getSession();
//            publish.unpublish("eng", SERVLET_REQUEST, metadataId, false);
//            assertPublished(key, false);
//        }
//
//        MockHttpServletRequest r = new MockHttpServletRequest("GET", "requesturi");
//        r.getSession();
//        publish.publish("eng", r, metadataId, false);
//        assertPublished(key, true);
//
//        MockHttpServletRequest r1 = new MockHttpServletRequest("GET", "requesturi");
//        r1.getSession();
//        publish.unpublish("eng", r1, metadataId, false);
//        assertPublished(key, false);
//
//        MockHttpServletRequest r2 = new MockHttpServletRequest("GET", "requesturi");
//        r2.getSession();
//        publish.publish("eng", r2, metadataId, false);
//        assertPublished(key, true);
    }

    private void assertPublished(Key key, boolean published) throws IOException, SQLException {
        assertEquals(published, formatterCache.getPublished(key) != null);
        assertEquals(published, fsStore.getPublished(key) != null);
    }

    @Test
    public void testUpdatesAfterMetadataDeleted() throws Exception {
        final long changeDate = new Date().getTime();
        final Key key = new Key(Integer.parseInt(metadataId), "eng", FormatType.html, "full_view", true, FormatterWidth._100);
        formatterCache.get(key, new ChangeDateValidator(changeDate), new TestLoader("result", changeDate, true), true);

        metadataManager.deleteMetadata(createServiceContext(), metadataId, DirectDeletionSubmittor.INSTANCE);
        entityManager.flush();

        assertNull(formatterCache.getPublished(key));
        assertNull(fsStore.getPublished(key));
        assertNull(fsStore.get(key));
        final byte[] result = formatterCache.get(key, new ChangeDateValidator(changeDate), new TestLoader("newValue", changeDate,
            true), true);
        assertEquals("newValue", new String(result, Constants.CHARSET));
    }
}
