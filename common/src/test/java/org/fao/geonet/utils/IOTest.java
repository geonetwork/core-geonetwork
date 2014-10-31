package org.fao.geonet.utils;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class IOTest {
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test(expected = RuntimeException.class)
    public void testDeleteFileDir() throws Exception {
        final Path java = temporaryFolder.getRoot().toPath().resolve("src/main/java");
        IO.deleteFile(java, true, "test");

    }

    @Test
    public void testDeleteFileDirSwallowError() throws Exception {
        final Path java = temporaryFolder.getRoot().toPath().resolve("src/main/java");
        IO.deleteFile(java, false, "IOTest.testDeleteFileDirSwallowError");
    }

    @Test
    public void testDeleteFile1() throws Exception {
        final Path file = temporaryFolder.getRoot().toPath().resolve("src.txt");
        Files.createFile(file);
        Files.write(file, "hi".getBytes());
        assertTrue(Files.exists(file));
        IO.deleteFile(file, true, "test");
        assertFalse(Files.exists(file));
    }

    @Test
    public void testCopy() throws Exception {
        final Path file = temporaryFolder.getRoot().toPath().resolve("src.txt");
        Files.createFile(file);
        Files.write(file, "hi".getBytes());
        assertTrue(Files.exists(file));

        final Path java = temporaryFolder.getRoot().toPath().resolve("src/main/java");
        Files.createDirectories(java);

        final Path to1 = java.resolve(file.getFileName());
        IO.copyDirectoryOrFile(file, to1);
        assertTrue(Files.exists(file));
        assertTrue(Files.exists(to1));

        final Path to2 = temporaryFolder.getRoot().toPath().resolve("to");
        IO.copyDirectoryOrFile(temporaryFolder.getRoot().toPath().resolve("src"), to2);
        assertTrue(Files.exists(to1));
        assertTrue(Files.exists(to2));
        assertTrue(Files.exists(to2.resolve("main")));
        assertTrue(Files.exists(to2.resolve("main").resolve("java")));
        assertTrue(Files.exists(to2.resolve("main").resolve("java").resolve("src.txt")));
    }

    @Test
    public void testMove() throws Exception {
        final Path file = temporaryFolder.getRoot().toPath().resolve("src.txt");
        Files.createFile(file);
        Files.write(file, "hi".getBytes());
        assertTrue(Files.exists(file));

        final Path java = temporaryFolder.getRoot().toPath().resolve("src/main/java");
        Files.createDirectories(java);

        final Path to1 = java.resolve(file.getFileName());
        IO.moveDirectoryOrFile(file, to1);
        assertFalse(Files.exists(file));
        assertTrue(Files.exists(to1));

        final Path to2 = temporaryFolder.getRoot().toPath().resolve("to");
        IO.moveDirectoryOrFile(temporaryFolder.getRoot().toPath().resolve("src"), to2);
        assertFalse(Files.exists(to1));
        assertTrue(Files.exists(to2));
        assertTrue(Files.exists(to2.resolve("main")));
        assertTrue(Files.exists(to2.resolve("main").resolve("java")));
        assertTrue(Files.exists(to2.resolve("main").resolve("java").resolve("src.txt")));
    }

    @Test
    public void testIsEmptyDir() throws Exception {
        final Path root = temporaryFolder.getRoot().toPath();
        assertTrue(IO.isEmptyDir(root));
        final Path file = root.resolve("src.txt");
        Files.createFile(file);
        Files.write(file, "hi".getBytes());

        assertFalse(IO.isEmptyDir(root));
    }

    @Test
    public void testDeleteFileOrDirectory() throws Exception {
        final Path java = temporaryFolder.getRoot().toPath().resolve("src/main/java");
        Files.createDirectories(java);

        final Path file = java.resolve("src.txt");
        Files.createFile(file);
        Files.write(file, "hi".getBytes());
        assertTrue(Files.exists(file));

        final Path src = temporaryFolder.getRoot().toPath().resolve("src");
        IO.deleteFileOrDirectory(src);

        assertFalse(Files.exists(file));
        assertFalse(Files.exists(src));
    }

    @Test
    public void testTouch() throws Exception {
        final Path java = temporaryFolder.getRoot().toPath().resolve("src/main/java");
        final Path file = java.resolve("src.txt");
        assertFalse(Files.exists(file));

        IO.touch(file);
        assertTrue(Files.exists(file));
    }

    @Test
    public void testToPath() throws Exception {
        final Path path = IO.toPath("part");
        assertEquals(new File("part").toURI(), path.toUri());
        final URI rootUri = temporaryFolder.getRoot().toURI();
        final URI zipFsUri = new URI("jar:" + rootUri + "x.zip");
        final FileSystem zipFileSystem = FileSystems.newFileSystem(zipFsUri, Collections.singletonMap("create", "true"));
        IO.setFileSystem(zipFileSystem);

        final Path zipPath = IO.toPath("part");
        assertEquals("jar:" + rootUri + "x.zip!/part".replaceAll("/+", "/"), zipPath.toUri().toString().replaceAll("/+", "/"));

        assertEquals("x.xml", IO.toPath("file:x.xml").toString());
    }
}