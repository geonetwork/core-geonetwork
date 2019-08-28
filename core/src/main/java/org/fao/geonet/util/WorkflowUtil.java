//=============================================================================
//===
//=== ThreadUtils
//===
//=============================================================================
//===	Copyright (C) 2001-2007 Food and Agriculture Organization of the
//===	United Nations (FAO-UN), United Nations World Food Programme (WFP)
//===	and United Nations Environment Programme (UNEP)
//===
//===	This program is free software; you can redistribute it and/or modify
//===	it under the terms of the GNU General Public License as published by
//===	the Free Software Foundation; either version 2 of the License, or (at
//===	your option) any later version.
//===
//===	This program is distributed in the hope that it will be useful, but
//===	WITHOUT ANY WARRANTY; without even the implied warranty of
//===	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
//===	General Public License for more details.
//===
//===	You should have received a copy of the GNU General Public License
//===	along with this program; if not, write to the Free Software
//===	Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
//===
//===	Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
//===	Rome - Italy. email: geonetwork@osgeo.org
//=============================================================================

package org.fao.geonet.util;

import org.apache.commons.lang.StringUtils;
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.kernel.setting.Settings;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WorkflowUtil {

    /**
     * Checks if a group has the workflow enabled.
     *
     * @param groupName Group name
     * @return
     */
    public static boolean isGroupWithEnabledWorkflow(String groupName) {
        SettingManager settingManager = ApplicationContextHolder.get().getBean(SettingManager.class);

        String groupMatchingRegex = settingManager.getValue(Settings.METADATA_WORKFLOW_DRAFT_WHEN_IN_GROUP);

        if (!StringUtils.isEmpty(groupMatchingRegex)) {
            final Pattern pattern = Pattern.compile(groupMatchingRegex);
            final Matcher matcher = pattern.matcher(groupName);
            return matcher.find();
        } else {
            return false;
        }
    }



}
