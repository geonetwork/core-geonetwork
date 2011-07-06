//=============================================================================
//===	Copyright (C) GeoNetwork
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
//===	Contact: Jeroen Ticheler - GeoCat
//===	email: Jeroen.Ticheler@geocat.org
//==============================================================================

package jeeves.resources.dbms;

import java.util.Collections;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import jeeves.constants.Jeeves;
import jeeves.server.resources.ResourceListener;
import jeeves.server.resources.ResourceProvider;
import jeeves.utils.Log;

import org.apache.commons.pool.KeyedObjectPool;
import org.apache.commons.pool.KeyedObjectPoolFactory;
import org.apache.commons.pool.ObjectPool;
import org.apache.commons.pool.impl.GenericKeyedObjectPoolFactory;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.apache.commons.dbcp.AbandonedConfig;
import org.apache.commons.dbcp.AbandonedObjectPool;
import org.apache.commons.dbcp.ConnectionFactory;
import org.apache.commons.dbcp.DriverManagerConnectionFactory;
import org.apache.commons.dbcp.PoolableConnection;
import org.apache.commons.dbcp.PoolableConnectionFactory;
import org.apache.commons.dbcp.PoolingDriver;

import org.jdom.Element;

/**
 * @author sppigot
 * @author fxprunayre
 */

//=============================================================================

/**
 * A pool of database connections via Apache Commons DBC Pool
 */

public class ApacheDBCPool implements ResourceProvider {

	private String name;
	private String user;
	private String passwd;
	private String url;
	private String apacheUrl = "jdbc:apache:commons:dbcp:";
	private final String apacheDriver = "org.apache.commons.dbcp.PoolingDriver";
	private Set<ResourceListener> hsListeners = Collections
			.synchronizedSet(new HashSet<ResourceListener>());
	int poolSize;
	int maxWait;
	String size;
	String driver;
	String maxw;
	
	// --------------------------------------------------------------------------
	// ---
	// --- API
	// ---
	// --------------------------------------------------------------------------

	/**
	 * Builds the pool using init parameters from config
	 */

	public void init(String name, Element config) throws Exception {
		
		user = config.getChildText(Jeeves.Res.Pool.USER);
		passwd = config.getChildText(Jeeves.Res.Pool.PASSWORD);
		url = config.getChildText(Jeeves.Res.Pool.URL);
		driver = config.getChildText(Jeeves.Res.Pool.DRIVER);
		size = config.getChildText(Jeeves.Res.Pool.POOL_SIZE);
		maxw = config.getChildText(Jeeves.Res.Pool.MAX_WAIT);
		String maxIdle  = config.getChildText(Jeeves.Res.Pool.MAX_IDLE);
		String minIdle = config.getChildText(Jeeves.Res.Pool.MIN_IDLE);
		String maxActive = config.getChildText(Jeeves.Res.Pool.MAX_ACTIVE);
		
		this.name = url;

		poolSize = (size == null) ? Jeeves.Res.Pool.DEF_POOL_SIZE : Integer
				.parseInt(size);
		maxWait = (maxw == null) ? Jeeves.Res.Pool.DEF_MAX_WAIT : Integer
				.parseInt(maxw);

		debug("Creating connection pool (" + this.name + ") with " + poolSize + " connections ("
				+ maxWait + ")");

		// register underlying JDBC driver
		Class.forName(driver);

		AbandonedConfig aconfig = new AbandonedConfig();
		aconfig.setRemoveAbandoned(true);
		aconfig.setLogAbandoned(true);
		aconfig.setRemoveAbandonedTimeout(5000);

		// GenericObjectPool connectionPool = new GenericObjectPool();
		GenericObjectPool connectionPool = new AbandonedObjectPool(null,
				aconfig) {
			protected void onRemove(PoolableConnection pc) {
				error("Connection was closed: " + pc.toString());
			}
		};
		// configure the rest of the pool from params
		// http://commons.apache.org/dbcp/configuration.html
		if (maxActive != null) {
			connectionPool.setMaxActive(Integer.parseInt(maxActive));
		} else {
			connectionPool.setMaxActive(poolSize);
		}
		if (maxIdle != null) {
			connectionPool.setMaxIdle(Integer.parseInt(maxIdle));
		} else {
			connectionPool.setMaxIdle(poolSize);
		}
		if (minIdle != null) {
			connectionPool.setMinIdle(Integer.parseInt(minIdle));
		} else {
			connectionPool.setMinIdle(0);
		}
		connectionPool.setMaxWait(maxWait);

		// GenericObjectPool connectionPool = new GenericObjectPool(null);

		KeyedObjectPoolFactory keyConnectionPool = new GenericKeyedObjectPoolFactory(
				null, poolSize,
				GenericObjectPool.DEFAULT_WHEN_EXHAUSTED_ACTION, maxWait,
				GenericObjectPool.DEFAULT_MAX_IDLE,
				GenericObjectPool.DEFAULT_TEST_ON_BORROW,
				GenericObjectPool.DEFAULT_TEST_ON_RETURN,
				GenericObjectPool.DEFAULT_TIME_BETWEEN_EVICTION_RUNS_MILLIS,
				GenericObjectPool.DEFAULT_NUM_TESTS_PER_EVICTION_RUN,
				GenericObjectPool.DEFAULT_SOFT_MIN_EVICTABLE_IDLE_TIME_MILLIS,
				GenericObjectPool.DEFAULT_TEST_WHILE_IDLE);

		// create the connection factory with the url provided in the config
		String validationQuery = "Select 1";
		boolean defaultReadOnly = false;
		boolean defaultAutoCommit = false;

		ConnectionFactory connectionFactory = new DriverManagerConnectionFactory(
				url, user, passwd);

		PoolableConnectionFactory poolableConnectionFactory = new PoolableConnectionFactory(
				connectionFactory, connectionPool, keyConnectionPool,
				validationQuery, defaultReadOnly, defaultAutoCommit);

		if (url.toUpperCase().contains("MCKOI")) {
      // McKoi doesn't work unless you use TRANSACTION_SERIALIZABLE
      poolableConnectionFactory.
        setDefaultTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
    } else {
      // Everything else seems safe and faster with TRANSACTION_READ_COMMITTED
      poolableConnectionFactory.
        setDefaultTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
    }

		// Create the number of connections specified in the params at startup
		for (int i = 0; i < poolSize; i++) {
			connectionPool.addObject();
		}

		// register apache pooling driver which we use in all Dbms objects
		Class.forName(apacheDriver);

		PoolingDriver pDriver = (PoolingDriver) DriverManager.getDriver(apacheUrl);
		pDriver.registerPool(this.name, connectionPool);

		debug(toString());
	}

	// --------------------------------------------------------------------------

	public Map<String, String> getProps() {
		Map<String, String> result = new HashMap<String, String>();
		result.put("name", name);
		result.put("user", user);
		result.put("password", passwd);
		result.put("url", url);
		return result;
	}

	// --------------------------------------------------------------------------

	public Map<String, String> getStats() throws SQLException {
		Map<String, String> result = new HashMap<String, String>();
		PoolingDriver driver = (PoolingDriver) DriverManager.getDriver(apacheUrl);
		ObjectPool connectionPool = driver.getConnectionPool(name);
		result.put("numactive",connectionPool.getNumActive()+"");
		result.put("numidle",connectionPool.getNumIdle()+"");
		result.put("maxactive",size); // we limit the poolsize to this config
		return result;
	}

	// --------------------------------------------------------------------------

	public void end() {

		try {
			PoolingDriver driver = (PoolingDriver) DriverManager.getDriver(apacheUrl);
			driver.closePool(name);
		} catch (java.sql.SQLException e) {
      error("Problem"+e);
    }
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
		debug("Opening " + url);
		Dbms dbms = new Dbms(apacheDriver, apacheUrl + name, url);
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

	public void addListener(ResourceListener l) {
		hsListeners.add(l);
	}

	// --------------------------------------------------------------------------

	public void removeListener(ResourceListener l) {
		hsListeners.remove(l);
	}

	//---------------------------------------------------------------------------

	public String toString() {
		StringBuffer sb = new StringBuffer("DBMSPool");
		sb.append("\t poolSize:").append(poolSize);
		sb.append("\t maxWait:").append(maxWait);

		return sb.toString();
	}

	//---------------------------------------------------------------------------

	private void debug(String message) {
		Log.debug(Log.DBMSPOOL, message);
	}

	//---------------------------------------------------------------------------

	private void info(String message) {
		Log.info(Log.DBMSPOOL, message);
	}

	//---------------------------------------------------------------------------

	private void error(String message) {
		Log.error(Log.DBMSPOOL, message);
	}
}

// =============================================================================

