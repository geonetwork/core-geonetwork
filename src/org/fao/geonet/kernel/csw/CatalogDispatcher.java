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

package org.fao.geonet.kernel.csw;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import jeeves.server.context.ServiceContext;
import jeeves.utils.Util;
import jeeves.utils.Xml;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.csw.common.exceptions.CatalogException;
import org.fao.geonet.csw.common.exceptions.NoApplicableCodeEx;
import org.fao.geonet.csw.common.exceptions.OperationNotSupportedEx;
import org.fao.geonet.kernel.csw.services.DescribeRecord;
import org.fao.geonet.kernel.csw.services.GetCapabilities;
import org.fao.geonet.kernel.csw.services.GetDomain;
import org.fao.geonet.kernel.csw.services.GetRecordById;
import org.fao.geonet.kernel.csw.services.GetRecords;
import org.fao.geonet.kernel.csw.services.Harvest;
import org.fao.geonet.kernel.csw.services.Transaction;
import org.jdom.Element;

//=============================================================================

public class CatalogDispatcher
{
	private HashMap<String, CatalogService> hmServices = new HashMap<String, CatalogService>();

	//---------------------------------------------------------------------------
	//---
	//--- Constructor
	//---
	//---------------------------------------------------------------------------

	public CatalogDispatcher()
	{
		register(new DescribeRecord());
		register(new GetCapabilities());
		register(new GetDomain());
		register(new GetRecordById());
		register(new GetRecords());
		register(new Harvest());
		register(new Transaction());
	}

	//---------------------------------------------------------------------------

	private void register(CatalogService s)
	{
		hmServices.put(s.getName(), s);
	}

	//---------------------------------------------------------------------------
	//---
	//--- API methods
	//---
	//---------------------------------------------------------------------------

	public Element dispatch(Element request, GeonetContext gc, ServiceContext context)
	{
		context.info("Received:\n"+Xml.getString(request));

		try
		{
			String operation = request.getName();

			CatalogService cs = hmServices.get(operation);

			//--- operation not found using the POST request format. Let's try with the GET

			if (cs == null)
			{
				Map<String, String> params = extractParams(request);

				operation = params.get("request");

				if (operation == null)
					throw new NoApplicableCodeEx("Missing 'request' parameter");

				cs = hmServices.get(operation);

				//--- operation not found. Raise exception

				if (cs == null)
					throw new OperationNotSupportedEx(operation);

				request = cs.adaptGetRequest(params);
				context.debug("Adapted GET request is:\n"+Xml.getString(request));
			}

			context.info("Dispatching operation : "+ operation);

			return cs.execute(request, context);
		}
		catch(CatalogException e)
		{
			return CatalogException.marshal(e);
		}
		catch(Exception e)
		{
			context.info("Exception stack trace : \n"+ Util.getStackTrace(e));

			return CatalogException.marshal(new NoApplicableCodeEx(e.toString()));
		}
	}

	//---------------------------------------------------------------------------
	//---
	//--- Private method
	//---
	//---------------------------------------------------------------------------

	private Map<String, String> extractParams(Element request)
	{
		HashMap<String, String> hm = new HashMap<String, String>();

		List params = request.getChildren();

		for(int i=0; i<params.size(); i++)
		{
			Element param = (Element) params.get(i);

			String name = param.getName().toLowerCase();
			String value= param.getTextTrim();

			hm.put(name, value);
		}

		return hm;
	}
}

//=============================================================================

