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
package org.fao.geonet.listeners.history;

import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.domain.AbstractMetadata;
import org.fao.geonet.domain.ISODate;
import org.fao.geonet.domain.MetadataStatus;
import org.fao.geonet.domain.MetadataStatusId;
import org.fao.geonet.domain.StatusValue;
import org.fao.geonet.events.history.create.MetadataHistoryEvent;
import org.fao.geonet.events.md.MetadataEvent;
import org.fao.geonet.repository.MetadataStatusRepository;
import org.fao.geonet.repository.StatusValueRepository;

public abstract class GenericMetadataEventListner  {

    public abstract String getEventType();
    public abstract String getChangeMessage();

    public void handleEvent(MetadataHistoryEvent event) {

        MetadataStatusRepository statusRepository = ApplicationContextHolder.get().getBean(MetadataStatusRepository.class);
        StatusValueRepository statusValueRepository = ApplicationContextHolder.get().getBean(StatusValueRepository.class);

        storeEvent(event, statusRepository, statusValueRepository);
    }
    
    public void storeEvent(MetadataHistoryEvent event, MetadataStatusRepository statusRepository, StatusValueRepository statusValueRepository) {        
               
        AbstractMetadata metadata = event.getMd();
        MetadataStatusId metadataStatusId = new MetadataStatusId()
                .setMetadataId(metadata.getId())
                .setStatusId(Integer.parseInt(getEventType()))
                .setUserId(event.getUserId())
                .setChangeDate(new ISODate(System.currentTimeMillis()));

        StatusValue status = statusValueRepository.findOneById(Integer.parseInt(getEventType()));

        MetadataStatus metadataStatus = new MetadataStatus();
        metadataStatus.setId(metadataStatusId);
        metadataStatus.setStatusValue(status);
        metadataStatus.setOwner(metadata.getSourceInfo().getOwner());
        metadataStatus.setChangeMessage(getChangeMessage());

        statusRepository.save(metadataStatus);
    }



}
