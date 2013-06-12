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

package org.fao.geonet.kernel.oaipmh.services;

import jeeves.resources.dbms.Dbms;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.oaipmh.OaiPmhService;
import org.fao.oaipmh.exceptions.BadResumptionTokenException;
import org.fao.oaipmh.requests.AbstractRequest;
import org.fao.oaipmh.requests.ListSetsRequest;
import org.fao.oaipmh.responses.AbstractResponse;
import org.fao.oaipmh.responses.ListSetsResponse;
import org.fao.oaipmh.responses.SetInfo;
import org.jdom.Element;

import java.util.List;

//=============================================================================

public class ListSets implements OaiPmhService
{
	public String getVerb() { return ListSetsRequest.VERB; }

	//---------------------------------------------------------------------------
	//---
	//--- Service
	//---
	//---------------------------------------------------------------------------

	public AbstractResponse execute(AbstractRequest request, ServiceContext context) throws Exception
	{
		ListSetsRequest  req = (ListSetsRequest) request;
		ListSetsResponse res = new ListSetsResponse();

		//--- we don't provide streaming for sets

		if (req.getResumptionToken() != null)
			throw new BadResumptionTokenException(req.getResumptionToken());

		Dbms dbms = (Dbms) context.getResourceManager().open(Geonet.Res.MAIN_DB);

		String query = "SELECT name, label FROM Categories, CategoriesDes "+
							"WHERE id=idDes AND langId=?";

		@SuppressWarnings("unchecked")
        List<Element> list = dbms.select(query, context.getLanguage()).getChildren();

		for (Element rec : list)
		{
			String name  = rec.getChildText("name");
			String label = rec.getChildText("label");

			res.addSet(new SetInfo(name, label));
		}

		return res;
	}
}

//=============================================================================

