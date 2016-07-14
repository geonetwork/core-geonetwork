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


import static org.fao.geonet.services.thesaurus.AddElement.*;

import jeeves.constants.Jeeves;
import jeeves.interfaces.Service;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;

import org.fao.geonet.Util;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.constants.Params;
import org.fao.geonet.domain.Pair;
import org.fao.geonet.kernel.KeywordBean;
import org.fao.geonet.kernel.Thesaurus;
import org.fao.geonet.kernel.ThesaurusManager;
import org.jdom.Element;

import java.nio.file.Path;
import java.util.Map;
import java.util.Set;

//=============================================================================

/**
 * Update the information of a thesaurus
 */

public class UpdateElement implements Service {
    public void init(Path appPath, ServiceConfig params) throws Exception {
    }

    // --------------------------------------------------------------------------
    // ---
    // --- Service
    // ---
    // --------------------------------------------------------------------------

    /*
     * TODO
     */
    public Element exec(Element params, ServiceContext context)
        throws Exception {
        GeonetContext gc = (GeonetContext) context
            .getHandlerContext(Geonet.CONTEXT_NAME);

        String ref = Util.getParam(params, Params.REF);
        String oldid = Util.getParam(params, "oldid");
        String newid = Util.getParam(params, "newid");
        String namespace = Util.getParam(params, "namespace");
        String thesaType = Util.getParam(params, "refType");

        ThesaurusManager manager = gc.getBean(ThesaurusManager.class);
        Thesaurus thesaurus = manager.getThesaurusByName(ref);

        if (!(oldid.equals(newid))) {
            if (thesaurus.isFreeCode(null, newid)) {
                thesaurus.updateCodeByURI(oldid, newid);
            } else {
                Element elResp = new Element(Jeeves.Elem.RESPONSE);
                elResp.addContent(new Element("error").addContent(new Element("message").setText("Code value already exists in thesaurus")));
                return elResp;
            }
        }
        KeywordBean bean = new KeywordBean(thesaurus.getIsoLanguageMapper())
            .setNamespaceCode(namespace)
            .setUriCode(newid);

        Map<Pair<String, String>, String> localizations = getLocalizedElements(params);
        if (localizations.isEmpty()) {
            String prefLab = Util.getParam(params, PREF_LAB);
            String lang = Util.getParam(params, "lang");
            String definition = Util.getParam(params, DEFINITION, "");

            bean.setValue(prefLab, lang).setDefinition(definition, lang);
        } else {
            Set<Map.Entry<Pair<String, String>, String>> entries = localizations.entrySet();

            for (Map.Entry<Pair<String, String>, String> entry : entries) {
                String lang = entry.getKey().one();
                if (entry.getKey().two().equals(DEFINITION)) {
                    final String definition = entry.getValue();
                    bean.setDefinition(definition, lang);
                } else if (entry.getKey().two().equals(PREF_LAB)) {
                    final String label = entry.getValue();
                    bean.setValue(label, lang);
                } else {
                    throw new IllegalArgumentException("Unknown localization type: " + entry.getKey().two());
                }

            }
        }

        if (thesaType.equals("place")) {
            bean.setCoordEast(Util.getParam(params, "east"))
                .setCoordNorth(Util.getParam(params, "north"))
                .setCoordSouth(Util.getParam(params, "south"))
                .setCoordWest(Util.getParam(params, "west"));
        }

        thesaurus.updateElement(bean, false);

        Element elResp = new Element(Jeeves.Elem.RESPONSE);
        elResp.addContent(new Element("selected").setText(ref));
        elResp.addContent(new Element("mode").setText("edit"));
        return elResp;
    }
}

// =============================================================================

