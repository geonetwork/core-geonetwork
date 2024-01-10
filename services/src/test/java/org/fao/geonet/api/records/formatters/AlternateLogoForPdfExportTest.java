package org.fao.geonet.api.records.formatters;

import jeeves.server.context.ServiceContext;
import org.fao.geonet.domain.AbstractMetadata;
import org.fao.geonet.domain.Setting;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.repository.SettingRepository;
import org.fao.geonet.services.AbstractServiceIntegrationTest;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import javax.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.UUID;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ContextConfiguration(locations = "classpath:formatter-test-context.xml")
public class AlternateLogoForPdfExportTest extends AbstractServiceIntegrationTest {

    @Autowired
    private WebApplicationContext wac;
    @Autowired
    private SettingManager settingManager;
    @Autowired
    SettingRepository settingRepository;
    @Autowired
    private PdfOrHtmlResponseWriter responseWriterSpy;

    private ServiceContext context;
    private AbstractMetadata metadata;

    @Before
    public void initSiteId() {
        settingManager.setValue("system/site/siteId", UUID.randomUUID().toString());
    }

    @Before
    public void createTestData() throws Exception {
        context = createServiceContext();
        loginAsAdmin(context);
        metadata = injectMetadataInDbDoNotRefreshHeader(getSampleISO19139MetadataXml(), context);
    }

    @Before
    public void initWriterSpy() {
        Mockito.reset(responseWriterSpy);
    }

    @Test
    public void whenGeneratingPdfWithPropertySetPdfLogoIsUsed() throws Exception {
        MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
        MockHttpSession mockHttpSession = loginAsAdmin();
        settingManager.setValue("metadata/pdfReport/headerLogoFileName", "pdf_test_banner_to_use.png");

        String url = "/srv/api/records/" + metadata.getUuid() + "/formatters/xsl-view?output=pdf&language=fre";
        mockMvc.perform(get(url)
            .session(mockHttpSession)
            .accept(MediaType.ALL_VALUE))
            .andExpect(status().isOk())
            .andReturn();

        ArgumentCaptor<byte[]> captor = ArgumentCaptor.forClass(byte[].class);
        Mockito.verify(responseWriterSpy).writeOutResponse(any(ServiceContext.class), any(String.class), any(String.class), any(HttpServletResponse.class), any(FormatType.class), captor.capture());
        assertTrue(new String(captor.getValue(), StandardCharsets.UTF_8).contains("images/harvesting/pdf_test_banner_to_use.png"));
    }

    @Test
    public void whenNotGeneratingPdfWithPropertySetSiteLogoIsUsed() throws Exception {
        MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
        MockHttpSession mockHttpSession = loginAsAdmin();
        settingManager.setValue("metadata/pdfReport/headerLogoFileName", "pdf_test_banner_to_use.png");
        String siteId = settingManager.getValue("system/site/siteId");

        String url = "/srv/api/records/" + metadata.getUuid() + "/formatters/xsl-view?language=fre";
        mockMvc.perform(get(url)
                .session(mockHttpSession)
                .accept(MediaType.ALL_VALUE))
            .andExpect(status().isOk())
            .andReturn();

        ArgumentCaptor<byte[]> captor = ArgumentCaptor.forClass(byte[].class);
        Mockito.verify(responseWriterSpy).writeOutResponse(any(ServiceContext.class), any(String.class), any(String.class), any(HttpServletResponse.class), any(FormatType.class), captor.capture());
        assertFalse(new String(captor.getValue(), StandardCharsets.UTF_8).contains("pdf_test_banner_to_use.png"));
        assertTrue(new String(captor.getValue(), StandardCharsets.UTF_8).contains("images/logos/" + siteId + ".png"));
    }

    @Test
    public void whenGeneratingPdfWithPropertyNotSetSiteLogoIsUsed() throws Exception {
        MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
        MockHttpSession mockHttpSession = loginAsAdmin();
        Optional<Setting> se = settingRepository.findById("metadata/pdfReport/headerLogoFileName");
        se.ifPresent(settingRepository::delete);
        String siteId = settingManager.getValue("system/site/siteId");

        String url = "/srv/api/records/" + metadata.getUuid() + "/formatters/xsl-view?output=pdf&language=fre";
        mockMvc.perform(get(url)
                .session(mockHttpSession)
                .accept(MediaType.ALL_VALUE))
            .andExpect(status().isOk())
            .andReturn();

        ArgumentCaptor<byte[]> captor = ArgumentCaptor.forClass(byte[].class);
        Mockito.verify(responseWriterSpy).writeOutResponse(any(ServiceContext.class), any(String.class), any(String.class), any(HttpServletResponse.class), any(FormatType.class), captor.capture());
        assertFalse(new String(captor.getValue(), StandardCharsets.UTF_8).contains("pdf_test_banner_to_use.png"));
        assertTrue(new String(captor.getValue(), StandardCharsets.UTF_8).contains("images/logos/" + siteId + ".png"));
    }
}
