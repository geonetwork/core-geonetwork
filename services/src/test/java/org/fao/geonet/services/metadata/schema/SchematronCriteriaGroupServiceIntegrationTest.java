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

package org.fao.geonet.services.metadata.schema;

import static org.fao.geonet.services.metadata.schema.SchematronServiceAction.*;
import static org.junit.Assert.*;

import jeeves.server.context.ServiceContext;

import org.fao.geonet.constants.Params;
import org.fao.geonet.domain.GeonetEntity;
import org.fao.geonet.domain.Schematron;
import org.fao.geonet.domain.SchematronRequirement;
import org.fao.geonet.exceptions.BadParameterEx;
import org.fao.geonet.repository.SchematronRepositoryTest;
import org.jdom.Element;
import org.junit.Test;

import static org.fao.geonet.domain.Pair.read;
import static org.junit.Assert.assertEquals;

/**
 * Test SchematronCriteriaGroupServiceIntegration. Created by Jesse on 2/12/14.
 */
public class SchematronCriteriaGroupServiceIntegrationTest extends AbstractSchematronServiceIntegrationTest {

    @Test
    public void testExecListAll_ExcludeCriteriaAndSchematron() throws Exception {
        ServiceContext context = createServiceContext();
        loginAsAdmin(context);

        Element params = createParams();

        Element result = createService(LIST).exec(params, context);
        assertEquals(4, result.getChildren().size());

        assertEquals(0, result.getChild(GeonetEntity.RECORD_EL_NAME).getChildren("schematron").size());
        assertCriteriaOnlyId(result);


        result = createService(LIST).exec(createParams(), context);

        assertEquals(4, result.getChildren().size());

        assertEquals(0, result.getChild(GeonetEntity.RECORD_EL_NAME).getChildren("schematron").size());
        assertCriteriaOnlyId(result);
    }

    @Test
    public void testExecListAll_IncludeCriteria() throws Exception {

        ServiceContext context = createServiceContext();
        loginAsAdmin(context);

        Element params = createParams(
            read(SchematronCriteriaGroupService.PARAM_INCLUDE_CRITERIA, Boolean.TRUE)
        );

        Element result = createService(LIST).exec(params, context);
        assertEquals(4, result.getChildren().size());

        assertEquals(0, result.getChild(GeonetEntity.RECORD_EL_NAME).getChildren("schematron").size());
        assertFullCriteria(result);
    }

    @Test
    public void testExecListAll_IncludeSchematron() throws Exception {
        ServiceContext context = createServiceContext();
        loginAsAdmin(context);

        Element params = createParams(
            read(SchematronCriteriaGroupService.PARAM_INCLUDE_SCHEMATRON, Boolean.TRUE)
        );

        Element result = createService(LIST).exec(params, context);
        assertEquals(4, result.getChildren().size());

        assertEquals(1, result.getChild(GeonetEntity.RECORD_EL_NAME).getChildren("schematron").size());
        assertCriteriaOnlyId(result);
    }

    @Test
    public void testExecListAll_IncludeSchematronAndCriteria() throws Exception {
        ServiceContext context = createServiceContext();
        loginAsAdmin(context);

        Element params = createParams(
            read(SchematronCriteriaGroupService.PARAM_INCLUDE_SCHEMATRON, Boolean.TRUE),
            read(SchematronCriteriaGroupService.PARAM_INCLUDE_CRITERIA, Boolean.TRUE)
        );

        Element result = createService(LIST).exec(params, context);
        assertEquals(4, result.getChildren().size());

        assertEquals(1, result.getChild(GeonetEntity.RECORD_EL_NAME).getChildren("schematron").size());
        assertFullCriteria(result);
    }

    @Test
    public void testExecListOwnedBySchematron() throws Exception {
        ServiceContext context = createServiceContext();
        loginAsAdmin(context);

        Element params = createParams(
            read(SchematronCriteriaGroupService.PARAM_SCHEMATRON_ID, _group1_Name1_SchematronId1.getId().getSchematronId()),
            read(SchematronCriteriaGroupService.PARAM_INCLUDE_SCHEMATRON, Boolean.TRUE)
        );

        Element result = createService(LIST).exec(params, context);
        assertEquals(2, result.getChildren().size());

        assertEquals(1, result.getChild(GeonetEntity.RECORD_EL_NAME).getChildren("schematron").size());
        assertCriteriaOnlyId(result);

        assertGroupNames(result, _group1_Name1_SchematronId1, _group3_Name3_SchemtronId1);
        assertSchematronIds(result, _group1_Name1_SchematronId1, _group3_Name3_SchemtronId1);
        assertNotSchematronIds(result, _group2_Name2_SchematronId2, _group4_Name2_SchematronId4);
    }

    @Test
    public void testExecListHasGroupName() throws Exception {
        ServiceContext context = createServiceContext();
        loginAsAdmin(context);

        Element params = createParams(
            read(SchematronCriteriaGroupService.PARAM_GROUP_NAME, _group2_Name2_SchematronId2.getId().getName()),
            read(SchematronCriteriaGroupService.PARAM_INCLUDE_SCHEMATRON, Boolean.TRUE)
        );

        Element result = createService(LIST).exec(params, context);
        assertEquals(2, result.getChildren().size());

        assertEquals(1, result.getChild(GeonetEntity.RECORD_EL_NAME).getChildren("schematron").size());
        assertCriteriaOnlyId(result);

        assertGroupNames(result, _group2_Name2_SchematronId2, _group4_Name2_SchematronId4);
        assertNotGroupNames(result, _group1_Name1_SchematronId1, _group3_Name3_SchemtronId1);
        assertSchematronIds(result, _group2_Name2_SchematronId2, _group4_Name2_SchematronId4);
    }

    @Test
    public void testExecListOne() throws Exception {
        ServiceContext context = createServiceContext();
        loginAsAdmin(context);

        Element params = createParams(
            read(SchematronCriteriaGroupService.PARAM_GROUP_NAME, _group2_Name2_SchematronId2.getId().getName()),
            read(SchematronCriteriaGroupService.PARAM_SCHEMATRON_ID, _group2_Name2_SchematronId2.getId().getSchematronId())
        );

        Element result = createService(LIST).exec(params, context);
        assertEquals(1, result.getChildren().size());

        assertGroupNames(result, _group2_Name2_SchematronId2);
        assertSchematronIds(result, _group2_Name2_SchematronId2);
        assertNotGroupNames(result, _group1_Name1_SchematronId1, _group3_Name3_SchemtronId1);
        assertNotSchematronIds(result, _group1_Name1_SchematronId1, _group4_Name2_SchematronId4);
    }

    @Test
    public void testExecExists() throws Exception {
        ServiceContext context = createServiceContext();
        loginAsAdmin(context);

        Element params = createParams(
            read(SchematronCriteriaGroupService.PARAM_GROUP_NAME, _group2_Name2_SchematronId2.getId().getName()),
            read(SchematronCriteriaGroupService.PARAM_SCHEMATRON_ID, _group2_Name2_SchematronId2.getId().getSchematronId())
        );

        Element result = createService(EXISTS).exec(params, context);
        assertEquals(Boolean.TRUE.toString(), result.getText());

        params = createParams(
            read(Params.ACTION, EXISTS),
            read(SchematronCriteriaGroupService.PARAM_GROUP_NAME, Integer.MAX_VALUE),
            read(SchematronCriteriaGroupService.PARAM_SCHEMATRON_ID, _group2_Name2_SchematronId2.getId().getSchematronId())
        );

        result = createService(EXISTS).exec(params, context);
        assertEquals(Boolean.FALSE.toString(), result.getText());

    }

    private SchematronCriteriaGroupService createService(SchematronServiceAction action) throws Exception {
        final SchematronCriteriaGroupService service = new SchematronCriteriaGroupService();
        super.init(service, action);
        return service;
    }

    @Test
    public void testExecDelete() throws Exception {
        ServiceContext context = createServiceContext();
        loginAsAdmin(context);

        Element deleteParams = createParams(
            read(SchematronCriteriaGroupService.PARAM_GROUP_NAME, _group1_Name1_SchematronId1.getId().getName()),
            read(SchematronCriteriaGroupService.PARAM_SCHEMATRON_ID, _group1_Name1_SchematronId1.getId().getSchematronId())
        );

        Element result = createService(DELETE).exec(deleteParams, context);
        assertEquals("ok", result.getName());

        Element listParams = createParams();

        result = createService(LIST).exec(listParams, context);
        assertEquals(3, result.getChildren().size());

        assertGroupNames(result, _group2_Name2_SchematronId2, _group3_Name3_SchemtronId1, _group4_Name2_SchematronId4);
        assertNotGroupNames(result, _group1_Name1_SchematronId1);
        assertSchematronIds(result, _group2_Name2_SchematronId2, _group3_Name3_SchemtronId1, _group4_Name2_SchematronId4);
    }

    @Test(expected = BadParameterEx.class)
    public void testExecDeleteBadGroupName() throws Exception {
        ServiceContext context = createServiceContext();
        loginAsAdmin(context);

        Element deleteParams = createParams(
            read(SchematronCriteriaGroupService.PARAM_GROUP_NAME, _group1_Name1_SchematronId1.getId().getName() + "asfasfd"),
            read(SchematronCriteriaGroupService.PARAM_SCHEMATRON_ID, _group1_Name1_SchematronId1.getId().getSchematronId())
        );

        createService(DELETE).exec(deleteParams, context);
    }

    @Test(expected = BadParameterEx.class)
    public void testExecDeleteBadSchematronId() throws Exception {
        ServiceContext context = createServiceContext();
        loginAsAdmin(context);

        Element deleteParams = createParams(
            read(SchematronCriteriaGroupService.PARAM_GROUP_NAME, _group1_Name1_SchematronId1.getId().getName()),
            read(SchematronCriteriaGroupService.PARAM_SCHEMATRON_ID, _group1_Name1_SchematronId1.getId().getSchematronId() + 1000)
        );

        createService(DELETE).exec(deleteParams, context);
    }

    @Test
    public void testExecAdd() throws Exception {
        ServiceContext context = createServiceContext();
        loginAsAdmin(context);

        final String newName = "NewName" + _inc.incrementAndGet();
        final String requirement = SchematronRequirement.REPORT_ONLY.name();
        final int schematronId = _group1_Name1_SchematronId1.getId().getSchematronId();
        Element deleteParams = createParams(
            read(SchematronCriteriaGroupService.PARAM_GROUP_NAME, newName),
            read(SchematronCriteriaGroupService.PARAM_SCHEMATRON_ID, schematronId),
            read(SchematronCriteriaGroupService.PARAM_REQUIREMENT, requirement)
        );

        Element result = createService(ADD).exec(deleteParams, context);
        assertSuccessfulAdd(result);

        Element listParams = createParams();

        result = createService(LIST).exec(listParams, context);
        assertEquals(5, result.getChildren().size());

        listParams = createParams(
            read(SchematronCriteriaGroupService.PARAM_GROUP_NAME, newName),
            read(SchematronCriteriaGroupService.PARAM_SCHEMATRON_ID, schematronId)
        );

        result = createService(LIST).exec(listParams, context);

        final Element idEl = result.getChild(GeonetEntity.RECORD_EL_NAME).getChild("id");
        assertEquals(newName, idEl.getChildText("name"));
        assertEquals("" + schematronId, idEl.getChildText("schematronid"));
        assertEquals(requirement, result.getChild(GeonetEntity.RECORD_EL_NAME).getChildText("requirement"));
    }

    @Test
    public void testExecEditRequirement() throws Exception {
        ServiceContext context = createServiceContext();
        loginAsAdmin(context);

        SchematronRequirement newRequirement = SchematronRequirement.DISABLED;
        if (_group1_Name1_SchematronId1.getRequirement() == SchematronRequirement.DISABLED) {
            newRequirement = SchematronRequirement.REQUIRED;
        }
        Element editParams = createParams(
            read(SchematronCriteriaGroupService.PARAM_GROUP_NAME, _group1_Name1_SchematronId1.getId().getName()),
            read(SchematronCriteriaGroupService.PARAM_SCHEMATRON_ID, _group1_Name1_SchematronId1.getId().getSchematronId()),
            read(SchematronCriteriaGroupService.PARAM_REQUIREMENT, newRequirement)
        );

        Element result = createService(EDIT).exec(editParams, context);
        assertEquals("ok", result.getName());

        Element listParams = createParams(
            read(SchematronCriteriaGroupService.PARAM_GROUP_NAME, _group1_Name1_SchematronId1.getId().getName()),
            read(SchematronCriteriaGroupService.PARAM_SCHEMATRON_ID, _group1_Name1_SchematronId1.getId().getSchematronId())
        );

        result = createService(LIST).exec(listParams, context);
        assertEquals(1, result.getChildren().size());

        assertEquals(newRequirement.name(), result.getChild(GeonetEntity.RECORD_EL_NAME).getChildText("requirement"));

    }

    @Test
    public void testExecEditName() throws Exception {
        ServiceContext context = createServiceContext();
        loginAsAdmin(context);


        final String newGroupName = "newGroupName";
        Element editParams = createParams(
            read(SchematronCriteriaGroupService.PARAM_GROUP_NAME, _group1_Name1_SchematronId1.getId().getName()),
            read(SchematronCriteriaGroupService.PARAM_SCHEMATRON_ID, _group1_Name1_SchematronId1.getId().getSchematronId()),
            read(SchematronCriteriaGroupService.PARAM_NEW_GROUP_NAME, newGroupName)

        );

        Element result = createService(EDIT).exec(editParams, context);
        assertEquals("ok", result.getName());

        Element listParamsOldName = createParams(
            read(SchematronCriteriaGroupService.PARAM_GROUP_NAME, _group1_Name1_SchematronId1.getId().getName()),
            read(SchematronCriteriaGroupService.PARAM_SCHEMATRON_ID, _group1_Name1_SchematronId1.getId().getSchematronId())
        );

        result = createService(EXISTS).exec(listParamsOldName, context);
        assertEquals("" + false, result.getText());

        Element listParamsNewName = createParams(
            read(SchematronCriteriaGroupService.PARAM_GROUP_NAME, newGroupName),
            read(SchematronCriteriaGroupService.PARAM_SCHEMATRON_ID, _group1_Name1_SchematronId1.getId().getSchematronId())
        );

        result = createService(LIST).exec(listParamsNewName, context);
        assertEquals(1, result.getChildren().size());

        assertEquals(_group1_Name1_SchematronId1.getRequirement().toString(), result.getChild("record").getChildText("requirement"));
        assertEquals(_group1_Name1_SchematronId1.getCriteria().size(), result.getChild("record").getChild("criteria").getChildren().size());
    }

    @Test
    public void testExecEditNameAndRequirement() throws Exception {
        ServiceContext context = createServiceContext();
        loginAsAdmin(context);


        SchematronRequirement newRequirement = SchematronRequirement.REPORT_ONLY;
        if (_group1_Name1_SchematronId1.getRequirement() == newRequirement) {
            newRequirement = SchematronRequirement.REQUIRED;
        }
        final String newGroupName = "newGroupName";
        Element editParams = createParams(
            read(SchematronCriteriaGroupService.PARAM_GROUP_NAME, _group1_Name1_SchematronId1.getId().getName()),
            read(SchematronCriteriaGroupService.PARAM_SCHEMATRON_ID, _group1_Name1_SchematronId1.getId().getSchematronId()),
            read(SchematronCriteriaGroupService.PARAM_NEW_GROUP_NAME, newGroupName),
            read(SchematronCriteriaGroupService.PARAM_REQUIREMENT, newRequirement)

        );

        Element result = createService(EDIT).exec(editParams, context);
        assertEquals("ok", result.getName());

        Element listParamsOldName = createParams(
            read(SchematronCriteriaGroupService.PARAM_GROUP_NAME, _group1_Name1_SchematronId1.getId().getName()),
            read(SchematronCriteriaGroupService.PARAM_SCHEMATRON_ID, _group1_Name1_SchematronId1.getId().getSchematronId())
        );

        result = createService(EXISTS).exec(listParamsOldName, context);
        assertEquals("" + false, result.getText());

        Element listParamsNewName = createParams(
            read(SchematronCriteriaGroupService.PARAM_GROUP_NAME, newGroupName),
            read(SchematronCriteriaGroupService.PARAM_SCHEMATRON_ID, _group1_Name1_SchematronId1.getId().getSchematronId())
        );

        result = createService(LIST).exec(listParamsNewName, context);
        assertEquals(1, result.getChildren().size());

        assertEquals(newRequirement.toString(), result.getChild(GeonetEntity.RECORD_EL_NAME).getChildText("requirement"));
        assertEquals(_group1_Name1_SchematronId1.getCriteria().size(), result.getChild(GeonetEntity.RECORD_EL_NAME).getChild("criteria").getChildren().size());
    }

    @Test
    public void testExecEditSchematronId() throws Exception {
        Schematron newSchematron = _schematronRepository.save(SchematronRepositoryTest.newSchematron(_inc));
        ServiceContext context = createServiceContext();
        loginAsAdmin(context);


        Element editParams = createParams(
            read(SchematronCriteriaGroupService.PARAM_GROUP_NAME, _group1_Name1_SchematronId1.getId().getName()),
            read(SchematronCriteriaGroupService.PARAM_SCHEMATRON_ID, _group1_Name1_SchematronId1.getId().getSchematronId()),
            read(SchematronCriteriaGroupService.PARAM_NEW_SCHEMATRON_ID, newSchematron.getId())

        );

        Element result = createService(EDIT).exec(editParams, context);
        assertEquals("ok", result.getName());

        Element listParamsOldName = createParams(
            read(SchematronCriteriaGroupService.PARAM_GROUP_NAME, _group1_Name1_SchematronId1.getId().getName()),
            read(SchematronCriteriaGroupService.PARAM_SCHEMATRON_ID, _group1_Name1_SchematronId1.getId().getSchematronId())
        );

        result = createService(EXISTS).exec(listParamsOldName, context);
        assertEquals("" + false, result.getText());

        Element listParamsNewName = createParams(
            read(SchematronCriteriaGroupService.PARAM_GROUP_NAME, _group1_Name1_SchematronId1.getId().getName()),
            read(SchematronCriteriaGroupService.PARAM_SCHEMATRON_ID, newSchematron.getId())
        );

        result = createService(LIST).exec(listParamsNewName, context);
        assertEquals(1, result.getChildren().size());

        assertEquals(_group1_Name1_SchematronId1.getRequirement().toString(), result.getChild(GeonetEntity.RECORD_EL_NAME).getChildText("requirement"));
    }

    private void assertCriteriaOnlyId(Element result) {
        assertEquals(1, result.getChild(GeonetEntity.RECORD_EL_NAME).getChildren("criteria").size());
        assertTrue(0 < result.getChild(GeonetEntity.RECORD_EL_NAME).getChild("criteria").getChildren().size());
        assertEquals(1, result.getChild(GeonetEntity.RECORD_EL_NAME).getChild("criteria").getChild("criteria").getChildren().size());
        assertNotNull(result.getChild(GeonetEntity.RECORD_EL_NAME).getChild("criteria").getChild("criteria").getChild("id"));
    }


    private void assertFullCriteria(Element result) {
        assertEquals(1, result.getChild(GeonetEntity.RECORD_EL_NAME).getChildren("criteria").size());
        assertTrue(0 < result.getChild(GeonetEntity.RECORD_EL_NAME).getChild("criteria").getChildren().size());
        assertNotNull(result.getChild(GeonetEntity.RECORD_EL_NAME).getChild("criteria").getChild("criteria").getChild("id"));
        assertNotNull(result.getChild(GeonetEntity.RECORD_EL_NAME).getChild("criteria").getChild("criteria").getChild("type"));
        assertNotNull(result.getChild(GeonetEntity.RECORD_EL_NAME).getChild("criteria").getChild("criteria").getChild("value"));
    }
}
