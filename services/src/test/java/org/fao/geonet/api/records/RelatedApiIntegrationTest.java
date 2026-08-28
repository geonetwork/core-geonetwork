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
package org.fao.geonet.api.records;

import jeeves.server.context.ServiceContext;
import org.fao.geonet.domain.AbstractMetadata;
import org.fao.geonet.domain.OperationAllowed;
import org.fao.geonet.domain.ReservedGroup;
import org.fao.geonet.domain.ReservedOperation;
import org.fao.geonet.kernel.datamanager.IMetadataIndexer;
import org.fao.geonet.kernel.search.IndexingMode;
import org.fao.geonet.kernel.search.submission.DirectIndexSubmitter;
import org.fao.geonet.repository.OperationAllowedRepository;
import org.fao.geonet.services.AbstractServiceIntegrationTest;
import org.jdom.Element;
import org.jdom.Namespace;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Verifies that the related-records API applies the view-privilege filter, so a
 * restricted record related to a public one is not disclosed to unauthorized users.
 *
 * <p>Requires a running Elasticsearch instance (see project CLAUDE.md): run with the
 * {@code -Pit} profile via the failsafe goals.</p>
 */
public class RelatedApiIntegrationTest extends AbstractServiceIntegrationTest {

    private static final Namespace GMD = Namespace.getNamespace("gmd", "http://www.isotc211.org/2005/gmd");
    private static final Namespace GCO = Namespace.getNamespace("gco", "http://www.isotc211.org/2005/gco");

    @Autowired
    private WebApplicationContext wac;
    @Autowired
    private IMetadataIndexer metadataIndexer;
    @Autowired
    private OperationAllowedRepository operationAllowedRepository;

    private ServiceContext context;

    // Public parent record.
    private String publicUuid;
    // Restricted child of the public record (no view privilege for anonymous users).
    private String restrictedUuid;

    @Before
    public void setUp() throws Exception {
        this.context = createServiceContext();
        loginAsAdmin(context);

        // Public parent record, viewable by the "all" (anonymous) group.
        AbstractMetadata parent = injectMetadataInDb(getSampleMetadataXml(), context);
        publicUuid = parent.getUuid();
        grantView(parent.getId(), ReservedGroup.all.getId());

        // Restricted record declaring the public record as its parent, so it surfaces
        // as a "children" relation. No view privilege is granted to any group.
        Element childXml = getSampleMetadataXml();
        Element characterSet = childXml.getChild("characterSet", GMD);
        Element parentIdentifier = new Element("parentIdentifier", GMD)
            .addContent(new Element("CharacterString", GCO).setText(publicUuid));
        childXml.addContent(childXml.indexOf(characterSet) + 1, parentIdentifier);
        AbstractMetadata child = injectMetadataInDb(childXml, context);
        restrictedUuid = child.getUuid();

        metadataIndexer.indexMetadata(String.valueOf(parent.getId()), DirectIndexSubmitter.INSTANCE, IndexingMode.full);
        metadataIndexer.indexMetadata(String.valueOf(child.getId()), DirectIndexSubmitter.INSTANCE, IndexingMode.full);
    }

    private void grantView(int metadataId, int groupId) {
        OperationAllowed op = new OperationAllowed();
        op.getId().setMetadataId(metadataId).setGroupId(groupId)
            .setOperationId(ReservedOperation.view.getId());
        operationAllowedRepository.save(op);
    }

    @Test
    public void anonymousDoesNotSeeRestrictedRelatedRecord() throws Exception {
        MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
        MockHttpSession session = loginAsAnonymous();

        mockMvc.perform(get("/srv/api/records/" + publicUuid + "/related")
                .param("type", "children")
                .session(session)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().string(not(containsString(restrictedUuid))));
    }

    @Test
    public void adminSeesRestrictedRelatedRecord() throws Exception {
        MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
        MockHttpSession session = loginAsAdmin();

        mockMvc.perform(get("/srv/api/records/" + publicUuid + "/related")
                .param("type", "children")
                .session(session)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString(restrictedUuid)));
    }
}
