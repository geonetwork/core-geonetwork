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

package org.fao.geonet.api.records.formatters;

import jeeves.config.springutil.JeevesDelegatingFilterProxy;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.SystemInfo;
import org.fao.geonet.domain.AbstractMetadata;
import org.fao.geonet.domain.ISODate;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.domain.OperationAllowed;
import org.fao.geonet.domain.OperationAllowedId;
import org.fao.geonet.domain.ReservedGroup;
import org.fao.geonet.domain.ReservedOperation;
import org.fao.geonet.repository.MetadataRepository;
import org.fao.geonet.repository.OperationAllowedRepository;
import org.fao.geonet.services.AbstractServiceIntegrationTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.context.request.ServletWebRequest;

import static org.fao.geonet.api.records.formatters.FormatterWidth._100;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * The output of a formatter depends on the user that requests it, so the validators used for
 * conditional requests (ETag and Last-Modified) are only sent to anonymous visitors.
 */
@ContextConfiguration(inheritLocations = true, locations = "classpath:formatter-test-context.xml")
public class FormatterApiConditionalRequestIntegrationTest extends AbstractServiceIntegrationTest {

    @Autowired
    private ApplicationContext applicationContext;
    @Autowired
    private FormatterApi formatService;
    @Autowired
    private SystemInfo systemInfo;
    @Autowired
    private OperationAllowedRepository operationAllowedRepository;

    private String stagingProfile;
    private String metadataUuid;

    @Before
    public void setUp() throws Exception {
        stagingProfile = systemInfo.getStagingProfile();
        systemInfo.setStagingProfile(SystemInfo.STAGE_PRODUCTION);

        ServiceContext serviceContext = createServiceContext();
        loginAsAdmin(serviceContext);
        AbstractMetadata metadata = injectMetadataInDb(getSampleMetadataXml(), serviceContext);
        metadataUuid = metadata.getUuid();
        operationAllowedRepository.save(new OperationAllowed(new OperationAllowedId()
            .setMetadataId(metadata.getId())
            .setGroupId(ReservedGroup.all.getId())
            .setOperationId(ReservedOperation.view.getId())));
    }

    @After
    public void restoreEnvironment() {
        systemInfo.setStagingProfile(stagingProfile);
        SecurityContextHolder.clearContext();
        RequestContextHolder.resetRequestAttributes();
        JeevesDelegatingFilterProxy.setApplicationContextAttributeKey(null);
    }

    @Test
    public void anonymousVisitorsCanRevalidateTheOutput() throws Exception {
        MockHttpServletResponse first = request(loginAsAnonymous(), null);
        assertEquals(200, first.getStatus());
        assertNotNull("An anonymous visitor gets the validators", first.getHeader(HttpHeaders.ETAG));
        assertNotNull("An anonymous visitor gets the validators", first.getHeader(HttpHeaders.LAST_MODIFIED));

        MockHttpServletResponse revalidated = request(loginAsAnonymous(), first.getHeader(HttpHeaders.ETAG));
        assertEquals(304, revalidated.getStatus());
        assertEquals(0, revalidated.getContentAsByteArray().length);
    }

    @Test
    public void usersDoNotGetValidatorsNorNotModified() throws Exception {
        MockHttpServletResponse anonymous = request(loginAsAnonymous(), null);
        String validator = anonymous.getHeader(HttpHeaders.ETAG);

        MockHttpServletResponse plain = request(loginAsAdmin(), null);
        assertEquals(200, plain.getStatus());
        assertNull("The validators are not sent to a user", plain.getHeader(HttpHeaders.ETAG));
        assertNull("The validators are not sent to a user", plain.getHeader(HttpHeaders.LAST_MODIFIED));

        MockHttpServletResponse conditional = request(loginAsAdmin(), validator);
        assertEquals("The output for a user is never answered as not modified", 200, conditional.getStatus());
        assertTrue(conditional.getContentAsByteArray().length > 0);
        assertFalse(conditional.containsHeader(HttpHeaders.ETAG));
    }

    @Test
    public void editedRecordIsNotAnsweredAsNotModified() throws Exception {
        MockHttpServletResponse first = request(loginAsAnonymous(), null);
        String etag = first.getHeader(HttpHeaders.ETAG);
        String lastModified = first.getHeader(HttpHeaders.LAST_MODIFIED);

        MetadataRepository metadataRepository = applicationContext.getBean(MetadataRepository.class);
        Metadata metadata = metadataRepository.findOneByUuid(metadataUuid);
        metadata.getDataInfo().setChangeDate(new ISODate(System.currentTimeMillis() + 60000));
        metadataRepository.save(metadata);

        // A browser revalidates its copy sending both validators.
        MockHttpServletResponse revalidated = request(loginAsAnonymous(), etag, lastModified);
        assertEquals("The record was edited, the copy of the browser is old", 200, revalidated.getStatus());
        assertNotEquals("The validator changes with the record", etag, revalidated.getHeader(HttpHeaders.ETAG));
    }

    private MockHttpServletResponse request(MockHttpSession session, String ifNoneMatch) throws Exception {
        return request(session, ifNoneMatch, null);
    }

    private MockHttpServletResponse request(MockHttpSession session, String ifNoneMatch, String ifModifiedSince) throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/srv/api/records/" + metadataUuid + "/formatters/xsl-view");
        request.setSession(session);
        request.setPathInfo("/eng/blahblah");
        if (ifNoneMatch != null) {
            request.addHeader(HttpHeaders.IF_NONE_MATCH, ifNoneMatch);
        }
        if (ifModifiedSince != null) {
            request.addHeader(HttpHeaders.IF_MODIFIED_SINCE, ifModifiedSince);
        }
        MockHttpServletResponse response = new MockHttpServletResponse();
        final String srvAppContext = "srvAppContext";
        request.getServletContext().setAttribute(srvAppContext, applicationContext);
        JeevesDelegatingFilterProxy.setApplicationContextAttributeKey(srvAppContext);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        formatService.getRecordFormattedBy("xsl-view", metadataUuid, _100, null, "eng", FormatType.html, true,
            new ServletWebRequest(request, response), request);

        return response;
    }
}
