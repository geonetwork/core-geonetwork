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

import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.utils.IO;
import org.fao.geonet.utils.Log;
import org.fao.geonet.utils.Xml;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.Util;
import jeeves.xlink.Processor;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.kernel.oaipmh.Lib;

import org.fao.geonet.repository.MetadataRepository;
import org.jdom.Element;

import org.openrdf.sesame.Sesame;
import org.openrdf.sesame.config.ConfigurationException;
import org.openrdf.sesame.config.RepositoryConfig;
import org.openrdf.sesame.config.SailConfig;
import org.openrdf.sesame.constants.RDFFormat;
import org.openrdf.sesame.repository.local.LocalRepository;
import org.openrdf.sesame.repository.local.LocalService;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

//=============================================================================
public class ThesaurusManager implements ThesaurusFinder {

	private ConcurrentHashMap<String, Thesaurus> thesauriMap = new ConcurrentHashMap<String, Thesaurus>();
	private LocalService service = null;
	private String thesauriDirectory = null;
    private boolean initialized = false;


    /**
	 * Initialize ThesaurusManager.
	 *
     * @param context ServiceContext used to check when servlet is up only
     * @param appPath to find conversion XSLTs etc
     * @param thesauriRepository
     * @throws Exception
	 */
	public synchronized void init(ServiceContext context, String appPath, String thesauriRepository)
			throws Exception {
        if (this.initialized) {
            return;
        }
        this.initialized = true;
		// Get Sesame interface
		service = Sesame.getService();

		File thesauriDir = new File(thesauriRepository);

		if (!thesauriDir.exists()) {
			thesauriDir = new File(appPath + thesauriDir);
        }

        thesauriDir = thesauriDir.getAbsoluteFile();
		thesauriDirectory = thesauriDir.getAbsolutePath();

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
        final String siteURL = context.getBean(SettingManager.class).getSiteURL(context);

        for (String aRdfDataFile : rdfDataFile) {

            Thesaurus gst = null;
            if (root.equals(Geonet.CodeList.REGISTER)) {
                if (Log.isDebugEnabled(Geonet.THESAURUS_MAN))
                    Log.debug(Geonet.THESAURUS_MAN, "Creating thesaurus : " + aRdfDataFile);

                File outputRdf = new File(thesauriDirectory, aRdfDataFile);
                String uuid = StringUtils.substringBefore(aRdfDataFile, ".rdf");
                try {
                    FileOutputStream outputRdfStream = new FileOutputStream(outputRdf);
                    getRegisterMetadataAsRdf(uuid, outputRdfStream, context);
                    outputRdfStream.close();
                } catch (Exception e) {
                    Log.error(Geonet.THESAURUS_MAN, "Register thesaurus " + aRdfDataFile + " could not be read/converted from ISO19135 "
                                                    + "record in catalog - skipping");
                    e.printStackTrace();
                    continue;
                }

                gst = new Thesaurus(context.getApplicationContext(), aRdfDataFile, root, thesauriDirectory.getName(), outputRdf, siteURL);

            } else {
                gst = new Thesaurus(context.getApplicationContext(), aRdfDataFile, root, thesauriDirectory.getName(), new File(thesauriDirectory, aRdfDataFile), siteURL);
            }

            try {
                addThesaurus(gst, false);
            } catch (Exception e) {
                e.printStackTrace();
                // continue loading
            }
        }
    }

	/**	
	 * Get ISO19135 Register Metadata from catalog and convert to rdf.   
	 * 
	 * @param uuid Uuid of register (ISO19135) metadata record describing thesaurus
	 * @param os OutputStream to write rdf to from XSLT conversion
	 */
	private void getRegisterMetadataAsRdf(String uuid, OutputStream os, ServiceContext context) throws Exception {
        Metadata mdInfo = context.getBean(MetadataRepository.class).findOneByUuid(uuid);
        Integer id = mdInfo.getId();
        final DataManager dataManager = context.getBean(DataManager.class);
        Element md = dataManager.getMetadata("" + id);
        Processor.detachXLink(md, context);
        final String siteURL = context.getBean(SettingManager.class).getSiteURL(context);
        Element env = Lib.prepareTransformEnv(mdInfo.getUuid(), mdInfo.getDataInfo().getChangeDate().getDateAndTime(),
                "", siteURL, "");

        //--- transform the metadata with the created env and specified stylesheet
        Element root = new Element("root");
        root.addContent(md);
        root.addContent(env);

        String styleSheet = dataManager.getSchemaDir("iso19135") + "convert/" + Geonet.File.EXTRACT_SKOS_FROM_ISO19135;
        Xml.transform(root, styleSheet, os);
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
	public String buildThesaurusFilePath(String fname, String type, String dname) throws IOException {
		String dirPath = thesauriDirectory + File.separator + type + File.separator + Geonet.CodeList.THESAURUS + File.separator + dname;
		File dir = new File(dirPath);
		IO.mkdirs(dir, "Thesaurus dname specific directory");
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
	public String createUpdateThesaurusFromRegister(String uuid, String type, ServiceContext context) throws Exception {

		String aRdfDataFile;
		String root = Geonet.CodeList.REGISTER;

	  // check whether we have created a thesaurus for this register already
		aRdfDataFile = uuid+".rdf";
        String thesaurusFile = buildThesaurusFilePath(aRdfDataFile, root, type);
        File outputRdf = new File(thesaurusFile);
        final String siteURL = context.getBean(SettingManager.class).getSiteURL(context);
        Thesaurus gst = new Thesaurus(context.getApplicationContext(), aRdfDataFile, root, type, outputRdf, siteURL);

		FileOutputStream outputRdfStream = null;
		try {
            outputRdfStream = new FileOutputStream(outputRdf);
			getRegisterMetadataAsRdf(uuid, outputRdfStream, context);
		} catch (Exception e) {
			Log.error(Geonet.THESAURUS_MAN, "Register thesaurus "+aRdfDataFile+" could not be read/converted from ISO19135 record in catalog - skipping");
			e.printStackTrace();
		} finally {
		    IOUtils.closeQuietly(outputRdfStream);
		}

		String theKey = gst.getKey();
		gst.retrieveThesaurusTitle();

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

		return theKey;
	}

}
