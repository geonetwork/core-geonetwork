package org.fao.geonet.services.metadata;

import static org.fao.geonet.domain.Pair.read;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpSession;

import org.apache.commons.io.IOUtils;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.constants.Params;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.mef.MEFLibIntegrationTest;
import org.fao.geonet.lib.Lib;
import org.fao.geonet.repository.GroupRepository;
import org.fao.geonet.repository.MetadataRepository;
import org.fao.geonet.services.AbstractServiceIntegrationTest;
import org.jdom.Element;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ContextConfiguration;

import jeeves.server.context.ServiceContext;
import jeeves.server.sources.http.JeevesServlet;

/**
 * Test CreateMetadata Updated for Spring
 */
@ContextConfiguration(inheritLocations = true, locations = "classpath:encoder-bean.xml")
public class CreateTest extends AbstractServiceIntegrationTest {

    private static final String GIF = "gif";
    @Autowired
    private DataManager _dataManager;
    @Autowired
    private MetadataRepository _metadataRepo;
    @Autowired
    private GroupRepository _groupRepo;

    @Autowired
    private Insert insertService;

    @Test
    public void testCreateNormalMetadata() throws Exception {
        System.out.println("___________________________");

        final ServiceContext serviceContext = createServiceContext();
        loginAsAdmin(serviceContext);

        Integer sampleGroup = _groupRepo.findByName("sample").getId();

        HttpSession session = new MockHttpSession();

        session.setAttribute(JeevesServlet.USER_SESSION_ATTRIBUTE_KEY,
                serviceContext.getUserSession());
        
        InputStream is = getClass().getResourceAsStream("/org/fao/geonet/guiservices/versioning/metadata.xml");
        StringWriter writer = new StringWriter();
        IOUtils.copy(is, writer, "UTF-8");
        String data = writer.toString();

        Insert.InsertResponse response = insertService.serviceSpecificExec(
                session, data, sampleGroup.toString(), "n", "_none_", "off", null,
                "_none_", null, Params.NOTHING);

        assertNotNull(response.getId());
        assertNotNull(response.getUuid());

        assertNotNull(_metadataRepo.findOne(response.getId()));
        assertNotNull(_metadataRepo.findOneByUuid(response.getUuid()));
    }

    @Test
    public void testCreateMetadataAndCopyExistingPictures() throws Exception {
        final ServiceContext context = createServiceContext();
        loginAsAdmin(context);

        final String id = importMetadata(context);

        Path mdPublicDataDir = Lib.resource.getDir(context,
                Params.Access.PUBLIC, id);
        Path mdPrivateDataDir = Lib.resource.getDir(context,
                Params.Access.PRIVATE, id);
        final Path smallImage = mdPublicDataDir.resolve("small.gif");
        final Path largeImage = mdPublicDataDir.resolve("large.gif");
        createImage(GIF, smallImage);
        createImage(GIF, largeImage);

        final Path privateImage = mdPrivateDataDir.resolve("privateFile.gif");
        createImage(GIF, privateImage);

        _dataManager.setThumbnail(context, id, true,
                smallImage.toAbsolutePath().normalize().toString(), false);
        _dataManager.setThumbnail(context, id, false,
                largeImage.toAbsolutePath().normalize().toString(), false);

        int sampleGroup = _groupRepo.findByName("sample").getId();
        final Element params = createParams(read(Geonet.Elem.ID, id),
                read(Geonet.Elem.GROUP, sampleGroup));

        final Create createService = new Create();

        final Element element = createService.serviceSpecificExec(params,
                context);

        assertEquals(2, _metadataRepo.count());
        final String newId = element.getChildText(Geonet.Elem.ID);
        assertNotNull(_metadataRepo.findOne(newId));

        Path newPublicMdDataDir = Lib.resource.getDir(context,
                Params.Access.PUBLIC, newId);
        assertTrue("Fail on small image", Files
                .exists(newPublicMdDataDir.resolve(smallImage.getFileName())));
        assertTrue("Fail on large image", Files
                .exists(newPublicMdDataDir.resolve(largeImage.getFileName())));

        final int expected = 2;
        assertFilesInDirectory(newPublicMdDataDir, expected);

        Path newPrivateMdDataDir = Lib.resource.getDir(context,
                Params.Access.PRIVATE, newId);
        assertTrue(Files.exists(newPrivateMdDataDir));
        assertFilesInDirectory(newPrivateMdDataDir, 1);
        assertTrue(Files.exists(
                newPrivateMdDataDir.resolve(privateImage.getFileName())));
    }

    protected void assertFilesInDirectory(Path newPublicMdDataDir, int expected)
            throws IOException {
        try (DirectoryStream<Path> paths = Files
                .newDirectoryStream(newPublicMdDataDir)) {
            int count = 0;
            for (Path path : paths) {
                count++;
            }
            assertEquals(expected, count);
        }
    }

    private String createImage(String format, Path outFile) throws IOException {

        BufferedImage image = new BufferedImage(10, 10,
                BufferedImage.TYPE_3BYTE_BGR);
        Graphics2D g2d = image.createGraphics();
        g2d.drawRect(1, 1, 5, 5);
        g2d.dispose();
        try (OutputStream out = Files.newOutputStream(outFile)) {
            final boolean writerWasFound = ImageIO.write(image, format, out);
            assertTrue(writerWasFound);
        }

        return outFile.toAbsolutePath().normalize().toString();
    }

    @Test
    public void testCreateMetadataAndPicturesAreMissing() throws Exception {
        final ServiceContext context = createServiceContext();
        loginAsAdmin(context);

        final String id = importMetadata(context);

        _dataManager.setThumbnail(context, id, true, "abc/small.gif", false);
        _dataManager.setThumbnail(context, id, false, "abc/large.gif", false);

        int sampleGroup = _groupRepo.findByName("sample").getId();
        final Element params = createParams(read(Geonet.Elem.ID, id),
                read(Geonet.Elem.GROUP, sampleGroup));

        final Create createService = new Create();

        final Element element = createService.serviceSpecificExec(params,
                context);

        assertEquals(2, _metadataRepo.count());
        assertNotNull(
                _metadataRepo.findOne(element.getChildText(Geonet.Elem.ID)));
    }

    private String importMetadata(ServiceContext context) throws Exception {
        final MEFLibIntegrationTest.ImportMetadata importMetadata = new MEFLibIntegrationTest.ImportMetadata(
                this, context).invoke();

        assertEquals(1, _metadataRepo.count());
        return importMetadata.getMetadataIds().get(0);
    }

}
