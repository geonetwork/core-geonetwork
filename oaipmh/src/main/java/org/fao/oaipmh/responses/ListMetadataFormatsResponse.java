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

package org.fao.oaipmh.responses;

import java.util.ArrayList;
import java.util.List;
import org.fao.oaipmh.OaiPmh;
import org.fao.oaipmh.requests.ListMetadataFormatsRequest;
import org.jdom.Element;

//=============================================================================

public class ListMetadataFormatsResponse extends AbstractResponse
{
	//---------------------------------------------------------------------------
	//---
	//--- Constructor
	//---
	//---------------------------------------------------------------------------

	public ListMetadataFormatsResponse() {}

	//---------------------------------------------------------------------------

	public ListMetadataFormatsResponse(Element response)
	{
		super(response);
		build(response);
	}

	//---------------------------------------------------------------------------
	//---
	//--- API methods
	//---
	//---------------------------------------------------------------------------

	public Iterable<MetadataFormat> getFormats() { return formats; }

	//---------------------------------------------------------------------------

	public void clearFormats() { formats.clear(); }

	//---------------------------------------------------------------------------

	public void addFormat(MetadataFormat mf)
	{
		formats.add(mf);
	}

	//---------------------------------------------------------------------------

	public Element toXml()
	{
		Element root = new Element(ListMetadataFormatsRequest.VERB, OaiPmh.Namespaces.OAI_PMH);

		for (MetadataFormat mf : formats)
			root.addContent(mf.toXml());

		return root;
	}

	//---------------------------------------------------------------------------
	//---
	//--- Private methods
	//---
	//---------------------------------------------------------------------------

	private void build(Element response)
	{
		Element listMdFor = response.getChild("ListMetadataFormats", OaiPmh.Namespaces.OAI_PMH);

		List mdFormats = listMdFor.getChildren("metadataFormat", OaiPmh.Namespaces.OAI_PMH);

		for (Object o : mdFormats)
			formats.add(new MetadataFormat((Element) o));
	}

	//---------------------------------------------------------------------------
	//---
	//--- Variables
	//---
	//---------------------------------------------------------------------------

	private List<MetadataFormat> formats = new ArrayList<MetadataFormat>();
}

//=============================================================================

