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

package org.fao.geonet.csw.common.requests;

import java.util.ArrayList;
import java.util.HashSet;
import org.fao.geonet.csw.common.Csw;
import org.fao.geonet.csw.common.Csw.Section;
import org.fao.geonet.csw.common.http.HttpRequest;
import org.jdom.Element;

//=============================================================================

/** Params:
  *  - sections       (0..n)
  *  - updateSequence (0..1)
  *  - acceptFormats  (0..n)
  *  - acceptVersions (0..n)
  */

public class GetCapabilitiesRequest extends CatalogRequest
{
	private String sequence;

	private ArrayList<String> alVersions = new ArrayList<String>();
	private ArrayList<String> alFormats  = new ArrayList<String>();
	private HashSet<Section>  hsSections = new HashSet<Section>();

	//---------------------------------------------------------------------------
	//---
	//--- Constructor
	//---
	//---------------------------------------------------------------------------

	public GetCapabilitiesRequest() {}

	//---------------------------------------------------------------------------
	//---
	//--- API methods
	//---
	//---------------------------------------------------------------------------

	public void addVersion(String version)
	{
		alVersions.add(version);
	}

	//---------------------------------------------------------------------------

	public void addSection(Section section)
	{
		hsSections.add(section);
	}

	//---------------------------------------------------------------------------

	public void addOutputFormat(String format)
	{
		alFormats.add(format);
	}

	//---------------------------------------------------------------------------

	public void setUpdateSequence(String sequence)
	{
		this.sequence = sequence;
	}

	//---------------------------------------------------------------------------
	//---
	//--- Protected methods
	//---
	//---------------------------------------------------------------------------

	protected String getRequestName() { return "GetCapabilities"; }

	//---------------------------------------------------------------------------

	protected void setupGetParams(HttpRequest request)
	{
		request.addParam("request", getRequestName());
		request.addParam("service", Csw.SERVICE);

		if (sequence != null)
			request.addParam("updateSequence", sequence);

		fill(request, "acceptVersions", alVersions);
		fill(request, "sections",       hsSections);
		fill(request, "acceptFormats",  alFormats);
	}

	//---------------------------------------------------------------------------

	protected void setupPostParams(HttpRequest request)
	{
		Element params  = new Element(getRequestName(), Csw.NAMESPACE_CSW);

		params.setAttribute("service", Csw.SERVICE);

		if (sequence != null)
			params.setAttribute("updateSequence", sequence);

		fill(params, "AcceptVersions", "Version",      alVersions, Csw.NAMESPACE_OWS);
		fill(params, "Sections",       "Section",      hsSections, Csw.NAMESPACE_OWS);
		fill(params, "AcceptFormats",  "OutputFormat", alFormats,  Csw.NAMESPACE_OWS);

		request.setParams(params);
	}
}

//=============================================================================

