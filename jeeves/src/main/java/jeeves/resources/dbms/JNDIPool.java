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
//===	Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
//===
//===	Contact: Jeroen Ticheler - GeoCat
//===	email: Jeroen.Ticheler@geocat.org
//==============================================================================

package jeeves.resources.dbms;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import jeeves.server.resources.Stats;

//=============================================================================

/**
 * A pool of database connections via JNDI 
 */

public class JNDIPool extends AbstractDbmsPool {

	private static final String UNKOWN_PARAM = "unknown - JNDI data source supplied by container";

	// --------------------------------------------------------------------------
	// ---
	// --- API
	// ---
	// --------------------------------------------------------------------------

	public Map<String, String> getProps() {
		Map<String, String> result = new HashMap<String, String>();
		result.put("name", name);
		result.put("user", 			UNKOWN_PARAM);
		result.put("password",	UNKOWN_PARAM);
		result.put("url", getUrl());
		return result;
	}
	
	// --------------------------------------------------------------------------

    @Override
    public String getUrl() {
        return "JNDI Resource" +name+", URL unknown.";
    }
    
	// --------------------------------------------------------------------------

	public Stats getStats() throws SQLException {
		return new Stats(null,null,null);
	}

	// --------------------------------------------------------------------------

	public String toString() {
		StringBuffer sb = new StringBuffer();

		sb.append("------------------------------------------------------------------------------");
		sb.append('\n');
		sb.append("Created JNDI connection pool (" + this.name + ")");
		sb.append('\n');
		sb.append("URL specified : " + getUrl());
		sb.append('\n');
		sb.append("------------------------------------------------------------------------------");
		sb.append('\n');

		return sb.toString();
	}

}
// =============================================================================

