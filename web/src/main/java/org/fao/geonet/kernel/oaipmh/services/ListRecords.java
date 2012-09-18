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
import jeeves.utils.Xml;

import org.fao.geonet.constants.Geonet;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.kernel.oaipmh.Lib;
import org.fao.geonet.kernel.oaipmh.ResumptionTokenCache;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.SchemaManager;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.oaipmh.OaiPmh;
import org.fao.oaipmh.exceptions.CannotDisseminateFormatException;
import org.fao.oaipmh.exceptions.IdDoesNotExistException;
import org.fao.oaipmh.requests.ListRecordsRequest;
import org.fao.oaipmh.requests.TokenListRequest;
import org.fao.oaipmh.responses.Header;
import org.fao.oaipmh.responses.ListRecordsResponse;
import org.fao.oaipmh.responses.Record;
import org.fao.oaipmh.util.ISODate;
import org.fao.oaipmh.util.SearchResult;
import org.jdom.Element;

//=============================================================================

public class ListRecords extends AbstractTokenLister
{


	public ListRecords(ResumptionTokenCache cache, SettingManager sm, SchemaManager scm) {
	    super(cache, sm, scm);
	}

	public String getVerb() { return ListRecordsRequest.VERB; }

	//---------------------------------------------------------------------------
	//---
	//--- Service
	//---
	//---------------------------------------------------------------------------




	public ListRecordsResponse processRequest(TokenListRequest req, int pos, SearchResult result, ServiceContext context) throws Exception  {
	 
		int num = 0;
		ListRecordsResponse res = new ListRecordsResponse();

		//--- loop to retrieve metadata

		while (num<Lib.MAX_RECORDS && pos < result.ids.size())
		{
			String id = result.ids.get(pos);

			Record r = buildRecord(context, id, result.prefix);

			if (r != null)
			{
				res.addRecord(r);
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
	private Record buildRecord(ServiceContext context, String id, String prefix) throws Exception
	{

		// have to catch exceptions and return null because this function can
		// be called several times for a list of MD records
		// and we do not want to stop because of one error
		try {
			return GetRecord.buildRecordStat(context,"id" ,id , prefix);
		} catch (IdDoesNotExistException e) {
			return null;
		} catch (CannotDisseminateFormatException e2) {
			return null;
		} catch (Exception e3) {
			throw e3;
		}
	}
}
