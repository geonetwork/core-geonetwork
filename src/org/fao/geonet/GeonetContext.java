//=============================================================================
//===	Copyright (C) 2001-2005 Food and Agriculture Organization of the
//===	United Nations (FAO-UN), United Nations World Food Programme (WFP)
//===	and United Nations Environment Programme (UNEP)
//===
//===	This program is free software; you can redistribute it and/or modify
//===	it under the terms of the GNU General Public License as published by
//===	the Free Software Foundation; either version 2 of the License, or (at
//===	your option) any later version.
//===
//===	This program is distributed in the hope that it will be useful, but
//===	WITHOUT ANY WARRANTY; without even the implied warranty of
//===	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
//===	General Public License for more details.
//===
//===	You should have received a copy of the GNU General Public License
//===	along with this program; if not, write to the Free Software
//===	Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
//===
//===	Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
//===	Rome - Italy. email: GeoNetwork@fao.org
//==============================================================================

package org.fao.geonet;

import jeeves.server.ServiceConfig;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.AccessManager;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.csw.CatalogDispatcher;
import org.fao.geonet.kernel.search.SearchManager;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.kernel.harvest.HarvestManager;

//=============================================================================

public class GeonetContext
{
	/* package */ DataManager       dataMan;
	/* package */ AccessManager     accessMan;
	/* package */ SearchManager     searchMan;
	/* package */ ServiceConfig     config;
	/* package */ CatalogDispatcher catalogDis;
	/* package */ SettingManager    settingMan;
	/* package */ HarvestManager    harvestMan;

	//---------------------------------------------------------------------------
	/*package*/ GeonetContext() {}
	//---------------------------------------------------------------------------

	public DataManager       getDataManager()       { return dataMan;    }
	public AccessManager     getAccessManager()     { return accessMan;  }
	public SearchManager     getSearchmanager()     { return searchMan;  }
	public ServiceConfig     getHandlerConfig()     { return config;     }
	public CatalogDispatcher getCatalogDispatcher() { return catalogDis; }
	public SettingManager    getSettingManager()    { return settingMan; }
	public HarvestManager    getHarvestManager()    { return harvestMan; }

	//---------------------------------------------------------------------------

	public String getSiteId() { return config.getValue(Geonet.Config.SITE_ID, "???"); }
}

//=============================================================================

