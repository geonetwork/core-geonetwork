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

package org.fao.geonet.kernel.harvest.harvester.oaipmh;

import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.fao.geonet.Util;
import org.fao.geonet.domain.ISODate;
import org.fao.geonet.exceptions.BadInputEx;
import org.fao.geonet.exceptions.BadParameterEx;
import org.jdom.Element;

//=============================================================================

class Search {
    //---------------------------------------------------------------------------
    //---
    //--- Constructor
    //---
    //---------------------------------------------------------------------------

    public String from;

    //---------------------------------------------------------------------------
    public String until;

    //---------------------------------------------------------------------------
    //---
    //--- API methods
    //---
    //---------------------------------------------------------------------------
    public String set;

    //---------------------------------------------------------------------------
    public String prefix;

    //---------------------------------------------------------------------------
    //---
    //--- Variables
    //---
    //---------------------------------------------------------------------------
    public String stylesheet;
    private Search() {
    }
    public Search(Element search) throws BadInputEx {
        from = Util.getParam(search, "from", "");
        until = Util.getParam(search, "until", "");
        set = Util.getParam(search, "set", "");
        prefix = Util.getParam(search, "prefix", "oai_dc");
        stylesheet = Util.getParam(search, "stylesheet", "");

        //--- check from parameter

        ISODate fromDate = null;
        ISODate untilDate = null;

		try {
		    if (StringUtils.isNotEmpty(from) && !from.equalsIgnoreCase("Invalid Date")) {
				fromDate = new ISODate(from);
				from     = fromDate.getDateAsString();
			} else {
			    from = "";
			}

        } catch (Exception e) {
            throw new BadParameterEx("from", from);
        }

        //--- check until parameter

		try {
		    if (StringUtils.isNotEmpty(until) && !until.equalsIgnoreCase("Invalid Date")) {
				untilDate = new ISODate(until);
				until     = untilDate.getDateAsString();
			} else {
			    until = "";
            }
		} catch(Exception e) {
			throw new BadParameterEx("until", until);
		}

        //--- check from <= until

        if (fromDate != null && untilDate != null)
            if (fromDate.timeDifferenceInSeconds(untilDate) > 0)
                throw new BadParameterEx("from greater than until", from + ">" + until);
    }

    public static Search createEmptySearch() throws BadInputEx {
        Search s = new Search();

        s.from = "";
        s.until = "";
        s.set = "";
        s.prefix = "oai_dc";
        s.stylesheet = "";

        return s;
    }

    public Search copy() {
        Search s = new Search();

        s.from = from;
        s.until = until;
        s.set = set;
        s.prefix = prefix;
        s.stylesheet = stylesheet;

        return s;
    }
}

//=============================================================================


