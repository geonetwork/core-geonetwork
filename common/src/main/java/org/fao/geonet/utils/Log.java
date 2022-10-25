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


import org.apache.log4j.Priority;
import org.apache.log4j.bridge.AppenderWrapper;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.appender.FileAppender;
import org.apache.logging.log4j.core.appender.RollingFileAppender;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.util.StackLocator;

import java.io.File;
import java.util.Enumeration;

//=============================================================================

/**
 * Jeeves logging integration, defining functional logger categories by module
 * (rather that strictly based on java package structure).
 */
public final class Log {

    /**
     * Jeeves base logging module name.
     *
     * @deprecated Use {@link #JEEVES_MARKER}
     */
    public static final String JEEVES = "jeeves";
    /**
     * Jeeves marker for log messages.
     */
    public static final Marker JEEVES_MARKER = MarkerManager.getMarker("jeeves");

    //---------------------------------------------------------------------------
    //--- Logging constants
    //---------------------------------------------------------------------------
    /**
     * Jeeves application handler
     *
     * @deprecated Use {@link #APPHAND_MARKER}
     */
    public static final String APPHAND = JEEVES + ".apphand";
    /**
     * Application handler marker for log messages.
     */
    public static final Marker APPHAND_MARKER = MarkerManager.getMarker("apphand").addParents(JEEVES_MARKER);
    /**
     * Jeeves engine
     * @deprecated Use {@link #ENGINE_MARKER}
     */
    public static final String ENGINE = JEEVES + ".engine";
    /**
     * Engine marker for log messages.
     */
    public static final Marker ENGINE_MARKER = MarkerManager.getMarker("engine").addParents(JEEVES_MARKER);
    /**
     * Jeeves monitor
     *
     * @deprecated Use {@link #MONITOR_MARKER}
     */
    public static final String MONITOR = JEEVES + ".monitor";
    /**
     * Monitor marker for log messages.
     */
    public static final Marker MONITOR_MARKER = MarkerManager.getMarker("monitor").addParents(JEEVES_MARKER);

    /**
     * Jeeves request handling
     *
     * @deprecated Use {@link #REQUEST_MARKER}
     */
    public static final String REQUEST = JEEVES + ".request";
    /**
     * Request handling marker for log messages.
     */
    public static final Marker REQUEST_MARKER = MarkerManager.getMarker("request").addParents(JEEVES_MARKER);

    /**
     * Jeeves resources
     * @deprecated Use {@link #RESOURCES_MARKER}
     */
    public static final String RESOURCES = JEEVES + ".resources";
    /**
     * Resources marker for log messages
     */
    public static final Marker RESOURCES_MARKER = MarkerManager.getMarker("resources").addParents(JEEVES_MARKER);

    /**
     * Jeeves scheduler, used for {@code ScheduleContext}.
     *
     * @deprecated Use {@link #SCHEDULER_MARKER}
     */
    public static final String SCHEDULER = JEEVES + ".scheduler";
    /**
     * Scheudler marker for log messages.
     */
    public static final Marker SCHEDULER_MARKER = MarkerManager.getMarker("scheduler").addParents(JEEVES_MARKER);

    /**
     * Jeeves service
     *
     * @deprecated Use {@link #SERVICE_MARKER}
     */
    public static final String SERVICE = JEEVES + ".service";
    /**
     * Service handling marker for log messages.
     */
    public static final Marker SERVICE_MARKER = MarkerManager.getMarker("service").addParents(JEEVES_MARKER);


    /**
     * Jeeves transformer factory
     * @deprecated Use {{@link #TRANSFORMER_FACTORY_MARKER}
     */
    public static final String TRANSFORMER_FACTORY = JEEVES
        + ".transformerFactory";
    /**
     * XML transformer factory marker for use with log events.
     */
    public static final Marker TRANSFORMER_FACTORY_MARKER = MarkerManager.getMarker("xmlresolver").addParents(JEEVES_MARKER);

    /**
     * Jeeves web application
     *
     * @deprecated Use {@link #WEBAPP_MARKER}
     */
    public static final String WEBAPP = JEEVES + ".webapp";
    /**
     * Web application marker for log messages.
     */
    public static final Marker WEBAPP_MARKER = MarkerManager.getMarker("webapp").addParents(JEEVES_MARKER);

    /**
     * Jeeves xlink processor
     * @deprecated Use {@link #XLINK_MARKER}
     */
    public static final String XLINK_PROCESSOR = JEEVES + ".xlinkprocessor";

    /**
     * Xlink processor marker for use with log messages
     */
    public static final Marker XLINK_MARKER = MarkerManager.getMarker("xlinkprocessor").addParents(JEEVES_MARKER);

    /**
     * Jeeves xml resolver/
     *
     * @deprecated Use {@link #XML_RESOLVER_MARKER}
     */
    public static final String XML_RESOLVER = JEEVES + ".xmlresolver";
    /**
     * Xml resolver marker for log messages.
     */
    public static final Marker XML_RESOLVER_MARKER = MarkerManager.getMarker("xmlresolver").addParents(JEEVES_MARKER);

    /**
     * Default constructor. Builds a Log.
     */
    private Log() {
    }

    /**
     * Log debug message for module on behalf of calling class.
     * @param module
     * @param message
     * @deprecated Please use {@link Log#createLogger(Class, Marker)} to create your own Logger directly
     */
    public static void debug(String module, Object message) {
        createLogger(module).debug(message);
    }


    /**
     * Log debug exception for module on behalf of calling class.
     * @param module
     * @param message
     * @deprecated Please use {@link Log#createLogger(Class, Marker)} to create your own Logger directly
     */
    public static void debug(String module, Object message, Exception e) {
        createLogger(module).debug(message, e);
    }

    /**
     * Check if debug is enabled for module
     * @param module
     * @deprecated Please use {@link Log#createLogger(Class, Marker)} to create your own Logger directly
     */
    public static boolean isDebugEnabled(String module) {
        return createLogger(module).isDebugEnabled();
    }

    /**
     * Check if label enabled for module
     * @param module
     * @param level
     * @deprecated Please use {@link Log#createLogger(Class, Marker)} to create your own Logger directly
     */
    public static boolean isEnabledFor(String module, Level level) {
        return createLogger(module).isEnabled(level);
    }
    //---------------------------------------------------------------------------

    /**
     * Log trace message for module on behalf of calling class.
     * @param module
     * @param message
     * @deprecated Please use {@link Log#createLogger(Class, Marker)} to create your own Logger directly
     */
    public static void trace(String module, Object message) {
        createLogger(module).trace(message);
    }

    /**
     * Log trace exception for module on behalf of calling class.
     * @param module
     * @param message
     * @deprecated Please use {@link Log#createLogger(Class, Marker)} to create your own Logger directly
     */
    public static void trace(String module, Object message, Exception e) {
        createLogger(module).trace(message, e);
    }

    /**
     * Check if trace enabled for module.
     * @param module
     * @deprecated Please use {@link Log#createLogger(Class, Marker)} to create your own Logger directly
     */
    public static boolean isTraceEnabled(String module) {
        return createLogger(module).isTraceEnabled();
    }

    //---------------------------------------------------------------------------

    /**
     * Log info message for module on behalf of calling class.
     * @param module
     * @param message
     * @deprecated Please use {@link Log#createLogger(Class, Marker)} to create your own Logger directly
     */
    public static void info(String module, Object message) {
        createLogger(module).info(message);
    }

    /**
     * Log info exception for module on behalf of calling class.
     * @param module
     * @param message
     * @deprecated Please use {@link Log#createLogger(Class, Marker)} to create your own Logger directly
     */
    public static void info(String module, Object message, Throwable t) {
        createLogger(module).info(message, t);
    }

    //---------------------------------------------------------------------------

    /**
     * Log warning message for module on behalf of calling class.
     * @param module
     * @param message
     * @deprecated Please use {@link Log#createLogger(Class, Marker)} to create your own Logger directly
     */
    public static void warning(String module, Object message) {
        createLogger(module).warn(message);
    }

    /**
     * Log warning exception for module on behalf of calling class.
     * @param module
     * @param message
     * @deprecated Please use {@link Log#createLogger(Class, Marker)} to create your own Logger directly
     */
    public static void warning(String module, Object message, Throwable e) {
        createLogger(module).warn(message, e);
    }


    //---------------------------------------------------------------------------

    /**
     * Log error message for module on behalf of calling class.
     * @param module
     * @param message
     * @deprecated Please use {@link Log#createLogger(Class, Marker)} to create your own Logger directly
     */
    public static void error(String module, Object message) {
        createLogger(module).error(message);
    }

    /**
     * Log error exception for module on behalf of calling class.
     * @param module
     * @param message
     * @deprecated Please use {@link Log#createLogger(Class, Marker)} to create your own Logger directly
     */
    public static void error(String module, Object message, Throwable t) {
        createLogger(module).error(message, t);
    }

    //---------------------------------------------------------------------------

    /**
     * Log fatal message for module on behalf of calling class.
     * @param module
     * @param message
     * @deprecated Please use {@link Log#createLogger(Class, Marker)} to create your own Logger directly
     */
    public static void fatal(String module, Object message) {
        createLogger(module).fatal(message);
    }

    //--------------------------------------------------------------------------

    /**
     * Logger wrapper providing module logging services.
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
     * @param fallback
     * @return logger providing module logging services.
     */
    public static org.fao.geonet.Logger createLogger(final String module,
                                                     final String fallback) {
        String caller = toCaller();
        Marker marker = toMarker(module,fallback);
        return new LoggerWrapper(caller,marker);
    }

    /**
     * Logger wrapper providing module logging services.
     * <p>
     * The provided {@code fallbackModule} is used to indicate parent module.
     *
     * @param clazz Class used for logger name
     * @param marker Marker used to indicate jeeves module
     * @return logger providing module logging services.
     */
    public static org.fao.geonet.Logger createLogger(Class clazz, Marker marker) {
        if(clazz == null){
            throw new NullPointerException("Class required to determine logger name");
        }
        if( marker == null){
            throw new NullPointerException("Marker required to indicate geonetwork module");
        }
        return new LoggerWrapper(clazz,marker);
    }

    /**
     * Module marker to use based on module and fallback names.
     *
     * @param module module name used in marker discovery
     * @param fallback module parent used in marker discovery, {@link Log#JEEVES_MARKER} assumed.
     * @return Marker determined from module and fallback, or {@link Log#JEEVES_MARKER} by default.
     */
    static Marker toMarker(String module, String fallback){
        if (module == null) {
            return Log.JEEVES_MARKER;
        }
        else if(module.contains(".") && fallback == null){
            Marker marker = null;
            for(String name : module.split("\\.")){
                Marker m = MarkerManager.getMarker(name);
                if( marker != null){
                    m.addParents(marker);
                }
                marker = m;
            }
            return marker;
        }
        else {
            Marker marker = MarkerManager.getMarker(module);
            Marker parent = fallback != null ? MarkerManager.getMarker(fallback) : Log.JEEVES_MARKER;
            if (!marker.isInstanceOf(parent)) {
                marker.addParents(parent);
            }
            return marker;
        }
    }

    /**
     * Determine caller of static logging methods (using an internal stack trace).
     *
     * Approach is inefficient and clients are encouraged to use their own logging instance if
     * they have more than one logging message to share.
     *
     * @return caller
     */
    static String toCaller(){
        Throwable t = new Throwable("logging context");
        StackTraceElement[] stackTrace = t.getStackTrace();
        for( StackTraceElement element : stackTrace ){
            if( element.getClassName().equals("org.fao.geonet.utils.Log")) {
                continue;
            }
            return element.getClassName();
        }
        return JEEVES;
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

}
