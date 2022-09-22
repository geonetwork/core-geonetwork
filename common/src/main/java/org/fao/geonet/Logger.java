//=============================================================================
//===	Copyright (C) 2001-2005 Food and Agriculture Organization of the
//===	United Nations (FAO-UN), United Nations World Food Programme (WFP)
//===	and United Nations Environment Programme (UNEP)
//===
//===	This library is free software; you can redistribute it and/or
//===	modify it under the terms of the GNU Lesser General Public
//===	License as published by the Free Software Foundation; either
//===	version 2.1 of the License, or (at your option) any later version.
//===
//===	This library is distributed in the hope that it will be useful,
//===	but WITHOUT ANY WARRANTY; without even the implied warranty of
//===	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
//===	Lesser General Public License for more details.
//===
//===	You should have received a copy of the GNU Lesser General Public
//===	License along with this library; if not, write to the Free Software
//===	Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
//===
//===	Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
//===	Rome - Italy. email: geonetwork@osgeo.org
//==============================================================================

package org.fao.geonet;

//=============================================================================

import org.apache.logging.log4j.core.appender.FileAppender;

/**
 * GeoNetwork logger wrapper providing module based logging services.
 */
public interface Logger {
    /**
     * Quick check to see if debug logging is available, used to avoid creating
     * expensive debug messages if they are not going to be used.
     *
     * @return check if debug logging is enabled
     */
    public boolean isDebugEnabled();

    /**
     * Log debug message used indicate module troubleshoot module activity.
     *
     * @param message debug message used to provide in
     */
    public void debug(String message);

    /**
     * Log information message indicating module progress.
     *
     * @param message information message indicating progress
     */
    public void info(String message);

    /** Log warning message indicating potentially harmful situation, module
     * will continue to try and complete current activity.
     *
     * @param message Warning message indicating potentially harmful situation
     */
    public void warning(String message);

    /**
     * Log error message indicating module cannot continue current activity.
     *
     * @param message Error message
     */
    public void error(String message);

    /**
     * Log error message using provided throwable, indicating module cannot continue
     * current activity.
     *
     * @param ex Cause of error condition.
     */
    public void error(Throwable ex);

    /**
     * Log severe message, indicating application cannot continue to operate.
     *
     * @param message severe message
     */
    public void fatal(String message);

    /**
     * Functional module used for logging messages (for example {@code jeeves.engine}).
     *
     * @return functional module used for logging messages.
     */
    public String getModule();

    /**
     * Configure logger with log4j {@link FileAppender}, used for output.
     *
     * The file appender is also responsible for log file location provided by {@link #getFileAppender()}.
     *
     * @param fileAppender Log4j FileAppender
     */
    public void setAppender(FileAppender fileAppender);

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
    public String getFileAppender();

    /**
     * Access to omodule logging level, providing
     * @return
     */
    public org.apache.logging.log4j.Level getThreshold();

}

//=============================================================================

