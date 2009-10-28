package org.fao.geonet.test;

import jeeves.constants.ConfigFile;
import jeeves.server.local.LocalJeeves;
import org.fao.gast.lib.DatabaseLib;
import org.fao.gast.lib.Lib;
import org.fao.gast.lib.Resource;
import org.fao.gast.lib.XMLLib;
import org.fao.geonet.util.McKoiDB;
import org.jdom.Document;
import org.jdom.Element;

import java.io.File;

/**
 * Provide API for communicating with a Local Jeeves GN server.
 * <p/>
 * This ServerMediator provides interaction with a Jeeves engine running within the same JVM
 * (a.k.a. "Local Jeeves").
 *
 * @author Just van den Broecke - just@justobjects.nl
 */
public class LocalJeevesMediator extends ServerMediator {
	private static Element dbConfigElm;

	/**
	 * Dispatch to server using local Jeeves service request.
	 *
	 * @param aRequest a server request
	 * @return a server response element
	 */
	protected Element doDispatch(Element aRequest) {
		return LocalJeeves.dispatch(aRequest);
	}

	/**
	 * Initializes McKOI DB and local jeeves engine .
	 */
	protected void doInit() throws Exception {
		// Open a database connection and setup data.
		Document config = new XMLLib().load(TestConfig.getConfigPath() + "config.xml");
		dbConfigElm = retrieveDbms(config);

		// Create a new/fresh DB
		try {
			dbCreate();
		} catch (Throwable t) {
			setError("dbCreate err: " + t);
			throw new Exception("Error creating DB", t);
		}

		// Setup with GN standard data
		try {
			dbFill();
		} catch (Throwable t) {
			setError("dbFill err: " + t);
			throw new Exception("Error filling DB", t);
		}

		// Initialize Jeeves engine locally
		LocalJeeves.init(TestConfig.getAppPath(), TestConfig.getConfigPath(), TestConfig.getBaseUrl());
	}

	protected boolean isRunning() {
		return LocalJeeves.isRunning();
	}

	private void dbCreate() throws Exception {
		Element configElm = dbConfigElm.getChild("config");
		String url = configElm.getChild("url").getText();

		// Only McKoi DB type needs explicit init
		// Other DBs like mysql we assume that they are already running
		if (url.indexOf("mckoi") != -1) {
			// Remove all McKoi files
			Lib.io.cleanDir(new File(TestConfig.getDBPath()));

			// Create database files
			McKoiDB mcKoi = new McKoiDB();
			mcKoi.setConfigFile(TestConfig.getConfigPath() + "mckoi.conf");
			mcKoi.create(configElm.getChild("user").getText(), configElm.getChild("password").getText());
		}
	}

	private void dbFill() throws Exception {
		Resource resource = new Resource(TestConfig.getAppPath(), dbConfigElm);
		DatabaseLib database = new DatabaseLib(TestConfig.getHomePath());

		// TODO: this also wipes Lucene index (I think) in web/geonetwork/WEB-INF !!
		Lib.init(TestConfig.getHomePath());
		database.setup(resource, null);
	}

	private Element retrieveDbms(Document config) {
		Element resources = config.getRootElement().getChild(ConfigFile.Child.RESOURCES);

		Element result = null;
		for (Object res : resources.getChildren(ConfigFile.Resources.Child.RESOURCE)) {
			Element resource = (Element) res;
			if ("true".equals(resource.getAttributeValue("enabled"))) {
				result = resource;
				break;
			}
		}

		// Return result (may be null)
		return result;
	}
}
