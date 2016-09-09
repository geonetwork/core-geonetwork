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

import org.fao.geonet.Util;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.constants.Params;
import org.fao.geonet.domain.Constants;
import org.fao.geonet.domain.ThesaurusActivation;
import org.fao.geonet.kernel.Thesaurus;
import org.fao.geonet.kernel.ThesaurusManager;
import org.fao.geonet.repository.ThesaurusActivationRepository;
import org.jdom.Element;

import java.nio.file.Path;


/**
 * Activate a thesaurus. Parameters: <ul> <li>fname: thesaurus identifier</li> <li>activated: "y" or
 * "n"</li> </ul>
 */
public class Activate implements Service {
    public void init(Path appPath, ServiceConfig params) throws Exception {
    }

    public Element exec(Element params, ServiceContext context) throws Exception {
        GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);

        String fname = Util.getParam(params, Params.REF);
        String activated = Util.getParam(params, "activated");

        ThesaurusManager tm = gc.getBean(ThesaurusManager.class);
        Thesaurus t = tm.getThesaurusByName(fname);

        if (t != null) {
            // Save activated status in the database
            final ThesaurusActivationRepository thesaurusRepository = context.getBean(ThesaurusActivationRepository.class);

            final ThesaurusActivation activation = new ThesaurusActivation();
            activation.setId(fname);
            activation.setActivated(Constants.toBoolean_fromYNChar(activated.charAt(0)));

            thesaurusRepository.save(activation);

        } else {
            // Thesaurus does not exist
            throw new Exception("Thesaurus not found");
        }

        return new Element(Jeeves.Elem.RESPONSE)
            .addContent(params.getChild(Params.REF).detach())
            .addContent(params.getChild("activated").detach());
    }
}

// =============================================================================

