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

package org.fao.geonet.kernel.harvest.harvester.geonet20;

import org.apache.commons.lang.StringUtils;
import org.fao.geonet.Util;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.exceptions.BadInputEx;
import org.fao.geonet.exceptions.BadParameterEx;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.harvest.harvester.AbstractParams;
import org.fao.geonet.lib.Lib;
import org.fao.geonet.utils.Log;
import org.jdom.Element;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

//=============================================================================

public class GeonetParams extends AbstractParams {
    //--------------------------------------------------------------------------
    //---
    //--- Constructor
    //---
    //--------------------------------------------------------------------------

    public String host;

    //---------------------------------------------------------------------------
    //---
    //--- Create : called when a new entry must be added. Reads values from the
    //---          provided entry, providing default values
    //---
    //---------------------------------------------------------------------------
    private ArrayList<Search> alSearches = new ArrayList<Search>();

    //---------------------------------------------------------------------------
    //---
    //--- Update : called when an entry has changed and variables must be updated
    //---
    //---------------------------------------------------------------------------

    @Override
    public String getIcon() {
        return "gn20.gif";
    }

    public GeonetParams(DataManager dm) {
        super(dm);
    }

    //---------------------------------------------------------------------------
    //---
    //--- Other API methods
    //---
    //---------------------------------------------------------------------------

    public void create(Element node) throws BadInputEx {
        super.create(node);

        Element site = node.getChild("site");
        Element searches = node.getChild("searches");

        host = Util.getParam(site, "host", "");

        //checkPort(port);
        addSearches(searches);
    }

    public void update(Element node) throws BadInputEx {
        super.update(node);

        Element site = node.getChild("site");
        Element searches = node.getChild("searches");

        host = Util.getParam(site, "host", host);

        //checkPort(port);

        //--- if some search queries are given, we drop the previous ones and
        //--- set these new ones

        if (searches != null)
            addSearches(searches);
    }

    //---------------------------------------------------------------------------

    public Iterable<Search> getSearches() {
        return alSearches;
    }

    //---------------------------------------------------------------------------
    //---
    //--- Private methods
    //---
    //---------------------------------------------------------------------------

    public String getServletPath() {
        if (StringUtils.isNotEmpty(host)) {
            try {
                return new URL(host).getPath();
            } catch (MalformedURLException ex) {
                Log.error(Geonet.HARVEST_MAN, ex.getMessage(), ex);
            }
        }

        return "";
    }

    //---------------------------------------------------------------------------
    //---
    //--- Variables
    //---
    //---------------------------------------------------------------------------

    public GeonetParams copy() {
        GeonetParams copy = new GeonetParams(dm);
        copyTo(copy);

        copy.host = host;

        for (Search s : alSearches)
            copy.alSearches.add(s.copy());

        return copy;
    }

    private void addSearches(Element searches) throws BadInputEx {
        alSearches.clear();

        if (searches == null)
            return;

        for (Object o : searches.getChildren("search")) {
            Element search = (Element) o;

            Search s = new Search();

            s.freeText = Util.getParam(search, "freeText", "");
            s.title = Util.getParam(search, "title", "");
            s.abstrac = Util.getParam(search, "abstract", "");
            s.keywords = Util.getParam(search, "keywords", "");
            s.digital = Util.getParam(search, "digital", false);
            s.hardcopy = Util.getParam(search, "hardcopy", false);
            s.siteId = Util.getParam(search, "siteId", "");

            alSearches.add(s);

            if (s.siteId.equals("")) {
                throw new BadParameterEx("siteId", "");
            }
        }
    }
}

//=============================================================================

class Search {
    //---------------------------------------------------------------------------
    //---
    //--- API methods
    //---
    //---------------------------------------------------------------------------

    public String freeText;

    //---------------------------------------------------------------------------
    public String title;

    //---------------------------------------------------------------------------
    //---
    //--- Variables
    //---
    //---------------------------------------------------------------------------
    public String abstrac;
    public String keywords;
    public boolean digital;
    public boolean hardcopy;
    public String siteId;

    public Search copy() {
        Search s = new Search();

        s.freeText = freeText;
        s.title = title;
        s.abstrac = abstrac;
        s.keywords = keywords;
        s.digital = digital;
        s.hardcopy = hardcopy;
        s.siteId = siteId;

        return s;
    }

    public Element createRequest() {
        Element req = new Element("request");

        Lib.element.add(req, "any", freeText);
        Lib.element.add(req, "title", title);
        Lib.element.add(req, "abstract", abstrac);
        Lib.element.add(req, "themekey", keywords);
        Lib.element.add(req, "siteId", siteId);

        if (digital)
            Lib.element.add(req, "digital", "on");

        if (hardcopy)
            Lib.element.add(req, "paper", "on");

        return req;
    }
}

//=============================================================================

