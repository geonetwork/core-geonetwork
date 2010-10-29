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

package org.fao.geonet.services.schema;

import jeeves.exceptions.BadInputEx;
import jeeves.exceptions.BadParameterEx;
import jeeves.exceptions.OperationAbortedEx;
import jeeves.interfaces.Service;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import jeeves.utils.Util;
import jeeves.utils.XmlFileCacher;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.DataManager;
import org.jdom.Element;
import org.jdom.Namespace;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

//=============================================================================

public class Info implements Service {
	// --------------------------------------------------------------------------
	// ---
	// --- Init
	// ---
	// --------------------------------------------------------------------------

	public void init(String appPath, ServiceConfig params) throws Exception {
		this.appPath = appPath;
	}

	// --------------------------------------------------------------------------
	// ---
	// --- Service
	// ---
	// --------------------------------------------------------------------------

	public Element exec(Element params, ServiceContext context)
			throws Exception {
		GeonetContext gc = (GeonetContext) context
				.getHandlerContext(Geonet.CONTEXT_NAME);
		DataManager dm = gc.getDataManager();

		String langCode = context.getLanguage();

		Element response = new Element("response");

		for (Object o : params.getChildren()) {
			Element elem = (Element) o;
			String name = elem.getName();

			if (name.equals("element"))
				response.addContent(handleElement(dm, langCode, elem));

			else if (name.equals("codelist"))
				response.addContent(handleCodelist(dm, langCode, elem));

			else
				throw new BadParameterEx("element", name);
		}

		return response;
	}

	// --------------------------------------------------------------------------
	// ---
	// --- Private methods
	// ---
	// --------------------------------------------------------------------------

	private Element handleElement(DataManager dm, String langCode, Element elem)
			throws Exception {
		return handleObject(dm, langCode, elem, "labels.xml");
	}

	// --------------------------------------------------------------------------

	private Element handleCodelist(DataManager dm, String langCode, Element elem)
			throws Exception {
		return handleObject(dm, langCode, elem, "codelists.xml");
	}

	// --------------------------------------------------------------------------

	private Element handleObject(DataManager dm, String langCode, Element elem,
			String fileName) throws BadInputEx, OperationAbortedEx {
		String schema = Util.getAttrib(elem, "schema");
		String name = Util.getAttrib(elem, "name");
		String context = Util.getAttrib(elem, "context", "");
		String isoType = Util.getAttrib(elem, "isoType", "");
        String fullContext = Util.getAttrib(elem, "fullContext", "");

		name = normalizeNamespace(elem, name);
		context = normalizeNamespace(elem, context);
		isoType = normalizeNamespace(elem, isoType);
        fullContext = normalizeNamespace(elem, fullContext);

		if (name == null)
			return buildError(elem, UNKNOWN_NAMESPACE);

		if (!dm.existsSchema(schema))
			return buildError(elem, UNKNOWN_SCHEMA);

        return getHelp(dm, langCode, elem, fileName, schema, name, context, fullContext,
				isoType);
	}

	private Element getHelp(DataManager dm, String langCode, Element elem,
            String fileName, String schema, String name, String context,  String fullContext,
			String isoType) throws BadInputEx, OperationAbortedEx {
		File file = getFile(langCode, schema, fileName);

		if (file == null)
			throw new OperationAbortedEx("File not found for : " + schema + "/"
					+ fileName);

		XmlFileCacher xfc = cache.get(file);

		if (xfc == null) {
			xfc = new XmlFileCacher(file);
			cache.put(file, xfc);
		}

		try {
			Element entries = xfc.get();

            // Check fullContext            
			for (Object o : entries.getChildren()) {
				Element currElem = (Element) o;
				String currName = currElem.getAttributeValue("name");
				String currContext = currElem.getAttributeValue("context");

				currName = normalizeNamespace(entries, currName);

				if (currName == null)
					throw new OperationAbortedEx("No namespace found for : "
							+ currName);

				if (currContext != null && context != null && isoType != null) {
					currContext = normalizeNamespace(entries, currContext);

					if (name.equals(currName)
							&& (fullContext.equals(currContext) || isoType
									.equals(currContext)))
						return (Element) currElem.clone();
				} else if (name.equals(currName)
                        && (currContext != null) && fullContext.equals(currContext))
					return (Element) currElem.clone();

			}

            // Check context
            for (Object o : entries.getChildren()) {
                Element currElem = (Element) o;
                String currName = currElem.getAttributeValue("name");
                String currContext = currElem.getAttributeValue("context");

                currName = normalizeNamespace(entries, currName);

                if (currName == null)
                    throw new OperationAbortedEx("No namespace found for : "
                            + currName);

                if (currContext != null && context != null && isoType != null) {
                    currContext = normalizeNamespace(entries, currContext);

                    if (name.equals(currName)
                            && (context.equals(currContext) || isoType
                                    .equals(currContext)))
                        return (Element) currElem.clone();
                } else if (name.equals(currName))
                    return (Element) currElem.clone();

            }

			if (schema.contains("iso19139") && !(schema.equals("iso19139"))) {
				return getHelp(dm, langCode, elem, fileName, "iso19139", name,
                        context, fullContext, isoType);
			} else
				return buildError(elem, NOT_FOUND);
		} catch (Exception e) {
            e.printStackTrace();
			throw new OperationAbortedEx("Can't load xml file : " + file
					+ " element name:" + name, e);
		}
	}

	// --------------------------------------------------------------------------

	private String normalizeNamespace(Element elem, String name) {
		int pos = name.indexOf(':');

		if (pos == -1)
			return name;

		String prefix = name.substring(0, pos);

		Namespace ns = elem.getNamespace(prefix);

		if (ns == null)
			return null;

		return ns.getURI() + name.substring(pos);
	}

	// --------------------------------------------------------------------------

	private File getFile(String langCode, String schema, String fileName) {
		File file = new File(appPath + "xml/schemas/" + schema + "/loc/"
				+ langCode + "/" + fileName);

		if (file.exists())
			return file;

		// --- let's try the default language 'en'
		file = new File(appPath + "xml/schemas/" + schema + "/loc/"
				+ Geonet.DEFAULT_LANGUAGE + "/" + fileName);

		if (file.exists())
			return file;

		return null;
	}

	// --------------------------------------------------------------------------

	private Element buildError(Element elem, String error) {
		elem = (Element) elem.clone();
		elem.setAttribute("error", error);

		return elem;
	}

	// --------------------------------------------------------------------------
	// ---
	// --- Variables
	// ---
	// --------------------------------------------------------------------------

	private static final String UNKNOWN_SCHEMA = "unknown-schema";
	private static final String UNKNOWN_NAMESPACE = "unknown-namespace";
	private static final String NOT_FOUND = "not-found";

	// --------------------------------------------------------------------------

	private String appPath;

	private Map<File, XmlFileCacher> cache = new HashMap<File, XmlFileCacher>();
}

// =============================================================================

