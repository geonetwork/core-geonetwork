package org.fao.geonet.kernel;

import org.fao.geonet.AbstractCoreIntegrationTest;
import org.fao.geonet.kernel.setting.SettingManager;
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
        this.settingManager.setValue(SettingManager.ENABLE_ALL_THESAURUS, false);
        int count = this.thesaurusManager.getThesauriMap().size();
        assertFalse(this.thesaurusManager.getThesauriMap().containsKey(AllThesaurus.ALL_THESAURUS_KEY));

        this.settingManager.setValue(SettingManager.ENABLE_ALL_THESAURUS, true);
        assertTrue(this.thesaurusManager.getThesauriMap().containsKey(AllThesaurus.ALL_THESAURUS_KEY));
        assertEquals(count + 1, this.thesaurusManager.getThesauriMap().size());


        this.settingManager.setValue(SettingManager.ENABLE_ALL_THESAURUS, false);
        assertFalse(this.thesaurusManager.getThesauriMap().containsKey(AllThesaurus.ALL_THESAURUS_KEY));
        assertEquals(count, this.thesaurusManager.getThesauriMap().size());
    }

    @Test
    public void testGetThesaurusByName() throws Exception {
        this.settingManager.setValue(SettingManager.ENABLE_ALL_THESAURUS, false);
        assertNull(this.thesaurusManager.getThesaurusByName(AllThesaurus.ALL_THESAURUS_KEY));

        this.settingManager.setValue(SettingManager.ENABLE_ALL_THESAURUS, true);
        assertNotNull(this.thesaurusManager.getThesaurusByName(AllThesaurus.ALL_THESAURUS_KEY));

        this.settingManager.setValue(SettingManager.ENABLE_ALL_THESAURUS, false);
        assertNull(this.thesaurusManager.getThesaurusByName(AllThesaurus.ALL_THESAURUS_KEY));
    }

    @Test
    public void testExistsThesaurus() throws Exception {
        this.settingManager.setValue(SettingManager.ENABLE_ALL_THESAURUS, false);
        assertFalse(this.thesaurusManager.existsThesaurus(AllThesaurus.ALL_THESAURUS_KEY));

        this.settingManager.setValue(SettingManager.ENABLE_ALL_THESAURUS, true);
        assertTrue(this.thesaurusManager.existsThesaurus(AllThesaurus.ALL_THESAURUS_KEY));

        this.settingManager.setValue(SettingManager.ENABLE_ALL_THESAURUS, false);
        assertFalse(this.thesaurusManager.existsThesaurus(AllThesaurus.ALL_THESAURUS_KEY));
    }
    @Test
    public void testBuildResultfromThTable() throws Exception {
        this.settingManager.setValue(SettingManager.ENABLE_ALL_THESAURUS, false);
        final int numThesaurus = this.thesaurusManager.getThesauriMap().size();
        Element element = this.thesaurusManager.buildResultfromThTable(createServiceContext());
        assertEquals(numThesaurus, Xml.selectNodes(element, "thesaurus").size());

        this.settingManager.setValue(SettingManager.ENABLE_ALL_THESAURUS, true);
        element = this.thesaurusManager.buildResultfromThTable(createServiceContext());
        assertEquals(numThesaurus + 1, Xml.selectNodes(element, "thesaurus").size());

        this.settingManager.setValue(SettingManager.ENABLE_ALL_THESAURUS, false);
        element = this.thesaurusManager.buildResultfromThTable(createServiceContext());
        assertEquals(numThesaurus, Xml.selectNodes(element, "thesaurus").size());
    }
}