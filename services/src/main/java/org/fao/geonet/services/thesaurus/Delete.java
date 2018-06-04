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
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;

import org.fao.geonet.GeonetContext;
import org.fao.geonet.Util;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.constants.Params;
import org.fao.geonet.kernel.Thesaurus;
import org.fao.geonet.kernel.ThesaurusManager;
import org.fao.geonet.repository.ThesaurusActivationRepository;
import org.fao.geonet.services.NotInReadOnlyModeService;
import org.fao.geonet.utils.IO;
import org.jdom.Element;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Removes a thesaurus from the system.
 */
@Deprecated
public class Delete extends NotInReadOnlyModeService {
    public void init(Path appPath, ServiceConfig params) throws Exception {
    }

    //--------------------------------------------------------------------------
    //---
    //--- Service
    //---
    //--------------------------------------------------------------------------

    public Element serviceSpecificExec(Element params, ServiceContext context) throws Exception {
        GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
        ThesaurusManager manager = gc.getBean(ThesaurusManager.class);

        // Get parameters
        String name = Util.getParam(params, Params.REF);


        // Load file
        Thesaurus thesaurus = manager.getThesaurusByName(name);
        Path item = thesaurus.getFile();

        // Remove old file from thesaurus manager
        manager.remove(name);

        // Remove file
        if (Files.exists(item)) {
            IO.deleteFile(item, true, Geonet.THESAURUS);

            // Delete thesaurus record in the database
            ThesaurusActivationRepository repo = context.getBean(ThesaurusActivationRepository.class);
            String thesaurusId = thesaurus.getFname();
            if (repo.exists(thesaurusId)) {
                repo.delete(thesaurusId);
            }
        } else {
            throw new IllegalArgumentException("Thesaurus not found --> " + name);
        }

        return new Element(Jeeves.Elem.RESPONSE)
            .addContent(new Element(Jeeves.Elem.OPERATION).setText(Jeeves.Text.REMOVED));
    }
}
