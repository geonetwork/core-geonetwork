/*
 * Copyright (C) 2001-2023 Food and Agriculture Organization of the
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

package org.fao.geonet.util;

import org.fao.geonet.AbstractCoreIntegrationTest;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.kernel.setting.Settings;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.Assert.*;

public class WorkflowUtilTest extends AbstractCoreIntegrationTest {
    @Autowired
    SettingManager settingManager;

    @Test
    public void testWorkflowDisabled() {
        settingManager.setValue(Settings.METADATA_WORKFLOW_ENABLE, false);
        assertFalse(WorkflowUtil.isGroupWithEnabledWorkflow("sample"));
    }

    @Test
    public void testWorkflowDisabledAndEnabledAllGroups() {
        settingManager.setValue(Settings.METADATA_WORKFLOW_ENABLE, false);
        settingManager.setValue(Settings.METADATA_WORKFLOW_DRAFT_WHEN_IN_GROUP, ".*");
        assertFalse(WorkflowUtil.isGroupWithEnabledWorkflow("sample"));
    }

    @Test
    public void testWorkflowDisabledAndEnabledInGroupList() {
        settingManager.setValue(Settings.METADATA_WORKFLOW_ENABLE, false);
        settingManager.setValue(Settings.METADATA_WORKFLOW_DRAFT_WHEN_IN_GROUP, "sample|test");
        assertFalse(WorkflowUtil.isGroupWithEnabledWorkflow("sample"));

        settingManager.setValue(Settings.METADATA_WORKFLOW_DRAFT_WHEN_IN_GROUP, "sam*|test");
        assertFalse(WorkflowUtil.isGroupWithEnabledWorkflow("sample"));
    }

    @Test
    public void testWorkflowEnabledAllGroups() {
        settingManager.setValue(Settings.METADATA_WORKFLOW_ENABLE, true);
        settingManager.setValue(Settings.METADATA_WORKFLOW_DRAFT_WHEN_IN_GROUP, ".*");
        assertTrue(WorkflowUtil.isGroupWithEnabledWorkflow("sample"));
    }

    @Test
    public void testWorkflowEnabledInGroupList() {
        settingManager.setValue(Settings.METADATA_WORKFLOW_ENABLE, true);
        settingManager.setValue(Settings.METADATA_WORKFLOW_DRAFT_WHEN_IN_GROUP, "sample|test");
        assertTrue(WorkflowUtil.isGroupWithEnabledWorkflow("sample"));

        settingManager.setValue(Settings.METADATA_WORKFLOW_DRAFT_WHEN_IN_GROUP, "sam*|test");
        assertTrue(WorkflowUtil.isGroupWithEnabledWorkflow("sample"));
    }

    @Test
    public void testWorkflowEnabledNotInGroupList() {
        settingManager.setValue(Settings.METADATA_WORKFLOW_ENABLE, true);
        settingManager.setValue(Settings.METADATA_WORKFLOW_DRAFT_WHEN_IN_GROUP, "test");
        assertFalse(WorkflowUtil.isGroupWithEnabledWorkflow("sample"));
    }
}
