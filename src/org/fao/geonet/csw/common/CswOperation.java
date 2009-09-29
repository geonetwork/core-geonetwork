//=============================================================================
//===	Copyright (C) 2001-2008 Food and Agriculture Organization of the
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

package org.fao.geonet.csw.common;

import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Representation of a CSW operation as it was described in a GetCapabilities response.
 *
 */
public class CswOperation {
	
	public String name;
	public URL    getUrl;
	public URL    postUrl;
	
	/**
	 * The OutputSchemas as advertised in the CSW server's GetCapabilities response.
	 */
	public List<String> outputSchemaList = new ArrayList<String>();

	/**
	 * The preferred OutputSchema from the above.
	 */
	public String preferredOutputSchema;

    public String preferredServerVersion;

	protected void choosePreferredOutputSchema() {
		OutputSchemaPreference preference = new OutputSchemaPreference();
		for(Iterator<String> i = preference.iterator(); i.hasNext();){
			String nextBest = i.next();
			if(outputSchemaList.contains(nextBest)) {
				preferredOutputSchema = nextBest;
				break;
			}
		}
	}
}
