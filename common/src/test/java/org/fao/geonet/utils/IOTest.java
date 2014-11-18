package org.fao.geonet.utils;

import org.junit.Test;

import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.Collections;

import static org.junit.Assert.assertEquals;

/**
 * @author Jesse on 11/5/2014.
 */
public class IOTest extends AbstractIOTest {
    // put any tests that don't apply to memory fs test here


    @Test
    public final void testToPath() throws Exception {
        final Path path = IO.toPath("part");
        assertEquals(getFileUri(), path.toUri());
        final URI rootUri = temporaryFolder.getRoot().toURI();
        final URI zipFsUri = new URI("jar:" + rootUri + "x.zip");

        IO.setFileSystemThreadLocal(null);
        final FileSystem zipFileSystem = FileSystems.newFileSystem(zipFsUri, Collections.singletonMap("create", "true"));
        IO.setFileSystem(zipFileSystem);

        final Path zipPath = IO.toPath("part");
        assertEquals("jar:" + rootUri + "x.zip!/part".replaceAll("/+", "/"), zipPath.toUri().toString().replaceAll("/+", "/"));
    }

}
