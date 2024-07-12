//=============================================================================
//===	Copyright (C) 2001-2005 Food and Agriculture Organization of the
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
//===	Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
//===
//===	Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
//===	Rome - Italy. email: geonetwork@osgeo.org
//==============================================================================

package org.fao.geonet.utils;


import org.apache.log4j.bridge.AppenderWrapper;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.FileAppender;
import org.apache.logging.log4j.core.appender.RollingFileAppender;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;

import java.io.File;

/**
 * Jeeves logging integration, defining functional logger categories by module
 * (rather that strictly based on java package structure).
 */
public final class Log {

    /**
     * Jeeves base logging moodule.
     */
    public static final String JEEVES = "jeeves";

    //---------------------------------------------------------------------------
    //--- Logging constants
    //---------------------------------------------------------------------------
    /**
     * Jeeves engine
     */
    public static final String ENGINE = JEEVES + ".engine";
    /**
     * Jeeves monitor
     */
    public static final String MONITOR = JEEVES + ".monitor";
    /**
     * Jeeves application handler
     */
    public static final String APPHAND = JEEVES + ".apphand";
    /**
     * Jeeves web application
     */
    public static final String WEBAPP = JEEVES + ".webapp";
    /**
     * Jeeves request handling
     */
    public static final String REQUEST = JEEVES + ".request";
    /**
     * Jeeves service
     */
    public static final String SERVICE = JEEVES + ".service";

    /**
     * Jeeves scheduler, used for {@code ScheduleContext}.
     */
    public static final String SCHEDULER = JEEVES + ".scheduler";

    /**
     * Jeeves resources
     */
    public static final String RESOURCES = JEEVES + ".resources";

    /**
     * Jeeves xlink processor
     */
    public static final String XLINK_PROCESSOR = JEEVES + ".xlinkprocessor";

    /**
     * Jeeves xml resolver
     */
    public static final String XML_RESOLVER = JEEVES + ".xmlresolver";
    /**
     * Jeeves transformer factory
     */
    public static final String TRANSFORMER_FACTORY = JEEVES
        + ".transformerFactory";

    /**
     * This is the name of the RollingFileAppender in your log4j2.xml configuration file.
     * <p>
     * LogConfig uses this name to lookup RollingFileAppender to check configuration in
     * case a custom log file location has been used.
     */
    private static final String FILE_APPENDER_NAME = "File";

    public static final String GEONETWORK_MODULE = "geonetwork";

    /**
     * Default constructor. Builds a Log.
     */
    private Log() {
    }

    public static void debug(String module, Object message) {
        LogManager.getLogger(module).debug(message);
    }

    public static void debug(String module, String message, Object... objects) {
        LogManager.getLogger(module).debug(message, objects);
    }

    public static void debug(String module, String message, Throwable throwable) {
        LogManager.getLogger(module).debug(message, throwable);
    }

    public static boolean isDebugEnabled(String module) {
        return LogManager.getLogger(module).isDebugEnabled();
    }

    @SuppressWarnings("deprecation")
    public static boolean isEnabledFor(String module, Level level) {
        return LogManager.getLogger(module).isEnabled(level);
    }
    //---------------------------------------------------------------------------

    public static void trace(String module, Object message) {
        LogManager.getLogger(module).trace(message);
    }

    public static void trace(String module, Object message, Exception e) {
        LogManager.getLogger(module).trace(message, e);
    }

    public static boolean isTraceEnabled(String module) {
        return LogManager.getLogger(module).isTraceEnabled();
    }

    //---------------------------------------------------------------------------

    public static void info(String module, Object message) {
        LogManager.getLogger(module).info(message);
    }

    public static void info(String module, String message, Object... objects) {
        LogManager.getLogger(module).info(message, objects);
    }

    public static void info(String module, String message, Throwable throwable) {
        LogManager.getLogger(module).info(message, throwable);
    }


    //---------------------------------------------------------------------------

    public static void warning(String module, Object message) {
        LogManager.getLogger(module).warn(message);
    }

    public static void warning(String module, Object message, Throwable e) {
        LogManager.getLogger(module).warn(message, e);
    }


    //---------------------------------------------------------------------------

    public static void error(String module, Object message) {
        LogManager.getLogger(module).error(message);
    }

    public static void error(String module, Object message, Throwable t) {
        LogManager.getLogger(module).error(message, t);
    }

    public static void error(String module, String message, Object... objects) {
        LogManager.getLogger(module).error(message, objects);
    }

    public static void error(String module, String message, Throwable throwable) {
        LogManager.getLogger(module).error(message, throwable);
    }

    //---------------------------------------------------------------------------

    public static void fatal(String module, Object message) {
        LogManager.getLogger(module).fatal(message);
    }

    //--------------------------------------------------------------------------

    /**
     * Logger wrapper providing module logging services.
     * <p>
     * The provided {@code fallbackModule} is used to indicate parent module to
     * assist in determing log file location.
     *
     * @param module
     * @return logger providing module logging services.
     */
    public static org.fao.geonet.Logger createLogger(final String module) {
        return createLogger(module, null);
    }

    /**
     * Logger wrapper providing module logging services.
     * <p>
     * The provided {@code fallbackModule} is used to indicate parent module to
     * assist in determing log file location.
     *
     * @param module
     * @param fallbackModule
     * @return logger providing module logging services.
     */
    public static org.fao.geonet.Logger createLogger(final String module,
                                                     final String fallbackModule) {
        return new org.fao.geonet.Logger() {

            public boolean isDebugEnabled() {
                return Log.isDebugEnabled(module);
            }

            public void debug(String message) {
                Log.debug(module, message);
            }

            @Override
            public void debug(String message, Throwable throwable) {
                Log.debug(module, message, throwable);
            }

            @Override
            public void debug(String message, Object... object) {
                Log.debug(module, message, object);
            }

            public void info(String message) {
                Log.info(module, message);
            }

            @Override
            public void info(String message, Throwable throwable) {
                Log.info(module, message, throwable);
            }

            @Override
            public void info(String message, Object... object) {
                Log.info(module, message, object);
            }

            public void warning(String message) {
                Log.warning(module, message);
            }

            @Override
            public void warning(String message, Throwable throwable) {
                Log.warning(module, message, throwable);
            }

            @Override
            public void warning(String message, Object... object) {

            }

            public void error(String message) {
                Log.error(module, message);
            }

            @Override
            public void error(String message, Throwable throwable) {
                Log.error(module, message, throwable);
            }

            @Override
            public void error(String message, Object... object) {
                Log.error(module, message, object);
            }

            public void fatal(String message) {
                Log.fatal(module, message);
            }

            public void error(Throwable t) {
                Log.error(module, t.getMessage(), t);
            }

            public void setAppender(FileAppender fa) {
                throw new IllegalStateException("Please use custom log4j2.xml to manage log4j behavior");
            }

            /**
             * The log file name from the file appender for this module.
             *
             * Note both module and fallback module are provided allowing providing a better opportunity
             * to learn the log file location. Harvesters use the log file name parent directory as a good
             * location to create {@code /harvester_logs/} folder.
             *
             * Built-in configuration uses log file location {@code logs/geonetwork.log} relative to the current directory, or relative to system property {@code log_file}.
             *
             * @return logfile location of {@code logs/geonetwork.log} file
             */
            public String getFileAppender() {
                Logger log = LogManager.getLogger(module);

                // Set effective level to be sure it writes the log
                org.apache.logging.log4j.core.config.Configurator.setLevel(log, getThreshold());

                LoggerContext loggerContext = (LoggerContext) LogManager.getContext(false);
                Configuration configuration = loggerContext.getConfiguration();

                LoggerConfig moduleConfig = configuration.getLoggers().get(module);
                if (moduleConfig != null) {
                    for (Appender appender : moduleConfig.getAppenders().values()) {
                        File file = toLogFile(appender);
                        if (file != null && file.exists()) {
                            return file.getName();
                        }
                    }
                }
                LoggerConfig fallbackConfig = configuration.getLoggers().get(fallbackModule);
                if (fallbackConfig != null) {
                    for (Appender appender : fallbackConfig.getAppenders().values()) {
                        File file = toLogFile(appender);
                        if (file != null && file.exists()) {
                            return file.getName();
                        }
                    }
                }
                if (System.getProperties().containsKey("log_dir")) {
                    File logDir = new File(System.getProperty("log_dir"));
                    if (logDir.exists() && logDir.isDirectory()) {
                        File logFile = new File(logDir, "logs/geonetwork.log");
                        if (logFile.exists()) {
                            return logFile.getName();
                        }
                    }
                } else {
                    File logFile = new File("logs/geonetwork.log");
                    if (logFile.exists()) {
                        return logFile.getName();
                    }
                }
                return "";
            }

            public Level getThreshold() {
                return LogManager.getLogger(fallbackModule).getLevel();
            }

            @Override
            public String getModule() {
                return module;
            }
        };
    }

    /**
     * Looks up log file location, and ensures the file exists.
     *
     * @param appender
     * @return log file location (providing file exists).
     */
    public static File toLogFile(Appender appender) {
        if (appender instanceof FileAppender) {
            FileAppender fileAppender = (FileAppender) appender;
            String fileName = fileAppender.getFileName();
            File file = new File(fileName);
            if (file.exists()) {
                return file;
            }
        }
        if (appender instanceof RollingFileAppender) {
            RollingFileAppender fileAppender = (RollingFileAppender) appender;
            String fileName = fileAppender.getFileName();
            File file = new File(fileName);
            if (file.exists()) {
                return file;
            }
        }
        return null;
    }

    public static File getLogfile() {
        // Appender is supplied by LogUtils based on parsing log4j2.xml file indicated
        // by database settings

        // First, try the fileappender from the logger named "geonetwork"
        org.apache.log4j.Appender appender = org.apache.log4j.Logger.getLogger(GEONETWORK_MODULE).getAppender(FILE_APPENDER_NAME);
        // If still not found, try the one from the logger named "jeeves"
        if (appender == null) {
            appender = org.apache.log4j.Logger.getLogger(Log.JEEVES).getAppender(FILE_APPENDER_NAME);
        }
        if (appender != null) {
            if (appender instanceof AppenderWrapper) {
                AppenderWrapper wrapper = (AppenderWrapper) appender;
                org.apache.logging.log4j.core.Appender appender2 = wrapper.getAppender();

                if (appender2 instanceof FileAppender) {
                    FileAppender fileAppender = (FileAppender) appender2;
                    String logFileName = fileAppender.getFileName();
                    if (logFileName != null) {
                        File logFile = new File(logFileName);
                        if (logFile.exists()) {
                            return logFile;
                        }
                    }
                }
                if (appender2 instanceof RollingFileAppender) {
                    RollingFileAppender fileAppender = (RollingFileAppender) appender2;
                    String logFileName = fileAppender.getFileName();
                    if (logFileName != null) {
                        File logFile = new File(logFileName);
                        if (logFile.exists()) {
                            return logFile;
                        }
                    }
                }
            }
        }
        Log.warning(GEONETWORK_MODULE, "Error when getting logger file for the " + "appender named '" + FILE_APPENDER_NAME + "'. "
            + "Check your log configuration file. "
            + "A FileAppender or RollingFileAppender is required to return last activity to the user interface."
            + "Appender file not found.");

        if (System.getProperties().containsKey("log_dir")) {
            File logDir = new File(System.getProperty("log_dir"));
            if (logDir.exists() && logDir.isDirectory()) {
                File logFile = new File(logDir, "logs/geonetwork.log");
                if (logFile.exists()) {
                    return logFile;
                }
            }
        } else {
            File logFile = new File("logs/geonetwork.log");
            if (logFile.exists()) {
                return logFile;
            }
        }
        return null; // unavailable
    }
}
