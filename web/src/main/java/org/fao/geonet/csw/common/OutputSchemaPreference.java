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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * OutputSchema in order of preference. 
 * 
 * TODO This should be made configurable by a system administrator.
 *  
 */
public class OutputSchemaPreference {

	/**
	 * List of OutputSchema in order of preference.
	 */
	private static List<String> outputSchemas = new ArrayList<String>();

	/**
	 * Populate list of OutputSchemas in order of preference.
	 */
	static {
		outputSchemas.add(Csw.NAMESPACE_GMD.getURI());
		outputSchemas.add(Csw.NAMESPACE_CSW.getURI());
	}
	
	public Iterator<String> iterator() {
		return outputSchemas.iterator();
	}
	
	
}
