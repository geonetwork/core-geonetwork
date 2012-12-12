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

package org.fao.geonet.services.util;

import jeeves.resources.dbms.Dbms;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.MetadataIndexerProcessor;
import org.jdom.Element;

import java.util.List;

/**
 * Class that extends MetadataIndexerProcessor to reindex the metadata
 * changed in any service that processes a list of metadata documents (as JDOM 
 * Elements)
 */
public class ServiceMetadataReindexer extends MetadataIndexerProcessor {
    List<Element> reindex;
		Dbms dbms;

    public ServiceMetadataReindexer(DataManager dm, Dbms dbms, List<Element> reindex) {
        super(dm);
				this.dbms = dbms;
				this.reindex = reindex;
    }

    @Override
    public void process() throws Exception {
			for (Element md : reindex) {
				String  mdId = md.getChildText("metadataid");
				dm.indexMetadata(dbms, mdId);
			}
    }
}
