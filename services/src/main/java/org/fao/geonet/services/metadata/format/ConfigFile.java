package org.fao.geonet.services.metadata.format;

import com.google.common.collect.Lists;
import org.fao.geonet.Constants;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ConfigFile {
    private static final String CONFIG_PROPERTIES_FILENAME = "config.properties";
    private static final String FIXED_LANG_CONFIG_PROP = "fixedLang";
    private static final String LOAD_STRINGS_PROP = "loadGeonetworkStrings";
    private static final String SCHEMAS_TO_LOAD_PROP = "schemasToLoad";
    private static final String APPLICABLE_SCHEMAS = "applicableSchemas";
    private static final String DEPENDS_ON = "dependsOn";

    private Properties config;

    public ConfigFile(Path bundleDir, boolean searchParentDir, Path schemaDir) throws IOException {
        this.config = new Properties();
        Path[] properties;
        if (searchParentDir) {
            if (schemaDir == null) {
                properties = new Path[]{
                        bundleDir.getParent().resolve(CONFIG_PROPERTIES_FILENAME),
                        bundleDir.resolve(CONFIG_PROPERTIES_FILENAME)};

            } else {
                List<Path> tmp = Lists.newArrayList();
                Path current = bundleDir;
                while (current.getParent() != null && !schemaDir.equals(current) && Files.exists(current.getParent())) {
                    tmp.add(current.resolve(CONFIG_PROPERTIES_FILENAME));
                    current = current.getParent();
                }
                tmp.add(schemaDir.resolve(CONFIG_PROPERTIES_FILENAME));
                properties = tmp.toArray(new Path[tmp.size()]);
            }
        } else {
            properties = new Path[]{bundleDir.resolve(CONFIG_PROPERTIES_FILENAME)};
        }

        for (Path file : properties) {
            if (Files.exists(file)) {
                try (InputStreamReader reader = new InputStreamReader(Files.newInputStream(file), Constants.ENCODING)) {
                    config.load(reader);
                }
            }
        }
    }

    public String getLang(String defaultLang) {
        if (config.containsKey(FIXED_LANG_CONFIG_PROP)) {
            return config.getProperty(FIXED_LANG_CONFIG_PROP).trim();
        }
        return defaultLang;
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
     * Get the schema ID of the schema this formatter depend on as a fallback for compiling, translations, resources, etc...
     */
    @Nullable
    public String dependOn() {
        return config.getProperty(DEPENDS_ON, null);
    }

    public static void generateDefault(Path bundleDir) throws IOException {
        Path configFile = bundleDir.resolve(CONFIG_PROPERTIES_FILENAME);
        if (!Files.exists(configFile)) {
            try (PrintStream out = new PrintStream(Files.newOutputStream(configFile), true, Constants.ENCODING)) {
                out.println("# Generated as part of download");
                out.println("# This file is an example of a metadata formatter configuration file");
                out.println("# Uncomment lines of interest");
                out.println("");
                out.println("# " + FIXED_LANG_CONFIG_PROP + " sets the language of the strings to the fixed language, " +
                            "this ensures that the formatter will always use the same language for its labels, strings, " +
                            "etc... no matter what language code is in the url.");
                out.println("#" + FIXED_LANG_CONFIG_PROP + "=eng");
                out.println("# " + LOAD_STRINGS_PROP + " - if true or non-existent then geonetwork strings will be added to the xml " +
                            "document before view.xsl is applied.  The default is true so if this parameter is not present then the " +
                            "strings will be loaded");
                out.println(LOAD_STRINGS_PROP + "=true");
                out.println("");
                out.println("# " + APPLICABLE_SCHEMAS + " - defines which metadata schemas this bundle applies to.  ");
                out.println("# For example one can specify only iso19139 or a comma separated list of schemas (or all)");
                out.println("# " + APPLICABLE_SCHEMAS + "=iso19115,fgdc-std,iso19139,csw-record,iso19110");
                out.println(APPLICABLE_SCHEMAS + "=all");
                out.println("");
                out.println("# " + DEPENDS_ON + " - defines which schema plugin's formatter folder is required to run the  ");
                out.println("# current formatter.  For example iso19139.che depends on iso19139.  This means that the");
                out.println("# files in iso19139 are accessible by iso19139.che formatter.");
                out.println("# " + DEPENDS_ON + "=iso19139");
            }
        }
    }

    public boolean loadStrings() {
        return contains(ConfigFile.LOAD_STRINGS_PROP);
    }

}
