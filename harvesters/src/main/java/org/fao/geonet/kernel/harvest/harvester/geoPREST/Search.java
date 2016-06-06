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

package org.fao.geonet.kernel.harvest.harvester.geoPREST;

import org.fao.geonet.Util;
import org.jdom.Element;

//=============================================================================

class Search {
    //---------------------------------------------------------------------------
    //---
    //--- Constructor
    //---
    //---------------------------------------------------------------------------

    public String freeText;

    //---------------------------------------------------------------------------

    public Search() {
    }

    //---------------------------------------------------------------------------
    //---
    //--- API methods
    //---
    //---------------------------------------------------------------------------

    public Search(Element search) {
        freeText = Util.getParam(search, "freeText", "").trim();
    }

    //---------------------------------------------------------------------------

    public static Search createEmptySearch() {
        return new Search(new Element("search"));
    }

    //---------------------------------------------------------------------------
    //---
    //--- Variables
    //---
    //---------------------------------------------------------------------------

    public Search copy() {
        Search s = new Search();

        s.freeText = freeText;

        return s;
    }
}

//=============================================================================
