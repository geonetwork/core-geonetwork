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

import java.io.File;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import jeeves.server.context.ServiceContext;
import jeeves.utils.Util;
import jeeves.utils.Xml;
import org.fao.geonet.csw.common.Csw;
import org.fao.geonet.csw.common.exceptions.CatalogException;
import org.fao.geonet.csw.common.exceptions.NoApplicableCodeEx;
import org.fao.geonet.csw.common.exceptions.VersionNegotiationFailedEx;
import org.fao.geonet.kernel.csw.CatalogService;
import org.jdom.Element;

//=============================================================================

public class GetCapabilities extends AbstractOperation implements CatalogService
{
	//---------------------------------------------------------------------------
	//---
	//--- Constructor
	//---
	//---------------------------------------------------------------------------

	public GetCapabilities() {}

	//---------------------------------------------------------------------------
	//---
	//--- API methods
	//---
	//---------------------------------------------------------------------------

	public String getName() { return "GetCapabilities"; }

	//---------------------------------------------------------------------------

	public Element execute(Element request, ServiceContext context) throws CatalogException
	{
		checkService(request);
		checkAcceptVersions(request);

		//--- return capabilities

		String FS   = File.separator;
		String file = context.getAppPath() +"xml"+ FS +"csw"+ FS +"capabilities.xml";

		try
		{
			Element capabilities = Xml.loadFile(file);
			handleSections(request, capabilities);

			return capabilities;
		}
		catch (Exception e)
		{
			context.error("Cannot load/process capabilities");
			context.error("  (C) StackTrace\n"+ Util.getStackTrace(e));

			throw new NoApplicableCodeEx("Cannot load/process capabilities");
		}
	}

	//---------------------------------------------------------------------------

	public Element adaptGetRequest(Map<String, String> params)
	{
		String service    = params.get("service");
		String sections   = params.get("sections");
		String sequence   = params.get("updatesequence");
		String acceptVers = params.get("acceptversions");
		String acceptForm = params.get("acceptformats");

		Element request = new Element(getName(), Csw.NAMESPACE_CSW);

		setAttrib(request, "service",        service);
		setAttrib(request, "updateSequence", sequence);

		fill(request, "AcceptVersions", "Version",      acceptVers, Csw.NAMESPACE_OWS);
		fill(request, "Sections",       "Section",      sections,   Csw.NAMESPACE_OWS);
		fill(request, "AcceptFormats",  "OutputFormat", acceptForm, Csw.NAMESPACE_OWS);

		return request;
	}

	//---------------------------------------------------------------------------
	//---
	//--- Private methods
	//---
	//---------------------------------------------------------------------------

	private void checkAcceptVersions(Element request) throws CatalogException
	{
		Element versions = request.getChild("AcceptVersions", Csw.NAMESPACE_OWS);

		if (versions == null)
			return;

		Iterator i = versions.getChildren().iterator();

		StringBuffer sb = new StringBuffer();

		while (i.hasNext())
		{
			Element version = (Element) i.next();

			if (version.getText().equals(Csw.CSW_VERSION))
				return;

			sb.append(version.getText());

			if (i.hasNext())
				sb.append(",");
		}

		throw new VersionNegotiationFailedEx(sb.toString());
	}

	//---------------------------------------------------------------------------

	private void handleSections(Element request, Element capabilities)
	{
		Element sections = request.getChild("Sections", Csw.NAMESPACE_OWS);

		if (sections == null)
			return;

		//--- handle 'section' parameters

		HashSet<String> hsSections = new HashSet<String>();

		Iterator i = sections.getChildren().iterator();

		while(i.hasNext())
		{
			Element section = (Element) i.next();
			hsSections.add(section.getText());
		}

		//--- remove not requested sections

		if (!hsSections.contains("ServiceIdentification"))
			capabilities.getChild("ServiceIdentification", Csw.NAMESPACE_OWS).detach();

		if (!hsSections.contains("ServiceProvider"))
			capabilities.getChild("ServiceProvider", Csw.NAMESPACE_OWS).detach();

		if (!hsSections.contains("OperationsMetadata"))
			capabilities.getChild("OperationsMetadata", Csw.NAMESPACE_OWS).detach();

		if (!hsSections.contains("Filter_Capabilities"))
			capabilities.getChild("Filter_Capabilities", Csw.NAMESPACE_OGC).detach();
	}
}

//=============================================================================

