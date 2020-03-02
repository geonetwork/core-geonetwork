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

package org.fao.geonet.component.csw;

import jeeves.server.context.ServiceContext;

import org.fao.geonet.Util;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.csw.common.Csw;
import org.fao.geonet.csw.common.exceptions.CatalogException;
import org.fao.geonet.csw.common.exceptions.InvalidParameterValueEx;
import org.fao.geonet.csw.common.exceptions.NoApplicableCodeEx;
import org.fao.geonet.kernel.csw.CatalogConfiguration;
import org.fao.geonet.kernel.csw.CatalogService;
import org.fao.geonet.kernel.csw.services.AbstractOperation;
import org.fao.geonet.utils.Xml;
import org.jdom.Attribute;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

//=============================================================================
@Component(CatalogService.BEAN_PREFIX + DescribeRecord.NAME)
public class DescribeRecord extends AbstractOperation implements CatalogService {
    static final String NAME = "DescribeRecord";
    @Autowired
    private CatalogConfiguration _catalogConfig;

    //---------------------------------------------------------------------------
    //---
    //--- Constructor
    //---
    //---------------------------------------------------------------------------

    public DescribeRecord() {
    }

    //---------------------------------------------------------------------------
    //---
    //--- API methods
    //---
    //---------------------------------------------------------------------------

    public String getName() {
        return NAME;
    }

    //---------------------------------------------------------------------------

    public Element execute(Element request, ServiceContext context) throws CatalogException {
        checkService(request);
        checkVersion(request);

        String outputFormat = request.getAttributeValue("outputFormat");
        String schemaLanguage = request.getAttributeValue("schemaLanguage");

        if (outputFormat != null && !outputFormat.equals("application/xml"))
            throw new InvalidParameterValueEx("outputFormat", outputFormat);

        if (schemaLanguage != null
            && !schemaLanguage.equals(Csw.SCHEMA_LANGUAGE))
            throw new InvalidParameterValueEx("schemaLanguage", schemaLanguage);

        //--- build output

        Element response = new Element(getName() + "Response", Csw.NAMESPACE_CSW);
        response.addNamespaceDeclaration(Csw.NAMESPACE_CSW);
        Attribute schemaLocation = new Attribute("schemaLocation", "http://www.opengis.net/cat/csw/2.0.2 http://schemas.opengis.net/csw/2.0.2/CSW-discovery.xsd", Csw.NAMESPACE_XSI);
        response.setAttribute(schemaLocation);


        @SuppressWarnings("unchecked")
        Iterator<Element> i = request.getChildren("TypeName", Csw.NAMESPACE_CSW).iterator();

        Map<String, Element> scMap = new HashMap<String, Element>();
        // default search without typename
        if (!i.hasNext())
            scMap = getSchemaComponents(context, null);
        else {
            while (i.hasNext()) {

                Element elem = i.next();

                String typeName = elem.getText();
                scMap.put(typeName, getSchemaComponents(context, typeName).get(typeName));
            }
        }


        for (Element schemaComponent : scMap.values()) {
            if (schemaComponent != null)
                response.addContent(schemaComponent);
        }

        return response;
    }

    //---------------------------------------------------------------------------

    public Element adaptGetRequest(Map<String, String> params) throws CatalogException {
        String service = params.get("service");
        String version = params.get("version");
        String outputFormat = params.get("outputformat");
        String schemaLang = params.get("schemalanguage");
        String typeNames = params.get("typename");
        String namespace = params.get("namespace");

        Element request = new Element(getName(), Csw.NAMESPACE_CSW);

        setAttrib(request, "service", service);
        setAttrib(request, "version", version);
        setAttrib(request, "outputFormat", outputFormat);
        setAttrib(request, "schemaLanguage", schemaLang);

        //--- setup type names
        if (typeNames != null && namespace == null)
            throw new InvalidParameterValueEx("Namespace", "Typename's namespace not declared for " + typeNames + ".");

        Map<String, String> hmTypeNames = retrieveTypeNames(typeNames, namespace);

        for (Map.Entry<String, String> entry : hmTypeNames.entrySet()) {
            Element el = new Element("TypeName", Csw.NAMESPACE_CSW);
            el.setText(entry.getKey());

            request.addContent(el);
        }

        return request;
    }

    //---------------------------------------------------------------------------

    public Element retrieveValues(String parameterName) throws CatalogException {

        Element listOfValues = null;

        if (parameterName.equalsIgnoreCase("outputformat")
            || parameterName.equalsIgnoreCase("namespace")
            || parameterName.equalsIgnoreCase("typename"))
            listOfValues = new Element("ListOfValues", Csw.NAMESPACE_CSW);

        // Handle outputFormat parameter
        if (parameterName.equalsIgnoreCase("outputformat")) {
            Set<String> formats = _catalogConfig
                .getDescribeRecordOutputFormat();
            List<Element> values = createValuesElement(formats);
            if (listOfValues != null) {
                listOfValues.addContent(values);
            }
        }

        // Handle namespace parameter
        if (parameterName.equalsIgnoreCase("namespace")) {
            Set<Namespace> namespaces = _catalogConfig
                .getDescribeRecordNamespaces();
            List<Element> values = createValuesElementNS(namespaces);
            if (listOfValues != null) {
                listOfValues.addContent(values);
            }
        }

        // Handle typename parameter
        if (parameterName.equalsIgnoreCase("typename")) {
            Set<String> typenames = _catalogConfig
                .getDescribeRecordTypename().keySet();
            List<Element> values = createValuesElement(typenames);
            if (listOfValues != null) {
                listOfValues.addContent(values);
            }
        }

        // TODO : Handle schemalanguage parameter

        return listOfValues;
    }

    //---------------------------------------------------------------------------
    //---
    //--- Private methods
    //---
    //---------------------------------------------------------------------------

    private HashMap<String, Element> getSchemaComponents(ServiceContext context, String typeName)
        throws NoApplicableCodeEx, InvalidParameterValueEx {

        HashMap<String, Element> scElements = new HashMap<>();

        if (typeName == null) {
            for (String tname : _catalogConfig.getDescribeRecordTypename().keySet()) {
                String schema = _catalogConfig.getDescribeRecordTypename().get(tname);
                try {
                    Element currentSC = loadSchemaComponent(context, tname, schema);
                    scElements.put(tname, currentSC);
                } catch (Exception ex) {
                    context.warning("Error while getting schema " + tname + " (" + schema+"): " + ex.getMessage());
                }
            }
        } else {
            if (_catalogConfig.getDescribeRecordTypename().containsKey(typeName)) {
                scElements.put(typeName, loadSchemaComponent(context, typeName,
                    _catalogConfig.getDescribeRecordTypename().get(typeName)));
            }
//		  CSW 2.0.2 testsuite csw:csw-2.0.2-DescribeRecord-tc3.1:
//		  "The response to a DescribeRecord request that contains an unknown TypeName
//		    element must not include any csw:SchemaComponent elements."
//
//			Previous behaviour:
//		else {
//			throw new InvalidParameterValueEx("TypeName", "Can't load typename " + typeName + " from CSW catalogue configuration.");
//		}

//		  CSW 2.0.2 testsuite csw:csw-2.0.2-DescribeRecord-tc7.1:
//	      "Pass if all of the following conditions are true: (1) the response
//	      entity has &lt;ows:ExceptionReport&gt; as the document element; and (2)
//	      ows:Exception/@exceptionCode="InvalidParameterValuePhase" (csw:TypeName not qualified)."
            else if (!typeName.contains(":")) {
                throw new InvalidParameterValueEx("TypeName", "csw:TypeName not qualified for typename: " + typeName);
            }
            // Return no exception but an empty DescribeRecordResponse if no typename found
        }
        return scElements;
    }

    //---------------------------------------------------------------------------

    private Element loadSchemaComponent(ServiceContext context, String tname, String schemafile)
        throws NoApplicableCodeEx {

        Path dir = context.getAppPath().resolve(Geonet.Path.VALIDATION).resolve("csw202_apiso100/csw/2.0.2/");

        try {
            Element schema = Xml.loadFile(dir.resolve(schemafile));
            Element sc = new Element("SchemaComponent", Csw.NAMESPACE_CSW);

            // Add required attributes to SchemaComponent
            sc.setAttribute("targetNamespace", Csw.NAMESPACE_CSW.getURI());
            // (optional) sc.setAttribute("parentSchema",    "?");
            sc.setAttribute("schemaLanguage", Csw.SCHEMA_LANGUAGE);

            sc.addContent(schema);
            return sc;

        } catch (Throwable e) {
            context.error("Cannot get schema file : " + dir);
            context.error("  (C) StackTrace\n" + Util.getStackTrace(e));

            throw new NoApplicableCodeEx("Cannot get schema file for : " + tname);
        }
    }

    //---------------------------------------------------------------------------
}

//=============================================================================

