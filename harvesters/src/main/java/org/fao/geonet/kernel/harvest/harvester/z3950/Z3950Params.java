//=============================================================================
//===	Copyright (C) 2009 Swisstopo
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

package org.fao.geonet.kernel.harvest.harvester.z3950;

import org.fao.geonet.Util;
import org.fao.geonet.exceptions.BadInputEx;
import org.fao.geonet.exceptions.MissingParameterEx;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.harvest.harvester.AbstractParams;
import org.jdom.Element;

import java.util.ArrayList;
import java.util.List;

//=============================================================================

public class Z3950Params extends AbstractParams {
	// --------------------------------------------------------------------------
	// ---
	// --- Constructor
	// ---
	// --------------------------------------------------------------------------

	public Z3950Params(DataManager dm) {
		super(dm);
	}

	// ---------------------------------------------------------------------------
	// ---
	// --- Create : called when a new entry must be added. Reads values from the
	// --- provided entry, providing default values
	// ---
	// ---------------------------------------------------------------------------

	public void create(Element node) throws BadInputEx {
		super.create(node);

		Element site = node.getChild("site");

		icon = Util.getParam(site, "icon", "default.gif");
		query = Util.getParam(site, "query", "");
		maximumHits = Util.getParam(site, "maximumHits", maximumHits);

		addRepositories(site.getChild("repositories"));
	}

	// ---------------------------------------------------------------------------
	// ---
	// --- Update : called when an entry has changed and variables must be
	// updated
	// ---
	// ---------------------------------------------------------------------------

	public void update(Element node) throws BadInputEx {
		super.update(node);

		Element site = node.getChild("site");

		icon = Util.getParam(site, "icon", icon);
		query = Util.getParam(site, "query", query);
		maximumHits = Util.getParam(site, "maximumHits", maximumHits);

		Element repos = site.getChild("repositories");
		if (repos != null) {
			addRepositories(repos);
		}
	}

	// ---------------------------------------------------------------------------

	public Z3950Params copy() {
		Z3950Params copy = new Z3950Params(dm);
		copyTo(copy);

		copy.icon = icon;
		copy.query = query;
		copy.maximumHits = maximumHits;

		for (String s : alRepositories) {
			copy.alRepositories.add(s);
		}

		return copy;
	}

	// ---------------------------------------------------------------------------

	private void addRepositories(Element repos) throws BadInputEx {
		alRepositories.clear();

		if (repos == null) return;

		@SuppressWarnings("unchecked")
        List<Element> repoList = repos.getChildren("repository");
        for (Element repoElem : repoList) {
            String repoId = repoElem.getAttributeValue("id");

            if (repoId == null)
                throw new MissingParameterEx("attribute:id", repoElem);

            alRepositories.add(repoId);
        }
	}

	//---------------------------------------------------------------------------

	public Iterable<String>     getRepositories() { return alRepositories; }

	//---------------------------------------------------------------------------
	//---
	//--- Variables
	//---
	//---------------------------------------------------------------------------

	public String icon;
	public ArrayList<String> alRepositories = new ArrayList<String>();
	public String query;
	public String maximumHits = "100000"; // default
}
