package org.fao.geonet.services;

import jeeves.exceptions.MissingParameterEx;
import jeeves.resources.dbms.Dbms;
import jeeves.server.context.ServiceContext;
import jeeves.utils.Util;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.constants.Params;
import org.fao.geonet.kernel.DataManager;
import org.jdom.Element;

public class Utils {

	/**
	 * Search for a UUID or an internal identifier parameter and return an
	 * internal identifier using default UUID and identifier parameter names
	 * (ie. uuid and id).
	 * 
	 * @param params
	 *            The params to search ids in
	 * @param context
	 *            The service context
	 * @param uuidParamName		UUID parameter name
	 * @param uuidParamName		Id parameter name
	 *  
	 * @return
	 * @throws Exception
	 */
	public static String getIdentifierFromParameters(Element params,
			ServiceContext context, String uuidParamName, String idParamName)
			throws Exception {

		// the metadata ID
		String id;
		GeonetContext gc = (GeonetContext) context
				.getHandlerContext(Geonet.CONTEXT_NAME);
		DataManager dm = gc.getDataManager();

		// does the request contain a UUID ?
		try {
			String uuid = Util.getParam(params, uuidParamName);
			// lookup ID by UUID
            Dbms dbms = (Dbms) context.getResourceManager().open(Geonet.Res.MAIN_DB);
			id = dm.getMetadataId(dbms, uuid);
		}
        catch (MissingParameterEx x) {
			// request does not contain UUID; use ID from request
			try {
				id = Util.getParam(params, idParamName);
			} catch (MissingParameterEx xx) {
				// request does not contain ID
				// give up
				throw new Exception("Request must contain a UUID ("
						+ uuidParamName + ") or an ID (" + idParamName + ")");
			}
		}
		return id;
	}

    /**
	 * Search for a UUID or an internal identifier parameter and return an
	 * internal identifier using default UUID and identifier parameter names
	 * (ie. uuid and id).
	 * 
	 * @param params
	 *            The params to search ids in
	 * @param context
	 *            The service context
	 * @return
	 * @throws Exception
	 */
	public static String getIdentifierFromParameters(Element params,
			ServiceContext context) throws Exception {
		return getIdentifierFromParameters(params, context, Params.UUID, Params.ID);
	}
}
