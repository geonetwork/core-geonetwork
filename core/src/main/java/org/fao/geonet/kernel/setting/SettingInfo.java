//=============================================================================
//===	Copyright (C) 2001-2021 Food and Agriculture Organization of the
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

package org.fao.geonet.kernel.setting;

import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.constants.Geonet;

import static org.fao.geonet.kernel.setting.SettingManager.isPortRequired;

public class SettingInfo {

    public String getSiteName() {
        SettingManager settingManager = ApplicationContextHolder.get().getBean(SettingManager.class);

        return settingManager.getSiteName();
    }

    //---------------------------------------------------------------------------

    /**
     * Return a string like 'http://HOST[:PORT]'
     */
    public String getSiteUrl() {
        SettingManager settingManager = ApplicationContextHolder.get().getBean(SettingManager.class);

        String protocol = settingManager.getValue(Settings.SYSTEM_SERVER_PROTOCOL);
        Integer port;
        String host = settingManager.getValue(Settings.SYSTEM_SERVER_HOST);
        Integer configuredPort = toIntOrNull(Settings.SYSTEM_SERVER_PORT);
        if (configuredPort != null) {
            port = configuredPort;
        } else if (protocol.equalsIgnoreCase(Geonet.HttpProtocol.HTTPS)) {
            port = Geonet.DefaultHttpPort.HTTPS;
        } else {
            port = Geonet.DefaultHttpPort.HTTP;
        }

        StringBuffer sb = new StringBuffer(protocol + "://");

        sb.append(host);

		if (isPortRequired(protocol, port + "")) {
			sb.append(":");
			sb.append(port);
		}

        return sb.toString();
    }

    //---------------------------------------------------------------------------

    private Integer toIntOrNull(String key) {
        try {
            SettingManager settingManager = ApplicationContextHolder.get().getBean(SettingManager.class);
            return Integer.parseInt(settingManager.getValue(key));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public String getSelectionMaxRecords() {
        SettingManager settingManager = ApplicationContextHolder.get().getBean(SettingManager.class);
    String value = settingManager.getValue(Settings.SYSTEM_SELECTIONMANAGER_MAXRECORDS);
        if (value == null) value = "10000";
        return value;
    }

    public boolean isXLinkResolverEnabled() {
        SettingManager settingManager = ApplicationContextHolder.get().getBean(SettingManager.class);
        String value = settingManager.getValue(Settings.SYSTEM_XLINKRESOLVER_ENABLE);
        if (value == null) return false;
        else return value.equals("true");
    }

    public boolean isSearchStatsEnabled() {
        SettingManager settingManager = ApplicationContextHolder.get().getBean(SettingManager.class);
        String value = settingManager.getValue(Settings.SYSTEM_SEARCHSTATS);
        if (value == null) return false;
        else return value.equals("true");
    }

    public String getFeedbackEmail() {
        SettingManager settingManager = ApplicationContextHolder.get().getBean(SettingManager.class);
        return settingManager.getValue("system/feedback/email");
    }
}
