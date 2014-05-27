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

import jeeves.interfaces.Service;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import jeeves.utils.Util;

import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.constants.Params;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.services.Utils;
import org.jdom.Element;

//=============================================================================

/** Retrieves a particular metadata with editing information. Access is restricted
  */

public class GetEditableData implements Service
{
	//--------------------------------------------------------------------------
	//---
	//--- Init
	//---
	//--------------------------------------------------------------------------

	public void init(String appPath, ServiceConfig params) throws Exception {}

	//--------------------------------------------------------------------------
	//---
	//--- Service
	//---
	//--------------------------------------------------------------------------

	public Element exec(Element params, ServiceContext context) throws Exception
	{
		GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
		DataManager   dataMan   = gc.getDataManager();

		String id = Utils.getIdentifierFromParameters(params, context);
		boolean showValidationErrors = Util.getParam(params, Params.SHOWVALIDATIONERRORS, false);
        String justCreated = Util.getParam(params, Geonet.Elem.JUSTCREATED, null);

        // Set current tab for new editing session if defined.
        Element elCurrTab = params.getChild(Params.CURRTAB);
        if (elCurrTab != null) {
            context.getUserSession().setProperty(Geonet.Session.METADATA_SHOW, elCurrTab.getText());
        }

        final Object currtab = context.getUserSession().getProperty(Geonet.Session.METADATA_SHOW);
        if ( currtab != null && currtab.toString().equalsIgnoreCase("inspire")) {
            context.getUserSession().setProperty(Geonet.Session.METADATA_SHOW, "complete");
        }

        //-----------------------------------------------------------------------
		//--- get metadata
		Element elMd = new AjaxEditUtils(context).getMetadataEmbedded(context, id, true, showValidationErrors);
		if (elMd == null)
			throw new IllegalArgumentException("Metadata not found --> " + id);

        if(justCreated != null) {
       //   elMd.addContent(new Element("JUSTCREATED").setText("true"));
        }
		//-----------------------------------------------------------------------
		//--- return metadata

		return elMd;
	}
}

//=============================================================================

