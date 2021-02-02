package org.fao.geonet.api.records;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import org.fao.geonet.domain.ISODate;
import org.fao.geonet.domain.MetadataStatus;
import org.fao.geonet.domain.StatusValue;
import org.fao.geonet.repository.MetadataStatusRepository;
import org.fao.geonet.repository.StatusValueRepository;
import org.fao.geonet.repository.StatusValueRepositoryTest;
import org.fao.geonet.services.AbstractServiceIntegrationTest;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

public class MetadataWorkflowApiTest  extends AbstractServiceIntegrationTest {

    @Autowired
    private WebApplicationContext wac;
    @Autowired
    MetadataStatusRepository metadataStatusRepo;
    @Autowired
    StatusValueRepository statusValueRepo;

    private String uuid;


    @Before
    public void setUp() throws Exception {
        createTestData();
    }

    private void createTestData() {
        this.uuid = UUID.randomUUID().toString();

        MetadataStatus metadataStatus = new MetadataStatus();
        metadataStatus.setMetadataId(111);
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
            .andExpect(status().is2xxSuccessful())
            .andExpect(content().contentType(API_JSON_EXPECTED_ENCODING))
            .andExpect(jsonPath("$.[0].uuid").value(uuid));

    }

}
