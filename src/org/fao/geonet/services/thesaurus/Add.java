//=============================================================================
//===	Copyright (C) 2001-2005 Food and Agriculture Organization of the
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
//===	Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
//===
//===	Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
//===	Rome - Italy. email: GeoNetwork@fao.org
//==============================================================================

package org.fao.geonet.services.thesaurus;

import java.io.File;

import jeeves.constants.Jeeves;
import jeeves.interfaces.Service;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import jeeves.utils.Util;

import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.Thesaurus;
import org.fao.geonet.kernel.ThesaurusManager;
import org.jdom.Element;

//=============================================================================

/**
 * For editing : adds a tag to a thesaurus. Access is restricted
 */

public class Add implements Service {
	public void init(String appPath, ServiceConfig params) throws Exception {
	}

	// --------------------------------------------------------------------------
	// ---
	// --- Service
	// ---
	// --------------------------------------------------------------------------

	public Element exec(Element params, ServiceContext context)
			throws Exception {
		GeonetContext gc = (GeonetContext) context
				.getHandlerContext(Geonet.CONTEXT_NAME);

		// Recuperation des parametres
		String fname = Util.getParam(params, "fname");
		String dname = Util.getParam(params, "dname");
		String type = Util.getParam(params, "type");

		fname = fname.trim();
		
		if (!fname.endsWith(".rdf")){
			fname = fname + ".rdf";
		}
		
		ThesaurusManager tm = gc.getThesaurusManager();

		String filePath = tm.buildThesaurusFilePath(fname, type, dname);
		
		File rdfFile = new File(filePath);		
		Thesaurus thesaurus = new Thesaurus(fname,type,dname,rdfFile);		
		tm.addThesaurus(thesaurus);

		Element elResp = new Element(Jeeves.Elem.RESPONSE);
		Element elRef = new Element("ref");		
		elRef.addContent(thesaurus.getKey());
		elResp.addContent(elRef);
		Element elName = new Element("thesaName").setText(fname);
		elResp.addContent(elName);
		
		return elResp;
	}
}

// =============================================================================

