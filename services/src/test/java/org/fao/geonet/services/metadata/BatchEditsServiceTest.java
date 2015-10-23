package org.fao.geonet.services.metadata;

import com.google.common.collect.Sets;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.AbstractCoreIntegrationTest;
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.csw.common.util.Xml;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.kernel.mef.MEFLibIntegrationTest;
import org.fao.geonet.repository.MetadataRepository;
import org.jdom.Element;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.fao.geonet.constants.Geonet.Namespaces.GCO;
import static org.fao.geonet.constants.Geonet.Namespaces.GMD;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

public class BatchEditsServiceTest extends AbstractCoreIntegrationTest {

    @Autowired
    private MetadataRepository repository;

    List<String> uuids = new ArrayList();
    String firstMetadataId = null;
    ServiceContext context;

    @Before
    public void loadSamples() throws Exception {
        context = createServiceContext();
        loginAsAdmin(context);

        final String metadataUuid = "0e1943d6-64e8-4430-827c-b465c3e9e55c";
        final MEFLibIntegrationTest.ImportMetadata importMetadata =
                new MEFLibIntegrationTest.ImportMetadata(this, context);
        importMetadata.getMefFilesToLoad().add("mef2-example-2md.zip");
        importMetadata.invoke();
        List<String> importedRecordUuids = importMetadata.getMetadataIds();

        // Check record are imported
        for (String id : importedRecordUuids) {
            final String uuid = repository.findOne(Integer.valueOf(id)).getUuid();
            uuids.add(uuid);
            if (firstMetadataId == null) {
                firstMetadataId = uuid;
            }
        }
        assertEquals(3, repository.count());
    }


    @Test
    public void testParameterMustBeSet() throws Exception {
        HttpServletRequest request = new MockHttpServletRequest();

        final BatchEdits batchEditsService = new BatchEdits();
        batchEditsService.setApplicationContext(context.getApplicationContext());
        final MultiValueMap parameters = new LinkedMultiValueMap();

        try {
            batchEditsService.serviceSpecificExec(new String[]{firstMetadataId},
                    null, parameters, "eng",
                    context.getUserSession().getsHttpSession(),
                    request
            );
        } catch (java.lang.IllegalArgumentException exception) {
            assertSame("Service MUST fail if no parameter are defined",
                    exception.getClass(), IllegalArgumentException.class);
        }
    }


    @Test
    public void testUpdateRecord() throws Exception {
        HttpServletRequest request = new MockHttpServletRequest();

        final BatchEdits batchEditsService = new BatchEdits();
        batchEditsService.setApplicationContext(context.getApplicationContext());
        final MultiValueMap parameters = new LinkedMultiValueMap();

        List<BatchEditParameter> listOfupdates = new ArrayList<BatchEditParameter>();
        listOfupdates.add(new BatchEditParameter(
                "gmd:identificationInfo/gmd:MD_DataIdentification/" +
                        "gmd:citation/gmd:CI_Citation/gmd:title/gco:CharacterString",
                "",
                "## UPDATED TITLE ##"));
        listOfupdates.add(new BatchEditParameter(
                "gmd:identificationInfo/gmd:MD_DataIdentification/" +
                        "gmd:abstract/gco:CharacterString",
                "",
                "## UPDATED ABSTRACT ##"));

        int i = 0;
        for (BatchEditParameter p : listOfupdates) {
            parameters.add("xpath_" + i, p.getXpath());
            parameters.add("search_" + i, p.getSearchValue());
            parameters.add("replace_" + i, p.getReplaceValue());
            i ++;
        }
        batchEditsService.serviceSpecificExec(new String[]{firstMetadataId},
                null, parameters, "eng",
                context.getUserSession().getsHttpSession(),
                request
        );
        Metadata updatedRecord = repository.findOneByUuid(firstMetadataId);
        Element xml = Xml.loadString(updatedRecord.getData(), false);
//        System.out.println(updatedRecord.getData());


        for (BatchEditParameter p : listOfupdates) {
            assertEqualsText(p.getReplaceValue(),
                    xml,
                    p.getXpath(),
                    GMD, GCO);
        }

    }
}