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

package org.geonetwork.http.proxy.util;

import javax.servlet.http.HttpServletRequest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Enumeration;

import org.fao.geonet.Constants;

/**
 * Created by IntelliJ IDEA. User: Jose Date: 19-may-2009 Time: 11:56:10 To change this template use
 * File | Settings | File Templates.
 */
public class RequestUtil {
    /**
     * Gets a HttpServletRequest parameter value, using a case insensitive name
     *
     * @param request   HttpServletRequest
     * @param paramName Name of the parameter
     * @return Value of parameter, null for non valid parameter
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
     * @param request   HttpServletRequest
     * @param paramName Name of the parameter
     * @param paramName Name of the parameter
     * @return Value of parameter, null for non valid parameter
     */
    public static String getParameter(HttpServletRequest request, String paramName, String defaultValue) {
        String paramValue = getParameter(request, paramName);

        if (paramValue == null) paramValue = defaultValue;

        return paramValue;
    }

    /**
     * Gets the input stream from an HttpServletRequest as a String
     *
     * @param request HttpServletRequest
     * @return String of the request input stream
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
