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
import jeeves.exceptions.OperationNotAllowedEx;
import jeeves.exceptions.XSDValidationErrorEx;
import jeeves.resources.dbms.Dbms;
import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;
import jeeves.utils.Log;
import jeeves.utils.Util;
import jeeves.utils.Xml;
import jeeves.utils.Xml.ErrorHandler;
import jeeves.xlink.Processor;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Edit;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.constants.Params;
import org.fao.geonet.exceptions.NoSchemaMatchesException;
import org.fao.geonet.exceptions.SchemaMatchConflictException;
import org.fao.geonet.exceptions.SchematronValidationErrorEx;
import org.fao.geonet.jms.ClusterConfig;
import org.fao.geonet.jms.Producer;
import org.fao.geonet.jms.message.reindex.ReIndexMessage;
import org.fao.geonet.kernel.csw.domain.CswCapabilitiesInfo;
import org.fao.geonet.kernel.csw.domain.CustomElementSet;
import org.fao.geonet.kernel.harvest.HarvestManager;
import org.fao.geonet.kernel.schema.MetadataSchema;
import org.fao.geonet.kernel.search.LuceneIndexField;
import org.fao.geonet.kernel.search.SearchManager;
import org.fao.geonet.kernel.search.spatial.Pair;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.lib.Lib;
import org.fao.geonet.services.metadata.StatusActions;
import org.fao.geonet.services.metadata.StatusActionsFactory;
import org.fao.geonet.util.IDFactory;
import org.fao.geonet.util.ISODate;
import org.fao.geonet.util.ThreadUtils;
import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.filter.ElementFilter;

import java.io.File;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
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
     * @param context
     * @param svnManager
     * @param xmlSerializer
     * @param scm
     * @param sm
     * @param am
     * @param dbms
     * @param ss
     * @param baseURL
     * @param dataDir
     * @param thesaurusDir TODO
     * @param appPath
     * @throws Exception
     */
	public DataManager(ServiceContext context, SvnManager svnManager, XmlSerializer xmlSerializer, SchemaManager scm,
                       SearchManager sm, AccessManager am, Dbms dbms, SettingManager ss, String baseURL, String dataDir,
                       String thesaurusDir, String appPath) throws Exception {
		searchMan = sm;
		accessMan = am;
		settingMan= ss;
		schemaMan = scm;
		editLib = new EditLib(schemaMan);
        servContext=context;

		this.baseURL = baseURL;
        this.dataDir = dataDir;
        this.thesaurusDir = thesaurusDir;
		this.appPath = appPath;

		stylePath = context.getAppPath() + FS + Geonet.Path.STYLESHEETS + FS;

		this.xmlSerializer = xmlSerializer;
		this.svnManager    = svnManager;

		init(context, dbms, false, true);
	}

	/**
	 * Init Data manager and refresh index if needed. 
	 * Can also be called after GeoNetwork startup in order to rebuild the lucene 
	 * index
	 * 
	 * @param context
	 * @param dbms
	 * @param force         Force reindexing all from scratch
     * @param startup whether this is the call at application startup
	 *
	 **/
	public synchronized void init(ServiceContext context, Dbms dbms, boolean force, boolean startup) throws Exception {

        initMetadata(context, dbms, force, startup);
        initWorkspace(context, dbms, force, startup);

	}

    /**
     *
     * @param context
     * @param dbms
     * @param force
     * @param startup
     * @param result
     * @param docs
     * @param workspace
     * @throws Exception
     */
    private void reindex(ServiceContext context, Dbms dbms, boolean force, boolean startup, Element result, Map<String,String> docs, boolean workspace) throws Exception {
        System.out.println("reindex(). workspace? " + workspace);
        // set up results HashMap for post processing of records to be indexed
        List<String> toIndex = new ArrayList<String>();

        if (Log.isDebugEnabled(Geonet.DATA_MANAGER)) {
            if(workspace) {
                Log.debug(Geonet.DATA_MANAGER, "INDEX CONTENT (workspace):");
            }
            else {
                Log.debug(Geonet.DATA_MANAGER, "INDEX CONTENT (metadata):");
            }
        }

        // index all metadata in DBMS if needed
        for(int i = 0; i < result.getContentSize(); i++) {
            // get metadata
            Element record = (Element) result.getContent(i);
            String id = record.getChildText("id");

            if (Log.isDebugEnabled(Geonet.DATA_MANAGER)) {
                if(workspace) {
                    Log.debug(Geonet.DATA_MANAGER, "metadata - record ("+ id +")");
                }
                else {
                    Log.debug(Geonet.DATA_MANAGER, "workspace - record ("+ id +")");
                }
            }
            String idxLastChange = docs.get(id);

            // if metadata is not indexed index it
            if (idxLastChange == null) {
                Log.debug(Geonet.DATA_MANAGER, "-  will be indexed");
                toIndex.add(id);
            }
            // else, if indexed version is not the latest index it
            else {
                docs.remove(id);
                String lastChange = record.getChildText("changedate");

                if(Log.isDebugEnabled(Geonet.DATA_MANAGER)) {
                    Log.debug(Geonet.DATA_MANAGER, "- lastChange: " + lastChange);
                    Log.debug(Geonet.DATA_MANAGER, "- idxLastChange: " + idxLastChange);
                }

                // date in index contains 't', date in DBMS contains 'T'
                if (force || !idxLastChange.equalsIgnoreCase(lastChange)) {
                    if(Log.isDebugEnabled(Geonet.DATA_MANAGER)) {
                        Log.debug(Geonet.DATA_MANAGER, "-  will be indexed");
                    }
                    toIndex.add(id);
                }
            }
        }

        // if anything to index then schedule it to be done after servlet is
        // up so that any links to local fragments are resolvable
        if ( toIndex.size() > 0 ) {
            boolean sendReIndexMessages = !startup;
            batchRebuild(context,toIndex, workspace, sendReIndexMessages);
        }

        if (docs.size() > 0 && Log.isDebugEnabled(Geonet.DATA_MANAGER)) { // anything left?
            Log.debug(Geonet.DATA_MANAGER, "INDEX HAS RECORDS THAT ARE NOT IN DB:");
        }

        // remove from index docs not in DBMS
        for ( String id : docs.keySet() ) {
            searchMan.delete(LuceneIndexField._ID, id, workspace);
            if(Log.isDebugEnabled(Geonet.DATA_MANAGER)) {
                Log.debug(Geonet.DATA_MANAGER, "- removed doc (" + id + ") from index");
            }
        }
    }

    /**
     *
     * @param context
     * @param dbms
     * @param force
     * @param startup
     * @throws Exception
     */
    private void initMetadata(ServiceContext context, Dbms dbms, boolean force, boolean startup) throws Exception {
        // get all metadata from DB
        Element result = dbms.select("SELECT id, changeDate FROM Metadata ORDER BY id ASC");

        if(Log.isDebugEnabled(Geonet.DATA_MANAGER)) {
            Log.debug(Geonet.DATA_MANAGER, "DB CONTENT (metadata):\n'"+ Xml.getString(result) +"'");
        }
        boolean workspace = false;
        // get lastchangedate of all metadata in index
        Map<String,String> docs = searchMan.getDocsChangeDate(workspace);

        System.out.println("initmmdd: db contents: " + Xml.getString(result) + "\nfromidx: " + docs.size());

        reindex(context, dbms, force, startup, result, docs, workspace);
    }

    /**
     *
     * @param context
     * @param dbms
     * @param force
     * @param startup
     * @throws Exception
     */
    private void initWorkspace(ServiceContext context, Dbms dbms, boolean force, boolean startup) throws Exception {
        // get all workspace from DB
        Element result = dbms.select("SELECT id, changeDate FROM Workspace ORDER BY id ASC");

        if(Log.isDebugEnabled(Geonet.DATA_MANAGER)) {
            Log.debug(Geonet.DATA_MANAGER, "DB CONTENT (workspace):\n'"+ Xml.getString(result) +"'");
        }
        // get lastchangedate of all workspace in index
        boolean workspace = true;
        Map<String,String> docs = searchMan.getDocsChangeDate(workspace);

        System.out.println("initowkrspace: db contents: " + Xml.getString(result) + "\nfromidx: " + docs.size());

        reindex(context, dbms, force, startup, result, docs, workspace);
    }

    /**
     * TODO javadoc.
     *
     * @param context
     * @param workspace
     * @throws Exception
     */
	public synchronized void rebuildIndexXLinkedMetadata(ServiceContext context, boolean workspace) throws Exception {
		
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
            batchRebuild(context,stringIds, workspace, true);
		}
	}
    
    /**
     * TODO javadoc.
     *
     * @param context
     * @param ids
     * @param workspace
     * @param sendReIndexMessages whether to send reindex messages to peer nodes in cluster
     */
    private void batchRebuild(ServiceContext context, List<String> ids, boolean workspace, boolean sendReIndexMessages) {

        System.out.println("batchrebuild(). workspace? " + workspace);

        // split reindexing task according to number of processors we can assign
        int threadCount = ThreadUtils.getNumberOfThreads();
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        int perThread;
        if (ids.size() < threadCount) {
            perThread = ids.size();
        }
        else {
            perThread = ids.size() / threadCount;
        }
        int index = 0;

        while(index < ids.size()) {
            int start = index;
            int count = Math.min(perThread,ids.size()-start);
            // create threads to process this chunk of ids
            Runnable worker = new IndexMetadataTask(context, ids, workspace, start, count, sendReIndexMessages);
            executor.execute(worker);
            index += count;
        }

        executor.shutdown();
    }

    /**
     * TODO javadoc.
     * @param dbms dbms
     * @param id metadata id
     * @param workspace
     * @throws Exception hmm
     */
    public void indexInThreadPoolIfPossible(Dbms dbms, String id, boolean workspace) throws Exception {
        if(ServiceContext.get() == null ) {
            boolean indexGroup = false;
            indexMetadata(dbms, id, workspace, indexGroup, true);
        } else {
            indexInThreadPool(ServiceContext.get(), id, dbms, workspace, true);
        }
    }

    /**
     * Adds metadata ids to the thread pool for indexing.
     *
     * @param context
     * @param id
     * @param dbms
     * @param workspace
     * @param sendReIndexMessages
     * @throws SQLException
     */
	public void indexInThreadPool(ServiceContext context, String id, Dbms dbms, boolean workspace, boolean sendReIndexMessages) throws SQLException {
        indexInThreadPool(context, Collections.singletonList(id), dbms, workspace, sendReIndexMessages);
    }
    /**
     * Adds metadata ids to the thread pool for indexing.
     *
     * @param context
     * @param ids
     * @param workspace
     * @param sendReIndexMessages
     * @throws SQLException
     */
    public void indexInThreadPool(ServiceContext context, List<String> ids, Dbms dbms, boolean workspace, boolean sendReIndexMessages) throws SQLException {

        if(dbms != null) dbms.commit();

        try {
            GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);

            if (ids.size() > 0) {
                Runnable worker = new IndexMetadataTask(context, ids, workspace, sendReIndexMessages);
                gc.getThreadPool().runTask(worker);
            }
        } 
        catch (Exception e) {
            Log.error(Geonet.DATA_MANAGER, e.getMessage());
            e.printStackTrace();
            // TODO why swallow
        }
    }

    /**
     * TODO javadoc.
     */
    final class IndexMetadataTask implements Runnable {

        private final ServiceContext context;
        private final List<String> ids;
        private int beginIndex;
        private int count;
        private boolean workspace;
        private boolean sendReIndexMessages;

        IndexMetadataTask(ServiceContext context, List<String> ids, boolean workspace, boolean sendReIndexMessages) {
            System.out.println("indemxetadatataxk. Workspace? " + workspace);
            this.context = context;
            this.ids = ids;
            this.workspace = workspace;
            this.beginIndex = 0;
            this.count = ids.size();
            this.sendReIndexMessages = sendReIndexMessages;
        }
        IndexMetadataTask(ServiceContext context, List<String> ids, boolean workspace, int beginIndex, int count, boolean sendReIndexMessages) {
            System.out.println("indemxetadatatask. Workspace? " + workspace);
            this.context = context;
            this.ids = ids;
            this.workspace = workspace;
            this.beginIndex = beginIndex;
            this.count = count;
            this.sendReIndexMessages = sendReIndexMessages;
        }

        /**
         * TODO javadoc.
         */
        public void run() {
            context.setAsThreadLocal();
            try {
                // poll context to see whether servlet is up yet
                while (!context.isServletInitialized()) {
                    if(Log.isDebugEnabled(Geonet.DATA_MANAGER)) {
                        Log.debug(Geonet.DATA_MANAGER, "Waiting for servlet to finish initializing..");
                    }
                    Thread.sleep(10000); // sleep 10 seconds
                }
                Dbms dbms = (Dbms) context.getResourceManager().openDirect(Geonet.Res.MAIN_DB);
                try {
                    if (this.ids.size() > 1) {
                        // servlet up so safe to index all metadata that needs indexing
                        startIndexGroup();
                        try {
                            for(int i=beginIndex; i<beginIndex+count; i++) {
                                try {
                                    indexMetadataGroup(dbms, this.ids.get(i), this.workspace, this.sendReIndexMessages);
                                }
                                catch (Exception e) {
                                    if(workspace) {
                                        Log.error(Geonet.INDEX_ENGINE, "Error indexing workspace '"+this.ids.get(i)+"': "+e.getMessage()+"\n"+ Util.getStackTrace(e));
                                    }
                                    else {
                                        Log.error(Geonet.INDEX_ENGINE, "Error indexing metadata '"+this.ids.get(i)+"': "+e.getMessage()+"\n"+ Util.getStackTrace(e));
                                    }
                                }
                            }
                        }
                        finally {
                            endIndexGroup();
                        }
                    }
                    else {
                        indexMetadata(dbms, this.ids.get(0), false, this.workspace, this.sendReIndexMessages);
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
            }
        }
    }

    /**
     *
     * @throws Exception
     */
	public void startIndexGroup() throws Exception {
		searchMan.startIndexGroup();
	}

    /**
     *
     * @throws Exception
     */
	public void endIndexGroup() throws Exception {
		searchMan.endIndexGroup();
	}

    /**
     * TODO remove this useless method.
     * @param dbms
     * @param id
     * @param workspace
     * @param sendReIndexMessages whether to send reindex messages to peer nodes in cluster
     * @throws Exception
     */
    public void indexMetadataGroup(Dbms dbms, String id, boolean workspace, boolean sendReIndexMessages) throws Exception {
        System.out.println("idxmdgroup workdpace? " + workspace);

        if(Log.isDebugEnabled(Geonet.DATA_MANAGER)) {
            if(workspace) {
                Log.debug(Geonet.DATA_MANAGER, "Indexing workspace record (" + id + ")");
            }
            else {
                Log.debug(Geonet.DATA_MANAGER, "Indexing metadata record (" + id + ")");
            }
        }
        indexMetadata(dbms, id, workspace, workspace, sendReIndexMessages);
    }

    /**
     * Indexes metadata without sending ReIndexMessage to JMS topic.
     *
     * @param dbms
     * @param id
     * @param indexGroup
     * @param workspace
     * @throws Exception
     */
    public void indexMetadataWithoutSendingTopic(Dbms dbms, String id, boolean indexGroup, boolean workspace) throws Exception {
        System.out.println("**** indexMetadataWithoutSendingTopic: workspace : " + workspace + " id: " + id);
        Vector<Element> moreFields = new Vector<Element>();

        // get metadata, extracting and indexing any xlinks
        Element md;
        if(workspace) {
            md   = xmlSerializer.selectNoXLinkResolver(dbms, "Workspace", id);
        }
        else {
            md   = xmlSerializer.selectNoXLinkResolver(dbms, "Metadata", id);
        }

        //
        // e.g. status change tries to reindex both workspace and metadata, but not always a workspace copy exists
        //
        if(md == null) {
            if(workspace) {
                System.out.println("indexMetadata failed to retrieve md with id " + id + " from workspace");
            }
            else {
                System.out.println("indexMetadata failed to retrieve md with id " + id + " from metadata");
            }
            return;
        }

        if (xmlSerializer.resolveXLinks()) {
            List<Attribute> xlinks = Processor.getXLinks(md);
            if (xlinks.size() > 0) {
                moreFields.add(SearchManager.makeField(LuceneIndexField._HASXLINKS, "1", true, true));
                StringBuilder sb = new StringBuilder();
                for (Attribute xlink : xlinks) {
                    sb.append(xlink.getValue()); sb.append(" ");
                }
                moreFields.add(SearchManager.makeField(LuceneIndexField._XLINK, sb.toString(), true, true));
                Processor.detachXLink(md);
            }
            else {
                moreFields.add(SearchManager.makeField(LuceneIndexField._HASXLINKS, "0", true, true));
            }
        }
        else {
            moreFields.add(SearchManager.makeField(LuceneIndexField._HASXLINKS, "0", true, true));
        }

        String query;
        if(! workspace) {
        // get metadata table fields
        //***String query = "SELECT schemaId, createDate, changeDate, source, isTemplate, isLocked, root, " +
        //        "title, uuid, isHarvested, owner, groupOwner, popularity, rating FROM Metadata WHERE id = ?";
            query = "SELECT schemaId, createDate, changeDate, source, isTemplate, isLocked, lockedBy, root, " +
                "title, uuid, isHarvested, owner, popularity, rating FROM Metadata WHERE id = ?";
        }
        else {
            query = "SELECT schemaId, createDate, changeDate, source, isTemplate, isLocked, lockedBy, root, " +
                    "title, uuid, isHarvested, owner, popularity, rating FROM Workspace WHERE id = ?";
        }

        Element rec = dbms.select(query, id).getChild("record");


        if(rec == null) {
            String msg ;
            if(workspace) {
                msg = "**** ERROR indexMetadataWithoutSendingTopic failed to retrieve md with id " + id + " from Workspace";
            }
            else {
                msg = "**** ERROR indexMetadataWithoutSendingTopic failed to retrieve md with id " + id + " from Metadata";
            }
            System.out.println(msg);
            throw new Exception(msg);
        }

        String  schema     = rec.getChildText("schemaid");
        String  createDate = rec.getChildText("createdate");
        String  changeDate = rec.getChildText("changedate");
        String  source     = rec.getChildText("source");
        String  isTemplate = rec.getChildText("istemplate");
        String  isLocked = rec.getChildText("islocked");
        String  lockedBy = rec.getChildText("lockedby");
        String  root       = rec.getChildText("root");
        String  title      = rec.getChildText("title");
        String  uuid       = rec.getChildText("uuid");
        String  isHarvested= rec.getChildText("isharvested");
        String  owner      = rec.getChildText("owner");
        //***
        // String  groupOwner = rec.getChildText("groupowner");
        String  popularity = rec.getChildText("popularity");
        String  rating     = rec.getChildText("rating");

        if(Log.isDebugEnabled(Geonet.DATA_MANAGER)) {
            Log.debug(Geonet.DATA_MANAGER, "record schema (" + schema + ")"); //DEBUG
            Log.debug(Geonet.DATA_MANAGER, "record createDate (" + createDate + ")"); //DEBUG
        }

        if(workspace) {
            moreFields.add(SearchManager.makeField(LuceneIndexField._IS_WORKSPACE, "true", true, true));
        }
        else {
            moreFields.add(SearchManager.makeField(LuceneIndexField._IS_WORKSPACE, "false", true, true));
        }

        moreFields.add(SearchManager.makeField(LuceneIndexField._ROOT, root, true, true));
        moreFields.add(SearchManager.makeField(LuceneIndexField._SCHEMA, schema, true, true));
        moreFields.add(SearchManager.makeField(LuceneIndexField._CREATE_DATE, createDate, true, true));
        moreFields.add(SearchManager.makeField(LuceneIndexField._CHANGE_DATE, changeDate, true, true));
        moreFields.add(SearchManager.makeField(LuceneIndexField._SOURCE, source, true, true));
        moreFields.add(SearchManager.makeField(LuceneIndexField._IS_TEMPLATE, isTemplate, true, true));
        moreFields.add(SearchManager.makeField(LuceneIndexField._TITLE, title, true, true));
        moreFields.add(SearchManager.makeField(LuceneIndexField._UUID, uuid, true, true));
        moreFields.add(SearchManager.makeField(LuceneIndexField._IS_HARVESTED, isHarvested, true, true));
        moreFields.add(SearchManager.makeField(LuceneIndexField._IS_LOCKED, isLocked, true, true));
        moreFields.add(SearchManager.makeField(LuceneIndexField._LOCKEDBY, lockedBy, true, true));
        moreFields.add(SearchManager.makeField(LuceneIndexField._OWNER, owner, true, true));
        moreFields.add(SearchManager.makeField(LuceneIndexField._DUMMY, "0", false, true));
        moreFields.add(SearchManager.makeField(LuceneIndexField._POPULARITY,  popularity,  true, true));
        moreFields.add(SearchManager.makeField(LuceneIndexField._RATING, rating, true, true));

        if (owner != null) {
            String userQuery = "SELECT username, surname, name, profile FROM Users WHERE id = ?";

            Element user = dbms.select(userQuery,  owner).getChild("record");

            moreFields.add(SearchManager.makeField(LuceneIndexField._USERINFO, user.getChildText("username") + "|" +
                    user.getChildText("surname") + "|" + user.getChildText("name") + "|" + user.getChildText("profile"),
                    true, false));

            moreFields.add(SearchManager.makeField(LuceneIndexField._OWNERNAME, user.getChildText("username"), true, false));
        }
        //***
        // if (groupOwner != null) {
        //    moreFields.add(SearchManager.makeField("_groupOwner", groupOwner, true, true));
        //}
        // get privileges
        List operations = dbms
                .select("SELECT groupId, operationId FROM OperationAllowed WHERE metadataId = ? ORDER BY operationId ASC", id)
                .getChildren();
        for (Object operation1 : operations) {
            Element operation = (Element) operation1;
            String groupId = operation.getChildText("groupid");
            String operationId = operation.getChildText("operationid");
            moreFields.add(SearchManager.makeField("_op" + operationId, groupId, true, true));
        }
        // get categories
        List categories = dbms
                .select("SELECT id, name FROM MetadataCateg, Categories WHERE metadataId = ? AND categoryId = id ORDER BY id", id)
                .getChildren();
        for (Object category1 : categories) {
            Element category = (Element) category1;
            String categoryName = category.getChildText("name");
            moreFields.add(SearchManager.makeField(LuceneIndexField._CAT, categoryName, true, true));
        }

        // get status
        // TODO why not use getCurrentStatus() here ?
        @SuppressWarnings(value = "unchecked")
        List<Element> statuses = dbms.select("SELECT statusId, userId, changeDate FROM MetadataStatus WHERE metadataId = ? ORDER BY changeDate DESC", id)
                .getChildren();

        if (statuses.size() > 0) {
            Element stat = (Element)statuses.get(0);
            String status = stat.getChildText("statusid");
            System.out.println("**************************** status to index: " + status);
            moreFields.add(SearchManager.makeField("_status", status, true, true));
            String statusChangeDate = stat.getChildText("changedate");
            moreFields.add(SearchManager.makeField(LuceneIndexField._STATUSCHANGEDATE, statusChangeDate, true, true));
        }
        else {
            System.out.println("**************************** status not found ,unkonwn ");
            moreFields.add(SearchManager.makeField(LuceneIndexField._STATUS, Params.Status.UNKNOWN, true, true));
        }



        // getValidationInfo
        // -1 : not evaluated
        // 0 : invalid
        // 1 : valid
        @SuppressWarnings(value = "unchecked")
        List<Element> validationInfo = dbms.select("SELECT valType, status FROM Validation WHERE metadataId = ?", id).getChildren();
        if (validationInfo.size() == 0) {
            moreFields.add(SearchManager.makeField(LuceneIndexField._VALID, "-1", true, true));
        }
        else {
            String isValid = "1";
            for (Object elem : validationInfo) {
                Element vi = (Element) elem;
                String type = vi.getChildText("valtype");
                String status = vi.getChildText("status");
                if ("0".equals(status)) {
                    isValid = "0";
                }
                moreFields.add(SearchManager.makeField("_valid_" + type, status, true, true));
            }
            moreFields.add(SearchManager.makeField("LuceneIndexField._VALID", isValid, true, true));
        }

        if (indexGroup) {
            searchMan.startIndexGroup();
            searchMan.indexGroup(schemaMan.getSchemaDir(schema), md, id, moreFields, isTemplate, title, workspace);
            searchMan.endIndexGroup();
        }
        else {
            searchMan.index(schemaMan.getSchemaDir(schema), md, id, moreFields, isTemplate, title, workspace);
        }
    }
    /**
     * TODO javadoc.
     *
     * @param dbms
     * @param id
     * @param indexGroup
     * @param workspace
     * @param sendReIndexMessages whether to send reindex messages to peer nodes in cluster
     * @throws Exception
     */
	public void indexMetadata(Dbms dbms, String id, boolean indexGroup, boolean workspace, boolean sendReIndexMessages) throws Exception {
        System.out.println("indexMetadata. workspace ? " + workspace);
        try {
            //
            // notify peers if clustered
            //
            if(sendReIndexMessages && ClusterConfig.isEnabled()) {
                ReIndexMessage message = new ReIndexMessage();
                message.setId(id);
                message.setWorkspace(workspace);
                message.setIndexGroup(Boolean.toString(indexGroup));
                message.setSenderClientID(ClusterConfig.getClientID());
                Producer reIndexProducer = ClusterConfig.get(Geonet.ClusterMessageTopic.REINDEX);

                if(reIndexProducer == null) {
                    System.err.println("CLUSTER ERROR: DataManager fails to retrieve producer for REINDEX message. Starting ClusterConfiguration verification.");
                    try {
                        ClusterConfig.verifyClusterConfig();
                        System.err.println("ClusterConfiguration verification could not confirm the problem. Trying once more to get the reindex producer.");
                        reIndexProducer = ClusterConfig.get(Geonet.ClusterMessageTopic.REINDEX);
                    }
                    catch(Exception x) {
                        System.err.println("ClusterConfiguration verification has confirmed the problem. Reinitializing ClusterConfiguration (TODO really do it).");
                        // TODO ClusterConfig.initialize();
                    }
                }

                reIndexProducer.produce(message);
            }
            indexMetadataWithoutSendingTopic(dbms, id, indexGroup, workspace);

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
		String schemaLoc = md.getAttributeValue("schemaLocation", Geonet.XSI_NAMESPACE);
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
		String schemaLoc = md.getAttributeValue("schemaLocation", Geonet.XSI_NAMESPACE);
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
	public Element doSchemaTronForEditor(String schema,Element md,String lang) throws Exception { 
    	// enumerate the metadata xml so that we can report any problems found  
    	// by the schematron_xml script to the geonetwork editor 
    	editLib.enumerateTree(md); 
    	
    	// get an xml version of the schematron errors and return for error display 
    	Element schemaTronXmlReport = getSchemaTronXmlReport(schema, md, lang, null); 
    	
    	// remove editing info added by enumerateTree 
    	editLib.removeEditingInfo(md); 
    	
    	return schemaTronXmlReport; 
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
		List list = dbms.select("SELECT schemaId FROM Metadata WHERE id = ?", id).getChildren();

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

		Element schemaTronXml = dataMan.doSchemaTronForEditor(schema,xml,context.getLanguage());
		xml.detach();
		if (schemaTronXml != null && schemaTronXml.getContent().size() > 0) {
			Element schemaTronReport = dataMan.doSchemaTronForEditor(schema,xml,context.getLanguage());

            List<Namespace> theNSs = new ArrayList<Namespace>();
            theNSs.add(Namespace.getNamespace("geonet", "http://www.fao.org/geonetwork"));
            theNSs.add(Namespace.getNamespace("svrl", "http://purl.oclc.org/dsdl/svrl"));

            Element failedAssert = Xml.selectElement(schemaTronReport, "geonet:report/svrl:schematron-output/svrl:failed-assert", theNSs);

            Element failedSchematronVerification = Xml.selectElement(schemaTronReport, "geonet:report/geonet:schematronVerificationError", theNSs);

            if ((failedAssert != null) || (failedSchematronVerification != null)) {
			    throw new SchematronValidationErrorEx("Schematron errors detected for file "+fileName+" - "
					    + Xml.getString(schemaTronReport) + " for more details",schemaTronReport);
            }
		}

	}

    /**
     * Creates XML schematron report for each set of rules defined in schema directory.
     * @param schema
     * @param md
     * @param lang
     * @param valTypeAndStatus
     * @return
     * @throws Exception
     */
	private Element getSchemaTronXmlReport(String schema, Element md, String lang, Map<String, Integer[]> valTypeAndStatus) throws Exception {
		// NOTE: this method assumes that you've run enumerateTree on the 
		// metadata

		MetadataSchema metadataSchema = getSchema(schema);
		String[] rules = metadataSchema.getSchematronRules();
		
		// Schematron report is composed of one or more report(s)
		// for each set of rules.
		Element schemaTronXmlOut = new Element("schematronerrors",
				Edit.NAMESPACE);

		for (String rule : rules) {
			// -- create a report for current rules.
			// Identified by a rule attribute set to shematron file name
            if(Log.isDebugEnabled(Geonet.DATA_MANAGER))
			Log.debug(Geonet.DATA_MANAGER, " - rule:" + rule);
			String ruleId = rule.substring(0, rule.indexOf(".xsl"));
			Element report = new Element("report", Edit.NAMESPACE);
			report.setAttribute("rule", ruleId,
					Edit.NAMESPACE);

			String schemaTronXmlXslt = metadataSchema.getSchemaDir() + File.separator
					+ rule;
			try {
				Map<String,String> params = new HashMap<String,String>();
				params.put("lang", lang);
				params.put("rule", rule);
				params.put("thesaurusDir", this.thesaurusDir);
				Element xmlReport = Xml.transform(md, schemaTronXmlXslt, params);
				if (xmlReport != null) {
					report.addContent(xmlReport);
				}
				// add results to persitent validation information
				int firedRules = 0;
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
				Integer[] results = {invalidRules!=0?0:1, firedRules, invalidRules};
				if (valTypeAndStatus != null) {
				    valTypeAndStatus.put(ruleId, results);
				}
			} catch (Exception e) {
				Log.error(Geonet.DATA_MANAGER,"WARNING: schematron xslt "+schemaTronXmlXslt+" failed");

                // If an error occurs that prevents to verify schematron rules, add to show in report
                Element errorReport = new Element("schematronVerificationError", Edit.NAMESPACE);
                errorReport.addContent("Schematron error ocurred, rules could not be verified: " + e.getMessage());
                report.addContent(errorReport);

				e.printStackTrace();
			}

			// -- append report to main XML report.
			schemaTronXmlOut.addContent(report);
		}

		return schemaTronXmlOut;
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

		List list = dbms.select(query, id).getChildren();

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

		List list = dbms.select(query, id).getChildren();

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
		//*** String query = "SELECT id, uuid, schemaId, isTemplate, isHarvested, createDate, "+
		//					"       changeDate, source, title, root, owner, groupOwner, displayOrder "+
		//					"FROM   Metadata "+
		//					"WHERE id=?";
        String query = "SELECT id, uuid, schemaId, isTemplate, isHarvested, isLocked, lockedBy, createDate, "+
                "       changeDate, source, title, root, owner, displayOrder "+
                "FROM   Metadata "+
                "WHERE id=?";

		List list = dbms.select(query, id).getChildren();

		if (list.size() == 0)
			return null;

		Element record = (Element) list.get(0);

		MdInfo info = new MdInfo();

		info.id          = id;
		info.uuid        = record.getChildText("uuid");
		info.schemaId    = record.getChildText("schemaid");
		info.isHarvested = "y".equals(record.getChildText("isharvested"));
        info.isLocked = "y".equals(record.getChildText("islocked"));
        if(info.isLocked) {
            info.lockedBy = record.getChildText("lockedby");
        }
		info.createDate  = record.getChildText("createdate");
		info.changeDate  = record.getChildText("changedate");
		info.source      = record.getChildText("source");
		info.title       = record.getChildText("title");
		info.root        = record.getChildText("root");
		info.owner       = record.getChildText("owner");
		//***
		// info.groupOwner  = record.getChildText("groupowner");
        info.displayOrder  = record.getChildText("displayorder");

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
     * TODO javadoc.
     *
     * @param dbms the dbms
     * @param id metadata id
     * @param isTemplate whether it is set to be a template
     * @param title optional title
     * @throws Exception hmm
     */
	public void setTemplate(Dbms dbms, String id, String isTemplate, String title) throws Exception {
		setTemplateExt(dbms, id, isTemplate, title);
        boolean workspace = false;
        indexInThreadPoolIfPossible(dbms, id, workspace);
        workspace = true;
        indexInThreadPoolIfPossible(dbms, id, workspace);
    }

    /**
     * TODO javadoc.
     *
     * @param dbms dbms
     * @param id metadata id
     * @param isTemplate whether it is set to be a template
     * @param title optional title
     * @throws Exception hmm
     */
	public void setTemplateExt(Dbms dbms, String id, String isTemplate, String title) throws Exception {
		if (title == null) dbms.execute("UPDATE Metadata SET isTemplate=? WHERE id=?", isTemplate, id);
		else               dbms.execute("UPDATE Metadata SET isTemplate=?, title=? WHERE id=?", isTemplate, title, id);
	}

    /**
     *
     * @param dbms
     * @param id
     * @param isTemplate
     * @param title
     * @throws Exception
     */
    public void setTemplateExtWorkspace(Dbms dbms, String id, String isTemplate, String title) throws Exception {
        if (title == null) dbms.execute("UPDATE Workspace SET isTemplate=? WHERE id=?", isTemplate, id);
        else               dbms.execute("UPDATE Workspace SET isTemplate=?, title=? WHERE id=?", isTemplate, title, id);
    }

    /**
     * TODO javadoc.
     *
     * @param dbms
     * @param id
     * @param harvestUuid
     * @throws Exception
     */
	public void setHarvested(Dbms dbms, String id, String harvestUuid) throws Exception {
		setHarvestedExt(dbms, id, harvestUuid);
        boolean indexGroup = false;
        boolean workspace = false;
        indexMetadata(dbms, id, indexGroup, workspace, true);
	}

    /**
     * TODO javadoc.
     *
     * @param dbms
     * @param id
     * @param harvestUuid
     * @throws Exception
     */
	public void setHarvestedExt(Dbms dbms, String id, String harvestUuid) throws Exception {
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
	public void setHarvestedExt(Dbms dbms, String id, String harvestUuid, String harvestUri) throws Exception {
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
		String locServ = baseURL +"/"+ Jeeves.Prefix.SERVICE +"/en";

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
    dbms.execute(query, new Integer(displayOrder), id);
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
	public int rateMetadata(Dbms dbms, String id, String ipAddress, int rating) throws Exception {
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

        // rating does not apply to workspace
        boolean workspace = false;
        indexInThreadPoolIfPossible(dbms,id, workspace);

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
     //*** @param groupOwner
     * @param source
     * @param owner
     * @param parentUuid
     * @param isTemplate TODO
     * @return
     * @throws Exception

	public String createMetadata(ServiceContext context, Dbms dbms, String templateId, String groupOwner, String source,
                                 String owner, String parentUuid, String isTemplate) throws Exception {
    */
    public String createMetadata(ServiceContext context, Dbms dbms, String templateId, String editGroup, String source,
                String owner, String parentUuid, String isTemplate) throws Exception {
		String query = "SELECT schemaId, data FROM Metadata WHERE id=?";
		List listTempl = dbms.select(query, templateId).getChildren();

		if (listTempl.size() == 0) {
			throw new IllegalArgumentException("Template id not found : " + templateId);
        }
		Element el = (Element) listTempl.get(0);

		String schema = el.getChildText("schemaid");
		String data   = el.getChildText("data");
		String uuid   = UUID.randomUUID().toString();

		//--- generate a new metadata id
        String id = IDFactory.newID();
		
		// Update fixed info for metadata record only, not for subtemplates
		Element xml = Xml.loadString(data, false);
		if (!isTemplate.equals("s")) {
		    xml = updateFixedInfo(schema, id, uuid, xml, parentUuid, DataManager.UpdateDatestamp.yes, dbms);
		}
		
		//--- store metadata
		//***
		// xmlSerializer.insert(dbms, schema, xml, id, source, uuid, null, null, isTemplate, null, owner, groupOwner, "", context);
        xmlSerializer.insert(dbms, schema, xml, id, source, uuid, null, null, isTemplate, null, owner, "", context);
		//***
		// copyDefaultPrivForGroup(context, dbms, id, groupOwner);
        copyDefaultPrivForGroup(context, dbms, id, editGroup);

		//--- store metadata categories copying them from the template
		List categList = dbms.select("SELECT categoryId FROM MetadataCateg WHERE metadataId = ?", templateId).getChildren();

        for (Object aCategList : categList) {
            Element elRec = (Element) aCategList;
            String catId = elRec.getChildText("categoryid");
            setCategory(context, dbms, id, catId);
        }

        GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
        StatusActionsFactory saf = new StatusActionsFactory(gc.getStatusActionsClass());
        StatusActions sa = saf.createStatusActions(context, dbms);
        saf.onCreate(sa, id);

        boolean workspace = false;
		//--- index metadata
        indexInThreadPoolIfPossible(dbms,id, workspace);
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
     //*** @param group group this metadata belongs to
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

    public String insertMetadata(ServiceContext context, Dbms dbms, String schema, Element metadata, String id,
                                 String uuid, String owner, String group, String source, String isTemplate,
                                 String docType, String title, String category, String createDate, String changeDate,
                                 boolean ufo, boolean index) throws Exception {
    */
    public String insertMetadata(ServiceContext context, Dbms dbms, String schema, Element metadata, String id,
                String uuid, String owner, String groupId, String source, String isTemplate,
                String docType, String title, String category, String createDate, String changeDate,
        boolean ufo, boolean index) throws Exception {

        //--- force namespace prefix for iso19139 metadata
        setNamespacePrefixUsingSchemas(schema, metadata);

        if (ufo && isTemplate.equals("n")) {
            String parentUuid = null;
            metadata = updateFixedInfo(schema, id, uuid, metadata, parentUuid, DataManager.UpdateDatestamp.no, dbms);
        }

         if (source == null) {
            source = getSiteID();
         }

        if(StringUtils.isBlank(isTemplate)) {
            isTemplate = "n";
        }

        //--- store metadata
        xmlSerializer.insert(dbms, schema, metadata, id, source, uuid, createDate, changeDate, isTemplate, title, owner,
                docType, context);

        copyDefaultPrivForGroup(context, dbms, id, groupId);

        if (category != null) {
            setCategory(context, dbms, id, category);
        }

        if(index) {
            boolean workspace = false;
            indexInThreadPoolIfPossible(dbms,id, workspace);
        }

        // Notifies the metadata change to metatada notifier service
        notifyMetadataChange(dbms, metadata, id);

        return id;
    }

    public void saveWorkspace(Dbms dbms, String id) throws Exception {
        xmlSerializer.copyToWorkspace(dbms, id);
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
		boolean doXLinks = xmlSerializer.resolveXLinks();
		Element md = xmlSerializer.selectNoXLinkResolver(dbms, "Metadata", id);
		if (md == null) return null;
		md.detach();
		return md;
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
	public Element getMetadata(ServiceContext srvContext, String id, boolean forEditing,
                               boolean withEditorValidationErrors, boolean keepXlinkAttributes) throws Exception {
		Dbms dbms = (Dbms) srvContext.getResourceManager().open(Geonet.Res.MAIN_DB);
		boolean doXLinks = xmlSerializer.resolveXLinks();
		Element md = xmlSerializer.selectNoXLinkResolver(dbms, "Metadata", id);
		if (md == null) return null;

		String version = null;

		if (forEditing) { // copy in xlink'd fragments but leave xlink atts to editor
			if (doXLinks) Processor.processXLink(md); 
			String schema = getMetadataSchema(dbms, id);
			
			if (withEditorValidationErrors) {
			    version = doValidate(srvContext.getUserSession(), dbms, schema, id, md, srvContext.getLanguage(), forEditing).two();
			}
            else {
                editLib.expandElements(schema, md);
                editLib.getVersionForEditing(schema, id, md);
            }
		}
        else {
			if (doXLinks) {
			    if (keepXlinkAttributes) {
			        Processor.processXLink(md);
			    } else {
			        Processor.detachXLink(md);
			    }
			}
		}

		md.addNamespaceDeclaration(Edit.NAMESPACE);
		Element info = buildInfoElem(srvContext, id, version);
		md.addContent(info);

		md.detach();
		return md;
	}

    /**
     * TODO javadoc.
     *
     * @param dbms
     * @param userId
     * @param metadataId
     * @throws Exception
     */
    public void lockMetadata(Dbms dbms, String userId, String metadataId) throws Exception {
        String query = "UPDATE Metadata set isLocked='y', lockedBy=?, owner=? WHERE id=?";
        Vector<Serializable> args = new Vector<Serializable>();
        args.add(userId);
        // set owner to same userid as lockedBy
        args.add(userId);
        args.add(metadataId);
        dbms.execute(query, args.toArray());

        boolean workspace = false;
        indexMetadata(dbms, metadataId, false, workspace, true);
        workspace = true;
        indexMetadata(dbms, metadataId, false, workspace, true);
    }

    /**
     * TODO javadoc.
     *
     * @param dbms
     * @param metadataId
     * @throws Exception
     */
    public void unLockMetadata(Dbms dbms, String metadataId) throws Exception {

        Log.debug(Geonet.DATA_MANAGER, "unlocking metadata");

        String query = "UPDATE Metadata set isLocked='n', lockedBy='' WHERE id=?";
        Vector<Serializable> args = new Vector<Serializable>();
        args.add(metadataId);
        dbms.execute(query, args.toArray());

        boolean workspace = false;
        indexMetadata(dbms, metadataId, false, workspace, true);
        workspace = true;
        indexMetadata(dbms, metadataId, false, workspace, true);
    }

    /**
     * TODO javadoc.
     *
     * @param dbms
     * @param metadataId
     * @param userId
     * @throws Exception
     */
    public void grabLockMetadata(Dbms dbms, String metadataId, String userId) throws Exception {
        if(isLocked(dbms, metadataId)) {
            String query = "UPDATE Metadata set owner = '" + userId + "', lockedBy = '" + userId + "' WHERE id=?";
            dbms.execute(query, metadataId);
            boolean workspace = false;
            indexMetadata(dbms, metadataId, false, workspace, true);
            query = "UPDATE Workspace set owner = '" + userId + "', lockedBy = '" + userId + "' WHERE id=?";
            dbms.execute(query, metadataId);
            workspace = true;
            indexMetadata(dbms, metadataId, false, workspace, true);
        }
        else {
            throw new OperationNotAllowedEx("Attempt to grab lock of metadata " + metadataId + " which isn't locked");
        }
    }

    /**
     * Returns whether a metadata is locked.
     *
     * @param dbms
     * @param metadataId
     * @return
     */
    public boolean isLocked(Dbms dbms, String metadataId) throws SQLException {
        // check metadata is locked
        String query = "SELECT isLocked from Metadata WHERE id=?";
        Element resultList = dbms.select(query, metadataId);
        Element result = (Element)resultList.getChildren().get(0);
        // md found and is locked
        return result != null && result.getChildText("islocked").equals("y");
    }

    /**
     * TODO javadoc.
     *
     * @param srvContext
     * @param id
     * @param forEditing
     * @param withEditorValidationErrors
     * @param keepXlinkAttributes
     * @param withInfoElement
     * @return
     * @throws Exception
     */
    public Element getMetadataFromWorkspace(ServiceContext srvContext, String id, boolean forEditing,
                               boolean withEditorValidationErrors, boolean keepXlinkAttributes, boolean withInfoElement) throws Exception {
        Dbms dbms = (Dbms) srvContext.getResourceManager().open(Geonet.Res.MAIN_DB);
        boolean doXLinks = xmlSerializer.resolveXLinks();
        Element md = xmlSerializer.selectNoXLinkResolver(dbms, "Workspace", id);

        if (md == null) {
            return null;
        }

        String version = null;

        if (forEditing) { // copy in xlink'd fragments but leave xlink atts to editor
            if (doXLinks) Processor.processXLink(md);
            String schema = getMetadataSchema(dbms, id);

            if (withEditorValidationErrors) {
                version = doValidate(srvContext.getUserSession(), dbms, schema, id, md, srvContext.getLanguage(), forEditing).two();
            }
            else {
                editLib.expandElements(schema, md);
                editLib.getVersionForEditing(schema, id, md);
            }
        }
        else {
            if (doXLinks) {
                if (keepXlinkAttributes) {
                    Processor.processXLink(md);
                } else {
                    Processor.detachXLink(md);
                }
            }
        }

        md.addNamespaceDeclaration(Edit.NAMESPACE);

        if(withInfoElement) {
            Element info = buildInfoElem(srvContext, id, version);
            Element workspace = new Element("workspace");
            workspace.setText("true");
            info.addContent(workspace);
            md.addContent(info);
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
	public boolean existsMetadata(Dbms dbms, String id) throws Exception {
		//FIXME : should use lucene
		List list = dbms.select("SELECT id FROM Metadata WHERE id=?", id).getChildren();
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
     //*** @param groupOwner
     * @throws Exception
     */
	//***public synchronized void updateMetadataOwner(Dbms dbms, String id, String owner, String groupOwner) throws Exception {
	//	dbms.execute("UPDATE Metadata SET owner=?, groupOwner=? WHERE id=?", owner, groupOwner, id);
	//}
    public synchronized void updateMetadataOwner(Dbms dbms, String id, String owner) throws Exception {
        dbms.execute("UPDATE Metadata SET owner=? WHERE id=?", owner, id);
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
                                               String changeDate, boolean updateDateStamp) throws Exception {
        Log.debug(Geonet.DATA_MANAGER, "updating metadata");
        System.out.println("updateMetadata");


        // when invoked from harvesters, session is null?
        UserSession session = context.getUserSession();
        if(session != null) {
            session.removeProperty(Geonet.Session.VALIDATION_REPORT + id);
        }
		String schema = getMetadataSchema(dbms, id);
        if(ufo) {
            String parentUuid = null;
		    md = updateFixedInfo(schema, id, null, md, parentUuid, (updateDateStamp ? DataManager.UpdateDatestamp.yes : DataManager.UpdateDatestamp.no), dbms);
        }
		//--- write metadata to dbms
        xmlSerializer.update(dbms, id, md, changeDate, updateDateStamp, context);

        String isTemplate = getMetadataTemplate(dbms, id);
        // Notifies the metadata change to metatada notifier service
        if (isTemplate.equals("n")) {
            // Notifies the metadata change to metatada notifier service
            notifyMetadataChange(dbms, md, id);
        }

		try {
    		//--- do the validation last - it throws exceptions
    		if (session != null && validate) {
    			doValidate(session, dbms, schema,id,md,lang, false);
    		}

            // Do a commit, otherwise cluster nodes can receive the reindex message, before data stored in database
            dbms.commit();
		}
        finally {
            if(index) {
                //--- update search criteria
                boolean indexGroup = false;
                boolean workspace = false;
                indexMetadata(dbms, id, indexGroup, workspace, true);
            }
		}
		return true;
	}

    /**
     * Moves a metadata from Workspace to Metadata, ending edit session.
     *
     * @param context
     * @param dbms
     * @param id
     * @throws Exception
     */
    public synchronized void moveFromWorkspaceToMetadata(ServiceContext context, Dbms dbms, String id) throws Exception {

        Log.debug(Geonet.DATA_MANAGER, "moving metadata from workspace to metadata");
        Element md = getMetadataFromWorkspace(context, id, false, false, false, false);
        // this is OK, because this method could be invoked e.g. when status is set to APPROVED but earlier status was
        // UNKNOWN so there exist no workspace copy
        if(md == null) {
            Log.warning(Geonet.DATA_MANAGER, "moveFromWorkspaceToMetadata could not find metadata with id: " + id );
            return;
        }
        boolean validate = true;
        boolean ufo = true;
        boolean index = true;
        String language = context.getLanguage();
        boolean updateDateStamp = true;

        unLockMetadata(dbms, id);
        updateMetadata(context, dbms, id, md, validate, ufo, index, language, new ISODate().toString(), updateDateStamp);
        deleteFromWorkspace(dbms, id);
    }

    /**
     * Deletes a metadata from Workspace table and from Lucene index.
     *
     * @param dbms the dbms
     * @param id metadata id
     * @throws Exception hmm
     */
    public synchronized void deleteFromWorkspace(Dbms dbms, String id) throws Exception{
        Log.debug(Geonet.DATA_MANAGER, "deleting metadata from workspace");
        xmlSerializer.deleteFromWorkspace(dbms, id);
        boolean workspace = true;
        searchMan.delete(LuceneIndexField._ID, id, workspace);
    }

    /**
     *
     * @param context
     * @param dbms
     * @param id
     * @param md
     * @param validate
     * @param ufo
     * @param index
     * @param lang
     * @param changeDate
     * @param updateDateStamp
     * @return
     * @throws Exception
     */
    public synchronized boolean updateMetadataWorkspace(ServiceContext context, Dbms dbms, String id, Element md,
                                               boolean validate, boolean ufo, boolean index, String lang,
                                               String changeDate, boolean updateDateStamp) throws Exception {
        System.out.println("updateMetadataWorkspace");
        // when invoked from harvesters, session is null?
        UserSession session = context.getUserSession();
        if(session != null) {
            session.removeProperty(Geonet.Session.VALIDATION_REPORT + id);
        }
        String schema = getMetadataSchema(dbms, id);
        if(ufo) {
            String parentUuid = null;
            md = updateFixedInfo(schema, id, null, md, parentUuid, (updateDateStamp ? DataManager.UpdateDatestamp.yes : DataManager.UpdateDatestamp.no), dbms);
        }
        //--- write metadata to dbms
        xmlSerializer.updateWorkspace(dbms, id, md, changeDate, updateDateStamp, context);

        //--- do the validation last - it throws exceptions
        if (session != null && validate) {
            doValidate(session, dbms, schema,id,md,lang, false);
        }

        // Do a commit, otherwise cluster nodes can receive the reindex message, before data stored in database
        dbms.commit();

        if(true) {
            boolean indexGroup = false;
            boolean workspace = true;
            indexMetadata(dbms, id, indexGroup, workspace, true);
        }

        return true;
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
            if(Log.isDebugEnabled(Geonet.DATA_MANAGER))
      Log.debug(Geonet.DATA_MANAGER, "Validating against XSD " + schema);
			// do XSD validation
			Element md = doc.getRootElement();
    	Element xsdErrors = getXSDXmlReport(schema,md);
    	if (xsdErrors != null && xsdErrors.getContent().size() > 0) {
     		Integer[] results = {0, 0, 0};
     		valTypeAndStatus.put("xsd", results);
            if(Log.isDebugEnabled(Geonet.DATA_MANAGER))
      	Log.debug(Geonet.DATA_MANAGER, "Invalid.");
				valid = false;
    	} else {
     		Integer[] results = {1, 0, 0};
     		valTypeAndStatus.put("xsd", results);
            if(Log.isDebugEnabled(Geonet.DATA_MANAGER))
      	Log.debug(Geonet.DATA_MANAGER, "Valid.");
    	}	
			// then do schematron validation
			Element schematronError = null;
			try {
				editLib.enumerateTree(md);
     		schematronError = getSchemaTronXmlReport(schema, md, lang, valTypeAndStatus);
     		editLib.removeEditingInfo(md);
			} catch (Exception e) {
				e.printStackTrace();
				Log.error(Geonet.DATA_MANAGER, "Could not run schematron validation on metadata "+id+": "+e.getMessage());
				valid = false;
			}
			if (schematronError != null && schematronError.getContent().size() > 0) {
				valid = false;
			}
		}

		// now save the validation status
		try {
			saveValidationStatus(dbms, id, valTypeAndStatus, new ISODate().toString(), false);
		} catch (Exception e) {
			e.printStackTrace();
			Log.error(Geonet.DATA_MANAGER, "Could not save validation status on metadata "+id+": "+e.getMessage());
		}

		return valid;
	}

	/**
	 * Used by the validate embedded service. The validation report is stored in the session.
	 * 
	 * @param session
	 * @param schema
	 * @param id
	 * @param md
	 * @param lang
	 * @param forEditing TODO
	 * @return
	 * @throws Exception
	 */
	public Pair <Element, String> doValidate(UserSession session, Dbms dbms, String schema, String id, Element md,
                                             String lang, boolean forEditing) throws Exception {
	    String version = null;
        if(Log.isDebugEnabled(Geonet.DATA_MANAGER))
		Log.debug(Geonet.DATA_MANAGER, "Creating validation report for record #" + id + " [schema: " + schema + "].");
		
		Element sessionReport = (Element)session.getProperty(Geonet.Session.VALIDATION_REPORT + id);		
		if (sessionReport != null && !forEditing) {
            if(Log.isDebugEnabled(Geonet.DATA_MANAGER))
			Log.debug(Geonet.DATA_MANAGER, "  Validation report available in session.");
			sessionReport.detach();
			return Pair.read(sessionReport, version);
		}
		
	    Map <String, Integer[]> valTypeAndStatus = new HashMap<String, Integer[]>();
		Element errorReport = new Element ("report", Edit.NAMESPACE);
		errorReport.setAttribute("id", id, Edit.NAMESPACE);

		//-- get an XSD validation report and add results to the metadata 
		//-- as geonet:xsderror attributes on the affected elements 
		Element xsdErrors = getXSDXmlReport(schema,md);
		if (xsdErrors != null && xsdErrors.getContent().size() > 0) {
			errorReport.addContent(xsdErrors);
			Integer[] results = {0, 0, 0};
			valTypeAndStatus.put("xsd", results);
            if (Log.isDebugEnabled(Geonet.DATA_MANAGER))
		     Log.debug(Geonet.DATA_MANAGER, "  - XSD error: " + Xml.getString(xsdErrors));
		}
        else {
		    Integer[] results = {1, 0, 0};
		    valTypeAndStatus.put("xsd", results);
		}

		// ...then schematrons
		Element schematronError;
		
		// edit mode
        if (forEditing) {
            if(Log.isDebugEnabled(Geonet.DATA_MANAGER))
              Log.debug(Geonet.DATA_MANAGER, "  - Schematron in editing mode.");
              //-- now expand the elements and add the geonet: elements
              editLib.expandElements(schema, md);
              editLib.getVersionForEditing(schema, id, md);
                    
              //-- get a schematron error report if no xsd errors and add results
              //-- to the metadata as a geonet:schematronerrors element with 
              //-- links to the ref id of the affected element
              schematronError = getSchemaTronXmlReport(schema, md, lang, valTypeAndStatus);
              if (schematronError != null) {
                  md.addContent((Element)schematronError.clone());
                  if (Log.isDebugEnabled(Geonet.DATA_MANAGER))
                  Log.debug(Geonet.DATA_MANAGER, "  - Schematron error: " + Xml.getString(schematronError));
              }
		}
        else {
	        // enumerate the metadata xml so that we can report any problems found 
	        // by the schematron_xml script to the geonetwork editor
	        editLib.enumerateTree(md);

	        // get an xml version of the schematron errors and return for error display
	        schematronError = getSchemaTronXmlReport(schema, md, lang, valTypeAndStatus);

	        // remove editing info added by enumerateTree
	        editLib.removeEditingInfo(md);
		}
        
        if (schematronError != null && schematronError.getContent().size() > 0) {
            Element schematron = new Element("schematronerrors", Edit.NAMESPACE);
            Element idElem = new Element("id", Edit.NAMESPACE);
            idElem.setText(id);
            schematron.addContent(idElem);
            errorReport.addContent(schematronError);
            //throw new SchematronValidationErrorEx("Schematron errors detected - see schemaTron report for "+id+" in htmlCache for more details",schematron);
        }
        
        // Save report in session (invalidate by next update) and db
   		session.setProperty(Geonet.Session.VALIDATION_REPORT + id, errorReport);
		saveValidationStatus(dbms, id, valTypeAndStatus, new ISODate().toString(), forEditing);
   		
		return Pair.read(errorReport, version);
	}
	
	/**
	 * Saves validation status information into the database for the current record.
	 * 
	 * @param id   the metadata record internal identifier
	 * @param valTypeAndStatus  the validation type could be xsd or schematron rules set identifier
	 * @param date the validation date time
     * @param forEditing whether validation was done from the editor
	 */
	private void saveValidationStatus (Dbms dbms, String id, Map<String, Integer[]> valTypeAndStatus, String date, boolean forEditing) throws Exception {
        String table;
        if(forEditing) {
            table = "ValidationWorkspace";
        }
        else {
            table = "Validation";
        }
	    clearValidationStatus(dbms, id, table);
	    Set<String> i = valTypeAndStatus.keySet();
	    for (String type : i) {
	        String query = "INSERT INTO " + table + " (metadataId, valType, status, tested, failed, valDate) VALUES (?,?,?,?,?,?)";
            Integer[] results = valTypeAndStatus.get(type);
            dbms.execute(query, id, type, results[0], results[1], results[2], date);
        }
        dbms.commit();
	}

	/**
	 * Removes validation status information for a metadata record.
     *
	 * @param dbms
	 * @param id   the metadata record internal identifier
     * @param table the validation table to clear
	 */
	private void clearValidationStatus (Dbms dbms, String id, String table) throws Exception {
	    dbms.execute("DELETE FROM " + table + " WHERE metadataId=?", id);
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
	    return dbms.select("SELECT valType, status, tested, failed FROM Validation WHERE metadataId=?", id).getChildren();
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

        dbms.execute("DELETE FROM MetadataRating WHERE metadataId=?", id);
        dbms.execute("DELETE FROM Validation WHERE metadataId=?", id);
        dbms.execute("DELETE FROM Workspace WHERE id=?", id);

        dbms.execute("DELETE FROM MetadataStatus WHERE metadataId=?", id);

        //--- remove metadata
        xmlSerializer.delete(dbms, "Metadata", id, context);

        // Notifies the metadata change to metatada notifier service
        if (isTemplate.equals("n")) {
            notifyMetadataDelete(dbms, id, uuid);
        }

        if(ClusterConfig.isEnabled()) {
            // to delete metadata from index
            ReIndexMessage message = new ReIndexMessage();
            message.setId(id);
            message.setSenderClientID(ClusterConfig.getClientID());
            message.setDeleteMetadata(true);
            message.setWorkspace(false);

            // to delete workspace from index
            ReIndexMessage message2 = new ReIndexMessage();
            message2.setId(id);
            message2.setSenderClientID(ClusterConfig.getClientID());
            message2.setDeleteMetadata(true);
            message2.setWorkspace(true);

            Producer reIndexProducer = ClusterConfig.get(Geonet.ClusterMessageTopic.REINDEX);

            if(reIndexProducer == null) {
                System.err.println("CLUSTER ERROR: DataManager fails to retrieve producer for REINDEX message. Starting ClusterConfiguration verification.");
                try {
                    ClusterConfig.verifyClusterConfig();
                    System.err.println("ClusterConfiguration verification could not confirm the problem. Trying once more to get the reindex producer.");
                    reIndexProducer = ClusterConfig.get(Geonet.ClusterMessageTopic.REINDEX);
                }
                catch(Exception x) {
                    System.err.println("ClusterConfiguration verification has confirmed the problem. Reinitializing ClusterConfiguration (TODO really do it).");
                    // TODO ClusterConfig.initialize();
                }
            }
            else {
                reIndexProducer.produce(message);
                reIndexProducer.produce(message2);
            }

        }

        boolean workspace = false;
        searchMan.delete(LuceneIndexField._ID, id, workspace);
        workspace = true;
        searchMan.delete(LuceneIndexField._ID, id, workspace);

    }

    public synchronized void deleteMetadataWithoutSendingTopic(ServiceContext context, Dbms dbms, String id, boolean workspace) throws Exception {
        //--- update search criteria
        searchMan.delete(LuceneIndexField._ID, id, workspace);
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

		dbms.execute("DELETE FROM MetadataRating WHERE metadataId=?", id);
        dbms.execute("DELETE FROM Validation WHERE metadataId=?", id);
        dbms.execute("DELETE FROM Workspace WHERE id=?", id);
        dbms.execute("DELETE FROM MetadataStatus WHERE metadataId=?", id);

        //--- remove metadata
		xmlSerializer.delete(dbms, "Metadata", id, context);

		//--- update search criteria
        boolean workspace = false;
		searchMan.deleteGroup(LuceneIndexField._ID, id, workspace);
        workspace = true;
        searchMan.deleteGroup(LuceneIndexField._ID, id, workspace);
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
			query += " AND groupId NOT IN (SELECT id from Groups WHERE internal = 'y')";

		dbms.execute(query, id);
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

		dbms.execute(query, id);
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
		Element md = xmlSerializer.select(dbms, "Metadata", id);

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
	public void setThumbnail(ServiceContext context, String id, boolean small, String file) throws Exception {
		int    pos = file.lastIndexOf('.');
		String ext = (pos == -1) ? "???" : file.substring(pos +1);

		Element env = new Element("env");
		env.addContent(new Element("file").setText(file));
		env.addContent(new Element("ext").setText(ext));

        // heikki: manually merged https://github.com/geonetwork/core-geonetwork/commit/ff1b9bff031620e8ec7083249e11109cb219d9cd here
        String host = settingMan.getValue(Geonet.Settings.SERVER_HOST);
        String port = settingMan.getValue(Geonet.Settings.SERVER_PORT);
        String baseUrl = context.getBaseUrl();
        env.addContent(new Element("host").setText(host));
        env.addContent(new Element("port").setText(port));
        env.addContent(new Element("baseUrl").setText(baseUrl));
        // end merge

        manageThumbnail(context, id, small, env, Geonet.File.SET_THUMBNAIL);
	}

    /**
     *
     * @param context
     * @param id
     * @param small
     * @throws Exception
     */
	public void unsetThumbnail(ServiceContext context, String id, boolean small) throws Exception {
		Element env = new Element("env");

		manageThumbnail(context, id, small, env, Geonet.File.UNSET_THUMBNAIL);
	}

    /**
     * TODO javadoc.
     *
     * @param context
     * @param id
     * @param small
     * @param env
     * @param styleSheet
     * @throws Exception
     */
	private void manageThumbnail(ServiceContext context, String id, boolean small, Element env, String styleSheet) throws Exception {
		
        boolean forEditing = false, withValidationErrors = false, keepXlinkAttributes = true, withInfoElement = false;
        Element md = getMetadataFromWorkspace(context, id, forEditing, withValidationErrors, keepXlinkAttributes, withInfoElement);

		if (md == null)
			return;

		md.detach();
		
		Dbms dbms = (Dbms) context.getResourceManager().open(Geonet.Res.MAIN_DB);
		String schema = getMetadataSchema(dbms, id);

		//--- remove thumbnail from metadata

		//--- setup environment
		String type = small ? "thumbnail" : "large_thumbnail";
		env.addContent(new Element("type").setText(type));
		transformMd(dbms,context,id,md,env,schema,styleSheet);
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
     * @throws Exception
     */
	private void transformMd(Dbms dbms, ServiceContext context, String id, Element md, Element env, String schema, String styleSheet) throws Exception {
		
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
		xmlSerializer.updateWorkspace(dbms, id, md, changeDate, true, context);

        // Notifies the metadata change to metatada notifier service
        notifyMetadataChange(dbms, md, id);

        boolean workspace = true;
		//--- update search criteria
        indexInThreadPoolIfPossible(dbms,id, workspace);
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
	public void setDataCommons(Dbms dbms, ServiceContext context, String id, String licenseurl, String imageurl,
                               String jurisdiction, String licensename, String type) throws Exception {
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
	public void setCreativeCommons(Dbms dbms, ServiceContext context, String id, String licenseurl, String imageurl,
                                   String jurisdiction, String licensename, String type) throws Exception {
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
		Element md = xmlSerializer.select(dbms, "Metadata", id);

		if (md == null) return;

		md.detach();

		String schema = getMetadataSchema(dbms, id);
		transformMd(dbms,context,id,md,env,schema,styleSheet);
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
        String query = "DELETE FROM OperationAllowed WHERE metadataId=? AND groupId=? AND operationId=?";
        dbms.execute(query, mdId, grpId, opId);
        if (svnManager != null) {
            svnManager.setHistory(dbms, mdId+"", context);
        }
    }

    /**
     * Sets default privileges for a metadata to a group.
     *
     * @param context service context
     * @param dbms the database
     * @param id metadata id
     * @param groupId group id
     * @throws Exception hmmm
     */
	public void copyDefaultPrivForGroup(ServiceContext context, Dbms dbms, String id, String groupId) throws Exception {
        if(StringUtils.isBlank(groupId)) {
            Log.info(Geonet.DATA_MANAGER, "Attempt to set default privileges for metadata " + id + " to an empty groupid");
            return;
        }
		//--- store access operations for group

		setOperation(context, dbms, id, groupId, AccessManager.OPER_VIEW);
		setOperation(context, dbms, id, groupId, AccessManager.OPER_NOTIFY);

        // set edit privilege, but not to one of the hardcoded 'system' groups
        if(!(groupId.equals("-1") || groupId.equals("0") || groupId.equals("1"))) {
            setOperation(context, dbms, id, groupId, AccessManager.OPER_EDITING);
        }
		//
		// Restrictive: new and inserted records should not be editable, 
		// their resources can't be downloaded and any interactive maps can't be 
		// displayed by users in the same group 
		// setOperation(dbms, id, groupId, AccessManager.OPER_EDITING);
		// setOperation(dbms, id, groupId, AccessManager.OPER_DOWNLOAD);
		// setOperation(dbms, id, groupId, AccessManager.OPER_DYNAMIC);
		// Ultimately this should be configurable elsewhere
	}

	//--------------------------------------------------------------------------
	//---
	//--- Check User Id to avoid foreign key problems
	//---
	//--------------------------------------------------------------------------

	public boolean isUserMetadataOwner(Dbms dbms, String userId) throws Exception {
		String query = "SELECT id FROM Metadata WHERE owner=?";
		Element elRes = dbms.select(query, userId);
		return (elRes.getChildren().size() != 0);
	}

	public boolean isUserMetadataStatus(Dbms dbms, String userId) throws Exception {
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
	public Element getStatus(Dbms dbms, String id) throws Exception {
		String query = "SELECT statusId, userId, changeDate, changeMessage, name FROM StatusValues, MetadataStatus WHERE statusId=id AND metadataId=? ORDER BY changeDate DESC";
		return dbms.select(query, id);
	}

    public String getLastBeforeCurrentStatus(Dbms dbms, String id) throws Exception {
        String query = "SELECT statusId, userId, changeDate, changeMessage FROM MetadataStatus WHERE metadataId=? ORDER BY changeDate DESC";
        Element states = dbms.select(query, id);
        List results = states.getChildren(Jeeves.Elem.RECORD);
        for(Iterator i = results.iterator(); i.hasNext();) {
            Element r = (Element)i.next();
        }
        if(results.size() < 2) {
            Log.info(Geonet.DATA_MANAGER,"did not find a status before current status, total status for this metadata: " + results.size());
            return null;
        }
        else {
            Element lastStatusBeforeCurrentRecord = (Element)results.get(1);
            String lastStatusBeforeCurrent = lastStatusBeforeCurrentRecord.getChildText("statusid");
            return lastStatusBeforeCurrent;
        }
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
	public String getCurrentStatus(Dbms dbms, String id) throws Exception {
		Element status = getStatus(dbms, id);
		if (status == null) {
            return Params.Status.UNKNOWN;
        }
		List<Element> statusKids = status.getChildren();
		if (statusKids.size() == 0) {
            return Params.Status.UNKNOWN;
        }
		return statusKids.get(0).getChildText("statusid");
	}

    /**
     * Returns translated status of metadata id.
     *
     * @param dbms
     * @param id
     * @return
     * @throws Exception
     *
     */
    public String getCurrentStatusName(Dbms dbms, String id) throws Exception {
        String statusId = getCurrentStatus(dbms, id);
        Element elStatus = Lib.local.retrieve(dbms, "StatusValues");
        List<Element> statuses = elStatus.getChildren();
        for(Element status : statuses) {
            if(status.getChildText("id").equals(id)) {
                return status.getChildText("name");
            }
        }
        // not found
        return null;
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
	public void setStatus(ServiceContext context, Dbms dbms, String id, int status, String changeDate, String changeMessage) throws Exception {
        System.out.println("setStatus to " + status);
		setStatusExt(context, dbms, id, status, changeDate, changeMessage);
        boolean indexGroup = false;
        boolean workspace = false;
        indexMetadata(dbms, id, indexGroup, workspace, true);
        workspace = true;
        indexMetadata(dbms, id, indexGroup, workspace, true);
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
	private void setStatusExt(ServiceContext context, Dbms dbms, String id, int status, String changeDate, String changeMessage) throws Exception {
		dbms.execute("INSERT into MetadataStatus(metadataId, statusId, userId, changeDate, changeMessage) VALUES (?,?,?,?,?)",
                id, status, context.getUserSession().getUserId(), changeDate, changeMessage);
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
		Object args[] = { mdId, categId };

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
		Element elRes = dbms.select(query, mdId, categId);
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
		dbms.execute(query, mdId, categId);
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
		return dbms.select(query, mdId);
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
	public Element updateFixedInfo(String schema, String id, String uuid, Element md, String parentUuid,
                                   UpdateDatestamp updateDatestamp, Dbms dbms) throws Exception {
        boolean autoFixing = settingMan.getValueAsBool("system/autofixing/enable", true);
        if(autoFixing) {
            if(Log.isDebugEnabled(Geonet.DATA_MANAGER))
        	Log.debug(Geonet.DATA_MANAGER, "Autofixing is enabled, trying update-fixed-info (updateDatestamp: " + updateDatestamp.name() + ")");
            
        	String query = "SELECT uuid, isTemplate FROM Metadata WHERE id = ?";
            Element rec = dbms.select(query, id).getChild("record");
            Boolean isTemplate = rec != null && !rec.getChildText("istemplate").equals("n");
            
            // don't process templates
            if(isTemplate) {
                if(Log.isDebugEnabled(Geonet.DATA_MANAGER))
                Log.debug(Geonet.DATA_MANAGER, "Not applying update-fixed-info for a template");
                return md;
            }
            else {
                uuid = uuid == null ? rec.getChildText("uuid") : uuid;
                
                //--- setup environment
                Element env = new Element("env");
                env.addContent(new Element("id").setText(id));
                env.addContent(new Element("uuid").setText(uuid));
                
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
                String styleSheet = getSchemaDir(schema) + Geonet.File.UPDATE_FIXED_INFO;
                result = Xml.transform(result, styleSheet);
                return result;
            }
        }
        else {
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
        List<Element> results = dbms.select(query, notifierId).getChildren();
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
        List<Element> results = dbms.select(query, notifierId).getChildren();
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
        dbms.execute(query, metadataId, notifierId);
        dbms.commit();

        if (!deleteNotification) {
            query = "INSERT INTO MetadataNotifications (metadataId, notifierId, metadataUuid, notified, action) VALUES (?,?,?,?,?)";
            dbms.execute(query, metadataId, notifierId, metadataUuid, "y", "u");
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
    public void setMetadataNotifiedError(String metadataId, String metadataUuid, String notifierId,
                                         boolean deleteNotification, String error, Dbms dbms) throws Exception {
        if(Log.isDebugEnabled(Geonet.DATA_MANAGER))
        Log.debug(Geonet.DATA_MANAGER, "setMetadataNotifiedError");
       try {
       String query = "DELETE FROM MetadataNotifications WHERE metadataId=? AND notifierId=?";
       dbms.execute(query, metadataId, notifierId);

       String action = (deleteNotification == true)?"d":"u";
       query = "INSERT INTO MetadataNotifications (metadataId, notifierId, metadataUuid, notified, action, errormsg) VALUES (?,?,?,?,?,?)";
       dbms.execute(query, metadataId, notifierId, metadataUuid, "n", action, error);
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
        Element parent = getMetadata(srvContext, parentId, forEditing, withValidationErrors, keepXlinkAttributes);

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

            Element child = getMetadata(srvContext, childId, forEditing, withValidationErrors, keepXlinkAttributes);

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

            xmlSerializer.update(dbms, childId, childForUpdate, new ISODate().toString(), true, srvContext);


            // Notifies the metadata change to metatada notifier service
            notifyMetadataChange(dbms, childForUpdate, childId);

			rootEl = null;
		}

		return untreatedChildSet;
	}

    /**
     * TODO : buildInfoElem contains similar portion of code with indexMetadata
     * @param context
     * @param id
     * @param version
     * @return
     * @throws Exception
     */
	private Element buildInfoElem(ServiceContext context, String id, String version) throws Exception {
		Dbms dbms = (Dbms) context.getResourceManager().open(Geonet.Res.MAIN_DB);

		String query ="SELECT schemaId, createDate, changeDate, source, isTemplate, title, "+
									"uuid, isHarvested, isLocked, lockedBy, harvestUuid, popularity, rating, owner, displayOrder FROM Metadata WHERE id = ?";

		// add Metadata table infos: schemaId, createDate, changeDate, source,
		Element rec = dbms.select(query, id).getChild("record");

		String  schema     = rec.getChildText("schemaid");
		String  createDate = rec.getChildText("createdate");
		String  changeDate = rec.getChildText("changedate");
		String  source     = rec.getChildText("source");
		String  isTemplate = rec.getChildText("istemplate");
		String  title      = rec.getChildText("title");
		String  uuid       = rec.getChildText("uuid");
        String  isHarvested = rec.getChildText("isharvested");
        String  isLocked = rec.getChildText("islocked");
        String  lockedBy = rec.getChildText("lockedby");
        String  harvestUuid= rec.getChildText("harvestuuid");
		String  popularity = rec.getChildText("popularity");
		String  rating     = rec.getChildText("rating");
		String  owner      = rec.getChildText("owner");
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
        addElement(info, Edit.Info.Elem.IS_HARVESTED, isHarvested);
        addElement(info, Edit.Info.Elem.IS_LOCKED, isLocked);
        addElement(info, Edit.Info.Elem.LOCKED_BY, lockedBy);
        addElement(info, Edit.Info.Elem.POPULARITY,  popularity);
		addElement(info, Edit.Info.Elem.RATING,      rating);
                addElement(info, Edit.Info.Elem.DISPLAY_ORDER,  displayOrder);

		if (isHarvested.equals("y"))
			info.addContent(harvestMan.getHarvestInfo(harvestUuid, id, uuid));

		if (version != null)
			addElement(info, Edit.Info.Elem.VERSION, version);

        String mdStatus = getCurrentStatus(dbms, id);
        if(StringUtils.isEmpty(mdStatus)) {
            mdStatus = Params.Status.UNKNOWN;
        }
        addElement(info, Edit.Info.Elem.STATUS, mdStatus);
        String mdStatusName = getCurrentStatusName(dbms, mdStatus);
        addElement(info, Edit.Info.Elem.STATUS_NAME, mdStatusName);

		buildExtraMetadataInfo(context, id, info);

        if(accessMan.isVisibleToAll(dbms, id)) {
            addElement(info, Edit.Info.Elem.IS_PUBLISHED_TO_ALL, "true");
        }
        else {
            addElement(info, Edit.Info.Elem.IS_PUBLISHED_TO_ALL, "false");
        }

        // add owner id
        addElement(info, Edit.Info.Elem.OWNER_ID, owner);

        // add owner name
		query = "SELECT username FROM Users WHERE id = ?";
		Element record = dbms.select(query, owner).getChild("record");
		if (record != null) {
			String ownerName = record.getChildText("username");
            addElement(info, Edit.Info.Elem.OWNERNAME, ownerName);
        }

		// add categories
		List categories = dbms.select("SELECT id, name FROM MetadataCateg, Categories "+
												"WHERE metadataId = ? AND categoryId = id ORDER BY id", id).getChildren();

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
        if(CollectionUtils.isNotEmpty(iso639_1_set)) {
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
	public void buildExtraMetadataInfo(ServiceContext context, String id, Element info) throws Exception {
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

        if(settingMan.getValueAsBool("system/symbolicLocking/enable")) {
            addElement(info, Edit.Info.Elem.SYMBOLIC_LOCKING, "enabled");
        }
        String userProfile = context.getUserSession().getProfile();
        addElement(info, Edit.Info.Elem.USER_PROFILE, userProfile);

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
	private void setNamespacePrefixUsingSchemas(String schema, Element md) throws Exception {
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
	//***public void setGroupOwner(Dbms dbms, String mdId, String grpId) throws Exception {
	//	dbms.execute("UPDATE Metadata SET groupOwner=? WHERE id=?", grpId, mdId);
	//}

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
        Dbms dbms = null;

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
        try {
       	    dbms = (Dbms) srvContext.getResourceManager().openDirect(Geonet.Res.MAIN_DB);
            String query = "UPDATE Metadata SET popularity = popularity +1 WHERE id = ?";
            dbms.execute(query, id);
            boolean indexGroup = false;
            boolean workspace = false;
            indexMetadata(dbms, id, indexGroup, workspace, true);
        }
        catch (Exception e) {
            Log.warning(Geonet.DATA_MANAGER, "The following exception is ignored: " + e.getMessage());
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
}
