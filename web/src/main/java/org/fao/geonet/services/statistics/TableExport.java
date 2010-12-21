package org.fao.geonet.services.statistics;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.ResultSetMetaData;

import jeeves.constants.Jeeves;
import jeeves.interfaces.Service;
import jeeves.resources.dbms.Dbms;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import jeeves.utils.Log;
import jeeves.utils.Util;

import jeeves.utils.Xml;
import org.fao.geonet.constants.Geonet;
import org.jdom.Element;

/**
 * Jeeves service to export a Database table (based on its given name) into the configured file format
 * (currently, only CSV supported, full fields dump for the given table)
 * @author nicolas ribot
 */
public class TableExport implements Service {
    /** constant for CSV file export */
	public final static String CSV = "CSV";

    private String currentExportFormat;
    /** the full path to the application directory */
    private  String appPath;
    /** the separator for CSV format
     * fixme: add a string quotation parameter
     */
    private String csvSep = ",";
    /** true to dump headers, false to dump only data */
    private boolean dumpHeader = true;


    //--------------------------------------------------------------------------
	//---
	//--- Init
	//---
	//--------------------------------------------------------------------------
	public void init(String appPath, ServiceConfig params) throws Exception	{
		this.currentExportFormat = params.getValue("exportType");
		this.csvSep = params.getValue("csvSeparator");
		this.dumpHeader = "true".equalsIgnoreCase(params.getValue("dumpHeader"));
        this.appPath = appPath;
    }

    //--------------------------------------------------------------------------
	//---
	//--- Service
	//---
	//--------------------------------------------------------------------------
    /** Physically dumps the given table, writing it to the App tmp folder,
     * returning the URL of the file to get.
     */
	public Element exec(Element params, ServiceContext context) throws Exception {
        String tableToExport = Util.getParam(params, "tableToExport");

        if (tableToExport == null ) {
            Log.debug(Geonet.SEARCH_LOGGER,"Export Statistics table: no table name received from the client.");
        }
        // file to write
		File tableDumpFile = new File(appPath + File.separator + "images" + File.separator + "statTmp");
		if (!tableDumpFile.exists()) {
			tableDumpFile.mkdirs();
		}
        String dumpFileName = tableToExport + "_" + context.getUserSession().getUserId() + ".csv";
        tableDumpFile = new File(tableDumpFile.getAbsolutePath(), dumpFileName);
        Log.debug(Geonet.SEARCH_LOGGER,"Export Statistics table: will dump CSV to file: " + tableDumpFile);

        // sql stuff
        String query = "select * from " + tableToExport;
        Log.debug(Geonet.SEARCH_LOGGER,"Export Statistics table: query to get table:\n" + query);
		Dbms dbms = (Dbms) context.getResourceManager().open(Geonet.Res.MAIN_DB);
        // use connection by hand, to allow us to control the resultset and avoid Java Heap Space Exception
        Connection con = dbms.getConnection();
        Statement stmt = con.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
        ResultSet rs = stmt.executeQuery(query);
        BufferedWriter out = new BufferedWriter(new FileWriter(tableDumpFile));
        ResultSetMetaData rsMetaData = rs.getMetaData();

        if (this.dumpHeader) {
            StringBuilder headers = new StringBuilder();
            for (int i = 1; i <= rsMetaData.getColumnCount(); i++) {
                headers.append(rsMetaData.getColumnName(i)).append(this.csvSep);
            }
            //removes trailing separator
            headers.deleteCharAt(headers.length()-1);
            out.write(headers.toString());
            out.newLine();
        }
        StringBuilder line = null;
        Log.debug(Geonet.SEARCH_LOGGER,"Export Statistics table: headers written, writting data");
        while (rs.next()) {
            line = new StringBuilder();
            for (int i = 1; i <= rsMetaData.getColumnCount(); i++) {
                line.append(rs.getString(i)).append(this.csvSep);
            }
            line.deleteCharAt(line.length()-1);
            out.write(line.toString());
            out.newLine();
        }
        Log.debug(Geonet.SEARCH_LOGGER,"data written");
        rs.close();
        stmt.close();
        out.flush();
        out.close();
        //dbms.disconnect();
        Log.debug(Geonet.SEARCH_LOGGER,"streams closed");

		Element elResp = new Element(Jeeves.Elem.RESPONSE);
		Element elFileUrl = new Element("fileURL").setText(context.getBaseUrl() +
				"/images/statTmp/" + dumpFileName);
		Element elExportedtable = new Element("exportedTable").setText(tableToExport);
		elResp.addContent(elFileUrl);
		elResp.addContent(elExportedtable);
        System.out.println(Xml.getString(elResp));
		return elResp;
    }
}
