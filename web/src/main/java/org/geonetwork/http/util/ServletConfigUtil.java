package org.geonetwork.http.proxy.util;

import javax.servlet.ServletConfig;

public class ServletConfigUtil {
    /**
     * Removes unwanted characters from a servlet init parameter  value
     *
     * @param servletConfig
     * @param paramName
     * @return
     */
    public static String getInitParamValue(ServletConfig servletConfig,  String paramName) {
        String paramValue = servletConfig.getInitParameter(paramName);

        if (paramValue != null) {
            paramValue = paramValue.replaceAll("\n", "");
            paramValue = paramValue.replaceAll("\r", "");
            paramValue = paramValue.replaceAll("\t", "");
            paramValue = paramValue.replaceAll(" ", "");
        }

        return paramValue;

    }
}
