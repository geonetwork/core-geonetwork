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

import com.google.common.collect.Lists;
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.constants.Edit;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.MetadataValidation;
import org.fao.geonet.domain.Schematron;
import org.fao.geonet.domain.SchematronCriteria;
import org.fao.geonet.domain.SchematronCriteriaGroup;
import org.fao.geonet.domain.SchematronRequirement;
import org.fao.geonet.kernel.schema.MetadataSchema;
import org.fao.geonet.repository.SchematronCriteriaGroupRepository;
import org.fao.geonet.repository.SchematronRepository;
import org.fao.geonet.utils.Log;
import org.jdom.Element;
import org.springframework.context.ConfigurableApplicationContext;

import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

/**
 * Class Handles validating a metadata against the applicable schematrons.
 *
 * @author Jesse on 4/1/2015.
 */
public class SchematronValidator extends AbstractSchematronValidator {

    public Element applyCustomSchematronRules(String schema, int metadataId, Element md,
                                              String lang, List<MetadataValidation> validations) {
        SchemaManager schemaManager = ApplicationContextHolder.get().getBean(SchemaManager.class);

        MetadataSchema metadataSchema = schemaManager.getSchema(schema);
        final Path schemaDir = schemaManager.getSchemaDir(schema);

        Element schemaTronXmlOut = new Element("schematronerrors", Edit.NAMESPACE);
        try {
            List<ApplicableSchematron> applicableSchematron = getApplicableSchematronList(metadataId, md, metadataSchema);

            for (ApplicableSchematron applicable : applicableSchematron) {
                runSchematron(lang, schemaDir, validations, schemaTronXmlOut, metadataId, md, applicable);
            }
        } catch (Throwable e) {
            Element errorReport = new Element("schematronVerificationError", Edit.NAMESPACE);
            errorReport.addContent("Schematron error occurred, rules could not be verified: " + e.getMessage());
            schemaTronXmlOut.addContent(errorReport);
        }

        if (schemaTronXmlOut.getChildren().isEmpty()) {
            schemaTronXmlOut = null;
        }

        return schemaTronXmlOut;
    }


    public List<ApplicableSchematron> getApplicableSchematronList(final int metadataId,
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


    public ApplicableSchematron getApplicableSchematron(final int metadataId,
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




}
