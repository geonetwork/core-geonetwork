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

package org.fao.geonet.kernel.oaipmh;

import jeeves.constants.Jeeves;
import jeeves.resources.dbms.Dbms;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import jeeves.utils.Xml;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Edit;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.search.MetaSearcher;
import org.fao.geonet.kernel.search.SearchManager;
import org.fao.oaipmh.exceptions.IdDoesNotExistException;
import org.fao.oaipmh.exceptions.OaiPmhException;
import org.jdom.Element;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

//=============================================================================

public class Lib
{
	public static final int MAX_RECORDS = 10;

	public static final String SESSION_OBJECT = "oai-list-records-result";

	//---------------------------------------------------------------------------
	//---
	//--- API methods
	//---
	//---------------------------------------------------------------------------

	public static String getMetadataSchema(ServiceContext context, String uuid) throws Exception
	{
		Dbms dbms = (Dbms) context.getResourceManager().open(Geonet.Res.MAIN_DB);

		String query = "SELECT schemaId FROM Metadata WHERE uuid=?";

		List list = dbms.select(query, uuid).getChildren();

		if (list.size() == 0)
			throw new IdDoesNotExistException(uuid);

		Element elem = (Element) list.get(0);

		return elem.getChildText("schemaid");
	}

	//---------------------------------------------------------------------------

	public static boolean existsConverter(String schemaDir, String prefix) {
		 File f = new File(schemaDir + "convert/" + prefix + ".xsl");
		 return f.exists();
	}

	//--------------------------------------------------------------------------

	public static Element prepareTransformEnv(String uuid, String changeDate, String baseUrl, String siteUrl, String siteName) {

		//--- setup environment

		Element env = new Element("env");

		env.addContent(new Element("uuid")      .setText(uuid));
		env.addContent(new Element("changeDate").setText(changeDate));
		env.addContent(new Element("baseURL")   .setText(baseUrl));
		env.addContent(new Element("siteURL")   .setText(siteUrl));
		env.addContent(new Element("siteName")  .setText(siteName));

		return env;
	}

	//--------------------------------------------------------------------------

	public static Element transform(String schemaDir, Element env, Element md, String targetFormat) throws Exception {

		//--- setup root element

		Element root = new Element("root");
		root.addContent(md);
		root.addContent(env);

		//--- do an XSL transformation

		String styleSheet = schemaDir + "/convert/" + targetFormat;

		return Xml.transform(root, styleSheet);
	}

	//---------------------------------------------------------------------------

	public static List<String> search(ServiceContext context, Element params) throws Exception
	{
		GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
		SearchManager sm = gc.getSearchmanager();

		MetaSearcher searcher = sm.newSearcher(SearchManager.LUCENE, Geonet.File.SEARCH_LUCENE);

        if(context.isDebug()) context.debug("Searching with params:\n"+ Xml.getString(params));

		searcher.search(context, params, dummyConfig);

		params.addContent(new Element("fast").setText("true"));
		params.addContent(new Element("from").setText("1"));
		params.addContent(new Element("to").setText(searcher.getSize() +""));

		context.info("Records found : "+ searcher.getSize());

		Element records = searcher.present(context, params, dummyConfig);

		records.getChild("summary").detach();

		List<String> result = new ArrayList<String>();

		for (Object o : records.getChildren())
		{
			Element rec  = (Element) o;
			Element info = rec.getChild("info", Edit.NAMESPACE);

			result.add(info.getChildText("id"));
		}

		searcher.close();

		return result;
	}

	//---------------------------------------------------------------------------

	public static Element toJeevesException(OaiPmhException e)
	{
		String  msg = e.getMessage();
		String  cls = e.getClass().getSimpleName();
		String  id  = e.getCode();
		Element res = e.getResponse();

		Element error = new Element(Jeeves.Elem.ERROR)
								.addContent(new Element("message").setText(msg))
								.addContent(new Element("class")  .setText(cls));

		error.setAttribute("id", id);

		if (res != null)
		{
			Element elObj = new Element("object");
			elObj.addContent(res.detach());

			error.addContent(elObj);
		}

		return error;
	}

	//---------------------------------------------------------------------------
	//---
	//--- Variables
	//---
	//---------------------------------------------------------------------------

	private static ServiceConfig dummyConfig = new ServiceConfig();
}

//=============================================================================

