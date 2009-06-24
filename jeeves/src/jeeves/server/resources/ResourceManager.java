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

import java.util.*;

//=============================================================================

/** This class contains all resources that a service can access and delivers them
  * on demand
  */

public class ResourceManager
{
	private ProviderManager provManager;

	private Hashtable htResources = new Hashtable(10, .75f);

	//--------------------------------------------------------------------------
	//---
	//--- Constructor
	//---
	//--------------------------------------------------------------------------

	public ResourceManager(ProviderManager pm)
	{
		provManager = pm;
	}

	//--------------------------------------------------------------------------
	//---
	//--- API methods
	//---
	//--------------------------------------------------------------------------

	/** Gets a resource, quering the appropriate resource provider
	  */

	public Object open(String name) throws Exception
	{
		Object resource = htResources.get(name);

		if (resource == null)
		{
			ResourceProvider provider = provManager.getProvider(name);

			resource = provider.open();

			if (resource != null)
				htResources.put(provider.getName(), resource);
		}

		return resource;
	}

	//--------------------------------------------------------------------------
	/** Closes all resources doing a commit
	  */

	public void close() throws Exception
	{
		release(true);
	}

	//--------------------------------------------------------------------------
	/** Closes all resources doing an abort
	  */

	public void abort() throws Exception
	{
		release(false);
	}

	//--------------------------------------------------------------------------
	//---
	//--- Private methods
	//---
	//--------------------------------------------------------------------------

	/** Scans all resources doing a commit/abort
	  */

	private void release(boolean commit) throws Exception
	{
		Exception errorExc = null;

		for (Enumeration e=htResources.keys(); e.hasMoreElements(); )
		{
			String name     = (String)e.nextElement();
			Object resource = htResources.get(name);

			ResourceProvider provider = provManager.getProvider(name);

			try
			{
				if (commit)	provider.close(resource);
					else 		provider.abort(resource);
			}
			catch (Exception ex)
			{
				errorExc = ex;
			}
		}

		htResources = new Hashtable(10, .75f);

		if (errorExc != null)
			throw errorExc;
	}
}

//=============================================================================

