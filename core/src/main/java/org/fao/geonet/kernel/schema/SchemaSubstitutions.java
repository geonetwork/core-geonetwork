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
//===	Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
//===
//===	Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
//===	Rome - Italy. email: geonetwork@osgeo.org
//==============================================================================

package org.fao.geonet.kernel.schema;

import org.fao.geonet.utils.Xml;
import org.jdom.Element;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

//=============================================================================

public class SchemaSubstitutions {
    private Hashtable<String, Element> htFields = new Hashtable<String, Element>();

    //--------------------------------------------------------------------------
    //---
    //--- Constructor
    //---
    //--------------------------------------------------------------------------

    public SchemaSubstitutions(Path xmlSubstitutionFile) throws Exception {
        if (xmlSubstitutionFile != null) {
            Element subs = Xml.loadFile(xmlSubstitutionFile);

            @SuppressWarnings("unchecked")
            List<Element> list = subs.getChildren();

            for (Element el : list) {
                if (el.getName().equals("field")) {
                    htFields.put(el.getAttributeValue("name"), el);
                }
            }
        }
    }

    //--------------------------------------------------------------------------
    //---
    //--- API methods
    //---
    //--------------------------------------------------------------------------

    public ArrayList<String> getSubstitutes(String child) {
        Element fieldEl = htFields.get(child);
        if (fieldEl == null)
            return null;

        ArrayList<String> results = new ArrayList<String>();

        @SuppressWarnings("unchecked")
        List<Element> list = fieldEl.getChildren();

        for (Element elem : list) {
            if (elem.getName().equals("substitute")) {
                String name = elem.getAttributeValue("name");
                results.add(name);
            }
        }

        return results;
    }
}

//=============================================================================

