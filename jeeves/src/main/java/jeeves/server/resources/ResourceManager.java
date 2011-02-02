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

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Map;

import jeeves.utils.Log;

//=============================================================================

/** 
 * This class contains all resources that a service can access and delivers them
 * on demand. Resource identifier depends on provider name and thread identifier
 * in order to not to share a same resource between two different threads.
 */
public class ResourceManager
{
	private ProviderManager provManager;

	private Hashtable<String, Object> htResources = new Hashtable<String, Object>(10, .75f);

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

	/** 
	 * Gets a resource, querying the appropriate resource provider - records
	 * the resource so that when the resourcemanager is closed the resource 
	 * will be too - This is the call used by Jeeves
	 *
	 * @param name The name of the Resource Provider to query
	 */
	public Object open(String name) throws Exception
	{

		Log.debug (Log.RESOURCES, "Opening: " + name + " in thread: " + Thread.currentThread().getId());
		String resourceId = name + ":" + Thread.currentThread().getId();
		Object resource = htResources.get(resourceId);

		if (resource == null) {
			Log.debug  (Log.RESOURCES, "  Null resource, opening a new one from the resource provider.");
			ResourceProvider provider = provManager.getProvider(name);

			resource = provider.open();

			if (resource != null)
				htResources.put(resourceId, resource);
				//htResources.put(provider.getName(), resource);
		}
		Log.debug  (Log.RESOURCES, "  Returning: " + resource);
		
		return resource;
	}

	/** 
	 * Gets a resource, querying the appropriate resource provider but does
	 * not record the resource for closure - this must be done by explicitly
	 * calling the close or abort methods with the resource as an argument.
	 * This method should be used by threads that want a database resource
	 * and which will close the resource themselves when finished.
	 *
	 * @param name The name of the Resource Provider to query
	 *    
	 */
	public Object openDirect(String name) throws Exception
	{
		Log.debug (Log.RESOURCES, "DIRECT Open: " + name + " in thread: " + Thread.currentThread().getId());
		ResourceProvider provider = provManager.getProvider(name);

		Object resource = provider.open();
		Log.debug  (Log.RESOURCES, "  Returning: " + resource);
		
		return resource;
	}

	//--------------------------------------------------------------------------
	/** Gets properties from the named resource provider
	  */

	public Map<String,String> getProps(String name) throws Exception
	{
		ResourceProvider provider = provManager.getProvider(name);
		return provider.getProps();
	}

	//--------------------------------------------------------------------------
	/** Closes a resource doing a commit
	  */

	public void close(String name, Object resource) throws Exception
	{
		Log.debug (Log.RESOURCES, "Closing: " + name + " in thread: " + Thread.currentThread().getId());
		String resourceId = name + ":" + Thread.currentThread().getId();
		if (htResources.get(resourceId) != null) {
			htResources.remove(resourceId);
		} else {
			Log.debug (Log.RESOURCES, "Cannot find resource: " + name + ":" + Thread.currentThread().getId() + " in resources table (this may not be an error)");
		}
		ResourceProvider provider = provManager.getProvider(name);
		provider.close(resource);
	}

	//--------------------------------------------------------------------------
	/** Closes a resource doing an abort
	  */

	public void abort(String name, Object resource) throws Exception
	{
		Log.debug (Log.RESOURCES, "Aborting: " + name + " in thread: " + Thread.currentThread().getId());
		String resourceId = name + ":" + Thread.currentThread().getId();
		if (htResources.get(resourceId) != null) {
			htResources.remove(resourceId);
		} else {
			Log.debug (Log.RESOURCES, "Cannot find resource: " + name + ":" + Thread.currentThread().getId() + " in resources table (this may not be an error)");
		}
		ResourceProvider provider = provManager.getProvider(name);
		provider.abort(resource);
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
		
		for (Enumeration<String> e=htResources.keys(); e.hasMoreElements(); )
		{
			String name     = e.nextElement();
			Object resource = htResources.get(name);

			ResourceProvider provider = provManager.getProvider(name.split(":")[0]);

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

		htResources = new Hashtable<String, Object>(10, .75f);

		if (errorExc != null)
			throw errorExc;
	}
}

//=============================================================================

