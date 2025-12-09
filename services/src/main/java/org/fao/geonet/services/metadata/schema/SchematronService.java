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

import jeeves.server.context.ServiceContext;
import org.fao.geonet.Util;
import org.fao.geonet.constants.Params;
import org.fao.geonet.domain.Schematron;
import org.fao.geonet.domain.Schematron_;
import org.fao.geonet.exceptions.BadParameterEx;
import org.fao.geonet.repository.SchematronRepository;
import org.fao.geonet.repository.SortUtils;
import org.fao.geonet.repository.Updater;
import org.jdom.Element;

import jakarta.annotation.Nonnull;

/**
 * Load, edit, delete {@link org.fao.geonet.domain.Schematron} entities.
 * <p>
 * Created by Jesse on 2/7/14.
 */
public class SchematronService extends AbstractSchematronService {

    static final String PARAM_DISPLAY_PRIORITY = "displaypriority";

    @Override
    protected Element list(Element params, ServiceContext context) throws Exception {
        String id = Util.getParam(params, Params.ID, null);
        final SchematronRepository repository = context.getBean(SchematronRepository.class);

        Element result;
        if (id == null) {
            result = repository.findAllAsXml(SortUtils.createSort(Schematron_.displayPriority));
        } else {
            final Schematron one = repository.findById(Integer.parseInt(id)).get();
            if (one == null) {
                throw new BadParameterEx(Params.ID, id);
            }
            result = new Element("schematron").addContent(one.asXml());
        }

        result.setName("schematron");
        return result;
    }

    @Override
    protected boolean exists(Element params, ServiceContext context) throws Exception {
        String id = Util.getParam(params, Params.ID);
        return context.getBean(SchematronRepository.class).existsById(Integer.parseInt(id));
    }

    @Override
    protected Element edit(Element params, ServiceContext context) throws Exception {
        String id = Util.getParam(params, Params.ID);
        final int displayPriority = Integer.parseInt(Util.getParam(params, PARAM_DISPLAY_PRIORITY));

        context.getBean(SchematronRepository.class).update(Integer.parseInt(id), new Updater<Schematron>() {
            @Override
            public void apply(@Nonnull Schematron entity) {
                entity.setDisplayPriority(displayPriority);
            }
        });

        return new Element("ok");
    }

    @Override
    protected Element delete(Element params, ServiceContext context) throws Exception {
        throw new UnsupportedOperationException("Cannot yet delete existing schematrons");
    }

    @Override
    protected Element add(Element params, ServiceContext context) throws Exception {
        throw new UnsupportedOperationException("Cannot yet add new schematrons");
    }
}
