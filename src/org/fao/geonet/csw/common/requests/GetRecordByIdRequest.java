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
import org.fao.geonet.csw.common.Csw;
import org.fao.geonet.csw.common.Csw.ElementSetName;
import org.jdom.Element;

//=============================================================================

/** Params:
  *  - elementSetName (0..1) Can be 'brief', 'summary', 'full'. Default is 'summary'
  *  - id             (1..n)
  */

public class GetRecordByIdRequest extends CatalogRequest
{
	private ElementSetName setName;

	private ArrayList<String> alIds = new ArrayList<String>();

	//---------------------------------------------------------------------------
	//---
	//--- Constructor
	//---
	//---------------------------------------------------------------------------

	public GetRecordByIdRequest() {}

	//---------------------------------------------------------------------------
	//---
	//--- API methods
	//---
	//---------------------------------------------------------------------------

	public void setElementSetName(ElementSetName name)
	{
		setName = name;
	}

	//---------------------------------------------------------------------------

	public void addId(String id)
	{
		alIds.add(id);
	}

	//---------------------------------------------------------------------------
	//---
	//--- Protected methods
	//---
	//---------------------------------------------------------------------------

	protected String getRequestName() { return "GetRecordById"; }

	//---------------------------------------------------------------------------

	protected void setupGetParams()
	{
		addParam("request", getRequestName());
		addParam("service", Csw.SERVICE);
		addParam("version", Csw.CSW_VERSION);

		if (setName != null)
			addParam("elementSetName", setName);

		fill("id", alIds);
	}

	//---------------------------------------------------------------------------

	protected Element getPostParams()
	{
		Element params = new Element(getRequestName(), Csw.NAMESPACE_CSW);

		//--- 'service' and 'version' are common mandatory attributes
		params.setAttribute("service", Csw.SERVICE);
		params.setAttribute("version", Csw.CSW_VERSION);

		fill(params, "Id", alIds);

		if (setName != null)
		{
			Element elem = new Element("ElementSetName", Csw.NAMESPACE_CSW);
			elem.setText(setName.toString());

			params.addContent(elem);
		}

		return params;
	}
}

//=============================================================================

