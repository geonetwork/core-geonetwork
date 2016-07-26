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

package org.fao.geonet.kernel;

import org.fao.geonet.AbstractCoreIntegrationTest;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.kernel.setting.Settings;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class ThesaurusManagerTest extends AbstractCoreIntegrationTest {

    @Autowired
    private ThesaurusManager thesaurusManager;
    @Autowired
    private SettingManager settingManager;

    @Test
    public void testGetThesauriMap() throws Exception {
        this.settingManager.setValue(Settings.SYSTEM_ENABLE_ALL_THESAURUS, false);
        int count = this.thesaurusManager.getThesauriMap().size();
        assertFalse(this.thesaurusManager.getThesauriMap().containsKey(AllThesaurus.ALL_THESAURUS_KEY));

        this.settingManager.setValue(Settings.SYSTEM_ENABLE_ALL_THESAURUS, true);
        assertTrue(this.thesaurusManager.getThesauriMap().containsKey(AllThesaurus.ALL_THESAURUS_KEY));
        assertEquals(count + 1, this.thesaurusManager.getThesauriMap().size());


        this.settingManager.setValue(Settings.SYSTEM_ENABLE_ALL_THESAURUS, false);
        assertFalse(this.thesaurusManager.getThesauriMap().containsKey(AllThesaurus.ALL_THESAURUS_KEY));
        assertEquals(count, this.thesaurusManager.getThesauriMap().size());
    }

    @Test
    public void testGetThesaurusByName() throws Exception {
        this.settingManager.setValue(Settings.SYSTEM_ENABLE_ALL_THESAURUS, false);
        assertNull(this.thesaurusManager.getThesaurusByName(AllThesaurus.ALL_THESAURUS_KEY));

        this.settingManager.setValue(Settings.SYSTEM_ENABLE_ALL_THESAURUS, true);
        assertNotNull(this.thesaurusManager.getThesaurusByName(AllThesaurus.ALL_THESAURUS_KEY));

        this.settingManager.setValue(Settings.SYSTEM_ENABLE_ALL_THESAURUS, false);
        assertNull(this.thesaurusManager.getThesaurusByName(AllThesaurus.ALL_THESAURUS_KEY));
    }

    @Test
    public void testExistsThesaurus() throws Exception {
        this.settingManager.setValue(Settings.SYSTEM_ENABLE_ALL_THESAURUS, false);
        assertFalse(this.thesaurusManager.existsThesaurus(AllThesaurus.ALL_THESAURUS_KEY));

        this.settingManager.setValue(Settings.SYSTEM_ENABLE_ALL_THESAURUS, true);
        assertTrue(this.thesaurusManager.existsThesaurus(AllThesaurus.ALL_THESAURUS_KEY));

        this.settingManager.setValue(Settings.SYSTEM_ENABLE_ALL_THESAURUS, false);
        assertFalse(this.thesaurusManager.existsThesaurus(AllThesaurus.ALL_THESAURUS_KEY));
    }

    @Test
    public void testBuildResultfromThTable() throws Exception {
        this.settingManager.setValue(Settings.SYSTEM_ENABLE_ALL_THESAURUS, false);
        final int numThesaurus = this.thesaurusManager.getThesauriMap().size();
        Element element = this.thesaurusManager.buildResultfromThTable(createServiceContext());
        assertEquals(numThesaurus, Xml.selectNodes(element, "thesaurus").size());

        this.settingManager.setValue(Settings.SYSTEM_ENABLE_ALL_THESAURUS, true);
        element = this.thesaurusManager.buildResultfromThTable(createServiceContext());
        assertEquals(numThesaurus + 1, Xml.selectNodes(element, "thesaurus").size());

        this.settingManager.setValue(Settings.SYSTEM_ENABLE_ALL_THESAURUS, false);
        element = this.thesaurusManager.buildResultfromThTable(createServiceContext());
        assertEquals(numThesaurus, Xml.selectNodes(element, "thesaurus").size());
    }
}
