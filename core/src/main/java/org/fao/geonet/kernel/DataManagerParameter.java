package org.fao.geonet.kernel;

import jeeves.server.context.ServiceContext;

import org.fao.geonet.kernel.search.SearchManager;
import org.fao.geonet.kernel.setting.SettingManager;

public class DataManagerParameter {
	public ServiceContext context;
	public SvnManager svnManager;
	public XmlSerializer xmlSerializer;
	public SchemaManager schemaManager;
	public SearchManager searchManager;
	public AccessManager accessManager;
	public SettingManager settingsManager;
	public String baseURL;
	public String dataDir;
	public String thesaurusDir;
	public String appPath;

	public DataManagerParameter(ServiceContext context, SvnManager svnManager,
			XmlSerializer xmlSerializer, SchemaManager scm, SearchManager sm,
			AccessManager am, SettingManager ss, String baseURL,
			String dataDir, String thesaurusDir, String appPath) {
		this.context = context;
		this.svnManager = svnManager;
		this.xmlSerializer = xmlSerializer;
		this.schemaManager = scm;
		this.searchManager = sm;
		this.accessManager = am;
		this.settingsManager = ss;
		this.baseURL = baseURL;
		this.dataDir = dataDir;
		this.thesaurusDir = thesaurusDir;
		this.appPath = appPath;
	}

	public DataManagerParameter() {
	}
	
	
}