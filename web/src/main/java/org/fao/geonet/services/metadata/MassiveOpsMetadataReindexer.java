//==============================================================================
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
//===	Contact: Jeroen Ticheler email: geonetwork@osgeo.org
//==============================================================================

package org.fao.geonet.services.metadata;

import jeeves.resources.dbms.Dbms;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.MetadataIndexerProcessor;

import java.util.Set;

/**
 * Class that extends MetadataIndexerProcessor to reindex the metadata
 * changed in any of the Massive operation services
 */
public class MassiveOpsMetadataReindexer extends MetadataIndexerProcessor {
    Set<Integer> metadata;
		Dbms dbms;

    public MassiveOpsMetadataReindexer(DataManager dm, Dbms dbms, Set<Integer> metadata) {
        super(dm);
				this.dbms = dbms;
        this.metadata = metadata;
    }

    @Override
    public void process() throws Exception {
			for (int mdId : metadata) {
				dm.indexMetadataGroup(dbms, Integer.toString(mdId));
			}
    }
}
