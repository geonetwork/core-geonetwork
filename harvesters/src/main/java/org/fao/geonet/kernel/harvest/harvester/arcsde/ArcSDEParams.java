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
package org.fao.geonet.kernel.harvest.harvester.arcsde;

import org.fao.geonet.Util;
import org.fao.geonet.exceptions.BadInputEx;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.harvest.harvester.AbstractParams;
import org.jdom.Element;

/**
 * @author heikki doeleman
 */
public class ArcSDEParams extends AbstractParams {

    /**
     * Name of the ArcSDE server.
     */
    public String server;
    /**
     * Port number to use for connecting to the ArcSDE server ("instance").
     */
    public int port;
    /**
     * Name of the ArcSDE database.
     */
    public String database;

    public String icon;
    /** The type of the connection */
    public ArcSDEConnectionType connectionType;

    /** The ArcSDE version */
    public String version;

    /** The ArcSDE database type */
    public String databaseType;

    @Override
    public String getIcon() {
        return icon;
    }

    public ArcSDEParams(DataManager dm) {
        super(dm);
    }

    /**

     * Create : called when a new entry must be added. Reads values from the provided
     * entry, providing default values.
     */
    public void create(Element node) throws BadInputEx {
        super.create(node);
        Element site = node.getChild("site");
        server = Util.getParam(site, "server", "");
        port = Util.getParam(site, "port", 0);
        setUsername(Util.getParam(site, "username", ""));
        setPassword(Util.getParam(site, "password", ""));
        database = Util.getParam(site, "database", "");
        icon = Util.getParam(site, "icon", "arcsde.gif");
        connectionType = ArcSDEConnectionType.valueOf(Util.getParam(site,
            "connectionType", ArcSDEConnectionType.ARCSDE.name()));
        version =  Util.getParam(site, "version", "9");
        databaseType = Util.getParam(site, "databaseType", "");
    }

    /**
     * Update : called when an entry has changed and variables must be updated.
     * @param node
     * @throws BadInputEx
     */
    public void update(Element node) throws BadInputEx {
        super.update(node);
        Element site = node.getChild("site");
        server = Util.getParam(site, "server", "");
        port = Util.getParam(site, "port", 5151);
        setUsername(Util.getParam(site, "username", ""));
        setPassword(Util.getParam(site, "password", ""));
        database = Util.getParam(site, "database", "");
        icon = Util.getParam(site, "icon", "arcsde.gif");
        connectionType = ArcSDEConnectionType.valueOf(Util.getParam(site,
            "connectionType", ArcSDEConnectionType.ARCSDE.name()));
        version =  Util.getParam(site, "version", "9");
        databaseType = Util.getParam(site, "databaseType", "");
    }

    //---------------------------------------------------------------------------
    //---
    //--- Private methods
    //---
    //---------------------------------------------------------------------------

    /**
     * Copy the values of the fields to a new instance.
     * @return a new instance with the same field values that the caller one.
     */
    public ArcSDEParams copy() {
        ArcSDEParams copy = new ArcSDEParams(dm);
        copyTo(copy);
        copy.icon = icon;
        copy.server = server;
        copy.port = port;
        copy.setUsername(getUsername());
        copy.setPassword(getPassword());
        copy.database = database;
        copy.connectionType = connectionType;
        copy.version = version;
        copy.databaseType = databaseType;
        return copy;
    }
}
