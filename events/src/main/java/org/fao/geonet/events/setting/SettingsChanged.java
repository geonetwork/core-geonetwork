/*
 * Copyright (C) 2001-2025 Food and Agriculture Organization of the
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
package org.fao.geonet.events.setting;

import java.util.List;
import org.fao.geonet.domain.Setting;
import org.springframework.context.ApplicationEvent;

public class SettingsChanged extends ApplicationEvent {
    private final List<Setting> newSettings;
    private final List<Setting> oldSettings;

    public SettingsChanged(List<Setting> newSettings, List<Setting> oldSettings) {
        super(newSettings);
        this.newSettings = newSettings;
        this.oldSettings = oldSettings;
    }

    public List<Setting> getOldSettings() {
        return oldSettings;
    }

    public List<Setting> getNewSettings() {
        return newSettings;
    }
}
