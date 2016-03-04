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

package org.fao.geonet.services.metadata.format;

import java.util.regex.Pattern;

/**
 * Constants used in formatter package and sub-packages.
 *
 * @author Jesse on 10/3/2014.
 */
public class FormatterConstants {
    public  static final String SCHEMA_PLUGIN_FORMATTER_DIR = "formatter";
    public static final String USER_XSL_DIR = "user_xsl_dir";
    public static final Pattern ID_XSL_REGEX = Pattern.compile("[\\w0-9\\-_/]+");
    public static final String VIEW_XSL_FILENAME = "view.xsl";
    public static final String VIEW_GROOVY_FILENAME = "view.groovy";
    public static final String GROOVY_SCRIPT_ROOT = "groovy";
}
