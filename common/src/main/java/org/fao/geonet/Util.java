//=============================================================================
//===	Copyright (C) 2001-2005 Food and Agriculture Organization of the
//===	United Nations (FAO-UN), United Nations World Food Programme (WFP)
//===	and United Nations Environment Programme (UNEP)
//===
//===	This library is free software; you can redistribute it and/or
//===	modify it under the terms of the GNU Lesser General Public
//===	License as published by the Free Software Foundation; either
//===	version 2.1 of the License, or (at your option) any later version.
//===
//===	This library is distributed in the hope that it will be useful,
//===	but WITHOUT ANY WARRANTY; without even the implied warranty of
//===	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
//===	Lesser General Public License for more details.
//===
//===	You should have received a copy of the GNU Lesser General Public
//===	License along with this library; if not, write to the Free Software
//===	Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
//===
//===	Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
//===	Rome - Italy. email: GeoNetwork@fao.org
//==============================================================================

package org.fao.geonet;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.fao.geonet.exceptions.BadInputEx;
import org.fao.geonet.exceptions.BadParameterEx;
import org.fao.geonet.exceptions.MissingParameterEx;
import org.jdom.Element;

//=============================================================================

/**
 * Generic utility class (static methods)
 */

public final class Util {
    /**
     * Default constructor. Builds a Util.
     */
    private Util() {
    }

    //--------------------------------------------------------------------------
    //---
    //--- API methods
    //---
    //--------------------------------------------------------------------------

    public static Element getChild(Element el, String name) throws MissingParameterEx {
        Element param = el.getChild(name);

        if (param == null)
            throw new MissingParameterEx(name, el);

        return param;
    }

    //--------------------------------------------------------------------------

    public static String getParam(Element el, String name) throws BadInputEx {
        if (el == null)
            throw new MissingParameterEx(name);

        Element param = el.getChild(name);

        if (param == null)
            throw new MissingParameterEx(name, el);

        String value = param.getTextTrim();

        if (value.length() == 0)
            throw new BadParameterEx(name, value);

        return value;
    }

    //--------------------------------------------------------------------------

    public static List<String> getParams(Element el, String name) throws BadInputEx {
        if (el == null)
            throw new MissingParameterEx(name);

        List<String> values = new LinkedList<String>();

        for (Object obj : el.getChildren(name)) {
            Element param = (Element) obj;

            String value = param.getTextTrim();

            if (value.length() == 0)
                throw new BadParameterEx(name, value);

            values.add(value);
        }

        return values;
    }

    //--------------------------------------------------------------------------

    public static String getParam(Element el, String name, String defValue) {
        if (el == null)
            return defValue;

        Element param = el.getChild(name);

        if (param == null)
            return defValue;

        String value = param.getTextTrim();

        if (value.length() == 0)
            return defValue;

        return value;
    }

    //--------------------------------------------------------------------------

    public static int getParamAsInt(Element el, String name) throws BadInputEx {
        String value = getParam(el, name);

        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new BadParameterEx(name, value);
        }
    }

    //--------------------------------------------------------------------------

    public static List<Integer> getParamsAsInt(Element el, String name) throws BadInputEx {

        List<Integer> values = new LinkedList<Integer>();
        for (String value : getParams(el, name)) {
            try {
                values.add(Integer.parseInt(value));
            } catch (NumberFormatException e) {
                throw new BadParameterEx(name, value);
            }
        }

        return values;
    }

    //--------------------------------------------------------------------------

    public static int getParam(Element el, String name, int defValue) throws BadParameterEx {
        String value = getParam(el, name, null);

        if (value == null)
            return defValue;

        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new BadParameterEx(name, value);
        }
    }

    //--------------------------------------------------------------------------

    public static boolean getParam(Element el, String name, boolean defValue) throws BadParameterEx {
        String value = getParam(el, name, null);

        if (value == null)
            return defValue;

        value = value.toLowerCase();

        if (value.equals("true") || value.equals("on") || value.equals("yes"))
            return true;

        if (value.equals("false") || value.equals("off") || value.equals("no"))
            return false;

        throw new BadParameterEx(name, value);
    }

    //--------------------------------------------------------------------------

    public static String getAttrib(Element el, String name) throws BadInputEx {
        String value = el.getAttributeValue(name);

        if (value == null)
            throw new MissingParameterEx("attribute:" + name, el);

        value = value.trim();

        if (value.length() == 0)
            throw new BadParameterEx("attribute:" + name, value);

        return value;
    }

    //--------------------------------------------------------------------------

    public static String getAttrib(Element el, String name, String defValue) {
        if (el == null)
            return defValue;

        String value = el.getAttributeValue(name);

        if (value == null)
            return defValue;

        value = value.trim();

        if (value.length() == 0)
            return defValue;

        return value;
    }

    /**
     * Retrieve the value of a named header.
     *
     * @param headers  The map of HTTP headers.
     * @param name     The name of the header to be retrieved.
     * @param defValue The default value to be used if the header is not present.
     * @return The header's value
     */
    public static String getHeader(Map<String, String> headers, String name, String defValue) {
        if (headers == null)
            return defValue;

        String value = headers.get(name);

        if (value == null)
            return defValue;

        if (value.length() == 0)
            return defValue;

        return value;
    }

    //--------------------------------------------------------------------------

    /**
     * replace occurrences of <p> in <s> with <r>
     */

    public static String replaceString(String s, String pattern, String replacement) {
        StringBuffer result = new StringBuffer();
        int i;

        while ((i = s.indexOf(pattern)) != -1) {
            result.append(s.substring(0, i));
            result.append(replacement);
            s = s.substring(i + pattern.length());
        }

        result.append(s);
        return result.toString();
    }

    //--------------------------------------------------------------------------

    public static String getStackTrace(Throwable e) {
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));

        return sw.toString();
    }

    public static String getParamText(Element params, String desired) {
        String value = getParam(params, desired, "");
        if (value.length() == 0) {
            return null;
        }
        return value;
    }

    public static void toLowerCase(Element params) {
        for (Object o : params.getChildren()) {
            if (o instanceof Element) {
                Element element = (Element) o;
                element.setName(element.getName().toLowerCase());
            }
        }
    }

}

//=============================================================================

