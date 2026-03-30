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
package org.fao.geonet.inspireatom.listener.setting;

import java.util.List;
import java.util.Optional;
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.Setting;
import org.fao.geonet.events.setting.SettingsChanged;
import org.fao.geonet.inspireatom.harvester.InspireAtomHarvesterScheduler;
import org.fao.geonet.kernel.setting.Settings;
import org.fao.geonet.utils.Log;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
public class InspireAtomSettingsChangedListener implements ApplicationListener<SettingsChanged> {
    @Override
    public void onApplicationEvent(SettingsChanged event) {
        String oldSettingInspireScheduleValue = value(event.getOldSettings(), Settings.SYSTEM_INSPIRE_ATOM_SCHEDULE);
        String newSettingInspireScheduleValue = value(event.getNewSettings(), Settings.SYSTEM_INSPIRE_ATOM_SCHEDULE);

        String newSettingInspireEnabled = value(event.getNewSettings(), Settings.SYSTEM_INSPIRE_ENABLE);

        // INSPIRE setting enabled
        if (newSettingInspireEnabled.equalsIgnoreCase("true")) {
            String oldSettingInspireTypeValue = value(event.getOldSettings(), Settings.SYSTEM_INSPIRE_ATOM);
            String newSettingInspireTypeValue = value(event.getNewSettings(), Settings.SYSTEM_INSPIRE_ATOM);

            // INSPIRE Atom type changed
            if (!oldSettingInspireTypeValue.equalsIgnoreCase(newSettingInspireTypeValue)) {

                // If the value is "remote", we enable the harvester with the new schedule.
                if (newSettingInspireTypeValue.equalsIgnoreCase("remote")) {
                    enableInspireAtomHarvester(newSettingInspireScheduleValue);
                } else {
                    disableInspireAtomHarvester();
                }
            } else {
                // If the type is still "remote", we check if the schedule has changed to reschedule the harvester.
                if (newSettingInspireTypeValue.equalsIgnoreCase("remote")
                    && !oldSettingInspireScheduleValue.equalsIgnoreCase(newSettingInspireScheduleValue)) {
                    enableInspireAtomHarvester(newSettingInspireScheduleValue);
                }
            }
        } else {
            // INSPIRE setting disabled
            disableInspireAtomHarvester();
        }
    }

    private void enableInspireAtomHarvester(String schedule) {
        try {
            GeonetContext gnContext = new GeonetContext(ApplicationContextHolder.get(), false);
            InspireAtomHarvesterScheduler.schedule(schedule, null, gnContext);
        } catch (Exception e) {
            Log.error(Geonet.ATOM, "Error enabling INSPIRE Atom Harvester with schedule: " + schedule, e);
        }
    }

    private void disableInspireAtomHarvester() {
        try {
            InspireAtomHarvesterScheduler.unSchedule();
        } catch (Exception e) {
            Log.error(Geonet.ATOM, "Error disabling INSPIRE Atom Harvester: ", e);
        }
    }

    /**
     * Returns the value of a setting by its name from a list of settings.
     *
     * @param settings    List of settings to search in.
     * @param settingName Setting name to look for.
     * @return The value of the setting if found, otherwise an empty string.
     */
    private String value(List<Setting> settings, String settingName) {
        Optional<Setting> settingOptional = settings.stream().filter(setting -> setting.getName().equalsIgnoreCase(settingName)).findFirst();

        if (settingOptional.isPresent()) {
            return settingOptional.get().getValue();
        } else {
            return "";
        }

    }
}
