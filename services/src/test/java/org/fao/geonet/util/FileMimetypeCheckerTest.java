/*
 * Copyright (C) 2001-2024 Food and Agriculture Organization of the
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

package org.fao.geonet.util;

import org.apache.commons.compress.utils.IOUtils;
import org.fao.geonet.AbstractCoreIntegrationTest;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;


public class FileMimetypeCheckerTest {

    private static final String resources = AbstractCoreIntegrationTest.getClassFile(FileMimetypeCheckerTest.class).getParent();

    @Test
    public void testValidImageMimetype() throws Exception {
        FileMimetypeChecker fileMimetypeChecker = new FileMimetypeChecker();

        Map<String, String> imageFiles = new HashMap<>();
        imageFiles.put("test.png", "image/png");
        imageFiles.put("test.gif", "image/gif");
        imageFiles.put("test.jpeg", "image/jpeg");

        for (Map.Entry<String, String> entry : imageFiles.entrySet()) {
            File file = Paths.get(resources, entry.getKey()).toFile();
            FileInputStream input = new FileInputStream(file);
            MultipartFile multipartFile = new MockMultipartFile("file",
                file.getName(), entry.getValue(), IOUtils.toByteArray(input));

            try {
                fileMimetypeChecker.checkValidImageMimeType(multipartFile);
            } catch (IllegalArgumentException ex) {
                Assert.fail(ex.getMessage());
            }
        }
    }

    @Test
    public void testNoValidImageMimetype() throws Exception {
        FileMimetypeChecker fileMimetypeChecker = new FileMimetypeChecker();

        File file = Paths.get(resources, "report.csv").toFile();
        FileInputStream input = new FileInputStream(file);
        MultipartFile multipartFile = new MockMultipartFile("file",
            file.getName(), "text/plain", IOUtils.toByteArray(input));

        try {
            fileMimetypeChecker.checkValidImageMimeType(multipartFile);
            Assert.fail("\"report.csv\" is not an image file");
        } catch (IllegalArgumentException ex) {
            Assert.assertEquals("File 'report.csv' with type 'text/plain' is not supported. To allow this file type, configure it in System Settings > Allowed file mime types to attach to a metadata record.", ex.getMessage());
        }
    }

    @Test
    public void testValidCsvMimetype() throws Exception {
        FileMimetypeChecker fileMimetypeChecker = new FileMimetypeChecker();

        File file = Paths.get(resources, "report.csv").toFile();
        FileInputStream input = new FileInputStream(file);
        MultipartFile multipartFile = new MockMultipartFile("file",
            file.getName(), "text/plain", IOUtils.toByteArray(input));

        try {
            fileMimetypeChecker.checkValidCsvMimeType(multipartFile);
        } catch (IllegalArgumentException ex) {
            Assert.fail(ex.getMessage());
        }
    }

    @Test
    public void testNoValidCsvMimetype() throws Exception {
        FileMimetypeChecker fileMimetypeChecker = new FileMimetypeChecker();

        File file = Paths.get(resources, "test.png").toFile();
        FileInputStream input = new FileInputStream(file);
        MultipartFile multipartFile = new MockMultipartFile("file",
            file.getName(), "image/png", IOUtils.toByteArray(input));

        try {
            fileMimetypeChecker.checkValidCsvMimeType(multipartFile);
            Assert.fail("\"test.png\" is not a CSV file");
        } catch (IllegalArgumentException ex) {
            Assert.assertEquals("File 'test.png' with type 'image/png' is not supported. To allow this file type, configure it in System Settings > Allowed file mime types to attach to a metadata record.", ex.getMessage());
        }
    }

    @Test
    public void testValidThesaurusMimetype() throws Exception {
        FileMimetypeChecker fileMimetypeChecker = new FileMimetypeChecker();

        File file = Paths.get(resources, "regions.rdf").toFile();
        FileInputStream input = new FileInputStream(file);
        MultipartFile multipartFile = new MockMultipartFile("file",
            file.getName(), "application/rdf+xml", IOUtils.toByteArray(input));

        try {
            fileMimetypeChecker.checkValidThesaurusMimeType(multipartFile);
        } catch (IllegalArgumentException ex) {
            Assert.fail(ex.getMessage());
        }
    }

    @Test
    public void testNoValidThesaurusMimetype() throws Exception {
        FileMimetypeChecker fileMimetypeChecker = new FileMimetypeChecker();

        File file = Paths.get(resources, "report.csv").toFile();
        FileInputStream input = new FileInputStream(file);
        MultipartFile multipartFile = new MockMultipartFile("file",
            file.getName(), "text/plain", IOUtils.toByteArray(input));

        try {
            fileMimetypeChecker.checkValidThesaurusMimeType(multipartFile);
            Assert.fail("\"report.csv\" is not a thesaurus file");
        } catch (IllegalArgumentException ex) {
            Assert.assertEquals("File 'report.csv' with type 'text/plain' is not supported. To allow this file type, configure it in System Settings > Allowed file mime types to attach to a metadata record.", ex.getMessage());
        }
    }


    @Test
    public void testValidMimetype() throws Exception {
        FileMimetypeChecker fileMimetypeChecker = new FileMimetypeChecker();

        File file = Paths.get(resources, "template.pdf").toFile();
        FileInputStream input = new FileInputStream(file);
        MultipartFile multipartFile = new MockMultipartFile("file",
            file.getName(), "application/pdf", IOUtils.toByteArray(input));

        try {
            fileMimetypeChecker.checkValidMimeType(multipartFile, new String[] {"application/pdf"});
        } catch (IllegalArgumentException ex) {
            Assert.fail(ex.getMessage());
        }
    }

    @Test
    public void testNoValidMimetype() throws Exception {
        FileMimetypeChecker fileMimetypeChecker = new FileMimetypeChecker();

        File file = Paths.get(resources, "template.pdf").toFile();
        FileInputStream input = new FileInputStream(file);
        MultipartFile multipartFile = new MockMultipartFile("file",
            file.getName(), "application/pdf", IOUtils.toByteArray(input));

        try {
            fileMimetypeChecker.checkValidMimeType(multipartFile, new String[] {"text/plain"});
            Assert.fail("\"template.pdf\" is not a text file");
        } catch (IllegalArgumentException ex) {
            Assert.assertEquals("File 'template.pdf' with type 'application/pdf' is not supported. To allow this file type, configure it in System Settings > Allowed file mime types to attach to a metadata record.", ex.getMessage());
        }
    }
}
