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

package jeeves.server.resources;

import org.jdom.Element;

import java.util.HashMap;

//=============================================================================

/** This class contains all resource providers present into the config file.
  * on demand
  */

public class ProviderManager
{
	private HashMap<String, ResourceProvider> hmProviders = new HashMap<String, ResourceProvider>(10, .75f);

	//--------------------------------------------------------------------------
	//---
	//--- API methods
	//---
	//--------------------------------------------------------------------------

	/** Register a new resource provider (like the dbmspool)
	  */

	public void register(String provider, String name, Element config) throws Exception
	{
		//--- load class and check it

		ResourceProvider resProv =  (ResourceProvider) Class.forName(provider).newInstance();

		resProv.init(name, config);
		hmProviders.put(name, resProv);
	}

	//--------------------------------------------------------------------------

	public void end()
	{
		for(ResourceProvider resProv : hmProviders.values())
			resProv.end();
	}

	//--------------------------------------------------------------------------

	public ResourceProvider getProvider(String name)
	{
		return hmProviders.get(name);
	}

	//--------------------------------------------------------------------------

	public Iterable<ResourceProvider> getProviders()
	{
		return hmProviders.values();
	}
}

//=============================================================================

