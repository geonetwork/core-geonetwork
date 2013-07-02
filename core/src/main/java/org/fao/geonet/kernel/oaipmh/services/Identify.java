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

import jeeves.constants.Jeeves;
import jeeves.resources.dbms.Dbms;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.oaipmh.OaiPmhService;
import org.fao.geonet.kernel.setting.SettingInfo;
import org.fao.oaipmh.requests.AbstractRequest;
import org.fao.oaipmh.requests.IdentifyRequest;
import org.fao.oaipmh.responses.AbstractResponse;
import org.fao.oaipmh.responses.IdentifyResponse;
import org.fao.oaipmh.responses.IdentifyResponse.DeletedRecord;
import org.fao.oaipmh.responses.IdentifyResponse.Granularity;
import org.fao.oaipmh.util.ISODate;
import org.jdom.Element;

import java.util.List;

//=============================================================================

public class Identify implements OaiPmhService
{
	public String getVerb() { return IdentifyRequest.VERB; }

	//---------------------------------------------------------------------------
	//---
	//--- Service
	//---
	//---------------------------------------------------------------------------

	public AbstractResponse execute(AbstractRequest request, ServiceContext context) throws Exception
	{
		IdentifyResponse res = new IdentifyResponse();
		SettingInfo      si  = new SettingInfo(context);

		String baseUrl = si.getSiteUrl() + context.getBaseUrl() +"/"+ Jeeves.Prefix.SERVICE +"/en/"+ context.getService();

		res.setRepositoryName(si.getSiteName());
		res.setBaseUrl(baseUrl);
		res.setEarliestDateStamp(getEarliestDS(context));
		res.setDeletedRecord(DeletedRecord.NO);
		res.setGranularity(Granularity.LONG);
		res.addAdminEmail(si.getFeedbackEmail());

		return res;
	}

	//---------------------------------------------------------------------------

	private ISODate getEarliestDS(ServiceContext context) throws Exception
	{
		Dbms dbms = (Dbms) context.getResourceManager().open(Geonet.Res.MAIN_DB);

		String query = "SELECT min(changeDate) as mcd FROM Metadata";

		@SuppressWarnings("unchecked")
        List<Element> list = dbms.select(query).getChildren();

		//--- if we don't have metadata, just return 'now'
		if (list.isEmpty())
			return new ISODate();

		Element rec = list.get(0);
	
		String date = rec.getChildText("mcd");
		if (date == null || date.equals("")) return new ISODate();

		return new ISODate(date);
	}
}

//=============================================================================

