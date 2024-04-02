//=============================================================================
//===	Copyright (C) 2001-2007 Food and Agriculture Organization of the
//===	United Nations (FAO-UN), United Nations World Food Programme (WFP)
//===	and United Nations Environment Programme (UNEP)
//===
//===	This program is free software; you can redistribute it and/or modify
//===	it under the terms of the GNU General Public License as published by
//===	the Free Software Foundation; either version 2 of the License, or (at
//===	your option) any later version.
//===
//===	This program is distributed in the hope that it will be useful, but
//===	WITHOUT ANY WARRANTY; without even the implied warranty of
//===	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
//===	General Public License for more details.
//===
//===	You should have received a copy of the GNU General Public License
//===	along with this program; if not, write to the Free Software
//===	Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
//===
//===	Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
//===	Rome - Italy. email: geonetwork@osgeo.org
//==============================================================================

package org.fao.geonet.kernel.csw.services;

import org.fao.geonet.utils.Log;
import org.fao.geonet.utils.Xml;
import org.apache.commons.lang.StringUtils;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.csw.common.Csw;
import org.fao.geonet.csw.common.ElementSetName;
import org.fao.geonet.csw.common.exceptions.CatalogException;
import org.fao.geonet.csw.common.exceptions.InvalidParameterValueEx;
import org.fao.geonet.csw.common.exceptions.MissingParameterValueEx;
import org.fao.geonet.csw.common.exceptions.NoApplicableCodeEx;
import org.geotools.filter.text.cql2.CQL;
import org.geotools.filter.text.cql2.CQLException;
import org.geotools.filter.v1_1.OGC;
import org.geotools.filter.v1_1.OGCConfiguration;
import org.geotools.xsd.Configuration;
import org.geotools.xsd.Encoder;
import org.jdom.Element;
import org.jdom.Namespace;
import org.geotools.api.filter.Filter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

/**
 *
 */
public abstract class AbstractOperation {
    //---------------------------------------------------------------------------
    //---
    //--- API methods
    //---
    //---------------------------------------------------------------------------

    /**
     * OGC 07-006 and OGC 07-045: Mandatory. Fixed value of “CSW”.
     */
    protected void checkService(Element request) throws MissingParameterValueEx, InvalidParameterValueEx {
        String service = request.getAttributeValue("service");
        if (StringUtils.isEmpty(service)) {
            throw new MissingParameterValueEx("service");
        }
        // TODO heikki: that csw namespace is not a valid value. Earlier comment here states: "//--- this is just a fix to the incorrect XSD schema default." Check if that's still necessary.
        if (!(service.equals(Csw.SERVICE) || service.equals("http://www.opengis.net/cat/csw"))) {
            throw new InvalidParameterValueEx("service", service);
        }
    }

    /**
     * OGC 07-006 and OGC 07-045: Mandatory. Fixed value of “2.0.2”.
     *
     * @param request the request
     */
    protected void checkVersion(Element request) throws MissingParameterValueEx, InvalidParameterValueEx {
        String version = request.getAttributeValue("version");
        if (StringUtils.isEmpty(version)) {
            throw new MissingParameterValueEx("version");
        }
        if (!version.equals(Csw.CSW_VERSION)) {
            throw new InvalidParameterValueEx("version", version);
        }
    }


    protected void setAttrib(Element elem, String name, String value) {
        if (value != null)
            elem.setAttribute(name, value);
    }

    //---------------------------------------------------------------------------

    protected void addElement(Element parent, String name, String value) {
        if (value != null) {
            Element elem = new Element(name, parent.getNamespace());
            elem.setText(value);
            parent.addContent(elem);
        }
    }

    //---------------------------------------------------------------------------

    protected void fill(Element root, String parentName, String childName, String list, Namespace ns) {
        if (list == null)
            return;

        StringTokenizer st = new StringTokenizer(list, ",");

        Element parent = new Element(parentName, ns);
        root.addContent(parent);

        while (st.hasMoreTokens()) {
            Element child = new Element(childName, ns);
            child.setText(st.nextToken());
            parent.addContent(child);
        }
    }

    //---------------------------------------------------------------------------

    protected void fill(Element root, String childName, String list) {
        if (list == null)
            return;

        StringTokenizer st = new StringTokenizer(list, ",");

        while (st.hasMoreTokens()) {
            Element child = new Element(childName, root.getNamespace());
            child.setText(st.nextToken());
            root.addContent(child);
        }
    }

    /**
     * Retrieves ElementSetName from a JDOM element if that is not null, otherwise returns the
     * provided default value.
     */
    protected ElementSetName getElementSetName(Element parent, ElementSetName defValue) throws InvalidParameterValueEx {
        if (parent == null) {
            return defValue;
        }
        return ElementSetName.parse(parent.getChildText("ElementSetName", parent.getNamespace()));
    }

    protected Map<String, String> retrieveNamespaces(String namespaces) {
        Map<String, String> hm = new HashMap<String, String>();

        if (namespaces != null) {
            StringTokenizer st = new StringTokenizer(namespaces, ",");

            while (st.hasMoreTokens()) {
                String ns = st.nextToken();
                int pos = ns.indexOf(':');

                if (pos == -1)
                    hm.put("", ns);
                else {
                    String prefix = ns.substring(0, pos);
                    String uri = ns.substring(pos + 1);

                    hm.put(prefix, uri);
                }
            }
        }

        return hm;
    }

    //---------------------------------------------------------------------------

    /**
     * @return For earch typeName returns the associated namespace
     */

    protected Map<String, String> retrieveTypeNames(String typeNames, String namespace)
        throws InvalidParameterValueEx {
        Map<String, String> hmTypeNames = new HashMap<String, String>();
        Map<String, String> hmNamespaces = retrieveNamespaces(namespace);

        if (typeNames != null) {
            StringTokenizer st = new StringTokenizer(typeNames, ",");

            while (st.hasMoreTokens()) {
                String typeName = st.nextToken();
                int pos = typeName.indexOf(':');
                String prefix = "";

                if (pos != -1) {
                    prefix = typeName.substring(0, pos);
                }

                String ns = hmNamespaces.get(prefix);

                if (ns == null)
                    throw new InvalidParameterValueEx("typeName", "Can't find a valid namespace for typename " + typeName + ". Check namespace parameter.");

                hmTypeNames.put(typeName, ns);
            }
        }

        return hmTypeNames;
    }

    //---------------------------------------------------------------------------

    /**
     * Create value element for each item of the string list
     */
    protected List<Element> createValuesElement(Collection<String> param) {
        List<Element> values = new ArrayList<Element>();
        for (String value : param) {
            values.add(new Element("Value", Csw.NAMESPACE_CSW).setText(value));
        }
        return values;
    }

    //---------------------------------------------------------------------------

    /**
     * Create value element for each item of the namespace list
     */
    protected List<Element> createValuesElementNS(Collection<Namespace> param) {
        List<Element> values = new ArrayList<Element>();
        for (Namespace ns : param) {
            String value = ns.getURI();
            values.add(new Element("Value", Csw.NAMESPACE_CSW).setText(value));
        }
        return values;
    }

    //---------------------------------------------------------------------------

    /**
     * @param constr
     * @return
     * @throws CatalogException
     */
    protected Element getFilterExpression(Element constr)
        throws CatalogException {

        if (constr == null)
            return null;

        Element filter = constr.getChild("Filter", Csw.NAMESPACE_OGC);
        Element cql = constr.getChild("CqlText", Csw.NAMESPACE_CSW);

        if (filter != null) {
            return filter;
        } else {
            if (cql != null) {
                return convertCQL(cql.getText());
            } else {
                return null;
            }
        }
    }


    //---------------------------------------------------------------------------

    /**
     * @param cql
     * @return
     * @throws CatalogException
     */
    private Element convertCQL(String cql) throws CatalogException {

        if (Log.isDebugEnabled(Geonet.CSW))
            Log.debug(Geonet.CSW, "Received CQL:\n" + cql);

        Filter filter;
        try {
            filter = CQL.toFilter(cql);
        } catch (CQLException e) {
            Log.error(Geonet.CSW, "Error parsing CQL or during conversion into Filter");
            throw new NoApplicableCodeEx("Error during CQL to Filter conversion : " + e);
        }

        final Configuration filter110Config = new OGCConfiguration();
        final Encoder encoder = new Encoder(filter110Config);

        final Charset charset = Charset.forName("UTF-16");
        encoder.setEncoding(charset);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        String xml;
        try {
            encoder.encode(filter, OGC.Filter, out);
            xml = new String(out.toByteArray(), charset.toString());
        } catch (IOException e) {
            Log.error(Geonet.CSW, e.getMessage());
            throw new NoApplicableCodeEx("Error transforming Filter to XML" + e);
        }

        Element xmlFilter;
        try {
            xmlFilter = Xml.loadString(xml, false);
        } catch (Exception e) {
            Log.error(Geonet.CSW, "Error loading xml filter as jdom Element ");
            throw new NoApplicableCodeEx("Error loading xml filter as jdom Element " + e);
        }

        if (Log.isDebugEnabled(Geonet.CSW))
            Log.debug(Geonet.CSW, "Transformed CQL gives the following filter:\n" + Xml.getString(xmlFilter));

        return xmlFilter;
    }

    //---------------------------------------------------------------------------

    /**
     * @param constr
     * @return
     * @throws CatalogException
     */
    protected String getFilterVersion(Element constr) throws CatalogException {
        if (constr == null)
            return Csw.FILTER_VERSION_1_1;
        String version = constr.getAttributeValue("version");
        if (version == null)
            throw new MissingParameterValueEx("CONSTRAINT_LANGUAGE_VERSION");

        // Check version in both cas (CQL or filter) in order to specify parser
        // version.
        if (!version.equals(Csw.FILTER_VERSION_1_0)
            && !version.equals(Csw.FILTER_VERSION_1_1))
            throw new InvalidParameterValueEx("version", version + ". Supported versions are "
                + Csw.FILTER_VERSION_1_0 + "," + Csw.FILTER_VERSION_1_1 + ".");

        return version;
    }
}

//=============================================================================

