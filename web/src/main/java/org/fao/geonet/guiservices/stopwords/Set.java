//=============================================================================
//===	Copyright (C) 2001-2010 Food and Agriculture Organization of the
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
//===	Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
//===
//===	Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
//===	Rome - Italy. email: geonetwork@osgeo.org
//==============================================================================
package org.fao.geonet.guiservices.stopwords;

import jeeves.interfaces.Service;
import jeeves.resources.dbms.Dbms;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.setting.domain.IndexLanguage;
import org.jdom.Element;

import java.util.List;

/**
 * Service to save languages used in the Lucene index. Selected languages' stopword lists are used at index time
 * if the metadata being indexed does not identify its main language, or if no stopwords file for that language is found.
 * Queries are also analyzed using the selected languages' stopwords, if found.
 *
 * @author heikki doeleman
 *
 */
public class Set implements Service {

	public void init(String appPath, ServiceConfig params) throws Exception {}

	public Element exec(Element params, ServiceContext context) throws Exception {

        // retrieve existing indexLanguages
        GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
        Dbms dbms = (Dbms) context.getResourceManager().open(Geonet.Res.MAIN_DB);
        DataManager dataManager = gc.getDataManager();
        java.util.Set<IndexLanguage> languages = dataManager.retrieveIndexLanguages(dbms);

        // set selected to false for all
        for (IndexLanguage indexLanguage : languages) {
            indexLanguage.setSelected(false);
        }

        // loop parameters to set selected to true where applicable 
        @SuppressWarnings("unchecked")
        List<Element> parameters = params.getChildren();
        for(Element parameter : parameters) {
            String paramLanguageName = parameter.getName().substring(parameter.getName().indexOf('-') + 1);
            String paramValue = parameter.getText();
            if(paramValue.equals("on")) {
                for (IndexLanguage indexLanguage : languages) {
                    if (indexLanguage.getName().equals(paramLanguageName)) {
                        indexLanguage.setSelected(true);
                    }
                }
            }
        }

        // save languages
        dataManager.saveIndexLanguages(languages, dbms);

        // loop languages to create a response
		Element response   = new Element("indexlanguages");
        for(IndexLanguage indexLanguage : languages) {
            Element language = new Element("indexlanguage");
            Element name = new Element("name");
            name.setText(indexLanguage.getName());
            language.addContent(name);
            Element selected = new Element("selected");
            selected.setText(Boolean.toString(indexLanguage.isSelected()));
            language.addContent(selected);
            response.addContent(language);
        }

        //
        // re-initialize Lucene analyzer with changed stopwords
        //
        gc.getSearchmanager().initAnalyzer(dataManager, dbms);

        response.addContent(new Element("displayRebuildIndex"));
		return response;
	}

}