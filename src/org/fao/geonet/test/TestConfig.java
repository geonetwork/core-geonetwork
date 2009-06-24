package org.fao.geonet.test;

import java.io.File;

public class TestConfig {
	/**
	 * Home dir: for now should be path to GeoNetwork svn topdir.
	 */
	private static String GN_HOME;
	private static String REL_TEST_HOME = "/test";
	private static String REL_APP_PATH = "/web/geonetwork/";
	private static String REL_CONFIG_PATH = REL_APP_PATH + "WEB-INF/";
 	private static String GN_BASE_URL = "/geonetwork";


	static public String getAppPath() {
		return GN_HOME + REL_APP_PATH;
	}

	static public String getConfigPath() {
		return GN_HOME + REL_CONFIG_PATH;
	}

	static public String getBaseUrl() {
		return GN_BASE_URL;
	}

	static public String getHomePath() {
		return GN_HOME;
	}

	static public String getTestHomePath() {
		return GN_HOME + REL_TEST_HOME;
	}

	static public void init() {
		// Get GN_HOME from sysproperty (-DGN_HOME) or user working dir
		GN_HOME = System.getProperty("GN_HOME");
		if (GN_HOME == null) {
			GN_HOME = System.getProperty("user.dir");
		}

		// Check with
		if (!new File(getTestHomePath()).exists()) {
			GN_HOME = System.getProperty("user.dir") + "/../..";

			if (!new File(getTestHomePath()).exists()) {
				throw new IllegalArgumentException(getTestHomePath() + " does not exist - please run from top of GN dir");
			}
		}
	}
}
