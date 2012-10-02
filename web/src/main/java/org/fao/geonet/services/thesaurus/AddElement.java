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

import jeeves.constants.Jeeves;
import jeeves.interfaces.Service;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import jeeves.utils.Util;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.constants.Params;
import org.fao.geonet.kernel.KeywordBean;
import org.fao.geonet.kernel.Thesaurus;
import org.fao.geonet.kernel.ThesaurusManager;
import org.jdom.Element;

//=============================================================================

/**
 * For editing : adds a tag to a thesaurus. Access is restricted
 */

public class AddElement implements Service {
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

		String ref = Util.getParam(params, Params.REF);
		String namespace = Util.getParam(params, "namespace", "");
		String newid = Util.getParam(params, "newid");
		String thesaType = Util.getParam(params, "refType");
		String prefLab = Util.getParam(params, "prefLab");
		String lang = Util.getParam(params, "lang");
		String definition = Util.getParam(params, "definition","");

		Element elResp = new Element(Jeeves.Elem.RESPONSE);

		ThesaurusManager manager = gc.getThesaurusManager();
		Thesaurus thesaurus = manager.getThesaurusByName(ref);

		if (thesaurus.isFreeCode(namespace, newid)) {

		    KeywordBean keyword = new KeywordBean()
	            .setNamespaceCode(namespace)
	            .setRelativeCode(newid)
	            .setValue(prefLab, lang)
            	.setDefinition(definition, lang);

			if (thesaType.equals("place")) {
				String east = Util.getParam(params, "east");
				String west = Util.getParam(params, "west");
				String south = Util.getParam(params, "south");
				String north = Util.getParam(params, "north");
				keyword.setCoordEast(east)
                    .setCoordNorth(north)
                    .setCoordSouth(south)
                    .setCoordWest(west);
			}

			thesaurus.addElement(keyword);

			elResp.addContent(new Element("selected").setText(ref));
			elResp.addContent(new Element("mode").setText("edit"));
		} else {
			elResp.addContent(new Element("error").addContent(new Element("message").setText("Code value already exists in thesaurus")));
		}

		return elResp;
	}
}

// =============================================================================

