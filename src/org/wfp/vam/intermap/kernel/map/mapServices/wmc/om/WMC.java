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
//==============================================================================

package org.wfp.vam.intermap.kernel.map.mapServices.wmc.om;

import org.jdom.Namespace;

/**
 *
 * @author etj
 */
public class WMC {
	
	public final static Namespace XLINKNS = Namespace.getNamespace("xlink", "http://www.w3.org/1999/xlink");

	/** Standard OGC namespace for WMC documents */
	public final static Namespace WMCNS   = Namespace.getNamespace("http://www.opengeospatial.net/context");
	
	/** Alternate namespace for WMC docs used sometime by non-compliant servers. */
	public final static Namespace OGWMCNS = Namespace.getNamespace("http://www.opengis.net/context");

}
