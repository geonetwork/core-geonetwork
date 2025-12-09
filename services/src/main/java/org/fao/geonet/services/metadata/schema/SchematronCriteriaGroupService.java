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

import jeeves.constants.Jeeves;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.Util;
import org.fao.geonet.domain.*;
import org.fao.geonet.exceptions.BadInputEx;
import org.fao.geonet.exceptions.BadParameterEx;
import org.fao.geonet.repository.SchematronCriteriaGroupRepository;
import org.fao.geonet.repository.SchematronRepository;
import org.fao.geonet.repository.Updater;
import org.fao.geonet.repository.specification.SchematronCriteriaGroupSpecs;
import org.jdom.Element;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.jpa.domain.Specification;

import jakarta.annotation.Nonnull;
import java.util.List;

/**
 * Load, edit, delete {@link org.fao.geonet.domain.SchematronCriteriaGroup} entities.
 * <p>
 * Created by Jesse on 2/7/14.
 */
public class SchematronCriteriaGroupService extends AbstractSchematronService {

    static final String PARAM_SCHEMATRON_ID = "schematronId";
    static final String PARAM_GROUP_NAME = "groupName";
    static final String PARAM_INCLUDE_CRITERIA = "includeCriteria";
    static final String PARAM_INCLUDE_SCHEMATRON = "includeSchematron";
    static final String PARAM_REQUIREMENT = "requirement";
    static final String PARAM_NEW_GROUP_NAME = "newGroupName";
    static final String PARAM_NEW_SCHEMATRON_ID = "newSchematronId";

    @Override
    protected Element delete(Element params, ServiceContext context) throws Exception {
        String groupName = Util.getParam(params, PARAM_GROUP_NAME);
        int schematronId = Integer.parseInt(Util.getParam(params, PARAM_SCHEMATRON_ID));

        final SchematronCriteriaGroupRepository repository = context.getBean(SchematronCriteriaGroupRepository.class);
        try {
            repository.deleteById(new SchematronCriteriaGroupId(groupName, schematronId));
        } catch (EmptyResultDataAccessException e) {
            if (!context.getBean(SchematronRepository.class).existsById(schematronId)) {
                throw new BadParameterEx(PARAM_SCHEMATRON_ID, "" + schematronId);
            } else {
                throw new BadParameterEx(PARAM_GROUP_NAME, groupName);

            }
        }
        return new Element("ok");
    }

    @Override
    protected Element add(Element params, ServiceContext context) throws Exception {
        String groupName = Util.getParam(params, PARAM_GROUP_NAME);
        Integer schematronId = Integer.parseInt(Util.getParam(params, PARAM_SCHEMATRON_ID));
        String requirement = Util.getParam(params, PARAM_REQUIREMENT, SchematronRequirement.REQUIRED.name());


        final SchematronCriteriaGroup group = new SchematronCriteriaGroup()
            .setId(new SchematronCriteriaGroupId(groupName, schematronId))
            .setRequirement(SchematronRequirement.valueOf(requirement.toUpperCase()));
        context.getBean(SchematronCriteriaGroupRepository.class).saveAndFlush(group);

        return new Element(Jeeves.Elem.RESPONSE).addContent(new Element("status").setText("success"));
    }

    @Override
    protected Element list(Element params, ServiceContext context) throws Exception {
        String groupName = Util.getParam(params, PARAM_GROUP_NAME, null);
        String schematronId = Util.getParam(params, PARAM_SCHEMATRON_ID, null);

        boolean includeCriteriaParamPresent = params.getChild(PARAM_INCLUDE_CRITERIA) != null;
        boolean includeCriteria = Util.getParam(params, PARAM_INCLUDE_CRITERIA, includeCriteriaParamPresent);
        boolean includeSchematronParamPresent = params.getChild(PARAM_INCLUDE_SCHEMATRON) != null;
        boolean includeSchematron = Util.getParam(params, PARAM_INCLUDE_SCHEMATRON, includeSchematronParamPresent);

        final SchematronCriteriaGroupRepository repository = context.getBean(SchematronCriteriaGroupRepository.class);
        Specification<SchematronCriteriaGroup> spec = null;

        if (schematronId != null) {
            spec = Specification.where(SchematronCriteriaGroupSpecs.hasSchematronId(Integer.parseInt(schematronId)));
        }

        if (groupName != null) {
            if (spec == null) {
                spec = Specification.where(SchematronCriteriaGroupSpecs.hasGroupName(groupName));
            } else {
                spec = spec.and(SchematronCriteriaGroupSpecs.hasGroupName(groupName));
            }
        }

        Element groups = repository.findAllAsXml(spec);

        @SuppressWarnings("unchecked") final List<Element> records = groups.getChildren(GeonetEntity.RECORD_EL_NAME);
        if (!includeCriteria || !includeSchematron) {
            for (Element child : records) {
                if (!includeCriteria) {
                    @SuppressWarnings("unchecked") final List<Element> criteria = child.getChild("criteria").getChildren();
                    for (Element element : criteria) {
                        element.setContent(element.getChild("id"));
                    }
                }
                if (!includeSchematron) {
                    child.removeChild("schematron");
                }
            }
        }

        return groups;
    }

    @Override
    protected boolean exists(Element params, ServiceContext context) throws Exception {
        String groupName = Util.getParam(params, PARAM_GROUP_NAME);
        int schematronId = Integer.parseInt(Util.getParam(params, PARAM_SCHEMATRON_ID));

        return context.getBean(SchematronCriteriaGroupRepository.class).existsById(new SchematronCriteriaGroupId(groupName, schematronId));
    }

    @Override
    protected Element edit(Element params, ServiceContext context) throws Exception {
        if (params.getChild(PARAM_NEW_GROUP_NAME) != null || params.getChild(PARAM_NEW_SCHEMATRON_ID) != null) {
            return renameGroup(params, context);
        } else {
            return updateRequirement(params, context);
        }

    }

    private Element updateRequirement(Element params, ServiceContext context) {
        final SchematronCriteriaGroupRepository repository = context.getBean(SchematronCriteriaGroupRepository.class);

        String groupName = Util.getParam(params, PARAM_GROUP_NAME);
        int schematronId = Integer.parseInt(Util.getParam(params, PARAM_SCHEMATRON_ID));
        final String requirement = Util.getParam(params, PARAM_REQUIREMENT, null);

        if (requirement != null) {
            repository.update(new SchematronCriteriaGroupId(groupName, schematronId), new Updater<SchematronCriteriaGroup>() {
                @Override
                public void apply(@Nonnull SchematronCriteriaGroup entity) {
                    entity.setRequirement(SchematronRequirement.valueOf(requirement.toUpperCase()));
                }
            });

            // Test that the new requirement is valid
            SchematronRequirement.valueOf(requirement.toUpperCase());

            return new Element("ok");
        }

        return new Element("NoUpdate");
    }

    private Element renameGroup(Element params, ServiceContext context) {
        final SchematronCriteriaGroupRepository repository = context.getBean(SchematronCriteriaGroupRepository.class);
        String groupName = Util.getParam(params, PARAM_GROUP_NAME);
        int schematronId = Integer.parseInt(Util.getParam(params, PARAM_SCHEMATRON_ID));
        String newRequirement = Util.getParam(params, PARAM_REQUIREMENT, null);
        final SchematronCriteriaGroup group = repository.findById(new SchematronCriteriaGroupId(groupName, schematronId)).get();

        SchematronRequirement finalRequirement = group.getRequirement();
        if (newRequirement != null) {
            finalRequirement = SchematronRequirement.valueOf(newRequirement.toUpperCase());
        }

        String newGroupName = Util.getParam(params, PARAM_NEW_GROUP_NAME, groupName);
        int newSchematronId = Util.getParam(params, PARAM_NEW_SCHEMATRON_ID, schematronId);

        SchematronCriteriaGroup newGroup = new SchematronCriteriaGroup().
            setId(new SchematronCriteriaGroupId(newGroupName, newSchematronId)).
            setRequirement(finalRequirement);
        for (SchematronCriteria schematronCriteria : group.getCriteria()) {
            newGroup.addCriteria(schematronCriteria.copy());
        }

        if (group.getId().equals(newGroup.getId())) {
            throw new BadInputEx(PARAM_NEW_GROUP_NAME + " and " + PARAM_NEW_SCHEMATRON_ID +
                " have the same value as the old values", newGroupName + ":" + newSchematronId) {
            };
        }

        repository.deleteById(group.getId());
        repository.saveAndFlush(newGroup);
        return new Element("ok");
    }
}
