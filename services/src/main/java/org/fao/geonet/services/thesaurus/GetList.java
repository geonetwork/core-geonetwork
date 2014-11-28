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
import org.fao.geonet.domain.Constants;
import org.fao.geonet.domain.ThesaurusActivation;
import org.fao.geonet.kernel.Thesaurus;
import org.fao.geonet.kernel.ThesaurusManager;
import org.fao.geonet.repository.ThesaurusActivationRepository;
import org.jdom.Element;
import org.jdom.JDOMException;

import java.io.IOException;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;

//=============================================================================

/**
 * 
 * Retrieve Thesauri list.
 * 
 * @author mcoudert
 *
 */
public class GetList implements Service {
	public void init(Path appPath, ServiceConfig params) throws Exception {
	}

	// --------------------------------------------------------------------------
	// ---
	// --- Service
	// ---
	// --------------------------------------------------------------------------

	public Element exec(Element params, ServiceContext context)
			throws Exception {
		Element response = new Element(Jeeves.Elem.RESPONSE);
		
		ThesaurusManager th = context.getBean(ThesaurusManager.class);
		Map<String, Thesaurus> thTable = th.getThesauriMap();
		
		response.addContent(buildResultfromThTable(context, thTable));
		
		return response;
	}
	
	/**
	 *
     * @param context
     * @param thTable
     * @return {@link org.jdom.Element}
	 * @throws java.sql.SQLException
	 */
	private Element buildResultfromThTable(ServiceContext context, Map<String, Thesaurus> thTable) throws SQLException, JDOMException, IOException {
		
		Element elRoot = new Element("thesauri");
		
		Collection<Thesaurus> e = thTable.values();
		for (Thesaurus currentTh : e) {
	        Element elLoop = new Element("thesaurus");
			
			Element elKey = new Element("key");
			String key = currentTh.getKey();
			elKey.addContent(key);
			
			Element elDname = new Element("dname");
			String dname = currentTh.getDname();
			elDname.addContent(dname);
			
			Element elFname = new Element("filename");
			String fname = currentTh.getFname();
			elFname.addContent(fname);
			
			Element elTitle = new Element("title");
			String title = currentTh.getTitles(context.getApplicationContext()).get(context.getLanguage());
			if(title == null) {
				title = currentTh.getTitle();
			}
			elTitle.addContent(title);
			
			Element elType = new Element("type");
			String type = currentTh.getType();
			elType.addContent(type);
			
			Element elDate = new Element("date");
            String date = currentTh.getDate();
            elDate.addContent(date);
            
            Element elUrl = new Element("url");
            String url = currentTh.getDownloadUrl();
            elUrl.addContent(url);
            
            Element elDefaultURI = new Element("defaultNamespace");
            String defaultURI = currentTh.getDefaultNamespace();
            elDefaultURI.addContent(defaultURI);
            
	        
			Element elActivated= new Element("activated");
            char activated = Constants.YN_TRUE;
            final ThesaurusActivationRepository activationRepository = context.getBean(ThesaurusActivationRepository.class);
            final ThesaurusActivation activation = activationRepository.findOne(currentTh.getKey());
            if (activation == null || !activation.isActivated()) {
                activated = Constants.YN_FALSE;
            }
            elActivated.setText(""+activated);

            elLoop.addContent(elKey);
            elLoop.addContent(elDname);
			elLoop.addContent(elFname);
			elLoop.addContent(elTitle);
            elLoop.addContent(elDate);
            elLoop.addContent(elUrl);
            elLoop.addContent(elDefaultURI);
			elLoop.addContent(elType);
			elLoop.addContent(elActivated);
			
			elRoot.addContent(elLoop);
		}
		
		return elRoot;
	}
}

// =============================================================================

