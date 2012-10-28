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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import jeeves.constants.Jeeves;

import jeeves.server.resources.Stats;
import org.apache.commons.dbcp.BasicDataSource;

import org.geotools.data.DataStore;

import org.geotools.data.postgis.PostgisNGDataStoreFactory;
import org.jdom.Element;

/**
 * @author sppigot
 * @author fxprunayre
 */

//=============================================================================

/**
 * A pool of database connections via Apache Commons DBC Pool.
 * Reads config params from web/geonetwork/WEB-INF/config.xml.
 */

public class ApacheDBCPool extends AbstractDbmsPool {

    private BasicDataSource basicDataSource;
	
	// --------------------------------------------------------------------------
	// ---
	// --- API
	// ---
	// --------------------------------------------------------------------------

	/**
	 * Builds the pool using JNDI context or init parameters from jeeves config.
	 */
	public void init(String name, Element config) throws Exception {

		parseJeevesDBConfig(config);

		setDataSource((DataSource)basicDataSource);
		setDataStore(createDataStore());
		debug(toString());
	}

	/**
	 * Builds the pool using init parameters from jeeves config.
	 */
	private void parseJeevesDBConfig(Element config) throws Exception {
		url = config.getChildText(Jeeves.Res.Pool.URL);

		String user = config.getChildText(Jeeves.Res.Pool.USER);
		String passwd = config.getChildText(Jeeves.Res.Pool.PASSWORD);
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
		String validationQuery = config.getChildText(Jeeves.Res.Pool.VALIDATION_QUERY);
		String transactionIsolation = config.getChildText(Jeeves.Res.Pool.TRANSACTION_ISOLATION);
		if (transactionIsolation == null) {
			// use READ_COMMITTED for everything by default except McKoi which 
			// only supports SERIALIZABLE 
			transactionIsolation = Jeeves.Res.Pool.TRANSACTION_ISOLATION_READ_COMMITTED;
			if (url.toUpperCase().contains("MCKOI")) transactionIsolation = Jeeves.Res.Pool.TRANSACTION_ISOLATION_SERIALIZABLE;
		} else {
			Set<String> isolations = new HashSet<String>();
			isolations.add(Jeeves.Res.Pool.TRANSACTION_ISOLATION_SERIALIZABLE);
			isolations.add(Jeeves.Res.Pool.TRANSACTION_ISOLATION_READ_COMMITTED);
			isolations.add(Jeeves.Res.Pool.TRANSACTION_ISOLATION_REPEATABLE_READ);

			if (!isolations.contains(transactionIsolation.toUpperCase())) {
				throw new IllegalArgumentException("Invalid "+Jeeves.Res.Pool.TRANSACTION_ISOLATION+" parameter value: "+transactionIsolation+". Should be one of "+isolations.toString());
			}
		}
		warning("Using transaction isolation setting "+transactionIsolation);
		
		this.name = url;

		int poolSize = (size == null) ? Jeeves.Res.Pool.DEF_POOL_SIZE : Integer
				.parseInt(size);
		int maxWait = (maxw == null) ? Jeeves.Res.Pool.DEF_MAX_WAIT : Integer
				.parseInt(maxw);

		// set maximum number of prepared statements in pool cache
		int iMaxOpen = getPreparedStatementCacheSize(config);

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
		basicDataSource = new BasicDataSource();

		basicDataSource.setDriverClassName(driver);

		basicDataSource.setRemoveAbandoned(true);
		basicDataSource.setRemoveAbandonedTimeout(60 * 60);
		basicDataSource.setLogAbandoned(true);

		// configure the rest of the pool from params
		// http://commons.apache.org/dbcp/configuration.html
		if (maxActive != null) {
			basicDataSource.setMaxActive(Integer.parseInt(maxActive));
		} else {
			basicDataSource.setMaxActive(poolSize);
		}
		if (maxIdle != null) {
			basicDataSource.setMaxIdle(Integer.parseInt(maxIdle));
		} else {
			basicDataSource.setMaxIdle(poolSize);
		}
		if (minIdle != null) {
			basicDataSource.setMinIdle(Integer.parseInt(minIdle));
		} else {
			basicDataSource.setMinIdle(0);
		}
		basicDataSource.setMaxWait(maxWait);

		// always test connections when we get them from the pool    
		basicDataSource.setTestOnBorrow(true);

		// time between runs of idle evictor thread                  
		basicDataSource.setTimeBetweenEvictionRunsMillis(timeBetweenEvictionRunsMillis);
		// test idle connections                    
		basicDataSource.setTestWhileIdle(testWhileIdle);
		// let idle connections sit in there forever
		basicDataSource.setMinEvictableIdleTimeMillis(minEvictableIdleTimeMillis);
		// test all idle connections each run
		basicDataSource.setNumTestsPerEvictionRun(numTestsPerEvictionRun);

		// set maximum number of prepared statements in pool cache, if not set
		// then switch it off altogether
		if (iMaxOpen != -1) {
			basicDataSource.setPoolPreparedStatements(true);
			basicDataSource.setMaxOpenPreparedStatements(iMaxOpen);
		} else {
			basicDataSource.setPoolPreparedStatements(false);
			basicDataSource.setMaxOpenPreparedStatements(-1);
		}

		if (validationQuery != null && validationQuery.trim().length() > 0) {
			basicDataSource.setValidationQuery(validationQuery);
		}
		basicDataSource.setDefaultReadOnly(false);
		basicDataSource.setDefaultAutoCommit(false);

		basicDataSource.setUrl(url);
		basicDataSource.setUsername(user);
		basicDataSource.setPassword(passwd);

		basicDataSource.setInitialSize(poolSize);

	}

	// --------------------------------------------------------------------------

	public void end() {
		try {
		  basicDataSource.close();
		} catch (java.sql.SQLException e) {
			error("Problem "+e);
			e.printStackTrace();
		}
	}

	// --------------------------------------------------------------------------

	public Map<String, String> getProps() {
		Map<String, String> result = new HashMap<String, String>();
		result.put("name", name);
		result.put("user", basicDataSource.getUsername());
		result.put("password", basicDataSource.getPassword());
		result.put("url", basicDataSource.getUrl());
		return result;
	}

	// --------------------------------------------------------------------------

	public Stats getStats() {
		return new Stats(basicDataSource.getNumActive(), basicDataSource.getNumIdle(), basicDataSource.getMaxActive());
	}

	//---------------------------------------------------------------------------

	public String toString() {
		StringBuffer sb = new StringBuffer();

		sb.append("------------------------------------------------------------------------------");
		sb.append('\n');
		sb.append("Created connection pool (" + this.name + ")");
		sb.append('\n');
		sb.append("MaxActive connections                 : " + basicDataSource.getMaxActive());
		sb.append('\n');
		sb.append("MaxIdle connections                   : " + basicDataSource.getMaxIdle()); 
		sb.append('\n');
		sb.append("MinIdle connections                   : " + basicDataSource.getMinIdle()); 
		sb.append('\n');
		sb.append("Maximum wait time connection (maxWait): " + basicDataSource.getMaxWait());
		sb.append('\n');
		sb.append("Test While Idle        (testWhileIdle): " + basicDataSource.getTestWhileIdle());
		sb.append('\n');
		sb.append("Time Between Eviction Runs (timeBetweenEvictionRunsMillis): " + basicDataSource.getTimeBetweenEvictionRunsMillis());
		sb.append('\n');
		sb.append("Minimum Evictable Idle Time (minEvictableIdleTimeMillis)  : " + basicDataSource.getMinEvictableIdleTimeMillis());
		sb.append('\n');
		sb.append("Number Connections Tested Per Eviction Run (numTestsPerEvictionRun) : " + basicDataSource.getNumTestsPerEvictionRun());
		sb.append('\n');
		sb.append("------------------------------------------------------------------------------");
		sb.append('\n');

		return sb.toString();
	}

	// --------------------------------------------------------------------------

	private DataStore createDataStore() throws Exception {
		Map<String,String> props = getProps();
		String url = props.get("url");
		String user = props.get("user");
		String passwd = props.get("password");
		String name = props.get("name");

		DataStore newDataStore = null;
		try {
			if (url.contains("postGIS")) {
				newDataStore = createPostgisDatastore(name, user, passwd, url);
			}
		} catch (Exception e) {
			error("Failed to create datastore for "+url+". Will use shapefile instead.");
			error(e.getMessage());
			e.printStackTrace();
		}

		return newDataStore;
	}

	//---------------------------------------------------------------------------

	private DataStore createPostgisDatastore(String name, String user, String passwd, String url) throws Exception {

		String[] values = url.split("/");

		DataStore newDataStore = null;

		Map<String, Object> params = new HashMap<String, Object>();
		params.put(PostgisNGDataStoreFactory.DBTYPE.key, PostgisNGDataStoreFactory.DBTYPE.sample);
		params.put(PostgisNGDataStoreFactory.DATABASE.key, getDatabase(url, values));
		params.put(PostgisNGDataStoreFactory.USER.key, user);
		params.put(PostgisNGDataStoreFactory.PASSWD.key, passwd);
		params.put(PostgisNGDataStoreFactory.HOST.key, getHost(url, values));
		params.put(PostgisNGDataStoreFactory.PORT.key, getPort(url, values));
		params.put(PostgisNGDataStoreFactory.EXPOSE_PK.key, false);

		PostgisNGDataStoreFactory factory = new PostgisNGDataStoreFactory();
		newDataStore = factory.createDataStore(params);
		if (newDataStore != null) info("NOTE: Using POSTGIS for spatial index");

		return newDataStore;
	}

	//---------------------------------------------------------------------------

	private String getDatabase(String url, String[] values) throws Exception {
		if (url.contains("postGIS")) {
			return values[3];
		} else {
			throw new Exception("Unknown database in url "+url);
		}
	}

	//---------------------------------------------------------------------------

	private String getHost(String url, String[] values) throws Exception {
		if (url.contains("postGIS")) {
			String value = values[2];
			return value.substring(0,value.indexOf(':'));
		} else {
			throw new Exception("Unknown database in url "+url);
		}
	}

	//---------------------------------------------------------------------------

	private String getPort(String url, String values[]) throws Exception {
		if (url.contains("postGIS")) {
			String value = values[2];
			return value.substring(value.indexOf(':')+1);
		} else {
			throw new Exception("Unknown database in url "+url);
		}
	}

}

// =============================================================================

