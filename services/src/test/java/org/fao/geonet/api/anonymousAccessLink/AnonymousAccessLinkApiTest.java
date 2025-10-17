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

package org.fao.geonet.api.anonymousAccessLink;

import com.fasterxml.jackson.databind.ObjectMapper;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.domain.AbstractMetadata;
import org.fao.geonet.kernel.search.IndexingMode;
import org.fao.geonet.services.AbstractServiceIntegrationTest;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class AnonymousAccessLinkApiTest extends AbstractServiceIntegrationTest {
	@Autowired
	private WebApplicationContext wac;

	private ServiceContext context;
	private MockHttpSession session;
	private MockMvc mockMvc;

	private ObjectMapper mapper = new ObjectMapper();

	@Before
	public void initWacContextAndSession() throws Exception {
		this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
		context = createServiceContext();
		session = loginAs(loginAsAdmin(context));
	}

	@Test
	public void createAnonymousAccessLink() throws Exception {
		AbstractMetadata md = injectMetadataInDb(getSampleMetadataXml(), context, true);

		MvcResult result = this.mockMvc.perform(post("/srv/api/anonymousAccessLink/{uuid}", md.getUuid())
						.session(session).accept(MediaType.parseMediaType("application/json")))
				.andExpect(status().isOk())
				.andExpect(content().contentType(API_JSON_EXPECTED_ENCODING))
				.andExpect(jsonPath("$.id").doesNotExist())
				.andReturn();

		String json = result.getResponse().getContentAsString();
		AnonymousAccessLinkDto createdAccessLink = mapper.readValue(json, AnonymousAccessLinkDto.class);
		assertEquals(md.getId(), createdAccessLink.getMetadataId());
		assertEquals(md.getUuid(), createdAccessLink.getMetadataUuid());
		assertNotNull(createdAccessLink.getHash());
	}

	@Test
	public void cannotBindTwoLinksToTheSameMd() throws Exception {
		AbstractMetadata md = injectMetadataInDb(getSampleMetadataXml(), context, true);
		this.mockMvc.perform(post("/srv/api/anonymousAccessLink/{uuid}", md.getUuid())
						.session(session).accept(MediaType.parseMediaType("application/json")))
				.andExpect(status().isOk());

		MvcResult result = this.mockMvc.perform(post("/srv/api/anonymousAccessLink/{uuid}", md.getUuid())
						.session(session).accept(MediaType.parseMediaType("application/json")))
				.andExpect(status().is4xxClientError())
				.andExpect(content().contentType(API_JSON_EXPECTED_ENCODING))
				.andReturn();

		String json = result.getResponse().getContentAsString();
		assertEquals("Resource already exists",  mapper.readValue(json, Map.class).get("message"));
	}

	@Test
	public void doesAnonymousAccessLinkExist() throws Exception {
		AbstractMetadata md = injectMetadataInDb(getSampleMetadataXml(), context, true);

		MvcResult result = this.mockMvc.perform(get("/srv/api/anonymousAccessLink/{uuid}", md.getUuid())
						.session(session).accept(MediaType.parseMediaType("application/json")))
				.andExpect(status().isOk())
				.andReturn();

		assertEquals("", result.getResponse().getContentAsString());

		this.mockMvc.perform(post("/srv/api/anonymousAccessLink/{uuid}", md.getUuid())
						.session(session).accept(MediaType.parseMediaType("application/json")))
				.andExpect(status().isOk());

		result = this.mockMvc.perform(get("/srv/api/anonymousAccessLink/{uuid}", md.getUuid())
						.session(session).accept(MediaType.parseMediaType("application/json")))
				.andExpect(status().isOk())
				.andReturn();

		String json = result.getResponse().getContentAsString();
		assertFalse(json.contains("\"hash\""));
		AnonymousAccessLinkDto createdAccessLink = mapper.readValue(json, AnonymousAccessLinkDto.class);
		assertEquals(md.getId(), createdAccessLink.getMetadataId());
		assertEquals(md.getUuid(), createdAccessLink.getMetadataUuid());
		assertNull(createdAccessLink.getHash());
	}

	@Test
	public void listAnonymousAccessLink() throws Exception {
		AbstractMetadata md1 = injectMetadataInDb(getSampleMetadataXml(), context, true, IndexingMode.full);
		AbstractMetadata md2 = injectMetadataInDb(getSampleMetadataXml(), context, true);
		this.mockMvc.perform(post("/srv/api/anonymousAccessLink/{uuid}", md1.getUuid())
						.session(session).accept(MediaType.parseMediaType("application/json")))
				.andExpect(status().isOk());
		this.mockMvc.perform(post("/srv/api/anonymousAccessLink/{uuid}", md2.getUuid())
						.session(session).accept(MediaType.parseMediaType("application/json")))
				.andExpect(status().isOk());

		MvcResult result = this.mockMvc.perform(get("/srv/api/anonymousAccessLink")
						.session(session).accept(MediaType.parseMediaType("application/json")))
				.andExpect(status().isOk())
				.andReturn();

		String json = result.getResponse().getContentAsString();
		AnonymousAccessLinkDto[] accessLinks = mapper.readValue(json, AnonymousAccessLinkDto[].class);
		AnonymousAccessLinkDto linkForMd1 = Arrays.stream(accessLinks)
				.filter(x -> md1.getUuid().equals(x.getMetadataUuid()))
				.findFirst().get();
		AnonymousAccessLinkDto linkForMd2 = Arrays.stream(accessLinks)
				.filter(x -> md2.getUuid().equals(x.getMetadataUuid()))
				.findFirst().get();
		assertEquals("Title",
				((HashMap)((HashMap) linkForMd1.getGetResultSource()).get("resourceTitleObject")).get("default"));
		assertNull(linkForMd2.getGetResultSource());
	}

	@Test
	public void deleteAccessLink() throws Exception {
		AbstractMetadata md = injectMetadataInDb(getSampleMetadataXml(), context, true);
		this.mockMvc.perform(post("/srv/api/anonymousAccessLink/{uuid}", md.getUuid())
						.session(session).accept(MediaType.parseMediaType("application/json")))
				.andExpect(status().isOk())
				.andReturn();

		this.mockMvc.perform(delete("/srv/api/anonymousAccessLink/{uuid}", md.getUuid())
						.session(session).contentType(MediaType.parseMediaType("application/json")))
				.andExpect(status().isOk());

		MvcResult result = this.mockMvc.perform(get("/srv/api/anonymousAccessLink/{uuid}", md.getUuid())
						.session(session).accept(MediaType.parseMediaType("application/json")))
				.andExpect(status().isOk())
				.andReturn();
		assertEquals("", result.getResponse().getContentAsString());
	}

}
