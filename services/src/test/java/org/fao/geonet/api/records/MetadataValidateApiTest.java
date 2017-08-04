package org.fao.geonet.api.records;

import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.AbstractCoreIntegrationTest;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.domain.MetadataType;
import org.fao.geonet.domain.MetadataValidation;
import org.fao.geonet.domain.MetadataValidationStatus;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.SchemaManager;
import org.fao.geonet.kernel.search.MetaSearcher;
import org.fao.geonet.kernel.search.SearchManager;
import org.fao.geonet.kernel.search.SearcherType;
import org.fao.geonet.repository.MetadataRepository;
import org.fao.geonet.repository.MetadataValidationRepository;
import org.fao.geonet.repository.SourceRepository;
import org.fao.geonet.services.AbstractServiceIntegrationTest;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.net.URL;
import java.util.List;
import java.util.UUID;

import static junit.framework.Assert.assertEquals;
import static org.fao.geonet.domain.MetadataValidationStatus.VALID;
import static org.fao.geonet.kernel.UpdateDatestamp.NO;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
    private SearchManager searchManager;

    @Test
    public void subTemplateValidIsTrue() throws Exception {
        Metadata subTemplate = subTemplateOnLineResourceDbInsert();

        MockMvc toTest = MockMvcBuilders.webAppContextSetup(this.wac).build();
        MockHttpSession mockHttpSession = loginAsAdmin();

        toTest.perform(put("/api/records/" + subTemplate.getUuid() + "/validate")
                .param(REQ_VALID_PARAM, "true")
                .session(mockHttpSession)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.report").value(hasSize(0)));

        List<MetadataValidation> validations = metadataValidationRepository.findAllById_MetadataId(subTemplate.getId());
        assertEquals(1, validations.size());
        assertEquals(VALID, validations.get(0).getStatus());
        assertEquals(1, countTemplateIndexed(subTemplate.getUuid(), "1"));
    }

    @Test
    public void subTemplateValidIsFalse() throws Exception {
        Metadata subTemplate = subTemplateOnLineResourceDbInsert();

        MockMvc toTest = MockMvcBuilders.webAppContextSetup(this.wac).build();
        MockHttpSession mockHttpSession = loginAsAdmin();

        toTest.perform(put("/api/records/" + subTemplate.getUuid() + "/validate")
                .param(REQ_VALID_PARAM, "false")
                .session(mockHttpSession)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.report").value(hasSize(0)));

        List<MetadataValidation> validations = metadataValidationRepository.findAllById_MetadataId(subTemplate.getId());
        assertEquals(1, validations.size());
        assertEquals(MetadataValidationStatus.INVALID, validations.get(0).getStatus());
        assertEquals(1, countTemplateIndexed(subTemplate.getUuid(), "0"));
    }

    @Test
    public void subTemplateValidIsNotSet() throws Exception {
        Metadata subTemplate = subTemplateOnLineResourceDbInsert();

        MockMvc toTest = MockMvcBuilders.webAppContextSetup(this.wac).build();
        MockHttpSession mockHttpSession = loginAsAdmin();

        toTest.perform(put("/api/records/" + subTemplate.getUuid() + "/validate")
                .session(mockHttpSession)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("BadParameterEx"))
                .andExpect(jsonPath("$.description").value("Parameter isvalid MUST be set for subtemplate."));

        List<MetadataValidation> validations = metadataValidationRepository.findAllById_MetadataId(subTemplate.getId());
        assertEquals(0, validations.size());
        assertEquals(1, countTemplateIndexed(subTemplate.getUuid(), "-1"));
    }

    @Test
    public void subTemplateValidIsTrueButNotLoggedAsAdmin() throws Exception {
        Metadata subTemplate = subTemplateOnLineResourceDbInsert();

        MockMvc toTest = MockMvcBuilders.webAppContextSetup(this.wac).build();
        MockHttpSession mockHttpSession = loginAsAnonymous();

        toTest.perform(put("/api/records/" + subTemplate.getUuid() + "/validate")
                .param(REQ_VALID_PARAM, "true")
                .session(mockHttpSession)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("SecurityException"));

        List<MetadataValidation> validations = metadataValidationRepository.findAllById_MetadataId(subTemplate.getId());
        assertEquals(0, validations.size());
        loginAsAdmin();
        assertEquals(1, countTemplateIndexed(subTemplate.getUuid(), "-1"));
    }

    @Test
    public void subTemplateValidSetButTemplate() throws Exception {
        Metadata subTemplate = subTemplateOnLineResourceDbInsertAsMetadata();

        MockMvc toTest = MockMvcBuilders.webAppContextSetup(this.wac).build();
        MockHttpSession mockHttpSession = loginAsAdmin();

        toTest.perform(put("/api/records/" + subTemplate.getUuid() + "/validate")
                .param(REQ_VALID_PARAM, "true")
                .session(mockHttpSession)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("BadParameterEx"))
                .andExpect(jsonPath("$.description").value("Parameter isvalid can't be set if it is not a Subtemplate. You cannot force validation of a metadata or a template."));

        List<MetadataValidation> validations = metadataValidationRepository.findAllById_MetadataId(subTemplate.getId());
        assertEquals(0, validations.size());
        assertEquals(1, countTemplateIndexed(subTemplate.getUuid(), "-1", "n"));
    }

    private ServiceContext context;

    @Before
    public void setUp() throws Exception {
        this.context = createServiceContext();
    }

    private Metadata subTemplateOnLineResourceDbInsert() throws Exception {
        return subTemplateOnLineResourceDbInsert(MetadataType.SUB_TEMPLATE);
    }

    private Metadata subTemplateOnLineResourceDbInsertAsMetadata() throws Exception {
        return subTemplateOnLineResourceDbInsert(MetadataType.METADATA);
    }

    private Metadata subTemplateOnLineResourceDbInsert(MetadataType type) throws Exception {
        loginAsAdmin(context);

        URL resource = AbstractCoreIntegrationTest.class.getResource("kernel/sub-OnlineResource.xml");
        Element sampleMetadataXml = Xml.loadStream(resource.openStream());

        Metadata metadata = new Metadata()
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

        Metadata dbInsertedMetadata = dataManager.insertMetadata(
                context,
                metadata,
                sampleMetadataXml,
                false,
                false,
                false,
                NO,
                false,
                false);

        dataManager.indexMetadata("" + dbInsertedMetadata.getId(), true, null);
        assertEquals(1, countTemplateIndexed(dbInsertedMetadata.getUuid(), "-1", type == MetadataType.SUB_TEMPLATE ? "s" : "n"));
        return dbInsertedMetadata;
    }

    private int countTemplateIndexed(String uuid, String validStatus) throws Exception {
        return countTemplateIndexed(uuid, validStatus, "s");
    }

    private int countTemplateIndexed(String uuid, String validStatus, String type) throws Exception {
        MetaSearcher searcher = searchManager.newSearcher(SearcherType.LUCENE, Geonet.File.SEARCH_LUCENE);
        Element request = new Element("request")
                .addContent(new Element(Geonet.IndexFieldNames.UUID).setText(uuid))
                .addContent(new Element(Geonet.IndexFieldNames.IS_TEMPLATE).setText(type))
                .addContent(new Element(Geonet.IndexFieldNames.VALID).setText(validStatus));
        searcher.search(context, request, new ServiceConfig());
        return searcher.getSize();
    }
}
