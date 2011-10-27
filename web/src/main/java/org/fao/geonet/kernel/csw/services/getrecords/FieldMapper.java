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

package org.fao.geonet.kernel.csw.services.getrecords;

import org.fao.geonet.kernel.csw.CatalogConfiguration;
import org.jdom.Element;

import java.util.HashMap;
import java.util.Set;

//==============================================================================

public class FieldMapper
{
    //---------------------------------------------------------------------------
    //---
    //--- API methods
    //---
    //---------------------------------------------------------------------------

    public static String map(String field)
    {
    	return CatalogConfiguration.getFieldMapping().get(getAbsolute(field));
    }

    //---------------------------------------------------------------------------

    public static String mapXPath(String field, String schema)
    {
        HashMap<String, String> xpaths =  CatalogConfiguration.getFieldMappingXPath().get(getAbsolute(field));

        return (xpaths != null)?xpaths.get(schema):null;
    }

    //---------------------------------------------------------------------------

    public static Iterable<String> getMappedFields()
    {
		return CatalogConfiguration.getFieldMapping().values();
    }

    //---------------------------------------------------------------------------

    public static boolean match(Element elem, Set<String> elemNames)
    {
	String name = elem.getQualifiedName();

	for (String field : elemNames)
		// Here we supposed that namespaces prefix are equals when removing elements 
		// when an ElementName parameter is set.
	    if (field.equals(name))
	    	return true;

	return false;
    }
    
    //---------------------------------------------------------------------------
    
    public static Set<String> getPropertiesByType(String type) {
    	return  CatalogConfiguration.getTypeMapping(type); 
    }

    //---------------------------------------------------------------------------
    //---
    //--- Private methods
    //---
    //---------------------------------------------------------------------------

    private static String getAbsolute(String field)
    {
	if (field.startsWith("./"))
	    field = field.substring(2);
	
	// Remove any namespaces ... to be validated
	if (field.contains(":"))
		field = field.substring(field.indexOf(':')+1);

	return field.toLowerCase();
    }

}

//==============================================================================

