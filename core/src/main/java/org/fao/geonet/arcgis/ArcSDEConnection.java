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

import com.esri.sde.sdk.client.SeConnection;
import com.esri.sde.sdk.client.SeError;
import com.esri.sde.sdk.client.SeException;

/**
 * Adapter for ArcSDE connections.
 *
 * See http://edndoc.esri.com/arcsde/9.3/api/japi/japi.htm for (very little) information about the
 * ArcSDE Java API.
 *
 * @author heikki doeleman
 */
public class ArcSDEConnection {

    protected SeConnection seConnection;

    /**
     * Opens a connection to the specified ArcSDE server.
     */
    public ArcSDEConnection(String server, int instance, String database, String username, String password) {
        try {
            seConnection = new SeConnection(server, instance, database, username, password);
            System.out.println("Connected to ArcSDE");
            seConnection.setConcurrency(SeConnection.SE_LOCK_POLICY);
        } catch (SeException x) {
            SeError error = x.getSeError();
            String description = error.getExtError() + " " + error.getExtErrMsg() + " " + error.getErrDesc();
            System.out.println(description);
            x.printStackTrace();
            throw new ExceptionInInitializerError(x);
        }
    }

    /**
     * Closes the connection to the ArcSDE server.
     */
    public void close() {
        try {
            seConnection.close();
        } catch (SeException x) {
            x.printStackTrace();
            // TODO handle exception
        }
    }

    /**
     * Closes the connection to ArcSDE server in case users of this class neglect to do so.
     */
    protected void finalize() throws Throwable {
        try {
            seConnection.close();
        } catch (Throwable x) {
            x.printStackTrace();
            throw x;
        } finally {
            super.finalize();
        }
    }
}
