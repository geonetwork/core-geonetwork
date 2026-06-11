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

package org.fao.geonet.repository;

import org.fao.geonet.domain.Setting;
import org.fao.geonet.domain.SettingDataType;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import javax.persistence.EntityManager;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Test {@link org.fao.geonet.repository.SettingRepository}
 */
public class SettingRepositoryTest extends AbstractSpringDataTest {
    @Autowired
    private EntityManager em;

    @Autowired
    private SettingRepository repo;

    @Test
    public void testCreateASetting() {
        Setting setting = newSetting("test", SettingDataType.STRING, "testValue", 1, false, false);
        save(setting);

        Optional<Setting> savedSetting = repo.findById("test");

        assertTrue(savedSetting.isPresent());
        checkSettingValues(setting, savedSetting.get());
    }

    @Test
    public void testCreateASettingEncrypted() {
        Setting setting = newSetting("test", SettingDataType.STRING, "testValue", 1, false, true);
        save(setting);

        Optional<Setting> savedSetting = repo.findById("test");
        assertTrue(savedSetting.isPresent());

        checkSettingValues(setting, savedSetting.get());
    }

    @Test
    public void testUpdateASetting() {
        Setting setting = newSetting("test", SettingDataType.STRING, "testValue", 1, false, false);
        save(setting);

        Optional<Setting> savedSettingOpt = repo.findById("test");
        assertTrue(savedSettingOpt.isPresent());

        Setting savedSetting = savedSettingOpt.get();
        savedSetting.setValue("newValue");
        save(savedSetting);

        Optional<Setting> savedSettingUpdated = repo.findById("test");
        assertTrue(savedSettingUpdated.isPresent());

        checkSettingValues(savedSetting, savedSettingUpdated.get());
    }

    @Test
    public void testUpdateASettingToEmptyValue() {
        Setting setting = newSetting("test", SettingDataType.STRING, "testValue", 1, false, false);
        save(setting);

        Optional<Setting> savedSettingOpt = repo.findById("test");
        assertTrue(savedSettingOpt.isPresent());

        Setting savedSetting = savedSettingOpt.get();
        savedSetting.setValue("");
        save(savedSetting);

        Optional<Setting> savedSettingUpdated = repo.findById("test");
        assertTrue(savedSettingUpdated.isPresent());

        checkSettingValues(savedSetting, savedSettingUpdated.get());
    }

    @Test
    public void testUpdateASettingEncryptedValue() {
        Setting setting = newSetting("test", SettingDataType.STRING, "testValue", 1, false, true);
        save(setting);

        Optional<Setting> savedSettingOpt = repo.findById("test");
        assertTrue(savedSettingOpt.isPresent());

        Setting savedSetting = savedSettingOpt.get();
        savedSetting.setValue("newValue");
        save(savedSetting);

        Optional<Setting> savedSettingUpdated = repo.findById("test");
        assertTrue(savedSettingUpdated.isPresent());

        checkSettingValues(savedSetting, savedSettingUpdated.get());
    }

    @Test
    public void testUpdateASettingEncryptedEmptyValue() {
        Setting setting = newSetting("test", SettingDataType.STRING, "testValue", 1, false, true);
        save(setting);

        Optional<Setting> savedSettingOpt = repo.findById("test");
        assertTrue(savedSettingOpt.isPresent());

        Setting savedSetting = savedSettingOpt.get();
        savedSetting.setValue("");
        save(savedSetting);

        Optional<Setting> savedSettingUpdated = repo.findById("test");
        assertTrue(savedSettingUpdated.isPresent());

        checkSettingValues(savedSetting, savedSettingUpdated.get());
    }

    @Test
    public void testCreateASettingUsingStoredValueDirectly() {
        Setting setting = new Setting();
        setting.setDataType(SettingDataType.STRING);
        setting.setName("test");
        setting.setPosition(1);
        setting.setInternal(false);
        // Set storedValue directly without going through setValue(), leaving the transient
        // value field null. Before the fix, PrePersist would overwrite storedValue with
        // null because it entered the else branch unconditionally.
        setting.setStoredValue("directValue");
        save(setting);

        Optional<Setting> savedSetting = repo.findById("test");

        assertTrue(savedSetting.isPresent());
        assertEquals("directValue", savedSetting.get().getValue());
    }


    private Setting newSetting(String key, SettingDataType type, String value,
                               int position, boolean internal, boolean encrypted) {
        Setting setting = new Setting();
        setting.setDataType(type);
        setting.setName(key);
        setting.setPosition(position);
        setting.setValue(value);
        setting.setInternal(internal);
        setting.setEncrypted(encrypted);

        return setting;
    }

    private void save(Setting setting) {
        repo.save(setting);
        // Ensures that the data is persisted in the database
        em.flush();
        // Empties 1st level cache, so find method retrieves the data from the database and listener PostLoad event is triggered
        em.clear();
    }

    private void checkSettingValues(Setting expected, Setting actual) {
        assertEquals(expected.getName(), actual.getName());
        assertEquals(expected.getValue(), actual.getValue());
        assertEquals(expected.getDataType(), actual.getDataType());
        assertEquals(expected.isInternal(), actual.isInternal());
        assertEquals(expected.isEncrypted(), actual.isEncrypted());
    }

}
