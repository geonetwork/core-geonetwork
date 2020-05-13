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

import org.fao.geonet.Util;
import org.fao.geonet.exceptions.BadInputEx;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.harvest.harvester.AbstractParams;
import org.jdom.Element;

import java.util.ArrayList;

//=============================================================================

public class OaiPmhParams extends AbstractParams {
    //--------------------------------------------------------------------------
    //---
    //--- Constructor
    //---
    //--------------------------------------------------------------------------

    public String url;

    /**
     * The filter is a process (see schema/process folder) which depends on the schema.
     * It could be composed of parameter which will be sent to XSL transformation using
     * the following syntax :
     * <pre>
     * anonymizer?protocol=MYLOCALNETWORK:FILEPATH&email=gis@organisation.org&thesaurus=MYORGONLYTHEASURUS
     * </pre>
     */
    public String  xslfilter;

    //---------------------------------------------------------------------------
    //---
    //--- Create : called when a new entry must be added. Reads values from the
    //---          provided entry, providing default values
    //---
    //---------------------------------------------------------------------------
    public String icon;

    //---------------------------------------------------------------------------
    //---
    //--- Update : called when an entry has changed and variables must be updated
    //---
    //---------------------------------------------------------------------------
    private ArrayList<Search> alSearches = new ArrayList<Search>();

    //---------------------------------------------------------------------------
    //---
    //--- Other API methods
    //---
    //---------------------------------------------------------------------------

    public OaiPmhParams(DataManager dm) {
        super(dm);
    }

    //---------------------------------------------------------------------------

    public void create(Element node) throws BadInputEx {
        super.create(node);

        Element site = node.getChild("site");
        Element searches = node.getChild("searches");

        url = Util.getParam(site, "url", "");
        icon = Util.getParam(site, "icon", "");
        xslfilter = Util.getParam(site, "xslfilter", "");

        addSearches(searches);
    }

    //---------------------------------------------------------------------------

    public void update(Element node) throws BadInputEx {
        super.update(node);

        Element site = node.getChild("site");
        Element searches = node.getChild("searches");

        url = Util.getParam(site, "url", url);
        icon = Util.getParam(site, "icon", icon);
        xslfilter = Util.getParam(site, "xslfilter", "");

        //--- if some search queries are given, we drop the previous ones and
        //--- set these new ones

        if (searches != null)
            addSearches(searches);
    }

    //---------------------------------------------------------------------------
    //---
    //--- Private methods
    //---
    //---------------------------------------------------------------------------

    public Iterable<Search> getSearches() {
        return alSearches;
    }

    //---------------------------------------------------------------------------
    //---
    //--- Variables
    //---
    //---------------------------------------------------------------------------

    public boolean isSearchEmpty() {
        return alSearches.isEmpty();
    }

    public OaiPmhParams copy() {
        OaiPmhParams copy = new OaiPmhParams(dm);
        copyTo(copy);

        copy.url = url;
        copy.icon = icon;
        copy.xslfilter = xslfilter;

        copy.setValidate(getValidate());

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

            alSearches.add(new Search(search));
        }
    }

    @Override
    public String getIcon() {
        return icon;
    }
}

//=============================================================================


