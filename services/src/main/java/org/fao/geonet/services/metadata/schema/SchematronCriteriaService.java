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
import org.fao.geonet.constants.Params;
import org.fao.geonet.domain.*;
import org.fao.geonet.exceptions.BadParameterEx;
import org.fao.geonet.repository.SchematronCriteriaGroupRepository;
import org.fao.geonet.repository.SchematronCriteriaRepository;
import org.fao.geonet.repository.Updater;
import org.fao.geonet.repository.specification.SchematronCriteriaSpecs;
import org.jdom.Element;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.domain.Specification;

import jakarta.annotation.Nonnull;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * Load, edit, delete {@link org.fao.geonet.domain.SchematronCriteria} entities.
 * <p>
 * Created by Jesse on 2/7/14.
 */
public class SchematronCriteriaService extends AbstractSchematronService {

    public static final String PARAM_GROUP_NAME = "groupName";
    public static final String PARAM_INCLUDE_GROUP = "includeGroup";
    public static final String PARAM_SCHEMATRON_ID = "schematronId";
    public static final String PARAM_TYPE = "type";
    public static final String PARAM_VALUE = "value";
    public static final String PARAM_UI_TYPE = "uitype";
    public static final String PARAM_UI_VALUE = "uivalue";

    @Override
    protected Element add(Element params, ServiceContext context) throws Exception {
        String groupName = Util.getParam(params, SchematronCriteriaGroupService.PARAM_GROUP_NAME);
        int schematronId = Integer.parseInt(Util.getParam(params, SchematronCriteriaGroupService.PARAM_SCHEMATRON_ID));

        final SchematronCriteriaGroupRepository criteriaGroupRepository = context.getBean(SchematronCriteriaGroupRepository.class);

        final SchematronCriteriaType type = SchematronCriteriaType
            .valueOf(Util.getParam(params, PARAM_TYPE));
        final String value = Util.getParam(params, PARAM_VALUE, "");
        final String uitype = Util.getParam(params, PARAM_UI_TYPE);
        final String uivalue = Util.getParam(params, PARAM_UI_VALUE, "");

        final SchematronCriteriaGroupId id = new SchematronCriteriaGroupId(groupName, schematronId);
        Optional<SchematronCriteriaGroup> group = criteriaGroupRepository.findById(id);
        SchematronCriteriaGroup schematronCriteriaGroup = null;
        if (!group.isPresent()) {
            schematronCriteriaGroup = new SchematronCriteriaGroup();
            schematronCriteriaGroup.setId(id);
            schematronCriteriaGroup.setRequirement(SchematronRequirement.REQUIRED);
        } else {
            schematronCriteriaGroup =group.get();
        }

        SchematronCriteria criteria = new SchematronCriteria();
        criteria.setType(type);
        criteria.setValue(value);
        criteria.setUiType(uitype);
        criteria.setUiValue(uivalue);
        schematronCriteriaGroup.addCriteria(criteria);

        schematronCriteriaGroup = criteriaGroupRepository.saveAndFlush(schematronCriteriaGroup);
        SchematronCriteria savedCriteria = schematronCriteriaGroup.getCriteria().get(schematronCriteriaGroup.getCriteria().size() - 1);

        Element result = new Element(Jeeves.Elem.RESPONSE);
        result.addContent(new Element("status").setText("success"));
        result.addContent(new Element("id").setText("" + savedCriteria.getId()));
        result.addContent(new Element("groupname").setText(schematronCriteriaGroup.getId().getName()));
        result.addContent(new Element("schematronid").setText("" + schematronCriteriaGroup.getId().getSchematronId()));

        return result;
    }

    @Override
    protected Element list(Element params, ServiceContext context) throws Exception {
        String groupName = Util.getParam(params, PARAM_GROUP_NAME, null);
        String schematronId = Util.getParam(params, PARAM_SCHEMATRON_ID, null);
        boolean includeGroup = Util.getParam(params, PARAM_INCLUDE_GROUP, false);
        String id = Util.getParam(params, Params.ID, null);

        if (id != null && schematronId != null && groupName != null) {
            throw new IllegalArgumentException("Either one of " + Params.ID + " cannot be present if either " + PARAM_GROUP_NAME + " or " + PARAM_SCHEMATRON_ID + " are also present.");
        }

        final SchematronCriteriaRepository criteriaRepository = context.getBean(SchematronCriteriaRepository.class);
        final Element element;

        if (id == null) {
            Specification spec = null;
            if (schematronId != null) {
                spec = Specification.where(SchematronCriteriaSpecs.hasSchematronId(Integer.parseInt(schematronId)));
            }

            if (groupName != null) {
                final Specification<SchematronCriteria> hasGroupSpec = SchematronCriteriaSpecs.hasGroupName(groupName);
                if (spec == null) {
                    spec = Specification.where(hasGroupSpec);
                } else {
                    spec = spec.and(hasGroupSpec);
                }
            }
            element = criteriaRepository.findAllAsXml(spec);
        } else {
            final SchematronCriteria criteria = criteriaRepository.findById(Integer.parseInt(id)).get();

            if (criteria == null) {
                throw new BadParameterEx(Params.ID, id);
            }
            element = new Element(Jeeves.Elem.RESPONSE).addContent(criteria.asXml());
        }

        @SuppressWarnings("unchecked") final List<Element> criteriaRecords = element.getChildren();
        if (!includeGroup) {
            for (Element criteriaRecord : criteriaRecords) {
                final Element groupEl = criteriaRecord.getChild("group");
                Element idEl = groupEl.getChild("id");
                idEl.detach();
                groupEl.setContent(idEl);
            }
        } else {
            for (Element criteriaRecord : criteriaRecords) {
                @SuppressWarnings("unchecked")
                List<Element> groups = criteriaRecord.getChildren("group");
                for (Element group : groups) {
                    group.removeChild("criteria");
                    group.removeChild("schematron");
                }
            }

        }
        return element;
    }

    @Override
    protected boolean exists(Element params, ServiceContext context) throws Exception {
        final Integer id = Integer.valueOf(Util.getParam(params, Params.ID));
        return context.getBean(SchematronCriteriaRepository.class).existsById(id);
    }

    @Override
    protected Element edit(Element params, ServiceContext context) throws Exception {
        int id = Integer.parseInt(Util.getParam(params, Params.ID));
        final String type = Util.getParam(params, PARAM_TYPE, null);
        final String value = Util.getParam(params, PARAM_VALUE, null);
        final String uiType = Util.getParam(params, PARAM_UI_TYPE, null);
        final String uiValue = Util.getParam(params, PARAM_UI_VALUE, null);


        final SchematronCriteriaRepository criteriaRepository = context.getBean(SchematronCriteriaRepository.class);
        criteriaRepository.update(id, new Updater<SchematronCriteria>() {
            @Override
            public void apply(@Nonnull SchematronCriteria entity) {
                if (type != null) {
                    entity.setType(SchematronCriteriaType.valueOf(type.toUpperCase()));
                }

                if (value != null) {
                    entity.setValue(value);
                }

                if (uiType != null) {
                    entity.setUiType(uiType);
                }

                if (uiValue != null) {
                    entity.setUiValue(uiValue);
                }
            }
        });

        return new Element("ok");
    }

    @Override
    protected Element delete(Element params, ServiceContext context) throws Exception {
        final Integer id = Integer.valueOf(Util.getParam(params, Params.ID));

        final SchematronCriteriaRepository criteriaRepository = context.getBean(SchematronCriteriaRepository.class);
        Optional<SchematronCriteria> criteria = criteriaRepository.findById(id);
        if (criteria.isPresent()) {
            criteria.get().getGroup().getCriteria().remove(criteria.get());
            criteriaRepository.deleteById(id);
            if (!criteriaRepository.existsById(id)) {
                return new Element("ok");
            } else {
                throw new IOException("Error deleting criteria object");
            }
        }

        throw new BadParameterEx(Params.ID, id);
    }
}
