//=============================================================================
//===	Copyright (C) Free Software Foundation
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
import java.sql.SQLException;

import javax.sql.DataSource;
import javax.naming.Context;
import javax.naming.InitialContext;

import jeeves.constants.Jeeves;
import jeeves.server.resources.ResourceListener;
import jeeves.server.resources.ResourceProvider;
import jeeves.utils.Log;

import org.apache.tomcat.dbcp.dbcp.BasicDataSource;

import org.jdom.Element;

/**
 * @author sppigot
 * @author fxprunayre
 */

//=============================================================================

/**
 * A pool of database connections via Apache Commons DBC Pool (or at least 
 * the tomcat wrapping of these). Checks first using JNDI to see whether the
 * container has defined a context with the details in it - uses those if 
 * they are found under jdbc/geonetwork otherwise reads the Jeeves config
 * from web/geonetwork/WEB-INF/config.xml.
 */

public class ApacheDBCPool implements ResourceProvider {

	private String name;
	private Set<ResourceListener> hsListeners = Collections.synchronizedSet(new HashSet<ResourceListener>());
	private BasicDataSource ds;
	
	// --------------------------------------------------------------------------
	// ---
	// --- API
	// ---
	// --------------------------------------------------------------------------

	/**
	 * Builds the pool using JNDI context or init parameters from jeeves config.
	 */
	public void init(String name, Element config) throws Exception {

		// check and see whether we have a JNDI context defined

		String contextName = config.getChildText(Jeeves.Res.Pool.CONTEXT);
		if (contextName != null) {
			String dsName      = config.getChildText(Jeeves.Res.Pool.RESOURCE_NAME);
			if (dsName == null) throw new IllegalArgumentException("context "+contextName+" specified but resource name parameter "+Jeeves.Res.Pool.RESOURCE_NAME+" has not been found");
			Context initContext = new InitialContext();
			Context envContext  = (Context)initContext.lookup(contextName);
			ds = (BasicDataSource)envContext.lookup(dsName);
			this.name = contextName + "/" + dsName;
		} else {
			parseJeevesDBConfig(config);
		}

		if (ds.getUrl().toUpperCase().contains("MCKOI")) {
			// McKoi doesn't work unless you use TRANSACTION_SERIALIZABLE
			ds.setDefaultTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
		} else {
			// Everything else seems safe and faster with TRANSACTION_READ_COMMITTED
			ds.setDefaultTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);		 }

		debug(toString());
	}

	/**
	 * Builds the pool using init parameters from jeeves config.
	 */
	public void parseJeevesDBConfig(Element config) throws Exception {
		String user = config.getChildText(Jeeves.Res.Pool.USER);
		String passwd = config.getChildText(Jeeves.Res.Pool.PASSWORD);
		String url = config.getChildText(Jeeves.Res.Pool.URL);
		String driver = config.getChildText(Jeeves.Res.Pool.DRIVER);
		String size = config.getChildText(Jeeves.Res.Pool.POOL_SIZE);
		String maxw = config.getChildText(Jeeves.Res.Pool.MAX_WAIT);
		String maxIdle  = config.getChildText(Jeeves.Res.Pool.MAX_IDLE);
		String minIdle = config.getChildText(Jeeves.Res.Pool.MIN_IDLE);
		String maxActive = config.getChildText(Jeeves.Res.Pool.MAX_ACTIVE);
		String testWhileIdleStr = config.getChildText(Jeeves.Res.Pool.TEST_WHILE_IDLE);
		String timeBetweenEvictionRunsMillisStr = config.getChildText(Jeeves.Res.Pool.TIME_BETWEEN_EVICTION_RUNS_MILLIS);
		String minEvictableIdleTimeMillisStr = config.getChildText(Jeeves.Res.Pool.MIN_EVICTABLE_IDLE_TIME_MILLIS);
		String numTestsPerEvictionRunStr = config.getChildText(Jeeves.Res.Pool.NUM_TESTS_PER_EVICTION_RUN);
		
		this.name = url;

		int poolSize = (size == null) ? Jeeves.Res.Pool.DEF_POOL_SIZE : Integer
				.parseInt(size);
		int maxWait = (maxw == null) ? Jeeves.Res.Pool.DEF_MAX_WAIT : Integer
				.parseInt(maxw);

		boolean testWhileIdle = false;
		if (testWhileIdleStr != null) {
			testWhileIdle = testWhileIdleStr.equals("true");
		}

		long timeBetweenEvictionRunsMillis = -1;
		if (timeBetweenEvictionRunsMillisStr != null) {
			timeBetweenEvictionRunsMillis = Long.parseLong(timeBetweenEvictionRunsMillisStr);
		}

		long minEvictableIdleTimeMillis = 1000 * 60 * 30;
		if (minEvictableIdleTimeMillisStr != null) {
			minEvictableIdleTimeMillis = Long.parseLong(minEvictableIdleTimeMillisStr);
		}

		int numTestsPerEvictionRun = 3;
		if (numTestsPerEvictionRunStr != null) {
			numTestsPerEvictionRun = Integer.parseInt(numTestsPerEvictionRunStr);
		}

		// create the datasource 
		ds = new BasicDataSource();

		ds.setDriverClassName(driver);

		ds.setRemoveAbandoned(true);
		ds.setRemoveAbandonedTimeout(60 * 60);
		ds.setLogAbandoned(true);

		// configure the rest of the pool from params
		// http://commons.apache.org/dbcp/configuration.html
		if (maxActive != null) {
			ds.setMaxActive(Integer.parseInt(maxActive));
		} else {
			ds.setMaxActive(poolSize);
		}
		if (maxIdle != null) {
			ds.setMaxIdle(Integer.parseInt(maxIdle));
		} else {
			ds.setMaxIdle(poolSize);
		}
		if (minIdle != null) {
			ds.setMinIdle(Integer.parseInt(minIdle));
		} else {
			ds.setMinIdle(0);
		}
		ds.setMaxWait(maxWait);

		// always test connections when we get them from the pool    
		ds.setTestOnBorrow(true);

		// time between runs of idle evictor thread                  
		ds.setTimeBetweenEvictionRunsMillis(timeBetweenEvictionRunsMillis);
		// test idle connections                    
		ds.setTestWhileIdle(testWhileIdle);
		// let idle connections sit in there forever
		ds.setMinEvictableIdleTimeMillis(minEvictableIdleTimeMillis);
		// test all idle connections each run
		ds.setNumTestsPerEvictionRun(numTestsPerEvictionRun);

		ds.setPoolPreparedStatements(true);
		ds.setMaxOpenPreparedStatements(-1);

		ds.setValidationQuery("Select 1");
		ds.setDefaultReadOnly(false);
		ds.setDefaultAutoCommit(false);

		ds.setUrl(url);
		ds.setUsername(user);
		ds.setPassword(passwd);

		ds.setInitialSize(poolSize);
	}

	// --------------------------------------------------------------------------

	public Map<String, String> getProps() {
		Map<String, String> result = new HashMap<String, String>();
		result.put("name", name);
		result.put("user", ds.getUsername());
		result.put("password", ds.getPassword());
		result.put("url", ds.getUrl());
		return result;
	}

	// --------------------------------------------------------------------------

	public Map<String, String> getStats() throws SQLException {
		Map<String, String> result = new HashMap<String, String>();
		result.put("numactive",ds.getNumActive()+"");
		result.put("numidle",ds.getNumIdle()+"");
		result.put("maxactive",ds.getMaxActive()+""); 
		return result;
	}

	// --------------------------------------------------------------------------

	public void end() {

		try {
			ds.close();
		} catch (java.sql.SQLException e) {
      error("Problem "+e);
			e.printStackTrace();
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
		debug("Opening " + ds.getUrl());
		Dbms dbms = new Dbms((DataSource)ds, ds.getUrl());
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
		StringBuffer sb = new StringBuffer();

		sb.append("------------------------------------------------------------------------------");
		sb.append('\n');
		sb.append("Created connection pool (" + this.name + ")");
		sb.append('\n');
		sb.append("MaxActive connections                 : " + ds.getMaxActive());
		sb.append('\n');
		sb.append("MaxIdle connections                   : " + ds.getMaxIdle()); 
		sb.append('\n');
		sb.append("MinIdle connections                   : " + ds.getMinIdle()); 
		sb.append('\n');
		sb.append("Maximum wait time connection (maxWait): " + ds.getMaxWait());
		sb.append('\n');
		sb.append("Test While Idle        (testWhileIdle): " + ds.getTestWhileIdle());
		sb.append('\n');
		sb.append("Time Between Eviction Runs (timeBetweenEvictionRunsMillis): " + ds.getTimeBetweenEvictionRunsMillis());
		sb.append('\n');
		sb.append("Minimum Evictable Idle Time (minEvictableIdleTimeMillis)  : " + ds.getMinEvictableIdleTimeMillis());
		sb.append('\n');
		sb.append("Number Connections Tested Per Eviction Run (numTestsPerEvictionRun) : " + ds.getNumTestsPerEvictionRun());
		sb.append('\n');
		sb.append("------------------------------------------------------------------------------");
		sb.append('\n');

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

