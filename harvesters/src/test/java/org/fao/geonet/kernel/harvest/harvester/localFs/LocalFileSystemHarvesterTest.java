package org.fao.geonet.kernel.harvest.harvester.localFs;

import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.MockRequestFactoryGeonet;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.kernel.GeonetworkDataDirectory;
import org.fao.geonet.kernel.harvest.AbstractHarvesterIntegrationTest;
import org.fao.geonet.kernel.harvest.harvester.AbstractHarvester;
import org.fao.geonet.kernel.harvest.harvester.localfilesystem.LocalFilesystemHarvester;
import org.fao.geonet.utils.IO;
import org.jdom.Element;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class LocalFileSystemHarvesterTest extends AbstractHarvesterIntegrationTest {

    public LocalFileSystemHarvesterTest() {
        super("filesystem");
    }

    @Override
    protected void mockHttpRequests(MockRequestFactoryGeonet bean) throws Exception {
    }

    @Override
    protected void customizeParams(Element params) {
        String path = Thread.currentThread().getContextClassLoader().
                getResource("org/fao/geonet/kernel/harvest/harvester/localFs/nominal").getPath();

        try {
            IO.copyDirectoryOrFile(FileSystems.getDefault().getPath(path), testFixture.getDataDirContainer(), true);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Element directory = new Element("directory");
        directory.setText(IO.toPath(testFixture.getDataDirContainer().toString(), "nominal").toString());
        params.getChild("site").addContent(directory);


        Element recurse = new Element("recurse");
        recurse.setText("on");
        params.getChild("site").addContent(recurse);

        Element id = new Element("id");
        id.setText("666");
        Element ownerParamElem = new Element("ownerUser");
        ownerParamElem.addContent(id);
        params.addContent(ownerParamElem);

    }

    @Override
    protected void performExtraAssertions(AbstractHarvester harvester) {
        List<Metadata> addedMetadata = metadataRepository.findAll();

        Integer ownerId = addedMetadata.get(0).getSourceInfo().getOwner();

        assertEquals(new Integer("666"), ownerId);

    }

    protected int getExpectedTotalFound() {
        return 1;
    }

    protected int getExpectedAdded() {
        return 1;
    }


    @Test
    public void testIsScriptAllowed() throws Exception {
        LocalFilesystemHarvester harvester = new LocalFilesystemHarvester();

        List<String> rejectedScripts = Arrays.asList(
            "myscript.sh | sudo reboot now",
            "myscript.sh || sudo reboot now",
            "myscript.sh $(sudo reboot now)",
            "myscript.sh && sudo reboot now",
            "myscript.sh & sudo reboot now"
        );

        rejectedScripts.forEach(script -> {
            List<String> args = new ArrayList<String>(Arrays.asList(script.split(" ")));
            assertFalse(harvester.isScriptAllowed(args));
        });

        GeonetworkDataDirectory dataDirectory = ApplicationContextHolder.get().getBean(GeonetworkDataDirectory.class);
        Path scriptFile = dataDirectory.getConfigDir().resolve("myscript.sh");
        try {
            Files.createFile(scriptFile);
        } catch (FileAlreadyExistsException e) {}

        List<String> acceptedScript = Arrays.asList(
            "myscript.sh",
            "myscript.sh arg1"
        );

        acceptedScript.forEach(script -> {
            List<String> args = new ArrayList<String>(Arrays.asList(script.split(" ")));
            assertTrue(harvester.isScriptAllowed(args));
        });
        
        Files.deleteIfExists(scriptFile);
    }
}
