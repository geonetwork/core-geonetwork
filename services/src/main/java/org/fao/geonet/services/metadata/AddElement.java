//=============================================================================
//===	Copyright (C) 2001-2007 Food and Agriculture Organization of the
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

package org.fao.geonet.services.metadata;

import jeeves.server.ServiceConfig;
import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;

import org.fao.geonet.Util;
import org.fao.geonet.api.records.editing.AjaxEditUtils;
import org.fao.geonet.constants.Params;
import org.fao.geonet.kernel.EditLib;
import org.fao.geonet.services.NotInReadOnlyModeService;
import org.jdom.Element;

import java.nio.file.Path;

/**
 * For editing : adds a tag to a metadata. Access is restricted.
 */
@Deprecated
public class AddElement extends NotInReadOnlyModeService {
    public void init(Path appPath, ServiceConfig params) throws Exception {
    }

    //--------------------------------------------------------------------------
    //---
    //--- Service
    //---
    //--------------------------------------------------------------------------

    /**
     *
     * @param params
     * @param context
     * @return
     * @throws Exception
     */
    @Override
    public Element serviceSpecificExec(Element params, ServiceContext context) throws Exception {

        UserSession session = context.getUserSession();

        String id = Util.getParam(params, Params.ID);
        String ref = Util.getParam(params, Params.REF);
        String name = Util.getParam(params, Params.NAME);
        String child = params.getChildText(Params.CHILD);

        // -- build the element to be added
        // -- Here we do mark the element that is added
        // -- then we traverse up the tree to the root
        // -- clone from the root and return the clone
        // -- this is done so that the style sheets have
        // -- access to important information like the
        // -- document language and other locales
        // -- this is important for multilingual editing
        // --
        // -- Note that the metadata-embedded.xsl stylesheet
        // -- only applies the templating to the added element, not to
        // -- the entire metadata so performance should not be a big issue
        Element elResp = new AjaxEditUtils(context).addElementEmbedded(session, id, ref, name, child);
        EditLib.tagForDisplay(elResp);
        Element md = (Element) findRoot(elResp).clone();
        EditLib.removeDisplayTag(elResp);

        return md;
    }

    private Element findRoot(Element element) {
        if (element.isRootElement() || element.getParentElement() == null) return element;
        return findRoot(element.getParentElement());
    }
}
