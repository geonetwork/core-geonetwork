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
import org.fao.geonet.domain.Setting;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Handler for database events to manage encrypt/decrypt of encrypted settings.
 *
 */
public class SettingValueSetter implements GeonetworkEntityListener<Setting> {
    @Autowired
    private StandardPBEStringEncryptor encryptor;

    @Override
    public Class<Setting> getEntityClass() {
        return Setting.class;
    }

    @Override
    public void handleEvent(final PersistentEventType type, final Setting entity) {
        if ((type == PersistentEventType.PrePersist)) {
            if (entity.isEncrypted() && StringUtils.isNotEmpty(entity.getValue())) {
                entity.setStoredValue(this.encryptor.encrypt(entity.getValue()));
            } else {
                entity.setStoredValue(entity.getValue());
            }
        } else if (type == PersistentEventType.PreUpdate) {
            if (entity.isEncrypted() && StringUtils.isNotEmpty(entity.getValue())) {
                entity.setStoredValue(this.encryptor.encrypt(entity.getValue()));
            }

        } else if ((type == PersistentEventType.PostLoad) ||  (type == PersistentEventType.PostUpdate)) {
            if (entity.isEncrypted() && StringUtils.isNotEmpty(entity.getStoredValue())) {
                try {
                    entity.setValue(this.encryptor.decrypt(entity.getStoredValue()));
                } catch (Exception e) {
                    entity.setValue("");
                }
            } else {
                entity.setValue(entity.getStoredValue());
            }
        }
    }
}
