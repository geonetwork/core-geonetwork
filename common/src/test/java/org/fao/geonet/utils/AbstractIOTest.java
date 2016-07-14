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

package org.fao.geonet.utils;

import com.google.common.io.Resources;

import org.fao.geonet.Constants;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public abstract class AbstractIOTest {
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @After
    public void resetIO() {
        IO.setFileSystem(null);
        IO.setFileSystemThreadLocal(null);
    }

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
        IO.copyDirectoryOrFile(file, to1, false);
        assertTrue(Files.exists(file));
        assertTrue(Files.exists(to1));

        final Path to2 = getRootPath().resolve("to");
        IO.copyDirectoryOrFile(getRootPath().resolve("src"), to2, true);
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

        IO.copyDirectoryOrFile(file, java, true);
        assertTrue(Files.exists(file));
        assertTrue(Files.exists(java));

        final Path to2 = getRootPath().resolve("to");
        Files.createDirectories(to2);
        final Path src = getRootPath().resolve("src");
        IO.copyDirectoryOrFile(src, to2, true);
        assertTrue(Files.exists(src));
        assertTrue(Files.exists(to2));
        assertTrue(Files.exists(to2.resolve("src")));
        assertTrue(Files.exists(to2.resolve("src").resolve("main")));
        assertTrue(Files.exists(to2.resolve("src").resolve("main").resolve("java")));
        assertTrue(Files.exists(to2.resolve("src").resolve("main").resolve("java").resolve("src.txt")));
    }

    @Test
    public void testCopyFromNonExistantDir() throws Exception {
        IO.copyDirectoryOrFile(IO.toPath("lkasjflkajsfljsaflasjflksajfaslj32487asfjk"), IO.toPath("to"), true);
    }

    @Test
    public void testMoveNonExistantDir() throws Exception {
        IO.moveDirectoryOrFile(IO.toPath("lkasjflkajsfljsaflasjflksajfaslj32487asfjk"), IO.toPath("to"), true);
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
        IO.moveDirectoryOrFile(file, to1, true);
        assertFalse(Files.exists(file));
        assertTrue(Files.exists(to1));

        final Path to2 = getRootPath().resolve("to");
        IO.moveDirectoryOrFile(getRootPath().resolve("src"), to2, true);
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

        IO.moveDirectoryOrFile(file, java, true);
        assertFalse(Files.exists(file));
        assertTrue(Files.exists(java));
        assertTrue(Files.exists(java.resolve(file.getFileName())));

        final Path to2 = getRootPath().resolve("to");
        Files.createDirectories(to2);
        final Path src = getRootPath().resolve("src");
        IO.moveDirectoryOrFile(src, to2, true);
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

    @Test
    public void testtoURL() throws Exception {
        final Path rootPath = getRootPath();
        final Path textFile = rootPath.resolve("text");
        final String text = "Hello";
        Files.write(textFile, text.getBytes(Constants.CHARSET));

        final URL url1 = IO.toURL(textFile);
        assertEquals(text, Resources.toString(url1, Constants.CHARSET));

        final URL url2 = IO.toURL(textFile.toUri());
        assertEquals(text, Resources.toString(url2, Constants.CHARSET));
    }

    protected URI getFileUri() {
        return new File("part").toURI();
    }
}
