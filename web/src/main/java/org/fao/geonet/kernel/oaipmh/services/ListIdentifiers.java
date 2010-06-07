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
import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.oaipmh.Lib;
import org.fao.geonet.kernel.oaipmh.OaiPmhService;
import org.fao.geonet.util.ISODate;
import org.fao.oaipmh.exceptions.BadArgumentException;
import org.fao.oaipmh.exceptions.BadResumptionTokenException;
import org.fao.oaipmh.exceptions.NoRecordsMatchException;
import org.fao.oaipmh.requests.AbstractRequest;
import org.fao.oaipmh.requests.ListIdentifiersRequest;
import org.fao.oaipmh.responses.AbstractResponse;
import org.fao.oaipmh.responses.Header;
import org.fao.oaipmh.responses.ListIdentifiersResponse;
import org.jdom.Element;

//=============================================================================

public class ListIdentifiers implements OaiPmhService
{
	public String getVerb() { return ListIdentifiersRequest.VERB; }

	//---------------------------------------------------------------------------
	//---
	//--- Service
	//---
	//---------------------------------------------------------------------------

	public AbstractResponse execute(AbstractRequest request, ServiceContext context) throws Exception
	{
		ListIdentifiersRequest  req = (ListIdentifiersRequest) request;
		ListIdentifiersResponse res = new ListIdentifiersResponse();

		UserSession  session = context.getUserSession();
		SearchResult result  = null;

		String token = req.getResumptionToken();

		int num = 0;
		int pos = 0;

		if (token == null)
		{
			Element params = new Element("request");

			ISODate from   = req.getFrom();
			ISODate until  = req.getUntil();
			String  set    = req.getSet();
			String  prefix = req.getMetadataPrefix();

			if (from != null)
			{
				String sFrom = from.isShort ? from.getDate() : from.toString();
				params.addContent(new Element("dateFrom").setText(sFrom));
			}

			if (until != null)
			{
				String sTo = until.isShort ? until.getDate() : until.toString();
				params.addContent(new Element("dateTo").setText(sTo));
			}

			if (from != null && until != null && from.sub(until) > 0)
				throw new BadArgumentException("From is greater than until");

			if (set != null)
				params.addContent(new Element("category").setText(set));

			result     = new SearchResult(prefix);
			result.ids = Lib.search(context, params);

			if (result.ids.size() == 0)
				throw new NoRecordsMatchException("No results");

			session.setProperty(Lib.SESSION_OBJECT, result);
		}
		else
		{
			result = (SearchResult) session.getProperty(Lib.SESSION_OBJECT);

			if (result == null)
				throw new BadResumptionTokenException("No session for token : "+ token);

			pos = result.parseToken(token);
		}

		//--- loop to retrieve metadata

		while (num<Lib.MAX_RECORDS && pos < result.ids.size())
		{
			int id = result.ids.get(pos);

			Header h = buildHeader(context, id, result.prefix);

			if (h != null)
			{
				res.addHeader(h);
				num++;
			}

			pos++;
		}

		if (token == null && res.getHeadersCount() == 0)
			throw new NoRecordsMatchException("No results");

		result.setupToken(res, pos);

		return res;
	}

	//---------------------------------------------------------------------------
	//---
	//--- Private methods
	//---
	//---------------------------------------------------------------------------

	private Header buildHeader(ServiceContext context, int id, String prefix) throws Exception
	{
		Dbms dbms = (Dbms) context.getResourceManager().open(Geonet.Res.MAIN_DB);

		String query = "SELECT uuid, schemaId, changeDate FROM Metadata WHERE id=?";

		List list = dbms.select(query, id).getChildren();

		//--- maybe the metadata has been removed

		if (list.size() == 0)
			return null;

		Element rec = (Element) list.get(0);

		String uuid       = rec.getChildText("uuid");
		String schema     = rec.getChildText("schemaid");
		String changeDate = rec.getChildText("changedate");

		//--- try to disseminate format if not by schema then by conversion

		if (!prefix.equals(schema)) {
			if (!Lib.existsConverter(schema, context.getAppPath(), prefix)) {
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

