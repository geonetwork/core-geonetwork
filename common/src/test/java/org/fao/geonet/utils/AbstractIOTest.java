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

public abstract class AbstractIOTest {
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test(expected = RuntimeException.class)
    public void testDeleteFileDir() throws Exception {
        final Path java = getRootPath().resolve("src/main/java");
        IO.deleteFile(java, true, "test");
    }

    @Test
    public void testDeleteFileDirSwallowError() throws Exception {
        final Path java = getRootPath().resolve("src/main/java");
        IO.deleteFile(java, false, "IOTest.testDeleteFileDirSwallowError");
    }

    @Test
    public void testDeleteFile1() throws Exception {
        final Path file = getRootPath().resolve("src.txt");
        Files.createFile(file);
        Files.write(file, "hi".getBytes());
        assertTrue(Files.exists(file));
        IO.deleteFile(file, true, "test");
        assertFalse(Files.exists(file));
    }

    protected Path getRootPath() {
        return temporaryFolder.getRoot().toPath();
    }

    @Test
    public final void testCopy() throws Exception {
        final Path file = getRootPath().resolve("src.txt");
        Files.createFile(file);
        Files.write(file, "hi".getBytes());
        assertTrue(Files.exists(file));

        final Path java = getRootPath().resolve("src/main/java");
        Files.createDirectories(java);

        final Path to1 = java.resolve(file.getFileName());
        IO.copyDirectoryOrFile(file, to1);
        assertTrue(Files.exists(file));
        assertTrue(Files.exists(to1));

        final Path to2 = getRootPath().resolve("to");
        IO.copyDirectoryOrFile(getRootPath().resolve("src"), to2);
        assertTrue(Files.exists(to1));
        assertTrue(Files.exists(to2));
        assertTrue(Files.exists(to2.resolve("main")));
        assertTrue(Files.exists(to2.resolve("main").resolve("java")));
        assertTrue(Files.exists(to2.resolve("main").resolve("java").resolve("src.txt")));
    }
    @Test
    public final void testCopyIntoDirectory() throws Exception {
        final Path file = getRootPath().resolve("src.txt");
        Files.createFile(file);
        Files.write(file, "hi".getBytes());
        assertTrue(Files.exists(file));

        final Path java = getRootPath().resolve("src/main/java");
        Files.createDirectories(java);

        IO.copyDirectoryOrFile(file, java);
        assertTrue(Files.exists(file));
        assertTrue(Files.exists(java));

        final Path to2 = getRootPath().resolve("to");
        Files.createDirectories(to2);
        final Path src = getRootPath().resolve("src");
        IO.copyDirectoryOrFile(src, to2);
        assertTrue(Files.exists(src));
        assertTrue(Files.exists(to2));
        assertTrue(Files.exists(to2.resolve("src")));
        assertTrue(Files.exists(to2.resolve("src").resolve("main")));
        assertTrue(Files.exists(to2.resolve("src").resolve("main").resolve("java")));
        assertTrue(Files.exists(to2.resolve("src").resolve("main").resolve("java").resolve("src.txt")));
    }

    @Test
    public final void testMove() throws Exception {
        final Path file = getRootPath().resolve("src.txt");
        Files.createFile(file);
        Files.write(file, "hi".getBytes());
        assertTrue(Files.exists(file));

        final Path java = getRootPath().resolve("src/main/java");
        Files.createDirectories(java);

        final Path to1 = java.resolve(file.getFileName());
        IO.moveDirectoryOrFile(file, to1);
        assertFalse(Files.exists(file));
        assertTrue(Files.exists(to1));

        final Path to2 = getRootPath().resolve("to");
        IO.moveDirectoryOrFile(getRootPath().resolve("src"), to2);
        assertFalse(Files.exists(to1));
        assertTrue(Files.exists(to2));
        assertTrue(Files.exists(to2.resolve("main")));
        assertTrue(Files.exists(to2.resolve("main").resolve("java")));
        assertTrue(Files.exists(to2.resolve("main").resolve("java").resolve("src.txt")));
    }

    @Test
    public final void testMoveIntoDirectory() throws Exception {
        final Path file = getRootPath().resolve("src.txt");
        Files.createFile(file);
        Files.write(file, "hi".getBytes());
        assertTrue(Files.exists(file));

        final Path java = getRootPath().resolve("src/main/java");
        Files.createDirectories(java);

        IO.moveDirectoryOrFile(file, java);
        assertFalse(Files.exists(file));
        assertTrue(Files.exists(java));
        assertTrue(Files.exists(java.resolve(file.getFileName())));

        final Path to2 = getRootPath().resolve("to");
        Files.createDirectories(to2);
        final Path src = getRootPath().resolve("src");
        IO.moveDirectoryOrFile(src, to2);
        assertFalse(Files.exists(src));
        assertTrue(Files.exists(to2));
        assertTrue(Files.exists(to2.resolve("src")));
        assertTrue(Files.exists(to2.resolve("src").resolve("main")));
        assertTrue(Files.exists(to2.resolve("src").resolve("main").resolve("java")));
        assertTrue(Files.exists(to2.resolve("src").resolve("main").resolve("java").resolve("src.txt")));
    }

    @Test
    public final void testIsEmptyDir() throws Exception {
        final Path root = getRootPath();
        assertTrue(IO.isEmptyDir(root));
        final Path file = root.resolve("src.txt");
        Files.createFile(file);
        Files.write(file, "hi".getBytes());

        assertFalse(IO.isEmptyDir(root));
    }

    @Test
    public final void testDeleteFileOrDirectory() throws Exception {
        final Path java = getRootPath().resolve("src/main/java");
        Files.createDirectories(java);

        final Path file = java.resolve("src.txt");
        Files.createFile(file);
        Files.write(file, "hi".getBytes());
        assertTrue(Files.exists(file));

        final Path src = getRootPath().resolve("src");
        IO.deleteFileOrDirectory(src);

        assertFalse(Files.exists(file));
        assertFalse(Files.exists(src));
    }

    @Test
    public final void testTouch() throws Exception {
        final Path java = getRootPath().resolve("src/main/java");
        final Path file = java.resolve("src.txt");
        assertFalse(Files.exists(file));

        IO.touch(file);
        assertTrue(Files.exists(file));
    }

    protected URI getFileUri() {
        return new File("part").toURI();
    }
}