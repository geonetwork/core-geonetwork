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

package org.fao.geonet.csw.common;

import org.jdom.Namespace;

//=============================================================================

public class Csw {
	//---------------------------------------------------------------------------
	//---
	//--- Namespaces
	//---
	//---------------------------------------------------------------------------

	public static final Namespace NAMESPACE_CSW = Namespace.getNamespace("csw", "http://www.opengis.net/cat/csw/2.0.2");
	public static final Namespace NAMESPACE_CSW_OLD = Namespace.getNamespace("csw", "http://www.opengis.net/cat/csw");
	public static final Namespace NAMESPACE_OGC = Namespace.getNamespace("ogc", "http://www.opengis.net/ogc");
	public static final Namespace NAMESPACE_OWS = Namespace.getNamespace("ows", "http://www.opengis.net/ows");
	public static final Namespace NAMESPACE_ENV = Namespace.getNamespace("env", "http://www.w3.org/2003/05/soap-envelope");
	public static final Namespace NAMESPACE_GMD = Namespace.getNamespace("gmd", "http://www.isotc211.org/2005/gmd");

	//---------------------------------------------------------------------------
	//---
	//--- Strings
	//---
	//---------------------------------------------------------------------------

	public static final String SCHEMA_LANGUAGE = "http://www.w3.org/XML/Schema";
	public static final String SERVICE         = "CSW";

	public static final String CSW_VERSION    = "2.0.2";
	public static final String OWS_VERSION    = "1.0.0";
	public static final String FILTER_VERSION = "1.1.0";
}

//=============================================================================

