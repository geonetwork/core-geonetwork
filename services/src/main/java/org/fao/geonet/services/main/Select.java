package org.fao.geonet.services.main;

import jeeves.constants.Jeeves;
import jeeves.interfaces.Service;
import jeeves.server.ServiceConfig;
import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.Util;
import org.fao.geonet.constants.Params;
import org.fao.geonet.kernel.SelectionManager;
import org.jdom.Element;

/**
 * Select a list of elements stored in session
 * Returns status 
 */

public class Select implements Service {
	
	String init_type;
	
	public void init(String appPath, ServiceConfig params) throws Exception {
		init_type = params.getValue(Params.TYPE);
	}

	// --------------------------------------------------------------------------
	// ---
	// --- Service
	// ---
	// --------------------------------------------------------------------------

	public Element exec(Element params, ServiceContext context)
			throws Exception {
		
		String type = Util.getParam(params, Params.TYPE, init_type );
		
		// Get the selection manager
		UserSession session = context.getUserSession();
		int nbSelected = SelectionManager.updateSelection(type, session, params, context);
		
		// send ok
		Element response = new Element(Jeeves.Elem.RESPONSE);
		response.addContent(new Element("Selected").setText(""+nbSelected));		

		return response;
	}
}

// =============================================================================

