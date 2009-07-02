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
 * Provide single API for communicating with a GN server.
 * <p/>
 * The ServerMediator provides a standard API that the protocol
 * test framework can use to send (dispatch) requests and receive
 * responses. For now only a "local Jeeves" version is available, but
 * this class can easily be extended to e.g. provide a HTTP client to
 * communicate with a real remote GN server.
 *
 * @author Just van den Broecke - just@justobjects.nl
 */
public class ServerMediator {
	// TODO get credentials from config.xml
	private static final String MCKOI_USER = "geonetwork";
	private static final String MCKOI_PASSWORD = "geonetwork";
	private static String ERR = "none";

	/**
	 * Dispatch to server using local Jeeves service request.
	 *
	 * @param aRequest a server request
	 * @return a server response element
	 */
	static public Element dispatch(Element aRequest) {
		return LocalJeeves.dispatch(aRequest);
	}

	static public String getError() {
		return ERR;
	}

	static public void init() throws Exception {
		// Create a new/fresh DB
		try {
			dbCreate();
		} catch (Throwable t) {
			ERR = "dbCreate err: " + t;
			throw new Exception("Error creating DB");
		}

		// Setup with GN standard data
		try {
			dbFill();
		} catch (Throwable t) {
			ERR = "dbFill err: " + t;
			throw new Exception("Error filling DB");
		}

		// Initialize Jeeves engine locally
		LocalJeeves.init(TestConfig.getAppPath(), TestConfig.getConfigPath(), TestConfig.getBaseUrl());
	}

	static public boolean isReady() {
		return LocalJeeves.isRunning();
	}

	static private void dbCreate() throws Exception {
		// Remove all McKoi files
		Lib.io.cleanDir(new File(TestConfig.getDBPath()));

		// Create database files
		McKoiDB mcKoi = new McKoiDB();
		mcKoi.setConfigFile(TestConfig.getConfigPath() + "mckoi.conf");
		mcKoi.create(MCKOI_USER, MCKOI_PASSWORD);
	}

	static private void dbFill() throws Exception {
		// Open a database connection and setup data.
		Document config = new XMLLib().load(TestConfig.getConfigPath() + "config.xml");
		Element dbmsElem = retrieveDbms(config);

		Resource resource = new Resource(TestConfig.getAppPath(), dbmsElem);
		DatabaseLib database = new DatabaseLib(TestConfig.getHomePath());

		// TODO: this also wipes Lucene index (I think) in web/geonetwork/WEB-INF !!
		Lib.init(TestConfig.getHomePath());
		database.setup(resource, null);
	}

	static private Element retrieveDbms(Document config) {
		Element resources = config.getRootElement().getChild(ConfigFile.Child.RESOURCES);

		for (Object res : resources.getChildren(ConfigFile.Resources.Child.RESOURCE)) {
			Element resource = (Element) res;
			String enabled = resource.getAttributeValue("enabled");

			if ("true".equals(enabled))
				return resource;
		}

		//--- we should not arrive here

		return null;
	}

}
