//=============================================================================
//===	Copyright (C) 2001-2005 Food and Agriculture Organization of the
//===	United Nations (FAO-UN), United Nations World Food Programme (WFP)
//===	and United Nations Environment Programme (UNEP)
//===
//===	This library is free software; you can redistribute it and/or
//===	modify it under the terms of the GNU Lesser General Public
//===	License as published by the Free Software Foundation; either
//===	version 2.1 of the License, or (at your option) any later version.
//===
//===	This library is distributed in the hope that it will be useful,
//===	but WITHOUT ANY WARRANTY; without even the implied warranty of
//===	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
//===	Lesser General Public License for more details.
//===
//===	You should have received a copy of the GNU Lesser General Public
//===	License along with this library; if not, write to the Free Software
//===	Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
//===
//===	Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
//===	Rome - Italy. email: GeoNetwork@fao.org
//==============================================================================

package jeeves.server.dispatchers.guiservices;

import org.jdom.*;

import jeeves.constants.*;
import jeeves.interfaces.*;
import jeeves.server.*;
import jeeves.server.context.*;
import jeeves.server.dispatchers.*;
import jeeves.utils.*;

//=============================================================================

public class Call implements GuiService
{
	private String  name;
	private Service serviceObj;

	//---------------------------------------------------------------------------
	//---
	//--- Init
	//---
	//--------------------------------------------------------------------------

	public Call(Element config, String pack, String appPath) throws Exception
	{
		name = Util.getAttrib(config, ConfigFile.Call.Attr.NAME);

		//--- handle 'class' attrib

		String clas = Util.getAttrib(config, ConfigFile.Call.Attr.CLASS);

		if (clas.startsWith("."))
			clas = pack + clas;

		serviceObj = (Service) Class.forName(clas).newInstance();
		serviceObj.init(appPath, new ServiceConfig(config.getChildren()));
	}

	//---------------------------------------------------------------------------
	//---
	//--- Exec
	//---
	//--------------------------------------------------------------------------

	public Element exec(Element response, ServiceContext context) throws Exception
	{
		try
		{
			//--- invoke the method and obtain a jdom result

			response = serviceObj.exec(response, context);

			context.getResourceManager().close();

			if (response != null)
				response.setName(name);

			return response;
		}
		catch(Exception e)
		{
			//--- in case of exception we have to abort all resources

			context.getResourceManager().abort();

			throw e;
		}
	}
}

//=============================================================================

