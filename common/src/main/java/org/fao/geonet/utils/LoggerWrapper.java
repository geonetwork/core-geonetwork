package org.fao.geonet.utils;/* (c) {{YEAR}} Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.FileAppender;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.message.EntryMessage;
import org.apache.logging.log4j.message.Message;
import org.apache.logging.log4j.message.MessageFactory;
import org.apache.logging.log4j.util.MessageSupplier;
import org.apache.logging.log4j.util.Supplier;
import org.fao.geonet.Logger;

import java.io.File;

/**
 * Logger wrapper providing module logging services.
 * <p>
 * Marker is used to indicate the module used, with module parent being used as a fallback.
 * The provided {@code fallbackModule} is used to indicate parent module to
 * assist in determing log file location.
 */
public class LoggerWrapper implements Logger {

    /**
     * Marker indicating module being logged.
     */
    private final Marker MARKER;

    /**
     * Log4J Logger used.
     */
    private final org.apache.logging.log4j.Logger LOGGER;

    /**
     * LoggerWrapper used to apply the module marker to generated messages.
     * @param module Marker indicating module being logged
     */
    LoggerWrapper(Marker module){
        this.MARKER = module != null ? module : Log.JEEVES_MARKER;
        this.LOGGER = LogManager.getLogger(module.getName());
    }

    /**
     * LoggerWrapper used to apply the module marker to generated messages.
     * @param clazz Class whose name used as the Logger name
     * @param module Marker indicating module being logged
     */
    LoggerWrapper(Class clazz, Marker module){
        this.MARKER = module != null ? module : Log.JEEVES_MARKER;
        this.LOGGER = LogManager.getLogger(clazz);
    }

    /**
     * LoggerWrapper used to apply the module marker to generated messages.
     * @param name Logger name
     * @param module Marker indicating module being logged
     */
    LoggerWrapper(String name, Marker module){
        this.MARKER = module != null ? module : Log.JEEVES_MARKER;
        this.LOGGER = LogManager.getLogger(name);
    }



    public boolean isDebugEnabled() {
        return LOGGER.isDebugEnabled();
    }

    public void debug(String message) {
        LOGGER.debug(message);
    }

    public void info(String message) {
        LOGGER.info(message);
    }

    public void warning(String message) {
        LOGGER.warn(message);
    }

    public void error(String message) {
        LOGGER.error(message);
    }

    public void fatal(String message) {
        LOGGER.fatal(message);
    }

    public void error(Throwable t) {
        LOGGER.error(t.getMessage(),t);
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

        // Set effective level to be sure it writes the log
        org.apache.logging.log4j.core.config.Configurator.setLevel(LOGGER, getThreshold());

        LoggerContext loggerContext = (LoggerContext) LogManager.getContext(false);
        Configuration configuration = loggerContext.getConfiguration();

        LoggerConfig loggerConfig = configuration.getLoggers().get(LOGGER.getName());
        if (loggerConfig != null) {
            for (Appender appender : loggerConfig.getAppenders().values()) {
                File file = Log.toLogFile(appender);
                if (file != null && file.exists()) {
                    return file.getName();
                }
            }
        }

        LoggerConfig moduleConfig = configuration.getLoggers().get(MARKER.getName());
        if (moduleConfig != null) {
            for (Appender appender : moduleConfig.getAppenders().values()) {
                File file = Log.toLogFile(appender);
                if (file != null && file.exists()) {
                    return file.getName();
                }
            }
        }

        for(Marker parent : MARKER.getParents()) {
            LoggerConfig fallbackConfig = configuration.getLoggers().get(parent);
            if (fallbackConfig != null) {
                for (Appender appender : fallbackConfig.getAppenders().values()) {
                    File file = Log.toLogFile(appender);
                    if (file != null && file.exists()) {
                        return file.getName();
                    }
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
        return LOGGER.getLevel();
    }

    @Override
    public String getModule() {
        return MARKER.getName();
    }

    @Override public String toString() {
        return "LoggerWrapper " + MARKER.getName() + ":" + LOGGER;
    }

    // Logging delegates

    @Override public void catching(Level level, Throwable throwable) {
        LOGGER.catching(level, throwable);
    }

    @Override public void catching(Throwable throwable) {
        LOGGER.catching(throwable);
    }

    @Override public void debug(Marker marker, Message message) {
        LOGGER.debug(marker, message);
    }

    @Override public void debug(Marker marker, Message message, Throwable throwable) {
        LOGGER.debug(marker, message, throwable);
    }

    @Override public void debug(Marker marker, MessageSupplier messageSupplier) {
        LOGGER.debug(marker, messageSupplier);
    }

    @Override public void debug(Marker marker, MessageSupplier messageSupplier, Throwable throwable) {
        LOGGER.debug(marker, messageSupplier, throwable);
    }

    @Override public void debug(Marker marker, CharSequence message) {
        LOGGER.debug(marker, message);
    }

    @Override public void debug(Marker marker, CharSequence message, Throwable throwable) {
        LOGGER.debug(marker, message, throwable);
    }

    @Override public void debug(Marker marker, Object message) {
        LOGGER.debug(marker, message);
    }

    @Override public void debug(Marker marker, Object message, Throwable throwable) {
        LOGGER.debug(marker, message, throwable);
    }

    @Override public void debug(Marker marker, String message) {
        LOGGER.debug(marker, message);
    }

    @Override public void debug(Marker marker, String message, Object... params) {
        LOGGER.debug(marker, message, params);
    }

    @Override public void debug(Marker marker, String message, Supplier<?>... paramSuppliers) {
        LOGGER.debug(marker, message, paramSuppliers);
    }

    @Override public void debug(Marker marker, String message, Throwable throwable) {
        LOGGER.debug(marker, message, throwable);
    }

    @Override public void debug(Marker marker, Supplier<?> messageSupplier) {
        LOGGER.debug(marker, messageSupplier);
    }

    @Override public void debug(Marker marker, Supplier<?> messageSupplier, Throwable throwable) {
        LOGGER.debug(marker, messageSupplier, throwable);
    }

    @Override public void debug(Message message) {
        LOGGER.debug(MARKER,message);
    }

    @Override public void debug(Message message, Throwable throwable) {
        LOGGER.debug(MARKER,message, throwable);
    }

    @Override public void debug(MessageSupplier messageSupplier) {
        LOGGER.debug(MARKER,messageSupplier);
    }

    @Override public void debug(MessageSupplier messageSupplier, Throwable throwable) {
        LOGGER.debug(MARKER,messageSupplier, throwable);
    }

    @Override public void debug(CharSequence message) {
        LOGGER.debug(MARKER,message);
    }

    @Override public void debug(CharSequence message, Throwable throwable) {
        LOGGER.debug(MARKER,message, throwable);
    }

    @Override public void debug(Object message) {
        LOGGER.debug(MARKER,message);
    }

    @Override public void debug(Object message, Throwable throwable) {
        LOGGER.debug(MARKER,message, throwable);
    }

    @Override public void debug(String message, Object... params) {
        LOGGER.debug(MARKER,message, params);
    }

    @Override public void debug(String message, Supplier<?>... paramSuppliers) {
        LOGGER.debug(MARKER,message, paramSuppliers);
    }

    @Override public void debug(String message, Throwable throwable) {
        LOGGER.debug(MARKER,message, throwable);
    }

    @Override public void debug(Supplier<?> messageSupplier) {
        LOGGER.debug(MARKER,messageSupplier);
    }

    @Override public void debug(Supplier<?> messageSupplier, Throwable throwable) {
        LOGGER.debug(MARKER,messageSupplier, throwable);
    }

    @Override public void debug(Marker marker, String message, Object p0) {
        LOGGER.debug(marker, message, p0);
    }

    @Override public void debug(Marker marker, String message, Object p0, Object p1) {
        LOGGER.debug(marker, message, p0, p1);
    }

    @Override public void debug(Marker marker, String message, Object p0, Object p1, Object p2) {
        LOGGER.debug(marker, message, p0, p1, p2);
    }

    @Override public void debug(Marker marker, String message, Object p0, Object p1, Object p2, Object p3) {
        LOGGER.debug(marker, message, p0, p1, p2, p3);
    }

    @Override public void debug(Marker marker, String message, Object p0, Object p1, Object p2, Object p3, Object p4) {
        LOGGER.debug(marker, message, p0, p1, p2, p3, p4);
    }

    @Override public void debug(Marker marker, String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5) {
        LOGGER.debug(marker, message, p0, p1, p2, p3, p4, p5);
    }

    @Override public void debug(Marker marker, String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5,
        Object p6) {
        LOGGER.debug(marker, message, p0, p1, p2, p3, p4, p5, p6);
    }

    @Override public void debug(Marker marker, String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6,
        Object p7) {
        LOGGER.debug(marker, message, p0, p1, p2, p3, p4, p5, p6, p7);
    }

    @Override public void debug(Marker marker, String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6,
        Object p7, Object p8) {
        LOGGER.debug(marker, message, p0, p1, p2, p3, p4, p5, p6, p7, p8);
    }

    @Override public void debug(Marker marker, String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6,
        Object p7, Object p8, Object p9) {
        LOGGER.debug(marker, message, p0, p1, p2, p3, p4, p5, p6, p7, p8, p9);
    }

    @Override public void debug(String message, Object p0) {
        LOGGER.debug(MARKER,message, p0);
    }

    @Override public void debug(String message, Object p0, Object p1) {
        LOGGER.debug(MARKER,message, p0, p1);
    }

    @Override public void debug(String message, Object p0, Object p1, Object p2) {
        LOGGER.debug(MARKER,message, p0, p1, p2);
    }

    @Override public void debug(String message, Object p0, Object p1, Object p2, Object p3) {
        LOGGER.debug(MARKER,message, p0, p1, p2, p3);
    }

    @Override public void debug(String message, Object p0, Object p1, Object p2, Object p3, Object p4) {
        LOGGER.debug(MARKER,message, p0, p1, p2, p3, p4);
    }

    @Override public void debug(String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5) {
        LOGGER.debug(MARKER,message, p0, p1, p2, p3, p4, p5);
    }

    @Override public void debug(String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6) {
        LOGGER.debug(MARKER,message, p0, p1, p2, p3, p4, p5, p6);
    }

    @Override public void debug(String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7) {
        LOGGER.debug(MARKER,message, p0, p1, p2, p3, p4, p5, p6, p7);
    }

    @Override public void debug(String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7,
        Object p8) {
        LOGGER.debug(MARKER,message, p0, p1, p2, p3, p4, p5, p6, p7, p8);
    }

    @Override public void debug(String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7,
        Object p8, Object p9) {
        LOGGER.debug(MARKER,message, p0, p1, p2, p3, p4, p5, p6, p7, p8, p9);
    }

    @Override @Deprecated public void entry() {
        LOGGER.entry();
    }

    @Override @Deprecated public void entry(Object... params) {
        LOGGER.entry(params);
    }

    @Override public void error(Marker marker, Message message) {
        LOGGER.error(marker, message);
    }

    @Override public void error(Marker marker, Message message, Throwable throwable) {
        LOGGER.error(marker, message, throwable);
    }

    @Override public void error(Marker marker, MessageSupplier messageSupplier) {
        LOGGER.error(marker, messageSupplier);
    }

    @Override public void error(Marker marker, MessageSupplier messageSupplier, Throwable throwable) {
        LOGGER.error(marker, messageSupplier, throwable);
    }

    @Override public void error(Marker marker, CharSequence message) {
        LOGGER.error(marker, message);
    }

    @Override public void error(Marker marker, CharSequence message, Throwable throwable) {
        LOGGER.error(marker, message, throwable);
    }

    @Override public void error(Marker marker, Object message) {
        LOGGER.error(marker, message);
    }

    @Override public void error(Marker marker, Object message, Throwable throwable) {
        LOGGER.error(marker, message, throwable);
    }

    @Override public void error(Marker marker, String message) {
        LOGGER.error(marker, message);
    }

    @Override public void error(Marker marker, String message, Object... params) {
        LOGGER.error(marker, message, params);
    }

    @Override public void error(Marker marker, String message, Supplier<?>... paramSuppliers) {
        LOGGER.error(marker, message, paramSuppliers);
    }

    @Override public void error(Marker marker, String message, Throwable throwable) {
        LOGGER.error(marker, message, throwable);
    }

    @Override public void error(Marker marker, Supplier<?> messageSupplier) {
        LOGGER.error(marker, messageSupplier);
    }

    @Override public void error(Marker marker, Supplier<?> messageSupplier, Throwable throwable) {
        LOGGER.error(marker, messageSupplier, throwable);
    }

    @Override public void error(Message message) {
        LOGGER.error(message);
    }

    @Override public void error(Message message, Throwable throwable) {
        LOGGER.error(message, throwable);
    }

    @Override public void error(MessageSupplier messageSupplier) {
        LOGGER.error(messageSupplier);
    }

    @Override public void error(MessageSupplier messageSupplier, Throwable throwable) {
        LOGGER.error(messageSupplier, throwable);
    }

    @Override public void error(CharSequence message) {
        LOGGER.error(message);
    }

    @Override public void error(CharSequence message, Throwable throwable) {
        LOGGER.error(message, throwable);
    }

    @Override public void error(Object message) {
        LOGGER.error(message);
    }

    @Override public void error(Object message, Throwable throwable) {
        LOGGER.error(message, throwable);
    }

    @Override public void error(String message, Object... params) {
        LOGGER.error(message, params);
    }

    @Override public void error(String message, Supplier<?>... paramSuppliers) {
        LOGGER.error(message, paramSuppliers);
    }

    @Override public void error(String message, Throwable throwable) {
        LOGGER.error(message, throwable);
    }

    @Override public void error(Supplier<?> messageSupplier) {
        LOGGER.error(messageSupplier);
    }

    @Override public void error(Supplier<?> messageSupplier, Throwable throwable) {
        LOGGER.error(messageSupplier, throwable);
    }

    @Override public void error(Marker marker, String message, Object p0) {
        LOGGER.error(marker, message, p0);
    }

    @Override public void error(Marker marker, String message, Object p0, Object p1) {
        LOGGER.error(marker, message, p0, p1);
    }

    @Override public void error(Marker marker, String message, Object p0, Object p1, Object p2) {
        LOGGER.error(marker, message, p0, p1, p2);
    }

    @Override public void error(Marker marker, String message, Object p0, Object p1, Object p2, Object p3) {
        LOGGER.error(marker, message, p0, p1, p2, p3);
    }

    @Override public void error(Marker marker, String message, Object p0, Object p1, Object p2, Object p3, Object p4) {
        LOGGER.error(marker, message, p0, p1, p2, p3, p4);
    }

    @Override public void error(Marker marker, String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5) {
        LOGGER.error(marker, message, p0, p1, p2, p3, p4, p5);
    }

    @Override public void error(Marker marker, String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5,
        Object p6) {
        LOGGER.error(marker, message, p0, p1, p2, p3, p4, p5, p6);
    }

    @Override public void error(Marker marker, String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6,
        Object p7) {
        LOGGER.error(marker, message, p0, p1, p2, p3, p4, p5, p6, p7);
    }

    @Override public void error(Marker marker, String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6,
        Object p7, Object p8) {
        LOGGER.error(marker, message, p0, p1, p2, p3, p4, p5, p6, p7, p8);
    }

    @Override public void error(Marker marker, String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6,
        Object p7, Object p8, Object p9) {
        LOGGER.error(marker, message, p0, p1, p2, p3, p4, p5, p6, p7, p8, p9);
    }

    @Override public void error(String message, Object p0) {
        LOGGER.error(message, p0);
    }

    @Override public void error(String message, Object p0, Object p1) {
        LOGGER.error(message, p0, p1);
    }

    @Override public void error(String message, Object p0, Object p1, Object p2) {
        LOGGER.error(message, p0, p1, p2);
    }

    @Override public void error(String message, Object p0, Object p1, Object p2, Object p3) {
        LOGGER.error(message, p0, p1, p2, p3);
    }

    @Override public void error(String message, Object p0, Object p1, Object p2, Object p3, Object p4) {
        LOGGER.error(message, p0, p1, p2, p3, p4);
    }

    @Override public void error(String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5) {
        LOGGER.error(message, p0, p1, p2, p3, p4, p5);
    }

    @Override public void error(String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6) {
        LOGGER.error(message, p0, p1, p2, p3, p4, p5, p6);
    }

    @Override public void error(String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7) {
        LOGGER.error(message, p0, p1, p2, p3, p4, p5, p6, p7);
    }

    @Override public void error(String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7,
        Object p8) {
        LOGGER.error(message, p0, p1, p2, p3, p4, p5, p6, p7, p8);
    }

    @Override public void error(String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7,
        Object p8, Object p9) {
        LOGGER.error(message, p0, p1, p2, p3, p4, p5, p6, p7, p8, p9);
    }

    @Override @Deprecated public void exit() {
        LOGGER.exit();
    }

    @Override @Deprecated public <R> R exit(R result) {
        return LOGGER.exit(result);
    }

    @Override public void fatal(Marker marker, Message message) {
        LOGGER.fatal(marker, message);
    }

    @Override public void fatal(Marker marker, Message message, Throwable throwable) {
        LOGGER.fatal(marker, message, throwable);
    }

    @Override public void fatal(Marker marker, MessageSupplier messageSupplier) {
        LOGGER.fatal(marker, messageSupplier);
    }

    @Override public void fatal(Marker marker, MessageSupplier messageSupplier, Throwable throwable) {
        LOGGER.fatal(marker, messageSupplier, throwable);
    }

    @Override public void fatal(Marker marker, CharSequence message) {
        LOGGER.fatal(marker, message);
    }

    @Override public void fatal(Marker marker, CharSequence message, Throwable throwable) {
        LOGGER.fatal(marker, message, throwable);
    }

    @Override public void fatal(Marker marker, Object message) {
        LOGGER.fatal(marker, message);
    }

    @Override public void fatal(Marker marker, Object message, Throwable throwable) {
        LOGGER.fatal(marker, message, throwable);
    }

    @Override public void fatal(Marker marker, String message) {
        LOGGER.fatal(marker, message);
    }

    @Override public void fatal(Marker marker, String message, Object... params) {
        LOGGER.fatal(marker, message, params);
    }

    @Override public void fatal(Marker marker, String message, Supplier<?>... paramSuppliers) {
        LOGGER.fatal(marker, message, paramSuppliers);
    }

    @Override public void fatal(Marker marker, String message, Throwable throwable) {
        LOGGER.fatal(marker, message, throwable);
    }

    @Override public void fatal(Marker marker, Supplier<?> messageSupplier) {
        LOGGER.fatal(marker, messageSupplier);
    }

    @Override public void fatal(Marker marker, Supplier<?> messageSupplier, Throwable throwable) {
        LOGGER.fatal(marker, messageSupplier, throwable);
    }

    @Override public void fatal(Message message) {
        LOGGER.fatal(message);
    }

    @Override public void fatal(Message message, Throwable throwable) {
        LOGGER.fatal(message, throwable);
    }

    @Override public void fatal(MessageSupplier messageSupplier) {
        LOGGER.fatal(messageSupplier);
    }

    @Override public void fatal(MessageSupplier messageSupplier, Throwable throwable) {
        LOGGER.fatal(messageSupplier, throwable);
    }

    @Override public void fatal(CharSequence message) {
        LOGGER.fatal(message);
    }

    @Override public void fatal(CharSequence message, Throwable throwable) {
        LOGGER.fatal(message, throwable);
    }

    @Override public void fatal(Object message) {
        LOGGER.fatal(message);
    }

    @Override public void fatal(Object message, Throwable throwable) {
        LOGGER.fatal(message, throwable);
    }

    @Override public void fatal(String message, Object... params) {
        LOGGER.fatal(message, params);
    }

    @Override public void fatal(String message, Supplier<?>... paramSuppliers) {
        LOGGER.fatal(message, paramSuppliers);
    }

    @Override public void fatal(String message, Throwable throwable) {
        LOGGER.fatal(message, throwable);
    }

    @Override public void fatal(Supplier<?> messageSupplier) {
        LOGGER.fatal(messageSupplier);
    }

    @Override public void fatal(Supplier<?> messageSupplier, Throwable throwable) {
        LOGGER.fatal(messageSupplier, throwable);
    }

    @Override public void fatal(Marker marker, String message, Object p0) {
        LOGGER.fatal(marker, message, p0);
    }

    @Override public void fatal(Marker marker, String message, Object p0, Object p1) {
        LOGGER.fatal(marker, message, p0, p1);
    }

    @Override public void fatal(Marker marker, String message, Object p0, Object p1, Object p2) {
        LOGGER.fatal(marker, message, p0, p1, p2);
    }

    @Override public void fatal(Marker marker, String message, Object p0, Object p1, Object p2, Object p3) {
        LOGGER.fatal(marker, message, p0, p1, p2, p3);
    }

    @Override public void fatal(Marker marker, String message, Object p0, Object p1, Object p2, Object p3, Object p4) {
        LOGGER.fatal(marker, message, p0, p1, p2, p3, p4);
    }

    @Override public void fatal(Marker marker, String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5) {
        LOGGER.fatal(marker, message, p0, p1, p2, p3, p4, p5);
    }

    @Override public void fatal(Marker marker, String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5,
        Object p6) {
        LOGGER.fatal(marker, message, p0, p1, p2, p3, p4, p5, p6);
    }

    @Override public void fatal(Marker marker, String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6,
        Object p7) {
        LOGGER.fatal(marker, message, p0, p1, p2, p3, p4, p5, p6, p7);
    }

    @Override public void fatal(Marker marker, String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6,
        Object p7, Object p8) {
        LOGGER.fatal(marker, message, p0, p1, p2, p3, p4, p5, p6, p7, p8);
    }

    @Override public void fatal(Marker marker, String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6,
        Object p7, Object p8, Object p9) {
        LOGGER.fatal(marker, message, p0, p1, p2, p3, p4, p5, p6, p7, p8, p9);
    }

    @Override public void fatal(String message, Object p0) {
        LOGGER.fatal(message, p0);
    }

    @Override public void fatal(String message, Object p0, Object p1) {
        LOGGER.fatal(message, p0, p1);
    }

    @Override public void fatal(String message, Object p0, Object p1, Object p2) {
        LOGGER.fatal(message, p0, p1, p2);
    }

    @Override public void fatal(String message, Object p0, Object p1, Object p2, Object p3) {
        LOGGER.fatal(message, p0, p1, p2, p3);
    }

    @Override public void fatal(String message, Object p0, Object p1, Object p2, Object p3, Object p4) {
        LOGGER.fatal(message, p0, p1, p2, p3, p4);
    }

    @Override public void fatal(String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5) {
        LOGGER.fatal(message, p0, p1, p2, p3, p4, p5);
    }

    @Override public void fatal(String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6) {
        LOGGER.fatal(message, p0, p1, p2, p3, p4, p5, p6);
    }

    @Override public void fatal(String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7) {
        LOGGER.fatal(message, p0, p1, p2, p3, p4, p5, p6, p7);
    }

    @Override public void fatal(String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7,
        Object p8) {
        LOGGER.fatal(message, p0, p1, p2, p3, p4, p5, p6, p7, p8);
    }

    @Override public void fatal(String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7,
        Object p8, Object p9) {
        LOGGER.fatal(message, p0, p1, p2, p3, p4, p5, p6, p7, p8, p9);
    }

    @Override public Level getLevel() {
        return LOGGER.getLevel();
    }

    @Override public <MF extends MessageFactory> MF getMessageFactory() {
        return LOGGER.getMessageFactory();
    }

    @Override public String getName() {
        return LOGGER.getName();
    }

    @Override public void info(Marker marker, Message message) {
        LOGGER.info(marker, message);
    }

    @Override public void info(Marker marker, Message message, Throwable throwable) {
        LOGGER.info(marker, message, throwable);
    }

    @Override public void info(Marker marker, MessageSupplier messageSupplier) {
        LOGGER.info(marker, messageSupplier);
    }

    @Override public void info(Marker marker, MessageSupplier messageSupplier, Throwable throwable) {
        LOGGER.info(marker, messageSupplier, throwable);
    }

    @Override public void info(Marker marker, CharSequence message) {
        LOGGER.info(marker, message);
    }

    @Override public void info(Marker marker, CharSequence message, Throwable throwable) {
        LOGGER.info(marker, message, throwable);
    }

    @Override public void info(Marker marker, Object message) {
        LOGGER.info(marker, message);
    }

    @Override public void info(Marker marker, Object message, Throwable throwable) {
        LOGGER.info(marker, message, throwable);
    }

    @Override public void info(Marker marker, String message) {
        LOGGER.info(marker, message);
    }

    @Override public void info(Marker marker, String message, Object... params) {
        LOGGER.info(marker, message, params);
    }

    @Override public void info(Marker marker, String message, Supplier<?>... paramSuppliers) {
        LOGGER.info(marker, message, paramSuppliers);
    }

    @Override public void info(Marker marker, String message, Throwable throwable) {
        LOGGER.info(marker, message, throwable);
    }

    @Override public void info(Marker marker, Supplier<?> messageSupplier) {
        LOGGER.info(marker, messageSupplier);
    }

    @Override public void info(Marker marker, Supplier<?> messageSupplier, Throwable throwable) {
        LOGGER.info(marker, messageSupplier, throwable);
    }

    @Override public void info(Message message) {
        LOGGER.info(message);
    }

    @Override public void info(Message message, Throwable throwable) {
        LOGGER.info(message, throwable);
    }

    @Override public void info(MessageSupplier messageSupplier) {
        LOGGER.info(messageSupplier);
    }

    @Override public void info(MessageSupplier messageSupplier, Throwable throwable) {
        LOGGER.info(messageSupplier, throwable);
    }

    @Override public void info(CharSequence message) {
        LOGGER.info(message);
    }

    @Override public void info(CharSequence message, Throwable throwable) {
        LOGGER.info(message, throwable);
    }

    @Override public void info(Object message) {
        LOGGER.info(message);
    }

    @Override public void info(Object message, Throwable throwable) {
        LOGGER.info(message, throwable);
    }

    @Override public void info(String message, Object... params) {
        LOGGER.info(message, params);
    }

    @Override public void info(String message, Supplier<?>... paramSuppliers) {
        LOGGER.info(message, paramSuppliers);
    }

    @Override public void info(String message, Throwable throwable) {
        LOGGER.info(message, throwable);
    }

    @Override public void info(Supplier<?> messageSupplier) {
        LOGGER.info(messageSupplier);
    }

    @Override public void info(Supplier<?> messageSupplier, Throwable throwable) {
        LOGGER.info(messageSupplier, throwable);
    }

    @Override public void info(Marker marker, String message, Object p0) {
        LOGGER.info(marker, message, p0);
    }

    @Override public void info(Marker marker, String message, Object p0, Object p1) {
        LOGGER.info(marker, message, p0, p1);
    }

    @Override public void info(Marker marker, String message, Object p0, Object p1, Object p2) {
        LOGGER.info(marker, message, p0, p1, p2);
    }

    @Override public void info(Marker marker, String message, Object p0, Object p1, Object p2, Object p3) {
        LOGGER.info(marker, message, p0, p1, p2, p3);
    }

    @Override public void info(Marker marker, String message, Object p0, Object p1, Object p2, Object p3, Object p4) {
        LOGGER.info(marker, message, p0, p1, p2, p3, p4);
    }

    @Override public void info(Marker marker, String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5) {
        LOGGER.info(marker, message, p0, p1, p2, p3, p4, p5);
    }

    @Override public void info(Marker marker, String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6) {
        LOGGER.info(marker, message, p0, p1, p2, p3, p4, p5, p6);
    }

    @Override public void info(Marker marker, String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6,
        Object p7) {
        LOGGER.info(marker, message, p0, p1, p2, p3, p4, p5, p6, p7);
    }

    @Override public void info(Marker marker, String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6,
        Object p7, Object p8) {
        LOGGER.info(marker, message, p0, p1, p2, p3, p4, p5, p6, p7, p8);
    }

    @Override public void info(Marker marker, String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6,
        Object p7, Object p8, Object p9) {
        LOGGER.info(marker, message, p0, p1, p2, p3, p4, p5, p6, p7, p8, p9);
    }

    @Override public void info(String message, Object p0) {
        LOGGER.info(message, p0);
    }

    @Override public void info(String message, Object p0, Object p1) {
        LOGGER.info(message, p0, p1);
    }

    @Override public void info(String message, Object p0, Object p1, Object p2) {
        LOGGER.info(message, p0, p1, p2);
    }

    @Override public void info(String message, Object p0, Object p1, Object p2, Object p3) {
        LOGGER.info(message, p0, p1, p2, p3);
    }

    @Override public void info(String message, Object p0, Object p1, Object p2, Object p3, Object p4) {
        LOGGER.info(message, p0, p1, p2, p3, p4);
    }

    @Override public void info(String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5) {
        LOGGER.info(message, p0, p1, p2, p3, p4, p5);
    }

    @Override public void info(String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6) {
        LOGGER.info(message, p0, p1, p2, p3, p4, p5, p6);
    }

    @Override public void info(String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7) {
        LOGGER.info(message, p0, p1, p2, p3, p4, p5, p6, p7);
    }

    @Override public void info(String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7,
        Object p8) {
        LOGGER.info(message, p0, p1, p2, p3, p4, p5, p6, p7, p8);
    }

    @Override public void info(String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7,
        Object p8, Object p9) {
        LOGGER.info(message, p0, p1, p2, p3, p4, p5, p6, p7, p8, p9);
    }

    @Override public boolean isDebugEnabled(Marker marker) {
        return LOGGER.isDebugEnabled(marker);
    }

    @Override public boolean isEnabled(Level level) {
        return LOGGER.isEnabled(level);
    }

    @Override public boolean isEnabled(Level level, Marker marker) {
        return LOGGER.isEnabled(level, marker);
    }

    @Override public boolean isErrorEnabled() {
        return LOGGER.isErrorEnabled();
    }

    @Override public boolean isErrorEnabled(Marker marker) {
        return LOGGER.isErrorEnabled(marker);
    }

    @Override public boolean isFatalEnabled() {
        return LOGGER.isFatalEnabled();
    }

    @Override public boolean isFatalEnabled(Marker marker) {
        return LOGGER.isFatalEnabled(marker);
    }

    @Override public boolean isInfoEnabled() {
        return LOGGER.isInfoEnabled();
    }

    @Override public boolean isInfoEnabled(Marker marker) {
        return LOGGER.isInfoEnabled(marker);
    }

    @Override public boolean isTraceEnabled() {
        return LOGGER.isTraceEnabled();
    }

    @Override public boolean isTraceEnabled(Marker marker) {
        return LOGGER.isTraceEnabled(marker);
    }

    @Override public boolean isWarnEnabled() {
        return LOGGER.isWarnEnabled();
    }

    @Override public boolean isWarnEnabled(Marker marker) {
        return LOGGER.isWarnEnabled(marker);
    }

    @Override public void log(Level level, Marker marker, Message message) {
        LOGGER.log(level, marker, message);
    }

    @Override public void log(Level level, Marker marker, Message message, Throwable throwable) {
        LOGGER.log(level, marker, message, throwable);
    }

    @Override public void log(Level level, Marker marker, MessageSupplier messageSupplier) {
        LOGGER.log(level, marker, messageSupplier);
    }

    @Override public void log(Level level, Marker marker, MessageSupplier messageSupplier, Throwable throwable) {
        LOGGER.log(level, marker, messageSupplier, throwable);
    }

    @Override public void log(Level level, Marker marker, CharSequence message) {
        LOGGER.log(level, marker, message);
    }

    @Override public void log(Level level, Marker marker, CharSequence message, Throwable throwable) {
        LOGGER.log(level, marker, message, throwable);
    }

    @Override public void log(Level level, Marker marker, Object message) {
        LOGGER.log(level, marker, message);
    }

    @Override public void log(Level level, Marker marker, Object message, Throwable throwable) {
        LOGGER.log(level, marker, message, throwable);
    }

    @Override public void log(Level level, Marker marker, String message) {
        LOGGER.log(level, marker, message);
    }

    @Override public void log(Level level, Marker marker, String message, Object... params) {
        LOGGER.log(level, marker, message, params);
    }

    @Override public void log(Level level, Marker marker, String message, Supplier<?>... paramSuppliers) {
        LOGGER.log(level, marker, message, paramSuppliers);
    }

    @Override public void log(Level level, Marker marker, String message, Throwable throwable) {
        LOGGER.log(level, marker, message, throwable);
    }

    @Override public void log(Level level, Marker marker, Supplier<?> messageSupplier) {
        LOGGER.log(level, marker, messageSupplier);
    }

    @Override public void log(Level level, Marker marker, Supplier<?> messageSupplier, Throwable throwable) {
        LOGGER.log(level, marker, messageSupplier, throwable);
    }

    @Override public void log(Level level, Message message) {
        LOGGER.log(level, message);
    }

    @Override public void log(Level level, Message message, Throwable throwable) {
        LOGGER.log(level, message, throwable);
    }

    @Override public void log(Level level, MessageSupplier messageSupplier) {
        LOGGER.log(level, messageSupplier);
    }

    @Override public void log(Level level, MessageSupplier messageSupplier, Throwable throwable) {
        LOGGER.log(level, messageSupplier, throwable);
    }

    @Override public void log(Level level, CharSequence message) {
        LOGGER.log(level, message);
    }

    @Override public void log(Level level, CharSequence message, Throwable throwable) {
        LOGGER.log(level, message, throwable);
    }

    @Override public void log(Level level, Object message) {
        LOGGER.log(level, message);
    }

    @Override public void log(Level level, Object message, Throwable throwable) {
        LOGGER.log(level, message, throwable);
    }

    @Override public void log(Level level, String message) {
        LOGGER.log(level, message);
    }

    @Override public void log(Level level, String message, Object... params) {
        LOGGER.log(level, message, params);
    }

    @Override public void log(Level level, String message, Supplier<?>... paramSuppliers) {
        LOGGER.log(level, message, paramSuppliers);
    }

    @Override public void log(Level level, String message, Throwable throwable) {
        LOGGER.log(level, message, throwable);
    }

    @Override public void log(Level level, Supplier<?> messageSupplier) {
        LOGGER.log(level, messageSupplier);
    }

    @Override public void log(Level level, Supplier<?> messageSupplier, Throwable throwable) {
        LOGGER.log(level, messageSupplier, throwable);
    }

    @Override public void log(Level level, Marker marker, String message, Object p0) {
        LOGGER.log(level, marker, message, p0);
    }

    @Override public void log(Level level, Marker marker, String message, Object p0, Object p1) {
        LOGGER.log(level, marker, message, p0, p1);
    }

    @Override public void log(Level level, Marker marker, String message, Object p0, Object p1, Object p2) {
        LOGGER.log(level, marker, message, p0, p1, p2);
    }

    @Override public void log(Level level, Marker marker, String message, Object p0, Object p1, Object p2, Object p3) {
        LOGGER.log(level, marker, message, p0, p1, p2, p3);
    }

    @Override public void log(Level level, Marker marker, String message, Object p0, Object p1, Object p2, Object p3, Object p4) {
        LOGGER.log(level, marker, message, p0, p1, p2, p3, p4);
    }

    @Override public void log(Level level, Marker marker, String message, Object p0, Object p1, Object p2, Object p3, Object p4,
        Object p5) {
        LOGGER.log(level, marker, message, p0, p1, p2, p3, p4, p5);
    }

    @Override public void log(Level level, Marker marker, String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5,
        Object p6) {
        LOGGER.log(level, marker, message, p0, p1, p2, p3, p4, p5, p6);
    }

    @Override public void log(Level level, Marker marker, String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5,
        Object p6, Object p7) {
        LOGGER.log(level, marker, message, p0, p1, p2, p3, p4, p5, p6, p7);
    }

    @Override public void log(Level level, Marker marker, String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5,
        Object p6, Object p7, Object p8) {
        LOGGER.log(level, marker, message, p0, p1, p2, p3, p4, p5, p6, p7, p8);
    }

    @Override public void log(Level level, Marker marker, String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5,
        Object p6, Object p7, Object p8, Object p9) {
        LOGGER.log(level, marker, message, p0, p1, p2, p3, p4, p5, p6, p7, p8, p9);
    }

    @Override public void log(Level level, String message, Object p0) {
        LOGGER.log(level, message, p0);
    }

    @Override public void log(Level level, String message, Object p0, Object p1) {
        LOGGER.log(level, message, p0, p1);
    }

    @Override public void log(Level level, String message, Object p0, Object p1, Object p2) {
        LOGGER.log(level, message, p0, p1, p2);
    }

    @Override public void log(Level level, String message, Object p0, Object p1, Object p2, Object p3) {
        LOGGER.log(level, message, p0, p1, p2, p3);
    }

    @Override public void log(Level level, String message, Object p0, Object p1, Object p2, Object p3, Object p4) {
        LOGGER.log(level, message, p0, p1, p2, p3, p4);
    }

    @Override public void log(Level level, String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5) {
        LOGGER.log(level, message, p0, p1, p2, p3, p4, p5);
    }

    @Override public void log(Level level, String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6) {
        LOGGER.log(level, message, p0, p1, p2, p3, p4, p5, p6);
    }

    @Override public void log(Level level, String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6,
        Object p7) {
        LOGGER.log(level, message, p0, p1, p2, p3, p4, p5, p6, p7);
    }

    @Override public void log(Level level, String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6,
        Object p7, Object p8) {
        LOGGER.log(level, message, p0, p1, p2, p3, p4, p5, p6, p7, p8);
    }

    @Override public void log(Level level, String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6,
        Object p7, Object p8, Object p9) {
        LOGGER.log(level, message, p0, p1, p2, p3, p4, p5, p6, p7, p8, p9);
    }

    @Override public void printf(Level level, Marker marker, String format, Object... params) {
        LOGGER.printf(level, marker, format, params);
    }

    @Override public void printf(Level level, String format, Object... params) {
        LOGGER.printf(level, format, params);
    }

    @Override public <T extends Throwable> T throwing(Level level, T throwable) {
        return LOGGER.throwing(level, throwable);
    }

    @Override public <T extends Throwable> T throwing(T throwable) {
        return LOGGER.throwing(throwable);
    }

    @Override public void trace(Marker marker, Message message) {
        LOGGER.trace(marker, message);
    }

    @Override public void trace(Marker marker, Message message, Throwable throwable) {
        LOGGER.trace(marker, message, throwable);
    }

    @Override public void trace(Marker marker, MessageSupplier messageSupplier) {
        LOGGER.trace(marker, messageSupplier);
    }

    @Override public void trace(Marker marker, MessageSupplier messageSupplier, Throwable throwable) {
        LOGGER.trace(marker, messageSupplier, throwable);
    }

    @Override public void trace(Marker marker, CharSequence message) {
        LOGGER.trace(marker, message);
    }

    @Override public void trace(Marker marker, CharSequence message, Throwable throwable) {
        LOGGER.trace(marker, message, throwable);
    }

    @Override public void trace(Marker marker, Object message) {
        LOGGER.trace(marker, message);
    }

    @Override public void trace(Marker marker, Object message, Throwable throwable) {
        LOGGER.trace(marker, message, throwable);
    }

    @Override public void trace(Marker marker, String message) {
        LOGGER.trace(marker, message);
    }

    @Override public void trace(Marker marker, String message, Object... params) {
        LOGGER.trace(marker, message, params);
    }

    @Override public void trace(Marker marker, String message, Supplier<?>... paramSuppliers) {
        LOGGER.trace(marker, message, paramSuppliers);
    }

    @Override public void trace(Marker marker, String message, Throwable throwable) {
        LOGGER.trace(marker, message, throwable);
    }

    @Override public void trace(Marker marker, Supplier<?> messageSupplier) {
        LOGGER.trace(marker, messageSupplier);
    }

    @Override public void trace(Marker marker, Supplier<?> messageSupplier, Throwable throwable) {
        LOGGER.trace(marker, messageSupplier, throwable);
    }

    @Override public void trace(Message message) {
        LOGGER.trace(message);
    }

    @Override public void trace(Message message, Throwable throwable) {
        LOGGER.trace(message, throwable);
    }

    @Override public void trace(MessageSupplier messageSupplier) {
        LOGGER.trace(messageSupplier);
    }

    @Override public void trace(MessageSupplier messageSupplier, Throwable throwable) {
        LOGGER.trace(messageSupplier, throwable);
    }

    @Override public void trace(CharSequence message) {
        LOGGER.trace(message);
    }

    @Override public void trace(CharSequence message, Throwable throwable) {
        LOGGER.trace(message, throwable);
    }

    @Override public void trace(Object message) {
        LOGGER.trace(message);
    }

    @Override public void trace(Object message, Throwable throwable) {
        LOGGER.trace(message, throwable);
    }

    @Override public void trace(String message) {
        LOGGER.trace(message);
    }

    @Override public void trace(String message, Object... params) {
        LOGGER.trace(message, params);
    }

    @Override public void trace(String message, Supplier<?>... paramSuppliers) {
        LOGGER.trace(message, paramSuppliers);
    }

    @Override public void trace(String message, Throwable throwable) {
        LOGGER.trace(message, throwable);
    }

    @Override public void trace(Supplier<?> messageSupplier) {
        LOGGER.trace(messageSupplier);
    }

    @Override public void trace(Supplier<?> messageSupplier, Throwable throwable) {
        LOGGER.trace(messageSupplier, throwable);
    }

    @Override public void trace(Marker marker, String message, Object p0) {
        LOGGER.trace(marker, message, p0);
    }

    @Override public void trace(Marker marker, String message, Object p0, Object p1) {
        LOGGER.trace(marker, message, p0, p1);
    }

    @Override public void trace(Marker marker, String message, Object p0, Object p1, Object p2) {
        LOGGER.trace(marker, message, p0, p1, p2);
    }

    @Override public void trace(Marker marker, String message, Object p0, Object p1, Object p2, Object p3) {
        LOGGER.trace(marker, message, p0, p1, p2, p3);
    }

    @Override public void trace(Marker marker, String message, Object p0, Object p1, Object p2, Object p3, Object p4) {
        LOGGER.trace(marker, message, p0, p1, p2, p3, p4);
    }

    @Override public void trace(Marker marker, String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5) {
        LOGGER.trace(marker, message, p0, p1, p2, p3, p4, p5);
    }

    @Override public void trace(Marker marker, String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5,
        Object p6) {
        LOGGER.trace(marker, message, p0, p1, p2, p3, p4, p5, p6);
    }

    @Override public void trace(Marker marker, String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6,
        Object p7) {
        LOGGER.trace(marker, message, p0, p1, p2, p3, p4, p5, p6, p7);
    }

    @Override public void trace(Marker marker, String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6,
        Object p7, Object p8) {
        LOGGER.trace(marker, message, p0, p1, p2, p3, p4, p5, p6, p7, p8);
    }

    @Override public void trace(Marker marker, String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6,
        Object p7, Object p8, Object p9) {
        LOGGER.trace(marker, message, p0, p1, p2, p3, p4, p5, p6, p7, p8, p9);
    }

    @Override public void trace(String message, Object p0) {
        LOGGER.trace(message, p0);
    }

    @Override public void trace(String message, Object p0, Object p1) {
        LOGGER.trace(message, p0, p1);
    }

    @Override public void trace(String message, Object p0, Object p1, Object p2) {
        LOGGER.trace(message, p0, p1, p2);
    }

    @Override public void trace(String message, Object p0, Object p1, Object p2, Object p3) {
        LOGGER.trace(message, p0, p1, p2, p3);
    }

    @Override public void trace(String message, Object p0, Object p1, Object p2, Object p3, Object p4) {
        LOGGER.trace(message, p0, p1, p2, p3, p4);
    }

    @Override public void trace(String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5) {
        LOGGER.trace(message, p0, p1, p2, p3, p4, p5);
    }

    @Override public void trace(String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6) {
        LOGGER.trace(message, p0, p1, p2, p3, p4, p5, p6);
    }

    @Override public void trace(String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7) {
        LOGGER.trace(message, p0, p1, p2, p3, p4, p5, p6, p7);
    }

    @Override public void trace(String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7,
        Object p8) {
        LOGGER.trace(message, p0, p1, p2, p3, p4, p5, p6, p7, p8);
    }

    @Override public void trace(String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7,
        Object p8, Object p9) {
        LOGGER.trace(message, p0, p1, p2, p3, p4, p5, p6, p7, p8, p9);
    }

    @Override public EntryMessage traceEntry() {
        return LOGGER.traceEntry();
    }

    @Override public EntryMessage traceEntry(String format, Object... params) {
        return LOGGER.traceEntry(format, params);
    }

    @Override public EntryMessage traceEntry(Supplier<?>... paramSuppliers) {
        return LOGGER.traceEntry(paramSuppliers);
    }

    @Override public EntryMessage traceEntry(String format, Supplier<?>... paramSuppliers) {
        return LOGGER.traceEntry(format, paramSuppliers);
    }

    @Override public EntryMessage traceEntry(Message message) {
        return LOGGER.traceEntry(message);
    }

    @Override public void traceExit() {
        LOGGER.traceExit();
    }

    @Override public <R> R traceExit(R result) {
        return LOGGER.traceExit(result);
    }

    @Override public <R> R traceExit(String format, R result) {
        return LOGGER.traceExit(format, result);
    }

    @Override public void traceExit(EntryMessage message) {
        LOGGER.traceExit(message);
    }

    @Override public <R> R traceExit(EntryMessage message, R result) {
        return LOGGER.traceExit(message, result);
    }

    @Override public <R> R traceExit(Message message, R result) {
        return LOGGER.traceExit(message, result);
    }

    @Override public void warn(Marker marker, Message message) {
        LOGGER.warn(marker, message);
    }

    @Override public void warn(Marker marker, Message message, Throwable throwable) {
        LOGGER.warn(marker, message, throwable);
    }

    @Override public void warn(Marker marker, MessageSupplier messageSupplier) {
        LOGGER.warn(marker, messageSupplier);
    }

    @Override public void warn(Marker marker, MessageSupplier messageSupplier, Throwable throwable) {
        LOGGER.warn(marker, messageSupplier, throwable);
    }

    @Override public void warn(Marker marker, CharSequence message) {
        LOGGER.warn(marker, message);
    }

    @Override public void warn(Marker marker, CharSequence message, Throwable throwable) {
        LOGGER.warn(marker, message, throwable);
    }

    @Override public void warn(Marker marker, Object message) {
        LOGGER.warn(marker, message);
    }

    @Override public void warn(Marker marker, Object message, Throwable throwable) {
        LOGGER.warn(marker, message, throwable);
    }

    @Override public void warn(Marker marker, String message) {
        LOGGER.warn(marker, message);
    }

    @Override public void warn(Marker marker, String message, Object... params) {
        LOGGER.warn(marker, message, params);
    }

    @Override public void warn(Marker marker, String message, Supplier<?>... paramSuppliers) {
        LOGGER.warn(marker, message, paramSuppliers);
    }

    @Override public void warn(Marker marker, String message, Throwable throwable) {
        LOGGER.warn(marker, message, throwable);
    }

    @Override public void warn(Marker marker, Supplier<?> messageSupplier) {
        LOGGER.warn(marker, messageSupplier);
    }

    @Override public void warn(Marker marker, Supplier<?> messageSupplier, Throwable throwable) {
        LOGGER.warn(marker, messageSupplier, throwable);
    }

    @Override public void warn(Message message) {
        LOGGER.warn(message);
    }

    @Override public void warn(Message message, Throwable throwable) {
        LOGGER.warn(message, throwable);
    }

    @Override public void warn(MessageSupplier messageSupplier) {
        LOGGER.warn(messageSupplier);
    }

    @Override public void warn(MessageSupplier messageSupplier, Throwable throwable) {
        LOGGER.warn(messageSupplier, throwable);
    }

    @Override public void warn(CharSequence message) {
        LOGGER.warn(message);
    }

    @Override public void warn(CharSequence message, Throwable throwable) {
        LOGGER.warn(message, throwable);
    }

    @Override public void warn(Object message) {
        LOGGER.warn(message);
    }

    @Override public void warn(Object message, Throwable throwable) {
        LOGGER.warn(message, throwable);
    }

    @Override public void warn(String message) {
        LOGGER.warn(message);
    }

    @Override public void warn(String message, Object... params) {
        LOGGER.warn(message, params);
    }

    @Override public void warn(String message, Supplier<?>... paramSuppliers) {
        LOGGER.warn(message, paramSuppliers);
    }

    @Override public void warn(String message, Throwable throwable) {
        LOGGER.warn(message, throwable);
    }

    @Override public void warn(Supplier<?> messageSupplier) {
        LOGGER.warn(messageSupplier);
    }

    @Override public void warn(Supplier<?> messageSupplier, Throwable throwable) {
        LOGGER.warn(messageSupplier, throwable);
    }

    @Override public void warn(Marker marker, String message, Object p0) {
        LOGGER.warn(marker, message, p0);
    }

    @Override public void warn(Marker marker, String message, Object p0, Object p1) {
        LOGGER.warn(marker, message, p0, p1);
    }

    @Override public void warn(Marker marker, String message, Object p0, Object p1, Object p2) {
        LOGGER.warn(marker, message, p0, p1, p2);
    }

    @Override public void warn(Marker marker, String message, Object p0, Object p1, Object p2, Object p3) {
        LOGGER.warn(marker, message, p0, p1, p2, p3);
    }

    @Override public void warn(Marker marker, String message, Object p0, Object p1, Object p2, Object p3, Object p4) {
        LOGGER.warn(marker, message, p0, p1, p2, p3, p4);
    }

    @Override public void warn(Marker marker, String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5) {
        LOGGER.warn(marker, message, p0, p1, p2, p3, p4, p5);
    }

    @Override public void warn(Marker marker, String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6) {
        LOGGER.warn(marker, message, p0, p1, p2, p3, p4, p5, p6);
    }

    @Override public void warn(Marker marker, String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6,
        Object p7) {
        LOGGER.warn(marker, message, p0, p1, p2, p3, p4, p5, p6, p7);
    }

    @Override public void warn(Marker marker, String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6,
        Object p7, Object p8) {
        LOGGER.warn(marker, message, p0, p1, p2, p3, p4, p5, p6, p7, p8);
    }

    @Override public void warn(Marker marker, String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6,
        Object p7, Object p8, Object p9) {
        LOGGER.warn(marker, message, p0, p1, p2, p3, p4, p5, p6, p7, p8, p9);
    }

    @Override public void warn(String message, Object p0) {
        LOGGER.warn(message, p0);
    }

    @Override public void warn(String message, Object p0, Object p1) {
        LOGGER.warn(message, p0, p1);
    }

    @Override public void warn(String message, Object p0, Object p1, Object p2) {
        LOGGER.warn(message, p0, p1, p2);
    }

    @Override public void warn(String message, Object p0, Object p1, Object p2, Object p3) {
        LOGGER.warn(message, p0, p1, p2, p3);
    }

    @Override public void warn(String message, Object p0, Object p1, Object p2, Object p3, Object p4) {
        LOGGER.warn(message, p0, p1, p2, p3, p4);
    }

    @Override public void warn(String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5) {
        LOGGER.warn(message, p0, p1, p2, p3, p4, p5);
    }

    @Override public void warn(String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6) {
        LOGGER.warn(message, p0, p1, p2, p3, p4, p5, p6);
    }

    @Override public void warn(String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7) {
        LOGGER.warn(message, p0, p1, p2, p3, p4, p5, p6, p7);
    }

    @Override public void warn(String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7,
        Object p8) {
        LOGGER.warn(message, p0, p1, p2, p3, p4, p5, p6, p7, p8);
    }

    @Override public void warn(String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7,
        Object p8, Object p9) {
        LOGGER.warn(message, p0, p1, p2, p3, p4, p5, p6, p7, p8, p9);
    }

    @Override public void logMessage(Level level, Marker marker, String fqcn, StackTraceElement location, Message message,
        Throwable throwable) {
        LOGGER.logMessage(level, marker, fqcn, location, message, throwable);
    }

    @Override public LogBuilder atTrace() {
        return LOGGER.atTrace();
    }

    @Override public LogBuilder atDebug() {
        return LOGGER.atDebug();
    }

    @Override public LogBuilder atInfo() {
        return LOGGER.atInfo();
    }

    @Override public LogBuilder atWarn() {
        return LOGGER.atWarn();
    }

    @Override public LogBuilder atError() {
        return LOGGER.atError();
    }

    @Override public LogBuilder atFatal() {
        return LOGGER.atFatal();
    }

    @Override public LogBuilder always() {
        return LOGGER.always();
    }

    @Override public LogBuilder atLevel(Level level) {
        return LOGGER.atLevel(level);
    }
}
