//=============================================================================
//=== Copyright (C) 2001-2011 Food and Agriculture Organization of the
//=== United Nations (FAO-UN), United Nations World Food Programme (WFP)
//=== and United Nations Environment Programme (UNEP)
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
//===	Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301 USA
//===
//===	Contact: Jeroen Ticheler - GeoCat
//===	email: Jeroen.Ticheler@geocat.org
//==============================================================================

package jeeves.resources.dbms;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.sql.SQLException;

import javax.sql.DataSource;

import jeeves.constants.Jeeves;
import jeeves.server.resources.ResourceListener;
import jeeves.server.resources.ResourceProvider;
import jeeves.server.resources.Stats;
import jeeves.utils.Log;

import org.geotools.data.DataStore;

import org.jdom.Element;

/**
 * @author sppigot
 */

//=============================================================================

/**
 * A pool of database connections. This class should be extended to initialize
 * database connection pool implementations (eg. JNDI via geotools or
 * apache commons database connection pool)
 */

public abstract class AbstractDbmsPool implements ResourceProvider {
	public String name;
	public String url;
	private Set<ResourceListener> hsListeners = Collections.synchronizedSet(new HashSet<ResourceListener>());
	private DataSource dataSource;
	private DataStore  dataStore;
	
	// --------------------------------------------------------------------------
	// ---
	// --- Abstract Methods that must be overridden by extending classes
	// ---
	// --------------------------------------------------------------------------

	/**
	 * Initializes the pool from jeeves config and sets the datastore.
	 *
	 * @param name The name of the database pool
	 * @param config The database config params from the jeeves config.xml file
	 */
	public abstract void init(String name, Element config) throws Exception;

	// --------------------------------------------------------------------------

	/**
	 * Closes the datasource and/or disposes the datastore
	 */
	public abstract void end();

	// --------------------------------------------------------------------------

	/**
	 * Return a map of properties describing the pool
	 */
	public abstract Map<String, String> getProps();

	// --------------------------------------------------------------------------

	/**
	 * Return statistics about the pool as a map.
	 */
	public abstract Stats getStats() throws SQLException;

	// --------------------------------------------------------------------------

	/**
	 * Return a string description of the database connection pool.
	 */
	public abstract String toString();

	//---------------------------------------------------------------------------
	// ---
	// --- API Methods common to all extending classes
	// ---
	// --------------------------------------------------------------------------

	/**
	 * NOTE: Must be called by implementing classes after creating their own
	 * (extended) datasource.
	 */
	public synchronized void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	/**
	 * NOTE: Must be called by implementing classes after creating their own
	 * (extended) dataStore. If creating a dataStore is not possible (eg. for
	 * databases that don't support spatial types etc) then set null. 
	 */
	public void setDataStore(DataStore dataStore) {
		this.dataStore = dataStore;
	}

	// --------------------------------------------------------------------------

	public String getName() {
		return name;
	}

	// --------------------------------------------------------------------------
	/**
	 * Gets an element from the pool
	 */

	public synchronized Object open() throws Exception {
		Dbms dbms = new Dbms(dataSource, url);
		String nullStr = null;
		dbms.connect(nullStr, nullStr);
		return dbms;
	}

	// --------------------------------------------------------------------------
	/**
	 * Releases one element from the pool
	 */

	public void close(Object resource) throws Exception {
		Dbms dbms = (Dbms) resource;
		synchronized (hsListeners) {
			for (ResourceListener l : hsListeners)
		 		l.beforeClose(resource);
		}
		try {
			dbms.commit();
		} finally {
			dbms.disconnect();
			synchronized (hsListeners) {
				for (ResourceListener l : hsListeners)
					l.close(resource);
			}
		}
	}

	// --------------------------------------------------------------------------
	/**
	 * Releases one element from the pool doing an abort
	 */

	public void abort(Object resource) throws Exception {
		Dbms dbms = (Dbms) resource;
		try {
			dbms.abort();
		} finally {
			dbms.disconnect();
			synchronized (hsListeners) {
				for (ResourceListener l : hsListeners)
					l.abort(resource);
			}
		}
	}

	// --------------------------------------------------------------------------
	/**
	 * Gets the DataStore from the pool, null if not set.
	 */

	public DataStore getDataStore() {
		if (dataStore != null) {
			return (DataStore)dataStore;
		} else {
			return null;
		}
	}

	// --------------------------------------------------------------------------

	public void addListener(ResourceListener l) {
		hsListeners.add(l);
	}

	// --------------------------------------------------------------------------

	public void removeListener(ResourceListener l) {
		hsListeners.remove(l);
	}

	// --------------------------------------------------------------------------

	protected int getPreparedStatementCacheSize(Element config) throws NumberFormatException {
		int iMaxOpen = -1;
		String maxOpenPreparedStatements = config.getChildText(Jeeves.Res.Pool.MAX_OPEN_PREPARED_STATEMENTS);
		if (maxOpenPreparedStatements != null) {
			try {
				iMaxOpen = Integer.valueOf(maxOpenPreparedStatements);
			} catch (NumberFormatException nfe) {
				throw new IllegalArgumentException(Jeeves.Res.Pool.MAX_OPEN_PREPARED_STATEMENTS+" has non-integer value "+maxOpenPreparedStatements);
			}
		}
		return iMaxOpen;
	}

	//---------------------------------------------------------------------------

	protected void debug(String message) {
		Log.debug(Log.DBMSPOOL, message);
	}

	//---------------------------------------------------------------------------

	protected void info(String message) {
		Log.info(Log.DBMSPOOL, message);
	}

	//---------------------------------------------------------------------------

	protected void error(String message) {
		Log.error(Log.DBMSPOOL, message);
	}

	//---------------------------------------------------------------------------

	protected void warning(String message) {
		Log.warning(Log.DBMSPOOL, message);
	}

	//---------------------------------------------------------------------------

}

// =============================================================================

