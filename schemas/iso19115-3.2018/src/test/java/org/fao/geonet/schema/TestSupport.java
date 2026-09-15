/*
 * Copyright (C) 2001-2025 Food and Agriculture Organization of the
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

package org.fao.geonet.schema;

import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertArrayEquals;

public class TestSupport {

    public static Path getResource(String name) throws URISyntaxException {
        return Paths.get(TestSupport.class.getClassLoader().getResource(name).toURI());
    }

    public static Path getResourceInsideSchema(String pathToResourceInsideSchema) throws URISyntaxException {
        return getResource(pathToResourceInsideSchema);
    }

    public static void assertGeneratedDataByteMatchExpected(String expectedFileName, String actual, boolean generateExpectedFile) throws IOException, URISyntaxException {
        byte[] expected;
        if (generateExpectedFile) {
            generateFileWithData(expectedFileName, actual);
            expected = actual.getBytes(StandardCharsets.UTF_8);
        } else {
            expected = Files.readAllBytes(getResource(expectedFileName));
        }
        assertArrayEquals(expected, actual.getBytes(StandardCharsets.UTF_8));
    }

    private static void generateFileWithData(String expectedFileName, String actual) throws IOException {
        try (FileWriter fw = new FileWriter("src/test/resources/" + expectedFileName)) {
            fw.write(actual);
            fw.flush();
        }
    }
}
