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

package org.fao.geonet.kernel.harvest.harvester.csw;

import jeeves.constants.Jeeves;
import jeeves.exceptions.BadInputEx;
import jeeves.utils.Util;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.harvest.harvester.AbstractParams;
import org.jdom.Element;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class CswParams extends AbstractParams {
	//--------------------------------------------------------------------------
	//---
	//--- Constructor
	//---
	//--------------------------------------------------------------------------

	public CswParams(DataManager dm) {
		super(dm);
	}

    /**
     * called when a new entry must be added. Reads values from the provided entry, providing default values.
     *
     * @param node
     * @throws BadInputEx
     */
	public void create(Element node) throws BadInputEx {
		super.create(node);

		Element site     = node.getChild("site");
		Element searches = node.getChild("searches");

		capabUrl = Util.getParam(site, "capabilitiesUrl", "");
        rejectDuplicateResource = Util.getParam(site, "rejectDuplicateResource",  false);
        queryScope = Util.getParam(site, "queryScope", "off");
        hopCount = Util.getParam(site, "hopCount", 2);
        
        try {
            capabUrl = URLDecoder.decode(capabUrl, Jeeves.ENCODING);
        }
        catch (UnsupportedEncodingException x) {
            System.out.println(x.getMessage());
            x.printStackTrace();
            // TODO should not swallow
        }
		icon     = Util.getParam(site, "icon",            "default.gif");

		addSearches(searches);
		
		if (searches!=null){
			if (searches.getChild("search")!=null){
			    @SuppressWarnings("unchecked")
                List<Element> tmp = searches.getChild("search").getChildren();
				eltSearches = tmp;
			}
		}
		
		

	}

    /**
     * called when an entry has changed and variables must be updated.
     *
     * @param node
     * @throws BadInputEx
     */
	public void update(Element node) throws BadInputEx {
		super.update(node);
		
		Element site     = node.getChild("site");
		Element searches = node.getChild("searches");
		
		capabUrl = Util.getParam(site, "capabilitiesUrl", capabUrl);
        rejectDuplicateResource = Util.getParam(site, "rejectDuplicateResource",  rejectDuplicateResource);
        
        try {
            capabUrl = URLDecoder.decode(capabUrl, Jeeves.ENCODING);
        }
        catch (UnsupportedEncodingException x) {
            System.out.println(x.getMessage());
            x.printStackTrace();
            // TODO should not swallow
        }
        queryScope = Util.getParam(site, "queryScope", queryScope);
        hopCount = Util.getParam(site, "hopCount", hopCount);

		icon     = Util.getParam(site, "icon",            icon);

		//--- if some search queries are given, we drop the previous ones and
		//--- set these new ones

		if (searches != null){
			addSearches(searches);
			
			if (searches.getChild("search")!=null){
			    @SuppressWarnings("unchecked")
                List<Element> tmp = searches.getChild("search").getChildren(); 
			    eltSearches = tmp;
			}
		}

	}

	//---------------------------------------------------------------------------
	//---
	//--- Other API methods
	//---
	//---------------------------------------------------------------------------

    /**
     *
     * @return
     */
	public Iterable<Search> getSearches() {
        return alSearches;
    }
	
	//public Iterable<Element> getSearchElements() { return eltSearches; }

    /**
     *
     * @return
     */
	public boolean isSearchEmpty() {
        return alSearches.isEmpty();
    }

    /**
     *
     * @return
     */
	public CswParams copy() {
		CswParams copy = new CswParams(dm);
		copyTo(copy);

		copy.capabUrl = capabUrl;
		copy.icon     = icon;
		copy.rejectDuplicateResource = rejectDuplicateResource;
	 	copy.queryScope = queryScope;
	 	copy.hopCount = hopCount;

		for (Search s : alSearches)
			copy.alSearches.add(s.copy());
		
		copy.eltSearches = eltSearches;

		return copy;
	}

	//---------------------------------------------------------------------------
	//---
	//--- Private methods
	//---
	//---------------------------------------------------------------------------

    /**
     *
     * @param searches
     */
	private void addSearches(Element searches) {
		alSearches.clear();

		if (searches == null) {
			return;
        }

        for (Object o : searches.getChildren("search")) {
            Element search = (Element) o;

            alSearches.add(new Search(search));
        }
	}

	//---------------------------------------------------------------------------
	//---
	//--- Variables
	//---
	//---------------------------------------------------------------------------

	public String capabUrl;
	public String icon;
    public boolean rejectDuplicateResource;
    public String queryScope;
    public Integer hopCount;

	private List<Search> alSearches = new ArrayList<Search>();	
	public List<Element> eltSearches = new ArrayList<Element>();
	
}