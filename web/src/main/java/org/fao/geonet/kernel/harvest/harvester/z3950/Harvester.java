//=============================================================================
//===	Copyright (C) GeoNetwork
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

package org.fao.geonet.kernel.harvest.harvester.z3950;

import jeeves.interfaces.Logger;
import jeeves.resources.dbms.Dbms;
import jeeves.server.ServiceConfig;
import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;
import jeeves.utils.Xml;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Edit;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.harvest.harvester.CategoryMapper;
import org.fao.geonet.kernel.harvest.harvester.GroupMapper;
import org.fao.geonet.kernel.harvest.harvester.Privileges;
import org.fao.geonet.kernel.harvest.harvester.UUIDMapper;
import org.fao.geonet.kernel.search.MetaSearcher;
import org.fao.geonet.kernel.search.SearchManager;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.lib.Lib;
import org.fao.geonet.services.main.Info;
import org.fao.geonet.util.ISODate;
import org.jdom.DocType;
import org.jdom.Document;
import org.jdom.Element;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

//=============================================================================

class Harvester {
	private UUIDMapper localUuids;
	private final DataManager dataMan;
	private final SearchManager searchMan;
	private final SettingManager settingMan;

	// --------------------------------------------------------------------------
	// ---
	// --- Constructor
	// ---
	// --------------------------------------------------------------------------

	public Harvester(Logger log, ServiceContext context, Dbms dbms,
			Z3950Params params) {
		GeonetContext gc = (GeonetContext) context
				.getHandlerContext(Geonet.CONTEXT_NAME);
		this.context = context;
		this.log = log;
		this.searchMan = gc.getSearchmanager();
		this.dataMan = gc.getDataManager();
		this.settingMan = gc.getSettingManager();
		this.context = context;
		this.dbms = dbms;
		this.params = params;
	}

	// ---------------------------------------------------------------------------
	// ---
	// --- API methods
	// ---
	// ---------------------------------------------------------------------------

	public Z3950ServerResults harvest() throws Exception {
		Set<String> newUuids = new HashSet<String>();

		int groupSize = 10;

		log.info("Retrieving remote metadata information:" + params.uuid);

		Z3950ServerResults serverResults = new Z3950ServerResults();

		// --- Clean all before harvest : Remove/Add mechanism
		localUuids = new UUIDMapper(dbms, params.uuid);

		// --- remove old metadata
		for (String uuid : localUuids.getUUIDs()) {
			String id = localUuids.getID(uuid);
            if(log.isDebugEnabled()) log.debug("  - Removing old metadata before update with id: " + id);
			dataMan.deleteMetadataGroup(context, dbms, id);
			serverResults.locallyRemoved++;
		}

		if (serverResults.locallyRemoved > 0)
			dbms.commit();

		// --- Search remote node
		MetaSearcher s = searchMan.newSearcher(SearchManager.Z3950, Geonet.File.SEARCH_Z3950_CLIENT);

		ServiceConfig config = new ServiceConfig();

		Element request = new Element("request");

		// --- Z39.50 servers from harvest params
		for (String id : params.getRepositories()) {
			request.addContent(new Element(Geonet.SearchResult.SERVERS).setText(id)); 
		}

		// --- Z39.50 query from harvest params
		request.addContent(new Element(Geonet.SearchResult.ZQUERY)
				.setText(params.query)); 

		// --- don't get html presentations (get them later)
		request.addContent(new Element(Geonet.SearchResult.SERVERHTML)
				.setText("off"));  

		// --- set timeout to be 100 seconds
		request.addContent(new Element(Geonet.SearchResult.TIMEOUT).setText("100"));  
		// --- set hitsPerPage
		request.addContent(new Element(Geonet.SearchResult.HITS_PER_PAGE).setText(groupSize+""));  

		// --- do the search
		s.search(context, request, config);

		if (s.getSize() == 0) {
			log.error("Search failed or returned 0 results, trying again");
			s.search(context, request, config);
			if (s.getSize() == 0) {
				throw new Exception("Bad luck, Search failed or returned 0 results");
			}
		}

        if(log.isDebugEnabled()) log.debug("Search returned "+s.getSize()+" hits");

		// -- process the hits in groups of groupSize
		int numberOfHits = Math.min(Integer.parseInt(params.maximumHits),s.getSize());
		// -- add from and to placeholders to request

		request.addContent(new Element("from"));
		request.addContent(new Element("to"));

		Element categories = Lib.local.retrieve(dbms, "Categories");
        if(log.isDebugEnabled()) log.debug("categories "+Xml.getString(categories));

		Element repositories = new Info().getZRepositories(context, settingMan);
        if(log.isDebugEnabled()) log.debug("repos "+Xml.getString(repositories));

		// -- build a map of collection code versus repository name for 
		// -- assigning the categories
		Map <String,String> codes = new HashMap<String,String>();
		Map <String,String> catCodes = new HashMap<String,String>();

		// -- add new category for each repository
		boolean addcateg = false;
		for (String repo : params.getRepositories()) {
			Element repoElem = Xml.selectElement(repositories,"record[id='"+repo+"']");
			if (repoElem != null) {
				Element repoId  = repoElem.getChild("id");
				String repoName = repoElem.getChildText("name");
				codes.put(repoId.getAttributeValue("serverCode")+":"+repoId.getAttributeValue("code"), repoName);
				// create a result holder for this repository
				Z3950Result result = serverResults.getServerResult(repoName);

				// sanitize the name of the category
				String categName = repoName.replaceAll("[^\\w]",""); 
				categName = categName.toLowerCase();
				catCodes.put(repoId.getAttributeValue("serverCode")+":"+repoId.getAttributeValue("code"), categName);

				if (Xml.selectElement(categories,"record[name='"+categName+"']") == null) {
					int newId = context.getSerialFactory().getSerial(dbms, "Categories");
					dbms.execute("INSERT INTO Categories(id, name) VALUES (?, ?)", newId, categName);
					Lib.local.insert(dbms, "Categories", newId, repoName);
					addcateg = true;
				}
			}
		}

		if (addcateg) dbms.commit();

		// --- return only maximum hits as directed by the harvest params
		int nrGroups = (numberOfHits / groupSize) + 1;
		for (int i = 1; i <= nrGroups; i++) {
			int lower = ((i-1)*groupSize)+1;	
			int upper = Math.min((i*groupSize),numberOfHits);
			request.getChild("from").setText(""+lower);  
			request.getChild("to").setText(""+upper);  

			// --- Loading results
			List<Document> list = s.presentDocuments(context, request, config);

			// --- Loading categories and groups
			localCateg = new CategoryMapper(dbms);
			localGroups = new GroupMapper(dbms);

            if(log.isDebugEnabled())
                log.debug("There are "+(list.size()-1)+" children in the results ("+lower+" to "+upper+")");

			boolean transformIt = false;
			String thisXslt = context.getAppPath() + Geonet.Path.IMPORT_STYLESHEETS + "/";
			if (!params.importXslt.equals("none")) {
				thisXslt = thisXslt + params.importXslt;
				transformIt = true;
			}

			// --- For each record....
			for (Document doc : list) {
				Element md = doc.getRootElement();
				String eName = md.getQualifiedName();
				if (eName.equals("summary")) continue;

				// -- Remove existing geonet:info children as for example
				// -- GeoNetwork Z39.50 server return when full mode
				// -- an extra element with server info not needed
				// -- once harvested
				String colCode = "";
				Element info = md.getChild(Edit.RootChild.INFO, Edit.NAMESPACE);
				if (info != null) {
					String serverCode = info.getChildText("server");
					int colPos = serverCode.indexOf(':');
					if (colPos != -1) {
						colCode = serverCode.substring(0,colPos)+":"+info.getChildText("collection");
					}
				}
				md.removeChildren(Edit.RootChild.INFO, Edit.NAMESPACE);
				String repoName = codes.get(colCode);
                if(log.isDebugEnabled()) log.debug("Processing record from server "+repoName);
				Z3950Result result = serverResults.getServerResult(repoName);
				result.totalMetadata++;

				if (eName.equals("error")) {
					log.error("JZKit could not retrieve record - returned "+Xml.getString(md));
					result.unretrievable++;
					continue;
				} 

				// transform using importxslt if not none
				if (transformIt) {
					try {
                        if(log.isDebugEnabled()) log.debug("Before transform: "+Xml.getString(md));
						md = Xml.transform(md, thisXslt);
                        if(log.isDebugEnabled()) log.debug("After transform: "+Xml.getString(md));
					} catch (Exception e) {
						System.out.println("Cannot transform XML, ignoring. Error was: "+e.getMessage());
						result.badFormat++;
						continue; // skip this one
					}
				}

				// detect schema, extract uuid and add
				String schema = dataMan.autodetectSchema(md);
				if (schema == null) {
					log.warning("Skipping metadata with unknown schema.");
					result.unknownSchema++;
					continue;
				}

				String uuid = null;
				try {
					uuid = dataMan.extractUUID(schema, md);
				} catch (Exception e) {
					log.error("Unable to extract UUID: "+e.getMessage());
					e.printStackTrace();
				}

				if (uuid == null || uuid.equals("")) {
					log.warning("Skipping metadata due to failure extracting uuid (uuid null or empty).");
					result.unretrievable++;
					continue;
				}

				log.info("  - Adding metadata with " + uuid);

				//--- generate a new metadata id
		
				int id = context.getSerialFactory().getSerial(dbms, "Metadata");
                // TODO end confusion about datatypes
                String id$ = Integer.toString(id);

				String docType = "";
				if (!transformIt && (doc.getDocType() != null)) {
					docType = Xml.getString(doc.getDocType());
				}

				//--- check for duplicate uuid - violates constraints on metadata table
				//--- if we attempt insert
				boolean alreadyAdded = !newUuids.add(uuid);
				boolean alreadyInDb  = (dataMan.getMetadataId(dbms,uuid) != null);
				if (alreadyAdded || alreadyInDb) {
					log.error("Uuid "+uuid+" already exists in this set/database - cannot insert");
					result.couldNotInsert++;
					continue;
				}

                //
                // insert metadata
                //
				try {
                    String groupOwner = "1", isTemplate = "n", title = null;
                    int owner = 1;
                    String category = null, createDate = new ISODate().toString(), changeDate = createDate;
                    boolean ufo = false, indexImmediate = false;
					dataMan.insertMetadata(context, dbms, schema, md, id, uuid, owner, groupOwner, params.uuid,
                        isTemplate, docType, title, category, createDate, changeDate, ufo, indexImmediate);

				}
                catch (Exception e) {
					log.error("Unable to insert metadata "+e.getMessage());
					e.printStackTrace();
					result.couldNotInsert++;
					continue;
				}

				addPrivileges(id$);
				addCategories(id$, catCodes.get(colCode));

				dataMan.setTemplateExt(dbms, id, "n", null);
				dataMan.setHarvestedExt(dbms, id, params.uuid, params.name);

				// validate it here if requested
				if (params.validate) {
					Document docVal;
					if (!transformIt && (doc.getDocType() != null)) {
						docVal = new Document(md, (DocType)doc.getDocType().detach());
					} else {
						docVal = new Document(md);
					}

					if (!dataMan.doValidate(dbms, schema, id$, docVal, context.getLanguage())) {
						result.doesNotValidate++;
					} 
				}
			
				boolean indexGroup = false;
				dataMan.indexMetadata(dbms, id$);

				result.addedMetadata++;
			}
		}

		dbms.commit();
		return serverResults;
	}

	/**
	 * Add categories according to harvesting configuration TODO : This is a
	 * common part of different harvester.
	 *
	 * @param id
	 *            GeoNetwork internal identifier
	 *
	 */
	private void addCategories(String id, String serverCategory) throws Exception {
		UserSession session = context.getUserSession();
		for (String catId : params.getCategories()) {
			String name = localCateg.getName(catId);

			if (name == null) {
                if(log.isDebugEnabled()) log.debug("    - Skipping removed category with id:" + catId);
			} else {
				dataMan.setCategory(context, dbms, id, catId);
			}
		}

		if (serverCategory != null) {
			String catId = localCateg.getID(serverCategory);
			if (catId == null) {
                if(log.isDebugEnabled()) log.debug("    - Skipping removed category :" + serverCategory);
			} else {
				dataMan.setCategory(context, dbms, id, catId);
			}
		}
	}

	/**
	 * Add privileges according to harvesting configuration
	 *
	 * @param id
	 *            GeoNetwork internal identifier
	 *
	 */
	private void addPrivileges(String id) throws Exception {
		UserSession session = context.getUserSession();
		for (Privileges priv : params.getPrivileges()) {
			String name = localGroups.getName(priv.getGroupId());

			if (name == null)
                if(log.isDebugEnabled())
                    log.debug("    - Skipping removed group with id:" + priv.getGroupId());
			else {
				for (int opId : priv.getOperations()) {
					name = dataMan.getAccessManager().getPrivilegeName(opId);

					// --- allow only: view, dynamic, featured
					if (opId == 0 || opId == 5 || opId == 6) {
						dataMan.setOperation(context, dbms, id, priv.getGroupId(), opId + "");
					} else
                    if(log.isDebugEnabled()) log.debug("       --> " + name + " (skipped)");
				}
			}
		}
	}

	// ---------------------------------------------------------------------------
	// ---
	// --- Variables
	// ---
	// ---------------------------------------------------------------------------

	private final Logger log;
	private final Dbms dbms;
	private final Z3950Params params;
	private ServiceContext context;
	private CategoryMapper localCateg;
	private GroupMapper localGroups;
}

// =============================================================================

