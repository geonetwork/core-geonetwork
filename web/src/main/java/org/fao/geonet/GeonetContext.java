//=============================================================================
//===	Copyright (C) 2001-2007 Food and Agriculture Organization of the
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
//===	Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
//===
//===	Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
//===	Rome - Italy. email: geonetwork@osgeo.org
//==============================================================================

package org.fao.geonet;

import jeeves.server.ServiceConfig;
import org.fao.geonet.kernel.AccessManager;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.SvnManager;
import org.fao.geonet.kernel.XmlSerializer;
import org.fao.geonet.kernel.SchemaManager;
import org.fao.geonet.kernel.ThesaurusManager;
import org.fao.geonet.kernel.csw.CatalogDispatcher;
import org.fao.geonet.kernel.harvest.HarvestManager;
import org.fao.geonet.kernel.oaipmh.OaiPmhDispatcher;
import org.fao.geonet.kernel.search.SearchManager;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.util.ThreadPool;
import org.fao.geonet.notifier.MetadataNotifierManager;
import org.springframework.context.ApplicationContext;

public class GeonetContext {
	/* package */ DataManager       dataMan;
	/* package */ SvnManager        svnManager;
	/* package */ XmlSerializer     xmlSerializer;
	/* package */ AccessManager     accessMan;
	/* package */ SearchManager     searchMan;
	/* package */ SchemaManager     schemaMan;
	/* package */ ServiceConfig     config;
	/* package */ CatalogDispatcher catalogDis;
	/* package */ SettingManager    settingMan;
	/* package */ HarvestManager    harvestMan;
	/* package */ ThesaurusManager  thesaurusMan;
	/* package */ OaiPmhDispatcher  oaipmhDis;
	/* package */ ApplicationContext app_context;
  /* package */ MetadataNotifierManager metadataNotifierMan;
	/* package */ ThreadPool        threadPool;
	Class statusActionsClass;
    boolean readOnly;


    //---------------------------------------------------------------------------
	/*package*/ GeonetContext() {}
	//---------------------------------------------------------------------------

	public DataManager       getDataManager()       { return dataMan;      }
	public SvnManager        getSvnManager()        { return svnManager;   }
	public XmlSerializer     getXmlSerializer()     { return xmlSerializer;}
	public AccessManager     getAccessManager()     { return accessMan;    }
	public SearchManager     getSearchmanager()     { return searchMan;    }
	public SchemaManager     getSchemamanager()     { return schemaMan;    }
	public ServiceConfig     getHandlerConfig()     { return config;       }
	public CatalogDispatcher getCatalogDispatcher() { return catalogDis;   }
	public SettingManager    getSettingManager()    { return settingMan;   }
	public HarvestManager    getHarvestManager()    { return harvestMan;   }
	public ThesaurusManager  getThesaurusManager()  { return thesaurusMan; }
	public OaiPmhDispatcher  getOaipmhDispatcher()  { return oaipmhDis;    }
	public ApplicationContext  getApplicationContext() { return app_context; }
  public MetadataNotifierManager getMetadataNotifier() { return metadataNotifierMan; }
    public ThreadPool        getThreadPool()        { return threadPool;   }

	//---------------------------------------------------------------------------

	public String getSiteId()   { return settingMan.getValue("system/site/siteId"); }
	public String getSiteName() { return settingMan.getValue("system/site/name");   }
	public Class getStatusActionsClass() { return statusActionsClass; }

    public boolean isReadOnly() {
        return readOnly;
    }

    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }
}