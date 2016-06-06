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

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;

import org.junit.Before;
import org.junit.Test;

import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertTrue;

/**
 * @author Jesse on 11/4/2014.
 */
public class IOMemoryFsTest extends AbstractIOTest {
    private FileSystem memoryFs;
    private Path root;

    @Before
    public void setUp() throws Exception {
        memoryFs = Jimfs.newFileSystem(Configuration.unix());
        root = memoryFs.getPath("rootPath");
        Files.createDirectories(root);
        IO.setFileSystemThreadLocal(memoryFs);
    }

    @Override
    protected Path getRootPath() {
        return root;
    }

    @Override
    protected URI getFileUri() {
        return memoryFs.getPath("part").toUri();
    }

    @Test
    public void testCopyFromAnotherFs() throws Exception {
        Path path = Paths.get(XmlTest.class.getResource("xmltest").toURI());

        IO.copyDirectoryOrFile(path, root, true);

        assertTrue(Files.isDirectory(root.resolve("xmltest/xsl")));
        assertTrue(Files.isRegularFile(root.resolve("xmltest/xml.xsd")));
        assertTrue(Files.isRegularFile(root.resolve("xmltest/XMLSchema.dtd")));
        assertTrue(Files.isRegularFile(root.resolve("xmltest/dependant2.xsl")));
        assertTrue(Files.isRegularFile(root.resolve("xmltest/xsl/test.xsl")));
        assertTrue(Files.isRegularFile(root.resolve("xmltest/xsl/dependant1.xsl")));
        assertTrue(Files.isDirectory(root.resolve("xmltest/xsl/nested")));
        assertTrue(Files.isRegularFile(root.resolve("xmltest/xsl/nested/dependant3.xsl")));

        assertTrue(Files.isDirectory(root.resolve("xmltest").resolve("xsl")));
        assertTrue(Files.isRegularFile(root.resolve("xmltest").resolve("xml.xsd")));
        assertTrue(Files.isRegularFile(root.resolve("xmltest").resolve("XMLSchema.dtd")));
        assertTrue(Files.isRegularFile(root.resolve("xmltest").resolve("dependant2.xsl")));
        assertTrue(Files.isRegularFile(root.resolve("xmltest").resolve("xsl").resolve("test.xsl")));
        assertTrue(Files.isRegularFile(root.resolve("xmltest").resolve("xsl").resolve("dependant1.xsl")));
        assertTrue(Files.isDirectory(root.resolve("xmltest").resolve("xsl").resolve("nested")));
        assertTrue(Files.isRegularFile(root.resolve("xmltest").resolve("xsl").resolve("nested").resolve("dependant3.xsl")));

    }
}
