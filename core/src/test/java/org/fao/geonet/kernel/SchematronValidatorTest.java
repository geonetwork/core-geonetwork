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

import jeeves.server.context.ServiceContext;

import org.fao.geonet.AbstractCoreIntegrationTest;
import org.fao.geonet.domain.Schematron;
import org.fao.geonet.domain.SchematronCriteria;
import org.fao.geonet.domain.SchematronCriteriaGroup;
import org.fao.geonet.domain.SchematronCriteriaType;
import org.fao.geonet.domain.SchematronRequirement;
import org.fao.geonet.kernel.ApplicableSchematron;
import org.fao.geonet.kernel.mef.MEFLibIntegrationTest;
import org.fao.geonet.kernel.schema.MetadataSchema;
import org.fao.geonet.repository.SchematronCriteriaGroupRepository;
import org.fao.geonet.repository.SchematronRepository;
import org.jdom.Element;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @author Jesse on 4/1/2015.
 */
public class SchematronValidatorTest extends AbstractCoreIntegrationTest {
    @Autowired
    SchematronValidator schematronValidator;
    @Autowired
    DataManager dataManager;
    @Autowired
    SchemaManager schemaManager;
    @Autowired
    private SchematronRepository schematronRepository;
    @Autowired
    private SchematronCriteriaGroupRepository criteriaGroupRepository;
    private int id;
    private Element metadata;
    private MetadataSchema schema;
    private Schematron schematron;

    @Before
    public void setUp() throws Exception {

        this.schematron = new Schematron();
        schematron.setSchemaName("test");
        schematron.setFile("test.xsl");
        schematron.setDisplayPriority(1);
        schematron = schematronRepository.save(schematron);

        final SchematronCriteriaGroup group = new SchematronCriteriaGroup();
        group.setSchematron(schematron);
        group.getId().setName("testGroup1");
        final SchematronCriteria criteria = new SchematronCriteria();
        criteria.setType(SchematronCriteriaType.ALWAYS_ACCEPT);
        criteria.setValue("");
        group.setRequirement(SchematronRequirement.REQUIRED);
        group.addCriteria(criteria);
        this.criteriaGroupRepository.save(group);

        final SchematronCriteriaGroup group2 = new SchematronCriteriaGroup();
        group2.setSchematron(schematron);
        group2.setRequirement(SchematronRequirement.REQUIRED);
        group2.getId().setName("testGroup2");
        final SchematronCriteria criteria2 = new SchematronCriteria();
        criteria2.setType(SchematronCriteriaType.XPATH);
        criteria2.setValue("gmd:val = 'no no no no no'");
        group2.addCriteria(criteria2);
        this.criteriaGroupRepository.save(group2);

        final ServiceContext context = createServiceContext();
        loginAsAdmin(context);

        final MEFLibIntegrationTest.ImportMetadata invoke = new MEFLibIntegrationTest.ImportMetadata(this, context).invoke();

        this.id = Integer.parseInt(invoke.getMetadataIds().get(0));
        this.metadata = dataManager.getMetadata(invoke.getMetadataIds().get(0));
        this.schema = schemaManager.getSchema(this.dataManager.autodetectSchema(metadata));
    }

    @Test
    public void testGetApplicableSchematron() throws Exception {
        ApplicableSchematron applicableSchematron = schematronValidator.getApplicableSchematron(id, metadata, schema, schematron);

        assertEquals(SchematronRequirement.REQUIRED, applicableSchematron.requirement);
    }

    @Test
    public void testGetApplicableSchematronList() throws Exception {
        List<ApplicableSchematron> applicableSchematron = schematronValidator.getApplicableSchematronList(id, metadata, schema);

        boolean found = false;
        for (ApplicableSchematron applicable : applicableSchematron) {
            found |= applicable.schematron.getId() == schematron.getId();
        }
    }
}
