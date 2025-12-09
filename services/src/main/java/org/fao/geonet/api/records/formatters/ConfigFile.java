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

package org.fao.geonet.api.records.formatters;

import org.fao.geonet.Constants;
import org.fao.geonet.utils.IO;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class ConfigFile {
    private static final String CONFIG_PROPERTIES_FILENAME = "config.properties";
    private static final String FIXED_LANG_CONFIG_PROP = "fixedLang";
    private static final String LOAD_STRINGS_PROP = "loadGeonetworkStrings";
    private static final String SCHEMAS_TO_LOAD_PROP = "schemasToLoad";
    private static final String APPLICABLE_SCHEMAS = "applicableSchemas";
    private static final String DEPENDS_ON = "dependsOn";
    private static final String PUBLISHED = "published";

    private final Properties config;

    /**
     * Create a new Config file reading the config.properties file from the specific formatter dir, general formatter
     * dir and schema dir. Properties are override if the same property is found in more than one file. The more general
     * property is the one in the schema folder and the most specific one is the one from the actual formatter folder.
     *
     * @param bundleDir       formatter folder.
     * @param searchParentDir {@code true} if config.properties in the parent folders must be included.
     * @param schemaDir       the schema root folder.
     * @throws IOException thrown if there are problems reading the config files.
     * @see ConfigFile#CONFIG_PROPERTIES_FILENAME
     */
    public ConfigFile(Path bundleDir, boolean searchParentDir, Path schemaDir) throws IOException {
        this.config = new Properties();
        List<Path> properties = new ArrayList<>();
        if (searchParentDir) {
            if (schemaDir == null) {
                properties.add(bundleDir.getParent().resolve(CONFIG_PROPERTIES_FILENAME));
                properties.add(bundleDir.resolve(CONFIG_PROPERTIES_FILENAME));

            } else {
                Path current = bundleDir;
                while (current.getParent() != null && !schemaDir.equals(current) && Files.exists(current.getParent())) {
                    properties.add(current.resolve(CONFIG_PROPERTIES_FILENAME));
                    current = current.getParent();
                }
                properties.add(schemaDir.resolve(CONFIG_PROPERTIES_FILENAME));
            }
        } else {
            properties.add(bundleDir.resolve(CONFIG_PROPERTIES_FILENAME));
        }

        // Reverse to allow override (issue #1973):
        // more general -> file in the schema root
        // more specific -> file in the formatter dir
        Collections.reverse(properties);
        for (Path file : properties) {
            if (Files.exists(file)) {
                try (Reader reader = IO.newBufferedReader(file, Constants.CHARSET)) {
                    config.load(reader);
                }
            }
        }
    }

    public static void generateDefault(Path bundleDir) throws IOException {
        Path configFile = bundleDir.resolve(CONFIG_PROPERTIES_FILENAME);
        if (!Files.exists(configFile)) {
            try (PrintStream out = new PrintStream(Files.newOutputStream(configFile), true, Constants.ENCODING)) {
                out.println("# Generated as part of download");
                out.println("# This file is an example of a metadata formatter configuration file");
                out.println("# Uncomment lines of interest");
                out.println();
                out.println("# " + FIXED_LANG_CONFIG_PROP + " sets the language of the strings to the fixed language, " +
                    "this ensures that the formatter will always use the same language for its labels, strings, " +
                    "etc... no matter what language code is in the url.");
                out.println("#" + FIXED_LANG_CONFIG_PROP + "=eng");
                out.println("# " + LOAD_STRINGS_PROP + " - if true or non-existent then geonetwork strings will be added to the xml " +
                    "document before view.xsl is applied.  The default is true so if this parameter is not present then the " +
                    "strings will be loaded");
                out.println(LOAD_STRINGS_PROP + "=true");
                out.println();
                out.println("# " + APPLICABLE_SCHEMAS + " - defines which metadata schemas this bundle applies to.  ");
                out.println("# For example one can specify only iso19139 or a comma separated list of schemas (or all)");
                out.println("# " + APPLICABLE_SCHEMAS + "=iso19115,fgdc-std,iso19139,csw-record,iso19110");
                out.println(APPLICABLE_SCHEMAS + "=all");
                out.println();
                out.println("# " + DEPENDS_ON + " - defines which schema plugin's formatter folder is required to run the  ");
                out.println("# current formatter.  For example iso19139.che depends on iso19139.  This means that the");
                out.println("# files in iso19139 are accessible by iso19139.che formatter.");
                out.println("# " + DEPENDS_ON + "=iso19139");
                out.println();
                out.println("# " + PUBLISHED + " (true/false) - declares if the formatter should be part of public listing of the formatter.");
                out.println(PUBLISHED + "=true");
            }
        }
    }

    public String getLang(String defaultLang) {
        if (config.containsKey(FIXED_LANG_CONFIG_PROP)) {
            return config.getProperty(FIXED_LANG_CONFIG_PROP).trim();
        }
        return defaultLang;
    }

    public boolean isPublished() {
        final String published = config.getProperty(PUBLISHED);
        return published == null || "true".equals(published);
    }

    public boolean contains(String propertyName) {
        return is(propertyName, true);
    }

    private boolean is(String propertyName, boolean defaultValue) {
        return Boolean.parseBoolean(config.getProperty(propertyName, String.valueOf(defaultValue)));
    }

    private List<String> toList(String schemasToLoad) {
        List<String> schemasToLoadList = Arrays.asList(schemasToLoad.split(","));
        List<String> tmp = new ArrayList<String>(schemasToLoadList.size());
        for (String string : schemasToLoadList) {
            tmp.add(string.trim().toLowerCase());
        }

        schemasToLoadList = tmp;
        return schemasToLoadList;
    }

    @Nonnull
    public List<String> listOfApplicableSchemas() {
        return toList(applicableSchemas());
    }

    @Nonnull
    private String applicableSchemas() {
        return config.getProperty(APPLICABLE_SCHEMAS, "all").toLowerCase();
    }

    @Nonnull
    public List<String> listOfSchemasToLoad() {
        return toList(schemasToLoad());
    }

    @Nonnull
    public String schemasToLoad() {
        return config.getProperty(SCHEMAS_TO_LOAD_PROP, "all");
    }

    /**
     * Get the schema ID of the schema this formatter depend on as a fallback for compiling,
     * translations, resources, etc...
     */
    @Nullable
    public String dependOn() {
        return config.getProperty(DEPENDS_ON, null);
    }

    public boolean loadStrings() {
        return contains(ConfigFile.LOAD_STRINGS_PROP);
    }

}
