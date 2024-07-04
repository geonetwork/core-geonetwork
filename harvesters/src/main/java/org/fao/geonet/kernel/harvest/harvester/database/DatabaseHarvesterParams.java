//=============================================================================
//===	Copyright (C) 2001-2024 Food and Agriculture Organization of the
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

package org.fao.geonet.kernel.harvest.harvester.database;

import org.fao.geonet.Util;
import org.fao.geonet.exceptions.BadInputEx;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.harvest.harvester.AbstractParams;
import org.jdom.Element;

class DatabaseHarvesterParams extends AbstractParams {
    /**
     * Type of database
     */
    private String databaseType;

    /**
     * Name of the database server.
     */
    private String server;

    /**
     * Port number to use for connecting to the database server
     */
    private int port;

    /**
     * Name of the database.
     */
    private String database;

    /**
     * Name of the table with the metadata.
     */
    private String tableName;

    /**
     * Field name that contains the metadata.
     */
    private String metadataField;

    /**
     * Field name to filter the table.
     */
    private String filterField;

    /**
     * Value to filter the results.
     */
    private String filterValue;

    /**
     * The filter is a process (see schema/process folder) which depends on the schema. It could be
     * composed of parameter which will be sent to XSL transformation using the following syntax :
     * <pre>
     * anonymizer?protocol=MYLOCALNETWORK:FILEPATH&email=gis@organisation.org&thesaurus=MYORGONLYTHEASURUS
     * </pre>
     */
    private String xslfilter;

    private String icon;

    public String getDatabaseType() {
        return databaseType;
    }

    public void setDatabaseType(String databaseType) {
        this.databaseType = databaseType;
    }

    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getMetadataField() {
        return metadataField;
    }

    public void setMetadataField(String metadataField) {
        this.metadataField = metadataField;
    }

    public String getFilterField() {
        return filterField;
    }

    public void setFilterField(String filterField) {
        this.filterField = filterField;
    }

    public String getFilterValue() {
        return filterValue;
    }

    public void setFilterValue(String filterValue) {
        this.filterValue = filterValue;
    }

    public String getXslfilter() {
        return xslfilter;
    }

    public void setXslfilter(String xslfilter) {
        this.xslfilter = xslfilter;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    @Override
    public String getIcon() {
        return icon;
    }


    public DatabaseHarvesterParams(DataManager dm) {
        super(dm);
    }

    @Override
    public void create(Element node) throws BadInputEx {
        super.create(node);

        Element site = node.getChild("site");

        databaseType = Util.getParam(site, "databaseType", "");
        server = Util.getParam(site, "server", "");
        port = Util.getParam(site, "port", 0);
        database = Util.getParam(site, "database", "");
        tableName = Util.getParam(site, "tableName", "");
        metadataField = Util.getParam(site, "metadataField", "");
        xslfilter = Util.getParam(site, "xslfilter", "");

        Element filter = node.getChild("filter");
        filterField = Util.getParam(filter, "field", "");
        filterValue = Util.getParam(filter, "value", "");

        icon = Util.getParam(site, "icon", "");
    }

    @Override
    public void update(Element node) throws BadInputEx {
        super.update(node);

        Element site = node.getChild("site");

        databaseType = Util.getParam(site, "databaseType", "");
        server = Util.getParam(site, "server", "");
        port = Util.getParam(site, "port", 0);
        database = Util.getParam(site, "database", "");
        tableName = Util.getParam(site, "tableName", "");
        metadataField = Util.getParam(site, "metadataField", "");
        xslfilter = Util.getParam(site, "xslfilter", "");

        Element filter = node.getChild("filter");
        filterField = Util.getParam(filter, "field", "");
        filterValue = Util.getParam(filter, "value", "");

        icon = Util.getParam(site, "icon", icon);

    }

    @Override
    public DatabaseHarvesterParams copy() {
        DatabaseHarvesterParams copy = new DatabaseHarvesterParams(dm);
        copyTo(copy);

        copy.databaseType = databaseType;
        copy.server = server;
        copy.port = port;
        copy.database = database;
        copy.tableName = tableName;
        copy.metadataField = metadataField;
        copy.filterField = filterField;
        copy.filterValue = filterValue;
        copy.xslfilter = xslfilter;
        copy.icon = icon;

        copy.setValidate(getValidate());

        return copy;
    }
}
