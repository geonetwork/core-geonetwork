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

package org.fao.geonet.kernel;

import jeeves.resources.dbms.Dbms;
import jeeves.utils.Log;
import jeeves.utils.Xml;
import jeeves.server.resources.ResourceManager;
import jeeves.server.context.ServiceContext;
import jeeves.server.UserSession;
import jeeves.utils.Util;
import jeeves.xlink.Processor;
import jeeves.guiservices.session.JeevesUser;

import org.apache.commons.lang.StringUtils;

import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.oaipmh.Lib;

import org.jdom.Element;

import org.openrdf.sesame.Sesame;
import org.openrdf.sesame.config.ConfigurationException;
import org.openrdf.sesame.config.RepositoryConfig;
import org.openrdf.sesame.config.SailConfig;
import org.openrdf.sesame.constants.RDFFormat;
import org.openrdf.sesame.repository.local.LocalRepository;
import org.openrdf.sesame.repository.local.LocalService;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.OutputStream;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

//=============================================================================

public class ThesaurusManager implements ThesaurusFinder {

	public synchronized static ThesaurusManager getInstance(ServiceContext context, String appPath, DataManager dm, ResourceManager rm, String thesauriRepository) throws Exception { 
	 	if (_instance == null){ 
	 	_instance = new ThesaurusManager(context, appPath, dm, rm, thesauriRepository); 
	 	} 
	 	return _instance; 
	}
	
	private ConcurrentHashMap<String, Thesaurus> thesauriMap = null;

	private LocalService service = null;

	private String thesauriDirectory = null;

	private ResourceManager rm;

	private DataManager dm;

	private String THESAURUS_MANAGER_NOTIFIER_ID;

	// Single instance 
 	private static ThesaurusManager _instance = null; 
 	

	/**
	 * 
	 * @param context ServiceContext used to check when servlet is up only
	 * @param appPath to find conversion XSLTs etc
	 * @param DataManager to retrieve metadata to convert to SKOS
	 * @param resourceManager for database connections
	 * @param thesauriRepository
	 * @throws Exception
	 */
	private ThesaurusManager(ServiceContext context, String appPath, DataManager dm, ResourceManager rm, String thesauriRepository)
			throws Exception {
		// Get Sesame interface
		service = Sesame.getService();

		File thesauriDir = new File(thesauriRepository);

		if (!thesauriDir.isAbsolute())
			thesauriDir = new File(appPath + thesauriDir);

		thesauriDirectory = thesauriDir.getAbsolutePath();

		this.dm = dm;
		this.rm = rm;
 		THESAURUS_MANAGER_NOTIFIER_ID = UUID.randomUUID().toString();

		batchBuildTable(context, thesauriDir);
	}
	
  /**
   * Start task to build thesaurus table once the servlet is up. 
   *
	 * @param context ServiceContext used to check when servlet is up only
	 * @param thesauriDir directory containing thesauri
   */
	private void batchBuildTable(ServiceContext context, File thesauriDir) {
		ExecutorService executor = Executors.newFixedThreadPool(1);
		try {
			Runnable worker = new InitThesauriTableTask(context, thesauriDir);
			executor.execute(worker);
		} finally {
			executor.shutdown();
		}
	}

  /**
   * A Task to build the thesaurus table once the servlet is up. 
	 * Since thesauri can be metadata records (registers) they can also have 
	 * xlinks. These may not be resolveable until the servlet is up. Hence
	 * we start a thread that waits until the servlet is up before reading
	 * the thesauri and creating the thesaurus table.
   */
  final class InitThesauriTableTask implements Runnable {

		private final ServiceContext context;
		private final File thesauriDir;

		InitThesauriTableTask(ServiceContext context, File thesauriDir) {
			this.context = context;
			this.thesauriDir = thesauriDir;
		}

		public void run() {
			context.setAsThreadLocal();
			try {
				// poll context to see whether servlet is up yet
				while (!context.isServletInitialized()) {
					if (Log.isDebugEnabled(Geonet.THESAURUS_MAN))
						Log.debug(Geonet.THESAURUS_MAN, "Waiting for servlet to finish initializing..");
					Thread.sleep(10000); // sleep 10 seconds
				}
				try {
					initThesauriTable(thesauriDir, context);
				} catch (Exception e) {
					Log.error(Geonet.THESAURUS_MAN, "Error rebuilding thesaurus table : "+e.getMessage()+"\n"+ Util.getStackTrace(e));
				} 
			} catch (Exception e) {
				Log.debug(Geonet.THESAURUS_MAN, "Thesaurus table rebuilding thread threw exception");
				e.printStackTrace();
			}
		}
	}

	/**
	 * 
	 * @param thesauriDirectory
	 */
	private void initThesauriTable(File thesauriDirectory, ServiceContext context) {
		UserSession session = new UserSession();
		session.loginAs(new JeevesUser(context.getProfileManager()).setUsername("admin").setId("-1").setProfile(Geonet.Profile.ADMINISTRATOR));
		context.setUserSession(session);

		thesauriMap = new ConcurrentHashMap<String,Thesaurus>();
		Log.info(Geonet.THESAURUS_MAN,"Scanning "+thesauriDirectory);

		if (thesauriDirectory.isDirectory()) {
			// init of external repositories
			File externalThesauriDirectory = new File(thesauriDirectory,
					Geonet.CodeList.EXTERNAL + File.separator + Geonet.CodeList.THESAURUS);
			if (externalThesauriDirectory.isDirectory()) {
				File[] rdfDataDirectory = externalThesauriDirectory.listFiles();
                for (File aRdfDataDirectory : rdfDataDirectory) {
                    if (aRdfDataDirectory.isDirectory()) {
                        loadRepositories(aRdfDataDirectory, Geonet.CodeList.EXTERNAL, context);
                    }
                }
			}

			// init of local repositories
			File localThesauriDirectory = new File(thesauriDirectory, Geonet.CodeList.LOCAL
					+ File.separator + Geonet.CodeList.THESAURUS);
			if (localThesauriDirectory.isDirectory()) {
				File[] rdfDataDirectory = localThesauriDirectory.listFiles();
                for (File aRdfDataDirectory : rdfDataDirectory) {
                    if (aRdfDataDirectory.isDirectory()) {
                        loadRepositories(aRdfDataDirectory, Geonet.CodeList.LOCAL, context);
                    }
                }
			}

			// init of register repositories
			File registerThesauriDirectory = new File(thesauriDirectory, Geonet.CodeList.REGISTER
					+ File.separator + Geonet.CodeList.THESAURUS);
			if (registerThesauriDirectory.isDirectory()) {
				File[] rdfDataDirectory = registerThesauriDirectory.listFiles();
                for (File aRdfDataDirectory : rdfDataDirectory) {
                    if (aRdfDataDirectory.isDirectory()) {
                        loadRepositories(aRdfDataDirectory, Geonet.CodeList.REGISTER, context);
                    }
                }
			}
		}
	}

	/**
	 * 
	 * @param thesauriDirectory
	 */
	private void loadRepositories(File thesauriDirectory, String root, ServiceContext context) {
		
		FilenameFilter filter = new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.endsWith(".rdf");
			}
		};

		String[] rdfDataFile = thesauriDirectory.list(filter);

        for (String aRdfDataFile : rdfDataFile) {

						Thesaurus gst = null;
						if (root.equals(Geonet.CodeList.REGISTER)) {
                            if(Log.isDebugEnabled(Geonet.THESAURUS_MAN))
                                Log.debug(Geonet.THESAURUS_MAN, "Creating thesaurus : "+ aRdfDataFile);

							File outputRdf = new File(thesauriDirectory, aRdfDataFile);
							String uuid = StringUtils.substringBefore(aRdfDataFile,".rdf");
							try {
								FileOutputStream outputRdfStream = new FileOutputStream(outputRdf);
								getRegisterMetadataAsRdf(context, uuid, outputRdfStream, true);					
								outputRdfStream.close();
							} catch (Exception e) {
								Log.error(Geonet.THESAURUS_MAN, "Register thesaurus "+aRdfDataFile+" could not be read/converted from ISO19135 record in catalog - skipping");
								e.printStackTrace();
								continue;
							}

            	gst = new Thesaurus(aRdfDataFile, root, thesauriDirectory.getName(), outputRdf, dm.getSiteURL());

						} else {
            	gst = new Thesaurus(aRdfDataFile, root, thesauriDirectory.getName(), new File(thesauriDirectory, aRdfDataFile), dm.getSiteURL());
						}

            try {
                addThesaurus(gst, false);
            }
            catch (Exception e) {
                e.printStackTrace();
                // continue loading
            }
        }
	}

	/**	
	 * Get ISO19135 Register Metadata from catalog and convert to rdf.   
	 * 
	 * @param context Service Context
	 * @param uuid Uuid of register (ISO19135) metadata record describing thesaurus
	 * @param os OutputStream to write rdf to from XSLT conversion
	 * @param startUp if true then extract metadata without permissions - used
	 * on startup of catalogue
	 */
	private void getRegisterMetadataAsRdf(ServiceContext context, String uuid, OutputStream os, boolean startUp) throws Exception {


		Dbms dbms = null;

		try {
			dbms = (Dbms) rm.openDirect(Geonet.Res.MAIN_DB);

			String id = dm.getMetadataId(dbms, uuid);
			Element md;
			if (startUp) {
				// if startup then don't worry about permissions
				md = dm.getMetadataIgnorePermissions(dbms, id);
			} else {
				md = dm.getMetadata(dbms, id);
			}
			Processor.detachXLink(md, context);
			MdInfo mdInfo = dm.getMetadataInfo(dbms, id);
			Element env = Lib.prepareTransformEnv(mdInfo.uuid, mdInfo.changeDate, "", dm.getSiteURL(), "");
	
			//--- transform the metadata with the created env and specified stylesheet
			Element root = new Element("root");
			root.addContent(md);
			root.addContent(env);
	
			String styleSheet = dm.getSchemaDir("iso19135") + "convert/" + Geonet.File.EXTRACT_SKOS_FROM_ISO19135;
			Xml.transform(root, styleSheet, os);
		} finally {
			if (dbms != null) rm.close(Geonet.Res.MAIN_DB, dbms);
		}
	}

	/**
	 * Build thesaurus file path according to thesaurus configuration (ie. codelist directory location).
	 * If directory does not exist, it will create it.
	 * 
	 * @param fname
	 * @param type
	 * @param dname
	 * 
	 * @return the thesaurus file path.
	 */
	public String buildThesaurusFilePath(String fname, String type, String dname) {
		String dirPath = thesauriDirectory + File.separator + type + File.separator + Geonet.CodeList.THESAURUS + File.separator + dname;
		File dir = new File(dirPath);
		if (!dir.exists()) {
			dir.mkdirs();
		}
		return dirPath + File.separator + fname;
	}	

	/**
	 * 
	 * @param gst
	 * @param writeTitle TODO
	 */
	public void addThesaurus(Thesaurus gst, boolean writeTitle) throws Exception {

		String thesaurusName = gst.getKey();

        if(Log.isDebugEnabled(Geonet.THESAURUS_MAN))
            Log.debug(Geonet.THESAURUS_MAN, "Adding thesaurus : "+ thesaurusName);

		if (existsThesaurus(thesaurusName)) {
			throw new Exception ("A thesaurus exists with code " + thesaurusName);
		}
		
		createThesaurusRepository(gst);
		thesauriMap.put(thesaurusName, gst);
		
		if (writeTitle) {
		    gst.addTitleElement(gst.getTitle());
		}

	}
	
	/**
	 * 
	 * @param name
	 */
	public void remove(String name){
		service.removeRepository(name);
		thesauriMap.remove(name);
	}
	
	/**
	 * 
	 * @param gst
	 */
	private void createThesaurusRepository(Thesaurus gst) throws Exception {
		LocalRepository thesaurusRepository;
		try {
			RepositoryConfig repConfig = new RepositoryConfig(gst.getKey());

			SailConfig syncSail = new SailConfig("org.openrdf.sesame.sailimpl.sync.SyncRdfSchemaRepository");
			SailConfig memSail = new org.openrdf.sesame.sailimpl.memory.RdfSchemaRepositoryConfig(
			                                                   gst.getFile().getAbsolutePath(), RDFFormat.RDFXML);
			repConfig.addSail(syncSail);
			repConfig.addSail(memSail);
			repConfig.setWorldReadable(true);
			repConfig.setWorldWriteable(true);
			
			thesaurusRepository = service.createRepository(repConfig);

			gst.setRepository(thesaurusRepository);
		} catch (ConfigurationException e) {
			e.printStackTrace();
			throw e;
		} 			
	}
			
	// =============================================================================
	// PUBLIC SERVICES

	public String getThesauriDirectory() {
		return thesauriDirectory;
	}
	
	@Override
    public Map<String, Thesaurus> getThesauriMap() {
		return Collections.unmodifiableMap(thesauriMap);
	}

    @Override
    public Thesaurus getThesaurusByName(String thesaurusName) {
		return thesauriMap.get(thesaurusName);
	}	

	/**
	 * @param name
	 * @return
	 */
	@Override
    public boolean existsThesaurus(String name) {
		return (thesauriMap.get(name) != null);
	}

	/**
	 * Create (or update an existing) rdf thesaurus from the specified ISO19135 
	 * register record.
	 *
	 * @param uuid Uuid of iso19135 register metadata record to update thesaurus
	 * @param type Type of thesaurus (theme, etc)
	 * @return id of thesaurus created/updated
	 */
	public String createUpdateThesaurusFromRegister(ServiceContext context, String uuid, String type) throws Exception {

		String aRdfDataFile;
		String root = Geonet.CodeList.REGISTER;

	  // check whether we have created a thesaurus for this register already
		aRdfDataFile = uuid+".rdf";
		String thesaurusFile = buildThesaurusFilePath(aRdfDataFile, root, type);
		File outputRdf = new File(thesaurusFile);
		Thesaurus gst = new Thesaurus(aRdfDataFile, root, type, outputRdf, dm.getSiteURL());

		try {
			FileOutputStream outputRdfStream = new FileOutputStream(outputRdf);
			getRegisterMetadataAsRdf(context, uuid, outputRdfStream, false);
			outputRdfStream.close();
		} catch (Exception e) {
			Log.error(Geonet.THESAURUS_MAN, "Register thesaurus "+aRdfDataFile+" could not be read/converted from ISO19135 record in catalog - skipping");
			e.printStackTrace();
		}

		String theKey = gst.getKey();
		gst.retrieveThesaurusTitle();

		synchronized(thesauriMap) {
			if (thesauriMap.replace(theKey, gst) == null) {
				createThesaurusRepository(gst);
				thesauriMap.put(theKey, gst);
                if(Log.isDebugEnabled(Geonet.THESAURUS_MAN))
                    Log.debug(Geonet.THESAURUS_MAN, "Created thesaurus "+theKey+" from register "+uuid);
			} else {
				service.removeRepository(theKey);
				createThesaurusRepository(gst);
                if(Log.isDebugEnabled(Geonet.THESAURUS_MAN))
                    Log.debug(Geonet.THESAURUS_MAN, "Rebuilt thesaurus "+theKey+" from register "+uuid);
			}
		}

		return theKey;
	}

}
