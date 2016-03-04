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
