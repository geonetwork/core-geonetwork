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

package org.fao.geonet.kernel;


import com.google.common.collect.Iterators;
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.constants.Edit;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.*;
import org.fao.geonet.exceptions.ILocalizedException;
import org.fao.geonet.utils.Log;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.jdom.filter.ElementFilter;
import org.springframework.context.ConfigurableApplicationContext;

import java.nio.file.Path;
import java.util.*;

import static org.fao.geonet.kernel.schema.MetadataSchema.SCHEMATRON_DIR;

/**
 * Class Handles validating a metadata against the applicable schematrons.
 *
 */
public class AbstractSchematronValidator {

    protected void runSchematron(String lang, Path schemaDir, List<MetadataValidation> validations, Element schemaTronXmlOut,
                                 int metadataId, Element md, ApplicableSchematron applicable) {
        final ConfigurableApplicationContext applicationContext = ApplicationContextHolder.get();
        ThesaurusManager thesaurusManager = applicationContext.getBean(ThesaurusManager.class);

        final Schematron schematron = applicable.schematron;
        final SchematronRequirement requirement = applicable.requirement;
        final String ruleId = schematron.getRuleName();

        Element report = new Element("report", Edit.NAMESPACE);
        report.setAttribute("rule", ruleId, Edit.NAMESPACE);
        report.setAttribute("displayPriority", "" + schematron.getDisplayPriority(), Edit.NAMESPACE);
        report.setAttribute("dbident", String.valueOf(schematron.getId()), Edit.NAMESPACE);
        report.setAttribute("required", requirement.toString(), Edit.NAMESPACE);

        MetadataValidationStatus metadataValidationStatus = null;
        int invalidRules = 0;
        int firedRules = 0;
        try {
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("lang", lang);
            params.put("rule", ruleId);
            params.put("thesaurusDir", thesaurusManager.getThesauriDirectory().toString());

            Path file = schemaDir.resolve(SCHEMATRON_DIR).resolve(schematron.getFile());
            Element xmlReport = Xml.transform(md, file, params);
            if (xmlReport != null) {
                report.addContent(xmlReport);
                // add results to persistent validation information
                @SuppressWarnings("unchecked")
                Iterator<Element> i = xmlReport.getDescendants(new ElementFilter("fired-rule", Geonet.Namespaces.SVRL));
                firedRules = Iterators.size(i);

                i = xmlReport.getDescendants(new ElementFilter("failed-assert", Geonet.Namespaces.SVRL));
                invalidRules = Iterators.size(i);

                metadataValidationStatus = invalidRules != 0 ? MetadataValidationStatus.INVALID : MetadataValidationStatus.VALID;
            }
        } catch (Exception e) {
            Log.error(Geonet.DATA_MANAGER, "WARNING: schematron xslt " + ruleId + " failed", e);

            // If an error occurs that prevents to verify schematron rules, add to show in report
            Element errorReport = new Element("schematronVerificationError", Edit.NAMESPACE);
            if (e.getCause() instanceof ILocalizedException) {
                errorReport.addContent(e.getCause().getLocalizedMessage());
            } else {
                errorReport.addContent(e.getMessage());
            }
            report.addContent(errorReport);

            // As the validation failed due to an exception lets identify the metadata as never validated.
            metadataValidationStatus = MetadataValidationStatus.NEVER_CALCULATED;
        } finally {
            if (metadataValidationStatus != null && validations != null) {
                validations.add(new MetadataValidation().
                    setId(new MetadataValidationId(metadataId, ruleId)).
                    setStatus(metadataValidationStatus).
                    setRequired(requirement == SchematronRequirement.REQUIRED).
                    setNumTests(firedRules).
                    setNumFailures(invalidRules));

            }
        }

        // -- append report to main XML report.
        schemaTronXmlOut.addContent(report);
    }
}
