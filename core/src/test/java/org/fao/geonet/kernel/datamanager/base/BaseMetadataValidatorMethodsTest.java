//=============================================================================
//===	Copyright (C) 2001-2026 Food and Agriculture Organization of the
//===	United Nations (FAO-UN), United Nations World Food Programme (WFP)
//===	and United Nations Environment Programme (UNEP)
//===
//===	This program is free software; you can redistribute it and/or modify
//===	it under the terms of the GNU General Public License as published by
//===	the Free Software Foundation; either version 2 of the License, or (at
//===	your option) any later version.
//===
//===	This program is distributed in the hope that it will be useful, but
//===	WITHOUT ANY WARRANTY; without even the implied warranty of
//===	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
//===	General Public License for more details.
//===
//===	You should have received a copy of the GNU General Public License
//===	along with this program; if not, write to the Free Software
//===	Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
//===
//===	Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
//===	Rome - Italy. email: geonetwork@osgeo.org
//==============================================================================

package org.fao.geonet.kernel.datamanager.base;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.LinkedHashMap;

import org.fao.geonet.kernel.datamanager.IMetadataUtils;
import org.jdom.Element;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import jeeves.server.context.ServiceContext;

public class BaseMetadataValidatorMethodsTest {

    private BaseMetadataValidator validator;
    private IMetadataUtils metadataUtils;
    private ServiceContext context;

    @Before
    public void setUp() {
        validator = new BaseMetadataValidator();
        metadataUtils = mock(IMetadataUtils.class);
        context = mock(ServiceContext.class);
        ReflectionTestUtils.setField(validator, "metadataUtils", metadataUtils);
    }

    @Test
    public void testExtractMetadataTitle_LanguageExists() {
        LinkedHashMap<String, String> titles = new LinkedHashMap<>();
        titles.put("eng", "English Title");
        titles.put("fra", "French Title");

        String result = (String) ReflectionTestUtils.invokeMethod(validator, "extractMetadataTitle", titles, "eng");
        assertEquals("English Title", result);
    }

    @Test
    public void testExtractMetadataTitle_LanguageDoesNotExist_ReturnsFirstValue() {
        LinkedHashMap<String, String> titles = new LinkedHashMap<>();
        titles.put("eng", "English Title");
        titles.put("fra", "French Title");

        String result = (String) ReflectionTestUtils.invokeMethod(validator, "extractMetadataTitle", titles, "ger");
        assertEquals("English Title", result);
    }

    @Test
    public void testExtractMetadataTitle_EmptyMap_ReturnsEmptyString() {
        LinkedHashMap<String, String> titles = new LinkedHashMap<>();

        String result = (String) ReflectionTestUtils.invokeMethod(validator, "extractMetadataTitle", titles, "eng");
        assertEquals("", result);
    }

    @Test
    public void testBuildSchematronValidationErrorMessage_WithFileName() {
        String errorReport = "Some error";
        String result = (String) ReflectionTestUtils.invokeMethod(validator, "buildSchematronValidationErrorMessage",
                context, "file.xml", "iso19139", new Element("root"), errorReport);
        assertEquals("Schematron errors detected for metadata in file 'file.xml' - Some error", result);
    }

    @Test
    public void testBuildSchematronValidationErrorMessage_WithoutFileName_Success() throws Exception {
        String schema = "iso19139";
        Element xml = new Element("root");
        String errorReport = "Some error";
        String lang = "eng";
        String uuid = "uuid-123";
        LinkedHashMap<String, String> titles = new LinkedHashMap<>();
        titles.put("eng", "Title");

        when(context.getLanguage()).thenReturn(lang);
        when(metadataUtils.extractUUID(schema, xml)).thenReturn(uuid);
        when(metadataUtils.extractTitles(schema, xml)).thenReturn(titles);

        String result = (String) ReflectionTestUtils.invokeMethod(validator, "buildSchematronValidationErrorMessage",
                context, null, schema, xml, errorReport);
        assertEquals("Schematron errors detected for metadata with UUID 'uuid-123' / Title - Some error", result);
    }

    @Test
    public void testBuildSchematronValidationErrorMessage_WithoutFileName_Exception() throws Exception {
        String schema = "iso19139";
        Element xml = new Element("root");
        String errorReport = "Some error";

        when(metadataUtils.extractUUID(schema, xml)).thenThrow(new RuntimeException("Error"));

        String result = (String) ReflectionTestUtils.invokeMethod(validator, "buildSchematronValidationErrorMessage",
                context, "", schema, xml, errorReport);
        assertEquals("Schematron errors detected - Some error", result);
    }
}
