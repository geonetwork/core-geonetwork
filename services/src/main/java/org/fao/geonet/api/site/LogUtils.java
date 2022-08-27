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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.status.StatusLogger;
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.domain.Setting;
import org.fao.geonet.exceptions.OperationAbortedEx;
import org.fao.geonet.kernel.setting.Settings;
import org.fao.geonet.repository.SettingRepository;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Optional;

/**
 * Logger utilities.
 *
 * <p>To troubleshoot use {@code org.apache.logging.log4j.simplelog.StatusLogger.level=DEBUG}, or
 * adjust {@code log4j2.xml} file &lt;Configuration status=&quote;trace&quote;&gt;.
 *
 * @author bmaire
 */
public class LogUtils {
    /**
     * Default built-in log4j2 configuration.
     */
    static final String DEFAULT_LOG_FILE = "log4j2.xml";

    /**
     * Refresh logger configuration. If settings is not set in database, using default log4j2.xml
     * file. If requested file does not exist, using default log4j2.xml file.
     *
     * As a side effect of this activity the a {@code Console} and {@code File} appender
     * may be configured. Jeeves {@code Log} uses this information to determine logfile location
     * (and set up harvesters with their own logfile).
     *
     * The {@code LoggingAPI} service, and {@code LogConfig} service, also make use of this information
     * to retrieve {@code geonetwork.log} file contents.
     */
    public static void refreshLogConfiguration() {
        final StatusLogger CONFIG_LOG = StatusLogger.getLogger();

        SettingRepository repository =
            ApplicationContextHolder.get().getBean(SettingRepository.class);
        Optional<Setting> settingOpt = repository.findById(Settings.SYSTEM_SERVER_LOG);
        Setting setting = null;

        if (settingOpt.isPresent()) {
            setting = settingOpt.get();
        }

        // get log config from db settings
        String log4jProp = setting != null ? setting.getValue() : DEFAULT_LOG_FILE;
        URL url = LogUtils.class.getResource("/" + log4jProp);
        try {
            if (url != null) {
                // refresh configuration
                if (log4jProp.startsWith("log4j2")){
                    // current context, no need to enforce autoclosable
                    final LoggerContext loggerContext = (LoggerContext) LogManager.getContext(false);

                    CONFIG_LOG.info("Load log4j2 config from '"+url+"'");
                    loggerContext.setConfigLocation(url.toURI());
                }
                else {
                    CONFIG_LOG.info("Load log4j config from '"+url+"'");
                    DOMConfigurator.configure(url);
                }
            } else {
                CONFIG_LOG.info("Unable  to load '"+log4jProp+"', using '"+DEFAULT_LOG_FILE+"'");
                DOMConfigurator.configure(LogUtils.class.getResource("/" + DEFAULT_LOG_FILE));
                throw new OperationAbortedEx("Can't refresh log configuration because file '" +
                    log4jProp + "' doesn't exist. Using log4j.xml.");
            }
        } catch (URISyntaxException | RuntimeException unsuccessful) {
            CONFIG_LOG.info("Unable  to load '"+log4jProp+"', using '"+DEFAULT_LOG_FILE+"'");
            DOMConfigurator.configure(LogUtils.class.getResource("/" + DEFAULT_LOG_FILE));
            throw new OperationAbortedEx("Can't refresh log configuration '" +
                log4jProp + "' due to issue with configuration: "+unsuccessful.getMessage(),unsuccessful);
        }
    }
}
