package org.fao.geonet.api.processing;

import jeeves.server.context.ServiceContext;
import org.fao.geonet.domain.AbstractMetadata;
import org.fao.geonet.domain.MetadataValidation;
import org.fao.geonet.domain.User;
import org.fao.geonet.kernel.SchemaManager;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.kernel.setting.Settings;
import org.fao.geonet.repository.MetadataValidationRepository;
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
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;

import static junit.framework.Assert.assertEquals;
import static org.fao.geonet.schema.iso19139.ISO19139Namespaces.GMD;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.fao.geonet.schema.iso19139.ISO19139Namespaces.XLINK;

//@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class ValidateApiTest extends AbstractServiceIntegrationTest {
    @Autowired
    private WebApplicationContext wac;
    @Autowired
    MetadataValidationRepository metadataValidationRepository;
    @Autowired
    private SettingManager settingManager;
    @Autowired
    private SchemaManager schemaManager;

    private ServiceContext context;

    @Before
    public void bugFix() {
        //schemaManager.reloadSchema("iso19139");
    }

    @Before
    public void initContext() throws Exception {
        context = createServiceContext();
    }

    @Test
    public void validateMd() throws Exception {
        Element holoceneElem = getSample("kernel/holocene.xml");
        AbstractMetadata metadata = injectMetadataInDb(holoceneElem, context, true);

        validateAndCheckIsoSchematronOk(context, metadata);
    }


    @Test
    public void validateMdWithSubtemplate() throws Exception {
        try {
            settingManager.setValue(Settings.SYSTEM_XLINKRESOLVER_ENABLE, true);
            Element holoceneElem = getSample("kernel/holocene.xml");
            AbstractMetadata subtemplate = insertTemplateResourceInDb(getSample("kernel/gossauExtent.xml"), context);
            Element extentElem = Xml.selectElement(holoceneElem, "gmd:identificationInfo/gmd:MD_DataIdentification/gmd:extent");
            extentElem.setAttribute("href", "local://srv/api/registries/entries/"+ subtemplate.getUuid() + "?lang=ger,fre,ita,eng,roh&amp;schema=iso19139.che", XLINK);
            extentElem.removeChild("EX_Extent", GMD);
            AbstractMetadata metadata = injectMetadataInDb(holoceneElem, context, true);

            validateAndCheckIsoSchematronOk(context, metadata);
        } finally {
            settingManager.setValue(Settings.SYSTEM_XLINKRESOLVER_ENABLE, false);
        }
    }

    private void validateAndCheckIsoSchematronOk(ServiceContext context, AbstractMetadata metadata) throws Exception {
        User user = loginAsAdmin(context);
        MockMvc toTest = MockMvcBuilders.webAppContextSetup(this.wac).build();
        MockHttpSession mockHttpSession = loginAs(user);

        toTest.perform(put("/srv/api/records/validate")
                        .queryParam("uuids", metadata.getUuid())
                        .session(mockHttpSession)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is2xxSuccessful());

        List<MetadataValidation> validations = metadataValidationRepository.findAllById_MetadataId(metadata.getId());
        assertEquals(0, validations.stream().filter(schematron -> "schematron-rules-iso".equals(schematron.getId().getValidationType())).findFirst().get().getNumFailures());
    }
}
