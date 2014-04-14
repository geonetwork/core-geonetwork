package org.fao.geonet.services.metadata;

import jeeves.server.context.ServiceContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.constants.Params;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.mef.MEFLibIntegrationTest;
import org.fao.geonet.lib.Lib;
import org.fao.geonet.repository.GroupRepository;
import org.fao.geonet.repository.MetadataRepository;
import org.fao.geonet.repository.SourceRepository;
import org.fao.geonet.services.AbstractServiceIntegrationTest;
import org.jdom.Element;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import static org.fao.geonet.domain.Pair.read;
import static org.junit.Assert.*;

/**
 * Test CreateMetadata
 * <p/>
 * Created by Jesse on 12/11/13.
 */
public class CreateTest extends AbstractServiceIntegrationTest {
    private static final String GIF = "gif";
    @Autowired
    private DataManager _dataManager;
    @Autowired
    private MetadataRepository _metadataRepo;
    @Autowired
    private GroupRepository _groupRepo;
    @Autowired
    private SourceRepository _sourceRepo;

    @Test
    public void testCreateNormalMetadata() throws Exception {
        final ServiceContext context = createServiceContext();
        loginAsAdmin(context);

        final String id = importMetadata(context);

        int sampleGroup = _groupRepo.findByName("sample").getId();
        final Element params = createParams(
                read(Geonet.Elem.ID, id),
                read(Geonet.Elem.GROUP, sampleGroup));

        final Create createService = new Create();

        final Element element = createService.serviceSpecificExec(params, context);

        assertEquals(2, _metadataRepo.count());
        assertNotNull(_metadataRepo.findOne(element.getChildText(Geonet.Elem.ID)));
    }

    @Test
    public void testCreateMetadataAndCopyExistingPictures() throws Exception {
        final ServiceContext context = createServiceContext();
        loginAsAdmin(context);

        final String id = importMetadata(context);

        String mdPublicDataDir = Lib.resource.getDir(context, Params.Access.PUBLIC, id);
        String mdPrivateDataDir = Lib.resource.getDir(context, Params.Access.PRIVATE, id);
        final File smallImage = new File(mdPublicDataDir, "small.gif");
        final File largeImage = new File(mdPublicDataDir, "large.gif");
        createImage(GIF, smallImage);
        createImage(GIF, largeImage);

        final File privateImage = new File(mdPrivateDataDir, "privateFile.gif");
        createImage(GIF, privateImage);

        _dataManager.setThumbnail(context, id, true, smallImage.getAbsolutePath(), false);
        _dataManager.setThumbnail(context, id, false, largeImage.getAbsolutePath(), false);

        int sampleGroup = _groupRepo.findByName("sample").getId();
        final Element params = createParams(
                read(Geonet.Elem.ID, id),
                read(Geonet.Elem.GROUP, sampleGroup));

        final Create createService = new Create();

        final Element element = createService.serviceSpecificExec(params, context);

        assertEquals(2, _metadataRepo.count());
        final String newId = element.getChildText(Geonet.Elem.ID);
        assertNotNull(_metadataRepo.findOne(newId));

        File newPublicMdDataDir = new File(Lib.resource.getDir(context, Params.Access.PUBLIC, newId));
        assertTrue(new File(newPublicMdDataDir, smallImage.getName()).exists());
        assertTrue(new File(newPublicMdDataDir, largeImage.getName()).exists());
        assertEquals(2, newPublicMdDataDir.list().length);

        File newPrivateMdDataDir = new File(Lib.resource.getDir(context, Params.Access.PRIVATE, newId));
        assertTrue(newPrivateMdDataDir.exists());
        assertNotNull(newPrivateMdDataDir.list());
        assertEquals(1, newPrivateMdDataDir.list().length);
        assertTrue(new File(newPrivateMdDataDir, privateImage.getName()).exists());
    }

    private String createImage(String format, File outFile) throws IOException {

        BufferedImage image = new BufferedImage(10, 10, BufferedImage.TYPE_3BYTE_BGR);
        Graphics2D g2d = image.createGraphics();
        g2d.drawRect(1, 1, 5, 5);
        g2d.dispose();

        final boolean writerWasFound = ImageIO.write(image, format, outFile);
        assertTrue(writerWasFound);

        return outFile.getAbsolutePath();
    }

    @Test
    public void testCreateMetadataAndPicturesAreMissing() throws Exception {
        final ServiceContext context = createServiceContext();
        loginAsAdmin(context);

        final String id = importMetadata(context);

        _dataManager.setThumbnail(context, id, true, "abc/small.gif", false);
        _dataManager.setThumbnail(context, id, false, "abc/large.gif", false);

        int sampleGroup = _groupRepo.findByName("sample").getId();
        final Element params = createParams(
                read(Geonet.Elem.ID, id),
                read(Geonet.Elem.GROUP, sampleGroup));

        final Create createService = new Create();

        final Element element = createService.serviceSpecificExec(params, context);

        assertEquals(2, _metadataRepo.count());
        assertNotNull(_metadataRepo.findOne(element.getChildText(Geonet.Elem.ID)));
    }


    private String importMetadata(ServiceContext context) throws Exception {
        final MEFLibIntegrationTest.ImportMetadata importMetadata = new MEFLibIntegrationTest.ImportMetadata(this, context).invoke();

        assertEquals(1, _metadataRepo.count());
        return importMetadata.getMetadataIds().get(0);
    }

}
