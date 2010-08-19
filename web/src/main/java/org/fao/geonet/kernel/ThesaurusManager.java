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

import jeeves.utils.Log;
import org.fao.geonet.constants.Geonet;
import org.openrdf.sesame.Sesame;
import org.openrdf.sesame.config.ConfigurationException;
import org.openrdf.sesame.config.RepositoryConfig;
import org.openrdf.sesame.config.SailConfig;
import org.openrdf.sesame.constants.RDFFormat;
import org.openrdf.sesame.repository.local.LocalRepository;
import org.openrdf.sesame.repository.local.LocalService;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Hashtable;

//=============================================================================

public class ThesaurusManager {

	private Hashtable<String, Thesaurus> thesauriTable = null;

	private LocalService service = null;

	private String thesauriDirectory = null;

	/**
	 * 
	 * @param appPath
	 * @param thesauriRepository
	 * @throws Exception
	 */
	public ThesaurusManager(String appPath, String thesauriRepository)
			throws Exception {
		// Get Sesame interface
		service = Sesame.getService();

		File thesauriDir = new File(thesauriRepository);

		if (!thesauriDir.isAbsolute())
			thesauriDir = new File(appPath + thesauriDir);

		thesauriDirectory = thesauriDir.getAbsolutePath();

		initThesauriTable(thesauriDir);
	}
	
	/**
	 * @param fname
	 * @param type
	 * @param dname
	 * @return
	 */
	public String buildThesaurusFilePath(String fname, String type, String dname) {
		return thesauriDirectory+File.separator+type+File.separator+Geonet.CodeList.THESAURUS+File.separator+dname+File.separator+fname;
	}	
	
	/**
	 * 
	 * @param thesauriDirectory
	 */
	private void initThesauriTable(File thesauriDirectory) {

		//repositoryTable = new Hashtable<String, LocalRepository>();
		thesauriTable = new Hashtable<String, Thesaurus>();

		if (thesauriDirectory.isDirectory()) {
			// init of external repositories
			File externalThesauriDirectory = new File(thesauriDirectory,
					Geonet.CodeList.EXTERNAL + File.separator + Geonet.CodeList.THESAURUS);
			if (externalThesauriDirectory.isDirectory()) {
				File[] rdfDataDirectory = externalThesauriDirectory.listFiles();
				for (int i = 0; i < rdfDataDirectory.length; i++) {
					if (rdfDataDirectory[i].isDirectory()) {
						loadRepositories(rdfDataDirectory[i], Geonet.CodeList.EXTERNAL);
					}
				}
			}

			// init of local repositories
			File localThesauriDirectory = new File(thesauriDirectory, Geonet.CodeList.LOCAL
					+ File.separator + Geonet.CodeList.THESAURUS);
			if (localThesauriDirectory.isDirectory()) {
				File[] rdfDataDirectory = localThesauriDirectory.listFiles();
				for (int i = 0; i < rdfDataDirectory.length; i++) {
					if (rdfDataDirectory[i].isDirectory()) {
						loadRepositories(rdfDataDirectory[i], Geonet.CodeList.LOCAL);
					}
				}
			}
		}
	}

	/**
	 * 
	 * @param thesauriDirectory
	 */
	private void loadRepositories(File thesauriDirectory, String root) {
		
		FilenameFilter filter = new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.endsWith(".rdf");
			}
		};

		String[] rdfDataFile = thesauriDirectory.list(filter);

		for (int i = 0; i < rdfDataFile.length; i++) {
		
			Thesaurus gst = new Thesaurus(rdfDataFile[i], root, thesauriDirectory.getName(), new File(thesauriDirectory, rdfDataFile[i]) );
			try {			
				addThesaurus(gst);
			} catch (Exception e) {
				e.printStackTrace();
				// continue loading
			} 
		}
	}

	/**
	 * 
	 * @param gst
	 */
	public void addThesaurus(Thesaurus gst) throws Exception {

		String thesaurusName = gst.getKey();
		
		Log.debug(Geonet.THESAURUS_MAN, "Adding thesaurus : "+ thesaurusName);

		if (existsThesaurus(thesaurusName)) {
			throw new Exception ("A thesaurus exists with code " + thesaurusName);
		}
		
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
			
			thesauriTable.put(thesaurusName, gst);
			
		} catch (ConfigurationException e) {
			e.printStackTrace();
			throw e;
		} 			
	}
	
	/**
	 * 
	 * @param name
	 */
	public void remove(String name){
		service.removeRepository(name);
		thesauriTable.remove(name);
	}
	
	// =============================================================================
	// PUBLIC SERVICES

	public String getThesauriDirectory() {
		return thesauriDirectory;
	}
	
	public Hashtable<String, Thesaurus> getThesauriTable() {
		return thesauriTable;
	}
	
	public LocalRepository getRepositoryByName(String thesaurusName) {
		return thesauriTable.get(thesaurusName).getRepository();
	}
	
	public Thesaurus getThesaurusByName(String thesaurusName) {
		return thesauriTable.get(thesaurusName);
	}	

	/**
	 * @param name
	 * @return
	 */
	public boolean existsThesaurus(String name) {
		return (thesauriTable.get(name) != null);
	}
	// =============================================================================

	public static void main(String[] args) throws Exception {
	/*	ThesaurusManager tm = new ThesaurusManager("",
				"E:\\workspace3.2\\TestSesame\\res\\codelist\\");*/
	//	tm.getAllPrefLabel("adminstrativeAreaFrv0.1");
	}

}
