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

import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.Logger;
import org.fao.geonet.constants.Edit;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.domain.MetadataCategory;
import org.fao.geonet.domain.MetadataType;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.HarvestValidationEnum;
import org.fao.geonet.kernel.UpdateDatestamp;
import org.fao.geonet.kernel.harvest.BaseAligner;
import org.fao.geonet.kernel.harvest.harvester.CategoryMapper;
import org.fao.geonet.kernel.harvest.harvester.GroupMapper;
import org.fao.geonet.kernel.harvest.harvester.HarvestError;
import org.fao.geonet.kernel.harvest.harvester.HarvestResult;
import org.fao.geonet.kernel.harvest.harvester.IHarvester;
import org.fao.geonet.kernel.harvest.harvester.UUIDMapper;
import org.fao.geonet.kernel.search.MetaSearcher;
import org.fao.geonet.kernel.search.SearchManager;
import org.fao.geonet.kernel.search.SearcherType;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.repository.MetadataCategoryRepository;
import org.fao.geonet.repository.MetadataRepository;
import org.fao.geonet.services.main.Info;
import org.fao.geonet.utils.Xml;
import org.jdom.DocType;
import org.jdom.Document;
import org.jdom.Element;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

//=============================================================================

class Harvester extends BaseAligner implements IHarvester<Z3950ServerResults> {
	private UUIDMapper localUuids;
	private final DataManager dataMan;
	private final SearchManager searchMan;
	private final SettingManager settingMan;

	// --------------------------------------------------------------------------
	// ---
	// --- Constructor
	// ---
	// --------------------------------------------------------------------------

	public Harvester(AtomicBoolean cancelMonitor, Logger log, ServiceContext context, Z3950Params params) {
        super(cancelMonitor);
		GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
		this.context = context;
		this.log = log;
		this.searchMan = gc.getBean(SearchManager.class);
		this.dataMan = gc.getBean(DataManager.class);
		this.settingMan = gc.getBean(SettingManager.class);
		this.context = context;
		this.params = params;
	}

	// ---------------------------------------------------------------------------
	// ---
	// --- API methods
	// ---
	// ---------------------------------------------------------------------------

	public Z3950ServerResults harvest(final Logger log) throws Exception {
		Set<String> newUuids = new HashSet<String>();

		int groupSize = 10;

		this.log = log;
		log.info("Retrieving remote metadata information:" + params.getUuid());

		Z3950ServerResults serverResults = new Z3950ServerResults();

		// --- Clean all before harvest : Remove/Add mechanism
		localUuids = new UUIDMapper(context.getBean(MetadataRepository.class), params.getUuid());

		// --- remove old metadata
		for (String uuid : localUuids.getUUIDs()) {
            if (cancelMonitor.get()) {
                return serverResults;
            }

            String id = localUuids.getID(uuid);
            if(this.log.isDebugEnabled()) log.debug("  - Removing old metadata before update with id: " + id);
			dataMan.deleteMetadataGroup(context, id);
			serverResults.locallyRemoved++;
		}


        if (serverResults.locallyRemoved > 0) {
            dataMan.flush();
        }

		// --- Search remote node
		MetaSearcher s = searchMan.newSearcher(SearcherType.Z3950, Geonet.File.SEARCH_Z3950_CLIENT);

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

		Element repositories = new Info().getZRepositories(context, settingMan);
        if(log.isDebugEnabled()) {
            log.debug("repos "+Xml.getString(repositories));
        }

		// -- build a map of collection code versus repository name for 
		// -- assigning the categories
		Map <String,String> codes = new HashMap<String,String>();
		Map <String,String> catCodes = new HashMap<String,String>();

        final MetadataCategoryRepository categoryRepository = this.context.getBean(MetadataCategoryRepository.class);
        // -- add new category for each repository
		boolean addcateg = false;
		for (String repo : params.getRepositories()) {
            if (cancelMonitor.get()) {
                return serverResults;
            }

            Element repoElem = Xml.selectElement(repositories, "record[id='"+repo+"']");
			if (repoElem != null) {
				Element repoId  = repoElem.getChild("id");
				String repoName = repoElem.getChildText("name");
				codes.put(repoId.getAttributeValue("serverCode")+":"+repoId.getAttributeValue("code"), repoName);
				// create a result holder for this repository
				serverResults.getServerResult(repoName);

				// sanitize the name of the category
				String categName = repoName.replaceAll("[^\\w]",""); 
				categName = categName.toLowerCase();
				catCodes.put(repoId.getAttributeValue("serverCode")+":"+repoId.getAttributeValue("code"), categName);

				if (categoryRepository.findOneByNameIgnoreCase(categName) == null) {
                    MetadataCategory category = new MetadataCategory();
                    category.setName(categName);
                    categoryRepository.save(category);
					addcateg = true;
				}
			}
		}

		if (addcateg) {
            categoryRepository.flush();
        }

		// --- return only maximum hits as directed by the harvest params
		int nrGroups = (numberOfHits / groupSize) + 1;
		for (int i = 1; i <= nrGroups; i++) {
            if (cancelMonitor.get()) {
                return serverResults;
            }

            int lower = ((i-1)*groupSize)+1;
			int upper = Math.min((i*groupSize),numberOfHits);
			request.getChild("from").setText(""+lower);  
			request.getChild("to").setText(""+upper);  

			// --- Loading results
			List<Document> list = s.presentDocuments(context, request, config);

			// --- Loading categories and groups
			localCateg = new CategoryMapper(context);
			localGroups = new GroupMapper(context);

            if(log.isDebugEnabled())
                log.debug("There are "+(list.size()-1)+" children in the results ("+lower+" to "+upper+")");

			boolean transformIt = false;
			Path thisXslt = context.getAppPath().resolve(Geonet.Path.IMPORT_STYLESHEETS);
			if (!params.getImportXslt().equals("none")) {
				thisXslt = thisXslt.resolve(params.getImportXslt());
				transformIt = true;
			}

			// --- For each record....
			for (Document doc : list) {
                if (cancelMonitor.get()) {
                    return serverResults;
                }

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
				HarvestResult result = serverResults.getServerResult(repoName);
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
					    HarvestError error = new HarvestError(e, log);
					    error.setDescription("Cannot transform XML, ignoring. Error was: "+e.getMessage());
						this.errors.add(error);
						error.printLog(log);
						result.badFormat++;
						continue; // skip this one
					}
				}

				// detect schema, extract uuid and add
				String schema = dataMan.autodetectSchema(md, null);
				if (schema == null) {
					log.warning("Skipping metadata with unknown schema.");
					result.unknownSchema++;
					continue;
				}

				String uuid = null;
				try {
					uuid = dataMan.extractUUID(schema, md);
				} catch (Exception e) {
                    HarvestError error = new HarvestError(e, log);
                    error.setDescription("Unable to extract UUID. " + e.getMessage());
                    this.errors.add(error);
                    error.printLog(log);
				}

				if (uuid == null || uuid.equals("")) {
					log.warning("Skipping metadata due to failure extracting uuid (uuid null or empty).");
					result.unretrievable++;
					continue;
				}

				log.info("  - Adding metadata with " + uuid);

				//--- generate a new metadata id
		
				String docType = "";
				if (!transformIt && (doc.getDocType() != null)) {
					docType = Xml.getString(doc.getDocType());
				}

				//--- check for duplicate uuid - violates constraints on metadata table
				//--- if we attempt insert
				boolean alreadyAdded = !newUuids.add(uuid);
				boolean alreadyInDb  = (dataMan.getMetadataId(uuid) != null);
				if (alreadyAdded || alreadyInDb) {
					log.error("Uuid "+uuid+" already exists in this set/database - cannot insert");
					result.couldNotInsert++;
					continue;
				}

                final String id;
                //
                // insert metadata
                //
                try {
                    int owner = 1;
                    if (params.getOwnerId() != null && !params.getOwnerId().isEmpty()) {
                        try {
                            owner = Integer.parseInt(params.getOwnerId());
                        } catch (NumberFormatException e) {
                            // skip
                        }
                    }

                    Metadata metadata = new Metadata().setUuid(uuid);
                    metadata.getDataInfo().
                            setSchemaId(schema).
                            setRoot(md.getQualifiedName()).
                            setType(MetadataType.METADATA).setDoctype(docType);
                    metadata.getSourceInfo().
                            setSourceId(params.getUuid()).
                            setOwner(owner).
                            setGroupOwner(1);
                    metadata.getHarvestInfo().
                            setHarvested(true).
                            setUuid(params.getUuid()).
                            setUri(params.getName());

                    addCategories(metadata, params.getCategories(), localCateg, context, log, null, false);
                    metadata = dataMan.insertMetadata(context, metadata, md, true, false, false, UpdateDatestamp.NO, false, false);

                    id = String.valueOf(metadata.getId());
                }
                catch (Exception e) {
                    HarvestError error = new HarvestError(e, log);
                    error.setDescription("Unable to insert metadata. "+e.getMessage());
                    this.errors.add(error);
                    error.printLog(log);
                    result.couldNotInsert++;
                    continue;
                }

                addPrivileges(id, params.getPrivileges(), localGroups, dataMan, context, log);

				// validate it here if requested
				if (params.getValidate() != HarvestValidationEnum.NOVALIDATION) {
					Document docVal;
					if (!transformIt && (doc.getDocType() != null)) {
						docVal = new Document(md, (DocType)doc.getDocType().detach());
					} else {
						docVal = new Document(md);
					}

					if (!dataMan.doValidate(schema, id, docVal, context.getLanguage())) {
						result.doesNotValidate++;
					} 
				}

                dataMan.flush();

                dataMan.indexMetadata(id, false);

                result.addedMetadata++;
            }
        }

        context.getBean(SearchManager.class).forceIndexChanges();

		return serverResults;
	}

	// ---------------------------------------------------------------------------
	// ---
	// --- Variables
	// ---
	// ---------------------------------------------------------------------------

	private Logger log;
	private final Z3950Params params;
	private ServiceContext context;
	private CategoryMapper localCateg;
	private GroupMapper localGroups;
    /**
     * Contains a list of accumulated errors during the executing of this harvest.
     */
    private List<HarvestError> errors = new LinkedList<HarvestError>();
	@Override
	public List<HarvestError> getErrors() {
		return errors;
	}
}
