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
//===	Rome - Italy. email: geonetwork@osgeo.org
//==============================================================================

package org.fao.geonet.services.thesaurus;

import jeeves.constants.Jeeves;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.Util;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.Constants;
import org.fao.geonet.domain.ThesaurusActivation;
import org.fao.geonet.kernel.Thesaurus;
import org.fao.geonet.kernel.ThesaurusManager;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.languages.IsoLanguagesMapper;
import org.fao.geonet.repository.ThesaurusActivationRepository;
import org.fao.geonet.services.NotInReadOnlyModeService;
import org.jdom.Element;

import java.nio.file.Path;

/**
 * For editing : adds a tag to a thesaurus. Access is restricted
 */
public class Add extends NotInReadOnlyModeService {
    public void init(Path appPath, ServiceConfig params) throws Exception {
    }

    // --------------------------------------------------------------------------
    // ---
    // --- Service
    // ---
    // --------------------------------------------------------------------------

    public Element serviceSpecificExec(Element params, ServiceContext context)
        throws Exception {
        GeonetContext gc = (GeonetContext) context
            .getHandlerContext(Geonet.CONTEXT_NAME);

        String fname = Util.getParam(params, "fname");
        String description = Util.getParam(params, "description", "");
        String tname = Util.getParam(params, "tname");
        String tnamespace = Util.getParam(params, "tns");
        String dname = Util.getParam(params, "dname");
        String type = Util.getParam(params, "type");
        String activated = Util.getParam(params, "activated", "y");

        fname = fname.trim().replaceAll("\\s+", "");

        if (!fname.endsWith(".rdf")) {
            fname = fname + ".rdf";
        }

        ThesaurusManager tm = gc.getBean(ThesaurusManager.class);

        Path rdfFile = tm.buildThesaurusFilePath(fname, type, dname);

        final String siteURL = context.getBean(SettingManager.class).getSiteURL(context);
        final IsoLanguagesMapper isoLanguageMapper = context.getBean(IsoLanguagesMapper.class);
        Thesaurus thesaurus = new Thesaurus(isoLanguageMapper, fname, tname, description, tnamespace, type, dname, rdfFile, siteURL, false, tm.getThesaurusCacheMaxSize());
        tm.addThesaurus(thesaurus, true);

        // Save activated status in the database
        ThesaurusActivation activation = new ThesaurusActivation();
        activation.setActivated(Constants.toBoolean_fromYNChar(activated.charAt(0)));
        activation.setId(fname);

        context.getBean(ThesaurusActivationRepository.class).save(activation);

        Element elResp = new Element(Jeeves.Elem.RESPONSE);
        Element elRef = new Element("ref");
        elRef.addContent(thesaurus.getKey());
        elResp.addContent(elRef);
        Element elName = new Element("thesaName").setText(fname);
        elResp.addContent(elName);

        return elResp;
    }
}
