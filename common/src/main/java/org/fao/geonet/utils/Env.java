package org.fao.geonet.utils;

import org.apache.commons.lang.StringUtils;

public final class Env {

    /**
     * Retrieves an environment variable with this priority:
     * - Java environment variable.
     * - System environment variable.
     * - Default value provided as parameter.
     *
     * @param propertyName
     * @param defaultValue
     * @return
     */
    public static String getPropertyFromEnv(String propertyName, String defaultValue) {
        // Check if provided in Java environment variable
        String propertyValue = System.getProperty(propertyName);

        if (StringUtils.isEmpty(propertyValue)) {
            // System environment variable
            propertyValue = System.getenv(propertyName.toUpperCase().replace('.', '_'));
        }

        if (StringUtils.isEmpty(propertyValue)) {
            propertyValue = defaultValue;
        }

        return propertyValue;
    }

}
