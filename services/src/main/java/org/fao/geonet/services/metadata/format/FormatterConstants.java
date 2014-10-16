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
