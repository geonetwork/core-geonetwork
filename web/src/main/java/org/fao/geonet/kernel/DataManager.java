//==============================================================================
//===
//=== DataManager
//===
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

package org.fao.geonet.kernel;

import jeeves.constants.Jeeves;
import jeeves.exceptions.JeevesException;
import jeeves.exceptions.ServiceNotAllowedEx;
import jeeves.exceptions.XSDValidationErrorEx;
import jeeves.guiservices.session.JeevesUser;
import jeeves.resources.dbms.Dbms;
import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;
import jeeves.utils.Log;
import jeeves.utils.SerialFactory;
import jeeves.utils.Util;
import jeeves.utils.Xml;
import jeeves.utils.Xml.ErrorHandler;
import jeeves.xlink.Processor;
import org.apache.commons.lang.StringUtils;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Edit;
import org.fao.geonet.constants.Geocat;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.constants.Geonet.Namespaces;
import org.fao.geonet.constants.Params;
import org.fao.geonet.exceptions.NoSchemaMatchesException;
import org.fao.geonet.exceptions.SchemaMatchConflictException;
import org.fao.geonet.exceptions.SchematronValidationErrorEx;
import org.fao.geonet.kernel.csw.domain.CswCapabilitiesInfo;
import org.fao.geonet.kernel.csw.domain.CustomElementSet;
import org.fao.geonet.kernel.harvest.HarvestManager;
import org.fao.geonet.kernel.reusable.ProcessParams;
import org.fao.geonet.kernel.reusable.ReusableObjManager;
import org.fao.geonet.kernel.reusable.log.ReusableObjectLogger;
import org.fao.geonet.kernel.schema.MetadataSchema;
import org.fao.geonet.kernel.schema.SchemaDao;
import org.fao.geonet.kernel.schema.SchematronCriteria;
import org.fao.geonet.kernel.schema.SchematronCriteriaGroup;
import org.fao.geonet.kernel.schema.SchematronRequirement;
import org.fao.geonet.kernel.search.SearchManager;
import org.fao.geonet.kernel.search.spatial.Pair;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.lib.Lib;
import org.fao.geonet.services.extent.ExtentManager;
import org.fao.geonet.util.ISODate;
import org.fao.geonet.util.ThreadUtils;
import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.Parent;
import org.jdom.filter.ElementFilter;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Handles all operations on metadata (select,insert,update,delete etc...).
 *
 */
public class DataManager {

	//--------------------------------------------------------------------------
	//---
	//--- Constructor
	//---
	//--------------------------------------------------------------------------

    /**
     *
     * @return
     */
    public EditLib getEditLib() {
        return editLib;
    }

    /**
     * Initializes the search manager and index not-indexed metadata.
     *
     * @throws Exception
     */
	public DataManager(DataManagerParameter parameterObject) throws Exception {
		searchMan = parameterObject.searchManager;
		accessMan = parameterObject.accessManager;
		settingMan= parameterObject.settingsManager;
		schemaMan = parameterObject.schemaManager;
		editLib = new EditLib(schemaMan);
        servContext=parameterObject.context;

		this.baseURL = parameterObject.baseURL;
        this.dataDir = parameterObject.dataDir;
        this.thesaurusDir = parameterObject.thesaurusDir;
		this.appPath = parameterObject.appPath;

		stylePath = parameterObject.context.getAppPath() + FS + Geonet.Path.STYLESHEETS + FS;
		this.reusableObjMan = parameterObject.reusableObjMan;
        this.extentMan = parameterObject.extentMan;
		this.xmlSerializer = parameterObject.xmlSerializer;
		this.svnManager    = parameterObject.svnManager;

		UserSession session = new UserSession();
		session.loginAs(new JeevesUser(servContext.getProfileManager()).setUsername("admin").setId("-1").setProfile(Geonet.Profile.ADMINISTRATOR));
        servContext.setUserSession(session);
		init(parameterObject.context, parameterObject.dbms, false);
	}

	/**
	 * Init Data manager and refresh index if needed. 
	 * Can also be called after GeoNetwork startup in order to rebuild the lucene 
	 * index
	 * 
	 * @param context
	 * @param dbms
	 * @param force         Force reindexing all from scratch
	 *
	 **/
	public synchronized void init(ServiceContext context, Dbms dbms, Boolean force) throws Exception {

	    
		// get all metadata from DB
		Element result = dbms.select("SELECT id, changeDate FROM Metadata ORDER BY id ASC");
		
        if(Log.isDebugEnabled(Geonet.DATA_MANAGER))
		    Log.debug(Geonet.DATA_MANAGER, "DB CONTENT:\n'"+ Xml.getString(result) +"'");

		// get lastchangedate of all metadata in index
		Map<String,String> docs = searchMan.getDocsChangeDate();

		// set up results HashMap for post processing of records to be indexed
		ArrayList<String> toIndex = new ArrayList<String>();

        if (Log.isDebugEnabled(Geonet.DATA_MANAGER))
            Log.debug(Geonet.DATA_MANAGER, "INDEX CONTENT:");

		// index all metadata in DBMS if needed
		for(int i = 0; i < result.getContentSize(); i++) {
			// get metadata
			Element record = (Element) result.getContent(i);
			String  id     = record.getChildText("id");
			int iId = Integer.parseInt(id);

            if (Log.isDebugEnabled(Geonet.DATA_MANAGER))
                Log.debug(Geonet.DATA_MANAGER, "- record ("+ id +")");

			String idxLastChange = docs.get(id);

			// if metadata is not indexed index it
			if (idxLastChange == null) {
				Log.debug(Geonet.DATA_MANAGER, "-  will be indexed");
				toIndex.add(id);
	
			// else, if indexed version is not the latest index it
			} else {
				docs.remove(id);
	
				String lastChange    = record.getChildText("changedate");

                if(Log.isDebugEnabled(Geonet.DATA_MANAGER))
                    Log.debug(Geonet.DATA_MANAGER, "- lastChange: " + lastChange);
                if(Log.isDebugEnabled(Geonet.DATA_MANAGER))
                    Log.debug(Geonet.DATA_MANAGER, "- idxLastChange: " + idxLastChange);
	
				// date in index contains 't', date in DBMS contains 'T'
				if (force || !idxLastChange.equalsIgnoreCase(lastChange)) {
                    if(Log.isDebugEnabled(Geonet.DATA_MANAGER))
                        Log.debug(Geonet.DATA_MANAGER, "-  will be indexed");
					toIndex.add(id);
				}
			}
		}

		// if anything to index then schedule it to be done after servlet is
		// up so that any links to local fragments are resolvable
		if ( toIndex.size() > 0 ) {
            batchRebuild(context,toIndex);
		}

		if (docs.size() > 0) { // anything left?
            if(Log.isDebugEnabled(Geonet.DATA_MANAGER))
                Log.debug(Geonet.DATA_MANAGER, "INDEX HAS RECORDS THAT ARE NOT IN DB:");
		}

		// remove from index metadata not in DBMS
		for ( String id : docs.keySet() )
		{
			searchMan.delete("_id", id);

            if(Log.isDebugEnabled(Geonet.DATA_MANAGER))
                Log.debug(Geonet.DATA_MANAGER, "- removed record (" + id + ") from index");
		}
	}

    /**
     * TODO javadoc.
     *
     * @param context
     * @throws Exception
     */
	public synchronized void rebuildIndexXLinkedMetadata(ServiceContext context) throws Exception {
		
		// get all metadata with XLinks
		Set<Integer> toIndex = searchMan.getDocsWithXLinks();

        if(Log.isDebugEnabled(Geonet.DATA_MANAGER))
            Log.debug(Geonet.DATA_MANAGER, "Will index "+toIndex.size()+" records with XLinks");
		if ( toIndex.size() > 0 ) {
			// clean XLink Cache so that cache and index remain in sync
			Processor.clearCache();

            ArrayList<String> stringIds = new ArrayList<String>();
            for (Integer id : toIndex) {
                stringIds.add(id.toString());
            }
            // execute indexing operation
            batchRebuild(context,stringIds);
		}
	}
    
    /**
     * TODO javadoc.
     *
     * @param context
     * @param ids
     */
    private void batchRebuild(ServiceContext context, List<String> ids) {

        // split reindexing task according to number of processors we can assign
        int threadCount = ThreadUtils.getNumberOfThreads();
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        int perThread;
        if (ids.size() < threadCount) perThread = ids.size();
        else perThread = ids.size() / threadCount;
        int index = 0;

        boolean performValidation = false;
        boolean processSharedObjects = true;

        while(index < ids.size()) {
            int start = index;
            int count = Math.min(perThread,ids.size()-start);
            // create threads to process this chunk of ids
            Runnable worker = new IndexMetadataTask(context, processSharedObjects, ids, start, count, performValidation);
            executor.execute(worker);
            index += count;
        }

        executor.shutdown();
    }

    /**
     * TODO javadoc.
     * @param dbms dbms
     * @param id metadata id
     * @throws Exception hmm
     */
    public void indexInThreadPoolIfPossible(Dbms dbms, String id, boolean processSharedObjects) throws Exception {
        if(ServiceContext.get() == null ) {
            boolean indexGroup = false;
            indexMetadata(dbms, id, processSharedObjects, getServiceContext());
        } else {
            indexInThreadPool(ServiceContext.get(), id, dbms, processSharedObjects);
        }
    }

    /**
     * Adds metadata ids to the thread pool for indexing.
     *
     * @param context
     * @param id
     * @throws SQLException
     */
    public void indexInThreadPool(ServiceContext context, String id, Dbms dbms, boolean processSharedObjects) throws SQLException {
        indexInThreadPool(context, Collections.singletonList(id), dbms, processSharedObjects, false);
    }   
    public void indexInThreadPool(ServiceContext context, String id, Dbms dbms, boolean processSharedObjects, boolean performValidation) throws SQLException {
        indexInThreadPool(context, Collections.singletonList(id), dbms, processSharedObjects, performValidation);
    }
    /**
     * Adds metadata ids to the thread pool for indexing.
     *
     * @param context
     * @param ids
     * @throws SQLException
     */
    public void indexInThreadPool(ServiceContext context, List<String> ids, Dbms dbms, boolean processSharedObjects, boolean performValidation) throws SQLException {

        if(dbms != null) dbms.commit();

        try {
            GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);

            if (ids.size() > 0) {
                Runnable worker = new IndexMetadataTask(context, processSharedObjects, ids, performValidation);
                gc.getThreadPool().runTask(worker, 5, TimeUnit.SECONDS);
            }
        } 
        catch (Exception e) {
            Log.error(Geonet.DATA_MANAGER, e.getMessage());
            e.printStackTrace();
            // TODO why swallow
        }
    }

    Set<IndexMetadataTask> indexing = Collections.synchronizedSet(new HashSet<IndexMetadataTask>());

    /**
     * TODO javadoc.
     */
    final class IndexMetadataTask implements Runnable {

        private final ServiceContext context;
        private final List<String> ids;
        private final int beginIndex;
        private final int count;
        private final boolean processSharedObjects;
        private JeevesUser user;

        IndexMetadataTask(ServiceContext context, boolean processSharedObjects, List<String> ids, boolean performValidation) {
            synchronized (indexing) {
                indexing.add(this);
            }
            this.context = context;
            this.ids = ids;
            this.beginIndex = 0;
            this.count = ids.size();
            this.processSharedObjects = processSharedObjects;
            if(context.getUserSession() != null) {
                this.user = context.getUserSession().getPrincipal();
            }
        }
        IndexMetadataTask(ServiceContext context, boolean processSharedObjects, List<String> ids, int beginIndex, int count, boolean performValidation) {
            synchronized (indexing) {
                indexing.add(this);
            }
            this.context = context;
            this.ids = ids;
            this.beginIndex = beginIndex;
            this.count = count;
            this.processSharedObjects = processSharedObjects;
            if(context.getUserSession() != null) {
                this.user = context.getUserSession().getPrincipal();
            }
        }

        /**
         * TODO javadoc.
         */
        public void run() {
            context.setAsThreadLocal();
            try {
                // poll context to see whether servlet is up yet
                while (!context.isServletInitialized()) {
                    if(Log.isDebugEnabled(Geonet.DATA_MANAGER))
                        Log.debug(Geonet.DATA_MANAGER, "Waiting for servlet to finish initializing..");
                    Thread.sleep(10000); // sleep 10 seconds
                }
                Dbms dbms = (Dbms) context.getResourceManager().openDirect(Geonet.Res.MAIN_DB);
                if(user != null && context.getUserSession().getUserId() == null) {
                    context.getUserSession().loginAs(user);
                }
                try {
                    if (ids.size() > 1) {
                        // servlet up so safe to index all metadata that needs indexing
                        try {
                            for(int i=beginIndex; i<beginIndex+count; i++) {
                                try {
                                    indexMetadata(dbms, ids.get(i).toString(), processSharedObjects, context, false, false,
                                            false);
                                }
                                catch (Exception e) {
                                    Log.error(Geonet.INDEX_ENGINE, "Error indexing metadata '"+ids.get(i)+"': "+e.getMessage()+"\n"+ Util.getStackTrace(e));
                                }
                            }
                        }
                        finally {
                        }
                    }
                    else {
                        indexMetadata(dbms, ids.get(0), processSharedObjects, context, false, false, false);
                    }
                }
                finally {
                    //-- commit Dbms resource (which makes it available to pool again)
                    //-- to avoid exhausting Dbms pool
                    context.getResourceManager().close(Geonet.Res.MAIN_DB, dbms);
                }
            }
            catch (Exception e) {
                Log.error(Geonet.DATA_MANAGER, "Reindexing thread threw exception");
                e.printStackTrace();
            } finally {
                synchronized (indexing) {
                    indexing.remove(this);
                }
            }
        }
    }
    public void indexMetadata(Dbms dbms, String id, boolean processSharedObjects, ServiceContext servContext) throws Exception {
        indexMetadata(dbms, id, processSharedObjects, servContext, false, false, false);
    }
    /**
     * TODO javadoc.
     *
     *
     * @param dbms
     * @param id
     * @param reloadXLinks
     * @throws Exception
     */
	public void indexMetadata(Dbms dbms, String id, boolean processSharedObjects, ServiceContext servContext, boolean performValidation,
                              boolean fastIndex, boolean reloadXLinks) throws Exception {
        try {
            Vector<Element> moreFields = new Vector<Element>();
            int id$ = new Integer(id);
            
            // get metadata, extracting and indexing any xlinks

            Element md   = xmlSerializer.selectNoXLinkResolver(dbms, "Metadata", id, true, servContext);

            if (reloadXLinks) {
                Processor.detachXLink(md, servContext);

            }

            // get metadata table fields
            String query = "SELECT schemaId, createDate, changeDate, source, isTemplate, root, " +
                "title, uuid, isHarvested, owner, groupOwner, popularity, rating FROM Metadata WHERE id = ?";

            Element rec = dbms.select(query, id$).getChild("record");

            String  schema     = rec.getChildText("schemaid");
            String  createDate = rec.getChildText("createdate");
            String  changeDate = rec.getChildText("changedate");
            String  source     = rec.getChildText("source");
            String  isTemplate = rec.getChildText("istemplate");
            String  root       = rec.getChildText("root");
            String  title      = rec.getChildText("title");
            String  uuid       = rec.getChildText("uuid");
            String  isHarvested= rec.getChildText("isharvested");
            String  owner      = rec.getChildText("owner");
            String  groupOwner = rec.getChildText("groupowner");
            String  popularity = rec.getChildText("popularity");
            String  rating     = rec.getChildText("rating");

            if(Log.isDebugEnabled(Geonet.DATA_MANAGER)) {
                Log.debug(Geonet.DATA_MANAGER, "record schema (" + schema + ")"); //DEBUG
                Log.debug(Geonet.DATA_MANAGER, "record createDate (" + createDate + ")"); //DEBUG
            }

            if(schema.trim().equals("iso19139.che") && !fastIndex) {
                try {
                    /*
                     * Geocat doesn't permit multilingual elements to have characterString elements only LocalizedString elements.
                     * This transformation ensures this property
                     */
                    md = Xml.transform(md, stylePath+"characterstring-to-localisedcharacterstring.xsl");
                    String parentUuid = null;
                    md = updateFixedInfo(schema, id, uuid, md, parentUuid , UpdateDatestamp.no, dbms, servContext);

                    xmlSerializer.update(dbms, id, md, new ISODate().toString(), false, uuid, servContext);
                } catch (Throwable t) {
                    Log.error(Geonet.DATA_MANAGER, "Error converting Characterstring to PTFreeText elements. For metadata "+id, t);
                }

            }
             if("n".equalsIgnoreCase(isHarvested) && processSharedObjects && schema.trim().equals("iso19139.che")) {
            	try {
	                ProcessParams processParameters = new ProcessParams(dbms, ReusableObjectLogger.THREAD_SAFE_LOGGER, id, md, md, thesaurusMan, extentMan, baseURL, settingMan, false, null,servContext);
	                List<Element> modified = reusableObjMan.process(processParameters);

	                if(modified != null && !modified.isEmpty()) {
	                    md = modified.get(0);
	                    xmlSerializer.update(dbms, id, md, new ISODate().toString(), false, null, servContext);
	                }
            	} catch (Exception e) {
            	    Element stackTrace = JeevesException.toElement(e);
            		Log.error(Geonet.DATA_MANAGER, "error while trying to update shared objects of metadata, "+id+":\n "+Xml.getString(stackTrace)); //DEBUG
            	}
            }
            if ("n".equalsIgnoreCase(isHarvested) && processSharedObjects && schema.trim().equals("iso19139.che")) {
                List<Attribute> xlinks = Processor.getXLinks(md);
                if (xlinks.size() > 0) {
                    moreFields.add(SearchManager.makeField("_hasxlinks", "1", true, true));
                    StringBuilder sb = new StringBuilder();
                    for (Attribute xlink : xlinks) {
                        sb.append(xlink.getValue()); sb.append(" ");
                    }
                    moreFields.add(SearchManager.makeField("_xlink", sb.toString(), true, true));
                    Processor.processXLink(md,servContext);
                    xmlSerializer.update(dbms, id, md, new ISODate().toString(), false, null, servContext);
                } else {
                    moreFields.add(SearchManager.makeField("_hasxlinks", "0", true, true));
                }
            }
            else {
                List<Attribute> xlinks = Processor.getXLinks(md);
                if(xlinks.size()>0) moreFields.add(SearchManager.makeField("_hasxlinks", "1", true, true));
            }


            if(performValidation) {
                try {
                    Element xlinkResolved = Processor.processXLink((Element) md.clone(), servContext);
                    doValidate(servContext, dbms, schema, id, xlinkResolved, servContext.getLanguage(), false);
                } catch (Exception e) {
                    Element stackTrace = JeevesException.toElement(e);
                    Log.error(Geonet.DATA_MANAGER, "error while trying to validating metadata (during indexing), "+id+":\n "+Xml.getString(stackTrace)); //DEBUG
                }
            }
            
            moreFields.add(SearchManager.makeField("_root",        root,        true, true));
            moreFields.add(SearchManager.makeField("_schema",      schema,      true, true));
            moreFields.add(SearchManager.makeField("_createDate",  createDate,  true, true));
            moreFields.add(SearchManager.makeField("_changeDate",  changeDate,  true, true));
            moreFields.add(SearchManager.makeField("_source",      source,      true, true));
            //Swisstopo specific
            if (isHarvested.contains("n")) {
                moreFields.add(SearchManager.makeField("_catalog",      groupOwner,      true, true));
            }
            moreFields.add(SearchManager.makeField("_catalog",      source,      true, true));
            moreFields.add(SearchManager.makeField("_isTemplate",  isTemplate,  true, true));
            moreFields.add(SearchManager.makeField("_title",       title,       true, true));
            moreFields.add(SearchManager.makeField("_uuid",        uuid,        true, true));
            moreFields.add(SearchManager.makeField("_isHarvested", isHarvested, true, true));
            moreFields.add(SearchManager.makeField("_owner",       owner,       true, true));
            moreFields.add(SearchManager.makeField("_dummy",       "0",        false, true));
            moreFields.add(SearchManager.makeField("_popularity",  popularity,  true, true));
            moreFields.add(SearchManager.makeField("_rating",      rating,      true, true));

            if (owner != null) {
                String userQuery = "SELECT username, surname, name, profile FROM Users WHERE id = ?";
                
                Element user = dbms.select(userQuery,  new Integer(owner)).getChild("record");
                
                if (user != null) {
                    moreFields.add(SearchManager.makeField("_userinfo", 
                           user.getChildText("username") + "|" + user.getChildText("surname") + "|" +
                           user.getChildText("name") + "|" + user.getChildText("profile"), 
                           true, false));
                }
            }
            try {
            	int groupOwnerId = Integer.valueOf(groupOwner);
                moreFields.add(SearchManager.makeField("_groupOwner", groupOwner, true, true));
				Element groupInfo = dbms.select("SELECT logouuid, website FROM groups where id=?", groupOwnerId ).getChild("record");
                moreFields.add(SearchManager.makeField("groupLogoUuid", groupInfo.getChildText("logouuid"), true, false));
                moreFields.add(SearchManager.makeField("groupWebsite", groupInfo.getChildText("website"), true, false));
            } catch (NumberFormatException nfe) {
            	// that's ok, sometime groupOwner is blank
            }

            // get privileges
            List operations = dbms
                              .select("SELECT groupId, operationId, g.name FROM OperationAllowed o, groups g WHERE g.id = o.groupId AND metadataId = ? ORDER BY operationId ASC", id$)
                                 .getChildren();

            boolean isPublished = false;
            for (Object operation1 : operations) {
                Element operation = (Element) operation1;
                String groupId = operation.getChildText("groupid");
                String operationId = operation.getChildText("operationid");
                moreFields.add(SearchManager.makeField("_op" + operationId, groupId, true, true));
                if(operationId.equals("0")) {
                	if ("1".equals(groupId)) {
                		isPublished = true;
                	}
                	String name = operation.getChildText("name");
                	moreFields.add(SearchManager.makeField("_groupPublished", name, true, true));
                }
            }
            // get categories
            List categories = dbms
                                .select("SELECT id, name FROM MetadataCateg, Categories WHERE metadataId = ? AND categoryId = id ORDER BY id", id$)
                                    .getChildren();

            for (Object category1 : categories) {
                Element category = (Element) category1;
                String categoryName = category.getChildText("name");
                moreFields.add(SearchManager.makeField("_cat", categoryName, true, true));
            }

            // get status
            List<Element> statuses = dbms.select("SELECT statusId, userId, changeDate FROM MetadataStatus WHERE metadataId = ? ORDER BY changeDate DESC", id$)
                                    .getChildren();
						if (statuses.size() > 0) {
							Element stat = (Element)statuses.get(0);
							String status = stat.getChildText("statusid");
              moreFields.add(SearchManager.makeField("_status", status, true, true));
							String statusChangeDate = stat.getChildText("changedate");
              moreFields.add(SearchManager.makeField("_statusChangeDate", statusChangeDate, true, true));
						}

            // getValidationInfo
            // -1 : not evaluated
            // 0 : invalid
            // 1 : valid
            List<Element> validationInfo = dbms
                                             .select("SELECT valType, status FROM Validation WHERE metadataId = ?", id$)
                                                 .getChildren();
            if (validationInfo.size() == 0) {
                moreFields.add(SearchManager.makeField("_valid", "-1", true, true));
            }
            else {
                String isValid = "1";

                for (Object elem : validationInfo) {
                    Element vi = (Element) elem;
                    String type = vi.getChildText("valtype");
                    String status = vi.getChildText("status");
                    if (!type.equals(Geocat.INSPIRE_SCHEMATRON_ID) && "0".equals(status)) {
                        isValid = "0";
                    }
                    moreFields.add(SearchManager.makeField("_valid_" + type, status, true, true));
                }
                moreFields.add(SearchManager.makeField("_valid", isValid, true, true));

                // toPublish index field: metadata is valid, schema=iso19139.che, not a template and not harvested
                if (isValid.equals("1") && schema.trim().equals("iso19139.che")
                        && isTemplate.equals("n") && isHarvested.equals("n") && !isPublished) {
                    moreFields.add(SearchManager.makeField("toPublish", "y", true, true));
                }
            }
            searchMan.index(schemaMan.getSchemaDir(schema), md, id, moreFields, isTemplate, title);
        }
		catch (Exception x) {
			Log.error(Geonet.DATA_MANAGER, "The metadata document index with id=" + id + " is corrupt/invalid - ignoring it. Error: " + x.getMessage());
			x.printStackTrace();
		}
	}

    /**
     *
     * @param beginAt
     * @param interval
     * @throws Exception
     */
	public void rescheduleOptimizer(Calendar beginAt, int interval) throws Exception {
		searchMan.rescheduleOptimizer(beginAt, interval);
	}

    /**
     *
     * @throws Exception
     */
	public void disableOptimizer() throws Exception {
		searchMan.disableOptimizer();
	}



	//--------------------------------------------------------------------------
	//---
	//--- Schema management API
	//---
	//--------------------------------------------------------------------------

    /**
     *
     * @param hm
     */
	public void setHarvestManager(HarvestManager hm) {
		harvestMan = hm;
	}

    /**
     *
     * @param name
     * @return
     */
	public MetadataSchema getSchema(String name) {
		return schemaMan.getSchema(name);
	}

    /**
     *
     * @return
     */
	public Set<String> getSchemas() {
		return schemaMan.getSchemas();
	}

    /**
     *
     * @param name
     * @return
     */
	public boolean existsSchema(String name) {
		return schemaMan.existsSchema(name);
	}

    /**
     *
     * @param name
     * @return
     */
	public String getSchemaDir(String name) {
		return schemaMan.getSchemaDir(name);
	}

    /**
     * Use this validate method for XML documents with dtd.
     *
     * @param schema
     * @param doc
     * @throws Exception
     */
	public void validate(String schema, Document doc) throws Exception {
		Xml.validate(doc);	
	}

    /**
     * Use this validate method for XML documents with xsd validation.
     *
     * @param schema
     * @param md
     * @throws Exception
     */
	public void validate(String schema, Element md) throws Exception {
		String schemaLoc = md.getAttributeValue("schemaLocation", Namespaces.XSI);
        if(Log.isDebugEnabled(Geonet.DATA_MANAGER))
            Log.debug(Geonet.DATA_MANAGER, "Extracted schemaLocation of "+schemaLoc);
		if (schemaLoc == null) schemaLoc = "";

		if (schema == null) {
			// must use schemaLocation 
			Xml.validate(md);
		} else {
			// if schemaLocation use that
			if (!schemaLoc.equals("")) { 
				Xml.validate(md);
			// otherwise use supplied schema name 
			} else {
				Xml.validate(getSchemaDir(schema) + Geonet.File.SCHEMA, md);
			}
		}
	}

    /**
     * TODO javadoc.
     *
     * @param schema
     * @param md
     * @param eh
     * @return
     * @throws Exception
     */
	public Element validateInfo(String schema, Element md, ErrorHandler eh) throws Exception {
	    md.removeAttribute("schemaLocation", Namespaces.XSI);

		String schemaLoc = md.getAttributeValue("schemaLocation", Namespaces.XSI);
        if(Log.isDebugEnabled(Geonet.DATA_MANAGER))
            Log.debug(Geonet.DATA_MANAGER, "Extracted schemaLocation of "+schemaLoc);
		if (schemaLoc == null) schemaLoc = "";

		if (schema == null) {
			// must use schemaLocation 
			return Xml.validateInfo(md, eh);
		} else {
			// if schemaLocation use that
			if (!schemaLoc.equals("")) { 
				return Xml.validateInfo(md, eh);
			// otherwise use supplied schema name 
			} else {
				return Xml.validateInfo(getSchemaDir(schema) + Geonet.File.SCHEMA, md, eh);
			}
		}
	}

    /**
     * Creates XML schematron report.
     * @param schema
     * @param md
     * @param lang
     * @return
     * @throws Exception
     */
	public Element doSchemaTronForEditor(ServiceContext context, String schema,Element md,String lang) throws Exception {
    	// enumerate the metadata xml so that we can report any problems found  
    	// by the schematron_xml script to the geonetwork editor 
    	editLib.enumerateTree(md);

        // NOTE: this method assumes that you've run enumerateTree on the
        // metadata

        MetadataSchema metadataSchema = getSchema(schema);

        Dbms dbms = (Dbms) context.getResourceManager().open(Geonet.Res.MAIN_DB);;
        final String mdIdString = getMetadataId(dbms, extractUUID(metadataSchema.getName(), md));
        int metadataId = -1;
        if (mdIdString != null) {
            metadataId = Integer.parseInt(mdIdString);
        }

        // Schematron report is composed of one or more report(s)
        // for each set of rules.
        Element schemaTronXmlOut = applyCustomSchematronRules(dbms, metadataSchema.getName(), metadataId, md, lang, null);
    	// remove editing info added by enumerateTree 
    	editLib.removeEditingInfo(md); 
    	
    	return schemaTronXmlOut;
	}

    /**
     * TODO javadoc.
     *
     * @param dbms
     * @param id
     * @return
     * @throws Exception
     */
	public String getMetadataSchema(Dbms dbms, String id) throws Exception {
		List list = dbms.select("SELECT schemaId FROM Metadata WHERE id = ?", new Integer(id)).getChildren();

		if (list.size() == 0)
			throw new IllegalArgumentException("Metadata not found for id : " +id);
		else {
			// get metadata
			Element record = (Element) list.get(0);
			return record.getChildText("schemaid");
		}
	}

    /**
     *
     * @param context
     * @param id
     * @param md
     * @throws Exception
     */
	public void versionMetadata(ServiceContext context, String id, Element md) throws Exception {
	    if (svnManager != null) {
	        svnManager.createMetadataDir(id, context, md);
	    }
	}

    /**
     *
     * @param md
     * @return
     * @throws Exception
     */
	public Element enumerateTree(Element md) throws Exception {
		editLib.enumerateTree(md);
		return md;
	}

        /**
     * Validates metadata against XSD and schematron files related to metadata schema throwing XSDValidationErrorEx
     * if xsd errors or SchematronValidationErrorEx if schematron rules fails.
     *
     * @param schema
     * @param xml
     * @param context
     * @throws Exception
     */
	public static void validateMetadata(String schema, Element xml, ServiceContext context) throws Exception
	{
		validateMetadata(schema, xml, context, " ");
	}

    /**
     * Validates metadata against XSD and schematron files related to metadata schema throwing XSDValidationErrorEx
     * if xsd errors or SchematronValidationErrorEx if schematron rules fails.
     *
     * @param schema
     * @param xml
     * @param context
     * @param fileName
     * @throws Exception
     */
	public static void validateMetadata(String schema, Element xml, ServiceContext context, String fileName) throws Exception
	{
		GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);

		DataManager dataMan = gc.getDataManager();

		dataMan.setNamespacePrefix(xml);
		try {
			dataMan.validate(schema, xml);
		} catch (XSDValidationErrorEx e) {
			if (!fileName.equals(" ")) {
				throw new XSDValidationErrorEx(e.getMessage()+ "(in "+fileName+"): ",e.getObject());
			} else {
				throw new XSDValidationErrorEx(e.getMessage(),e.getObject());
			}
		}

		//-----------------------------------------------------------------------
		//--- if the uuid does not exist we generate it

		String uuid = dataMan.extractUUID(schema, xml);

		if (uuid.length() == 0)
			uuid = UUID.randomUUID().toString();

		//--- Now do the schematron validation on this file - if there are errors
		//--- then we say what they are!
		//--- Note we have to use uuid here instead of id because we don't have
		//--- an id...

		Element schemaTronXml = dataMan.doSchemaTronForEditor(context, schema,xml,context.getLanguage());
		xml.detach();
		if (schemaTronXml != null && schemaTronXml.getContent().size() > 0) {
			Element schemaTronReport = dataMan.doSchemaTronForEditor(context, schema,xml,context.getLanguage());

            List<Namespace> theNSs = new ArrayList<Namespace>();
            theNSs.add(Namespace.getNamespace("geonet", "http://www.fao.org/geonetwork"));
            theNSs.add(Namespace.getNamespace("svrl", "http://purl.oclc.org/dsdl/svrl"));

            ArrayList informationalReports = new ArrayList(Xml.selectNodes(schemaTronReport, "geonet:report[@geonet:required != 'true']", theNSs));
            for (Object informationalReport : informationalReports) {
                ((Element)informationalReport).detach();
            }
            Element failedAssert = Xml.selectElement(schemaTronReport, "geonet:report[@geonet:required = 'true']/svrl:schematron-output/svrl:failed-assert", theNSs);

            Element failedSchematronVerification = Xml.selectElement(schemaTronReport, "geonet:report[@geonet:required = 'true']/geonet:schematronVerificationError", theNSs);

            if ((failedAssert != null) || (failedSchematronVerification != null)) {

			    throw new SchematronValidationErrorEx("Schematron errors detected for file "+fileName+" - "
					    + Xml.getString(schemaTronReport) + " for more details",schemaTronReport);
            }
		}

	}

    /**
     * Valid the metadata record against its schema. For each error found, an xsderror attribute is added to
	 * the corresponding element trying to find the element based on the xpath return by the ErrorHandler.
     *
     * @param schema
     * @param md
     * @return
     * @throws Exception
     */
	private synchronized Element getXSDXmlReport(String schema, Element md) {
		// NOTE: this method assumes that enumerateTree has NOT been run on the metadata
		ErrorHandler errorHandler = new ErrorHandler();
		errorHandler.setNs(Edit.NAMESPACE);
		Element xsdErrors;
		
		try {
		    xsdErrors = validateInfo(schema,
				md, errorHandler);
		}catch (Exception e) {
		    xsdErrors = JeevesException.toElement(e);
		    return xsdErrors;
        }
		
		if (xsdErrors != null) {
			MetadataSchema mds = getSchema(schema);
			List<Namespace> schemaNamespaces = mds.getSchemaNS();
		
			//-- now get each xpath and evaluate it
			//-- xsderrors/xsderror/{message,xpath} 
			List list = xsdErrors.getChildren();
			for (Object o : list) {
				Element elError = (Element) o;
				String xpath = elError.getChildText("xpath", Edit.NAMESPACE);
				String message = elError.getChildText("message", Edit.NAMESPACE);
				message = "\\n" + message;

				//-- get the element from the xpath and add the error message to it 
				Element elem = null;
				try {
					elem = Xml.selectElement(md, xpath, schemaNamespaces);
				} catch (JDOMException je) {
					je.printStackTrace();
					Log.error(Geonet.DATA_MANAGER,"Attach xsderror message to xpath "+xpath+" failed: "+je.getMessage());
				}
				if (elem != null) {
					String existing = elem.getAttributeValue("xsderror",Edit.NAMESPACE);
					if (existing != null) message = existing + message;
					elem.setAttribute("xsderror",message,Edit.NAMESPACE);
				} else {
					Log.warning(Geonet.DATA_MANAGER,"WARNING: evaluating XPath "+xpath+" against metadata failed - XSD validation message: "+message+" will NOT be shown by the editor");
				}
			}
		}
		return xsdErrors;
	}

    /**
     *
     * @return
     */
	public AccessManager getAccessManager() {
		return accessMan;
	}

	//--------------------------------------------------------------------------
	//---
	//--- General purpose API
	//---
	//--------------------------------------------------------------------------

    /**
     * Extract UUID from the metadata record using the schema
     * XSL for UUID extraction ({@link Geonet.File.EXTRACT_UUID})
     * 
     * @param schema
     * @param md
     * @return
     * @throws Exception
     */
	public String extractUUID(String schema, Element md) throws Exception {
		String styleSheet = getSchemaDir(schema) + Geonet.File.EXTRACT_UUID;
		String uuid       = Xml.transform(md, styleSheet).getText().trim();

        if(Log.isDebugEnabled(Geonet.DATA_MANAGER))
            Log.debug(Geonet.DATA_MANAGER, "Extracted UUID '"+ uuid +"' for schema '"+ schema +"'");

		//--- needed to detach md from the document
		md.detach();

		return uuid;
	}


    /**
     *
     * @param schema
     * @param md
     * @return
     * @throws Exception
     */
	public String extractDateModified(String schema, Element md) throws Exception {
		String styleSheet = getSchemaDir(schema) + Geonet.File.EXTRACT_DATE_MODIFIED;
		String dateMod    = Xml.transform(md, styleSheet).getText().trim();

        if(Log.isDebugEnabled(Geonet.DATA_MANAGER))
            Log.debug(Geonet.DATA_MANAGER, "Extracted Date Modified '"+ dateMod +"' for schema '"+ schema +"'");

		//--- needed to detach md from the document
		md.detach();

		return dateMod;
	}

    /**
     *
     * @param schema
     * @param uuid
     * @param md
     * @return
     * @throws Exception
     */
	public Element setUUID(String schema, String uuid, Element md) throws Exception {
		//--- setup environment

		Element env = new Element("env");
		env.addContent(new Element("uuid").setText(uuid));

		//--- setup root element

		Element root = new Element("root");
		root.addContent(md.detach());
		root.addContent(env.detach());

		//--- do an XSL  transformation

		String styleSheet = getSchemaDir(schema) + Geonet.File.SET_UUID;

		return Xml.transform(root, styleSheet);
	}

    /**
     *
     * @param dbms
     * @param harvestingSource
     * @return
     * @throws Exception
     */
	@SuppressWarnings("unchecked")
	public List<Element> getMetadataByHarvestingSource(Dbms dbms, String harvestingSource) throws Exception {
		String query = "SELECT id FROM Metadata WHERE harvestUuid=?";
		return dbms.select(query, harvestingSource).getChildren();
	}

    /**
     *
     * @param md
     * @return
     * @throws Exception
     */
	public Element extractSummary(Element md) throws Exception {
		String styleSheet = stylePath + Geonet.File.METADATA_BRIEF;
		Element summary       = Xml.transform(md, styleSheet);
        if (Log.isDebugEnabled(Geonet.DATA_MANAGER))
		    Log.debug(Geonet.DATA_MANAGER, "Extracted summary '\n"+Xml.getString(summary));

		//--- needed to detach md from the document
		md.detach();

		return summary;
	}

    /**
     *
     * @param dbms
     * @param uuid
     * @return
     * @throws Exception
     */
	public String getMetadataId(Dbms dbms, String uuid) throws Exception {
		String query = "SELECT id FROM Metadata WHERE uuid=?";

		List list = dbms.select(query, uuid).getChildren();

		if (list.size() == 0)
			return null;

		Element record = (Element) list.get(0);

		return record.getChildText("id");
	}

    /**
     *
     * @param dbms
     * @param id
     * @return
     * @throws Exception
     */
	public String getMetadataUuid(Dbms dbms, String id) throws Exception {
		String query = "SELECT uuid FROM Metadata WHERE id=?";

		List list = dbms.select(query, new Integer(id)).getChildren();

		if (list.size() == 0)
			return null;

		Element record = (Element) list.get(0);

		return record.getChildText("uuid");
	}

    /**
     *
     * @param dbms
     * @param id
     * @return
     * @throws Exception
     */
	public String getMetadataTemplate(Dbms dbms, String id) throws Exception {
		String query = "SELECT istemplate FROM Metadata WHERE id=?";

		List list = dbms.select(query, new Integer(id)).getChildren();

		if (list.size() == 0)
			return null;

		Element record = (Element) list.get(0);

		return record.getChildText("istemplate");
	}

    /**
     *
     * @param dbms
     * @param id
     * @return
     * @throws Exception
     */
	public MdInfo getMetadataInfo(Dbms dbms, String id) throws Exception {
		String query = "SELECT id, uuid, schemaId, isTemplate, isHarvested, createDate, "+
							"       changeDate, source, title, root, owner, groupOwner, displayOrder "+
							"FROM   Metadata "+
							"WHERE id=?";

		List list = dbms.select(query, new Integer(id)).getChildren();

		if (list.size() == 0)
			return null;

		Element record = (Element) list.get(0);

		MdInfo info = new MdInfo();

		info.id          = id;
		info.uuid        = record.getChildText("uuid");
		info.schemaId    = record.getChildText("schemaid");
		info.isHarvested = "y".equals(record.getChildText("isharvested"));
		info.createDate  = record.getChildText("createdate");
		info.changeDate  = record.getChildText("changedate");
		info.source      = record.getChildText("source");
		info.title       = record.getChildText("title");
		info.root        = record.getChildText("root");
		info.owner       = record.getChildText("owner");
		info.groupOwner  = record.getChildText("groupowner");
        info.displayOrder  = record.getChildText("displayOrder");

		String temp = record.getChildText("istemplate");

		if ("y".equals(temp))
			info.template = MdInfo.Template.TEMPLATE;

		else if ("s".equals(temp))
			info.template = MdInfo.Template.SUBTEMPLATE;

		else
			info.template = MdInfo.Template.METADATA;

		return info;
	}

    /**
     *
     * @param id
     * @return
     */
	public String getVersion(String id) {
		return editLib.getVersion(id);
	}

    /**
     *
     * @param id
     * @return
     */
	public String getNewVersion(String id){
		return editLib.getNewVersion(id);
	}

    /**
     * TODO javadoc.
     *
     * @param dbms
     * @param id
     * @param isTemplate
     * @param title
     * @throws Exception
     */
	public void setTemplate(Dbms dbms, int id, String isTemplate, String title) throws Exception {
		setTemplateExt(dbms, id, isTemplate, title);
        indexInThreadPoolIfPossible(dbms,Integer.toString(id), true);
	}

    /**
     * TODO javadoc.
     *
     * @param dbms
     * @param id
     * @param isTemplate
     * @param title
     * @throws Exception
     */
	public void setTemplateExt(Dbms dbms, int id, String isTemplate, String title) throws Exception {
		if (title == null) dbms.execute("UPDATE Metadata SET isTemplate=? WHERE id=?", isTemplate, id);
		else               dbms.execute("UPDATE Metadata SET isTemplate=?, title=? WHERE id=?", isTemplate, title, id);
	}

    /**
     * TODO javadoc.
     *
     * @param dbms
     * @param id
     * @param harvestUuid
     * @throws Exception
     */
	public void setHarvested(Dbms dbms, int id, String harvestUuid) throws Exception {
		setHarvestedExt(dbms, id, harvestUuid);
        boolean indexGroup = false;
        ServiceContext context = ServiceContext.get();
        if(context == null) context = servContext;

        indexMetadata(dbms, Integer.toString(id), false, context);
	}

    /**
     * TODO javadoc.
     *
     * @param dbms
     * @param id
     * @param harvestUuid
     * @throws Exception
     */
	public void setHarvestedExt(Dbms dbms, int id, String harvestUuid) throws Exception {
		String value = (harvestUuid != null) ? "y" : "n";
		if (harvestUuid == null) {
			dbms.execute("UPDATE Metadata SET isHarvested=? WHERE id=?", value,id );
		}
        else {
			dbms.execute("UPDATE Metadata SET isHarvested=?, harvestUuid=? WHERE id=?", value, harvestUuid, id);
		}
	}

    /**
     * TODO javadoc.
     *
     * @param dbms
     * @param id
     * @param harvestUuid
     * @param harvestUri
     * @throws Exception
     */
	public void setHarvestedExt(Dbms dbms, int id, String harvestUuid, String harvestUri) throws Exception {
		String value = (harvestUuid != null) ? "y" : "n";
		String query = "UPDATE Metadata SET isHarvested=?, harvestUuid=?, harvestUri=? WHERE id=?";
		dbms.execute(query, value, harvestUuid, harvestUri, id);
	}

    /**
     * TODO javadoc.
     *
     * @return
     */
	public String getSiteURL() {
        String protocol = settingMan.getValue(Geonet.Settings.SERVER_PROTOCOL);
		String host    = settingMan.getValue(Geonet.Settings.SERVER_HOST);
		String port    = settingMan.getValue(Geonet.Settings.SERVER_PORT);
		String locServ = baseURL +"/"+ Jeeves.Prefix.SERVICE +"/"+Geonet.DEFAULT_LANGUAGE;

		return protocol + "://" + host + (port.equals("80") ? "" : ":" + port) + locServ;
	}

    /**
     * Checks autodetect elements in installed schemas to determine whether the metadata record belongs to that schema.
     * Use this method when you want the default schema from the geonetwork config to be returned when no other match
     * can be found.
		 *
     * @param md Record to checked against schemas
     * @throws SchemaMatchConflictException
     * @throws NoSchemaMatchesException
     * @return
     */
	public String autodetectSchema(Element md) throws SchemaMatchConflictException, NoSchemaMatchesException {
		return autodetectSchema(md, schemaMan.getDefaultSchema());
	}

    /**
     * Checks autodetect elements in installed schemas to determine whether the metadata record belongs to that schema.
     * Use this method when you want to set the default schema to be returned when no other match can be found.
		 *
     * @param md Record to checked against schemas
     * @param defaultSchema Schema to be assigned when no other schema matches
     * @throws SchemaMatchConflictException
     * @throws NoSchemaMatchesException
     * @return
     */
	public String autodetectSchema(Element md, String defaultSchema) throws SchemaMatchConflictException, NoSchemaMatchesException {

        if(Log.isDebugEnabled(Geonet.DATA_MANAGER))
            Log.debug(Geonet.DATA_MANAGER, "Autodetect schema for metadata with :\n * root element:'" + md.getQualifiedName()
				 + "'\n * with namespace:'" + md.getNamespace()
				 + "\n * with additional namespaces:" + md.getAdditionalNamespaces().toString());
		String schema =  schemaMan.autodetectSchema(md, defaultSchema);
        if(Log.isDebugEnabled(Geonet.DATA_MANAGER))
            Log.debug(Geonet.DATA_MANAGER, "Schema detected was "+schema);
		return schema;
	}

    /**
     *
     * @param dbms
     * @param id
     * @param displayOrder
     * @throws Exception
     */
  public void updateDisplayOrder(Dbms dbms, String id, String displayOrder) throws Exception {
    String query = "UPDATE Metadata SET displayOrder = ? WHERE id = ?";
    dbms.execute(query, new Integer(displayOrder), new  Integer(id));
  }

    /**
     *
     * @param srvContext
     * @param id
     * @throws Exception hmm
     */
	public void increasePopularity(ServiceContext srvContext, String id) throws Exception {
		GeonetContext gc = (GeonetContext) srvContext.getHandlerContext(Geonet.CONTEXT_NAME);
		gc.getThreadPool().runTask(new IncreasePopularityTask(srvContext, id));
	}

    /**
     * Rates a metadata.
     *
     * @param dbms
     * @param id
     * @param ipAddress ipAddress IP address of the submitting client
     * @param rating range should be 1..5
     * @return
     * @throws Exception hmm
     */
	public int rateMetadata(Dbms dbms, int id, String ipAddress, int rating) throws Exception {
		//
		// update rating on the database
		//
		String query = "UPDATE MetadataRating SET rating=? WHERE metadataId=? AND ipAddress=?";
		int res = dbms.execute(query, rating, id, ipAddress);

		if (res == 0) {
			query = "INSERT INTO MetadataRating(metadataId, ipAddress, rating) VALUES(?,?,?)";
			dbms.execute(query, id, ipAddress, rating);
		}

        //
		// calculate new rating
        //
		query = "SELECT sum(rating) as total FROM MetadataRating WHERE metadataId=?";
		List list = dbms.select(query, id).getChildren();
		String sum = ((Element) list.get(0)).getChildText("total");
		query = "SELECT count(*) as numr FROM MetadataRating WHERE metadataId=?";
		list  = dbms.select(query, id).getChildren();
		String count = ((Element) list.get(0)).getChildText("numr");
		rating = (int)(Float.parseFloat(sum) / Float.parseFloat(count) + 0.5);
        if(Log.isDebugEnabled(Geonet.DATA_MANAGER))
            Log.debug(Geonet.DATA_MANAGER, "Setting rating for id:"+ id +" --> rating is:"+rating);
        //
		// update metadata and reindex it
        //
		query = "UPDATE Metadata SET rating=? WHERE id=?";
		dbms.execute(query, rating, id);

        indexInThreadPoolIfPossible(dbms,Integer.toString(id), false);

		return rating;
	}

	//--------------------------------------------------------------------------
	//---
	//--- Metadata Insert API
	//---
	//--------------------------------------------------------------------------

    /**
     * Creates a new metadata duplicating an existing template.
     *
     * @param context
     * @param dbms
     * @param templateId
     * @param groupOwner
     * @param sf
     * @param source
     * @param owner
     * @param parentUuid
     * @param isTemplate TODO
     * @param fullRightsForGroup TODO
     * @return
     * @throws Exception
     */
	public String createMetadata(ServiceContext context, Dbms dbms, String templateId, String groupOwner,
										  SerialFactory sf, String source, int owner,
										  String parentUuid, String isTemplate, boolean fullRightsForGroup) throws Exception {
		int iTemplateId = new Integer(templateId);
		String query = "SELECT schemaId, data FROM Metadata WHERE id=?";
		List listTempl = dbms.select(query, iTemplateId).getChildren();

		if (listTempl.size() == 0) {
			throw new IllegalArgumentException("Template id not found : " + templateId);
        }
		Element el = (Element) listTempl.get(0);

		String schema = el.getChildText("schemaid");
		String data   = el.getChildText("data");
		String uuid   = UUID.randomUUID().toString();

		if (!schema.equals("iso19139.che")) {
				throw new IllegalArgumentException("Tried to a make a metadata with schema: '"+schema+"' only metadata of schema 'iso19139.che' are allowed in this catalog");
		}
		//--- generate a new metadata id
		int serial = sf.getSerial(dbms, "Metadata");
		
		// Update fixed info for metadata record only, not for subtemplates
		Element xml = Xml.loadString(data, false);
		if (!isTemplate.equals("s")) {
		    xml = updateFixedInfo(schema, Integer.toString(serial), uuid, xml, parentUuid, DataManager.UpdateDatestamp.yes, dbms, context);
		}
		
		//--- store metadata
		String id = xmlSerializer.insert(dbms, schema, xml, serial, source, uuid, null, null, isTemplate, null, owner, groupOwner, "", context);
		copyDefaultPrivForGroup(context, dbms, id, groupOwner, fullRightsForGroup);

		//--- store metadata categories copying them from the template
		List categList = dbms.select("SELECT categoryId FROM MetadataCateg WHERE metadataId = ?",iTemplateId).getChildren();

        for (Object aCategList : categList) {
            Element elRec = (Element) aCategList;
            String catId = elRec.getChildText("categoryid");
            setCategory(context, dbms, id, catId);
        }


        // --- store metadata hiding by copying them from the template
        List hidingList = dbms.select(
                "SELECT xPathExpr, level FROM HiddenMetadataElements WHERE metadataId = " + templateId).getChildren();

        String insertSQL = "INSERT INTO HiddenMetadataElements (metadataId, xPathExpr, level) VALUES (?, ?, ?)";
        Integer idInteger = new Integer(id);
        for (int i = 0; i < hidingList.size(); i++)
        {
            Element hidingRec = (Element) hidingList.get(i);

            String xPathExpr = hidingRec.getChildText("xpathexpr");
            String level = hidingRec.getChildText("level");
            if (xPathExpr == null || level == null) {
                Log.warning("DataManager", "xPathExpr (" + xPathExpr + ") or level (" + level + ") is null for template=" +templateId);
                continue;
            }
            dbms.execute(insertSQL, idInteger, xPathExpr, level);
        }


		//--- index metadata
        indexInThreadPoolIfPossible(dbms,id, true);
		return id;
	}

    /**
     * Inserts a metadata into the database, optionally indexing it, and optionally applying automatic changes to it (update-fixed-info).
     *
     * @param context the context describing the user and service
     * @param dbms the database
     * @param schema XSD this metadata conforms to
     * @param metadata the metadata to store
     * @param id database id for new metadata record
     * @param uuid unique id for this metadata
     * @param owner user who owns this metadata
     * @param group group this metadata belongs to
     * @param source id of the origin of this metadata (harvesting source, etc.)
     * @param isTemplate whether this metadata is a template
     * @param docType ?!
     * @param title title of this metadata
     * @param category category of this metadata
     * @param createDate date of creation
     * @param changeDate date of modification
     * @param ufo whether to apply automatic changes
     * @param index whether to index this metadata
     * @return id, as a string
     * @throws Exception hmm
     */
    public String insertMetadata(ServiceContext context, Dbms dbms, String schema, Element metadata, int id, String uuid, int owner, String group, String source,
                                 String isTemplate, String docType, String title, String category, String createDate, String changeDate, boolean ufo, boolean index) throws Exception {

    	if("n".equals(isTemplate) && !"iso19139.che".equals(schema)) {
    		throw new IllegalArgumentException(schema+" is not permitted in the database as a non-harvested metadata.  Apply a import stylesheet to convert file to iso19139.che");
    	}
    	
        // TODO resolve confusion about datatypes
        String id$ = Integer.toString(id);

        //--- force namespace prefix for iso19139 metadata
        setNamespacePrefixUsingSchemas(schema, metadata);

        if (ufo && "n".equals(isTemplate)) {
            String parentUuid = null;
            metadata = updateFixedInfo(schema, id$, uuid, metadata, parentUuid, DataManager.UpdateDatestamp.no, dbms, context);
        }

         if (source == null) {
            source = getSiteID();
         }

        if(StringUtils.isBlank(isTemplate)) {
            isTemplate = "n";
        }

        //--- store metadata
        xmlSerializer.insert(dbms, schema, metadata, id, source, uuid, createDate, changeDate, isTemplate, title, owner, group, docType, context);

        copyDefaultPrivForGroup(context, dbms, id$, group, false);

        if (category != null) {
            setCategory(context, dbms, id$, category);
        }

        if(index) {
            indexInThreadPoolIfPossible(dbms,id$, true);
        }

        // Notifies the metadata change to metatada notifier service
        notifyMetadataChange(dbms, metadata, id$);

        return id$;
    }

	//--------------------------------------------------------------------------
	//---
	//--- Metadata Get API
	//---
	//--------------------------------------------------------------------------

    /**
     * Retrieves a metadata (in xml) given its id with no geonet:info.
     * @param srvContext
     * @param id
     * @return
     * @throws Exception
     */
	public Element getMetadataNoInfo(ServiceContext srvContext, String id) throws Exception {
	    Element md = getMetadata(srvContext, id, false, false, false);
		md.removeChild(Edit.RootChild.INFO, Edit.NAMESPACE);
		return md;
	}

    /**
     * Retrieves a metadata (in xml) given its id. Use this method when you must retrieve a metadata in the same
     * transaction.
     * @param dbms
     * @param id
     * @return
     * @throws Exception
     */
	public Element getMetadata(Dbms dbms, String id) throws Exception {
	    ServiceContext context = ServiceContext.get();
	    if(context == null) {
	        throw new IllegalStateException("This method is not supported by Geocat because hiding is not currently supported.  We can add it later");
	    } else {
	        Element md = getMetadata(context, id, false, false, false);
	        if (md == null) return null;
	        md.detach();
	        return md;
	    }
		/*boolean doXLinks = xmlSerializer.resolveXLinks();
		Element md = xmlSerializer.selectNoXLinkResolver(dbms, "Metadata", id, getServiceContext());
		if (md == null) return null;
		md.detach();
		return md;*/
	}

    private ServiceContext getServiceContext() {

        return ServiceContext.get() == null? servContext : ServiceContext.get();
    }

    /**
     * Retrieves a metadata (in xml) given its id; adds editing information if requested and validation errors if
     * requested.
     * 
     * @param srvContext
     * @param id
     * @param forEditing        Add extra element to build metadocument {@link EditLib#expandElements(String, Element)}
     * @param withEditorValidationErrors
     * @param keepXlinkAttributes When XLinks are resolved in non edit mode, do not remove XLink attributes.
     * @return
     * @throws Exception
     */
	public Element getMetadata(ServiceContext srvContext, String id, boolean forEditing, boolean withEditorValidationErrors, boolean keepXlinkAttributes) throws Exception {
		return getGeocatMetadata(srvContext,id,forEditing,withEditorValidationErrors,keepXlinkAttributes, true, false);
	}
	public Element getGeocatMetadata(ServiceContext srvContext, String id, boolean forEditing, boolean withEditorValidationErrors, boolean keepXlinkAttributes, boolean elementsHide, boolean allowDbmsClosing) throws Exception {
		Dbms dbms = (Dbms) srvContext.getResourceManager().open(Geonet.Res.MAIN_DB);
		Element md = xmlSerializer.selectNoXLinkResolver(dbms, "Metadata", id, false, srvContext);
		if (md == null) return null;

		String version = null;

		if (forEditing) { // copy in xlink'd fragments but leave xlink atts to editor
			Processor.processXLink(md, srvContext);
			String schema = getMetadataSchema(dbms, id);
            md = Xml.transform(md, stylePath+"characterstring-to-localisedcharacterstring.xsl");

            editLib.expandElements(schema, md);
            version = editLib.getVersionForEditing(schema, id, md);
		}
        else {
            if (keepXlinkAttributes) {
                Processor.processXLink(md, srvContext);
            } else {
                Processor.detachXLink(md,srvContext);
            }
            if( getMetadataSchema(dbms, id).equals("iso19139.che")) {
                md = Xml.transform(md, stylePath+"add-charstring.xsl");
            }
		}

        if (elementsHide) {
            hideElements(srvContext, md, id, forEditing, allowDbmsClosing);
        }

		md.addNamespaceDeclaration(Edit.NAMESPACE);
		Element info = buildInfoElem(srvContext, id, version);
		md.addContent(info);

        if (forEditing) {
            addHidingInfo(srvContext, md, id);
        }

		md.detach();
		return md;
	}

    /**
     * Retrieves a metadata element given it's ref.
     *
     * @param md
     * @param ref
     * @return
     */
	public Element getElementByRef(Element md, String ref) {
		return editLib.findElement(md, ref);
	}

    /**
     * Returns true if the metadata exists in the database.
     * @param dbms
     * @param id
     * @return
     * @throws Exception
     */
	public boolean existsMetadata(Dbms dbms, int id) throws Exception {
		//FIXME : should use lucene
		List list = dbms.select("SELECT id FROM Metadata WHERE id=?", new Integer(id)).getChildren();
		return list.size() != 0;
	}

    /**
     * Returns true if the metadata uuid exists in the database.
     * @param dbms
     * @param uuid
     * @return
     * @throws Exception
     */
	public boolean existsMetadataUuid(Dbms dbms, String uuid) throws Exception {
		//FIXME : should use lucene

		List list = dbms.select("SELECT uuid FROM Metadata WHERE uuid=?",uuid).getChildren();
		return list.size() != 0;
	}

    /**
     * Returns all the keywords in the system.
     *
     * @return
     * @throws Exception
     */
	public Element getKeywords() throws Exception {
		Vector keywords = searchMan.getTerms("keyword");
		Element el = new Element("keywords");

        for (Object keyword : keywords) {
            el.addContent(new Element("keyword").setText((String) keyword));
        }
		return el;
	}

	//--------------------------------------------------------------------------
	//---
	//--- Metadata Update API
	//---
	//--------------------------------------------------------------------------

    /**
     *  For update of owner info.
     *
     * @param dbms
     * @param id
     * @param owner
     * @param groupOwner
     * @throws Exception
     */
	public synchronized void updateMetadataOwner(Dbms dbms, int id, String owner, String groupOwner) throws Exception {
		dbms.execute("UPDATE Metadata SET owner=?, groupOwner=? WHERE id=?", new Integer(owner), new Integer(groupOwner), id);
	}

    /**
     * Updates a metadata record. Deletes validation report currently in session (if any). If user asks for validation
     * the validation report will be (re-)created then.
     *
     * @param context
     * @param dbms
     * @param id
     * @param md
     * @param validate
     * @param lang
     * @param changeDate
     * @param updateDateStamp
     *
     * @return
     * @throws Exception
     */
	public synchronized boolean updateMetadata(ServiceContext context, Dbms dbms, String id, Element md,
                                               boolean validate, boolean ufo, boolean index, String lang,
                                               String changeDate, boolean updateDateStamp, boolean processSharedObject) throws Exception {
		// when invoked from harvesters, session is null?
				UserSession session = context.getUserSession();
        if(session != null) {
            session.removeProperty(Geonet.Session.VALIDATION_REPORT + id);
        }
		String schema = getMetadataSchema(dbms, id);
        if(ufo) {
            String parentUuid = null;
		    md = updateFixedInfo(schema, id, null, md, parentUuid, (updateDateStamp ? DataManager.UpdateDatestamp.yes : DataManager.UpdateDatestamp.no), dbms, context);
        }

        if(processSharedObject) {
            md = processSharedObjects(dbms, id, md, lang);
        }

        
        String uuid = null;
        if (schemaMan.getSchema(schema).isReadwriteUUID()) {
            uuid = extractUUID(schema, md);
        }
        
		//--- write metadata to dbms
        xmlSerializer.update(dbms, id, md, changeDate, updateDateStamp, uuid, context);

        String isTemplate = getMetadataTemplate(dbms, id);
        // Notifies the metadata change to metatada notifier service
        if (isTemplate.equals("n")) {
            // Notifies the metadata change to metatada notifier service
            notifyMetadataChange(dbms, md, id);
        }

		try {
    		//--- do the validation last - it throws exceptions
//    		if (session != null && validate) {
//    		    Element xlinkResolved = Processor.processXLink((Element) md.clone(), context);
//    			doValidate(context, dbms, schema,id,xlinkResolved,lang, false);
//    		}
		}
        finally {
            if(index) {
                //--- update search criteria
                boolean indexGroup = false;
                boolean processSharedObjects = false;
                indexMetadata(dbms, id, processSharedObjects, context, false, false, false);
            }
		}
		return true;
	}

	public Element processSharedObjects(Dbms dbms, String id, Element md, String lang)
			throws Exception, SQLException {
		ProcessParams processParameters = new ProcessParams(dbms, ReusableObjectLogger.THREAD_SAFE_LOGGER, id, md, md, thesaurusMan, extentMan, baseURL, settingMan, false, lang,servContext);
        List<Element> modified = reusableObjMan.process(processParameters);

        if(!modified.isEmpty()) {
            md = modified.get(0);
            Processor.processXLink(md, servContext);
            dbms.commit(); // commit shared objects like contacts that were created during processing
        }
		return md;
	}

    /**
     * Validates an xml document, using autodetectschema to determine how.
     *
     * @param xml
     * @return true if metadata is valid
     */
    public boolean validate(Element xml) {
        try {
        		String schema = autodetectSchema(xml);
            validate(schema, xml);
            return true;
        }
        // XSD validation error(s)
        catch (Exception x) {
            // do not print stacktrace as this is 'normal' program flow
            if(Log.isDebugEnabled(Geonet.DATA_MANAGER))
                Log.debug(Geonet.DATA_MANAGER, "invalid metadata: " + x.getMessage());
            return false;
        }
    }

	/**
	 * Used by harvesters that need to validate metadata.
	 * 
	 * @param dbms connection to database
	 * @param schema name of the schema to validate against
	 * @param id metadata id - used to record validation status
	 * @param doc metadata document as JDOM Document not JDOM Element
	 * @param lang Language from context
	 * @return
	 */
	public boolean doValidate(Dbms dbms, String schema, String id, Document doc, String lang) {
		HashMap <String, Integer[]> valTypeAndStatus = new HashMap<String, Integer[]>();
		boolean valid = true;

		if (doc.getDocType() != null) {
            if(Log.isDebugEnabled(Geonet.DATA_MANAGER))
                Log.debug(Geonet.DATA_MANAGER, "Validating against dtd " + doc.getDocType());
			
			// if document has a doctype then validate using that (assuming that the
			// dtd is either mapped locally or will be cached after first validate)
			try {
				Xml.validate(doc);
				Integer[] results = {1, 0, 0};
				valTypeAndStatus.put("dtd", results);
                if(Log.isDebugEnabled(Geonet.DATA_MANAGER))
                    Log.debug(Geonet.DATA_MANAGER, "Valid.");
			} catch (Exception e) {
				e.printStackTrace();
				Integer[] results = {0, 0, 0};
				valTypeAndStatus.put("dtd", results);
                if(Log.isDebugEnabled(Geonet.DATA_MANAGER))
                    Log.debug(Geonet.DATA_MANAGER, "Invalid.");
				valid = false;
			}
		} else {
            if (Log.isDebugEnabled(Geonet.DATA_MANAGER)) {
                Log.debug(Geonet.DATA_MANAGER, "Validating against XSD " + schema);
            }
            // do XSD validation
            Element md = doc.getRootElement();
            Element xsdErrors = getXSDXmlReport(schema, md);
            if (xsdErrors != null && xsdErrors.getContent().size() > 0) {
                Integer[] results = {0, 0, 0};
                valTypeAndStatus.put("xsd", results);
                if (Log.isDebugEnabled(Geonet.DATA_MANAGER))
                    Log.debug(Geonet.DATA_MANAGER, "Invalid.");
                valid = false;
            } else {
                Integer[] results = {1, 0, 0};
                valTypeAndStatus.put("xsd", results);
                if (Log.isDebugEnabled(Geonet.DATA_MANAGER))
                    Log.debug(Geonet.DATA_MANAGER, "Valid.");
            }
            try {
                editLib.enumerateTree(md);
                //Apply custom schematron rules
                Element errors = applyCustomSchematronRules(dbms, schema, Integer.parseInt(id), doc.getRootElement(), lang, valTypeAndStatus);
                valid = valid && errors == null;
                editLib.removeEditingInfo(md);
            } catch (Exception e) {
                e.printStackTrace();
                Log.error(Geonet.DATA_MANAGER, "Could not run schematron validation on metadata " + id + ": " + e.getMessage());
                valid = false;
            }
        }

		// now save the validation status
		try {
			saveValidationStatus(dbms, id, valTypeAndStatus, new ISODate().toString());
		} catch (Exception e) {
			e.printStackTrace();
			Log.error(Geonet.DATA_MANAGER, "Could not save validation status on metadata "+id+": "+e.getMessage());
		}

		return valid;
	}

	/**
	 * Used by the validate embedded service. The validation report is stored in the session.
	 * 
	 * @param context
	 * @param schema
	 * @param id
	 * @param metadata
	 * @param lang
	 * @param forEditing TODO
	 * @return
	 * @throws Exception
	 */
	public Pair <Element, String> doValidate(ServiceContext context, Dbms dbms, String schema, String id, Element metadata, String lang, boolean forEditing) throws Exception {
        String version = null;
        if (Log.isDebugEnabled(Geonet.DATA_MANAGER))
            Log.debug(Geonet.DATA_MANAGER, "Creating validation report for record #" + id + " [schema: " + schema + "].");

        Element md;
        if (!forEditing) {
            md = (Element) metadata.clone();
            // always hideElements for validation
            hideElements(context, dbms, md, id, false, true);
        } else {
            md = metadata;
        }
        UserSession session = null;
        if (context != null && context.getUserSession() != null) {
            session = context.getUserSession();
            Element sessionReport = (Element) session.getProperty(Geonet.Session.VALIDATION_REPORT + id);
            if (sessionReport != null && !forEditing) {
                if (Log.isDebugEnabled(Geonet.DATA_MANAGER))
                    Log.debug(Geonet.DATA_MANAGER, "  Validation report available in session.");
                sessionReport.detach();
                return Pair.read(sessionReport, version);
            }
        }

        Map<String, Integer[]> valTypeAndStatus = new HashMap<String, Integer[]>();
        Element errorReport = new Element("report", Edit.NAMESPACE);
        errorReport.setAttribute("id", id, Edit.NAMESPACE);

        //-- get an XSD validation report and add results to the metadata
        //-- as geonet:xsderror attributes on the affected elements
        Element xsdErrors = getXSDXmlReport(schema, md);
        if (xsdErrors != null && xsdErrors.getContent().size() > 0) {
            errorReport.addContent(xsdErrors);
            Integer[] results = {0, xsdErrors.getChildren().size(), xsdErrors.getChildren("error", Namespaces.GEONET).size()};
            valTypeAndStatus.put("xsd", results);
            if (Log.isDebugEnabled(Geonet.DATA_MANAGER)) {
                Log.debug(Geonet.DATA_MANAGER, "  - XSD error: " + Xml.getString(xsdErrors));
            }
        } else {
            Integer[] results = {1, 0, 0};
            valTypeAndStatus.put("xsd", results);

            if (Log.isTraceEnabled(Geonet.DATA_MANAGER)) {
                Log.trace(Geonet.DATA_MANAGER, "Valid.");
            }
        }

        // ...then schematrons
        Element schematronError = null;

        // edit mode
        Element error = null;
        if (forEditing) {
            if (Log.isDebugEnabled(Geonet.DATA_MANAGER))
                Log.debug(Geonet.DATA_MANAGER, "  - Schematron in editing mode.");
            //-- now expand the elements and add the geonet: elements
            editLib.expandElements(schema, md);
            version = editLib.getVersionForEditing(schema, id, md);

            //Apply custom schematron rules
            error = applyCustomSchematronRules(dbms, schema, Integer.parseInt(id), md, lang, valTypeAndStatus);
        } else {
            try {
                // enumerate the metadata xml so that we can report any problems found
                // by the schematron_xml script to the geonetwork editor
                editLib.enumerateTree(md);

                //Apply custom schematron rules
                error = applyCustomSchematronRules(dbms, schema, Integer.parseInt(id), md, lang, valTypeAndStatus);

                // remove editing info added by enumerateTree
                editLib.removeEditingInfo(md);

            } catch (Exception e) {
                e.printStackTrace();
                Log.error(Geonet.DATA_MANAGER, "Could not run schematron validation on metadata " + id + ": " + e.getMessage());
            }
        }

        if (error != null) {
            errorReport.addContent(error);
        }

        // Save report in session (invalidate by next update) and db
        try {
            saveValidationStatus(dbms, id, valTypeAndStatus, new ISODate().toString());
        } catch (Exception e) {
            e.printStackTrace();
            Log.error(Geonet.DATA_MANAGER, "Could not save validation status on metadata " + id + ": " + e.getMessage());
        }

        return Pair.read(errorReport, version);
    }
	

    /**
     * 
     * Creates XML schematron report for each set of rules defined in schema 
     * directory. This method assumes that you've run enumerateTree on the 
     * metadata
     * 
     * Returns null if no error on validation.
     * 
     *
     * @param schema
     * @param metadataId
     *@param md
     * @param lang
     * @param valTypeAndStatus    @return errors
     */
	public Element applyCustomSchematronRules(Dbms dbms, String schema, int metadataId, Element md,
                                              String lang, Map<String, Integer[]> valTypeAndStatus) {
		MetadataSchema metadataSchema = getSchema(schema);

		Element schemaTronXmlOut = new Element("schematronerrors",
				Edit.NAMESPACE);
		try {
			String schemaname = metadataSchema.getName();
			final List<Element> schemas = SchemaDao.selectSchema(dbms, schemaname);

            final String schemaDir = this.schemaMan.getSchemaDir(schemaname);

            //Loop through all xsl files
			for (Element schematron : schemas) {

                int id = Integer.parseInt(schematron.getChildText(SchemaDao.COL_SCHEMATRON_ID));
                //it contains absolute path to the xsl file
                final String schematronFileName = schematron.getChildText(SchemaDao.COL_SCHEMATRON_FILE);
                String file = schemaDir + MetadataSchema.SCHEMATRON_DIR + File.separator + schematronFileName;
                String rule = SchemaDao.toRuleName(schematronFileName);
                String dbident = ""+id;

                List<SchematronCriteriaGroup> criteriaGroups = SchemaDao.selectCriteriaBySchema(dbms, id);

                //Loop through all criteria to see if apply schematron
                //if any criteria does not apply, do not apply at all (AND)
                SchematronRequirement requirement = SchematronRequirement.DISABLED;
                for (SchematronCriteriaGroup criteriaGroup : criteriaGroups) {
                    if (criteriaGroup.getRequirement() == SchematronRequirement.DISABLED) {
                        continue;
                    }
                    List<SchematronCriteria> criterias = criteriaGroup.getCriteriaList();
                    boolean apply = false;
                    for(SchematronCriteria criteria : criterias) {
                        boolean tmpApply = criteria.accepts(dbms, metadataId, md, metadataSchema.getSchemaNS());

                        if(!tmpApply) {
                            apply = false;
                            break;
                        } else {
                            apply = true;
                        }

                    }

                    if (apply) {
                        if(Log.isDebugEnabled(Geonet.DATA_MANAGER)) {
                            Log.debug(Geonet.DATA_MANAGER, " - Schematron group is accepted:" + criteriaGroup.getName() + " for schematron: "+rule);
                        }
                        requirement = requirement.highestRequirement(criteriaGroup.getRequirement());
                    }
                }

                if(requirement != SchematronRequirement.DISABLED) {
                    if(Log.isDebugEnabled(Geonet.DATA_MANAGER)) {
                        Log.debug(Geonet.DATA_MANAGER, " - rule:" + rule + ". XSL validation file is: "+file);
                    }


                    Integer ifNotValid = (requirement == SchematronRequirement.REQUIRED ? 0 : 2);

                    String ruleId = rule;

                    Element report = new Element("report", Edit.NAMESPACE);
                    report.setAttribute("rule", ruleId,
                            Edit.NAMESPACE);
                    report.setAttribute("dbident", dbident,
                            Edit.NAMESPACE);
                    report.setAttribute("required", Boolean.toString(requirement == SchematronRequirement.REQUIRED),
                            Edit.NAMESPACE);

                    try {
                        Map<String,String> params = new HashMap<String,String>();
                        params.put("lang", lang);
                        params.put("rule", ruleId);
                        params.put("thesaurusDir", this.thesaurusDir);

                        Element xmlReport = Xml.transform(md, file, params);
                        if (xmlReport != null) {
                            report.addContent(xmlReport);
                        }
                        // add results to persistent validation information
                        int firedRules = 0;
                        @SuppressWarnings("unchecked")
                        Iterator<Element> i = xmlReport.getDescendants(new ElementFilter ("fired-rule", Namespace.getNamespace("http://purl.oclc.org/dsdl/svrl")));
                        while (i.hasNext()) {
                            i.next();
                            firedRules ++;
                        }
                        int invalidRules = 0;
                        i = xmlReport.getDescendants(new ElementFilter ("failed-assert", Namespace.getNamespace("http://purl.oclc.org/dsdl/svrl")));
                        while (i.hasNext()) {
                            i.next();
                            invalidRules ++;
                        }
                        Integer[] results = {invalidRules!=0?ifNotValid:1, firedRules, invalidRules};
                        if (valTypeAndStatus != null) {
                            valTypeAndStatus.put(ruleId, results);
                        }
                    } catch (Exception e) {
                        Log.error(Geonet.DATA_MANAGER,"WARNING: schematron xslt "+rule+" failed", e);

                        // If an error occurs that prevents to verify schematron rules, add to show in report
                        Element errorReport = new Element("schematronVerificationError", Edit.NAMESPACE);
                        errorReport.addContent("Schematron error ocurred, rules could not be verified: " + e.getMessage());
                        report.addContent(errorReport);
                    }

                    // -- append report to main XML report.
                    schemaTronXmlOut.addContent(report);

                }

			}
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			Element errorReport = new Element("schematronVerificationError", Edit.NAMESPACE);
            errorReport.addContent("Schematron error ocurred, rules could not be verified: " + sqle.getMessage());
            schemaTronXmlOut.addContent(errorReport);
		} catch (Throwable e) {
			e.printStackTrace();
		}

		if(schemaTronXmlOut.getChildren().isEmpty()) {
			schemaTronXmlOut = null;
		}

		return schemaTronXmlOut;
	}

	private boolean checkKeyword(Element element, String value) {
		
		try {
			 return ((Boolean)Xml.selectNodes(element, "//gmd:keyword/gco:CharacterString/text() = '" + value + "' ")
					 	.get(0)) ||
					 ((Boolean) Xml.selectNodes(element, "//gmd:keyword/gmd:PT_FreeText/gmd:textGroup/"
				  + "gmd:LocalisedCharacterString/text() = '" + value + "'").get(0));
		} catch (JDOMException e) {
			e.printStackTrace();
		}
        
		//if xpath fails:
		List<Element> children = element.getChildren();
		boolean res = false;
		
		for(Element child : children) {
			if(child.getName().equalsIgnoreCase("keyword")) {
				List<Element> freeTexts = child.getChildren();
				for(Element freeText : freeTexts) {
					if("CharacterString".equals(freeText.getName())) {
						res = res || value.equals(freeText.getText());
					}
					else if("PT_FreeText".equals(freeText.getName())) {
						List<Element> textGroups = freeText.getChildren();
						for(Element textGroup : textGroups) {
							if("textGroup".equals(textGroup.getName())) {
								List<Element> localiseds = textGroup.getChildren();
								for(Element localised : localiseds) {
									res = res || value.equals(localised.getText());
								}
								if(res) {
									break;
								}
							}
							if(res) {
								break;
							}
						}
					}
				}
				
			} else {
				res = res || checkKeyword(child, value);
			}
			
			if(res) {
				break;
			}
		}
        
		return res;
	}

	/**
	 * Saves validation status information into the database for the current record.
	 * 
	 * @param id   the metadata record internal identifier
	 * @param valTypeAndStatus  the validation type could be xsd or schematron rules set identifier
	 * @param date the validation date time
	 */
	private void saveValidationStatus (Dbms dbms, String id, Map<String, Integer[]> valTypeAndStatus, String date) throws Exception {
	    clearValidationStatus(dbms, id);
	    Set<String> i = valTypeAndStatus.keySet();
	    for (String type : i) {
	        String query = "INSERT INTO Validation (metadataId, valType, status, tested, failed, valDate) VALUES (?,?,?,?,?,?)";
            Integer[] results = valTypeAndStatus.get(type);
            dbms.execute(query, new Integer(id), type, results[0], results[1], results[2], date);
        }
        dbms.commit();
	}

	/**
	 * Removes validation status information for a metadata record.
     *
	 * @param dbms
	 * @param id   the metadata record internal identifier
	 */
	private void clearValidationStatus (Dbms dbms, String id) throws Exception {
	    dbms.execute("DELETE FROM Validation WHERE metadataId=?", new Integer(id));
	    dbms.commit();
	}

	/**
	 * Return the validation status information for the metadata record.
     *
	 * @param dbms
	 * @param id   the metadata record internal identifier
	 * @return
	 */
	private List<Element> getValidationStatus (Dbms dbms, String id) throws Exception {
	    return dbms.select("SELECT valType, status, tested, failed FROM Validation WHERE metadataId=?", new Integer(id)).getChildren();
    }

	//--------------------------------------------------------------------------
	//---
	//--- Metadata Delete API
	//---
	//--------------------------------------------------------------------------

    /**
     * Removes a metadata.
     *
     * @param context
     * @param dbms
     * @param id
     * @throws Exception
     */
	public synchronized void deleteMetadata(ServiceContext context, Dbms dbms, String id) throws Exception {
        String uuid = getMetadataUuid(dbms, id);
        String isTemplate = getMetadataTemplate(dbms, id);

		//--- remove operations
		deleteMetadataOper(dbms, id, false);

		//--- remove categories
		deleteAllMetadataCateg(dbms, id);

		dbms.execute("DELETE FROM MetadataRating WHERE metadataId=?", new Integer(id));
		dbms.execute("DELETE FROM Validation WHERE metadataId=?", new Integer(id));

		dbms.execute("DELETE FROM MetadataStatus WHERE metadataId=?", new Integer(id));

		//--- remove metadata
		xmlSerializer.delete(dbms, "Metadata", id, context);

        // Notifies the metadata change to metatada notifier service
        if (isTemplate.equals("n")) {
            notifyMetadataDelete(dbms, id, uuid);
        }

		//--- update search criteria
		searchMan.delete("_id", id+"");
	}

    /**
     *
     * @param context
     * @param dbms
     * @param id
     * @throws Exception
     */
	public synchronized void deleteMetadataGroup(ServiceContext context, Dbms dbms, String id) throws Exception {
		//--- remove operations
		deleteMetadataOper(dbms, id, false);

		//--- remove categories
		deleteAllMetadataCateg(dbms, id);

		dbms.execute("DELETE FROM MetadataRating WHERE metadataId=?", new Integer(id));
        dbms.execute("DELETE FROM Validation WHERE metadataId=?", new Integer(id));
		dbms.execute("DELETE FROM MetadataStatus WHERE metadataId=?", new Integer(id));

		//--- remove metadata
		xmlSerializer.delete(dbms, "Metadata", id, context);

		//--- update search criteria
		searchMan.deleteGroup("_id", id + "");
	}

    /**
     * Removes all operations stored for a metadata.
     * @param dbms
     * @param id
     * @param skipAllIntranet
     * @throws Exception
     */
	public void deleteMetadataOper(Dbms dbms, String id, boolean skipAllIntranet) throws Exception {
		String query = "DELETE FROM OperationAllowed WHERE metadataId=?";

		if (skipAllIntranet)
			query += " AND groupId>1";

		dbms.execute(query, new Integer(id));
	}

    /**
     * Removes all categories stored for a metadata.
     *
     * @param dbms
     * @param id
     * @throws Exception
     */
	public void deleteAllMetadataCateg(Dbms dbms, String id) throws Exception {
		String query = "DELETE FROM MetadataCateg WHERE metadataId=?";

		dbms.execute(query, new Integer(id));
	}

	//--------------------------------------------------------------------------
	//---
	//--- Metadata thumbnail API
	//---
	//--------------------------------------------------------------------------

    /**
     *
     * @param dbms
     * @param id
     * @return
     * @throws Exception
     */
	public Element getThumbnails(Dbms dbms, String id) throws Exception {
		Element md = xmlSerializer.select(dbms, "Metadata", id, getServiceContext());

		if (md == null)
			return null;

		md.detach();

		String schema = getMetadataSchema(dbms, id);

		//--- do an XSL  transformation
		String styleSheet = getSchemaDir(schema) + Geonet.File.EXTRACT_THUMBNAILS;

		Element result = Xml.transform(md, styleSheet);
		result.addContent(new Element("id").setText(id));

		return result;
	}

    /**
     *
     * @param context
     * @param id
     * @param small
     * @param file
     * @throws Exception
     */
	public void setThumbnail(ServiceContext context, Dbms dbms, String id, boolean small, String file, boolean indexAfterChange) throws Exception {
		int    pos = file.lastIndexOf('.');
		String ext = (pos == -1) ? "???" : file.substring(pos +1);

		Element env = new Element("env");
		env.addContent(new Element("file").setText(file));
		env.addContent(new Element("ext").setText(ext));
		
		String host    = settingMan.getValue(Geonet.Settings.SERVER_HOST);
		String port    = settingMan.getValue(Geonet.Settings.SERVER_PORT);
		String baseUrl = context.getBaseUrl();
		
		env.addContent(new Element("host").setText(host));
		env.addContent(new Element("port").setText(port));
		env.addContent(new Element("baseUrl").setText(baseUrl));
		
		manageThumbnail(context, dbms, id, small, env, Geonet.File.SET_THUMBNAIL, indexAfterChange);
	}

    /**
     *
     * @param context
     * @param id
     * @param small
     * @throws Exception
     */
	public void unsetThumbnail(ServiceContext context, Dbms dbms, String id, boolean small, boolean indexAfterChange) throws Exception {
		Element env = new Element("env");

		manageThumbnail(context, dbms, id, small, env, Geonet.File.UNSET_THUMBNAIL, indexAfterChange);
	}

    /**
     *
     * @param context
     * @param id
     * @param small
     * @param env
     * @param styleSheet
     * @param indexAfterChange 
     * @throws Exception
     */
	private void manageThumbnail(ServiceContext context, Dbms dbms, String id, boolean small, Element env,
										  String styleSheet, boolean indexAfterChange) throws Exception {
		
        boolean forEditing = false, withValidationErrors = false, keepXlinkAttributes = true;
        Element md = getGeocatMetadata(context, id, forEditing, withValidationErrors, keepXlinkAttributes,false, false);

		if (md == null)
			return;

		md.detach();

		String schema = getMetadataSchema(dbms, id);

		//--- setup environment
		String type = small ? "thumbnail" : "large_thumbnail";
		env.addContent(new Element("type").setText(type));
		transformMd(dbms,context,id,md,env,schema,styleSheet, indexAfterChange);
	}

    /**
     *
     * @param dbms
		 * @param context
     * @param id
     * @param md
     * @param env
     * @param schema
     * @param styleSheet
     * @param indexAfterChange 
     * @throws Exception
     */
	private void transformMd(Dbms dbms, ServiceContext context, String id, Element md, Element env, String schema, String styleSheet, boolean indexAfterChange) throws Exception {
		
		if(env.getChild("host")==null){
			String host    = settingMan.getValue(Geonet.Settings.SERVER_HOST);
			String port    = settingMan.getValue(Geonet.Settings.SERVER_PORT);
			
			env.addContent(new Element("host").setText(host));
			env.addContent(new Element("port").setText(port));
		}
		
		//--- setup root element
		Element root = new Element("root");
		root.addContent(md);
		root.addContent(env);

		//--- do an XSL  transformation
		styleSheet = getSchemaDir(schema) + styleSheet;

		md = Xml.transform(root, styleSheet);
        String changeDate = null;
        String uuid = null;
        if (schemaMan.getSchema(schema).isReadwriteUUID()) {
            uuid = extractUUID(schema, md);
        }
        
		xmlSerializer.update(dbms, id, md, changeDate, true, uuid, context);

        if (indexAfterChange) {
            // Notifies the metadata change to metatada notifier service
            notifyMetadataChange(dbms, md, id);

		    //--- update search criteria
            indexInThreadPoolIfPossible(dbms,id, false);
        }
	}

    /**
     *
     * @param dbms
     * @param context
     * @param id
     * @param licenseurl
     * @param imageurl
     * @param jurisdiction
     * @param licensename
     * @param type
     * @throws Exception
     */
	public void setDataCommons(Dbms dbms, ServiceContext context, String id, String licenseurl, String imageurl, String jurisdiction, String licensename, String type) throws Exception {
		Element env = new Element("env");
		env.addContent(new Element("imageurl").setText(imageurl));
		env.addContent(new Element("licenseurl").setText(licenseurl));
		env.addContent(new Element("jurisdiction").setText(jurisdiction));
		env.addContent(new Element("licensename").setText(licensename));
		env.addContent(new Element("type").setText(type));

		manageCommons(dbms,context,id,env,Geonet.File.SET_DATACOMMONS);
	}

    /**
     *
     * @param dbms
     * @param context
     * @param id
     * @param licenseurl
     * @param imageurl
     * @param jurisdiction
     * @param licensename
     * @param type
     * @throws Exception
     */
	public void setCreativeCommons(Dbms dbms, ServiceContext context, String id, String licenseurl, String imageurl, String jurisdiction, String licensename, String type) throws Exception {
		Element env = new Element("env");
		env.addContent(new Element("imageurl").setText(imageurl));
		env.addContent(new Element("licenseurl").setText(licenseurl));
		env.addContent(new Element("jurisdiction").setText(jurisdiction));
		env.addContent(new Element("licensename").setText(licensename));
		env.addContent(new Element("type").setText(type));

		manageCommons(dbms,context,id,env,Geonet.File.SET_CREATIVECOMMONS);
	}

    /**
     *
     * @param dbms
		 * @param context
     * @param id
     * @param env
     * @param styleSheet
     * @throws Exception
     */
	private void manageCommons(Dbms dbms, ServiceContext context, String id, Element env, String styleSheet) throws Exception {
		Element md = xmlSerializer.select(dbms, "Metadata", id, context);

		if (md == null) return;

		md.detach();

		String schema = getMetadataSchema(dbms, id);
		transformMd(dbms,context,id,md,env,schema,styleSheet,true);
	}

	//--------------------------------------------------------------------------
	//---
	//--- Privileges API
	//---
	//--------------------------------------------------------------------------

    /**
     *  Adds a permission to a group. Metadata is not reindexed.
     *
     * @param context
     * @param dbms
     * @param mdId
     * @param grpId
     * @param opId
     * @throws Exception
     */
	public void setOperation(ServiceContext context, Dbms dbms, String mdId, String grpId, String opId) throws Exception {
		setOperation(context,dbms,new Integer(mdId),new Integer(grpId),new Integer(opId));
	}

    /**
     * Set metadata privileges.
     * 
     * Administrator can set operation for any groups.
     * 
     * For reserved group (ie. Internet, Intranet & Guest), user MUST be reviewer of one group.
     * For other group, if "Only set privileges to user's groups" is set in catalog configuration
     * user MUST be a member of the group.
     * 
     * @param context
     * @param dbms
     * @param mdId The metadata identifier
     * @param grpId The group identifier
     * @param opId The operation identifier
     * 
     * @throws Exception
     */
	public void setOperation(ServiceContext context, Dbms dbms, int mdId, int grpId, int opId) throws Exception {
        // Check user privileges
        // Session may not be defined when a harvester is running
        if (context.getUserSession() != null) {
            String userProfile = context.getUserSession().getProfile();
            if (!userProfile.equals(Geonet.Profile.ADMINISTRATOR)) {
                int userId = Integer.parseInt(context.getUserSession()
                        .getUserId());
                // Reserved groups
                if (grpId <= 1) {
                    // If user is reviewer, user can change operation for groups
                    // -1, 0, 1
                    String isReviewerQuery = "SELECT groupId FROM UserGroups WHERE userId=? AND profile=?";
                    Element isReviewerRes = dbms.select(isReviewerQuery,
                            userId, Geonet.Profile.REVIEWER);
                    if (isReviewerRes.getChildren().size() == 0) {
                        throw new ServiceNotAllowedEx(
                                "User can't set operation for group "
                                        + grpId
                                        + " because the user in not a Reviewer of any group.");
                    }
                } else {

                    GeonetContext gc = (GeonetContext) context
                            .getHandlerContext(Geonet.CONTEXT_NAME);
                    String userGroupsOnly = settingMan
                            .getValue("system/metadataprivs/usergrouponly");
                    if (userGroupsOnly.equals("true")) {
                        // If user is member of the group, user can set
                        // operation
                        String isMemberQuery = "SELECT groupId FROM UserGroups WHERE groupId=? AND userId=?";
                        Element isMemberRes = dbms.select(isMemberQuery, grpId,
                                userId);
                        if (isMemberRes.getChildren().size() == 0) {
                            throw new ServiceNotAllowedEx(
                                    "User can't set operation for group "
                                            + grpId
                                            + " because the user in not member of this group.");
                        }
                    }
                }
            }
        }
		// Set operation
		String query = "SELECT metadataId FROM OperationAllowed WHERE metadataId=? AND groupId=? AND operationId=?";
		Element elRes = dbms.select(query, mdId, grpId, opId);
		if (elRes.getChildren().size() == 0) {
			dbms.execute("INSERT INTO OperationAllowed(metadataId, groupId, operationId) VALUES(?,?,?)", mdId, grpId, opId);
			if (svnManager != null) {
			    svnManager.setHistory(dbms, mdId+"", context);
			}
		}
	}

    /**
     *
     * @param context
     * @param dbms
     * @param mdId
     * @param grpId
     * @param opId
     * @throws Exception
     */
	public void unsetOperation(ServiceContext context, Dbms dbms, String mdId, String grpId, String opId) throws Exception {
		unsetOperation(context,dbms,new Integer(mdId),new Integer(grpId),new Integer(opId));
	}

    /**
     *
     * @param context
     * @param dbms dbms
     * @param mdId metadata id
     * @param groupId group id
     * @param operId operation id
     * @throws Exception hmm
     */
	public void unsetOperation(ServiceContext context, Dbms dbms, int mdId, int groupId, int operId) throws Exception {
		String query = "DELETE FROM OperationAllowed WHERE metadataId=? AND groupId=? AND operationId=?";
		dbms.execute(query, mdId, groupId, operId);
		if (svnManager != null) {
		    svnManager.setHistory(dbms, mdId+"", context);
		}
	}

    /**
     * Sets VIEW and NOTIFY privileges for a metadata to a group.
     *
     * @param context service context
     * @param dbms the database
     * @param id metadata id
     * @param groupId group id
     * @param fullRightsForGroup TODO
     * @throws Exception hmmm
     */
	public void copyDefaultPrivForGroup(ServiceContext context, Dbms dbms, String id, String groupId, boolean fullRightsForGroup) throws Exception {
        if(StringUtils.isBlank(groupId)) {
            Log.info(Geonet.DATA_MANAGER, "Attempt to set default privileges for metadata " + id + " to an empty groupid");
            return;
        }
		//--- store access operations for group

		setOperation(context, dbms, id, groupId, AccessManager.OPER_VIEW);
		setOperation(context, dbms, id, groupId, AccessManager.OPER_NOTIFY);
		//
		// Restrictive: new and inserted records should not be editable, 
		// their resources can't be downloaded and any interactive maps can't be 
		// displayed by users in the same group 
		if(fullRightsForGroup) {
			setOperation(context, dbms, id, groupId, AccessManager.OPER_EDITING);
			setOperation(context, dbms, id, groupId, AccessManager.OPER_DOWNLOAD);
			setOperation(context, dbms, id, groupId, AccessManager.OPER_DYNAMIC);
		}
		// Ultimately this should be configurable elsewhere
	}

	//--------------------------------------------------------------------------
	//---
	//--- Check User Id to avoid foreign key problems
	//---
	//--------------------------------------------------------------------------

	public boolean isUserMetadataOwner(Dbms dbms, int userId) throws Exception {
		String query = "SELECT id FROM Metadata WHERE owner=?";
		Element elRes = dbms.select(query, userId);
		return (elRes.getChildren().size() != 0);
	}

	public boolean isUserMetadataStatus(Dbms dbms, int userId) throws Exception {
		String query = "SELECT metadataId FROM MetadataStatus WHERE userId=?";
		Element elRes = dbms.select(query, userId);
		return (elRes.getChildren().size() != 0);
	}

	//--------------------------------------------------------------------------
	//---
	//--- Status API
	//---
	//--------------------------------------------------------------------------


    /**
     * Return all status records for the metadata id - current status is the
		 * first child due to sort by DESC on changeDate
		 *
     * @param dbms
     * @param id
		 * @return 
     * @throws Exception
		 *
     */
	public Element getStatus(Dbms dbms, int id) throws Exception {
		String query = "SELECT statusId, userId, changeDate, changeMessage, name FROM StatusValues, MetadataStatus WHERE statusId=id AND metadataId=? ORDER BY changeDate DESC";
		return dbms.select(query, id);
	}

    /**
     * Return status of metadata id.
     *
     * @param dbms
     * @param id
		 * @return 
     * @throws Exception
		 *
     */
	public String getCurrentStatus(Dbms dbms, int id) throws Exception {
		Element status = getStatus(dbms, id);
		if (status == null) return Params.Status.UNKNOWN;
		List<Element> statusKids = status.getChildren();
		if (statusKids.size() == 0) return Params.Status.UNKNOWN;
		return statusKids.get(0).getChildText("statusid");
	}

    /**
     * Set status of metadata id and reindex metadata id afterwards.
     *
     * @param context
     * @param dbms
     * @param id
     * @param status
     * @param changeDate
     * @param changeMessage
     * @throws Exception
     */
	public void setStatus(ServiceContext context, Dbms dbms, int id, int status, String changeDate, String changeMessage) throws Exception {
		setStatusExt(context, dbms, id, status, changeDate, changeMessage);
    indexMetadata(dbms, Integer.toString(id), true, context);
	}

    /**
     * Set status of metadata id and do not reindex metadata id afterwards.
     *
     * @param context
     * @param dbms
     * @param id
     * @param status
     * @param changeDate
     * @param changeMessage
     * @throws Exception
     */
	public void setStatusExt(ServiceContext context, Dbms dbms, int id, int status, String changeDate, String changeMessage) throws Exception {
		dbms.execute("INSERT into MetadataStatus(metadataId, statusId, userId, changeDate, changeMessage) VALUES (?,?,?,?,?)", id, status, context.getUserSession().getUserIdAsInt(), changeDate, changeMessage);
		if (svnManager != null) {
		    svnManager.setHistory(dbms, id+"", context);
		}
	}

	//--------------------------------------------------------------------------
	//---
	//--- Categories API
	//---
	//--------------------------------------------------------------------------

    /**
     * Adds a category to a metadata. Metadata is not reindexed.
     * @param dbms
     * @param mdId
     * @param categId
     * @throws Exception
     */
	public void setCategory(ServiceContext context, Dbms dbms, String mdId, String categId) throws Exception {
		Object args[] = { new Integer(mdId), new Integer(categId) };

		if (!isCategorySet(dbms, mdId, categId)) {
			dbms.execute("INSERT INTO MetadataCateg(metadataId, categoryId) VALUES(?,?)", args);
			if (svnManager != null) {
			    svnManager.setHistory(dbms, mdId+"", context);
			}
		}
	}

    /**
     *
     * @param dbms
     * @param mdId
     * @param categId
     * @return
     * @throws Exception
     */
	public boolean isCategorySet(Dbms dbms, String mdId, String categId) throws Exception {
		String query = "SELECT metadataId FROM MetadataCateg " +"WHERE metadataId=? AND categoryId=?";
		Element elRes = dbms.select(query, new Integer(mdId), new Integer(categId));
		return (elRes.getChildren().size() != 0);
	}

    /**
     *
     * @param dbms
     * @param mdId
     * @param categId
     * @throws Exception
     */
	public void unsetCategory(ServiceContext context, Dbms dbms, String mdId, String categId) throws Exception {
		String query = "DELETE FROM MetadataCateg WHERE metadataId=? AND categoryId=?";
		dbms.execute(query, new Integer(mdId), new Integer(categId));
		if (svnManager != null) {
		    svnManager.setHistory(dbms, mdId+"", context);
		}
	}

    /**
     *
     * @param dbms
     * @param mdId
     * @return
     * @throws Exception
     */
	public Element getCategories(Dbms dbms, String mdId) throws Exception {
		String query = "SELECT id, name FROM Categories, MetadataCateg WHERE id=categoryId AND metadataId=?";
		return dbms.select(query, new Integer(mdId));
	}

    /**
     * Update metadata record (not template) using update-fixed-info.xsl
     * 
     * 
     * @param schema
     * @param id
     * @param uuid If the metadata is a new record (not yet saved), provide the uuid for that record
     * @param md
     * @param parentUuid
     * @param updateDatestamp   FIXME ? updateDatestamp is not used when running XSL transformation
     * @param dbms
     * @return
     * @throws Exception
     */
	public Element updateFixedInfo(String schema, String id, String uuid, Element md, String parentUuid, UpdateDatestamp updateDatestamp, Dbms dbms, ServiceContext context) throws Exception {
        boolean autoFixing = settingMan.getValueAsBool("system/autofixing/enable", true);
        if(autoFixing) {
            if(Log.isDebugEnabled(Geonet.DATA_MANAGER))
                Log.debug(Geonet.DATA_MANAGER, "Autofixing is enabled, trying update-fixed-info (updateDatestamp: " + updateDatestamp.name() + ")");
            
        	String query = "SELECT uuid, isTemplate FROM Metadata WHERE id = ?";
            Element rec = dbms.select(query, new Integer(id)).getChild("record");
            Boolean isTemplate = rec != null && !rec.getChildText("istemplate").equals("n");
            
            // don't process templates
                uuid = uuid == null ? rec.getChildText("uuid") : uuid;
                
                //--- setup environment
                Element env = new Element("env");
                env.addContent(new Element("id").setText(id));
                env.addContent(new Element("uuid").setText(uuid));
								Element schemaLoc = new Element("schemaLocation");
                schemaLoc.setAttribute(schemaMan.getSchemaLocation(schema,context));
								env.addContent(schemaLoc);
                
                if (updateDatestamp == UpdateDatestamp.yes) {
                        env.addContent(new Element("changeDate").setText(new ISODate().toString()));
                }
                if(parentUuid != null) {
                    env.addContent(new Element("parentUuid").setText(parentUuid));
                }
                env.addContent(new Element("datadir").setText(Lib.resource.getDir(dataDir, Params.Access.PRIVATE, id)));

                // add original metadata to result
                Element result = new Element("root");
                result.addContent(md);
                // add 'environment' to result
                env.addContent(new Element("siteURL")   .setText(getSiteURL()));
                Element system = settingMan.get("system", -1);
                env.addContent(Xml.transform(system, appPath + Geonet.Path.STYLESHEETS+ "/xml/config.xsl"));
                result.addContent(env);
                // apply update-fixed-info.xsl
                String styleSheet;
                if(isTemplate) {
                    styleSheet = getSchemaDir(schema) + Geonet.File.UPDATE_TEMPLATE_FIXED_INFO;
                } else {
                    styleSheet = getSchemaDir(schema) + Geonet.File.UPDATE_FIXED_INFO;
                }
                result = Xml.transform(result, styleSheet);
                return result;
        } else {
            if(Log.isDebugEnabled(Geonet.DATA_MANAGER))
                Log.debug(Geonet.DATA_MANAGER, "Autofixing is disabled, not applying update-fixed-info");
            return md;
        }
	}

    /**
     * Retrieves the unnotified metadata to update/insert for a notifier service
     *
     * @param dbms
     * @param notifierId
     * @return
     * @throws Exception
     */
    public Map<String,Element> getUnnotifiedMetadata(Dbms dbms, String notifierId) throws Exception {
        if(Log.isDebugEnabled(Geonet.DATA_MANAGER))
            Log.debug(Geonet.DATA_MANAGER, "getUnnotifiedMetadata start");
        Map<String,Element> unregisteredMetadata = new HashMap<String,Element>();

        String query = "select m.id, m.uuid, m.data, mn.notifierId, mn.action from metadata m left join metadatanotifications mn on m.id = mn.metadataId\n" +
                "where (mn.notified is null or mn.notified = 'n') and (mn.action <> 'd') and (mn.notifierId is null or mn.notifierId = ?)";
        List<Element> results = dbms.select(query, new Integer(notifierId)).getChildren();
        if(Log.isDebugEnabled(Geonet.DATA_MANAGER))
            Log.debug(Geonet.DATA_MANAGER, "getUnnotifiedMetadata after select: " + (results != null));

        if (results != null) {
          for(Element result : results) {
              String uuid = result.getChild("uuid").getText();
              if(Log.isDebugEnabled(Geonet.DATA_MANAGER))
                  Log.debug(Geonet.DATA_MANAGER, "getUnnotifiedMetadata: " + uuid);
              unregisteredMetadata.put(uuid, (Element)((Element)result.clone()).detach());
          }
        }

        if(Log.isDebugEnabled(Geonet.DATA_MANAGER))
            Log.debug(Geonet.DATA_MANAGER, "getUnnotifiedMetadata returning #" + unregisteredMetadata.size() + " results");
        return unregisteredMetadata;
    }

    /**
     * Retrieves the unnotified metadata to delete for a notifier service
     *
     * @param dbms
     * @param notifierId
     * @return
     * @throws Exception
     */
    public Map<String,Element> getUnnotifiedMetadataToDelete(Dbms dbms, String notifierId) throws Exception {
        if(Log.isDebugEnabled(Geonet.DATA_MANAGER))
            Log.debug(Geonet.DATA_MANAGER, "getUnnotifiedMetadataToDelete start");
        Map<String,Element> unregisteredMetadata = new HashMap<String,Element>();
        if(Log.isDebugEnabled(Geonet.DATA_MANAGER))
            Log.debug(Geonet.DATA_MANAGER, "getUnnotifiedMetadataToDelete after dbms");

        String query = "select metadataId as id, metadataUuid as uuid, notifierId, action from metadatanotifications " +
                "where (notified = 'n') and (action = 'd') and (notifierId = ?)";
        List<Element> results = dbms.select(query, new Integer(notifierId)).getChildren();
        if(Log.isDebugEnabled(Geonet.DATA_MANAGER))
            Log.debug(Geonet.DATA_MANAGER, "getUnnotifiedMetadataToDelete after select: " + (results != null));

        if (results != null) {
          for(Element result : results) {
              String uuid = result.getChild("uuid").getText();
              if(Log.isDebugEnabled(Geonet.DATA_MANAGER))
                  Log.debug(Geonet.DATA_MANAGER, "getUnnotifiedMetadataToDelete: " + uuid);
              unregisteredMetadata.put(uuid, (Element)((Element)result.clone()).detach());

          }
        }

        if(Log.isDebugEnabled(Geonet.DATA_MANAGER))
            Log.debug(Geonet.DATA_MANAGER, "getUnnotifiedMetadataToDelete returning #" + unregisteredMetadata.size() + " results");
        return unregisteredMetadata;
    }

    /**
     * Marks a metadata record as notified for a notifier service.
     *
     * @param metadataId    Metadata identifier
     * @param notifierId    Notifier service identifier
     * @param deleteNotification    Indicates if the notification was a delete action
     * @param dbms
     * @throws Exception
     */
    public void setMetadataNotified(String metadataId, String metadataUuid, String notifierId, boolean deleteNotification, Dbms dbms) throws Exception {
        String query = "DELETE FROM MetadataNotifications WHERE metadataId=? AND notifierId=?";
        dbms.execute(query, new Integer(metadataId), new Integer(notifierId));
        dbms.commit();

        if (!deleteNotification) {
            query = "INSERT INTO MetadataNotifications (metadataId, notifierId, metadataUuid, notified, action) VALUES (?,?,?,?,?)";
            dbms.execute(query, new Integer(metadataId), new Integer(notifierId), metadataUuid, "y", "u");
            dbms.commit();
        }

        if(Log.isDebugEnabled(Geonet.DATA_MANAGER))
            Log.debug(Geonet.DATA_MANAGER, "setMetadataNotified finished for metadata with id " + metadataId + "and notitifer with id " + notifierId);
    }

    /**
     * Marks a metadata record as notified for a notifier service.
     *
     * @param metadataId    Metadata identifier
     * @param notifierId    Notifier service identifier
     * @param dbms
     * @throws Exception
     */
    public void setMetadataNotifiedError(String metadataId, String metadataUuid, String notifierId, boolean deleteNotification, String error, Dbms dbms) throws Exception {
        if(Log.isDebugEnabled(Geonet.DATA_MANAGER))
            Log.debug(Geonet.DATA_MANAGER, "setMetadataNotifiedError");
       try {
       String query = "DELETE FROM MetadataNotifications WHERE metadataId=? AND notifierId=?";
       dbms.execute(query, new Integer(metadataId), new Integer(notifierId));

       String action = (deleteNotification == true)?"d":"u";
       query = "INSERT INTO MetadataNotifications (metadataId, notifierId, metadataUuid, notified, action, errormsg) VALUES (?,?,?,?,?,?)";
       dbms.execute(query, new Integer(metadataId), new Integer(notifierId), metadataUuid, "n", action, error);
       dbms.commit();

           if(Log.isDebugEnabled(Geonet.DATA_MANAGER))
               Log.debug(Geonet.DATA_MANAGER, "setMetadataNotifiedError finished for metadata with id " + metadataId + "and notitifer with id " + notifierId);
       }
       catch (Exception ex) {
           ex.printStackTrace();
           throw ex;
       }
    }

    /**
     *
     * @param dbms
     * @return
     * @throws Exception
     */
    public List<Element> retrieveNotifierServices(Dbms dbms) throws Exception {
        String query = "SELECT id, url, username, password FROM MetadataNotifiers WHERE enabled = 'y'";
        return dbms.select(query).getChildren();
    }

	
	/**
	 * Updates all children of the selected parent. Some elements are protected
	 * in the children according to the stylesheet used in
	 * xml/schemas/[SCHEMA]/update-child-from-parent-info.xsl.
	 * 
	 * Children MUST be editable and also in the same schema of the parent. 
	 * If not, child is not updated. 
	 * 
	 * @param srvContext
	 *            service context
	 * @param parentUuid
	 *            parent uuid
	 * @param params
	 *            parameters
	 * @param children
	 *            children
	 * @return
	 * @throws Exception
	 */
	public Set<String> updateChildren(ServiceContext srvContext, String parentUuid, String[] children, Map<String, String> params) throws Exception {
		Dbms dbms = (Dbms) srvContext.getResourceManager().open(Geonet.Res.MAIN_DB);

		String parentId = params.get(Params.ID);
		String parentSchema = params.get(Params.SCHEMA);

		// --- get parent metadata in read/only mode
        boolean forEditing = false, withValidationErrors = false, keepXlinkAttributes = false;
        Element parent = getGeocatMetadata(srvContext, parentId, forEditing, withValidationErrors, keepXlinkAttributes,false, false);

		Element env = new Element("update");
		env.addContent(new Element("parentUuid").setText(parentUuid));
		env.addContent(new Element("siteURL").setText(getSiteURL()));
		env.addContent(new Element("parent").addContent(parent));

		// Set of untreated children (out of privileges, different schemas)
		Set<String> untreatedChildSet = new HashSet<String>();

		// only get iso19139 records
		for (String childId : children) {

			// Check privileges
			if (!accessMan.canEdit(srvContext, childId)) {
				untreatedChildSet.add(childId);
                if(Log.isDebugEnabled(Geonet.DATA_MANAGER))
                    Log.debug(Geonet.DATA_MANAGER, "Could not update child ("
						+ childId + ") because of privileges.");
				continue;
			}

            Element child = getGeocatMetadata(srvContext, childId, forEditing, withValidationErrors, keepXlinkAttributes, false, false);

			String childSchema = child.getChild(Edit.RootChild.INFO,
					Edit.NAMESPACE).getChildText(Edit.Info.Elem.SCHEMA);

			// Check schema matching. CHECKME : this suppose that parent and
			// child are in the same schema (even not profil different)
			if (!childSchema.equals(parentSchema)) {
				untreatedChildSet.add(childId);
                if(Log.isDebugEnabled(Geonet.DATA_MANAGER))
                    Log.debug(Geonet.DATA_MANAGER, "Could not update child ("
						+ childId + ") because schema (" + childSchema
						+ ") is different from the parent one (" + parentSchema
						+ ").");
				continue;
			}

            if(Log.isDebugEnabled(Geonet.DATA_MANAGER))
                Log.debug(Geonet.DATA_MANAGER, "Updating child (" + childId +") ...");

			// --- setup xml element to be processed by XSLT

			Element rootEl = new Element("root");
			Element childEl = new Element("child").addContent(child.detach());
			rootEl.addContent(childEl);
			rootEl.addContent(env.detach());

			// --- do an XSL transformation

			String styleSheet = getSchemaDir(parentSchema)
					+ Geonet.File.UPDATE_CHILD_FROM_PARENT_INFO;
			Element childForUpdate = new Element("root");
			childForUpdate = Xml.transform(rootEl, styleSheet, params);
			
			xmlSerializer.update(dbms, childId, childForUpdate, new ISODate().toString(), true, null, srvContext);


            // Notifies the metadata change to metatada notifier service
            notifyMetadataChange(dbms, childForUpdate, childId);

			rootEl = null;
		}

		return untreatedChildSet;
	}

    public void addHidingInfo(ServiceContext context, Element md, String id) throws Exception
    {
        md.detach(); // DEBUG

        Dbms dbms = (Dbms) context.getResourceManager().open(Geonet.Res.MAIN_DB);
        Element xPathExpressions = dbms.select(
                "SELECT xPathExpr, level FROM HiddenMetadataElements WHERE metadataId = ?", new Integer(id));

        List elements = Xml.selectNodes(xPathExpressions, "*//xpathexpr");
        xPathExpressions.detach();
        List levels = Xml.selectNodes(xPathExpressions, "*//level");

        Iterator l = levels.iterator();

        // System.out.println(Xml.getString(xPathExpressions)); // DEBUG
        for (Iterator i = elements.iterator(); i.hasNext();)
        {
            try
            {
                String expr = ((Element) i.next()).getText();
                String level = ((Element) l.next()).getText();
                // System.out.println("expr = " + expr + " - level = " + level);
                // // DEBUG

                Element t = Xml.selectElement(md, expr);
                md.detach();

                // System.out.println(Xml.getString(t)); // DEBUG
                if(t != null) {
                    t.addContent(new Element("hide", Edit.NAMESPACE).setAttribute("level", level));
                } else {
                    Log.error(Geonet.DATA_MANAGER, "path to hidden xpath no longer exists. Expr:"+expr+" Level: "+level);
                }

            } catch (JDOMException e) {
                Log.error(Geonet.DATA_MANAGER, "Error occurred adding hiding info: "+e.getMessage());
            }
        }

        md.detach();
    }

    /**
     * TODO : buildInfoElem contains similar portion of code with indexMetadata
     * @param context
     * @param id
     * @param version
     * @return
     * @throws Exception
     */
	public Element buildInfoElem(ServiceContext context, String id, String version) throws Exception {
		Dbms dbms = (Dbms) context.getResourceManager().open(Geonet.Res.MAIN_DB);

		String query ="SELECT schemaId, createDate, changeDate, source, isTemplate, title, "+
									"uuid, isHarvested, harvestUuid, popularity, rating, owner, groupOwner, displayOrder FROM Metadata WHERE id = ?";

		// add Metadata table infos: schemaId, createDate, changeDate, source,
		Element rec = dbms.select(query, new Integer(id)).getChild("record");

		String  schema     = rec.getChildText("schemaid");
		String  createDate = rec.getChildText("createdate");
		String  changeDate = rec.getChildText("changedate");
		String  source     = rec.getChildText("source");
		String  isTemplate = rec.getChildText("istemplate");
		String  title      = rec.getChildText("title");
		String  uuid       = rec.getChildText("uuid");
		String  isHarvested= rec.getChildText("isharvested");
		String  harvestUuid= rec.getChildText("harvestuuid");
		String  popularity = rec.getChildText("popularity");
		String  rating     = rec.getChildText("rating");
		String  owner      = rec.getChildText("owner");
        String  groupOwner = rec.getChildText("groupowner");
        String  displayOrder = rec.getChildText("displayorder");

		Element info = new Element(Edit.RootChild.INFO, Edit.NAMESPACE);

		addElement(info, Edit.Info.Elem.ID,          id);
		addElement(info, Edit.Info.Elem.SCHEMA,      schema);
		addElement(info, Edit.Info.Elem.CREATE_DATE, createDate);
		addElement(info, Edit.Info.Elem.CHANGE_DATE, changeDate);
		addElement(info, Edit.Info.Elem.IS_TEMPLATE, isTemplate);
		addElement(info, Edit.Info.Elem.TITLE,       title);
		addElement(info, Edit.Info.Elem.SOURCE,      source);
		addElement(info, Edit.Info.Elem.UUID,        uuid);
		addElement(info, Edit.Info.Elem.IS_HARVESTED,isHarvested);
		addElement(info, Edit.Info.Elem.POPULARITY,  popularity);
		addElement(info, Edit.Info.Elem.RATING,      rating);
        addElement(info, Edit.Info.Elem.DISPLAY_ORDER,  displayOrder);

		if (isHarvested.equals("y"))
			info.addContent(harvestMan.getHarvestInfo(harvestUuid, id, uuid));

		if (version != null)
			addElement(info, Edit.Info.Elem.VERSION, version);

		buildExtraMetadataInfo(context, id, info);

        if(accessMan.isVisibleToAll(dbms, id)) {
            addElement(info, Edit.Info.Elem.IS_PUBLISHED_TO_ALL, "true");
        }
        else {
            addElement(info, Edit.Info.Elem.IS_PUBLISHED_TO_ALL, "false");
        }
        try {
        	Integer groupOwnerId = Integer.valueOf(groupOwner);
        	addElement(info, "groupOwner", groupOwner);
			Element groupInfo = dbms.select("SELECT logouuid, website FROM groups where id=?", groupOwnerId).getChild("record");
            addElement(info, "groupLogoUuid", groupInfo.getChildText("logouuid"));
            addElement(info, "groupWebsite", groupInfo.getChildText("website"));
        } catch (NumberFormatException nfe) {
        	// that's ok, sometime groupOwner is blank
        }
		// add owner name
		query = "SELECT username FROM Users WHERE id = ?";
		Element record = dbms.select(query, new Integer(owner)).getChild("record");
		if (record != null) {
			String ownerName = record.getChildText("username");
			addElement(info, Edit.Info.Elem.OWNERNAME, ownerName);
		}

		// add categories
		List categories = dbms.select("SELECT id, name FROM MetadataCateg, Categories "+
												"WHERE metadataId = ? AND categoryId = id ORDER BY id", new Integer(id)).getChildren();

        for (Object category1 : categories) {
            Element category = (Element) category1;
            addElement(info, Edit.Info.Elem.CATEGORY, category.getChildText("name"));
        }

		// add subtemplates
		/* -- don't add as we need to investigate indexing for the fields 
		   -- in the metadata table used here
		List subList = getSubtemplates(dbms, schema);
		if (subList != null) {
			Element subs = new Element(Edit.Info.Elem.SUBTEMPLATES);
			subs.addContent(subList);
			info.addContent(subs);
		}
		*/


        // Add validity information
        List<Element> validationInfo = getValidationStatus(dbms, id);
        if (validationInfo == null || validationInfo.size() == 0) {
            addElement(info, Edit.Info.Elem.VALID, "-1");
        } else {
            String isValid = "1";
            for (Object elem : validationInfo) {
                Element vi = (Element) elem;
                String type = vi.getChildText("valtype");
                String status = vi.getChildText("status");
                if ("0".equals(status)) {
                    isValid = "0";
                }
                String ratio = "xsd".equals(type) ? "" : vi.getChildText("failed") + "/" + vi.getChildText("tested");
                
                info.addContent(new Element(Edit.Info.Elem.VALID + "_details").
                        addContent(new Element("type").setText(type)).
                        addContent(new Element("status").setText(status)).
                        addContent(new Element("ratio").setText(ratio))
                        );
            }
            addElement(info, Edit.Info.Elem.VALID, isValid);
        }
        
		// add baseUrl of this site (from settings)
        String protocol = settingMan.getValue(Geonet.Settings.SERVER_PROTOCOL);
		String host    = settingMan.getValue(Geonet.Settings.SERVER_HOST);
		String port    = settingMan.getValue(Geonet.Settings.SERVER_PORT);
		addElement(info, Edit.Info.Elem.BASEURL, protocol + "://" + host + (port == "80" ? "" : ":" + port) + baseURL);
		addElement(info, Edit.Info.Elem.LOCSERV, "/srv/en" );
		return info;
	}

    /**
     * Returns a mapping from ISO 639-1 codes to ISO 639-2 codes.
     *
     * @param context here, there, and everywhere
     * @param iso639_1_set 639-1 codes to be mapped
     * @return mapping
     * @throws Exception hmm
     */
    public Map<String, String> iso639_1_to_iso639_2(ServiceContext context, Set<String> iso639_1_set) throws Exception {
        Map<String, String> result = new HashMap<String, String>();
        if(iso639_1_set!= null && !iso639_1_set.isEmpty() ) {
            Dbms dbms = (Dbms) context.getResourceManager().open(Geonet.Res.MAIN_DB);
            String query = "SELECT code, shortcode FROM IsoLanguages WHERE ";
            for(String iso639_1 : iso639_1_set) {
                query += "shortcode = ? OR ";
            }
            query = query.substring(0, query.lastIndexOf("OR"));
            @SuppressWarnings(value = "unchecked")
            List<Element> records = dbms.select(query, iso639_1_set.toArray()).getChildren();
            for(Element record : records) {
                result.put(record.getChildText("shortcode"), record.getChildText("code"));
            }
        }
        return result;       
    }

	/**
	 * Add extra information about the metadata record
	 * which depends on context and could not be stored in db or Lucene index.
	 * 
	 * @param context
	 * @param id
	 * @param info
	 * @throws Exception
	 */
	public void buildExtraMetadataInfo(ServiceContext context, String id,
			Element info) throws Exception {
		if (accessMan.canEdit(context, id))
			addElement(info, Edit.Info.Elem.EDIT, "true");

		if (accessMan.isOwner(context, id)) {
			addElement(info, Edit.Info.Elem.OWNER, "true");
		}

		Element operations = accessMan.getAllOperations(context, id, context.getIpAddress());
		Set<String> hsOper = accessMan.getOperations(context, id, context.getIpAddress(), operations);

		addElement(info, Edit.Info.Elem.VIEW,     			String.valueOf(hsOper.contains(AccessManager.OPER_VIEW)));
		addElement(info, Edit.Info.Elem.NOTIFY,   			String.valueOf(hsOper.contains(AccessManager.OPER_NOTIFY)));
		addElement(info, Edit.Info.Elem.DOWNLOAD, 			String.valueOf(hsOper.contains(AccessManager.OPER_DOWNLOAD)));
		addElement(info, Edit.Info.Elem.DYNAMIC,  			String.valueOf(hsOper.contains(AccessManager.OPER_DYNAMIC)));
		addElement(info, Edit.Info.Elem.FEATURED, 			String.valueOf(hsOper.contains(AccessManager.OPER_FEATURED)));

		if (!hsOper.contains(AccessManager.OPER_DOWNLOAD)) {
			boolean gDownload = Xml.selectNodes(operations, "guestoperations/record[operationid="+AccessManager.OPER_DOWNLOAD+" and groupid='-1']").size() == 1;
			addElement(info, Edit.Info.Elem.GUEST_DOWNLOAD, gDownload+"");
		}

	}

    /**
     *
     * @param root
     * @param name
     * @param value
     */
	private static void addElement(Element root, String name, String value) {
		root.addContent(new Element(name).setText(value));
	}

    /**
     *
     * @return
     */
	public String getSiteID() {
		return settingMan.getValue("system/site/siteId");
	}

	
	//---------------------------------------------------------------------------
	//---
	//--- Static methods are for external modules like GAST to be able to use
	//--- them.
	//---
	//---------------------------------------------------------------------------

    /**
     *
     * @param md
     */
	public static void setNamespacePrefix(Element md){
		//--- if the metadata has no namespace or already has a namespace then
		//--- we must skip this phase

		Namespace ns = md.getNamespace();
    if (ns == Namespace.NO_NAMESPACE || (!md.getNamespacePrefix().equals("")))
      return;
		//--- set prefix for iso19139 metadata

		ns = Namespace.getNamespace("gmd", md.getNamespace().getURI());
		setNamespacePrefix(md, ns);
	}

    /**
     *
     * @param md
     * @param ns
     */
	private static void setNamespacePrefix(Element md, Namespace ns) {
		if (md.getNamespaceURI().equals(ns.getURI()))
			md.setNamespace(ns);

		for (Object o : md.getChildren())
			setNamespacePrefix((Element) o, ns);
	}

    /**
     *
     * @param md
     * @throws Exception
     */
	public void setNamespacePrefixUsingSchemas(String schema, Element md) throws Exception {
		//--- if the metadata has no namespace or already has a namespace prefix
		//--- then we must skip this phase
		Namespace ns = md.getNamespace();
		if (ns == Namespace.NO_NAMESPACE)  
			return;

		MetadataSchema mds = schemaMan.getSchema(schema);
		
		//--- get the namespaces and add prefixes to any that are
		//--- default (ie. prefix is '') if namespace match one of the schema
		ArrayList nsList = new ArrayList();
		nsList.add(ns);
		nsList.addAll(md.getAdditionalNamespaces());
        for (Object aNsList : nsList) {
            Namespace aNs = (Namespace) aNsList;
            if (aNs.getPrefix().equals("")) { // found default namespace
                String prefix = mds.getPrefix(aNs.getURI());
                if (prefix == null) {
                    Log.warning(Geonet.DATA_MANAGER, "Metadata record contains a default namespace " + aNs.getURI() + " (with no prefix) which does not match any " + schema + " schema's namespaces.");
                }
                ns = Namespace.getNamespace(prefix, aNs.getURI());
                setNamespacePrefix(md, ns);
                if (!md.getNamespace().equals(ns)) {
                    md.removeNamespaceDeclaration(aNs);
                    md.addNamespaceDeclaration(ns);
                }
            }
        }
    }

    /**
     *
     * @param dbms
     * @param md
     * @param id
     * @throws Exception
     */
    public void notifyMetadataChange(Dbms dbms, Element md, String id) throws Exception {
        String isTemplate = getMetadataTemplate(dbms, id);

        if (isTemplate.equals("n")) {
            GeonetContext gc = (GeonetContext) servContext.getHandlerContext(Geonet.CONTEXT_NAME);

            String uuid = getMetadataUuid(dbms, id);
            gc.getMetadataNotifier().updateMetadata(md, id, uuid, dbms, gc);
        }
    }

    /**
     *
     * @param dbms
     * @param id
     * @param uuid
     * @throws Exception
     */
    private void notifyMetadataDelete(Dbms dbms, String id, String uuid) throws Exception {
        GeonetContext gc = (GeonetContext) servContext.getHandlerContext(Geonet.CONTEXT_NAME);
        gc.getMetadataNotifier().deleteMetadata(id, uuid, dbms, gc);        
    }

	/**
	 * Update group owner when handling privileges during import.
	 * Does not update the index.
	 * 
	 * @param dbms
	 * @param mdId
	 * @param grpId
	 * @throws Exception
	 */
	public void setGroupOwner(Dbms dbms, String mdId, String grpId)
			throws Exception {
		dbms.execute("UPDATE Metadata SET groupOwner=? WHERE id=?", Integer
				.parseInt(grpId), Integer.parseInt(mdId));
	}

    /**
     *
     * @param dbms
     * @return
     * @throws Exception
     */
    public Element getCswCapabilitiesInfo(Dbms dbms) throws Exception {
        return dbms.select("SELECT * FROM CswServerCapabilitiesInfo");
    }

    /**
     *
     * @param dbms
     * @param language
     * @return
     * @throws Exception
     */
    public CswCapabilitiesInfo getCswCapabilitiesInfo(Dbms dbms, String language) throws Exception {

        CswCapabilitiesInfo cswCapabilitiesInfo = new CswCapabilitiesInfo();
        cswCapabilitiesInfo.setLangId(language);
        Element capabilitiesInfoRecord = dbms.select("SELECT * FROM CswServerCapabilitiesInfo WHERE langId = ?", language);

        List<Element> records = capabilitiesInfoRecord.getChildren();
        for(Element record : records) {
            String field = record.getChild("field").getText();
            String label = record.getChild("label").getText();

            if (field.equals("title")) {
                cswCapabilitiesInfo.setTitle(label);
            }
            else if (field.equals("abstract")) {
                cswCapabilitiesInfo.setAbstract(label);
            }
            else if (field.equals("fees")) {
                cswCapabilitiesInfo.setFees(label);
            }
            else if (field.equals("accessConstraints")) {
                cswCapabilitiesInfo.setAccessConstraints(label);
            }
        }
        return cswCapabilitiesInfo;
    }

    /**
     *
     * @param dbms
     * @param cswCapabilitiesInfo
     * @throws Exception
     */
    public void saveCswCapabilitiesInfo(Dbms dbms, CswCapabilitiesInfo cswCapabilitiesInfo)
            throws Exception {

        String langId = cswCapabilitiesInfo.getLangId();

        dbms.execute("UPDATE CswServerCapabilitiesInfo SET label = ? WHERE langId = ? AND field = ?", cswCapabilitiesInfo.getTitle(), langId, "title");
        dbms.execute("UPDATE CswServerCapabilitiesInfo SET label = ? WHERE langId = ? AND field = ?", cswCapabilitiesInfo.getAbstract(), langId, "abstract");
        dbms.execute("UPDATE CswServerCapabilitiesInfo SET label = ? WHERE langId = ? AND field = ?", cswCapabilitiesInfo.getFees(), langId, "fees");
        dbms.execute("UPDATE CswServerCapabilitiesInfo SET label = ? WHERE langId = ? AND field = ?",  cswCapabilitiesInfo.getAccessConstraints(), langId, "accessConstraints");
    }

    /**
     * Replaces the contents of table CustomElementSet.
     *
     * @param dbms database
     * @param customElementSet customelementset definition to save
     * @throws Exception hmm
     */
    public void saveCustomElementSets(Dbms dbms, CustomElementSet customElementSet) throws Exception {
        dbms.execute("DELETE FROM CustomElementSet");
        for(String xpath : customElementSet.getXpaths()) {
             if(StringUtils.isNotEmpty(xpath)) {
                 dbms.execute("INSERT INTO CustomElementSet (xpath) VALUES (?)", xpath);
             }
        }
    }

    /**
     * Retrieves contents of CustomElementSet.
     *
     * @param dbms database
     * @return List of elements (denoted by XPATH)
     * @throws Exception hmm
     */
    public List<Element> getCustomElementSets(Dbms dbms) throws Exception {
		Element customElementSetList = dbms.select("SELECT * FROM CustomElementSet");
        List<Element> records = customElementSetList.getChildren();
        return records;
    }

    private void hideElements(ServiceContext context, Element elMd, String id, boolean forEditing, boolean allowDbmsClosing) throws Exception {
        Dbms dbms = (Dbms) context.getResourceManager().open(Geonet.Res.MAIN_DB);
        try {
            boolean forceHideElements = false;
            hideElements(context, dbms, elMd, id, forEditing, forceHideElements);
        } finally {
            try {
                dbms.commit();
            }finally {
                if(allowDbmsClosing) {
                    context.getResourceManager().close(Geonet.Res.MAIN_DB, dbms);
                }
            }
        }
    }

    /**
     *
     * @param context Null when called from indexMetadata.
     * @param elMd
     * @param id
     * @param forEditing
     * @throws Exception
     */
    private void hideElements(ServiceContext context, Dbms dbms, Element elMd, String id, boolean forEditing, boolean forceHide) throws Exception
    {
        Element xPathExpressions = dbms.select(
                "SELECT xPathExpr, level FROM HiddenMetadataElements WHERE metadataId = ?", new Integer(id));

        AccessManager am = this.getAccessManager();

        // Editors can always see all elements
        if (!forceHide && (forEditing || (context != null && am.canEdit(context, id)))) {
            return;
        }

        Set<String> groups = null;

        if (context != null && context.getUserSession()!=null && context.getIpAddress() != null) {
            groups = am.getUserGroups(dbms, context.getUserSession(), context.getIpAddress(), true);
        }

        List<?> elements = Xml.selectNodes(xPathExpressions, "*//xpathexpr");
        xPathExpressions.detach();
        List<?> levels = Xml.selectNodes(xPathExpressions, "*//level");

        Iterator<?> l = levels.iterator();

        List<Element> removeElms = new ArrayList<Element>(elements.size());

        for (Iterator<?> i = elements.iterator(); i.hasNext();)
        {
            try
            {

                String expr = ((Element)i.next()).getText();
                String level = ((Element)l.next()).getText();
                Log.debug(Geonet.DATA_MANAGER, "Hide expr = " + expr + " - level = " + level);

                // Intranet level for admin groups: no hiding
                if ((groups != null && groups.contains("0") && "intranet".equals(level)))
                {
                    continue;
                }

                // ASSERT: we must hide the element

                // Find the element using the XPath expr
                List<?> targetElms = Xml.selectNodes(elMd, expr);
                if (targetElms == null || targetElms.size() == 0)
                {
                    Log.debug(Geonet.DATA_MANAGER, "ERROR no targetElms found for " + expr);
                    continue;
                }

                // Found target
                Element targetElm = (Element)targetElms.get(0);

                // We cannot remove immediately since this will break
                // XPath expressions: like /descendant::gmd:electronicMailAddress[2]
                // So we remember the elements to be removed.
                removeElms.add(targetElm);
            } catch (JDOMException e)
            {
                e.printStackTrace();
            }
        }

        // Remove elements marked for removal
        for (Iterator<Element> i = removeElms.iterator(); i.hasNext();)
        {
            Element removeElm = i.next();
            Parent removeElmParent = removeElm.getParent();

            // Could happen if parent or ancestor was already removed
            if (removeElmParent == null) {
                Log.debug(Geonet.DATA_MANAGER, "No parent found for" + removeElm.getName());
                continue;
            }

            Log.debug(Geonet.DATA_MANAGER, "Removing " + removeElm.getName());
            removeElmParent.removeContent(removeElm);
            // TODO ?? t.setAttribute("nilreason", "withheld", ns);
        }

        elMd.detach();
    }

	//--------------------------------------------------------------------------
	//---
	//--- Variables
	//---
	//--------------------------------------------------------------------------

	private String baseURL;

	private EditLib editLib;

	private AccessManager  accessMan;
	private SearchManager  searchMan;
	private SettingManager settingMan;
	private SchemaManager  schemaMan;
	private HarvestManager harvestMan;
    private final ReusableObjManager reusableObjMan;
    private final ExtentManager extentMan;
    private ThesaurusManager thesaurusMan;
    private String dataDir;
	private String thesaurusDir;
    private ServiceContext servContext;
	private String appPath;
	private String stylePath;
	private static String FS = File.separator;
	private XmlSerializer xmlSerializer;
	private SvnManager svnManager;

    /**
     * TODO javadoc.
     */
	class IncreasePopularityTask implements Runnable {
        private ServiceContext srvContext;
        String id;

        /**
         *
         * @param srvContext
         * @param id
         */
        public IncreasePopularityTask(ServiceContext srvContext,
				String id) {
        			this.srvContext = srvContext;
        			this.id = id;
    	}

		public void run() {
        Dbms dbms = null;
        try {
       	    dbms  = (Dbms) srvContext.getResourceManager().openDirect(Geonet.Res.MAIN_DB);
            String updateQuery = "UPDATE Metadata SET popularity = popularity +1 WHERE id = ?";
            Integer iId = new Integer(id);
            dbms.execute(updateQuery, iId);
            indexMetadata(dbms, id, false, srvContext, false, false, false);
        }
        catch (Exception e) {
            Log.error(Geonet.DATA_MANAGER, "The following exception is ignored: " + e.getMessage());
			e.printStackTrace();
		}
        finally {
				try {
					if (dbms != null) srvContext.getResourceManager().close(Geonet.Res.MAIN_DB, dbms);
				}
                catch (Exception e) {
					Log.error(Geonet.DATA_MANAGER, "There may have been an error updating the popularity of the metadata "+id+". Error: " + e.getMessage());
					e.printStackTrace();
				}
			}

        }

	}

    public enum UpdateDatestamp {
        yes, no
    }


	public boolean isIndexing() {
	    synchronized (indexing) {
	        return !indexing.isEmpty();
        }
    }

    public void updateXlinkObjects(Dbms dbms, String metadataId, String lang, Element md, Element... updatedXLinks) throws Exception {
        ProcessParams params = new ProcessParams(dbms, ReusableObjectLogger.THREAD_SAFE_LOGGER, metadataId, md, md, thesaurusMan,
                extentMan, baseURL, settingMan, false, lang, servContext);
        for (Element xlink : updatedXLinks) {
            editLib.removeEditingInfo(xlink);
            Collection<Element> newElements = reusableObjMan.updateXlink(xlink, params);

            for (Element element : newElements) {
                element.detach();
            }

            Element parentElement = xlink.getParentElement();
            int index = parentElement.indexOf(xlink);
            parentElement.addContent(index + 1, newElements);
        }

        dbms.commit();
    }

    public void setThesaurusManager(ThesaurusManager thesaurusMan2) {
        this.thesaurusMan = thesaurusMan2;
    }
}
