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

package org.fao.geonet.api.records.attachments;

import jeeves.server.context.ServiceContext;
import org.fao.geonet.domain.MetadataResourceVisibility;
import org.fao.geonet.kernel.datamanager.IMetadataUtils;
import org.fao.geonet.kernel.mef.MEFLibIntegrationTest;
import org.fao.geonet.services.AbstractServiceIntegrationTest;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.attribute.FileTime;
import java.time.Instant;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class AttachmentsApiTest extends AbstractServiceIntegrationTest {

    private static final String FILENAME = "thumbnail.png";

    @Autowired
    private WebApplicationContext wac;

    @Autowired
    private IMetadataUtils metadataUtils;

    @Autowired
    @Qualifier("resourceStore")
    private Store store;

    private MockMvc mockMvc;
    private MockHttpSession session;
    private String url;

    @Before
    public void setUp() throws Exception {
        ServiceContext context = createServiceContext();
        loginAsAdmin(context);
        String metadataId = new MEFLibIntegrationTest.ImportMetadata(this, context).invoke().getMetadataIds().get(0);
        String metadataUuid = metadataUtils.getMetadataUuid(metadataId);

        BufferedImage image = new BufferedImage(100, 80, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream png = new ByteArrayOutputStream();
        ImageIO.write(image, "png", png);
        store.putResource(context, metadataUuid,
            new MockMultipartFile(FILENAME, FILENAME, "image/png", png.toByteArray()),
            MetadataResourceVisibility.PUBLIC, true);
        // Date the file in the past, so a Last-Modified set to the time of the request is noticed.
        try (Store.ResourceHolder holder = store.getResource(context, metadataUuid, FILENAME, true)) {
            Files.setLastModifiedTime(FilesystemStore.getResourcePath(holder.getResource(), context),
                FileTime.from(Instant.parse("2020-01-01T00:00:00Z")));
        }

        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
        session = loginAsAdmin();
        url = "/srv/api/records/" + metadataUuid + "/attachments/" + FILENAME;
    }

    @Test
    public void getResourceNotModified() throws Exception {
        String eTag = mockMvc.perform(get(url).session(session))
            .andExpect(status().isOk())
            .andReturn().getResponse().getHeader(HttpHeaders.ETAG);
        assertNotNull(eTag);

        mockMvc.perform(get(url).session(session).header(HttpHeaders.IF_NONE_MATCH, eTag))
            .andExpect(status().isNotModified());
    }

    @Test
    public void getResizedResourceNotModified() throws Exception {
        var original = mockMvc.perform(get(url).session(session)).andReturn().getResponse();
        String originalETag = original.getHeader(HttpHeaders.ETAG);
        var resized = mockMvc.perform(get(url).session(session).param("size", "50"))
            .andExpect(status().isOk())
            .andExpect(header().string(HttpHeaders.CONTENT_TYPE, "image/png"))
            .andReturn().getResponse();
        String eTag = resized.getHeader(HttpHeaders.ETAG);
        String lastModified = resized.getHeader(HttpHeaders.LAST_MODIFIED);
        assertNotNull(eTag);
        assertNotEquals("A resized image is a different representation", originalETag, eTag);
        assertTrue("A resized image is generated on each request, its ETag is weak", eTag.startsWith("W/\""));
        assertEquals("Last-Modified is the date of the file, not the time of the request",
            original.getHeader(HttpHeaders.LAST_MODIFIED), lastModified);

        // A browser revalidating its cached resized image must get a 304, not the image again.
        mockMvc.perform(get(url).session(session).param("size", "50").header(HttpHeaders.IF_NONE_MATCH, eTag))
            .andExpect(status().isNotModified())
            .andExpect(content().bytes(new byte[0]));
        mockMvc.perform(get(url).session(session).param("size", "50").header(HttpHeaders.IF_MODIFIED_SINCE, lastModified))
            .andExpect(status().isNotModified());

        // The ETag of one size must not validate another size.
        mockMvc.perform(get(url).session(session).param("size", "60").header(HttpHeaders.IF_NONE_MATCH, eTag))
            .andExpect(status().isOk());
    }

    @Test
    public void getResizedResourceIgnoresRange() throws Exception {
        mockMvc.perform(get(url).session(session))
            .andExpect(header().string(HttpHeaders.ACCEPT_RANGES, "bytes"));

        // Ranges apply to the stored file only: a resized image is always sent whole,
        // even when the range would not fit the stored file.
        mockMvc.perform(get(url).session(session).param("size", "50").header(HttpHeaders.RANGE, "bytes=1000000-"))
            .andExpect(status().isOk())
            .andExpect(header().doesNotExist(HttpHeaders.ACCEPT_RANGES))
            .andExpect(header().doesNotExist(HttpHeaders.CONTENT_RANGE))
            .andExpect(header().string(HttpHeaders.CONTENT_TYPE, "image/png"));
    }

    @Test
    public void getResizedResourceWithInvalidSize() throws Exception {
        String lastModified = mockMvc.perform(get(url).session(session))
            .andReturn().getResponse().getHeader(HttpHeaders.LAST_MODIFIED);

        // An invalid size is rejected, even when the conditional headers would allow a 304.
        mockMvc.perform(get(url).session(session).param("size", "5000").header(HttpHeaders.IF_MODIFIED_SINCE, lastModified))
            .andExpect(status().isBadRequest())
            .andExpect(header().doesNotExist(HttpHeaders.ETAG));
    }
}
