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

package org.fao.geonet.entitylistener;

import org.apache.commons.lang.StringUtils;
import org.fao.geonet.domain.Setting;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * JPA entity listener that keeps {@link Setting#getValue()} (transient) and
 * {@link Setting#getStoredValue()} (persisted column) in sync, handling encryption
 * transparently.
 *
 * <p>The {@code value} field is transient and not managed by Hibernate, so this listener
 * bridges the gap between the in-memory representation and the database column:</p>
 * <ul>
 *   <li><b>PrePersist</b>: copies {@code value} → {@code storedValue}, encrypting if needed.
 *       Skipped when {@code value} is null/empty to preserve any {@code storedValue} set
 *       directly via {@link Setting#setStoredValue(String)}.</li>
 *   <li><b>PreUpdate</b>: re-encrypts {@code value} into {@code storedValue} for encrypted
 *       settings only. Non-encrypted settings are not handled here because
 *       {@link Setting#setValue(String)} already calls {@link Setting#setStoredValue(String)}
 *       as a side effect, which is what triggers Hibernate's dirty detection on the
 *       persistent field.</li>
 *   <li><b>PostLoad / PostUpdate</b>: populates the transient {@code value} from
 *       {@code storedValue}, decrypting if needed.</li>
 * </ul>
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
            if (entity.isEncrypted()) {
                if (StringUtils.isNotEmpty(entity.getValue())) {
                    entity.setStoredValue(this.encryptor.encrypt(entity.getValue()));
                } else if (StringUtils.isNotEmpty(entity.getStoredValue())) {
                    entity.setStoredValue(this.encryptor.encrypt(entity.getStoredValue()));
                }
            } else if (StringUtils.isNotEmpty(entity.getValue())) {
                entity.setStoredValue(entity.getValue());
            }
        } else if (type == PersistentEventType.PreUpdate) {
            if (entity.isEncrypted() && StringUtils.isNotEmpty(entity.getValue())) {
                entity.setStoredValue(this.encryptor.encrypt(entity.getValue()));
            }

        } else if ((type == PersistentEventType.PostPersist) || (type == PersistentEventType.PostLoad) || (type == PersistentEventType.PostUpdate)) {
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
