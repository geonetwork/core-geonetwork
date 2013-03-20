package org.fao.geonet.services.statistics;


import jeeves.interfaces.Service;
import jeeves.resources.dbms.Dbms;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import jeeves.utils.Log;

import org.fao.geonet.constants.Geonet;
import org.fao.geonet.services.NotInReadOnlyModeService;
import org.jdom.Element;

/**
 * Jeeves service to select the Metadata popularity from database. made as a java service
 * to allow passing limit parameter to the UI part
 * @author nicolas ribot
 */
public class MdPopularity extends NotInReadOnlyModeService{
    /** the max number of results to display */
    private int limit = 25;

    /** the SQL query */
    private String query;


    //--------------------------------------------------------------------------
	//---
	//--- Init
	//---
	//--------------------------------------------------------------------------
	public void init(String appPath, ServiceConfig params) throws Exception	{
        super.init(appPath, params);
		this.limit = Integer.parseInt(params.getValue("limit"));
		this.query = params.getValue("query");
    }

    //--------------------------------------------------------------------------
	//---
	//--- Service
	//---
	//--------------------------------------------------------------------------
    /** Physically dumps the given table, writing it to the App tmp folder,
     * returning the URL of the file to get.
     */
    @Override
	public Element serviceSpecificExec(Element params, ServiceContext context) throws Exception {
        boolean readOnlyMode = super.exec(params, context) == null;
        if(readOnlyMode) {
            return null;
        }
		Dbms dbms = (Dbms) context.getResourceManager().open(Geonet.Res.MAIN_DB);
        if(Log.isDebugEnabled(Geonet.SEARCH_LOGGER))
            Log.debug(Geonet.SEARCH_LOGGER, "query to get MD popularity: " + query);
		Element response = dbms.select(query);
		Element elLimit = new Element("limit").setText("" + limit);
		response.addContent(elLimit);
		return response;
    }
}
