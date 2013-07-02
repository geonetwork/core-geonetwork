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

package org.fao.geonet.kernel.search;

import jeeves.resources.dbms.Dbms;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import jeeves.utils.Util;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.util.ISODate;
import org.jdom.Document;
import org.jdom.Element;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

//==============================================================================

class UnusedSearcher extends MetaSearcher
{
	private ArrayList<String> alResult;
	private Element   elSummary;

	//--------------------------------------------------------------------------
	//---
	//--- Constructor
	//---
	//--------------------------------------------------------------------------

	public UnusedSearcher() {}

	//--------------------------------------------------------------------------
	//---
	//--- MetaSearcher Interface
	//---
	//--------------------------------------------------------------------------

	public void search(ServiceContext context, Element request,
							 ServiceConfig config) throws Exception
	{
		GeonetContext  gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
		SettingManager sm = gc.getBean(SettingManager.class);

		String siteId = sm.getValue("system/site/siteId");

		alResult = new ArrayList<String>();

		//--- get maximun delta in minutes

		int maxDiff = Integer.parseInt(Util.getParam(request, "maxDiff", "5"));

		context.info("UnusedSearcher : using maxDiff="+maxDiff);

		//--- proper search

		String query =	"SELECT DISTINCT id, createDate, changeDate "+
							"FROM   Metadata "+
							"WHERE  isTemplate='n' AND isHarvested='n' AND source=?";

		Dbms dbms = (Dbms) context.getResourceManager().open(Geonet.Res.MAIN_DB);
		@SuppressWarnings("unchecked")
        List<Element> list = dbms.select(query, siteId).getChildren();

        for (Element rec : list) {
            String id = rec.getChildText("id");

            ISODate createDate = new ISODate(rec.getChildText("createdate"));
            ISODate changeDate = new ISODate(rec.getChildText("changedate"));

            if (changeDate.sub(createDate) / 60 < maxDiff) {
                if (!hasInternetGroup(dbms, id)) {
                    alResult.add(id);
                }
            }
        }

		//--- build summary

		makeSummary();

		initSearchRange(context);
	}

	//--------------------------------------------------------------------------------

	public List<Document> presentDocuments(ServiceContext srvContext, Element request, ServiceConfig config) throws Exception {
		throw new UnsupportedOperationException("Not supported by Unused searcher");
	}

	//--------------------------------------------------------------------------

	public Element present(ServiceContext srvContext, Element request,
								  ServiceConfig config) throws Exception
	{
		updateSearchRange(request);

		GeonetContext gc = (GeonetContext) srvContext.getHandlerContext(Geonet.CONTEXT_NAME);

		//--- build response

		Element response =  new Element("response");
		response.setAttribute("from",  getFrom()+"");
		response.setAttribute("to",    getTo()+"");

		response.addContent((Element) elSummary.clone());

		if (getTo() > 0) {
			for(int i = getFrom() -1; i < getTo(); i++) {
				String  id = alResult.get(i);
                boolean forEditing = false, withValidationErrors = false, keepXlinkAttributes = false;
                Element md = gc.getBean(DataManager.class).getMetadata(srvContext, id, forEditing, withValidationErrors, keepXlinkAttributes);
				response.addContent(md);
			}
		}

		return response;
	}

	//--------------------------------------------------------------------------

	public int getSize()
	{
		return alResult.size();
	}

	//--------------------------------------------------------------------------

	public Element getSummary() throws Exception
	{
		Element response =  new Element("response");
		response.addContent((Element) elSummary.clone());

		return response;
	}

	//--------------------------------------------------------------------------

	public void close() {}

	//--------------------------------------------------------------------------
	//---
	//--- Private methods
	//---
	//--------------------------------------------------------------------------

	private boolean hasInternetGroup(Dbms dbms, String id) throws SQLException
	{
		String query ="SELECT COUNT(*) AS result FROM OperationAllowed WHERE groupId=1 AND metadataId=?";

		@SuppressWarnings("unchecked")
        List<Element> list = dbms.select(query, Integer.valueOf(id)).getChildren();

		Element record = list.get(0);

		int result = Integer.parseInt(record.getChildText("result"));

		return (result > 0);
	}

	//--------------------------------------------------------------------------

	private void makeSummary() throws Exception
	{
		elSummary = new Element("summary");

		elSummary.setAttribute("count", getSize()+"");
		elSummary.setAttribute("type", "local");

		Element elKeywords = new Element("keywords");
		elSummary.addContent(elKeywords);

		Element elCategories = new Element("categories");
		elSummary.addContent(elCategories);
	}
}

//==============================================================================


