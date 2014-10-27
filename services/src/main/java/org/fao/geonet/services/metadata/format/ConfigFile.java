package org.fao.geonet.services.metadata.format;

import com.google.common.collect.Lists;
import com.google.common.io.Closer;
import org.fao.geonet.Constants;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
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

    public ConfigFile(File bundleDir, boolean searchParentDir, File schemaDir) throws IOException {
        this.config = new Properties();
        File[] properties;
        if (searchParentDir) {
            if (schemaDir == null) {
                properties = new File[]{
                        new File(bundleDir.getParentFile(), CONFIG_PROPERTIES_FILENAME),
                        new File(bundleDir, CONFIG_PROPERTIES_FILENAME)};

            } else {
                List<File> tmp = Lists.newArrayList();
                File current = bundleDir;
                while (!schemaDir.equals(current) && current.getParentFile() != null) {
                    tmp.add(new File(current, CONFIG_PROPERTIES_FILENAME));
                    current = current.getParentFile();
                }
                properties = tmp.toArray(new File[tmp.size()]);
            }
        } else {
            properties = new File[]{new File(bundleDir, CONFIG_PROPERTIES_FILENAME)};
        }

        for (File file : properties) {
            if (file.exists()) {
                Closer closer = Closer.create();
                try {
                    final FileInputStream fileInputStream = closer.register(new FileInputStream(file));
                    InputStreamReader reader = closer.register(new InputStreamReader(fileInputStream, Constants.ENCODING));
                    config.load(reader);
                } finally {
                    closer.close();
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

    @Nullable
    public String dependOn() {
        return config.getProperty(DEPENDS_ON, null);
    }

    public static void generateDefault(File bundleDir) throws IOException {
        File configFile = new File(bundleDir, CONFIG_PROPERTIES_FILENAME);
        if (!configFile.exists()) {
            PrintStream out = new PrintStream(configFile, Constants.ENCODING);
            try {
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
                out.println("# " + SCHEMAS_TO_LOAD_PROP + " - defines which schema localization files should be loaded and added to the" +
                            " xml document before view.xsl is applied");
                out.println("# if a comma separated list then only those schemas will be loaded");
                out.println("# " + SCHEMAS_TO_LOAD_PROP + "=none");
                out.println("# " + SCHEMAS_TO_LOAD_PROP + "=iso19115,fgdc-std,iso19139,csw-record,iso19110");
                out.println(SCHEMAS_TO_LOAD_PROP + "=all");
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
            } finally {
                out.close();
            }
        }
    }

    public boolean loadStrings() {
        return contains(ConfigFile.LOAD_STRINGS_PROP);
    }

}
