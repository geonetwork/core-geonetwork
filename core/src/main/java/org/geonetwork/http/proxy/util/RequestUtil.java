package org.geonetwork.http.proxy.util;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Enumeration;

import org.fao.geonet.Constants;

/**
 * Created by IntelliJ IDEA.
 * User: Jose
 * Date: 19-may-2009
 * Time: 11:56:10
 * To change this template use File | Settings | File Templates.
 */
public class RequestUtil {
       /**
     * Gets a HttpServletRequest parameter value, using a case insensitive name
     *
     * @param request       HttpServletRequest
     * @param paramName     Name of the parameter
     * @return              Value of parameter, null for non valid parameter
     */
    public static String getParameter(HttpServletRequest request, String paramName) {
        @SuppressWarnings("unchecked")
        Enumeration<String> paramNames = request.getParameterNames();

        while (paramNames.hasMoreElements()) {
            String name = paramNames.nextElement();

            if (name.equalsIgnoreCase(paramName)) {
                return request.getParameter(name);
            }
        }

        return null;
    }


    /**
     * Gets a HttpServletRequest parameter value, using a case insensitive name
     *
     * @param request       HttpServletRequest
     * @param paramName     Name of the parameter
     * @param paramName     Name of the parameter
     * @return              Value of parameter, null for non valid parameter
     */
    public static String getParameter(HttpServletRequest request, String paramName, String defaultValue) {
       String paramValue = getParameter(request, paramName);

       if (paramValue == null) paramValue = defaultValue;

       return paramValue;
    }

    /**
     * Gets the input stream from an HttpServletRequest as a String
     *
     * @param request           HttpServletRequest
     * @return                  String of the request input stream
     * @throws IOException
     */
    public static String inputStreamAsString(HttpServletRequest request)
            throws IOException {

        InputStream stream = request.getInputStream();

        BufferedReader br = new BufferedReader(new InputStreamReader(stream, Constants.ENCODING));
        StringBuilder sb = new StringBuilder();
        String line = null;

        while ((line = br.readLine()) != null) {
            sb.append(line + "\n");
        }

        br.close();
        return sb.toString();
    }
}
