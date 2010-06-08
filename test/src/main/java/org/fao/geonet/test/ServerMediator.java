package org.fao.geonet.test;

import org.jdom.Element;
import org.jdom.Document;
import org.jdom.xpath.XPath;
import org.fao.gast.lib.XMLLib;

import java.util.List;

/**
 * Provide single API for communicating with a GN server.
 * <p/>
 * The ServerMediator provides a standard API that the protocol
 * test framework can use to send (dispatch) requests and receive
 * responses. For example a "local Jeeves" version LocalJeevesMediator is available, but
 * this class can easily be extended to e.g. provide a HTTP client to
 * communicate with a real remote GN server, or basically any HTTP server that
 * supports XML webservices.
 *
 * @author Just van den Broecke - just@justobjects.nl
 */
public abstract class ServerMediator {
	static private String error = "none";
	static private Element configElm;

	private static ServerMediator serverMediator;

	/**
	 * Forward to actual Server Mediator.
	 *
	 * @param aRequest a server request
	 * @return a server response element
	 */
	static public Element dispatch(Element aRequest) {
		return serverMediator.doDispatch(aRequest);
	}

	/**
	 * Return Servermediator XML config element.
	 *
	 * @return a config element
	 */
	static public Element getConfigElm() {
		return configElm;
	}

	static public String getError() {
		return error;
	}

	/**
	 * Return specific configuration parameter.
	 *
	 * @return a config element
	 */
	static public String getConfigParam(String aName) throws Exception {
		XPath xPath = XPath.newInstance("//param[@name=\"" + aName + "\"]");
		Element paramElm = (Element) xPath.selectSingleNode(getConfigElm());
		if (paramElm == null) {
			throw new IllegalArgumentException("Cannot find config parameter: " + aName);
		}

		return paramElm.getText().trim();
	}

	/**
	 * Creates and initializes actual ServerMedaitor.
	 */
	static public void init() throws Exception {
		init(TestConfig.getConfigPath() + "/server-mediator.xml");
	}

	/**
	 * Creates and initializes actual ServerMedaitor.
	 */
	static public void init(String aMediatorConfigFile) throws Exception {
		// The ServerMediator config
		configElm = new XMLLib().load(aMediatorConfigFile).getRootElement();

		// Factory: create ServerMediator from class name in config
		String mediatorClassName = configElm.getChildText("class").trim();
		serverMediator = (ServerMediator) Class.forName(mediatorClassName).newInstance();

		// Initialize new instance
		serverMediator.doInit();
	}

	static public boolean isReady() {
		return serverMediator != null && serverMediator.isRunning();
	}

	static public void setError(String e) {
		error = e;
	}

	/**
	 * Dispatch to server using active ServerMediator.
	 *
	 * @param aRequest a server request
	 * @return a server response element
	 */
	abstract protected Element doDispatch(Element aRequest);

	/**
	 * Initialize actual ServerMediator.
	 */
	abstract protected void doInit() throws Exception;

	/**
	 * Check if actual ServerMediator is running.
	 *
	 * @return true if running
	 */
	abstract protected boolean isRunning();

}
