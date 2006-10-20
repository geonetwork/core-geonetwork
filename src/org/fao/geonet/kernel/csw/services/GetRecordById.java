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

package org.fao.geonet.kernel.csw.services;

import java.io.File;
import java.util.Iterator;
import java.util.Map;
import jeeves.resources.dbms.Dbms;
import jeeves.server.context.ServiceContext;
import jeeves.utils.Util;
import jeeves.utils.Xml;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.csw.common.Csw;
import org.fao.geonet.csw.common.Csw.ElementSetName;
import org.fao.geonet.csw.common.exceptions.CatalogException;
import org.fao.geonet.csw.common.exceptions.NoApplicableCodeEx;
import org.fao.geonet.kernel.csw.CatalogService;
import org.jdom.Element;

//=============================================================================

public class GetRecordById extends AbstractOperation implements CatalogService
{
	//---------------------------------------------------------------------------
	//---
	//--- Constructor
	//---
	//---------------------------------------------------------------------------

	public GetRecordById() {}

	//---------------------------------------------------------------------------
	//---
	//--- API methods
	//---
	//---------------------------------------------------------------------------

	public String getName() { return "GetRecordById"; }

	//---------------------------------------------------------------------------

	public Element execute(Element request, ServiceContext context) throws CatalogException
	{
		checkService(request);
		checkVersion(request);

		ElementSetName setName = getElementSetName(request);

		Element response = new Element(getName() +"Response", Csw.NAMESPACE_CSW);

		Iterator ids = request.getChildren("Id", Csw.NAMESPACE_CSW).iterator();

		while(ids.hasNext())
		{
			String  id = ((Element) ids.next()).getText();
			Element md = retrieveMetadata(context, id, setName);

			if (md != null)
				response.addContent(md);
		}

		return response;
	}

	//---------------------------------------------------------------------------

	public Element adaptGetRequest(Map<String, String> params)
	{
		String service     = params.get("service");
		String version     = params.get("version");
		String elemSetName = params.get("elementsetname");
		String ids         = params.get("id");

		Element request = new Element(getName(), Csw.NAMESPACE_CSW);

		setAttrib(request, "service", service);
		setAttrib(request, "version", version);

		fill(request, "Id", ids);

		addElement(request, "ElementSetName", elemSetName);

		return request;
	}

	//---------------------------------------------------------------------------
	//---
	//--- Private methods
	//---
	//---------------------------------------------------------------------------

	private Element retrieveMetadata(ServiceContext context, String uuid,
												ElementSetName setName) throws CatalogException
	{
		try
		{
			Dbms dbms = (Dbms) context.getResourceManager().open(Geonet.Res.MAIN_DB);

			//--- get metadata from DB

			Element  res = dbms.select("SELECT schemaId, data FROM Metadata WHERE uuid='"+uuid+"'");
			Iterator i   = res.getChildren().iterator();

			dbms.commit();

			if (!i.hasNext())
				return null;

			Element record = (Element) i.next();

			String schema = record.getChildText("schemaid");
			String data   = record.getChildText("data");

			Element md = Xml.loadString(data, false);

			//--- apply stylesheet according to setName and schema

			String FS         = File.separator;
			String schemaDir  = context.getAppPath() +"xml"+ FS +"csw"+ FS +"schemas"+ FS +schema+ FS;
			String styleSheet = schemaDir + "ogc-"+setName+".xsl";

			md = Xml.transform(md, styleSheet);

			//--- needed to detach md from the document
			md.detach();

			return md;
		}
		catch (Exception e)
		{
			context.error("Raised : "+ e);
			context.error(" (C) Stacktrace is\n"+Util.getStackTrace(e));

			throw new NoApplicableCodeEx(e.toString());
		}
	}
}

//=============================================================================

