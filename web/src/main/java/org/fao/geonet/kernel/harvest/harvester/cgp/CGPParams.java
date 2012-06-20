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

package org.fao.geonet.kernel.harvest.harvester.cgp;

import jeeves.exceptions.BadInputEx;
import jeeves.utils.Util;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.harvest.harvester.AbstractParams;
import org.jdom.Element;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

//=============================================================================

public class CGPParams extends AbstractParams
{
	//--------------------------------------------------------------------------
	//---
	//--- Constructor
	//---
	//--------------------------------------------------------------------------

	public CGPParams(DataManager dm)
	{
		super(dm);
	}

	//---------------------------------------------------------------------------
	//---
	//--- Create : called when a new entry must be added. Reads values from the
	//---          provided entry, providing default values
	//---
	//---------------------------------------------------------------------------

	public void create(Element node) throws BadInputEx
	{
		super.create(node);

		Element site = node.getChild("site");
		Element searches = node.getChild("searches");
		Element options  = node.getChild("options");

		url = Util.getParam(site, "url", "");
		icon = Util.getParam(site, "icon", "");
		validate = Util.getParam(options, "validate", false);

		addSearches(searches);
	}

	//---------------------------------------------------------------------------
	//---
	//--- Update : called when an entry has changed and variables must be updated
	//---
	//---------------------------------------------------------------------------

	public void update(Element node) throws BadInputEx
	{
		super.update(node);

		Element site = node.getChild("site");
		Element searches = node.getChild("searches");
		Element options = node.getChild("options");

		url = Util.getParam(site, "url", url);
		icon = Util.getParam(site, "icon", icon);
		validate = Util.getParam(options, "validate", validate);

		//--- if some search queries are given, we drop the previous ones and
		//--- set these new ones

		if (searches != null)
		{
			addSearches(searches);
		}
	}


	//---------------------------------------------------------------------------
	//---
	//--- Other API methods
	//---
	//---------------------------------------------------------------------------

	public Iterable<Search> getSearches()
	{
		return alSearches;
	}

	//---------------------------------------------------------------------------

	public boolean isSearchEmpty()
	{
		return alSearches.isEmpty();
	}

	//---------------------------------------------------------------------------
	//---
	//--- Other API methods
	//---
	//---------------------------------------------------------------------------

	public CGPParams copy()
	{
		CGPParams copy = new CGPParams(dm);
		copyTo(copy);

		copy.url = url;
		copy.icon = icon;
		copy.validate = validate;

		for (Search s : alSearches)
		{
			copy.alSearches.add(s.copy());
		}

		return copy;
	}


	//---------------------------------------------------------------------------
	//---
	//--- Private methods
	//---
	//---------------------------------------------------------------------------

	private void addSearches(Element searches) throws BadInputEx
	{
		alSearches.clear();

		if (searches == null)
		{
			return;
		}

		Iterator searchList = searches.getChildren("search").iterator();

		while (searchList.hasNext())
		{
			Element search = (Element) searchList.next();

			alSearches.add(new Search(search));
		}
	}

	//---------------------------------------------------------------------------
	//---
	//--- Variables
	//---
	//---------------------------------------------------------------------------

	public String url;
	public String icon;
	public boolean validate;
	private List<Search> alSearches = new ArrayList<Search>(2);
}

//=============================================================================


