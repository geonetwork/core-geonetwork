package org.fao.geonet.api.records;

import com.google.gson.Gson;
import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.api.ApiUtils;
import org.fao.geonet.api.records.model.MetadataBatchSubmitParameter;
import org.fao.geonet.api.records.model.MetadataStatusParameter;
import org.fao.geonet.domain.*;
import org.fao.geonet.kernel.SelectionManager;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.kernel.setting.Settings;
import org.fao.geonet.repository.MetadataRepository;
import org.fao.geonet.repository.MetadataValidationRepository;
import org.fao.geonet.repository.MetadataStatusRepository;
import org.fao.geonet.repository.StatusValueRepository;
import org.fao.geonet.services.AbstractServiceIntegrationTest;
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

import java.util.Optional;

import static org.fao.geonet.domain.StatusValue.Status.APPROVED;
import static org.fao.geonet.domain.StatusValue.Status.DRAFT;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class MetadataWorkflowApiTest  extends AbstractServiceIntegrationTest {

    @Autowired
    private WebApplicationContext wac;
    @Autowired
    MetadataStatusRepository metadataStatusRepo;
    @Autowired
    private SettingManager settingManager;
    @Autowired
    private MetadataRepository metadataRepository;
    @Autowired
    private MetadataValidationRepository metadataValidationRepository;
    @Autowired
    private StatusValueRepository statusValueRepository;

    private String uuid;
    private String uuid2;
    private int metadataId;


    @Before
    public void setUp() throws Exception {
        ServiceContext context = createServiceContext();
        loginAsAdmin(context);
        settingManager.setValue(Settings.METADATA_WORKFLOW_ENABLE, true);
        settingManager.setValue(Settings.METADATA_WORKFLOW_DRAFT_WHEN_IN_GROUP, ".*");

        AbstractMetadata metadata = injectMetadataInDb(getSampleMetadataXml(), context);
        injectStatusForMetadata(metadata, DRAFT);
        uuid = metadata.getUuid();
        metadataId = metadata.getId();
        AbstractMetadata metadata2 = injectMetadataInDb(getSampleMetadataXml(), context);
        injectStatusForMetadata(metadata2, APPROVED);
        uuid2 = metadata2.getUuid();
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

    @Test
    public void testApproveInvalidTemplateSucceedsWhenAllowInvalidSubmissionApprovalDisabled() throws Exception {
        settingManager.setValue(Settings.METADATA_WORKFLOW_ALLOW_SUBMIT_APPROVE_INVALID_MD, false);

        Metadata templateMetadata = metadataRepository.findOneByUuid(uuid);
        templateMetadata.getDataInfo().setType(MetadataType.TEMPLATE);
        metadataRepository.save(templateMetadata);

        MetadataValidation invalidValidation = new MetadataValidation()
            .setId(new MetadataValidationId(metadataId, "xsd"))
            .setStatus(MetadataValidationStatus.INVALID)
            .setRequired(true)
            .setNumTests(1)
            .setNumFailures(1);
        metadataValidationRepository.save(invalidValidation);

        MetadataStatusParameter statusParameter = new MetadataStatusParameter();
        statusParameter.setStatus(Integer.parseInt(APPROVED));
        statusParameter.setChangeMessage("approve template in test");

        MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
        MockHttpSession mockHttpSession = loginAsAdmin();
        Gson gson = new Gson();
        String json = gson.toJson(statusParameter);

        mockMvc.perform(put("/srv/api/records/" + uuid + "/status")
                .content(json)
                .contentType(API_JSON_EXPECTED_ENCODING)
                .accept(MediaType.parseMediaType("application/json"))
                .session(mockHttpSession))
            .andExpect(status().isCreated());

        boolean approvedStatusCreated = metadataStatusRepo.findAllByMetadataId(metadataId, org.springframework.data.domain.Sort.unsorted()).stream()
            .map(MetadataStatus::getStatusValue)
            .map(StatusValue::getId)
            .anyMatch(id -> id == Integer.parseInt(APPROVED));
        Assert.isTrue(approvedStatusCreated);
    }

    private void injectStatusForMetadata(AbstractMetadata metadata, String status) {
        MetadataStatus metadataStatus;
        Optional<StatusValue> statusValue;
        metadataStatus = new MetadataStatus();
        metadataStatus.setMetadataId(metadata.getId());
        metadataStatus.setChangeDate(new ISODate());
        metadataStatus.setUserId(1);
        metadataStatus.setChangeMessage("change message test");
        metadataStatus.setOwner(1);
        metadataStatus.setUuid(metadata.getUuid());
        statusValue = statusValueRepository.findById(Integer.parseInt(status));
        metadataStatus.setStatusValue(statusValue.get());
        metadataStatusRepo.save(metadataStatus);
    }
}
