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

package org.fao.geonet.api.records.formatters.cache;

import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.domain.OperationAllowed;
import org.fao.geonet.domain.ReservedOperation;
import org.fao.geonet.events.md.MetadataIndexCompleted;
import org.fao.geonet.repository.OperationAllowedRepository;
import org.fao.geonet.repository.specification.OperationAllowedSpecs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.data.jpa.domain.Specification;

import java.io.IOException;

import static org.springframework.data.jpa.domain.Specifications.where;

/**
 * This class is responsible for listening for metadata index events and updating the cache's
 * publication values so that it stays in sync with the actual metadata.
 *
 * @author Jesse on 3/6/2015.
 */
public class FormatterCachePublishListener implements ApplicationListener<MetadataIndexCompleted> {
    @Autowired
    private FormatterCache formatterCache;

    @Override
    public synchronized void onApplicationEvent(MetadataIndexCompleted event) {
        final int metadataId = event.getMd().getId();
        final Specification<OperationAllowed> isPublished = OperationAllowedSpecs.isPublic(ReservedOperation.view);
        final Specification<OperationAllowed> hasMdId = OperationAllowedSpecs.hasMetadataId(metadataId);
        final ConfigurableApplicationContext context = ApplicationContextHolder.get();
        final OperationAllowed one = context.getBean(OperationAllowedRepository.class).findOne(where(hasMdId).and(isPublished));
        try {
            formatterCache.setPublished(metadataId, one != null);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
