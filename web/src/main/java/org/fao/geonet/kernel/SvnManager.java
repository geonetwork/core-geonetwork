//=============================================================================
//===	Copyright (C) 2001-2011 Food and Agriculture Organization of the
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

import jeeves.resources.dbms.Dbms;
import jeeves.server.context.ServiceContext;
import jeeves.server.resources.ProviderManager;
import jeeves.server.resources.ResourceListener;
import jeeves.server.resources.ResourceProvider;
import jeeves.server.UserSession;
import jeeves.utils.Log;
import jeeves.utils.Xml;

import org.apache.commons.lang.StringUtils;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.constants.Params;
import org.fao.geonet.kernel.setting.SettingManager;

import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager;
import org.tmatesoft.svn.core.io.ISVNEditor;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.io.SVNRepositoryFactory;
import org.tmatesoft.svn.core.wc.SVNWCUtil;
import org.tmatesoft.svn.core.SVNCommitInfo;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNNodeKind;
import org.tmatesoft.svn.core.SVNProperties;
import org.tmatesoft.svn.core.SVNURL;

import org.jdom.Element;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class SvnManager {

	private ServiceContext context;
	private SVNURL repoUrl;

	private static String username = "geonetwork";
	private static String password = "geonetwork";

	private Map<Dbms, SvnTask> tasks = new ConcurrentHashMap<Dbms, SvnTask>();

	private ResourceListener resList = new ResourceListener() {
		public void beforeClose(Object resource) { commit(resource); }
		public void close(Object resource)       {} // do nothing on commit
		public void abort(Object resource)       { rollback(resource); }
	};

	// SvnTask holds information used to commit changes to a metadata record
	private class SvnTask {
		private Set<String> ids; 					// metadata ids
		private String sessionLogMessage; // session log message for svn commit
		private Map<String,String> props; // properties to set on metadata record
	}
    /**
     *  Constructor. Creates the subversion repository if it doesn't exist
		 *  or just open the subversion repository if it does. Stores the URL
		 *  of the repository in repoUrl. Adds the commit/abort listeners to 
		 *  the DbmsPool resource provider.
     *
		 * @param context Service context used to get GeoNetwork context objects
		 * @param sm SettingManager used to get system settings like catalog id 
     * @param subversionPath File path of the subversion repository.
     * @param dbms Database used to find resource provider
     * @param created set to true if a new database has been created
     */
	public SvnManager(ServiceContext context, SettingManager sm, String subversionPath, Dbms dbms, boolean created) throws Exception {

        String dbUrl = dbms.getURL();
		this.context = context;
		String uuid = sm.getValue("system/site/svnUuid");

        if (StringUtils.isEmpty(uuid)) {
            uuid = UUID.randomUUID().toString();
            sm.setValue(dbms, "system/site/svnUuid", uuid);
        }

    File subFile = new File(subversionPath);
		boolean repoCreated = false;

    // if subversion repo doesn't exist then create it with siteId 
    try {
			boolean enableRevisionProperties = true;
			boolean force = false;
      repoUrl = SVNRepositoryFactory.createLocalRepository(subFile, uuid, enableRevisionProperties, force);
			repoCreated = true;
    } catch (SVNException e) {

      if (subFile.exists()) { // set the repoUrl and try and open it
        repoUrl = SVNURL.fromFile(subFile);

      } else {
      	e.printStackTrace();
				throw new IllegalArgumentException("Problem creating or using repository at path "+subversionPath);
			}
    }

		// open the repository now
		SVNRepository repo = null;
		try {
			repo = getRepository();
    } catch (SVNException se) {
      Log.error(Geonet.SVN_MANAGER, "Failed to open subversion repo at path "+subversionPath);
			se.printStackTrace();
			throw se;
    }

		// do some checks on existing repo and recreate it if empty
		if (!repoCreated) {
			String repoUuid = repo.getRepositoryUUID(true);
			long latestRev = repo.getLatestRevision();
			// check that repository has something in it
			if (latestRev > 1) {
				// check that repository uuid matches the repo uuid in database
				if (!uuid.equals(repoUuid)) {
					throw new IllegalArgumentException("Subversion repository at "+subversionPath+" has uuid "+repoUuid+" which does not match repository uuid held in database "+uuid);
				}
			} else {
			// if nothing in repo and it doesn't have the uuid we expect then 
			// recreate it and reopen it
				if (!uuid.equals(repoUuid)) {
    			Log.warning(Geonet.SVN_MANAGER, "Recreating subversion repository at "+subversionPath+" as previous repository was empty");
					boolean enableRevisionProperties = true;
					boolean force = true;
      		repoUrl = SVNRepositoryFactory.createLocalRepository(subFile, uuid, enableRevisionProperties, force);
					repoCreated = true;
					repo = getRepository();
				}
			}
		}

		// set database URL as property on root of repository if database was
		// created or if new subversion repo created
		if (created || repoCreated) {
			Map<String,String> props = new HashMap<String,String>();
			props.put(Params.Svn.DBURLPROP, dbUrl);
   		ISVNEditor editor = getEditor("Setting "+Params.Svn.DBURLPROP+" to "+dbUrl);
			editor.openRoot(-1);
			SvnUtils.modifyFileProps(editor, "/", props);
			editor.closeDir();
			SVNCommitInfo info = editor.closeEdit();
            if(Log.isDebugEnabled(Geonet.SVN_MANAGER))
                Log.debug(Geonet.SVN_MANAGER, "Committed "+dbUrl+" as property "+Params.Svn.DBURLPROP+":"+info);
		} else {
		// get database URL from root of repository and check against dbUrl
		// if it doesn't match then stop
			SVNProperties rootProps = new SVNProperties();
			Collection nullColl = null;
			repo.getDir("/", -1, rootProps, nullColl);
			String repoDbUrl = rootProps.getStringValue(Params.Svn.DBURLPROP).trim();
			if (repoDbUrl == null || (!dbUrl.trim().equals(repoDbUrl))) {
				throw new IllegalArgumentException("Repository uses database URL of '"+repoDbUrl+"' which does not match current database URL '"+dbUrl+"'. Modify the svn property "+Params.Svn.DBURLPROP+" on the root of the subversion repository at "+subversionPath+" or specify a different subversion repository");
			}
		}

		// now add the listener to the DbmsPool resource provider
		ProviderManager provMan = context.getProviderManager();
		for(ResourceProvider rp : provMan.getProviders()) {
			if (rp.getName().equals(dbUrl)) {
				rp.addListener(resList);
			}
		}
  }

    /**
     * Create a string from the user information in the UserSession session.
     *
		 * @param context Context describing the user and service
		 * @return The string representing the user information in the session
     */
	private String sessionToLogMessage(ServiceContext context) {
		UserSession session = context.getUserSession();
		if (session == null) session = getDefaultSession();
		String result = "GeoNetwork service: "+context.getService()+" GeoNetwork User "+session.getUserIdAsInt()+" (Username: "+session.getUsername()+" Name: "+session.getName()+" "+session.getSurname()+") Executed from IP address "+context.getIpAddress();
		return result;
	}

    /**
     * Create a map of the user information in the UserSession session.
     *
		 * @param context Context describing the user and service
		 * @param props The map of properties to add the session props too
		 * @return The map of properties with the session props added
     */
	private Map<String,String> sessionToProps(ServiceContext context, Map<String,String> props) {
		UserSession session = context.getUserSession();
		if (session == null) session = getDefaultSession();
		props.put(Params.Svn.OPERATOR, session.getUserId());
		props.put(Params.Svn.USERNAME, session.getUsername());
		props.put(Params.Svn.NAME,     session.getName());
		props.put(Params.Svn.SURNAME,  session.getSurname());
		props.put(Params.Svn.SERVICE,  context.getService());
		props.put(Params.Svn.IPADDR,   context.getIpAddress());
		return props;
	}

    /**
     * Create a blank user session object in cases where the context doesn't
		 * have one.
     *
		 * @return The blank user session object
     */
	private UserSession getDefaultSession() {
		UserSession session = new UserSession();
		return session;
	}

    /**
     * Creates a history request for this metadata id that will cause 
		 * metadata and metadata properties to be committed/aborted when the 
		 * database is committed/aborted.
     *
		 * @param dbms The database channel to listen for commit/abort on
		 * @param id The metadata id that will be tracked
		 * @param context Describing the servicer and user carrying out operation
     * @throws Exception when something goes wrong
     */
	public void setHistory(Dbms dbms, String id, ServiceContext context) throws Exception {

		if (!exists(id)) return; // not in repo so exit

        if(Log.isDebugEnabled(Geonet.SVN_MANAGER))
            Log.debug(Geonet.SVN_MANAGER, "History will be recorded on metadata "+id);

    Map<String,String> props = new HashMap<String,String>();
		props = sessionToProps(context, props);

		checkSvnTask(dbms, id, sessionToLogMessage(context), props);

        if(Log.isDebugEnabled(Geonet.SVN_MANAGER))
            Log.debug(Geonet.SVN_MANAGER, "Changes to metadata "+id+" will be committed/aborted with the database channel "+dbms);
		return;
	}	

	/** 
	 * Get the latest file from the directory of the subversion repository
   * corresponding to this metadata id.
	 *
   * @param id Id of the metadata record in the subversion repo
   * @param file File to retrieve from the subversion repo - could be metadata.xml, owner.xml, privileges.xml, status.xml, categories.xml, ...
   * @return Element XML metadata
	 */
	private Element getFile(String id, String file) throws Exception {

		if (!exists(id)) return null; // not in repo so exit

    String filePath = id + "/" + file;
    SVNRepository repo = getRepository();
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    repo.getFile(filePath, -1, new SVNProperties(), baos);

    Element rec = Xml.loadString(baos.toString(), false);
    return (Element) rec.detach();
  }

    /**
     * Creates a directory to hold metadata and property histories and add
		 * the metadata specified as the first version.
     *
		 * @param id The metadata id that will be tracked
		 * @param context Service context describing user and operation
		 * @param md Metadata record - initial version 
     * @throws Exception
     */
	public void createMetadataDir(String id, ServiceContext context, Element md) throws Exception {

		if (exists(id)) return; // already in repo so exit

    String logMessage = sessionToLogMessage(context)+" adding directory for metadata "+id;
        if(Log.isDebugEnabled(Geonet.SVN_MANAGER))
            Log.debug(Geonet.SVN_MANAGER, logMessage);

    ISVNEditor editor = getEditor(logMessage);

    try {
    	// Create an id/ directory item in the repository
			editor.openRoot(-1);
      SvnUtils.addDir(editor, id);
        if(Log.isDebugEnabled(Geonet.SVN_MANAGER))
            Log.debug(Geonet.SVN_MANAGER, "Directory for metadata "+id+" was added");
      editor.closeDir();
      editor.closeEdit();
    } catch (SVNException svne) {
      editor.abortEdit();
      svne.printStackTrace();
      throw svne;
    }


		// Add the id/metadata.xml item plus properties to the repository
		Dbms dbms = (Dbms) context.getResourceManager().openDirect(Geonet.Res.MAIN_DB);
		try {

    	logMessage = sessionToLogMessage(context)+" adding initial version of metadata "+id;
    	editor = getEditor(logMessage);
			editor.openRoot(-1);
			commitMetadata(dbms, id, editor);
      if(Log.isDebugEnabled(Geonet.SVN_MANAGER))
        Log.debug(Geonet.SVN_MANAGER, "Metadata "+id+" was added");
      editor.closeDir();
      SVNCommitInfo commitInfo = editor.closeEdit();
      if(Log.isDebugEnabled(Geonet.SVN_MANAGER))
        Log.debug(Geonet.SVN_MANAGER, "Commit returned "+commitInfo);
    } catch (SVNException svne) {
      editor.abortEdit();
      svne.printStackTrace();
      throw svne;
    } finally {
			context.getResourceManager().close(Geonet.Res.MAIN_DB, dbms);
		}
	}	

		/**
     * Deletes a metadata directory from the subversion repository immediately.
     *
		 * @param id The metadata id that will be removed
		 * @param context Service context describing the user and operation 
     * @throws Exception when something goes wrong
     */
	public void deleteDir(String id, ServiceContext context) throws Exception {
		
		if (!exists(id)) return; // not in repo so exit

    String logMessage = sessionToLogMessage(context)+" deleting directory for metadata "+id;
        if(Log.isDebugEnabled(Geonet.SVN_MANAGER))
            Log.debug(Geonet.SVN_MANAGER, logMessage);

		ISVNEditor editor = getEditor(logMessage);

    try {
      SvnUtils.deleteDir(editor, id);
			SVNCommitInfo commitInfo = editor.closeEdit();
        if(Log.isDebugEnabled(Geonet.SVN_MANAGER))
            Log.debug(Geonet.SVN_MANAGER, "Directory for metadata "+id+" deleted: " + commitInfo);
    } catch (SVNException svne) {
      editor.abortEdit(); // abort the update on the XML in the repository
      svne.printStackTrace();
      throw svne;
    }
	}

		/**
     * Abort changes to the subversion repository. Actually just remove any
		 * task assigned to this dbms resource.
     *
		 * @param resource The resource being aborted that we're listening too
     */
	private void rollback(Object resource) {
		Dbms dbms = (Dbms)resource;

		SvnTask task = tasks.get(dbms);
		if (task != null) tasks.remove(dbms);
	}

		/**
     * Commit changes to the subversion repository.
     *
		 * @param resource The resource being committed that we're listening too
     */
	private void commit(Object resource) {
		Dbms dbms = (Dbms)resource;

		SvnTask task = tasks.get(dbms);

		ISVNEditor editor = null;

		if (task != null) {
			try {
				editor = getEditor(task.sessionLogMessage+" (committing dbms session "+dbms+")");
				editor.openRoot(-1); // open the root directory.
				for (Iterator<String> it = task.ids.iterator(); it.hasNext();) {
					String id = it.next();
					commitMetadata(dbms, id, editor);
					it.remove();
				}
				editor.closeDir(); // close the root directory.
				SVNCommitInfo commitInfo = editor.closeEdit();
                if(Log.isDebugEnabled(Geonet.SVN_MANAGER))
                    Log.debug(Geonet.SVN_MANAGER, "Committed changes to subversion repository for metadata ids "+task.ids);
			} catch (Exception e) {
       	        Log.error(Geonet.SVN_MANAGER, "Failed to commit changes to subversion repository for metadata ids "+task.ids);
				e.printStackTrace();
				if (editor != null) {
					try {
						editor.abortEdit();
					} catch (Exception ex) {
       			Log.error(Geonet.SVN_MANAGER, "Failed to abort subversion editor");
						ex.printStackTrace();
					}
				}
			} finally {
				tasks.remove(dbms);
			}
		}
	}	

		/**
     * Check (and create if not present) a subversion commit task for a 
		 * metadata id. If a commit task exists then the metadata and properties
     * will be read at the end of the database commit and committed to the
     * subversion repo.
     *
		 * @param dbms The resource we will listen to for commits/aborts
		 * @param id The metadata id we want to track
		 * @param sessionLogMessage The log message used to record changes
		 * @param props The properties we want to record on the metadata in repo
     * @throws Exception when something goes wrong with the commit
     */
	private void checkSvnTask(Dbms dbms, String id, String sessionLogMessage, Map<String,String> props) throws Exception {
		SvnTask task = tasks.get(dbms);
		if (task == null) {
			task = new SvnTask();
			task.ids = new HashSet<String>();
			task.sessionLogMessage = sessionLogMessage;
			task.props = props;
			tasks.put(dbms, task);
		}

		Set<String> ids = task.ids;
		if (!ids.contains(id)) {
			ids.add(id);
		}
	}

		/**
     * Get an SVNRepository object for use by this class.
     *
     * @return SVNRepository object
     */
	private SVNRepository getNewRepository() throws SVNException {
		SVNRepository repository = SVNRepositoryFactory.create(repoUrl);
		if (username != null) {
			ISVNAuthenticationManager authManager = SVNWCUtil.createDefaultAuthenticationManager(new File(repoUrl.getPath()), username, password);
			repository.setAuthenticationManager(authManager);
		}
		return repository;
	}

    /**
     * Returns an ISVNEditor that allows commits to occur on the repository.
     *
     * @param logMessage The Log message that will be used on commit success
     * @return ISVNEditor object for operations on the repository
		 * @throws SVNException if something goes wrong
     */
	public ISVNEditor getEditor(String logMessage) throws SVNException {

		SVNRepository repository = getNewRepository();
		ISVNEditor editor = repository.getCommitEditor(logMessage,
						                      null  /*locks*/,
																	false /*keepLocks*/,
																	null  /*mediator*/);
		return editor;
	}	

    /**
     * Returns an SVNRepository.
     *
		 * @throws SVNException if something goes wrong
     */
	public SVNRepository getRepository() throws SVNException {
		return getNewRepository();
	}

    /**
     * Commits an ISVNEditor with changes to the repository.
     *
     * @param id Id number of metadata record being tracked for changes
     * @param editor ISVNEditor for commits to subversion repo
		 * @throws SVNException if something goes wrong
     */
	public void commitMetadata(Dbms dbms, String id, ISVNEditor editor) throws Exception {

		GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
		DataManager dataMan = gc.getDataManager();
		AccessManager accessMan = gc.getAccessManager();

		try {
			// get the metadata record and if different commit changes
			commitMetadataChanges(editor, id, dbms, dataMan);

			// get the metadata owner and if different commit changes
			commitMetadataOwner(editor, id, dbms, accessMan);

			// get the metadata privileges and if different commit changes
			commitMetadataPrivileges(editor, id, dbms);

			// get the metadata categories and if different commit changes
			commitMetadataCategories(editor, id, dbms, dataMan);

			// get the metadata status and if different commit changes
			commitMetadataStatus(editor, id, dbms, dataMan);
    } catch (Exception e) {
      editor.abortEdit();
      e.printStackTrace();
      throw e;
		}
	}	

    /**
     * Commits changes to metadata categories.
     *
     * @param editor ISVNEditor for commits to subversion repo
     * @param id Id number of metadata record being tracked for changes
     * @param dbms Database channel used to extract info from database 
     * @param dataMan DataManager object with extract methods 
		 * @throws Exception if something goes wrong
     */
	private void commitMetadataCategories(ISVNEditor editor, String id, Dbms dbms, DataManager dataMan) throws Exception {

		// get categories from the database
		Element categs = dataMan.getCategories(dbms, id);
		String now = Xml.getString(categs);

		if (exists(id+"/categories.xml")) {
			// Update the id/categories.xml item in the repository
			Element categsPrevVersion = getFile(id, "categories.xml");
			String old = Xml.getString(categsPrevVersion);
			if (!old.equals(now)) {
    		SvnUtils.modifyFile(editor, 
												id+"/categories.xml", 
												old.getBytes(), 
												now.getBytes());
            if(Log.isDebugEnabled(Geonet.SVN_MANAGER))
                Log.debug(Geonet.SVN_MANAGER, "Categories of metadata "+id+" updated");
			}
		} else {
			// Add the id/owner.xml item to the repository
    	SvnUtils.addFile(editor, 
												id+"/categories.xml", 
												now.getBytes());
            if(Log.isDebugEnabled(Geonet.SVN_MANAGER))
                Log.debug(Geonet.SVN_MANAGER, "Categories of metadata "+id+" added");

		}
	}

    /**
     * Commits changes to metadata status.
     *
     * @param editor ISVNEditor for commits to subversion repo
     * @param id Id number of metadata record being tracked for changes
     * @param dbms Database channel used to extract info from database 
     * @param dataMan DataManager object with extract methods 
		 * @throws Exception if something goes wrong
     */
	private void commitMetadataStatus(ISVNEditor editor, String id, Dbms dbms, DataManager dataMan) throws Exception {

		// get current status from the database
		Element status = dataMan.getStatus(dbms, new Integer(id));
		if (status == null) return;
		List<Element> statusKids = status.getChildren();
		if (statusKids.size() == 0) return;
		status = (Element)status.getChildren().get(0);
		String now = Xml.getString(status);

		if (exists(id+"/status.xml")) {
			// Update the id/status.xml item in the repository
			Element statusPrevVersion = getFile(id, "status.xml");
			String old = Xml.getString(statusPrevVersion);
			if (!old.equals(now)) {

    		SvnUtils.modifyFile(editor, 
												id+"/status.xml", 
												old.getBytes(), 
												now.getBytes());
            if(Log.isDebugEnabled(Geonet.SVN_MANAGER))
                Log.debug(Geonet.SVN_MANAGER, "Status of metadata "+id+" was updated");
			}
		} else {
			// Add the id/owner.xml item to the repository
    	SvnUtils.addFile(editor, 
												id+"/status.xml", 
												now.getBytes());
            if(Log.isDebugEnabled(Geonet.SVN_MANAGER))
                Log.debug(Geonet.SVN_MANAGER, "Status of metadata "+id+" was added");

		}
	}

    /**
     * Commits changes to metadata record.
     *
     * @param editor ISVNEditor for commits to subversion repo
     * @param id Id number of metadata record being tracked for changes
     * @param dataMan DataManager object with extract methods 
		 * @throws Exception if something goes wrong
     */
	private void commitMetadataChanges(ISVNEditor editor, String id, Dbms dbms, DataManager dataMan) throws Exception {
		// get metadata record from database
		Element md = dataMan.getMetadata(dbms, id);
		String now = Xml.getString(md);

		if (exists(id+"/metadata.xml")) {
			Element mdPrevVersion = getFile(id, "metadata.xml");
			String old = Xml.getString(mdPrevVersion);
			if (!old.equals(now)) {

				// Update the id/metadata.xml item in the repository
    		SvnUtils.modifyFile(editor, 
												id+"/metadata.xml", 
												old.getBytes(), 
												now.getBytes());

            if(Log.isDebugEnabled(Geonet.SVN_MANAGER))
                Log.debug(Geonet.SVN_MANAGER, "Metadata "+id+" was updated");
			}
		} else {
    	SvnUtils.addFile(editor,
												id+"/metadata.xml", 
												now.getBytes());
		}
	}

    /**
     * Commits changes to metadata owners.
     *
     * @param editor ISVNEditor for commits to subversion repo
     * @param id Id number of metadata record being tracked for changes
     * @param dbms Database channel used to extract info from database 
     * @param accessMan AccessManager object with extract methods 
		 * @throws Exception if something goes wrong
     */
	private void commitMetadataOwner(ISVNEditor editor, String id, Dbms dbms, AccessManager accessMan) throws Exception {

		// get owner from the database
		Set<Integer> ids = new HashSet<Integer>();
		ids.add(new Integer(id));
		Element owner = accessMan.getOwners(dbms, ids);
		String now = Xml.getString(owner);

		if (exists(id+"/owner.xml")) {
			// Update the id/owner.xml item in the repository
			Element ownerPrevVersion = getFile(id, "owner.xml");
			String old = Xml.getString(ownerPrevVersion);
			if (!old.equals(now)) {

    		SvnUtils.modifyFile(editor, 
												id+"/owner.xml", 
												old.getBytes(), 
												now.getBytes());
            if(Log.isDebugEnabled(Geonet.SVN_MANAGER))
                Log.debug(Geonet.SVN_MANAGER, "Ownership of metadata "+id+" was updated");
			}
		} else {
			// Add the id/owner.xml item to the repository
    	SvnUtils.addFile(editor, 
												id+"/owner.xml", 
												now.getBytes());
            if(Log.isDebugEnabled(Geonet.SVN_MANAGER))
                Log.debug(Geonet.SVN_MANAGER, "Ownership of metadata "+id+" was added");

		}
	}

    /**
     * Commits changes to metadata privileges.
     *
     * @param editor ISVNEditor for commits to subversion repo
     * @param id Id number of metadata record being tracked for changes
     * @param dbms Database channel used to extract info from database 
		 * @throws Exception if something goes wrong
     */
	private void commitMetadataPrivileges(ISVNEditor editor, String id, Dbms dbms) throws Exception {
    StringBuffer query = new StringBuffer();

    query.append("SELECT g.id as group_id, g.name as group_name,          ");
    query.append("       o.id as operation_id, o.name as operation_name   ");
		query.append("FROM groups g                                           ");
		query.append("JOIN OperationAllowed oa on oa.groupId = g.id           ");
		query.append("JOIN Operations o on o.id = oa.operationId              ");
    query.append("WHERE oa.metadataId = ?                                 ");
    query.append("ORDER BY o.id                                           ");

    Element privs = dbms.select(query.toString(), new Integer(id));	
		String now = Xml.getString(privs);

		if (exists(id+"/privileges.xml")) {
			// Update the id/privileges.xml item in the repository
			Element privsPrevVersion = getFile(id, "privileges.xml");
			String old = Xml.getString(privsPrevVersion);
			if (!old.equals(now)) {

    		SvnUtils.modifyFile(editor, 
												id+"/privileges.xml", 
												old.getBytes(), 
												now.getBytes());

            if(Log.isDebugEnabled(Geonet.SVN_MANAGER))
                Log.debug(Geonet.SVN_MANAGER, "Privileges of metadata "+id+" were updated");
			}
		} else {
			// Add the id/owner.xml item to the repository
    	SvnUtils.addFile(editor, 
												id+"/privileges.xml", 
												now.getBytes());
            if(Log.isDebugEnabled(Geonet.SVN_MANAGER))
                Log.debug(Geonet.SVN_MANAGER, "Privileges of metadata "+id+" were added");
		}
	}	

    /**
     *  Check if the directory/file exists in the subversion repository.
     *
     * @param filePath The file path from root. eg. 10/metadata.xml.
     * @return true if exists, false otherwise
     */
	private boolean exists(String filePath) throws SVNException {
    SVNRepository repo = getRepository();
    SVNNodeKind nodeKind = repo.checkPath(filePath, -1);
    if (nodeKind == SVNNodeKind.FILE || nodeKind == SVNNodeKind.DIR) return true;
		else return false;
	}
}
