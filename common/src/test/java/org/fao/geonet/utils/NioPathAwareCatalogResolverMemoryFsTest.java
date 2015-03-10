package org.fao.geonet.utils;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;

public class NioPathAwareCatalogResolverMemoryFsTest extends NioPathAwareCatalogResolverTest {
    @Override
    protected Path getPathToFile() throws URISyntaxException, IOException {
        FileSystem memoryFs = Jimfs.newFileSystem(Configuration.unix());

        Path root = memoryFs.getPath("rootPath/gmd/xxx.txt");
        Files.createDirectories(root.getParent());
        Files.createFile(root);

        return root;
    }
}
