package org.fao.geonet.api.records;

import com.google.gson.Gson;
import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.api.ApiUtils;
import org.fao.geonet.api.records.model.MetadataBatchSubmitParameter;
import org.fao.geonet.domain.*;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.SchemaManager;
import org.fao.geonet.kernel.SelectionManager;
import org.fao.geonet.kernel.UpdateDatestamp;
import org.fao.geonet.kernel.search.IndexingMode;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.kernel.setting.Settings;
import org.fao.geonet.repository.MetadataStatusRepository;
import org.fao.geonet.repository.SourceRepository;
import org.fao.geonet.repository.StatusValueRepository;
import org.fao.geonet.services.AbstractServiceIntegrationTest;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.junit.Before;
import org.junit.Test;
import org.locationtech.jts.util.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

import static org.fao.geonet.schema.iso19139.ISO19139Namespaces.GCO;
import static org.fao.geonet.schema.iso19139.ISO19139Namespaces.GMD;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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

    @Autowired
    private SettingManager settingManager;

    @Autowired
    private StatusValueRepository statusValueRepository;

    private String uuid;
    private String uuid2;
    private ServiceContext context;


    @Before
    public void setUp() throws Exception {
        this.context = createServiceContext();
        createTestData();
    }

    private void createTestData() throws Exception {
        this.uuid = UUID.randomUUID().toString();
        this.uuid2 = UUID.randomUUID().toString();

        loginAsAdmin(context);
        settingManager.setValue(Settings.METADATA_WORKFLOW_ENABLE, true);
        settingManager.setValue(Settings.METADATA_WORKFLOW_DRAFT_WHEN_IN_GROUP, ".*");

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

        int id = dataManager.insertMetadata(context, metadata, sampleMetadataXml, IndexingMode.full, false, UpdateDatestamp.NO,
            false, false).getId();

        MetadataStatus metadataStatus = new MetadataStatus();
        metadataStatus.setMetadataId(id);
        metadataStatus.setChangeDate(new ISODate());
        metadataStatus.setUserId(1);
        metadataStatus.setChangeMessage("change message test");
        metadataStatus.setOwner(1);
        metadataStatus.setUuid(uuid);

        Optional<StatusValue> statusValue = statusValueRepository.findById(Integer.parseInt(StatusValue.Status.DRAFT));
        metadataStatus.setStatusValue(statusValue.get());
        metadataStatusRepo.save(metadataStatus);

        final Element sampleMetadataXml2 = getSampleMetadataXml();
        this.uuid2 = UUID.randomUUID().toString();
        Xml.selectElement(sampleMetadataXml2, "gmd:fileIdentifier/gco:CharacterString", Arrays.asList(GMD, GCO)).setText(this.uuid2);

        final Metadata metadata2 = (Metadata) new Metadata()
            .setDataAndFixCR(sampleMetadataXml2)
            .setUuid(uuid2);
        metadata2.getDataInfo()
            .setRoot(sampleMetadataXml.getQualifiedName())
            .setSchemaId(schema)
            .setType(MetadataType.METADATA)
            .setPopularity(1000);
        metadata2.getSourceInfo()
            .setOwner(1)
            .setSourceId(source);
        metadata2.getHarvestInfo()
            .setHarvested(false);

        id = dataManager.insertMetadata(context, metadata2, sampleMetadataXml, IndexingMode.full, false, UpdateDatestamp.NO,
            false, false).getId();

        metadataStatus = new MetadataStatus();
        metadataStatus.setMetadataId(id);
        metadataStatus.setChangeDate(new ISODate());
        metadataStatus.setUserId(1);
        metadataStatus.setChangeMessage("change message test");
        metadataStatus.setOwner(1);
        metadataStatus.setUuid(uuid);


        statusValue = statusValueRepository.findById(Integer.parseInt(StatusValue.Status.APPROVED));
        metadataStatus.setStatusValue(statusValue.get());
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

    @Test
    public void testBatchSubmit() throws Exception {
        MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
        MockHttpSession mockHttpSession = loginAsAdmin();

        // Select the metadata
        UserSession session = ApiUtils.getUserSession( mockHttpSession);
        SelectionManager.getManager(session).getSelection(SelectionManager.SELECTION_METADATA).add(this.uuid);
        SelectionManager.getManager(session).getSelection(SelectionManager.SELECTION_METADATA).add(this.uuid2);
        int selected = SelectionManager.getManager(session).getSelection(SelectionManager.SELECTION_METADATA).size();
        Assert.isTrue(selected == 2);

        MetadataBatchSubmitParameter submitParameter = new MetadataBatchSubmitParameter();
        submitParameter.setBucket(SelectionManager.SELECTION_METADATA);
        submitParameter.setMessage("");
        Gson gson = new Gson();
        String json = gson.toJson(submitParameter);


        mockMvc.perform(put("/srv/api/records/submit")
                .content(json)
                .contentType(API_JSON_EXPECTED_ENCODING)
                .accept(MediaType.parseMediaType("application/json"))
                .session(mockHttpSession))
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().is2xxSuccessful())
            .andExpect(content().contentType(API_JSON_EXPECTED_ENCODING))
            .andExpect(jsonPath("$.numberOfRecordsProcessed").value(1));

    }

}
