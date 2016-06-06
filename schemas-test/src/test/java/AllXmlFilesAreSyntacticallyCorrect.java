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

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

import org.fao.geonet.AbstractCoreIntegrationTest;
import org.fao.geonet.utils.Xml;
import org.jdom.JDOMException;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;

import static org.junit.Assert.assertTrue;

/**
 * Test each xml file in each schema plugin and verify that they are all well-formed.
 *
 * @author Jesse on 2/5/2015.
 */
public class AllXmlFilesAreSyntacticallyCorrect {
    @Test
    public void testWellformedXmlFiles() throws Exception {
        Path classFile = AbstractCoreIntegrationTest.getClassFile(getClass()).toPath();
        Path tmp = classFile;
        while (tmp != null && !Files.exists(tmp.resolve("schemas/dublin-core"))) {
            tmp = tmp.getParent();
        }
        final List<String> badXmlFiles = Lists.newArrayList();

        if (tmp == null) {
            throw new RuntimeException("Check schemas data directory!");
        }
        Path resolve = tmp.resolve("schemas");
        try (DirectoryStream<Path> paths = Files.newDirectoryStream(resolve)) {
            for (Path path : paths) {
                Path pluginDir = path.resolve("src/main/plugin");
                if (Files.exists(pluginDir)) {
                    Files.walkFileTree(pluginDir, new SimpleFileVisitor<Path>() {
                        @Override
                        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                            if (file.toString().endsWith(".xml") || file.toString().endsWith(".html") ||
                                file.toString().endsWith(".xsd") || file.toString().endsWith(".xsl")) {
                                try {
                                    Xml.loadFile(file);
                                } catch (JDOMException e) {
                                    badXmlFiles.add(file + " -- " + e.getMessage());
                                }
                            }
                            return super.visitFile(file, attrs);
                        }
                    });
                }
            }
        }

        assertTrue("\n  *" + Joiner.on("\n  * ").join(badXmlFiles), badXmlFiles.isEmpty());
    }
}
