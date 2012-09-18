//=============================================================================
//===	Copyright (C) 2001-2009 Food and Agriculture Organization of the
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
package org.fao.geonet.arcgis;

import com.esri.sde.sdk.client.SeError;
import com.esri.sde.sdk.client.SeException;
import com.esri.sde.sdk.client.SeQuery;
import com.esri.sde.sdk.client.SeRow;
import com.esri.sde.sdk.client.SeSqlConstruct;
import jeeves.utils.Log;
import org.fao.geonet.constants.Geonet;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Adapter to retrieve ISO metadata from an ArcSDE server. The metadata in ArcSDE is scanned for
 * "MD_Metadata" and those that match are included in the result unprocessed, so including any
 * non-ISO ESRI elements they may contain.
 * 
 * @author heikki doeleman
 *
 */
public class ArcSDEMetadataAdapter extends ArcSDEConnection {

	public ArcSDEMetadataAdapter(String server, int instance, String database, String username, String password) {
		super(server, instance, database, username, password);
	}
	
	private static final String METADATA_TABLE = "SDE.GDB_USERMETADATA";
	private static final String METADATA_COLUMN = "SDE.GDB_USERMETADATA.XML";
	private static final String ISO_METADATA_IDENTIFIER = "MD_Metadata";
	
	public List<String> retrieveMetadata() throws Exception {
        Log.info(Geonet.ARCSDE, "start retrieve metadata");
		List<String> results = new ArrayList<String>();
		try {	
			// query table containing XML metadata
			SeSqlConstruct sqlConstruct = new SeSqlConstruct();
			String[] tables = {METADATA_TABLE };
			sqlConstruct.setTables(tables);
			String[] propertyNames = { METADATA_COLUMN };			
			SeQuery query = new SeQuery(seConnection);
			query.prepareQuery(propertyNames, sqlConstruct);
			query.execute();
			
			// it is not documented in the ArcSDE API how you know there are no more rows to fetch!
			// I'm assuming: query.fetch returns null (empiric tests indicate this assumption is correct).
			boolean allRowsFetched = false;
			while(! allRowsFetched) {
				SeRow row = query.fetch();
				if(row != null) {
					ByteArrayInputStream bytes = row.getBlob(0);
					byte [] buff = new byte[bytes.available()];
					bytes.read(buff);
					String document = new String(buff);
					if(document.contains(ISO_METADATA_IDENTIFIER)) {
                        Log.debug(Geonet.ARCSDE, "ISO metadata found");
						results.add(document);
					}
				}
				else {
					allRowsFetched = true;
				}
			}			
			query.close();
            Log.info(Geonet.ARCSDE, "cool");
			return results;
		}
		catch(SeException x) {
			SeError error = x.getSeError();
			String description = error.getExtError() + " " + error.getExtErrMsg() + " " + error.getErrDesc();
            Log.error(Geonet.ARCSDE, description);
			x.printStackTrace();
			throw new Exception(x);
		}
	}
}
