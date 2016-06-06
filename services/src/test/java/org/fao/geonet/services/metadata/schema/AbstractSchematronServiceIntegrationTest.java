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

import com.google.common.base.Function;
import com.google.common.collect.Lists;

import jeeves.interfaces.Service;
import jeeves.server.ServiceConfig;

import org.fao.geonet.Constants;
import org.fao.geonet.constants.Params;
import org.fao.geonet.domain.SchematronCriteriaGroup;
import org.fao.geonet.repository.SchematronCriteriaGroupRepository;
import org.fao.geonet.repository.SchematronCriteriaRepository;
import org.fao.geonet.repository.SchematronRepository;
import org.fao.geonet.services.AbstractServiceIntegrationTest;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Text;
import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nullable;

import java.util.Arrays;
import java.util.List;

import static org.fao.geonet.repository.SchematronCriteriaGroupRepositoryTest.newGroup;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by Jesse on 2/13/14.
 */
public abstract class AbstractSchematronServiceIntegrationTest extends AbstractServiceIntegrationTest {
    protected SchematronCriteriaGroup _group1_Name1_SchematronId1;
    protected SchematronCriteriaGroup _group2_Name2_SchematronId2;
    protected SchematronCriteriaGroup _group3_Name3_SchemtronId1;
    protected SchematronCriteriaGroup _group4_Name2_SchematronId4;
    @Autowired
    SchematronRepository _schematronRepository;
    @Autowired
    SchematronCriteriaRepository _schematronCriteriaRepository;
    @Autowired
    private SchematronCriteriaGroupRepository _schematronCriteriaGroupRepository;

    @Before
    public void addTestData() {
        _schematronCriteriaGroupRepository.deleteAll();
        _schematronRepository.deleteAll();
        assertEquals(0, _schematronCriteriaRepository.count());
        this._group1_Name1_SchematronId1 = _schematronCriteriaGroupRepository.save(newGroup(_inc, _schematronRepository));
        this._group2_Name2_SchematronId2 = _schematronCriteriaGroupRepository.save(newGroup(_inc, _schematronRepository));
        final SchematronCriteriaGroup entity = newGroup(_inc, _schematronRepository);
        entity.setSchematron(_group1_Name1_SchematronId1.getSchematron());
        this._group3_Name3_SchemtronId1 = _schematronCriteriaGroupRepository.save(entity);
        final SchematronCriteriaGroup entity2 = newGroup(_inc, _schematronRepository);
        entity2.getId().setName(_group2_Name2_SchematronId2.getId().getName());
        this._group4_Name2_SchematronId4 = _schematronCriteriaGroupRepository.save(entity2);
    }

    protected void assertGroupNames(Element result, SchematronCriteriaGroup... groups) throws JDOMException {
        final List<?> groupNames = selectGroupNames(result);
        assertEquals(groups.length, groupNames.size());
        for (SchematronCriteriaGroup group : groups) {
            assertTrue(groupNames.contains(group.getId().getName()));
        }
    }

    protected void assertSchematronIds(Element result, SchematronCriteriaGroup... groups) throws JDOMException {
        final List<?> schematronIds = selectSchematronIds(result);
        assertEquals(groups.length, schematronIds.size());
        for (SchematronCriteriaGroup group : groups) {
            assertTrue(schematronIds.contains("" + group.getId().getSchematronId()));
        }
    }

    private List<?> selectSchematronIds(Element result) throws JDOMException {
        return Lists.transform(Xml.selectNodes(result, "record/id/schematronid/text()"), new Function<Object,
            Object>() {
            @Nullable
            @Override
            public Object apply(@Nullable Object input) {
                return ((Text) input).getText();
            }
        });
    }

    protected void assertNotGroupNames(Element result, SchematronCriteriaGroup... groups) throws JDOMException {
        final List<?> groupNames = selectGroupNames(result);
        for (SchematronCriteriaGroup group : groups) {
            assertFalse(groupNames.contains(group.getId().getName()));
        }
    }

    private List<?> selectGroupNames(Element result) throws JDOMException {
        return Lists.transform(Xml.selectNodes(result, "record/id/name/text()"), new Function<Object, Object>() {
            @Nullable
            @Override
            public Object apply(@Nullable Object input) {
                return ((Text) input).getText();
            }
        });
    }

    protected void assertNotSchematronIds(Element result, SchematronCriteriaGroup... groups) throws JDOMException {
        final List<?> schematronIds = selectSchematronIds(result);
        for (SchematronCriteriaGroup group : groups) {
            assertFalse(schematronIds.contains("" + group.getId().getSchematronId()));
        }
    }

    protected void assertSuccessfulAdd(Element result) {
        assertEquals("success", result.getChildText("status"));
    }

    public void init(AbstractSchematronService service, SchematronServiceAction action) throws Exception {
        ServiceConfig params = new ServiceConfig(Arrays.asList(createServiceConfigParam(Params.ACTION, action.toString())));
        service.init(getWebappDir(AbstractSchematronServiceIntegrationTest.class), params);
    }
}
