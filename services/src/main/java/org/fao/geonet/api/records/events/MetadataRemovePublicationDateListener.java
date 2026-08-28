/*
 * Copyright (C) 2001-2026 Food and Agriculture Organization of the
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

package org.fao.geonet.api.records.events;

import org.fao.geonet.domain.AbstractMetadata;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.domain.MetadataType;
import org.fao.geonet.events.md.MetadataUnpublished;
import org.fao.geonet.kernel.datamanager.IMetadataUtils;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.kernel.setting.Settings;
import org.fao.geonet.utils.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;

public class MetadataRemovePublicationDateListener implements ApplicationListener<MetadataUnpublished> {

    @Autowired
    private MetadataUpdatePublicationDateService metadataUpdatePublicationDateService;

    @Autowired
    private SettingManager settingManager;

    @Autowired
    private IMetadataUtils metadataUtils;

    @Override
    public void onApplicationEvent(MetadataUnpublished event) {
        if (!settingManager.getValueAsBool(Settings.SYSTEM_METADATAPRIVS_PUBLICATION_MANAGEPUBLICATIONDATE)) {
            return;
        }

        AbstractMetadata md = event.getMd();
        // On republication (approval workflow), the metadata received in the event is the draft
        // version. The publication date must be removed from the approved record, not the draft.
        if (!(md instanceof Metadata)) {
            md = metadataUtils.findOneByUuid(md.getUuid());
        }
        if (md == null || md.getDataInfo().getType() != MetadataType.METADATA)  {
            return;
        }

        try {
            metadataUpdatePublicationDateService.removePublicationDate(md);
        } catch (Exception e) {
            Log.error("org.fao.geonet.services.metadata", "Error removing publication date of metadata " + md.getId(), e);
        }
    }

}

