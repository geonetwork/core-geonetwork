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

import java.util.List;

import jeeves.resources.dbms.Dbms;
import jeeves.server.context.ServiceContext;

import org.fao.geonet.constants.Geonet;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.kernel.oaipmh.Lib;
import org.fao.geonet.kernel.oaipmh.ResumptionTokenCache;
import org.fao.geonet.kernel.SchemaManager;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.oaipmh.requests.ListIdentifiersRequest;
import org.fao.oaipmh.requests.TokenListRequest;
import org.fao.oaipmh.responses.Header;
import org.fao.oaipmh.responses.ListIdentifiersResponse;
import org.fao.oaipmh.responses.ListResponse;
import org.fao.oaipmh.util.ISODate;
import org.fao.oaipmh.util.SearchResult;
import org.jdom.Element;

//=============================================================================

public class ListIdentifiers extends AbstractTokenLister
{
	public ListIdentifiers(ResumptionTokenCache cache, SettingManager sm, SchemaManager scm) {
		super(cache, sm, scm);
	}

	public String getVerb() { return ListIdentifiersRequest.VERB; }

	//---------------------------------------------------------------------------
	//---
	//--- Service
	//---
	//---------------------------------------------------------------------------

	public ListResponse processRequest(TokenListRequest req, int pos, SearchResult result, ServiceContext context) throws Exception  {

		//--- loop to retrieve metadata		
		ListIdentifiersResponse res = new ListIdentifiersResponse();

		int num = 0;

		while (num<Lib.MAX_RECORDS && pos < result.getIds().size())
		{
			int id = result.getIds().get(pos);

			Header h = buildHeader(context, id, result.prefix);

			if (h != null)
			{
				res.addHeader(h);
				num++;
			}

			pos++;
		}

		return res;

	}


	//---------------------------------------------------------------------------
	//---
	//--- Private methods
	//---
	//---------------------------------------------------------------------------

	@SuppressWarnings("unchecked")
	private Header buildHeader(ServiceContext context, int id, String prefix) throws Exception
	{
		Dbms dbms = (Dbms) context.getResourceManager().open(Geonet.Res.MAIN_DB);
		GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
		SchemaManager   sm = gc.getBean(SchemaManager.class);

		String query = "SELECT uuid, schemaId, changeDate FROM Metadata WHERE id=?";

		List<Element> list = dbms.select(query, id).getChildren();

		//--- maybe the metadata has been removed

		if (list.size() == 0)
			return null;

		Element rec = list.get(0);

		String uuid       = rec.getChildText("uuid");
		String schema     = rec.getChildText("schemaid");
		String changeDate = rec.getChildText("changedate");

		//--- try to disseminate format if not by schema then by conversion

		if (!prefix.equals(schema)) {
			if (!Lib.existsConverter(sm.getSchemaDir(schema), prefix)) {
				return null;
			}
		}

		//--- build header and set some infos

		Header h = new Header();

		h.setIdentifier(uuid);
		h.setDateStamp(new ISODate(changeDate));

		//--- find and add categories (here called sets)

		query = "SELECT name FROM Categories, MetadataCateg WHERE id=categoryId AND metadataId=?";

		list = dbms.select(query, id).getChildren();

		for (Object o : list)
		{
			rec = (Element) o;

			h.addSet(rec.getChildText("name"));
		}

		return h;
	}
}

//=============================================================================

