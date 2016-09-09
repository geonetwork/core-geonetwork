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
//===	Rome - Italy. email: GeoNetwork@fao.org
//==============================================================================

package org.fao.geonet.utils;


import org.apache.log4j.Appender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.Priority;

import java.util.Enumeration;

//=============================================================================

public final class Log {
    public static final String JEEVES = "jeeves";

    //---------------------------------------------------------------------------
    //--- Logging constants
    //---------------------------------------------------------------------------
    public static final String ENGINE = JEEVES + ".engine";
    public static final String MONITOR = JEEVES + ".monitor";
    public static final String APPHAND = JEEVES + ".apphand";
    public static final String WEBAPP = JEEVES + ".webapp";
    public static final String REQUEST = JEEVES + ".request";
    public static final String SERVICE = JEEVES + ".service";
    public static final String SCHEDULER = JEEVES + ".scheduler";
    public static final String RESOURCES = JEEVES + ".resources";
    public static final String XLINK_PROCESSOR = JEEVES + ".xlinkprocessor";
    public static final String XML_RESOLVER = JEEVES + ".xmlresolver";
    public static final String TRANSFORMER_FACTORY = JEEVES
        + ".transformerFactory";
    /**
     * Default constructor. Builds a Log.
     */
    private Log() {
    }

    //---------------------------------------------------------------------------
    //---
    //--- API methods
    //---
    //---------------------------------------------------------------------------

    public static void debug(String module, Object message) {
        Logger.getLogger(module).debug(message);
    }

    public static void debug(String module, Object message, Exception e) {
        Logger.getLogger(module).debug(message, e);
    }

    public static boolean isDebugEnabled(String module) {
        return Logger.getLogger(module).isDebugEnabled();
    }

    @SuppressWarnings("deprecation")
    public static boolean isEnabledFor(String module, int priority) {
        return Logger.getLogger(module).isEnabledFor(Priority.toPriority(priority));
    }
    //---------------------------------------------------------------------------

    public static void trace(String module, Object message) {
        Logger.getLogger(module).trace(message);
    }

    public static void trace(String module, Object message, Exception e) {
        Logger.getLogger(module).trace(message, e);
    }

    public static boolean isTraceEnabled(String module) {
        return Logger.getLogger(module).isTraceEnabled();
    }

    //---------------------------------------------------------------------------

    public static void info(String module, Object message) {
        Logger.getLogger(module).info(message);
    }

    public static void info(String module, Object message, Throwable t) {
        Logger.getLogger(module).info(message, t);
    }

    //---------------------------------------------------------------------------

    public static void warning(String module, Object message) {
        Logger.getLogger(module).warn(message);
    }

    public static void warning(String module, Object message, Throwable e) {
        Logger.getLogger(module).warn(message, e);
    }


    //---------------------------------------------------------------------------

    public static void error(String module, Object message) {
        Logger.getLogger(module).error(message);
    }

    public static void error(String module, Object message, Throwable t) {
        Logger.getLogger(module).error(message, t);
    }

    //---------------------------------------------------------------------------

    public static void fatal(String module, Object message) {
        Logger.getLogger(module).fatal(message);
    }

    //--------------------------------------------------------------------------

    /**
     * Returns a simple logger object
     */
    public static org.fao.geonet.Logger createLogger(final String module) {
        return createLogger(module, null);
    }

    public static org.fao.geonet.Logger createLogger(final String module,
                                                     final String fallbackModule) {
        return new org.fao.geonet.Logger() {

            public boolean isDebugEnabled() {
                return Log.isDebugEnabled(module);
            }

            public void debug(String message) {
                Log.debug(module, message);
            }

            public void info(String message) {
                Log.info(module, message);
            }

            public void warning(String message) {
                Log.warning(module, message);
            }

            public void error(String message) {
                Log.error(module, message);
            }

            public void fatal(String message) {
                Log.fatal(module, message);
            }

            public void error(Throwable t) {
                Log.error(module, t.getMessage(), t);
            }

            public void setAppender(FileAppender fa) {
                Logger.getLogger(module).removeAllAppenders();
                Logger.getLogger(module).addAppender(fa);
            }

            public String getFileAppender() {
                // Set effective level to be sure it writes the log
                Logger.getLogger(module).setLevel(getThreshold());

                @SuppressWarnings("rawtypes")
                Enumeration appenders = Logger.getLogger(module)
                    .getAllAppenders();
                while (appenders.hasMoreElements()) {
                    Appender a = (Appender) appenders.nextElement();
                    if (a instanceof FileAppender) {
                        return ((FileAppender) a).getFile();
                    }
                }
                appenders = Logger.getLogger(fallbackModule).getAllAppenders();
                while (appenders.hasMoreElements()) {
                    Appender a = (Appender) appenders.nextElement();
                    if (a instanceof FileAppender) {
                        return ((FileAppender) a).getFile();
                    }
                }

                return "";

            }

            public Level getThreshold() {
                return Logger.getLogger(fallbackModule).getEffectiveLevel();
            }

            @Override
            public String getModule() {
                return module;
            }
        };
    }

}

//=============================================================================

