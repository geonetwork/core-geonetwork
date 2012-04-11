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

import org.geotools.data.DataStore;

import java.sql.SQLException;
import java.util.Map;

//=============================================================================

/** Describe a generic resource provider. Usually it is a pool of dbms objects
  */

public interface ResourceProvider
{
	/** Gets the name of the provider. This name is used by clients to obtain
	  * a resource of a certain type */

	public String getName();

	/** Initializes the provider */
	public void init(String name, Element config) throws Exception;

	/** gets props from the provider */
	public Map<String,String> getProps();

	/** gets stats from the provider */
	public Stats getStats() throws SQLException;

	/** gets datastore from the provider */
	public DataStore getDataStore();

	/** Stops the provider */
	public void end();

	/** Retrieves a resource from the pool */

	public Object open() throws Exception;

	/** Gives back a resource to the pool, signaling a success. The provider
	  * should commit any operation */

	public void close(Object resource) throws Exception;

	/** Gives back a resource to the pool, signaling an abort. The provider
	  * should abort any operation */

	public void abort(Object resource) throws Exception;

	/** Adds a listener that is notified every time a resource is closed or aborted */
	public void addListener(ResourceListener l);

	public void removeListener(ResourceListener l);
}

//=============================================================================

