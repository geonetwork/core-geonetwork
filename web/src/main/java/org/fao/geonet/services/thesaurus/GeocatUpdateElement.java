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

import com.google.common.base.Functions;
import jeeves.constants.Jeeves;
import jeeves.interfaces.Service;
import jeeves.resources.dbms.Dbms;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import jeeves.utils.Util;
import jeeves.xlink.Processor;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.constants.Params;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.KeywordBean;
import org.fao.geonet.kernel.Thesaurus;
import org.fao.geonet.kernel.ThesaurusManager;
import org.fao.geonet.kernel.reusable.KeywordsStrategy;
import org.fao.geonet.kernel.reusable.MetadataRecord;
import org.fao.geonet.kernel.reusable.Utils;
import org.jdom.Element;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

//=============================================================================

/**
 * Update the information of a thesaurus
 */

public class GeocatUpdateElement implements Service {
	public void init(String appPath, ServiceConfig params) throws Exception {
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
		Map<String,String> prefLab = lookupLabels(params);
		String definition = Util.getParam(params, "definition", "");

		ThesaurusManager manager = gc.getThesaurusManager();
		Thesaurus thesaurus = manager.getThesaurusByName(ref);

		if (!(oldid.equals(newid))) {
			if (thesaurus.isFreeCode(namespace, newid)) {
				thesaurus.updateCode(namespace, oldid, newid);
			}else{
				Element elResp = new Element(Jeeves.Elem.RESPONSE);
				elResp.addContent(new Element("error").addContent(new Element("message").setText("Code value already exists in thesaurus")));
				return elResp;
			}
		}
		KeywordBean bean = new KeywordBean().setNamespaceCode(namespace).setRelativeCode(newid);
		if (thesaType.equals("place")) {
			String east = Util.getParam(params, "east");
			String west = Util.getParam(params, "west");
			String south = Util.getParam(params, "south");
			String north = Util.getParam(params, "north");
			bean.setCoordEast(east)
			    .setCoordNorth(north)
			    .setCoordSouth(south)
			    .setCoordWest(west);
		}
		
        for (Map.Entry<String, String> entry : prefLab.entrySet()) {
            bean.setValue(entry.getValue(), entry.getKey());
            bean.setDefinition(definition, entry.getKey());
        }

        thesaurus.updateElement(bean, true);

        reindex(context, gc, newid, manager);


        Element elResp = new Element(Jeeves.Elem.RESPONSE);
		elResp.addContent(new Element("selected").setText(ref));
		elResp.addContent(new Element("mode").setText("edit"));
		return elResp;
	}

    static void reindex(ServiceContext context, GeonetContext gc, String newid, ThesaurusManager manager) throws Exception {
        Processor.clearCache();

        final KeywordsStrategy strategy = new KeywordsStrategy(manager, context.getAppPath(), context.getBaseUrl(), context.getLanguage());
        ArrayList<String> fields = new ArrayList<String>();

        fields.addAll(Arrays.asList(strategy.getInvalidXlinkLuceneField()));
        fields.addAll(Arrays.asList(strategy.getValidXlinkLuceneField()));
        final Set<MetadataRecord> referencingMetadata = Utils.getReferencingMetadata(context, strategy, fields, newid, null, false,
                Functions.<String>identity());


        Dbms dbms = (Dbms) context.getResourceManager().open(Geonet.Res.MAIN_DB);
        DataManager dm = gc.getDataManager();
        for (MetadataRecord metadataRecord : referencingMetadata) {
            dm.indexMetadata(dbms, metadataRecord.id, true, context, false, false, true);
        }
    }

    static Map<String, String> lookupLabels(Element params)
    {
        final String prefix = "prefLab";

        HashMap<String, String> mappings = new HashMap<String, String>();
        for (Element e : (Collection<Element>) params.getChildren()) {
            if( e.getName().startsWith(prefix) ){
                String language = e.getName().substring(prefix.length()).toLowerCase();
                mappings.put(language, e.getTextTrim());
            }
        }

        return mappings;
    }
}

// =============================================================================

