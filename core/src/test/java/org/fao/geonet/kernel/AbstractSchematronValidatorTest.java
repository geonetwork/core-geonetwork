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

package org.fao.geonet.kernel;

import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.api.exception.ForbiddenException;
import org.fao.geonet.api.exception.ResourceNotFoundException;
import org.fao.geonet.constants.Edit;
import org.fao.geonet.domain.MetadataValidation;
import org.fao.geonet.domain.MetadataValidationStatus;
import org.fao.geonet.domain.Schematron;
import org.fao.geonet.domain.SchematronRequirement;
import org.fao.geonet.exceptions.ILocalizedException;
import org.fao.geonet.exceptions.LocalizedException;
import org.fao.geonet.exceptions.LocalizedRuntimeException;
import org.fao.geonet.exceptions.ResourceNotFoundEx;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import org.junit.Test;
import org.springframework.context.ConfigurableApplicationContext;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;


public class AbstractSchematronValidatorTest {

    @Test
    public void testRunSchematronExceptionHandling() {
        final String errorMessage="Test Exception";

        ConfigurableApplicationContext mockContext = Mockito.mock(ConfigurableApplicationContext.class);
        try (MockedStatic<ApplicationContextHolder> mocked = Mockito.mockStatic(ApplicationContextHolder.class)) {
            mocked.when(ApplicationContextHolder::get).thenReturn(mockContext);
            ThesaurusManager mockThesaurusManager = Mockito.mock(ThesaurusManager.class);
            Mockito.when(mockContext.getBean(ThesaurusManager.class)).thenReturn(mockThesaurusManager);

            AbstractSchematronValidator validator = new AbstractSchematronValidator();
            String lang = "en";
            Path schemaDir = Mockito.mock(Path.class);
            List<MetadataValidation> validations = new ArrayList<>();
            Element schemaTronXmlOut = new Element("schemaTronXmlOut");
            int metadataId = 1;
            Element md = new Element("metadata");
            ApplicableSchematron applicable = Mockito.mock(ApplicableSchematron.class);

            Schematron schematron = new Schematron();
            schematron.setSchemaName("testSchemaName");

            Mockito.when(applicable.getSchematron()).thenReturn(schematron);
            Mockito.when(applicable.getRequirement()).thenReturn(SchematronRequirement.DISABLED);
            Mockito.when(mockThesaurusManager.getThesauriDirectory()).thenThrow(new RuntimeException(errorMessage));

            validator.runSchematron(lang, schemaDir, validations, schemaTronXmlOut, metadataId, md, applicable);

            Element errorReport = schemaTronXmlOut.getChild("report", Edit.NAMESPACE)
                .getChild("schematronVerificationError", Edit.NAMESPACE);
            assertNotNull("schematronVerificationError element should exist", errorReport);
            assertEquals(errorMessage, errorReport.getText());
            assertEquals(MetadataValidationStatus.NEVER_CALCULATED, validations.get(0).getStatus());
        }
    }

    @Test
    public void testRunSchematronLocalizedExceptionHandling() {
        final String errorMessage="Test Localized Message";
        ConfigurableApplicationContext mockContext = Mockito.mock(ConfigurableApplicationContext.class);
        try (MockedStatic<ApplicationContextHolder> mocked = Mockito.mockStatic(ApplicationContextHolder.class)) {
            mocked.when(ApplicationContextHolder::get).thenReturn(mockContext);
            ThesaurusManager mockThesaurusManager = Mockito.mock(ThesaurusManager.class);
            Mockito.when(mockContext.getBean(ThesaurusManager.class)).thenReturn(mockThesaurusManager);

            ForbiddenException mockLocalizedException = Mockito.spy(ForbiddenException.class);
            Mockito.when(mockLocalizedException.getLocalizedMessage()).thenReturn(errorMessage);

            AbstractSchematronValidator validator = new AbstractSchematronValidator();
            String lang = "fr";
            Path schemaDir = Mockito.mock(Path.class);
            List<MetadataValidation> validations = new ArrayList<>();
            Element schemaTronXmlOut = new Element("schemaTronXmlOut");
            int metadataId = 1;
            Element md = new Element("metadata");
            ApplicableSchematron applicable = Mockito.mock(ApplicableSchematron.class);

            Schematron schematron = new Schematron();
            schematron.setSchemaName("testSchemaName");

            Mockito.when(applicable.getSchematron()).thenReturn(schematron);
            Mockito.when(applicable.getRequirement()).thenReturn(SchematronRequirement.DISABLED);
            Mockito.when(mockThesaurusManager.getThesauriDirectory()).thenThrow(new RuntimeException("Runtime exception caused by localized exception",mockLocalizedException));

            validator.runSchematron(lang, schemaDir, validations, schemaTronXmlOut, metadataId, md, applicable);

            Element errorReport = schemaTronXmlOut.getChild("report", Edit.NAMESPACE)
                .getChild("schematronVerificationError", Edit.NAMESPACE);
            assertNotNull("schematronVerificationError element should exist", errorReport);
            assertEquals(errorMessage, errorReport.getText());
            assertEquals(MetadataValidationStatus.NEVER_CALCULATED, validations.get(0).getStatus());
        }
    }
}
