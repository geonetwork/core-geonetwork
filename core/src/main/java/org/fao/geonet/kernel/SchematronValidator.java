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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.constants.Edit;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.MetadataValidation;
import org.fao.geonet.domain.MetadataValidationId;
import org.fao.geonet.domain.MetadataValidationStatus;
import org.fao.geonet.domain.Schematron;
import org.fao.geonet.domain.SchematronCriteria;
import org.fao.geonet.domain.SchematronCriteriaGroup;
import org.fao.geonet.domain.SchematronRequirement;
import org.fao.geonet.kernel.schema.MetadataSchema;
import org.fao.geonet.repository.SchematronCriteriaGroupRepository;
import org.fao.geonet.repository.SchematronRepository;
import org.fao.geonet.utils.Log;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.jdom.filter.ElementFilter;
import org.springframework.context.ConfigurableApplicationContext;

import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static org.fao.geonet.kernel.schema.MetadataSchema.SCHEMATRON_DIR;

/**
 * Class Handles validating a metadata against the applicable schematrons.
 *
 * @author Jesse on 4/1/2015.
 */
public class SchematronValidator {

    public Element applyCustomSchematronRules(String schema, int metadataId, Element md,
                                              String lang, List<MetadataValidation> validations) {
        SchemaManager schemaManager = ApplicationContextHolder.get().getBean(SchemaManager.class);

        MetadataSchema metadataSchema = schemaManager.getSchema(schema);
        final Path schemaDir = schemaManager.getSchemaDir(schema);

        Element schemaTronXmlOut = new Element("schematronerrors", Edit.NAMESPACE);
        try {
            List<ApplicableSchematron> applicableSchematron = getApplicableSchematronList(metadataId, md, metadataSchema);

            for (SchematronValidator.ApplicableSchematron applicable : applicableSchematron) {
                runSchematron(lang, schemaDir, validations, schemaTronXmlOut, metadataId, md, applicable);
            }
        } catch (Throwable e) {
            Element errorReport = new Element("schematronVerificationError", Edit.NAMESPACE);
            errorReport.addContent("Schematron error ocurred, rules could not be verified: " + e.getMessage());
            schemaTronXmlOut.addContent(errorReport);
        }

        if (schemaTronXmlOut.getChildren().isEmpty()) {
            schemaTronXmlOut = null;
        }

        return schemaTronXmlOut;
    }

    @VisibleForTesting
    List<ApplicableSchematron> getApplicableSchematronList(final int metadataId,
                                                           final Element md,
                                                           final MetadataSchema metadataSchema) {
        List<ApplicableSchematron> applicableSchematron = Lists.newArrayList();
        SchematronRepository schematronRepository = ApplicationContextHolder.get().getBean(SchematronRepository.class);

        final List<Schematron> schematronList = schematronRepository.findAllBySchemaName(metadataSchema.getName());

        Collections.sort(schematronList, Schematron.DISPLAY_PRIORITY_COMPARATOR);

        //Loop through all xsl files
        for (Schematron schematron : schematronList) {
            final ApplicableSchematron applicable = getApplicableSchematron(metadataId, md, metadataSchema, schematron);


            if (applicable.requirement != SchematronRequirement.DISABLED) {
                if (Log.isDebugEnabled(Geonet.DATA_MANAGER)) {
                    Log.debug(Geonet.DATA_MANAGER, " - rule:" + schematron.getRuleName());
                }

                applicableSchematron.add(applicable);
            }
        }
        return applicableSchematron;
    }

    @VisibleForTesting
    ApplicableSchematron getApplicableSchematron(final int metadataId,
                                                 final Element md,
                                                 final MetadataSchema metadataSchema,
                                                 final Schematron schematron) {
        //it contains absolute path to the xsl file
        final ConfigurableApplicationContext applicationContext = ApplicationContextHolder.get();
        SchematronCriteriaGroupRepository criteriaGroupRepository = applicationContext.getBean(SchematronCriteriaGroupRepository.class);

        List<SchematronCriteriaGroup> criteriaGroups = criteriaGroupRepository.findAllById_SchematronId(schematron.getId());

        //Loop through all criteria to see if apply schematron
        //if any criteria does not apply, do not apply at all (AND)
        SchematronRequirement requirement = SchematronRequirement.DISABLED;
        for (SchematronCriteriaGroup criteriaGroup : criteriaGroups) {
            List<SchematronCriteria> criteriaList = criteriaGroup.getCriteria();
            boolean apply = false;
            for (SchematronCriteria criteria : criteriaList) {
                apply = criteria.accepts(applicationContext, metadataId, md, metadataSchema.getSchemaNS());
                if (!apply) {
                    break;
                }
            }

            if (apply) {
                if (Log.isDebugEnabled(Geonet.DATA_MANAGER)) {
                    Log.debug(Geonet.DATA_MANAGER, " - Schematron group is accepted:" + criteriaGroup.getId().getName() +
                        " for schematron: " + schematron.getRuleName());
                }
                requirement = requirement.highestRequirement(criteriaGroup.getRequirement());
            } else {
                requirement = requirement.highestRequirement(SchematronRequirement.DISABLED);
            }
        }
        return new ApplicableSchematron(requirement, schematron);
    }

    private void runSchematron(String lang, Path schemaDir, List<MetadataValidation> validations, Element schemaTronXmlOut,
                               int metadataId, Element md, SchematronValidator.ApplicableSchematron applicable) {
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
                int firedRules = Iterators.size(i);

                i = xmlReport.getDescendants(new ElementFilter("failed-assert", Geonet.Namespaces.SVRL));
                int invalidRules = Iterators.size(i);

                if (validations != null) {
                    validations.add(new MetadataValidation().
                        setId(new MetadataValidationId(metadataId, ruleId)).
                        setStatus(invalidRules != 0 ? MetadataValidationStatus.INVALID : MetadataValidationStatus.VALID).
                        setRequired(requirement == SchematronRequirement.REQUIRED).
                        setNumTests(firedRules).
                        setNumFailures(invalidRules));

                }
            }
        } catch (Exception e) {
            Log.error(Geonet.DATA_MANAGER, "WARNING: schematron xslt " + ruleId + " failed", e);

            // If an error occurs that prevents to verify schematron rules, add to show in report
            Element errorReport = new Element("schematronVerificationError", Edit.NAMESPACE);
            errorReport.addContent("Schematron error ocurred, rules could not be verified: " + e.getMessage());
            report.addContent(errorReport);
        }

        // -- append report to main XML report.
        schemaTronXmlOut.addContent(report);
    }

    static class ApplicableSchematron {
        final SchematronRequirement requirement;
        final Schematron schematron;

        ApplicableSchematron(SchematronRequirement requirement, Schematron schematron) {
            this.requirement = requirement;
            this.schematron = schematron;
        }
    }

}
