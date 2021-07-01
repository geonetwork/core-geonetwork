/*
 * Copyright (C) 2001-2021 Food and Agriculture Organization of the
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

package org.fao.geonet.entitylistener;

import org.apache.commons.lang.StringUtils;
import org.fao.geonet.domain.HarvesterSetting;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.springframework.beans.factory.annotation.Autowired;


/**
 * Handler for database events to manage encrypt/decrypt of encrypted harvester settings.
 *
 */
public class HarvesterSettingValueSetter implements GeonetworkEntityListener<HarvesterSetting> {
    @Autowired
    private StandardPBEStringEncryptor encryptor;

    @Override
    public Class<HarvesterSetting> getEntityClass() {
        return HarvesterSetting.class;
    }

    @Override
    public void handleEvent(final PersistentEventType type, final HarvesterSetting entity) {
        if (type == PersistentEventType.PrePersist) {
            if (entity.isEncrypted() && StringUtils.isNotEmpty(entity.getValue())) {
                entity.setStoredValue(this.encryptor.encrypt(entity.getValue()));
            } else {
                entity.setStoredValue(entity.getValue());
            }

        } else if (type == PersistentEventType.PreUpdate) {
            // PreUpdate should deal with storedValue, at least during the startup of the
            // application, the transient value is cleared in PreUpdate and storedValue has the
            // correct value.
            if (entity.isEncrypted() && StringUtils.isNotEmpty(entity.getStoredValue())) {
                entity.setStoredValue(this.encryptor.encrypt(entity.getStoredValue()));
            }

        } else if ((type == PersistentEventType.PostLoad) ||  (type == PersistentEventType.PostUpdate)) {
            if (entity.isEncrypted() && StringUtils.isNotEmpty(entity.getStoredValue())) {
                entity.setValue(this.encryptor.decrypt(entity.getStoredValue()));
            } else {
                entity.setValue(entity.getStoredValue());
            }
        }
    }
}
