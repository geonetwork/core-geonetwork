/*
 * Copyright (C) 2001-2016 Food and Agriculture Organization of the
 * United Nations (FAO-UN), United Nations World Food Programme (WFP)
 * and United Nations Environment Programme (UNEP)
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or (at
 * your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
 *
 * Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
 * Rome - Italy. email: geonetwork@osgeo.org
 */

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

import java.nio.file.Path;

/**
 * Select a list of elements stored in session
 * Returns status 
 */

public class Select implements Service {
	
	String init_type;
	
	public void init(Path appPath, ServiceConfig params) throws Exception {
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

