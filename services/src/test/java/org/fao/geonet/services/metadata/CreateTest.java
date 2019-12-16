/*
 * Copyright (C) 2001-2016 Food and Agriculture Organization of the
 * United Nations (FAO-UN), United Nations World Food Programme (WFP)
 * and United Nations Environment Programme (UNEP)
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or (at
 * your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
 *
 * Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
 * Rome - Italy. email: geonetwork@osgeo.org
 */

package org.fao.geonet.services.metadata;

import jeeves.server.context.ServiceContext;

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

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.imageio.ImageIO;

import static org.fao.geonet.domain.Pair.read;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

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

        Path mdPublicDataDir = Lib.resource.getDir(context, Params.Access.PUBLIC, Integer.parseInt(id));
        Path mdPrivateDataDir = Lib.resource.getDir(context, Params.Access.PRIVATE, Integer.parseInt(id));
        final Path smallImage = mdPublicDataDir.resolve("small.gif");
        final Path largeImage = mdPublicDataDir.resolve("large.gif");
        createImage(GIF, smallImage);
        createImage(GIF, largeImage);

        final Path privateImage = mdPrivateDataDir.resolve("privateFile.gif");
        createImage(GIF, privateImage);

        _dataManager.setThumbnail(context, id, true, smallImage.toAbsolutePath().normalize().toString(), false);
        _dataManager.setThumbnail(context, id, false, largeImage.toAbsolutePath().normalize().toString(), false);

        int sampleGroup = _groupRepo.findByName("sample").getId();
        final Element params = createParams(
            read(Geonet.Elem.ID, id),
            read(Geonet.Elem.GROUP, sampleGroup));

        final Create createService = new Create();

        final Element element = createService.serviceSpecificExec(params, context);

        assertEquals(2, _metadataRepo.count());
        final String newId = element.getChildText(Geonet.Elem.ID);
        assertNotNull(_metadataRepo.findOne(newId));

        Path newPublicMdDataDir = Lib.resource.getDir(context, Params.Access.PUBLIC, Integer.parseInt(newId));
        assertTrue(Files.exists(newPublicMdDataDir.resolve(smallImage.getFileName())));
        assertTrue(Files.exists(newPublicMdDataDir.resolve(largeImage.getFileName())));

        final int expected = 2;
        assertFilesInDirectory(newPublicMdDataDir, expected);

        Path newPrivateMdDataDir = Lib.resource.getDir(context, Params.Access.PRIVATE, Integer.parseInt(newId));
        assertTrue(Files.exists(newPrivateMdDataDir));
        assertFilesInDirectory(newPrivateMdDataDir, 1);
        assertTrue(Files.exists(newPrivateMdDataDir.resolve(privateImage.getFileName())));
    }

    protected void assertFilesInDirectory(Path newPublicMdDataDir, int expected) throws IOException {
        try (DirectoryStream<Path> paths = Files.newDirectoryStream(newPublicMdDataDir)) {
            int count = 0;
            for (Path path : paths) {
                count++;
            }
            assertEquals(expected, count);
        }
    }

    private String createImage(String format, Path outFile) throws IOException {

        BufferedImage image = new BufferedImage(10, 10, BufferedImage.TYPE_3BYTE_BGR);
        Graphics2D g2d = image.createGraphics();
        g2d.drawRect(1, 1, 5, 5);
        g2d.dispose();
        Files.createDirectories(outFile.getParent());
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
