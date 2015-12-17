package org.fao.geonet.services.config;

import java.io.File;
import java.net.URL;

import org.apache.log4j.xml.DOMConfigurator;
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.domain.Setting;
import org.fao.geonet.exceptions.OperationAbortedEx;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.repository.SettingRepository;

/**
 * Logger utilities
 * @author bmaire
 *
 */
public class LogUtils {
    public static final String DEFAULT_LOG_FILE = "log4j.xml";

    /**
     * Refresh logger configuration.
     * If settings is not set in database, using default log4j.xml file.
     * If requested file does not exist, using default log4j.xml file.
     */
    public static void refreshLogConfiguration() {
        SettingRepository repository =
                ApplicationContextHolder.get().getBean(SettingRepository.class);
        Setting setting = repository.findOne(SettingManager.SYSTEM_SERVER_LOG);

        // if logPlaceHolder is defined, use it instead of the resources
        // defined in the classpath
        try {
            Object logov = ApplicationContextHolder.get().getBean("logPlaceHolder");
            File logFile = new File(logov.toString(), setting != null ? setting.getValue() : DEFAULT_LOG_FILE);
            DOMConfigurator.configure(logFile.toURI().toURL());
            return;
        } catch (Throwable e) {
            // ignore, and continue with the old behaviour
        }

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
