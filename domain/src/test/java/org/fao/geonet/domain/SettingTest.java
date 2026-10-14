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

package org.fao.geonet.domain;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SettingTest {

    @Test
    public void testGetSetName() {
        Setting setting = new Setting();
        String name = "system/site/name";
        setting.setName(name);
        assertEquals(name, setting.getName());
    }

    @Test
    public void testGetSetValue() {
        Setting setting = new Setting();
        String value = "GeoNetwork";
        setting.setValue(value);
        assertEquals(value, setting.getValue());
        assertEquals(value, setting.getStoredValue());
    }

    @Test
    public void testGetSetStoredValue() {
        Setting setting = new Setting();
        String storedValue = "encryptedValue";
        setting.setStoredValue(storedValue);
        assertEquals(storedValue, setting.getStoredValue());
    }

    @Test
    public void testGetSetDataType() {
        Setting setting = new Setting();
        assertEquals(SettingDataType.STRING, setting.getDataType());
        setting.setDataType(SettingDataType.INT);
        assertEquals(SettingDataType.INT, setting.getDataType());
    }

    @Test
    public void testGetSetPosition() {
        Setting setting = new Setting();
        assertEquals(0, setting.getPosition());
        setting.setPosition(10);
        assertEquals(10, setting.getPosition());
    }

    @Test
    public void testGetSetInternal() {
        Setting setting = new Setting();
        assertTrue(setting.isInternal());
        assertEquals(Constants.YN_TRUE, setting.getInternal_JpaWorkaround());

        setting.setInternal(false);
        assertFalse(setting.isInternal());
        assertEquals(Constants.YN_FALSE, setting.getInternal_JpaWorkaround());

        setting.setInternal(true);
        assertTrue(setting.isInternal());
        assertEquals(Constants.YN_TRUE, setting.getInternal_JpaWorkaround());
    }

    @Test
    public void testGetSetEncrypted() {
        Setting setting = new Setting();
        assertFalse(setting.isEncrypted());
        assertEquals(Constants.YN_FALSE, setting.getEncrypted_JpaWorkaround());

        setting.setEncrypted(true);
        assertTrue(setting.isEncrypted());
        assertEquals(Constants.YN_TRUE, setting.getEncrypted_JpaWorkaround());

        setting.setEncrypted(false);
        assertFalse(setting.isEncrypted());
        assertEquals(Constants.YN_FALSE, setting.getEncrypted_JpaWorkaround());
    }

    @Test
    public void testGetSetEditable() {
        Setting setting = new Setting();
        assertTrue(setting.isEditable());
        assertEquals(Constants.YN_TRUE, setting.getEditable_JpaWorkaround());

        setting.setEditable(false);
        assertFalse(setting.isEditable());
        assertEquals(Constants.YN_FALSE, setting.getEditable_JpaWorkaround());

        setting.setEditable(true);
        assertTrue(setting.isEditable());
        assertEquals(Constants.YN_TRUE, setting.getEditable_JpaWorkaround());
    }

    @Test
    public void testToString() {
        Setting setting = new Setting();
        setting.setName("name").setValue("value");
        assertEquals("Setting{'name' = 'value'}", setting.toString());
    }

    @Test
    public void testCreateDeepCopy() {
        Setting setting = new Setting();
        setting.setName("name")
                .setValue("5")
                .setDataType(SettingDataType.INT)
                .setPosition(5)
                .setInternal(false)
                .setEncrypted(true)
                .setEditable(false);

        Setting copy = Setting.createDeepCopy(setting);

        assertEquals(setting.getName(), copy.getName());
        assertEquals(setting.getValue(), copy.getValue());
        assertEquals(setting.getStoredValue(), copy.getStoredValue());
        assertEquals(setting.getDataType(), copy.getDataType());
        assertEquals(setting.getPosition(), copy.getPosition());
        assertEquals(setting.isInternal(), copy.isInternal());
        // assertEquals(setting.isEncrypted(), copy.isEncrypted()); // isEncrypted is not serialized
        assertEquals(setting.isEditable(), copy.isEditable());

        assertFalse(setting == copy);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateDeepCopyInvalidData() {
        Setting setting = new Setting();
        setting.setName("name")
                .setValue("not-an-int")
                .setDataType(SettingDataType.INT);

        Setting.createDeepCopy(setting);
    }
}
