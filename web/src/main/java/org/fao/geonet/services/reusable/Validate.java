//==============================================================================
//===	Copyright (C) 2001-2008 Food and Agriculture Organization of the
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

package org.fao.geonet.services.reusable;

import com.google.common.base.Function;
import jeeves.interfaces.Service;
import jeeves.resources.dbms.Dbms;
import jeeves.server.ServiceConfig;
import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;
import jeeves.utils.Log;
import jeeves.utils.Util;
import jeeves.xlink.Processor;
import jeeves.xlink.XLink;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geocat;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.reusable.MetadataRecord;
import org.fao.geonet.kernel.reusable.ReplacementStrategy;
import org.fao.geonet.kernel.reusable.ReusableTypes;
import org.fao.geonet.kernel.reusable.Utils;
import org.jdom.Element;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Makes a list of all the non-validated elements
 * 
 * @author jeichar
 */
public class Validate implements Service
{

    public Element exec(Element params, ServiceContext context) throws Exception
    {
        String page = Util.getParamText(params, "type");
        String[] ids = Util.getParamText(params, "id").split(",");

        Log.debug(Geocat.Module.REUSABLE, "Starting to validate following reusable objects: " + page
                + " \n(" + Arrays.toString(ids) + ")");

        GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
        Dbms dbms = (Dbms) context.getResourceManager().open(Geonet.Res.MAIN_DB);
        String baseUrl = Utils.mkBaseURL(context.getBaseUrl(), gc.getSettingManager());
        ReplacementStrategy strategy = Utils.strategy(ReusableTypes.valueOf(page), context);

        Element results = new Element("results");
        if (strategy != null) {
            results.addContent(performValidation(ids, strategy, dbms, context, baseUrl));
        }

        Log.info(Geocat.Module.REUSABLE, "Successfully validated following reusable objects: " + page
                + " \n(" + Arrays.toString(ids) + ")");

        return results;
    }

    private List<Element> performValidation(String[] ids, ReplacementStrategy strategy, Dbms dbms, ServiceContext context,
            String baseUrl) throws Exception
    {
        Map<String, String> idMapping = strategy.markAsValidated(ids, dbms, context.getUserSession());

        List<Element> result = new ArrayList<Element>();
        for (String id : ids) {
            Element e = updateXLink(dbms, strategy, context, idMapping, id);
            result.add(e);
        }

        return result;
    }

    public void init(String appPath, ServiceConfig params) throws Exception
    {
    }

	private Element updateXLink(Dbms dbms, ReplacementStrategy strategy, ServiceContext context, Map<String, String> idMapping, String id) throws Exception {
	
	    UserSession session = context.getUserSession();
	    List<String> luceneFields = new LinkedList<String>();
	    luceneFields.addAll(Arrays.asList(strategy.getInvalidXlinkLuceneField()));

	    final Function<String, String> idConverter = strategy.numericIdToConcreteId(session);
	    Set<MetadataRecord> results = Utils.getReferencingMetadata(context, strategy, luceneFields, id, false, true, idConverter);
	
	    for( MetadataRecord metadataRecord : results ) {
	
	        for( String xlinkHref : metadataRecord.xlinks ) {
	
	            @SuppressWarnings("unchecked")
	            Iterator<Element> xlinks = metadataRecord.xml.getDescendants(new Utils.FindXLinks(xlinkHref));
	            while( xlinks.hasNext() ) {
	                Element xlink = xlinks.next();
	                xlink.removeAttribute(XLink.ROLE, XLink.NAMESPACE_XLINK);
	
	                String oldHref = xlink.getAttributeValue(XLink.HREF, XLink.NAMESPACE_XLINK);
	                String newId = idMapping.get(id);
	                if (newId == null) {
	                    newId = id;
	                }
	                String validateHRef = strategy.updateHrefId(oldHref, newId, session);
                    xlink.setAttribute(XLink.HREF, validateHRef, XLink.NAMESPACE_XLINK);
	            }
	
	        }
	    }

        Processor.clearCache();
        for (MetadataRecord metadataRecord : results) {
            metadataRecord.commit(dbms, context);
        }

        Element e = new Element("id");
	    e.setText(id);
	    return e;
	}

}
