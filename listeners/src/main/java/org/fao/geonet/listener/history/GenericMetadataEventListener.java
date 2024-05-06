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
package org.fao.geonet.listener.history;

import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.ISODate;
import org.fao.geonet.domain.MetadataStatus;
import org.fao.geonet.domain.StatusValue;
import org.fao.geonet.events.history.AbstractHistoryEvent;
import org.fao.geonet.kernel.datamanager.IMetadataUtils;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.kernel.setting.Settings;
import org.fao.geonet.repository.MetadataStatusRepository;
import org.fao.geonet.repository.StatusValueRepository;
import org.fao.geonet.utils.Log;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class GenericMetadataEventListener {

    @Autowired
    private SettingManager settingManager;

    @Autowired
    private MetadataStatusRepository metadataStatusRepository;

    @Autowired
    private StatusValueRepository statusValueRepository;

    @Autowired
    IMetadataUtils metadataUtils;

    public abstract String getChangeMessage();

    public abstract String getEventType();

    /**
     * Event handler
     *
     * @param event
     */
    public final void handleEvent(AbstractHistoryEvent event) {

        storeContentHistoryEvent(event);
    }

    /**
     * Stores the event in the database
     *
     * @param event
     */
    public final void storeContentHistoryEvent(AbstractHistoryEvent event) {

        if(settingManager.getValueAsBool(Settings.METADATA_HISTORY_ENABLED)) {

            Integer metadataid = Math.toIntExact(event.getMdId());

            StatusValue status = statusValueRepository.findOneById(Integer.parseInt(getEventType()));

            if (status != null) {
                MetadataStatus metadataStatus = new MetadataStatus();
                metadataStatus.setMetadataId(metadataid);
                metadataStatus.setUserId(event.getUserId());
                metadataStatus.setChangeDate(new ISODate(System.currentTimeMillis()));
                metadataStatus.setStatusValue(status);
                metadataStatus.setOwner(event.getUserId());
                // Used the uuid if supplied otherwise try to look it up.
                if (event.getUuid() != null && event.getUuid().length() > 0) {
                    metadataStatus.setUuid(event.getUuid());
                } else {
                    try {
                        metadataStatus.setUuid(metadataUtils.getMetadataUuid(Integer.toString(metadataid)));
                    } catch (Exception e) {
                        Log.error(Geonet.DATA_MANAGER, String.format(
                                "Error locating uuid for metadata id: %d", + metadataid), e);
                    }
                    if (metadataStatus.getUuid() == null || metadataStatus.getUuid().length() == 0) {
                        Log.error(Geonet.DATA_MANAGER, String.format(
                                "Could not located uuid for metadata id: %d", +metadataid));
                    }
                }

                if (event.getTitles() != null && event.getTitles().size() > 0) {
                    metadataStatus.setTitles(event.getTitles());
                } else {
                    try {
                        metadataStatus.setTitles(metadataUtils.extractTitles(Integer.toString(metadataid)));
                    } catch (Exception e) {
                        Log.error(Geonet.DATA_MANAGER, String.format(
                                "Error locating titles for metadata id: %d", +metadataid), e);
                    }
                }

                metadataStatus.setChangeMessage(getChangeMessage());
                metadataStatus.setCurrentState(event.getCurrentState());
                metadataStatus.setPreviousState(event.getPreviousState());
                metadataStatus.setRelatedMetadataStatus(event.getRelatedMetadataStatus());

                metadataStatusRepository.save(metadataStatus);
            } else {
                Log.warning(Geonet.DATA_MANAGER, String.format(
                        "Status with id '%s' not found in database. Check database migration SQL file to add default status if you want to log record history.",
                        getEventType()));
            }
        }
    }

}
