//=============================================================================
//===	Copyright (C) 2001-2025 Food and Agriculture Organization of the
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
//===	Rome - Italy. email: geonetwork@osgeo.org
//==============================================================================

package org.fao.geonet;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang.StringUtils;
import org.fao.geonet.exceptions.BadInputEx;
import org.fao.geonet.exceptions.BadParameterEx;
import org.fao.geonet.exceptions.MissingParameterEx;
import org.jdom.Element;


/**
 * Utility class providing various static helper methods for working with XML elements,
 * attributes, parameters, HTTP headers, and string manipulations.
 * This class is a non-instantiable utility class and cannot be instantiated.
 */
public final class Util {
    /**
     * A utility class that provides static methods and fields.
     * This class is not intended to be instantiated, hence the private constructor.
     */
    private Util() {
    }

    /**
     * Retrieves a child element with the specified name from the given XML element.
     * If the child element is not found, a MissingParameterEx exception is thrown.
     *
     * @param el   The parent XML element to search for the child element. Must not be null.
     * @param name The name of the child element to retrieve. Must not be null.
     * @return The child element with the specified name, if found.
     * @throws MissingParameterEx If the child element with the specified name is not found.
     */
    public static Element getChild(Element el, String name) throws MissingParameterEx {
        Element param = el.getChild(name);

        if (param == null)
            throw new MissingParameterEx(name, el);

        return param;
    }


    /**
     * Retrieves the value of the specified parameter from an XML element.
     * If the element is null, a MissingParameterEx is thrown.
     * If the parameter does not exist or its value is empty, appropriate exceptions are thrown.
     *
     * @param el   The XML element containing the desired parameter. Must not be null.
     * @param name The name of the parameter to retrieve.
     * @return The trimmed text value of the parameter.
     * @throws MissingParameterEx If the element is null or the parameter is not found in the element.
     * @throws BadParameterEx     If the parameter exists but its value is empty.
     */
    public static String getParam(Element el, String name) throws BadInputEx {
        if (el == null)
            throw new MissingParameterEx(name);

        Element param = el.getChild(name);

        if (param == null)
            throw new MissingParameterEx(name, el);

        String value = param.getTextTrim();

        if (value.isEmpty())
            throw new BadParameterEx(name, value);

        return value;
    }

    /**
     * Extracts a list of parameter values from the provided XML element based on the specified parameter name.
     * If the element is null, throws a MissingParameterEx. If any parameter value is empty, throws a BadParameterEx.
     *
     * @param el   The XML element containing the parameters. Must not be null.
     * @param name The name of the parameter whose values are to be extracted.
     * @return A list of non-empty parameter values associated with the specified name.
     * @throws BadInputEx If the element is null, or if a parameter value is invalid (e.g., empty).
     */
    public static List<String> getParams(Element el, String name) throws BadInputEx {
        if (el == null)
            throw new MissingParameterEx(name);

        List<String> values = new LinkedList<>();

        for (Object obj : el.getChildren(name)) {
            Element param = (Element) obj;

            String value = param.getTextTrim();

            if (value.isEmpty())
                throw new BadParameterEx(name, value);

            values.add(value);
        }

        return values;
    }


    /**
     * Retrieves the value of the specified parameter from the provided XML element.
     * If the parameter does not exist or its value is empty, the default value is returned.
     *
     * @param el       The XML element to retrieve the parameter from. Can be null.
     * @param name     The name of the parameter to retrieve.
     * @param defValue The default value to return if the parameter is not found or is empty.
     * @return The value of the specified parameter, or the default value if the parameter
     * is not found or its value is empty.
     */
    public static String getParam(Element el, String name, String defValue) {
        if (el == null)
            return defValue;

        Element param = el.getChild(name);

        if (param == null)
            return defValue;

        String value = param.getTextTrim();

        if (value.isEmpty())
            return defValue;

        return value;
    }


    /**
     * Retrieves the value of the specified parameter from an XML element and converts it to an integer.
     * If the parameter is not found or its value is invalid (e.g., non-numeric), an exception is thrown.
     *
     * @param el   The XML element containing the desired parameter. Must not be null.
     * @param name The name of the parameter to retrieve and convert to an integer.
     * @return The integer value of the specified parameter.
     * @throws BadInputEx If the parameter is not found, is empty, or cannot be converted to an integer.
     */
    public static int getParamAsInt(Element el, String name) throws BadInputEx {
        String value = getParam(el, name);

        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new BadParameterEx(name, value);
        }
    }

    /**
     * Extracts a list of parameter values from the provided XML element based on the specified parameter name
     * and converts them into integers. If any value cannot be converted to an integer, a BadInputEx exception is thrown.
     *
     * @param el   The XML element containing the parameters. Must not be null.
     * @param name The name of the parameter whose values are to be extracted and converted to integers.
     * @return A list of integer values parsed from the parameters associated with the specified name.
     * @throws BadInputEx If the element is null, if a parameter value is invalid (e.g., not a number),
     *                    or if a parameter has an empty value.
     */
    public static List<Integer> getParamsAsInt(Element el, String name) throws BadInputEx {

        List<Integer> values = new LinkedList<>();
        for (String value : getParams(el, name)) {
            try {
                values.add(Integer.parseInt(value));
            } catch (NumberFormatException e) {
                throw new BadParameterEx(name, value);
            }
        }

        return values;
    }


    /**
     * Retrieves the value of the specified parameter from the provided XML element and converts it to an integer.
     * If the parameter does not exist or its value is invalid (e.g., non-numeric), the default value is returned
     * or an exception is thrown depending on the situation.
     *
     * @param el       The XML element to retrieve the parameter from. Can be null.
     * @param name     The name of the parameter to retrieve and convert to an integer. Must not be null.
     * @param defValue The integer default value to return if the parameter is not found or its value is invalid
     */
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


    /**
     * Retrieves the boolean value of the specified parameter from the given XML element.
     * If the parameter is not found or its value does not match common boolean representations,
     * the default value is returned or an exception is thrown if the value is invalid.
     *
     * @param el       The XML element to retrieve the parameter from. Can be null.
     * @param name     The name of the parameter to retrieve.
     * @param defValue The default boolean value to return if the parameter is not found.
     * @return The boolean value of the specified parameter if it matches recognized boolean
     * representations ("true", "on", "yes" for true; "false", "off", "no" for false),
     * or the default value if the parameter is not found.
     * @throws BadParameterEx If the parameter value is neither null nor a recognized boolean representation.
     */
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


    /**
     * Retrieves the trimmed value of the specified attribute from the given XML element.
     * If the attribute is not found or its value is empty, an appropriate exception is thrown.
     *
     * @param el   The XML element containing the attribute. Must not be null.
     * @param name The name of the attribute to retrieve. Must not be null.
     * @return The trimmed value of the specified attribute.
     * @throws MissingParameterEx If the attribute with the specified name is not found in the element.
     * @throws BadParameterEx     If the attribute exists but its value is empty.
     */
    public static String getAttrib(Element el, String name) throws BadInputEx {
        String value = el.getAttributeValue(name);

        if (value == null)
            throw new MissingParameterEx("attribute:" + name, el);

        value = value.trim();

        if (value.isEmpty())
            throw new BadParameterEx("attribute:" + name, value);

        return value;
    }


    /**
     * Retrieves the trimmed value of the specified attribute from the given XML element.
     * If the attribute is not found or is null/empty, the provided default value is returned.
     *
     * @param el       The XML element containing the attribute. Can be null.
     * @param name     The name of the attribute to retrieve. Must not be null.
     * @param defValue The default value to return if the attribute is not found or is empty.
     * @return The trimmed value of the specified attribute, or the provided default value
     * if the attribute is not found or its value is empty.
     */
    public static String getAttrib(Element el, String name, String defValue) {
        if (el == null)
            return defValue;

        String value = el.getAttributeValue(name);
        if (StringUtils.isBlank(value)) {
            return defValue;
        }

        return value.trim();
    }

    /**
     * Retrieves the value of a specified header by its name from the provided headers map.
     * If the header is not found or its value is blank, the default value is returned.
     *
     * @param headers  A map containing header names and values. Can be null.
     * @param name     The name of the header to retrieve. Must not be null.
     * @param defValue The default value to return if the header is not found or its value is blank.
     * @return The value of the specified header if found and not blank, or the default value otherwise.
     */
    public static String getHeader(Map<String, String> headers, String name, String defValue) {
        if (headers == null)
            return defValue;

        String value = headers.get(name);
        return StringUtils.defaultIfBlank(value, defValue);
    }


    /**
     * Replaces all occurrences of a specified substring (pattern) in the input string
     * with a given replacement string.
     *
     * @param original    The original string in which replacements are to be made.
     * @param pattern     The substring that will be replaced.
     * @param replacement The string to replace each occurrence of the pattern.
     * @return A new string with all occurrences of the pattern replaced by the replacement string.
     */
    public static String replaceString(String original, String pattern, String replacement) {
        return StringUtils.replace(original, pattern, replacement);
    }


    /**
     * Converts the stack trace of a given {@link Throwable} into a string representation.
     *
     * @param e The {@link Throwable} whose stack trace is to be converted. Must not be null.
     * @return A string representation of the stack trace of the provided {@link Throwable}.
     */
    public static String getStackTrace(Throwable e) {
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));

        return sw.toString();
    }

    /**
     * Retrieves the text value of a specified parameter from the given XML element.
     * If the parameter is not found or its value is empty, returns null.
     *
     * @param params  The XML element containing the parameters. Can be null.
     * @param desired The name of the parameter to retrieve the text for. Must not be null.
     * @return The text value of the specified parameter, or null if it is not found or its value is empty.
     */
    public static String getParamText(Element params, String desired) {
        String value = getParam(params, desired, "");
        if (value.isEmpty()) {
            return null;
        }
        return value;
    }

    /**
     * Converts the names of all child elements of the given Element to lowercase.
     *
     * @param params the XML Element whose child elements' names will be converted to lowercase
     */
    public static void toLowerCase(Element params) {
        for (Object o : params.getChildren()) {
            if (o instanceof Element) {
                Element element = (Element) o;
                element.setName(element.getName().toLowerCase());
            }
        }
    }
}

