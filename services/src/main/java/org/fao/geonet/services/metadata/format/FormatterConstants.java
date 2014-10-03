package org.fao.geonet.services.metadata.format;

import org.jdom.Namespace;

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
    public static final String COMPILED_FILE_NAME = "compile-functions.xsl";
    public static final Namespace GNF_NAMESPACE = Namespace.getNamespace("gnf", "http://www.fao.org/geonetwork/functions");
}
