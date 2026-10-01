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
import org.fao.geonet.domain.Group;
import org.fao.geonet.domain.OperationAllowed;
import org.fao.geonet.domain.OperationAllowedId;
import org.fao.geonet.domain.Profile;
import org.fao.geonet.domain.ReservedGroup;
import org.fao.geonet.domain.ReservedOperation;
import org.fao.geonet.domain.User;
import org.fao.geonet.domain.UserGroup;
import org.fao.geonet.repository.OperationAllowedRepository;
import org.fao.geonet.services.AbstractServiceIntegrationTest;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.jdom.Namespace;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.context.request.ServletWebRequest;

import java.util.Arrays;

import static org.fao.geonet.api.records.formatters.FormatterWidth._100;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * The formatter output depends on who is viewing the record (for example the download links are
 * only shown to users that can download). The cache is shared, so only the output generated for
 * anonymous visitors can be stored in it.
 */
@ContextConfiguration(inheritLocations = true, locations = {
    "classpath:formatter-test-context.xml", "classpath:formatter-cache-test-context.xml"})
public class FormatterApiSharedCacheIntegrationTest extends AbstractServiceIntegrationTest {

    private static final String DOWNLOAD_URL = "http://example.org/shared-cache-test-download";
    private static final String FORMATTER = "xsl-view";

    @Autowired
    private ApplicationContext applicationContext;
    @Autowired
    private FormatterApi formatService;
    @Autowired
    private SystemInfo systemInfo;
    @Autowired
    private OperationAllowedRepository operationAllowedRepository;

    private String stagingProfile;
    private int metadataId;
    private String metadataUuid;
    private User groupMember;

    @Before
    public void setUp() throws Exception {
        stagingProfile = systemInfo.getStagingProfile();
        systemInfo.setStagingProfile(SystemInfo.STAGE_PRODUCTION);

        ServiceContext serviceContext = createServiceContext();
        loginAsAdmin(serviceContext);

        Element metadataXml = getSampleMetadataXml();
        addDownloadLink(metadataXml);
        AbstractMetadata metadata = injectMetadataInDb(metadataXml, serviceContext);
        metadataId = metadata.getId();
        metadataUuid = metadata.getUuid();

        // The group "all" can only view the record, the members of the test group can also download.
        Group group = _groupRepo.save(new Group().setName("shared-cache-test-group"));
        groupMember = _userRepo.save(new User()
            .setUsername("shared-cache-member")
            .setProfile(Profile.RegisteredUser)
            .setName("Member")
            .setSurname("Test"));
        _userGroupRepo.save(new UserGroup().setGroup(group).setUser(groupMember).setProfile(Profile.RegisteredUser));

        allow(ReservedGroup.all.getId(), ReservedOperation.view);
        allow(group.getId(), ReservedOperation.view);
        allow(group.getId(), ReservedOperation.download);
    }

    @After
    public void restoreEnvironment() throws Exception {
        systemInfo.setStagingProfile(stagingProfile);
        SecurityContextHolder.clearContext();
        RequestContextHolder.resetRequestAttributes();
        JeevesDelegatingFilterProxy.setApplicationContextAttributeKey(null);
    }

    @Test
    public void outputGeneratedForAUserIsNotReusedForAnonymousVisitors() throws Exception {
        String viewedByMember = render(loginAs(groupMember));
        assertTrue("The output for a user that can download has the download link", viewedByMember.contains(DOWNLOAD_URL));

        String viewedByAnonymous = render(loginAsAnonymous());
        assertFalse("The output for an anonymous visitor has no download link", viewedByAnonymous.contains(DOWNLOAD_URL));
    }

    @Test
    public void outputGeneratedForAnonymousVisitorsIsNotReusedForUsers() throws Exception {
        String viewedByAnonymous = render(loginAsAnonymous());
        assertFalse("The output for an anonymous visitor has no download link", viewedByAnonymous.contains(DOWNLOAD_URL));

        String viewedByMember = render(loginAs(groupMember));
        assertTrue("The output for a user that can download has the download link", viewedByMember.contains(DOWNLOAD_URL));

        // The output generated for the user is not stored in the cache
        String viewedByAnonymousAgain = render(loginAsAnonymous());
        assertFalse("The output for an anonymous visitor has no download link",
            viewedByAnonymousAgain.contains(DOWNLOAD_URL));
    }

    private void allow(int groupId, ReservedOperation operation) {
        operationAllowedRepository.save(new OperationAllowed(new OperationAllowedId()
            .setMetadataId(metadataId)
            .setGroupId(groupId)
            .setOperationId(operation.getId())));
    }

    private void addDownloadLink(Element metadataXml) throws Exception {
        Namespace gmd = Namespace.getNamespace("gmd", "http://www.isotc211.org/2005/gmd");
        Namespace gco = Namespace.getNamespace("gco", "http://www.isotc211.org/2005/gco");
        Element distribution = Xml.selectElement(metadataXml, "gmd:distributionInfo/gmd:MD_Distribution",
            Arrays.asList(gmd, gco));
        distribution.addContent(Xml.loadString(
            "<gmd:transferOptions xmlns:gmd=\"http://www.isotc211.org/2005/gmd\""
                + " xmlns:gco=\"http://www.isotc211.org/2005/gco\">"
                + "<gmd:MD_DigitalTransferOptions><gmd:onLine><gmd:CI_OnlineResource>"
                + "<gmd:linkage><gmd:URL>" + DOWNLOAD_URL + "</gmd:URL></gmd:linkage>"
                + "<gmd:protocol><gco:CharacterString>WWW:DOWNLOAD-1.0-http--download</gco:CharacterString></gmd:protocol>"
                + "<gmd:name><gco:CharacterString>Shared cache test download</gco:CharacterString></gmd:name>"
                + "</gmd:CI_OnlineResource></gmd:onLine></gmd:MD_DigitalTransferOptions>"
                + "</gmd:transferOptions>", false));
    }

    private String render(MockHttpSession session) throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setSession(session);
        request.setPathInfo("/eng/blahblah");
        MockHttpServletResponse response = new MockHttpServletResponse();
        final String srvAppContext = "srvAppContext";
        request.getServletContext().setAttribute(srvAppContext, applicationContext);
        JeevesDelegatingFilterProxy.setApplicationContextAttributeKey(srvAppContext);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        formatService.getRecordFormattedBy(FORMATTER, metadataUuid, _100, null, "eng", FormatType.html, true,
            new ServletWebRequest(request, response), request);

        return response.getContentAsString();
    }
}
