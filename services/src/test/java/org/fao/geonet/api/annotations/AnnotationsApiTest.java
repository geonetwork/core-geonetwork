package org.fao.geonet.api.annotations;

import com.fasterxml.jackson.databind.ObjectMapper;
import jeeves.constants.Jeeves;
import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.domain.AnnotationEntity;
import org.fao.geonet.domain.ISODate;
import org.fao.geonet.domain.ReservedGroup;
import org.fao.geonet.kernel.datamanager.IMetadataManager;
import org.fao.geonet.kernel.datamanager.IMetadataSchemaUtils;
import org.fao.geonet.kernel.search.IndexingMode;
import org.fao.geonet.repository.AnnotationRepository;
import org.fao.geonet.services.AbstractServiceIntegrationTest;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.jdom.Element;
import org.junit.Test;
import org.mockito.AdditionalAnswers;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import javax.annotation.PostConstruct;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import static java.util.UUID.randomUUID;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.never;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@PrepareForTest({AnnotationRepository.class})
public class AnnotationsApiTest extends AbstractServiceIntegrationTest {
    private static Date TODAY = new GregorianCalendar(2020, 6, 6 , 13, 0, 0).getTime();
    private static Date ONE_DAY = new GregorianCalendar(2025, 6, 6 , 17, 0, 0).getTime();
    private static String TIMESTAMP = "1580468510430"; // 2020-01-31T12:01:50.430+0100
    private static String METADATA_UUID = "metadata-uuid";

    @Autowired
    private WebApplicationContext wac;

    @Autowired
    private AnnotationRepository annotationRepository;

    @Autowired
    private IMetadataManager metadataManager;

    @Autowired
    private IMetadataSchemaUtils metadataSchemaUtils;

    private MockMvc mockMvc;

    @PostConstruct
    public void prepareMockMvcAndSession() {
        mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
        wac.getBean(AnnotationsApi.class).dateFactory = new AnnotationsApi.IDateFactory() {
            @Override
            public Date getTodayNoon() {
                return TODAY;
            }
        };
    }

    @Test
    public void getAnnotations() throws Exception {
        MockHttpSession httpSession = this.loginAsAdmin();
        AnnotationEntity annotation = annotationRepository.save(new AnnotationEntity()
            .setUuid(randomUUID().toString())
            .setLastRead(TODAY)
            .setLastWrite(TODAY)
            .setMetadataUuid(METADATA_UUID));

        mockMvc.perform(get("/api/annotations")
                            .session(httpSession)
                            .accept(MediaType.parseMediaType("application/json")))
                .andExpect(status().isOk())
                .andExpect(content().contentType(API_JSON_EXPECTED_ENCODING))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].uuid").value(equalTo(annotation.getUuid())))
                .andExpect(jsonPath("$[0].id)").doesNotExist())
                .andExpect(jsonPath("$[0].lastWrite)").doesNotExist())
                .andExpect(jsonPath("$[0].lastRead)").doesNotExist())
                .andExpect(jsonPath("$[0].metadataUuid").value(equalTo(METADATA_UUID)));

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
        MockHttpSession httpSession = this.loginAsAdmin();
        AnnotationRepository annotationRepositorySpy = PowerMockito.mock(AnnotationRepository.class, AdditionalAnswers.delegatesTo(annotationRepository));
        wac.getBean(AnnotationsApi.class).annotationRepository = annotationRepositorySpy;
        AnnotationEntity annotation = annotationRepository.save(
                new AnnotationEntity()
                    .setGeometry(
                            new ObjectMapper().createObjectNode()
                                .put("type", "Feature")
                                .put("coord", "10 20"))
                    .setUuid(randomUUID().toString()))
                    .setLastWrite(TODAY)
                    .setLastRead(ONE_DAY)
                    .setMetadataUuid(METADATA_UUID);
        createMetadata(METADATA_UUID, ((UserSession)httpSession.getAttribute(Jeeves.Elem.SESSION)).getUserIdAsInt());

        mockMvc.perform(get("/api/annotations/" + annotation.getUuid())
                            .session(httpSession)
                            .accept(MediaType.parseMediaType("application/json")))
                .andExpect(status().isOk())
                .andExpect(content().contentType(API_JSON_EXPECTED_ENCODING))
                .andExpect(jsonPath("$.id)").doesNotExist())
                .andExpect(jsonPath("$.uuid").value(equalTo(annotation.getUuid())))
                .andExpect(jsonPath("$.geometry.type").value(equalTo("Feature")))
                .andExpect(jsonPath("$.geometry.coord").value(equalTo("10 20")))
                .andExpect(jsonPath("$.lastWrite)").doesNotExist())
                .andExpect(jsonPath("$.lastRead)").doesNotExist())
                .andExpect(jsonPath("$.metadataUuid").value(equalTo(METADATA_UUID)));

        AnnotationEntity created = annotationRepository.findByUUID(annotation.getUuid());
        assertEquals(TODAY, created.getLastRead());
        Mockito.verify(annotationRepositorySpy).save(isA(AnnotationEntity.class));
    }

    @Test
    public void getExistingAnnotationReadDateLetUnchanged() throws Exception {
        MockHttpSession httpSession = this.loginAsAdmin();
        AnnotationRepository annotationRepositorySpy = PowerMockito.mock(AnnotationRepository.class, AdditionalAnswers.delegatesTo(annotationRepository));
        wac.getBean(AnnotationsApi.class).annotationRepository = annotationRepositorySpy;
        AnnotationEntity annotation = annotationRepository.save(
                new AnnotationEntity().setUuid(randomUUID().toString())).setLastRead(TODAY);

        mockMvc.perform(get("/api/annotations/" + annotation.getUuid())
                .session(httpSession)
                .accept(MediaType.parseMediaType("application/json")))
                .andExpect(status().isOk());

        AnnotationEntity created = annotationRepository.findByUUID(annotation.getUuid());
        assertEquals(TODAY, created.getLastRead());
        Mockito.verify(annotationRepositorySpy, never()).save(isA(AnnotationEntity.class));
    }

    @Test
    public void getExistingAnnotationWithNoGeomNorMetadataUuid() throws Exception {
        MockHttpSession httpSession = this.loginAsAdmin();
        AnnotationRepository annotationRepositorySpy = PowerMockito.mock(AnnotationRepository.class, AdditionalAnswers.delegatesTo(annotationRepository));
        wac.getBean(AnnotationsApi.class).annotationRepository = annotationRepositorySpy;
        AnnotationEntity annotation = annotationRepository.save(new AnnotationEntity().setUuid(randomUUID().toString()));

        mockMvc.perform(get("/api/annotations/" + annotation.getUuid())
                            .session(httpSession)
                            .accept(MediaType.parseMediaType("application/json")))
                .andExpect(status().isOk())
                .andExpect(content().contentType(API_JSON_EXPECTED_ENCODING))
                .andExpect(jsonPath("$.uuid").value(equalTo(annotation.getUuid())))
                .andExpect(jsonPath("$.geometry").value(nullValue()))
                .andExpect(jsonPath("$.metadataUuid").value(nullValue()));

        AnnotationEntity created = annotationRepository.findByUUID(annotation.getUuid());
        assertEquals(TODAY, created.getLastRead());
        Mockito.verify(annotationRepositorySpy).save(isA(AnnotationEntity.class));
    }

    @Test
    public void getNonExistingAnnotation() throws Exception {
        MockHttpSession httpSession = this.loginAsAdmin();

        mockMvc.perform(get("/api/annotations/666")
                            .session(httpSession)
                            .accept(MediaType.parseMediaType("application/json")))
                .andExpect(status().isNotFound());
    }

    @Test
    public void createAnnotation() throws Exception {
        MockHttpSession httpSession = this.loginAsAdmin();
        createMetadata(METADATA_UUID, ((UserSession)httpSession.getAttribute(Jeeves.Elem.SESSION)).getUserIdAsInt());

        mockMvc.perform(put("/api/annotations")
                            .content("{ \"geometry\": { \"type\": \"Feature\", \"coord\": \"30 40\" },"
                                    + " \"lastRead\": \"" + TIMESTAMP + "\","
                                    + " \"metadataUuid\": \""+ METADATA_UUID + "\"}")
                            .contentType(MediaType.APPLICATION_JSON)
                            .session(httpSession)
                            .accept(MediaType.parseMediaType("application/json")))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(API_JSON_EXPECTED_ENCODING))
                .andExpect(jsonPath("$.id)").doesNotExist())
                .andExpect(jsonPath("$.uuid").value(new UUIDMatcher() {

                    @Override
                    public boolean matches(AnnotationEntity created) {
                        return created.getGeometry().get("coord").textValue().equals("30 40") &&
                                created.getGeometry().get("type").textValue().equals("Feature") &&
                                created.getUuid().length() > 0 &&
                                created.getLastWrite().compareTo(TODAY) == 0 &&
                                created.getLastRead() == null &&
                                created.getMetadataUuid().equals(METADATA_UUID);
                    }
                }))
                .andExpect(jsonPath("$.geometry.type").value(equalTo("Feature")))
                .andExpect(jsonPath("$.geometry.coord").value(equalTo("30 40")))
                .andExpect(jsonPath("$.lastWrite)").doesNotExist())
                .andExpect(jsonPath("$.lastRead)").doesNotExist())
                .andExpect(jsonPath("$.metadataUuid").value(equalTo(METADATA_UUID)));
    }

    @Test
    public void updateAnnotation() throws Exception {
        MockHttpSession httpSession = this.loginAsAdmin();
        AnnotationEntity annotation = annotationRepository.save(
                new AnnotationEntity()
                        .setGeometry(
                                new ObjectMapper().createObjectNode()
                                        .put("type", "Feature")
                                        .put("coord", "10 20"))
                        .setUuid(randomUUID().toString()))
                        .setMetadataUuid(randomUUID().toString())
                        .setLastRead(ONE_DAY);
        createMetadata(annotation.getMetadataUuid(), ((UserSession)httpSession.getAttribute(Jeeves.Elem.SESSION)).getUserIdAsInt());
        createMetadata(METADATA_UUID, ((UserSession)httpSession.getAttribute(Jeeves.Elem.SESSION)).getUserIdAsInt());

        mockMvc.perform(put("/api/annotations/" + annotation.getUuid())
                            .content("{ \"geometry\": { \"type\": \"Polygon\", \"coord\": \"30 40\" },"
                                    + " \"lastRead\": \"" + TIMESTAMP + "\","
                                    + " \"uuid\": \"666\","
                                    + " \"metadataUuid\": \""+ METADATA_UUID + "\"}")
                            .contentType(MediaType.APPLICATION_JSON)
                            .session(httpSession)
                            .accept(MediaType.parseMediaType("application/json")))
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().contentType(API_JSON_EXPECTED_ENCODING))
                .andExpect(jsonPath("$.id)").doesNotExist())
                .andExpect(jsonPath("$.uuid").value(new UUIDMatcher() {
                    @Override
                    public boolean matches(AnnotationEntity created) {
                        return  created.getUuid().equals(annotation.getUuid()) &&
                                created.getGeometry().get("coord").textValue().equals("30 40") &&
                                created.getGeometry().get("type").textValue().equals("Polygon") &&
                                created.getUuid().length() > 0 &&
                                created.getLastRead().compareTo(ONE_DAY) == 0 &&
                                created.getLastWrite().compareTo(TODAY) == 0 &&
                                created.getMetadataUuid().equals(METADATA_UUID);
                    }
                }))
                .andExpect(jsonPath("$.uuid").value(equalTo(annotation.getUuid())))
                .andExpect(jsonPath("$.geometry.type").value(equalTo("Polygon")))
                .andExpect(jsonPath("$.geometry.coord").value(equalTo("30 40")))
                .andExpect(jsonPath("$.lastWrite)").doesNotExist())
                .andExpect(jsonPath("$.lastRead)").doesNotExist())
                .andExpect(jsonPath("$.metadataUuid").value(equalTo(METADATA_UUID)));
    }

    @Test
    public void updateNonExistingAnnotation() throws Exception {
        MockHttpSession httpSession = this.loginAsAdmin();
        mockMvc.perform(put("/api/annotations/666")
                            .content("{ \"geometry\": { \"type\": \"Polygon\", \"coord\": \"30 40\" } }")
                            .contentType(MediaType.APPLICATION_JSON)
                            .session(httpSession)
                            .accept(MediaType.parseMediaType("application/json")))
                .andExpect(status().isNotFound());
    }

    @Test
    public void longGeometry() throws Exception {
        MockHttpSession httpSession = this.loginAsAdmin();
        String longGeom = String.join("", Collections.nCopies(8000, "1"));
        mockMvc.perform(put("/api/annotations")
                            .content("{ \"geometry\": { \"type\": \"Feature\", \"coord\": \"" + longGeom +"\" } }")
                            .contentType(MediaType.APPLICATION_JSON)
                            .session(httpSession)
                            .accept(MediaType.parseMediaType("application/json")))
                .andExpect(jsonPath("$.uuid").value(new UUIDMatcher() {
                    @Override
                    public boolean matches(AnnotationEntity created) {
                        return created.getGeometry().get("coord").textValue().equals(longGeom);
                    }
                }));
    }

    @Test
    public void createErrorWhenUuidAlreadyUsed() throws Exception {
        MockHttpSession httpSession = this.loginAsAdmin();
        AnnotationEntity existing = annotationRepository.save(
                new AnnotationEntity()
                        .setGeometry(
                                new ObjectMapper().createObjectNode()
                                        .put("type", "Feature")
                                        .put("coord", "10 20"))
                        .setUuid(randomUUID().toString()));

        mockMvc.perform(put("/api/annotations")
                            .contentType(MediaType.APPLICATION_JSON)
                            .session(httpSession)
                            .content("{ \"uuid\": \""+ existing.getUuid() + "\", \"geometry\": { \"type\": \"Feature\", \"coord\": \"30 40\" } }")
                            .accept(MediaType.parseMediaType("application/json")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(equalTo("duplicate_uuid")));

        List<AnnotationEntity> annotationEntities = annotationRepository.findAll();
        assertEquals(1, annotationEntities.size());
        assertEquals("10 20", annotationEntities.get(0).getGeometry().get("coord").asText());
    }

    @Test
    public void createAnnotationSettingUUID() throws Exception {
        MockHttpSession httpSession = this.loginAsAdmin();
        mockMvc.perform(put("/api/annotations")
                            .content("{ \"geometry\": { \"type\": \"Feature\", \"coord\": \"30 40\" }, \"uuid\": \"666\" }")
                            .contentType(MediaType.APPLICATION_JSON)
                            .session(httpSession)
                            .accept(MediaType.parseMediaType("application/json")))
                .andExpect(jsonPath("$.uuid").value(new UUIDMatcher() {
                    @Override
                    public boolean matches(AnnotationEntity created) {
                        return created.getUuid().equals("666");
                    }
                }));
    }

    @Test
    public void notNeedToBeLoggedToWorkWithAssociationNotLinkedWithAMetadata() throws Exception {
        MockHttpSession httpSession = new MockHttpSession();

        mockMvc.perform(put("/api/annotations")
                            .content("{  \"uuid\": \"666\" }")
                            .contentType(MediaType.APPLICATION_JSON)
                            .session(httpSession)
                            .accept(MediaType.parseMediaType("application/json")))
                .andExpect(status().isCreated());

        mockMvc.perform(put("/api/annotations/666")
                            .content("{ \"geometry\": { \"type\": \"Polygon\", \"coord\": \"30 40\" } }")
                            .contentType(MediaType.APPLICATION_JSON)
                            .session(httpSession)
                            .accept(MediaType.parseMediaType("application/json")))
                .andExpect(status().is2xxSuccessful());

        mockMvc.perform(get("/api/annotations/666")
                            .session(httpSession)
                            .accept(MediaType.parseMediaType("application/json")))
                .andExpect(status().isOk());
    }

    @Test
    public void notToBeLoggedForbidToWorkWithAssociationLinkedWithAMetadata() throws Exception {

        MockHttpSession httpSession = new MockHttpSession();

        mockMvc.perform(put("/api/annotations")
                            .content("{  \"uuid\": \"666\", \"metadataUuid\": \""+ METADATA_UUID + "\"}")
                            .contentType(MediaType.APPLICATION_JSON)
                            .session(httpSession)
                            .accept(MediaType.parseMediaType("application/json")))
                .andExpect(status().isForbidden());

        AnnotationEntity annotation = annotationRepository.save(
                new AnnotationEntity().setUuid(randomUUID().toString()).setMetadataUuid(METADATA_UUID)).setLastRead(TODAY);

        mockMvc.perform(put("/api/annotations/"+ annotation.getUuid())
                            .content("{ \"geometry\": { \"type\": \"Polygon\", \"coord\": \"30 40\" } }")
                            .contentType(MediaType.APPLICATION_JSON)
                            .session(httpSession)
                            .accept(MediaType.parseMediaType("application/json")))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/annotations/"+ annotation.getUuid())
                .session(httpSession)
                .accept(MediaType.parseMediaType("application/json")))
                .andExpect(status().isForbidden());

        AnnotationEntity annotationWithNoMetadataUuidSet = annotationRepository.save(
                new AnnotationEntity().setUuid(randomUUID().toString())).setLastRead(TODAY);

        mockMvc.perform(put("/api/annotations/"+ annotationWithNoMetadataUuidSet.getUuid())
                .content("{ \"metadataUuid\": \""+ METADATA_UUID + "\" }")
                .contentType(MediaType.APPLICATION_JSON)
                .session(httpSession)
                .accept(MediaType.parseMediaType("application/json")))
                .andExpect(status().isForbidden());
    }

    abstract class UUIDMatcher extends BaseMatcher {
        @Override
        public boolean matches(Object o) {
            String uuid = (String)o;
            if (!annotationRepository.exists(uuid)) {
                return false;
            }
            AnnotationEntity created = annotationRepository.findByUUID(uuid);
            return matches(created);
        }

        @Override
        public void describeTo(Description description) {
            description.appendText("error persisting entity");
        }

        public abstract boolean matches(AnnotationEntity created);
    }

    private void createMetadata(String uuid, int owner) throws Exception {
        ServiceContext context = createServiceContext();
        Element sampleMetadataXml = getSampleMetadataXml();
        String schema = metadataSchemaUtils.autodetectSchema(sampleMetadataXml);

        metadataManager.insertMetadata(
                context,
                schema,
                new Element(sampleMetadataXml.getName(),
                sampleMetadataXml.getNamespace()),
                uuid,
                owner,
                "" + ReservedGroup.all.getId(),
                "sourceid",
                "n",
                "doctype",
                null,
                new ISODate().getDateAndTime(),
                new ISODate().getDateAndTime(),
                false,
                IndexingMode.none);
    }
}
