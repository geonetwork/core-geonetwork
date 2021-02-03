package org.fao.geonet.api.records;

import static org.fao.geonet.schema.iso19139.ISO19139Namespaces.GCO;
import static org.fao.geonet.schema.iso19139.ISO19139Namespaces.GMD;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Arrays;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import jeeves.server.context.ServiceContext;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.domain.*;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.SchemaManager;
import org.fao.geonet.kernel.UpdateDatestamp;
import org.fao.geonet.kernel.datamanager.IMetadataManager;
import org.fao.geonet.kernel.metadata.StatusActionsFactory;
import org.fao.geonet.repository.MetadataStatusRepository;
import org.fao.geonet.repository.SourceRepository;
import org.fao.geonet.repository.StatusValueRepository;
import org.fao.geonet.repository.StatusValueRepositoryTest;
import org.fao.geonet.services.AbstractServiceIntegrationTest;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

public class MetadataWorkflowApiTest  extends AbstractServiceIntegrationTest {

    @Autowired
    private WebApplicationContext wac;
    @Autowired
    MetadataStatusRepository metadataStatusRepo;
    @Autowired
    StatusValueRepository statusValueRepo;


    @Autowired
    private DataManager dataManager;
    @Autowired
    private SourceRepository sourceRepository;
    @Autowired
    private SchemaManager schemaManager;

    private String uuid;
    private ServiceContext context;


    @Before
    public void setUp() throws Exception {
        this.context = createServiceContext();
        createTestData();
    }

    private void createTestData() throws Exception {
        this.uuid = UUID.randomUUID().toString();

        loginAsAdmin(context);

        final Element sampleMetadataXml = getSampleMetadataXml();
        this.uuid = UUID.randomUUID().toString();
        Xml.selectElement(sampleMetadataXml, "gmd:fileIdentifier/gco:CharacterString", Arrays.asList(GMD, GCO)).setText(this.uuid);

        String source = sourceRepository.findAll().get(0).getUuid();
        String schema = schemaManager.autodetectSchema(sampleMetadataXml);
        final Metadata metadata = (Metadata) new Metadata()
            .setDataAndFixCR(sampleMetadataXml)
            .setUuid(uuid);
        metadata.getDataInfo()
            .setRoot(sampleMetadataXml.getQualifiedName())
            .setSchemaId(schema)
            .setType(MetadataType.METADATA)
            .setPopularity(1000);
        metadata.getSourceInfo()
            .setOwner(1)
            .setSourceId(source);
        metadata.getHarvestInfo()
            .setHarvested(false);

        int id = dataManager.insertMetadata(context, metadata, sampleMetadataXml, false, true, false, UpdateDatestamp.NO,
            false, false).getId();

        MetadataStatus metadataStatus = new MetadataStatus();
        metadataStatus.setMetadataId(id);
        metadataStatus.setChangeDate(new ISODate());
        metadataStatus.setUserId(1);
        metadataStatus.setChangeMessage("change message test");
        metadataStatus.setOwner(1);
        metadataStatus.setUuid(uuid);

        Random random = new Random();
        AtomicInteger inc = new AtomicInteger(random.nextInt(16));
        final StatusValue statusValue = statusValueRepo.save(StatusValueRepositoryTest.newStatusValue(inc));
        metadataStatus.setStatusValue(statusValue);
        metadataStatusRepo.save(metadataStatus);
    }

    @Test
    public void testGetStatusByType() throws Exception {
        MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
        MockHttpSession mockHttpSession = loginAsAdmin();

        mockMvc.perform(get("/srv/api/records/status/search?type=&uuid="+uuid+"&from=0&size=100")
            .accept(MediaType.parseMediaType("application/json"))
            .session(mockHttpSession))
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().is2xxSuccessful())
            .andExpect(content().contentType(API_JSON_EXPECTED_ENCODING))
            .andExpect(jsonPath("$.[0].uuid").value(uuid));

    }

}
