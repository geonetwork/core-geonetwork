package org.fao.geonet.api.records;

import static org.fao.geonet.schema.iso19139.ISO19139Namespaces.GCO;
import static org.fao.geonet.schema.iso19139.ISO19139Namespaces.GMD;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import jeeves.server.context.ServiceContext;
import org.fao.geonet.domain.*;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.SchemaManager;
import org.fao.geonet.kernel.UpdateDatestamp;
import org.fao.geonet.repository.MetadataStatusRepository;
import org.fao.geonet.repository.SourceRepository;
import org.fao.geonet.repository.StatusValueRepository;
import org.fao.geonet.repository.StatusValueRepositoryTest;
import org.fao.geonet.services.AbstractServiceIntegrationTest;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

public class MetadataWorkflowApiTest  extends AbstractServiceIntegrationTest {

    @Autowired
    private WebApplicationContext wac;
    @Autowired
    private SchemaManager schemaManager;
    @Autowired
    MetadataStatusRepository metadataStatusRepo;
    @Autowired
    StatusValueRepository statusValueRepo;
    @Autowired
    private DataManager dataManager;
    @Autowired
    private SourceRepository sourceRepository;

    private String uuid;
    private int id;
    private ServiceContext context;


    @Before
    public void setUp() throws Exception {
        this.context = createServiceContext();
        createTestData();
    }

    private void createTestData() throws Exception {
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

        this.id = dataManager.insertMetadata(context, metadata, sampleMetadataXml, false, true, false, UpdateDatestamp.NO,
            false, false).getId();

        MetadataStatus metadataStatus = new MetadataStatus();
        metadataStatus.setMetadataId(id);
        metadataStatus.setChangeDate(new ISODate());
        metadataStatus.setUserId(1);
        metadataStatus.setChangeMessage("change message " + this.id);
        metadataStatus.setOwner(1);
        metadataStatus.setUuid(uuid);
        metadataStatus.setTitles(new LinkedHashMap<String, String>(){{
            put("eng", "SampleTitle");
        }});

        // Fixme For some reason, ID 100 exists at this point.  Seems like the sequence was reset back to 100 but the table was not cleaned up?
        //     So for now lets remove the record.
        statusValueRepo.delete(100);
        final StatusValue statusValue = statusValueRepo.save(StatusValueRepositoryTest.newStatusValue(_inc));
        metadataStatus.setStatusValue(statusValue);
        metadataStatusRepo.save(metadataStatus);
    }

    @Test
    public void testGetStatusByType() throws Exception {
        MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
        MockHttpSession mockHttpSession = loginAsAdmin();

        mockMvc.perform(get("/srv/api/records/status/search?type=&uuid="+this.uuid+"&from=0&size=100")
            .accept(MediaType.parseMediaType("application/json"))
            .session(mockHttpSession))
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().is2xxSuccessful())
            .andExpect(content().contentType(API_JSON_EXPECTED_ENCODING))
            .andExpect(jsonPath("$.[0].uuid").value(this.uuid));

    }

}
