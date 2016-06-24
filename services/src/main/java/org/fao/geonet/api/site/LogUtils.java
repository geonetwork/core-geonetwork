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

package org.fao.geonet.api.site;

import org.apache.log4j.xml.DOMConfigurator;
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.domain.Setting;
import org.fao.geonet.exceptions.OperationAbortedEx;
import org.fao.geonet.kernel.setting.Settings;
import org.fao.geonet.repository.SettingRepository;
import org.fao.geonet.services.config.DoActions;

import java.net.URL;

/**
 * Logger utilities
 *
 * @author bmaire
 */
public class LogUtils {
    public static final String DEFAULT_LOG_FILE = "log4j.xml";

    /**
     * Refresh logger configuration. If settings is not set in database, using default log4j.xml
     * file. If requested file does not exist, using default log4j.xml file.
     */
    public static void refreshLogConfiguration() {
        SettingRepository repository =
            ApplicationContextHolder.get().getBean(SettingRepository.class);
        Setting setting = repository.findOne(Settings.SYSTEM_SERVER_LOG);

        // get log config from db settings
        String log4jProp = setting != null ? setting.getValue() : DEFAULT_LOG_FILE;
        URL url = DoActions.class.getResource("/" + log4jProp);
        if (url != null) {
            // refresh configuration
            DOMConfigurator.configure(url);
        } else {
            DOMConfigurator.configure(
                LogUtils.class.getResource("/" + DEFAULT_LOG_FILE));
            throw new OperationAbortedEx("Can't refresh log configuration because file '" +
                log4jProp + "' doesn't exist. Using log4j.xml.");
        }
    }
}
