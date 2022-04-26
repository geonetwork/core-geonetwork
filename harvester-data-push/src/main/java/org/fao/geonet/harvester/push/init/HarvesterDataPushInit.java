//=============================================================================
//===	Copyright (C) 2001-2022 Food and Agriculture Organization of the
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
//==============================================================================
package org.fao.geonet.harvester.push.init;

import org.fao.geonet.domain.Setting;
import org.fao.geonet.domain.SettingDataType;
import org.fao.geonet.events.server.ServerStartup;
import org.fao.geonet.harvester.push.tasks.HarvesterDataTask;
import org.fao.geonet.repository.SettingRepository;
import org.fao.geonet.repository.specification.SettingSpec;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;

import javax.annotation.PostConstruct;
import java.util.Optional;

public class HarvesterDataPushInit  implements
    ApplicationListener<ServerStartup> {

    @Autowired
    private SettingRepository _settingRepository;

    @PostConstruct
    public void init() {
    }

    private void addSetting(String key, SettingDataType type, String value, int position ) {
        if (!_settingRepository.findOne(SettingSpec.nameStartsWith(key)).isPresent()) {
            Setting setting = new Setting();
            setting.setDataType(type);
            setting.setName(key);
            setting.setPosition(position);
            setting.setValue(value);
            _settingRepository.save(setting);
        }
    }

    private void addSettings() {
        int position = 9013; // default position
        Optional<Setting> setting = _settingRepository.findOne(SettingSpec.nameStartsWith("system/harvester/remoteHarvesterApiUrl"));

        if (setting.isPresent()) {
            position = setting.get().getPosition()+1;
        }

        addSetting(HarvesterDataTask.SYSTEM_HARVESTER_SYNCH_TOOLS_PATH,
            SettingDataType.STRING,"",position);
    }

    //when the server starts up, we are ready to
    @Override
    public void onApplicationEvent(ServerStartup event) {
        addSettings();
    }
}
