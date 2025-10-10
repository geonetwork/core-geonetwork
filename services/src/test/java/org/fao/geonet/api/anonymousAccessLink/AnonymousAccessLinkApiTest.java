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
import org.fao.geonet.domain.AnonymousAccessLink;
import org.fao.geonet.repository.AnonymousAccessLinkRepository;
import org.fao.geonet.services.AbstractServiceIntegrationTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class AnonymousAccessLinkApiTest extends AbstractServiceIntegrationTest {
	@Autowired
	private WebApplicationContext wac;

	@Autowired
	private AnonymousAccessLinkRepository anonymousAccessLinkRepository;

	private MockMvc mockMvc;

	@Test
	public void createAnonymousAccessLink() throws Exception {
		this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
		ServiceContext context = createServiceContext();
		MockHttpSession session = loginAs(loginAsAdmin(context));
		AbstractMetadata md = injectMetadataInDb(getSampleMetadataXml(), context, true);
		ObjectMapper mapper = new ObjectMapper();

		MvcResult result = this.mockMvc.perform(post("/srv/api/anonymousAccessLink")
						.session(session)
						.content(jsonRequestBodyForCreate(md))
						.contentType(MediaType.parseMediaType("application/json"))
						.accept(MediaType.parseMediaType("application/json")))
				.andExpect(status().isOk())
				.andExpect(content().contentType(API_JSON_EXPECTED_ENCODING))
				.andExpect(jsonPath("$.id").doesNotExist())
				.andReturn();

		String json = result.getResponse().getContentAsString();
		AnonymousAccessLink createdAccessLink = mapper.readValue(json, AnonymousAccessLink.class);
		assertEquals(md.getId(), createdAccessLink.getMetadataId());
		assertEquals(md.getUuid(), createdAccessLink.getMetadataUuid());
		assertEquals(0, createdAccessLink.getId());

		AnonymousAccessLink inDb = anonymousAccessLinkRepository.findOneByHash(createdAccessLink.getHash());
		assertEquals(md.getId(), inDb.getMetadataId());
		assertEquals(md.getUuid(), inDb.getMetadataUuid());
		assertNotEquals(0, inDb.getId());

		assertEquals(createdAccessLink.getHash(), inDb.getHash());
	}

	@Test
	public void listAnonymousAccessLink() throws Exception {
		ServiceContext context = createServiceContext();
		AbstractMetadata md1 = injectMetadataInDb(getSampleMetadataXml(), context, true);
		AbstractMetadata md2 = injectMetadataInDb(getSampleMetadataXml(), context, true);
		this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
		MockHttpSession session = loginAs(loginAsAdmin(context));
		this.mockMvc.perform(post("/srv/api/anonymousAccessLink")
					.session(session)
					.content(jsonRequestBodyForCreate(md1))
					.contentType(MediaType.parseMediaType("application/json"))
					.accept(MediaType.parseMediaType("application/json")))
				.andExpect(status().isOk());
		this.mockMvc.perform(post("/srv/api/anonymousAccessLink")
						.session(session)
						.content(jsonRequestBodyForCreate(md2))
						.contentType(MediaType.parseMediaType("application/json"))
						.accept(MediaType.parseMediaType("application/json")))
				.andExpect(status().isOk());

		MvcResult result = this.mockMvc.perform(get("/srv/api/anonymousAccessLink")
						.session(session)
						.accept(MediaType.parseMediaType("application/json")))
				.andExpect(status().isOk())
				.andReturn();

		String json = result.getResponse().getContentAsString();
		ObjectMapper mapper = new ObjectMapper();
		AnonymousAccessLink[] accessLinks = mapper.readValue(json, AnonymousAccessLink[].class);
		List<String> referencedMd = Arrays.stream(accessLinks) //
				.map(AnonymousAccessLink::getMetadataUuid).collect(Collectors.toList());
		assertTrue(accessLinks.length >= 2);
		assertTrue(referencedMd.contains(md1.getUuid()));
		assertTrue(referencedMd.contains(md2.getUuid()));
	}

	@Test
	public void deleteAccessLink() throws Exception {
		ObjectMapper mapper = new ObjectMapper();
		ServiceContext context = createServiceContext();
		AbstractMetadata md = injectMetadataInDb(getSampleMetadataXml(), context, true);
		this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
		MockHttpSession session = loginAs(loginAsAdmin(context));
		MvcResult result = this.mockMvc.perform(post("/srv/api/anonymousAccessLink")
						.session(session)
						.content(jsonRequestBodyForCreate(md))
						.contentType(MediaType.parseMediaType("application/json"))
						.accept(MediaType.parseMediaType("application/json")))
				.andExpect(status().isOk())
				.andReturn();
		AnonymousAccessLink createdAccessLink = mapper.readValue(result.getResponse().getContentAsString(), AnonymousAccessLink.class);

		String requestBody = "{\"metadataUuid\" : \"" + createdAccessLink.getMetadataUuid() + "\"}";
		this.mockMvc.perform(delete("/srv/api/anonymousAccessLink")
						.session(session)
						.content(requestBody)
						.contentType(MediaType.parseMediaType("application/json")))
				.andExpect(status().isOk())
				.andReturn();

		result = this.mockMvc.perform(MockMvcRequestBuilders.get("/srv/api/anonymousAccessLink")
						.session(session)
						.accept(MediaType.parseMediaType("application/json")))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andReturn();
		String json = result.getResponse().getContentAsString();
		AnonymousAccessLink[] accessLinks = mapper.readValue(json, AnonymousAccessLink[].class);
		List<String> referencedMd = Arrays.stream(accessLinks) //
				.map(AnonymousAccessLink::getMetadataUuid).collect(Collectors.toList());
		assertFalse(referencedMd.contains(md.getUuid()));
		assertNull(anonymousAccessLinkRepository.findOneByHash(createdAccessLink.getHash()));
	}

	private static String jsonRequestBodyForCreate(AbstractMetadata md) {
		return "{\"metadataId\" : 12345," +
				"\"metadataUuid\" : \"" + md.getUuid() + "\"," +
				"\"hash\" : \"this hash will not be taken into account\"}";
	}
}
