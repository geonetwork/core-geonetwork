package org.fao.geonet.api.records;

import static junit.framework.Assert.assertEquals;
import static org.fao.geonet.domain.MetadataValidationStatus.VALID;
import static org.fao.geonet.kernel.UpdateDatestamp.NO;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.net.URL;
import java.util.List;
import java.util.UUID;

import org.fao.geonet.AbstractCoreIntegrationTest;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.AbstractMetadata;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.domain.MetadataType;
import org.fao.geonet.domain.MetadataValidation;
import org.fao.geonet.domain.MetadataValidationStatus;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.SchemaManager;
import org.fao.geonet.kernel.search.EsSearchManager;
import org.fao.geonet.kernel.search.IndexingMode;
import org.fao.geonet.kernel.search.submission.DirectIndexSubmitter;
import org.fao.geonet.repository.MetadataRepository;
import org.fao.geonet.repository.MetadataValidationRepository;
import org.fao.geonet.repository.SourceRepository;
import org.fao.geonet.services.AbstractServiceIntegrationTest;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import jeeves.server.context.ServiceContext;

// TODOES
@Ignore
public class MetadataValidateApiTest extends AbstractServiceIntegrationTest {
    private static final int SUBTEMPLATE_TEST_OWNER = 42;
    public static final String REQ_VALID_PARAM = "isvalid";
    @Autowired
    private WebApplicationContext wac;
    @Autowired
    private SchemaManager schemaManager;
    @Autowired
    private DataManager dataManager;
    @Autowired
    private SourceRepository sourceRepository;
    @Autowired
    private MetadataRepository metadataRepository;
    @Autowired
    MetadataValidationRepository metadataValidationRepository;
    @Autowired
    private EsSearchManager searchManager;

    @Test
    public void subTemplateValidIsTrue() throws Exception {
        AbstractMetadata subTemplate = subTemplateOnLineResourceDbInsert();

        MockMvc toTest = MockMvcBuilders.webAppContextSetup(this.wac).build();
        MockHttpSession mockHttpSession = loginAsAdmin();

        toTest.perform(put("/srv/api/records/" + subTemplate.getUuid() + "/validate/internal")
                .param(REQ_VALID_PARAM, "true")
                .session(mockHttpSession)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().contentType(API_JSON_EXPECTED_ENCODING))
                .andExpect(jsonPath("$.report").value(hasSize(0)));

        List<MetadataValidation> validations = metadataValidationRepository.findAllById_MetadataId(subTemplate.getId());
        assertEquals(1, validations.size());
        assertEquals(VALID, validations.get(0).getStatus());
        assertEquals(1, countTemplateIndexed(subTemplate.getUuid(), true));
    }

    @Test
    public void subTemplateValidIsFalse() throws Exception {
        AbstractMetadata subTemplate = subTemplateOnLineResourceDbInsert();

        MockMvc toTest = MockMvcBuilders.webAppContextSetup(this.wac).build();
        MockHttpSession mockHttpSession = loginAsAdmin();

        toTest.perform(put("/srv/api/records/" + subTemplate.getUuid() + "/validate/internal")
                .param(REQ_VALID_PARAM, "false")
                .session(mockHttpSession)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().contentType(API_JSON_EXPECTED_ENCODING))
                .andExpect(jsonPath("$.report").value(hasSize(0)));

        List<MetadataValidation> validations = metadataValidationRepository.findAllById_MetadataId(subTemplate.getId());
        assertEquals(1, validations.size());
        assertEquals(MetadataValidationStatus.INVALID, validations.get(0).getStatus());
        assertEquals(1, countTemplateIndexed(subTemplate.getUuid(), false));
    }

    @Test
    public void subTemplateValidIsNotSet() throws Exception {
        AbstractMetadata subTemplate = subTemplateOnLineResourceDbInsert();

        MockMvc toTest = MockMvcBuilders.webAppContextSetup(this.wac).build();
        MockHttpSession mockHttpSession = loginAsAdmin();

        toTest.perform(put("/srv/api/records/" + subTemplate.getUuid() + "/validate/internal")
                .session(mockHttpSession)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(API_JSON_EXPECTED_ENCODING))
                .andExpect(jsonPath("$.message").value("Parameter isvalid MUST be set for subtemplate."));

        List<MetadataValidation> validations = metadataValidationRepository.findAllById_MetadataId(subTemplate.getId());
        assertEquals(0, validations.size());
        assertEquals(1, countTemplateIndexed(subTemplate.getUuid(), null));
    }

    @Test
    public void subTemplateValidIsTrueButNotLoggedAsAdmin() throws Exception {
        AbstractMetadata subTemplate = subTemplateOnLineResourceDbInsert();

        MockMvc toTest = MockMvcBuilders.webAppContextSetup(this.wac).build();
        MockHttpSession mockHttpSession = loginAsAnonymous();

        toTest.perform(put("/srv/api/records/" + subTemplate.getUuid() + "/validate/internal")
                .param(REQ_VALID_PARAM, "true")
                .session(mockHttpSession)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError())
                .andExpect(content().contentType(API_JSON_EXPECTED_ENCODING))
                .andExpect(jsonPath("$.message").value("SecurityException"));

        List<MetadataValidation> validations = metadataValidationRepository.findAllById_MetadataId(subTemplate.getId());
        assertEquals(0, validations.size());
        loginAsAdmin();
        assertEquals(1, countTemplateIndexed(subTemplate.getUuid(), null));
    }

    @Test
    public void subTemplateValidSetButTemplate() throws Exception {
        AbstractMetadata subTemplate = subTemplateOnLineResourceDbInsertAsMetadata();

        MockMvc toTest = MockMvcBuilders.webAppContextSetup(this.wac).build();
        MockHttpSession mockHttpSession = loginAsAdmin();

        toTest.perform(put("/srv/api/records/" + subTemplate.getUuid() + "/validate/internal")
                .param(REQ_VALID_PARAM, "true")
                .session(mockHttpSession)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(API_JSON_EXPECTED_ENCODING))
                .andExpect(jsonPath("$.message").value("Parameter isvalid can't be set if it is not a Subtemplate. You cannot force validation of a metadata or a template."));

        List<MetadataValidation> validations = metadataValidationRepository.findAllById_MetadataId(subTemplate.getId());
        assertEquals(0, validations.size());
        assertEquals(1, countTemplateIndexed(subTemplate.getUuid(), null, "n"));
    }

    private ServiceContext context;

    @Before
    public void setUp() throws Exception {
        this.context = createServiceContext();
    }

    private AbstractMetadata subTemplateOnLineResourceDbInsert() throws Exception {
        return subTemplateOnLineResourceDbInsert(MetadataType.SUB_TEMPLATE);
    }

    private AbstractMetadata subTemplateOnLineResourceDbInsertAsMetadata() throws Exception {
        return subTemplateOnLineResourceDbInsert(MetadataType.METADATA);
    }

    private AbstractMetadata subTemplateOnLineResourceDbInsert(MetadataType type) throws Exception {
        loginAsAdmin(context);

        URL resource = AbstractCoreIntegrationTest.class.getResource("kernel/sub-OnlineResource.xml");
        Element sampleMetadataXml = Xml.loadStream(resource.openStream());

        Metadata metadata = new Metadata();
        metadata
                .setDataAndFixCR(sampleMetadataXml)
                .setUuid(UUID.randomUUID().toString());
        metadata.getDataInfo()
                .setRoot(sampleMetadataXml.getQualifiedName())
                .setSchemaId(schemaManager.autodetectSchema(sampleMetadataXml))
                .setType(type)
                .setPopularity(1000);
        metadata.getSourceInfo()
                .setOwner(SUBTEMPLATE_TEST_OWNER)
                .setSourceId(sourceRepository.findAll().get(0).getUuid());
        metadata.getHarvestInfo()
                .setHarvested(false);

        AbstractMetadata dbInsertedMetadata = dataManager.insertMetadata(
                context,
                metadata,
                sampleMetadataXml,
                IndexingMode.none,
                false,
                NO,
                false,
            DirectIndexSubmitter.INSTANCE);

        dataManager.indexMetadata("" + dbInsertedMetadata.getId(), DirectIndexSubmitter.INSTANCE);
        assertEquals(1, countTemplateIndexed(dbInsertedMetadata.getUuid(), false, type == MetadataType.SUB_TEMPLATE ? "s" : "n"));
        return dbInsertedMetadata;
    }

    private long countTemplateIndexed(String uuid, Boolean validStatus) throws Exception {
        return countTemplateIndexed(uuid, validStatus, "s");
    }

    private long countTemplateIndexed(String uuid, Boolean validStatus, String type) throws Exception {
        return searchManager.getNumDocs("+" + Geonet.IndexFieldNames.UUID + ":" + uuid + "" +
            " +" + Geonet.IndexFieldNames.IS_TEMPLATE + ":" + type +
            " +" + Geonet.IndexFieldNames.VALID + ":" + validStatus +  "");
    }
}
