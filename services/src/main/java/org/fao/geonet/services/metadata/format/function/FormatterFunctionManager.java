package org.fao.geonet.services.metadata.format.function;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.io.Files;
import org.fao.geonet.Constants;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.GeonetworkDataDirectory;
import org.fao.geonet.kernel.SchemaManager;
import org.fao.geonet.utils.Xml;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.fao.geonet.services.metadata.format.FormatterConstants.SCHEMA_PLUGIN_FORMATTER_DIR;

/**
 * Class for obtaining formatter functions and resolving dependencies of the functions.
 * <p>
 *     Any Formatter that references functions starting with gnf: will automatically have a new xsl file compiled containing the
 *     definitions of those files and an include will be added to the file.  The idea is to reduce the overhead of using shared
 *     xslt functions.  Shared functions are defined in the formatter directory as well as in the
 *     <code>schema_plugins/&amp;schema>/formatter</code> folder in a file with the name pattern: <code>functions[.*].xsl</code>.
 *     Legal examples: formatter.xsl, formatter-iso19139.xsl, etc...
 * </p>
 * <p>
 *     Functions in the <code>functions[.*].xsl</code> file in the shared formatter folder can be used in any formatter and should be written
 *     in a way that there are no dependencies on any specific schema.  As such they should consist almost exclusively of html.
 * </p>
 * <p>
 *     Functions in a <code>schema_plugins/&amp;schema>/formatter/functions[.*].xsl</code> file are only accessible by that formatters
 *     defined that apply to that schema (as defined in the config.properties).  These can contain utility methods (like accessing
 *     the metadata language) and can be schema specific.
 * </p>
 * <p>
 *     Functions can call other functions, all dependencies will be added to the compiled functions file.  However, there may not be
 *     any circular dependencies.
 * </p>
 *
 * @author Jesse on 10/2/2014.
 */
@Component
public class FormatterFunctionManager {
    private static final String FUNCTIONS_FILE_NAME = "functions";
    private static final Pattern NAMESPACE_PATTERN = Pattern.compile("xmlns:([^=]+)\\s*=\\s*\"([^\"]+)\"");
    static final Pattern FUNCTION_FINDER = Pattern.compile("gnf:([^(\\s]+?)\\s*\\(");

    @Autowired
    private SchemaManager schemaManager;
    @Autowired
    private GeonetworkDataDirectory geonetworkDataDirectory;
    @Qualifier("stagingProfile")
    @Autowired
    private String stagingProfile;

    private Map<String, FormatterFunctionMap> schemaToFormatters = Maps.newHashMap();

    /**
     * Search the xsl files for references to gnf: functions and return a String which is the generated xslt functions file.
     */
    public String getFunctionsFor(String xslFilePath, String xslFileData, Set<String> applicableSchemas) throws IOException, JDOMException {
        FormatterFunctionMap functions = getFunctions(null);
        if (applicableSchemas.contains("all")) {
            for (String schema : this.schemaManager.getSchemas()) {
                functions.merge(getFunctions(schema));
            }
        } else {
            for (String schema : applicableSchemas) {
                functions.merge(getFunctions(schema));
            }
        }
        final Matcher matcher = FUNCTION_FINDER.matcher(xslFileData);

        if (!matcher.find()) {
            return null;
        }

        StringBuilder functionsFile = new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        functionsFile.append("<xsl:stylesheet version=\"2.0\"\n    ");
        for (Namespace namespace : functions.getNamespaces()) {
            if (namespace.getPrefix().trim().isEmpty() || namespace.getURI().trim().isEmpty()) {
                continue;
            }
            functionsFile.append("xmlns:").
                    append(namespace.getPrefix()).
                    append("=\"").
                    append(namespace.getURI()).
                    append("\"\n    ");
        }
        functionsFile.append(">\n\n");

        Set<String> added = Sets.newHashSet();
        do{
            final String functionName = matcher.group(1);
            if (!added.contains(functionName)) {
                added.add(functionName);
                functions.get(xslFilePath, functionName).addTo(xslFilePath, functions, functionsFile);
            }
        } while (matcher.find());

        functionsFile.append("</xsl:stylesheet>\n");

        return functionsFile.toString();
    }

    @VisibleForTesting
    synchronized FormatterFunctionMap getFunctions(String schema) throws IOException, JDOMException {
        FormatterFunctionMap functions = this.schemaToFormatters.get(schema);

        if (functions == null || Geonet.StagingProfile.DEVELOPMENT.equals(this.stagingProfile)) {
            if (schema == null) {
                functions = loadSchemaFunctions(this.geonetworkDataDirectory.getFormatterDir());
            } else {
                final File formatterDir = new File(this.schemaManager.getSchemaDir(schema), SCHEMA_PLUGIN_FORMATTER_DIR);
                functions = loadSchemaFunctions(formatterDir);
                functions.addNamespaces(this.schemaManager.getSchema(schema).getNamespaces());
            }

            this.schemaToFormatters.put(schema, functions);
        }

        if (schema == null) {
            return functions.copy();
        } else {
            return functions;
        }
    }

    private FormatterFunctionMap loadSchemaFunctions(File formatterDir) throws IOException, JDOMException {
        FormatterFunctionMap map = new FormatterFunctionMap();

        final File[] files = formatterDir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile() && file.getName().startsWith(FUNCTIONS_FILE_NAME) && file.getName().endsWith(".xsl")) {
                    final String data = Files.toString(file, Charset.forName(Constants.ENCODING));

                    final Matcher matcher = NAMESPACE_PATTERN.matcher(data);
                    while (matcher.find()) {
                        String prefix = matcher.group(1);
                        String uri = matcher.group(2);
                        if (!(prefix.isEmpty() || uri.isEmpty())) {
                            map.addNamespace(Namespace.getNamespace(prefix, uri));
                        }
                    }

                    map.loadFrom(file.getPath(), Xml.loadString(data, false));
                }
            }
        }

        return map;
    }
}
