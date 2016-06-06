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
import org.fao.geonet.domain.Pair;
import org.fao.geonet.kernel.KeywordBean;
import org.fao.geonet.kernel.Thesaurus;
import org.fao.geonet.kernel.ThesaurusManager;
import org.fao.geonet.languages.IsoLanguagesMapper;
import org.jdom.Element;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

//=============================================================================

/**
 * For editing : adds a tag to a thesaurus. Access is restricted
 */

public class AddElement implements Service {

    public static final String PREFIX = "loc_";
    public static final String DEFINITION = "definition";
    public static final String PREF_LAB = "label";

    public static Map<Pair<String, String>, String> getLocalizedElements(Element params) {
        final java.util.List<Element> children = params.getChildren();

        Map<Pair<String, String>, String> map = new HashMap<Pair<String, String>, String>();

        for (Element child : children) {
            if (child.getName().startsWith(PREFIX)) {
                String[] parts = child.getName().split("_");
                String lang = parts[1];
                String type = parts[2];
                map.put(Pair.read(lang, type), child.getTextTrim());
            }
        }
        return map;
    }

    // --------------------------------------------------------------------------
    // ---
    // --- Service
    // ---
    // --------------------------------------------------------------------------

    public void init(Path appPath, ServiceConfig params) throws Exception {
    }

    public Element exec(Element params, ServiceContext context)
        throws Exception {
        GeonetContext gc = (GeonetContext) context
            .getHandlerContext(Geonet.CONTEXT_NAME);

        String ref = Util.getParam(params, Params.REF);
        String namespace = Util.getParam(params, "namespace", "");
        String newid = Util.getParam(params, "newid", null);
        if (newid == null) {
            newid = UUID.randomUUID().toString();
        }
        String thesaType = Util.getParam(params, "refType");
        Element elResp = new Element(Jeeves.Elem.RESPONSE);

        ThesaurusManager manager = gc.getBean(ThesaurusManager.class);
        Thesaurus thesaurus = manager.getThesaurusByName(ref);

        if (thesaurus.isFreeCode(null, newid)) {

            KeywordBean keyword = new KeywordBean(context.getBean(IsoLanguagesMapper.class))
                .setNamespaceCode(namespace)
                .setUriCode(newid);

            Map<Pair<String, String>, String> localizations = getLocalizedElements(params);
            if (localizations.isEmpty()) {
                String prefLab = Util.getParam(params, PREF_LAB);
                String lang = Util.getParam(params, "lang");
                String definition = Util.getParam(params, DEFINITION, "");

                keyword.setValue(prefLab, lang)
                    .setDefinition(definition, lang);
            } else {
                Set<Map.Entry<Pair<String, String>, String>> entries = localizations.entrySet();

                for (Map.Entry<Pair<String, String>, String> entry : entries) {
                    String lang = entry.getKey().one();
                    if (entry.getKey().two().equals(DEFINITION)) {
                        final String definition = entry.getValue();
                        keyword.setDefinition(definition, lang);
                    } else if (entry.getKey().two().equals(PREF_LAB)) {
                        final String label = entry.getValue();
                        keyword.setValue(label, lang);
                    } else {
                        throw new IllegalArgumentException("Unknown localization type: " + entry.getKey().two());
                    }

                }
            }

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
            elResp.addContent(new Element("error").setAttribute("message",
                String.format("Code value '%s' already exists in thesaurus", newid)));
        }

        return elResp;
    }
}

// =============================================================================

