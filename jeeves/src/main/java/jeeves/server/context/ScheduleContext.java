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

package jeeves.server.context;

import jeeves.config.springutil.JeevesApplicationContext;
import jeeves.monitor.MonitorManager;
import jeeves.server.resources.ProviderManager;
import jeeves.utils.Log;
import jeeves.utils.SerialFactory;

import java.util.Hashtable;

//=============================================================================

/** Contains the context for a schedule execution
  */

public class ScheduleContext extends BasicContext
{
	private String scheduleName;

    //--------------------------------------------------------------------------
	//---
	//--- Constructor
	//---
	//--------------------------------------------------------------------------

	public ScheduleContext(String name,JeevesApplicationContext appContext, MonitorManager mm, ProviderManager pm, SerialFactory sf, Hashtable<String, Object> contexts)
	{
		super(appContext, mm, pm, sf, contexts);

		logger = Log.createLogger(Log.SCHEDULER +"."+ name);
		this.scheduleName = name;
	}

    public String getScheduleName() {
        return scheduleName;
    }
}

//=============================================================================

