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
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class AnonymousAccessLinkTest extends AbstractServiceIntegrationTest {
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
		String content =
				"{\"id\" : 12345," +
				"\"metadataId\" : 12345," +
				"\"metadataUuid\" : \"" + md.getUuid() + "\"," +
				"\"hash\" : \"this hash will not be taken into account\"}";

		MvcResult result = this.mockMvc.perform(post("/srv/api/anonymousAccessLink")
						.session(session)
						.content(content)
						.contentType(MediaType.parseMediaType("application/json"))
						.accept(MediaType.parseMediaType("application/json")))
				.andExpect(status().isOk())
				.andExpect(content().contentType(API_JSON_EXPECTED_ENCODING))
				.andExpect(jsonPath("$.id").doesNotExist())
				.andReturn();

		String json = result.getResponse().getContentAsString();
		AnonymousAccessLink createdAccesLink = mapper.readValue(json, AnonymousAccessLink.class);
		assertEquals(md.getId(), createdAccesLink.getMetadataId());
		assertEquals(md.getUuid(), createdAccesLink.getMetadataUuid());
		assertEquals(md.getUuid() + "_hash", createdAccesLink.getHash());
		assertEquals(0, createdAccesLink.getId());

		AnonymousAccessLink inDb = anonymousAccessLinkRepository.findOneByHash(createdAccesLink.getHash());
		assertEquals(md.getId(), inDb.getMetadataId());
		assertEquals(md.getUuid(), inDb.getMetadataUuid());
		assertEquals(md.getUuid() + "_hash", inDb.getHash());
		assertNotEquals(0, inDb.getId());	}
}
