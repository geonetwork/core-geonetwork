/*
 * Copyright (C) 2001-2016 Food and Agriculture Organization of the
 * United Nations (FAO-UN), United Nations World Food Programme (WFP)
 * and United Nations Environment Programme (UNEP)
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or (at
 * your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
 *
 * Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
 * Rome - Italy. email: geonetwork@osgeo.org
 */

package org.fao.geonet.services.statistics;

import jeeves.constants.Jeeves;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;

import org.fao.geonet.Constants;
import org.fao.geonet.Util;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.exceptions.BadParameterEx;
import org.fao.geonet.services.NotInReadOnlyModeService;
import org.fao.geonet.utils.FilePathChecker;
import org.fao.geonet.utils.Log;
import org.jdom.Element;

import java.io.BufferedWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.Arrays;
import java.util.List;

import javax.sql.DataSource;

/**
 * Jeeves service to export a Database table (based on its given name) into the configured file
 * format (currently, only CSV supported, full fields dump for the given table)
 *
 * @author nicolas ribot
 */
public class TableExport extends NotInReadOnlyModeService {
    /**
     * constant for CSV file export
     */
    public final static String CSV = "CSV";

    /**
     * the full path to the application directory
     */
    private Path appPath;
    /**
     * the separator for CSV format fixme: add a string quotation parameter
     */
    private String csvSep = ",";
    /**
     * true to dump headers, false to dump only data
     */
    private boolean dumpHeader = true;

    /**
     * List of tables that can be exported
     **/

    private List<String> allowedTablesToExport;

    // --------------------------------------------------------------------------
    // ---
    // --- Init
    // ---
    // --------------------------------------------------------------------------
    public void init(Path appPath, ServiceConfig params) throws Exception {
        super.init(appPath, params);
        // this.currentExportFormat = params.getValue("exportType");
        this.csvSep = params.getValue("csvSeparator");
        this.dumpHeader = "true".equalsIgnoreCase(params.getValue("dumpHeader"));
        this.allowedTablesToExport = Arrays.asList(params.getValue("allowedTables").split(","));
        this.appPath = appPath;
    }

    // --------------------------------------------------------------------------
    // ---
    // --- Service
    // ---
    // --------------------------------------------------------------------------

    /**
     * Physically dumps the given table, writing it to the App tmp folder, returning the URL of the
     * file to get.
     */
    @Override
    public Element serviceSpecificExec(Element params, ServiceContext context) throws Exception {
        String tableToExport = Util.getParam(params, "tableToExport");

        if (tableToExport == null) {
            if (Log.isDebugEnabled(Geonet.SEARCH_LOGGER))
                Log.debug(Geonet.SEARCH_LOGGER, "Export Statistics table: no table name received from the client.");
        }

        if (!allowedTablesToExport.contains(tableToExport)) {
            throw new BadParameterEx("tableToExport", tableToExport);
        }

        // file to write
        Path tableDumpFile = appPath.resolve("images").resolve("statTmp");
        Files.createDirectories(tableDumpFile);

        FilePathChecker.verify(tableToExport);

        String dumpFileName = tableToExport + "_" + context.getUserSession().getUserId() + ".csv";
        tableDumpFile = tableDumpFile.toAbsolutePath().resolve(dumpFileName);

        if (Log.isDebugEnabled(Geonet.SEARCH_LOGGER))
            Log.debug(Geonet.SEARCH_LOGGER, "Export Statistics table: will dump CSV to file: " + tableDumpFile);

        // sql stuff
        String query = "select * from " + tableToExport;
        if (Log.isDebugEnabled(Geonet.SEARCH_LOGGER)) {
            Log.debug(Geonet.SEARCH_LOGGER, "Export Statistics table: query to get table:\n" + query);
        }

        // use connection by hand, to allow us to control the resultset and avoid Java Heap Space Exception
        try (Connection con = context.getBean(DataSource.class).getConnection();
             Statement stmt = con.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
             ResultSet rs = stmt.executeQuery(query);
             BufferedWriter out = Files.newBufferedWriter(tableDumpFile, Constants.CHARSET)) {
            ResultSetMetaData rsMetaData = rs.getMetaData();

            if (this.dumpHeader) {
                StringBuilder headers = new StringBuilder();
                for (int i = 1; i <= rsMetaData.getColumnCount(); i++) {
                    headers.append(rsMetaData.getColumnName(i)).append(this.csvSep);
                }
                // removes trailing separator
                headers.deleteCharAt(headers.length() - 1);
                out.write(headers.toString());
                out.newLine();
            }
            StringBuilder line;
            if (Log.isDebugEnabled(Geonet.SEARCH_LOGGER)) {
                Log.debug(Geonet.SEARCH_LOGGER, "Export Statistics table: headers written, writting data");
            }
            while (rs.next()) {
                line = new StringBuilder();
                for (int i = 1; i <= rsMetaData.getColumnCount(); i++) {
                    line.append(rs.getString(i)).append(this.csvSep);
                }
                line.deleteCharAt(line.length() - 1);
                out.write(line.toString());
                out.newLine();
            }
            out.flush();
            if (Log.isDebugEnabled(Geonet.SEARCH_LOGGER)) {
                Log.debug(Geonet.SEARCH_LOGGER, "data written");
            }
        }
        // dbms.disconnect();
        if (Log.isDebugEnabled(Geonet.SEARCH_LOGGER))
            Log.debug(Geonet.SEARCH_LOGGER, "streams closed");

        Element elResp = new Element(Jeeves.Elem.RESPONSE);
        Element elFileUrl = new Element("fileURL").setText(context.getBaseUrl() + "/images/statTmp/" + dumpFileName);
        Element elExportedtable = new Element("exportedTable").setText(tableToExport);
        elResp.addContent(elFileUrl);
        elResp.addContent(elExportedtable);
        return elResp;
    }
}
