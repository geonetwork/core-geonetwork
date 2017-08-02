package org.fao.geonet.api.registries;

import org.fao.geonet.api.processing.report.SimpleMetadataProcessingReport;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.domain.User;
import org.fao.geonet.kernel.SpringLocalServiceInvoker;
import org.fao.geonet.repository.MetadataRepository;
import org.fao.geonet.services.AbstractServiceIntegrationTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.mock.web.MockMultipartHttpServletRequest;

import javax.servlet.http.HttpSession;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.util.List;

import static org.fao.geonet.repository.specification.MetadataSpecs.isOwnedByUser;
import static org.junit.Assert.assertEquals;

public class ApiImportSpatialDirectoryEntriesTest extends AbstractServiceIntegrationTest {

    public static final int USER_ID = 42;

    @Autowired
    private MetadataRepository metadataRepo;

    @Autowired
    private SpringLocalServiceInvoker invoker;


    @Test
    public void nominal() throws Exception {
        createServiceContext();
        User user = new User().setId(USER_ID);

        HttpSession session = loginAs(user);

        MockMultipartHttpServletRequest request = new MockMultipartHttpServletRequest(session.getServletContext());
        request.setRequestURI("/api/0.1/registries/actions/entries/import/spatial");
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "layers.zip",
                null,
                getClass().getClassLoader().getResourceAsStream("org/fao/geonet/api/registries/layers.zip"));
        request.addFile(file);
        request.setSession(session);
        request.setParameter("schema", "iso19139");
        request.setParameter("process", "build-extent-subtemplate");
        request.setParameter("uuidAttribute", "id");
        request.setParameter("descriptionAttribute", "desc");
        request.setParameter("onlyBoundingBox", "false");
        MockHttpServletResponse response = new MockHttpServletResponse();

        SimpleMetadataProcessingReport report = (SimpleMetadataProcessingReport) invoker.invoke(request, response);
        assertEquals(200, response.getStatus());
        assertEquals(3, report.getNumberOfRecords());

        List<Metadata> datas = metadataRepo.findAll(isOwnedByUser(USER_ID));
        assertEquals(3, datas.size());

        Metadata data = metadataRepo.findOneByUuid("111");
        BufferedReader expectedBRForFirst = new BufferedReader(new InputStreamReader(getClass().getClassLoader().getResourceAsStream("org/fao/geonet/api/registries/layers_111_feature.xml")));
        BufferedReader BRForFirst = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(data.getData().getBytes())));

        String line;
        while ((line = expectedBRForFirst.readLine()) != null) {
            assertEquals("The files differ!", line.replaceAll("^\\s+|\\s+$", ""), BRForFirst.readLine().replaceAll("^\\s+|\\s+$", ""));
        }
    }
}
