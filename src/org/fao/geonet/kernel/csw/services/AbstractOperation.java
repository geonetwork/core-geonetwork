//=============================================================================
//===	Copyright (C) 2001-2005 Food and Agriculture Organization of the
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
//===	Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
//===
//===	Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
//===	Rome - Italy. email: GeoNetwork@fao.org
//==============================================================================

package org.fao.geonet.kernel.csw.services;

import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import org.fao.geonet.csw.common.Csw;
import org.fao.geonet.csw.common.Csw.ElementSetName;
import org.fao.geonet.csw.common.exceptions.CatalogException;
import org.fao.geonet.csw.common.exceptions.InvalidParameterValueEx;
import org.jdom.Element;
import org.jdom.Namespace;

//=============================================================================

public abstract class AbstractOperation
{
	//---------------------------------------------------------------------------
	//---
	//--- API methods
	//---
	//---------------------------------------------------------------------------

	protected boolean checkService(Element request) throws CatalogException
	{
		String service = request.getAttributeValue("service");

		if (service == null)
			return false;

		if (service.equals(Csw.SERVICE))
			return true;

		//--- this is just a fix to the incorrect XSD schema default

		if (service.equals("http://www.opengis.net/cat/csw"))
			return true;

		throw new InvalidParameterValueEx("service", service);
	}

	//---------------------------------------------------------------------------

	protected void checkVersion(Element request) throws CatalogException
	{
		String version = request.getAttributeValue("version");

		if (version == null)
			return;

		if (!version.equals(Csw.CSW_VERSION))
			throw new InvalidParameterValueEx("version", version);
	}

	//---------------------------------------------------------------------------

	protected void setAttrib(Element elem, String name, String value)
	{
		if (value != null)
			elem.setAttribute(name, value);
	}

	//---------------------------------------------------------------------------

	protected void addElement(Element parent, String name, String value)
	{
		if (value != null)
		{
			Element elem = new Element(name, parent.getNamespace());
			elem.setText(value);
			parent.addContent(elem);
		}
	}

	//---------------------------------------------------------------------------

	protected void fill(Element root, String parentName, String childName, String list, Namespace ns)
	{
		if (list == null)
			return;

		StringTokenizer st = new StringTokenizer(list, ",");

		Element parent = new Element(parentName, ns);
		root.addContent(parent);

		while (st.hasMoreTokens())
		{
			Element child = new Element(childName, ns);
			child.setText(st.nextToken());
			parent.addContent(child);
		}
	}

	//---------------------------------------------------------------------------

	protected void fill(Element root, String childName, String list)
	{
		if (list == null)
			return;

		StringTokenizer st = new StringTokenizer(list, ",");

		while (st.hasMoreTokens())
		{
			Element child = new Element(childName, root.getNamespace());
			child.setText(st.nextToken());
			root.addContent(child);
		}
	}

	//---------------------------------------------------------------------------

	protected ElementSetName getElementSetName(Element parent) throws InvalidParameterValueEx
	{
		if (parent == null)
			return ElementSetName.FULL;

		return ElementSetName.parse(parent.getChildText("ElementSetName", parent.getNamespace()));
	}

	//---------------------------------------------------------------------------

	protected Map<String, String> retrieveNamespaces(String namespaces)
	{
		HashMap<String, String> hm = new HashMap<String, String>();

		if (namespaces != null)
		{
			StringTokenizer st = new StringTokenizer(namespaces, ",");

			while (st.hasMoreTokens())
			{
				String ns  = st.nextToken();
				int    pos = ns.indexOf(":");

				if (pos == -1)
					hm.put("", ns);
				else
				{
					String prefix = ns.substring(0, pos);
					String uri    = ns.substring(pos +1);

					hm.put(prefix, uri);
				}
			}
		}

		return hm;
	}

	//---------------------------------------------------------------------------
	/** @return For earch typeName returns the associated namespace */

	protected Map<String, String> retrieveTypeNames(String typeNames, String namespace)
																	throws InvalidParameterValueEx
	{
		Map<String, String> hmTypeNames  = new HashMap<String, String>();
		Map<String, String> hmNamespaces = retrieveNamespaces(namespace);

		if (typeNames != null)
		{
			StringTokenizer st = new StringTokenizer(typeNames, ",");

			while(st.hasMoreTokens())
			{
				String typeName = st.nextToken();
				int    pos      = typeName.indexOf(":");
				String prefix   = "";
				String type     = typeName;

				if (pos != -1)
				{
					prefix = typeName.substring(0, pos);
					type   = typeName.substring(pos +1);
				}

				String ns= hmNamespaces.get(prefix);

				if (ns == null)
					throw new InvalidParameterValueEx("typeName", typeName);

				hmTypeNames.put(type, ns);
			}
		}

		return hmTypeNames;
	}
}

//=============================================================================

