package org.fao.geonet.api.annotations;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.fao.geonet.domain.AnnotationEntity;
import org.fao.geonet.repository.AnnotationRepository;
import org.fao.geonet.services.AbstractServiceIntegrationTest;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;

import static java.util.UUID.randomUUID;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class AnnotationsApiTest extends AbstractServiceIntegrationTest {

    @Autowired
    private WebApplicationContext wac;

    @Autowired
    private AnnotationRepository annotationRepository;

    private MockHttpSession httpSession;
    private MockMvc mockMvc;

    @Before
    public void prepareMockMvcAndSession() {
        httpSession = this.loginAsAdmin();
        mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
    }

    @Test
    public void getAnnotations() throws Exception {
        AnnotationEntity annotation = annotationRepository.save(new AnnotationEntity().setUuid(randomUUID().toString()));

        mockMvc.perform(get("/api/annotations")
                            .session(httpSession)
                            .accept(MediaType.parseMediaType("application/json")))
                .andExpect(status().isOk())
                .andExpect(content().contentType(API_JSON_EXPECTED_ENCODING))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].uuid").value(equalTo(annotation.getUuid())))
                .andExpect(jsonPath("$[0].id)").doesNotExist());

        annotationRepository.deleteAll();

        mockMvc.perform(get("/api/annotations")
                            .session(httpSession)
                            .accept(MediaType.parseMediaType("application/json")))
                .andExpect(status().isOk())
                .andExpect(content().contentType(API_JSON_EXPECTED_ENCODING))
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    public void getExistingAnnotation() throws Exception {
        AnnotationEntity annotation = annotationRepository.save(
                new AnnotationEntity()
                    .setGeometry(
                            new ObjectMapper().createObjectNode()
                                .put("type", "Feature")
                                .put("coord", "10 20"))
                    .setUuid(randomUUID().toString()));

        mockMvc.perform(get("/api/annotations/" + annotation.getUuid())
                            .session(httpSession)
                            .accept(MediaType.parseMediaType("application/json")))
                .andExpect(status().isOk())
                .andExpect(content().contentType(API_JSON_EXPECTED_ENCODING))
                .andExpect(jsonPath("$.id)").doesNotExist())
                .andExpect(jsonPath("$.uuid").value(equalTo(annotation.getUuid())))
                .andExpect(jsonPath("$.geometry.type").value(equalTo("Feature")))
                .andExpect(jsonPath("$.geometry.coord").value(equalTo("10 20")));
    }

    @Test
    public void getExistingAnnotationWithNoGeom() throws Exception {
        AnnotationEntity annotation = annotationRepository.save(new AnnotationEntity().setUuid(randomUUID().toString()));

        mockMvc.perform(get("/api/annotations/" + annotation.getUuid())
                            .session(httpSession)
                            .accept(MediaType.parseMediaType("application/json")))
                .andExpect(status().isOk())
                .andExpect(content().contentType(API_JSON_EXPECTED_ENCODING))
                .andExpect(jsonPath("$.uuid").value(equalTo(annotation.getUuid())))
                .andExpect(jsonPath("$.geometry").value(nullValue()));
    }

    @Test
    public void getNonExistingAnnotation() throws Exception {
        mockMvc.perform(get("/api/annotations/666")
                            .session(httpSession)
                            .accept(MediaType.parseMediaType("application/json")))
                .andExpect(status().isNotFound());
    }

    @Test
    public void createAnnotation() throws Exception {

        mockMvc.perform(put("/api/annotations")
                            .content("{ \"geometry\": { \"type\": \"Feature\", \"coord\": \"30 40\" } }")
                            .contentType(MediaType.APPLICATION_JSON)
                            .session(httpSession)
                            .accept(MediaType.parseMediaType("application/json")))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(API_JSON_EXPECTED_ENCODING))
                .andExpect(jsonPath("$.id)").doesNotExist())
                .andExpect(jsonPath("$.uuid").value(new BaseMatcher() {
                    @Override
                    public void describeTo(Description description) {
                        description.appendText("error persisting entity");
                    }

                    @Override
                    public boolean matches(Object o) {
                        String uuid = (String)o;
                        if (!annotationRepository.exists(uuid)) {
                            return false;
                        }
                        AnnotationEntity created = annotationRepository.findByUUID(uuid);
                        return created.getGeometry().get("coord").textValue().equals("30 40") &&
                                created.getGeometry().get("type").textValue().equals("Feature") &&
                                created.getUuid().length() > 0;
                    }
                }))
                .andExpect(jsonPath("$.geometry.type").value(equalTo("Feature")))
                .andExpect(jsonPath("$.geometry.coord").value(equalTo("30 40")));
    }

    @Test
    public void updateAnnotation() throws Exception {
        AnnotationEntity annotation = annotationRepository.save(
                new AnnotationEntity()
                        .setGeometry(
                                new ObjectMapper().createObjectNode()
                                        .put("type", "Feature")
                                        .put("coord", "10 20"))
                        .setUuid(randomUUID().toString()));


        mockMvc.perform(put("/api/annotations/" + annotation.getUuid())
                            .content("{ \"geometry\": { \"type\": \"Polygon\", \"coord\": \"30 40\" } }")
                            .contentType(MediaType.APPLICATION_JSON)
                            .session(httpSession)
                            .accept(MediaType.parseMediaType("application/json")))
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().contentType(API_JSON_EXPECTED_ENCODING))
                .andExpect(jsonPath("$.id)").doesNotExist())
                .andExpect(jsonPath("$.uuid").value(new BaseMatcher() {
                    @Override
                    public void describeTo(Description description) {
                        description.appendText("error persisting entity");
                    }

                    @Override
                    public boolean matches(Object o) {
                        String uuid = (String) o;
                        if (!annotationRepository.exists(uuid)) {
                            return false;
                        }
                        AnnotationEntity created = annotationRepository.findByUUID(uuid);
                        return created.getGeometry().get("coord").textValue().equals("30 40") &&
                                created.getGeometry().get("type").textValue().equals("Polygon") &&
                                created.getUuid().length() > 0;
                    }
                }))
                .andExpect(jsonPath("$.uuid").value(equalTo(annotation.getUuid())))
                .andExpect(jsonPath("$.geometry.type").value(equalTo("Polygon")))
                .andExpect(jsonPath("$.geometry.coord").value(equalTo("30 40")));
    }

    @Test
    public void updateNonExistingAnnotation() throws Exception {
        mockMvc.perform(put("/api/annotations/666")
                            .content("{ \"geometry\": { \"type\": \"Polygon\", \"coord\": \"30 40\" } }")
                            .contentType(MediaType.APPLICATION_JSON)
                            .session(httpSession)
                            .accept(MediaType.parseMediaType("application/json")))
                .andExpect(status().isNotFound());
    }

    @Test
    @Ignore
    public void uuidIgnoredWhenCreate() throws Exception {
        AnnotationEntity existing = annotationRepository.save(
                new AnnotationEntity()
                        .setGeometry(
                                new ObjectMapper().createObjectNode()
                                        .put("type", "Feature")
                                        .put("coord", "10 20"))
                        .setUuid(randomUUID().toString()));

        mockMvc.perform(put("/api/annotations")
                .content("{ \"uuid\": \""+ existing.getUuid() + "\", \"geometry\": { \"type\": \"Feature\", \"coord\": \"30 40\" } }")
                .contentType(MediaType.APPLICATION_JSON)
                .session(httpSession)
                .accept(MediaType.parseMediaType("application/json")))
                .andExpect(status().isCreated());

        List<AnnotationEntity> annotationEntities = annotationRepository.findAll();
        assertEquals("10 20", annotationEntities.stream().filter(x -> x.getUuid().equals(existing.getUuid())).findFirst().get().getGeometry().get("coord").asText());
        assertEquals("30 40", annotationEntities.stream().filter(x -> !x.getUuid().equals(existing.getUuid())).findFirst().get().getGeometry().get("coord").asText());
    }
}
