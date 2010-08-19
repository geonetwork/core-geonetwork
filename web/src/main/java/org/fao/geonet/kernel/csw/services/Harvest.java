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

package org.fao.geonet.kernel.csw.services;

import jeeves.server.context.ServiceContext;
import org.fao.geonet.csw.common.Csw;
import org.fao.geonet.csw.common.exceptions.CatalogException;
import org.fao.geonet.csw.common.exceptions.OperationNotSupportedEx;
import org.fao.geonet.kernel.csw.CatalogService;
import org.jdom.Element;

import java.util.Map;

//=============================================================================
/**
 * "This is the pull mechanism that 'pulls' data into the catalogue. That is,
 * this operation only references the data to be inserted or updated in the
 * catalogue, and it is the job of the catalogue service to resolve the
 * reference, fetch that data, and process it into the catalogue."
 * 
 */
public class Harvest extends AbstractOperation implements CatalogService {
	// ---------------------------------------------------------------------------
	// ---
	// --- Constructor
	// ---
	// ---------------------------------------------------------------------------

	public Harvest() {
	}

	// ---------------------------------------------------------------------------
	// ---
	// --- API methods
	// ---
	// ---------------------------------------------------------------------------

	public String getName() {
		return "Harvest";
	}

	// ---------------------------------------------------------------------------
	public Element execute(Element request, ServiceContext context)
	throws CatalogException {

		throw new OperationNotSupportedEx(getName() + 
			"Harvest operation is not supported by this catalogue.\n");
	}


    // ---------------------------------------------------------------------------

	public Element adaptGetRequest(Map<String, String> params) {

        return new Element(getName(), Csw.NAMESPACE_CSW);
	}

	// ---------------------------------------------------------------------------

	public Element retrieveValues(String parameterName) throws CatalogException {
		// TODO Auto-generated method stub
		return null;
	}
}
