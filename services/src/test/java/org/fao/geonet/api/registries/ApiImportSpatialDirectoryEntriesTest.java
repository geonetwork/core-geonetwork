package org.fao.geonet.api.registries;

import static org.fao.geonet.repository.specification.MetadataSpecs.isOwnedByUser;
import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.util.List;

import javax.servlet.http.HttpSession;

import org.fao.geonet.api.processing.report.SimpleMetadataProcessingReport;
import org.fao.geonet.domain.AbstractMetadata;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.domain.User;
import org.fao.geonet.kernel.SpringLocalServiceInvoker;
import org.fao.geonet.kernel.datamanager.IMetadataUtils;
import org.fao.geonet.services.AbstractServiceIntegrationTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.mock.web.MockMultipartHttpServletRequest;

public class ApiImportSpatialDirectoryEntriesTest extends AbstractServiceIntegrationTest {

    public static final int USER_ID = 42;

    @Autowired
    private IMetadataUtils metadataRepo;

    @Autowired
    private SpringLocalServiceInvoker invoker;

    @Test
    public void nominal() throws Exception {
        processLayersZip("layers_111_feature.xml",
                new RequestCustomizer() {
                    @Override
                    public void customizeRequest(MockMultipartHttpServletRequest request) {
                        request.setParameter("schema", "iso19139");
                        request.setParameter("process", "build-extent-subtemplate");
                        request.setParameter("descriptionAttribute", "desc");
                        request.setParameter("onlyBoundingBox", "false");
                    }
                });
    }

    @Test
    public void withReprojTo4326_IESame() throws Exception {
        processLayersZip("layers_111_feature.xml",
                new RequestCustomizer() {
                    @Override
                    public void customizeRequest(MockMultipartHttpServletRequest request) {
                        request.setParameter("schema", "iso19139");
                        request.setParameter("process", "build-extent-subtemplate");
                        request.setParameter("descriptionAttribute", "desc");
                        request.setParameter("onlyBoundingBox", "false");
                        request.setParameter("geomProjectionTo", "EPSG:4326");
                    }
                });
    }

    @Test
    public void withReprojTo3857() throws Exception {
        processLayersZip("layers_111_feature_EPSG_3857.xml",
                new RequestCustomizer() {
                    @Override
                    public void customizeRequest(MockMultipartHttpServletRequest request) {
                        request.setParameter("schema", "iso19139");
                        request.setParameter("process", "build-extent-subtemplate");
                        request.setParameter("descriptionAttribute", "desc");
                        request.setParameter("onlyBoundingBox", "false");
                        request.setParameter("geomProjectionTo", "EPSG:3857");
                    }
                });
    }

    static interface RequestCustomizer {
        void customizeRequest(MockMultipartHttpServletRequest request);
    }

    private void processLayersZip(String expectedFileName, RequestCustomizer rc) throws Exception {
        createServiceContext();
        User user = new User().setId(USER_ID);
        HttpSession session = loginAs(user);

        MockMultipartHttpServletRequest request = new MockMultipartHttpServletRequest(session.getServletContext());
        request.setRequestURI("/srv/api/0.1/registries/actions/entries/import/spatial");
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "layers.zip",
                null,
                getClass().getClassLoader().getResourceAsStream("org/fao/geonet/api/registries/layers.zip"));
        request.addFile(file);
        request.setSession(session);
        rc.customizeRequest(request);
        request.setParameter("uuidAttribute", "id");
        MockHttpServletResponse response = new MockHttpServletResponse();

        SimpleMetadataProcessingReport report = (SimpleMetadataProcessingReport) invoker.invoke(request, response);
        assertEquals(200, response.getStatus());
        assertEquals(3, report.getNumberOfRecords());

        List<? extends AbstractMetadata> datas = metadataRepo.findAll(isOwnedByUser(USER_ID));
        assertEquals(3, datas.size());

        AbstractMetadata data = metadataRepo.findOneByUuid("111");
        BufferedReader expectedBRForFirst = new BufferedReader(new InputStreamReader(getClass().getClassLoader().getResourceAsStream("org/fao/geonet/api/registries/" + expectedFileName)));
        BufferedReader BRForFirst = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(data.getData().getBytes())));

        String expectedLine;
        while ((expectedLine = expectedBRForFirst.readLine()) != null) {
            String line = BRForFirst.readLine();
            if (expectedLine.indexOf("do_no_compare") < 0) {
                assertEquals("The files differ!", expectedLine.replaceAll("^\\s+|\\s+$", ""), line.replaceAll("^\\s+|\\s+$", ""));
            }
        }
    }
}
