package org.fao.geonet.services.metadata.format;

import org.apache.commons.io.FileUtils;
import org.fao.geonet.Constants;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

public class ConfigFile {
    private static final String CONFIG_PROPERTIES_FILENAME = "config.properties";
    private static final String FIXED_LANG_CONFIG_PROP = "fixedLang";
    private static final String LOAD_STRINGS_PROP = "loadGeonetworkStrings";
    private static final String SCHEMAS_TO_LOAD_PROP = "schemasToLoad";
    private static final String APPLICABLE_SCHEMAS = "applicableSchemas";

	private Properties config;

	public ConfigFile(File bundleDir) throws IOException {
		this.config = new Properties();
        for (File file: FileUtils.listFiles(bundleDir, new String[]{"properties"}, false)) {
            if(file.getName().equalsIgnoreCase(CONFIG_PROPERTIES_FILENAME)){
                FileInputStream inStream = new FileInputStream (file);
                try {
                    config.load(inStream);
                } finally {
                    inStream.close();
                }
            }
        }
	}

	public String getLang(String defaultLang) {
        if(config.containsKey(FIXED_LANG_CONFIG_PROP)) {
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

	public List<String> listOfApplicableSchemas() {
		return toList(applicableSchemas());
	}

	private String applicableSchemas() {
		return config.getProperty(APPLICABLE_SCHEMAS, "all").toLowerCase();
	}

	public List<String> listOfSchemasToLoad() {
		return toList(schemasToLoad());
	}

	public String schemasToLoad() {
		return config.getProperty(SCHEMAS_TO_LOAD_PROP, "all");
	}

	public static void generateDefault(File bundleDir) throws IOException {
        File configFile = new File(bundleDir, CONFIG_PROPERTIES_FILENAME);
        if(!configFile.exists()) {
            PrintStream out = new PrintStream(configFile, Constants.ENCODING);
            try {
                out.println("# Generated as part of download");
                out.println("# This file is an example of a metadata formatter configuration file");
                out.println("# Uncomment lines of interest");
                out.println("");
                out.println("# "+FIXED_LANG_CONFIG_PROP+" sets the language of the strings to the fixed language, this ensures that the formatter will always use the same language for its labels, strings, etc... no matter what language code is in the url.");
                out.println("#"+FIXED_LANG_CONFIG_PROP+"=eng");
                out.println("# "+LOAD_STRINGS_PROP+" - if true or non-existent then geonetwork strings will be added to the xml document before view.xsl is applied.  The default is true so if this parameter is not present then the strings will be loaded");
                out.println(LOAD_STRINGS_PROP+"=true");
                out.println("");
                out.println("# "+SCHEMAS_TO_LOAD_PROP+" - defines which schema localization files should be loaded and added to the xml document before view.xsl is applied");
                out.println("# if a comma separated list then only those schemas will be loaded");
                out.println("# "+SCHEMAS_TO_LOAD_PROP+"=none");
                out.println("# "+SCHEMAS_TO_LOAD_PROP+"=iso19115,fgdc-std,iso19139,csw-record,iso19110");
                out.println(SCHEMAS_TO_LOAD_PROP+"=all");
                out.println("");
                out.println("# "+APPLICABLE_SCHEMAS+" - defines which metadata schemas this bundle applies to.  ");
                out.println("# For example one can specify only iso19139 or a comma separated list of schemas (or all)");
                out.println("# "+APPLICABLE_SCHEMAS+"=iso19115,fgdc-std,iso19139,csw-record,iso19110");
                out.println(APPLICABLE_SCHEMAS+"=all");
            } finally {
                out.close();
            }
        }
	}

	public boolean loadStrings() {
		return contains(ConfigFile.LOAD_STRINGS_PROP);
	}

}
