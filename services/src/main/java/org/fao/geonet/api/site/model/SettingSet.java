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

package org.fao.geonet.api.site.model;

/**
 * Created by francois on 03/06/16.
 */
public enum SettingSet {
    INSPIRE(new String[]{
        "system/inspire/enableSearchPanel",
        "system/inspire/enable"
    }),
    HARVESTER(new String[]{
        "system/harvester/enableEditing"
    }),
    USER_GROUP_ONLY(
        new String[]{"system/metadataprivs/usergrouponly"}),
    AUTH,READ_ONLY,INDEX,SYSTEMINFO,STAGING_PROFILE,TYPE;

    private String[] listOfSettings;

    public String[] getListOfSettings() {
        return listOfSettings;
    }

    SettingSet() {
    }

    SettingSet(String[] listOfSettings) {
        this.listOfSettings = listOfSettings;
    }

    static boolean find(String value) {
        for (SettingSet v : values()) {
            if (v.toString().equalsIgnoreCase(value)) {
                return true;
            }
        }
        return false;
    }
}
