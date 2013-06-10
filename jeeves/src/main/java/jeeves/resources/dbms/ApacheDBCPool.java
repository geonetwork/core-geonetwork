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
import java.util.Map;

import javax.sql.DataSource;

import jeeves.server.resources.Stats;

import org.apache.commons.dbcp.BasicDataSource;

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

    @Override
    public synchronized void setDataSource(DataSource dataSource) {
        this.basicDataSource = (BasicDataSource) dataSource;
        super.setDataSource(dataSource);
    }
	// --------------------------------------------------------------------------

	public void end() {
	    try {
            basicDataSource.close();
        } catch (java.sql.SQLException e) {
            error("Problem "+e);
            e.printStackTrace();
        } finally {
            super.end();
        }
	}

	// --------------------------------------------------------------------------

	public Map<String, String> getProps() {
		Map<String, String> result = new HashMap<String, String>();
		result.put("name", name);
		result.put("user", basicDataSource.getUsername());
		result.put("password", basicDataSource.getPassword());
		result.put("url", getUrl());
		return result;
	}

	// --------------------------------------------------------------------------

	@Override
    public String getUrl() {
        return basicDataSource.getUrl();
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

}

// =============================================================================

