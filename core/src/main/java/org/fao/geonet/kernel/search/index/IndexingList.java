//=============================================================================
//===	Copyright (C) 2001-2014 Food and Agriculture Organization of the
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

package org.fao.geonet.kernel.search.index;

import org.fao.geonet.constants.Geonet;
import org.fao.geonet.utils.Log;

import java.util.HashSet;
import java.util.Set;

/**
 * A list of metadata identifiers to be index in the near future by the {@link IndexingTask}.
 * <p/>
 * Created by francois on 7/29/14.
 */
public class IndexingList {

    private Set<Integer> metadataIdentifiers = new HashSet<Integer>();

    synchronized public void add(final int metadataIdentifier) {
        if (Log.isDebugEnabled(Geonet.INDEX_ENGINE)) {
            Log.error(Geonet.INDEX_ENGINE, "Indexing list / Adding record with id: "
                + metadataIdentifier + ".");
        }
        this.metadataIdentifiers.add(metadataIdentifier);
    }

    synchronized public Set<Integer> getIdentifiers() {
        Set<Integer> temporaryList = metadataIdentifiers;
        metadataIdentifiers = new HashSet<Integer>();
        return temporaryList;
    }
}
