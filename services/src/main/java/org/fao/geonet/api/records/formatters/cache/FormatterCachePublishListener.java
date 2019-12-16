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

import org.fao.geonet.domain.MetadataType;
import org.fao.geonet.domain.OperationAllowed;
import org.fao.geonet.domain.ReservedGroup;
import org.fao.geonet.domain.ReservedOperation;
import org.fao.geonet.events.md.MetadataIndexCompleted;
import org.fao.geonet.repository.OperationAllowedRepository;
import org.fao.geonet.repository.specification.OperationAllowedSpecs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.EnableAsync;

import java.io.IOException;

/**
 * This class is responsible for listening for metadata index events and updating the cache's
 * publication values so that it stays in sync with the actual metadata.
 *
 * @author Jesse on 3/6/2015.
 */

@EnableAsync
public class FormatterCachePublishListener implements AsynchAfterCommitListener {
    @Autowired
    private FormatterCache formatterCache;

    @Autowired
    OperationAllowedRepository operationAllowedRepository;

    private static Logger LOGGER =  LoggerFactory.getLogger("geonetwork.formatter");

    private static final Specification<OperationAllowed> isPublished = OperationAllowedSpecs.isPublic(ReservedOperation.view);

    @Override
    public synchronized void onApplicationEvent(MetadataIndexCompleted event) {
        final int metadataId = event.getMd().getId();
        LOGGER.debug("Refreshing formatter cache for record '{}' [{}].", metadataId, Thread.currentThread());
        final OperationAllowed one = operationAllowedRepository.findOneById_GroupIdAndId_MetadataIdAndId_OperationId(ReservedGroup.all.getId(), metadataId, ReservedOperation.view.getId());
        try {
            boolean isPublic = one != null;
            formatterCache.setPublished(metadataId, event.getMd().getUuid(), isPublic);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void handleAsync(MetadataIndexCompleted event) {
        final int metadataId = event.getMd().getId();
        if (event.getMd().getDataInfo().getType() == MetadataType.METADATA
            && operationAllowedRepository.findOneById_GroupIdAndId_MetadataIdAndId_OperationId(
                ReservedGroup.all.getId(), metadataId, ReservedOperation.view.getId()
                ) != null) {
            LOGGER.debug("Refreshing landing page of public record '{}' [{}].", metadataId, Thread.currentThread());
            formatterCache.buildLandingPage(metadataId);
        }
    }

}
