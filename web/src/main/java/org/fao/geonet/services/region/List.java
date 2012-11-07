//=============================================================================
//===	Copyright (C) 2001-2007 Food and Agriculture Organization of the
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

package org.fao.geonet.services.region;

import java.util.Collection;
import java.util.Map;

import jeeves.constants.Jeeves;
import jeeves.interfaces.Service;
import jeeves.resources.dbms.Dbms;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import jeeves.utils.Util;

import org.fao.geonet.constants.Geonet;
import org.fao.geonet.constants.Params;
import org.fao.geonet.lib.Lib;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.jdom.Element;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.TransformException;

import com.vividsolutions.jts.geom.Envelope;

//=============================================================================

/** Returns a specific region and coordinates given its id
  */

public class List implements Service
{
	public static final String REGION_EL = "region";
    static final String REGIONS_EL = "regions";
    static final String ID_ATT = "id";
    static final String HAS_GEOM_ATT = "hasGeom";
    static final String CATEGORY_ID_ATT = "categoryId";
    static final String COUNT_ATT = "count";
    static final String NORTH_EL = "north";
    static final String SOUTH_EL = "south";
    static final String EAST_EL = "east";
    static final String WEST_EL = "west";
    private static final String LABEL_EL = "label";
    private static final String CATEGORY_EL = "categoryLabel";

    public void init(String appPath, ServiceConfig params) throws Exception {}

	//--------------------------------------------------------------------------
	//---
	//--- Service
	//---
	//--------------------------------------------------------------------------

	public Element exec(Element params, ServiceContext context) throws Exception
	{
		RegionsDAO dao = context.getApplicationContext().getBean(RegionsDAO.class);
		String labelParam = Util.getParam(params, RegionParams.LABEL_SEARCH, null);
		String categoryIdParam = Util.getParam(params, RegionParams.CATEGORY_SEARCH, null);
		int maxRecordsParam = Util.getParam(params, RegionParams.MAX_RECORDS, -1);
		
		Request request = dao.createSearchRequest(context);
		if(labelParam != null) { request.label(labelParam); }
		if(categoryIdParam != null) { request.categoryId(categoryIdParam); }
		if(maxRecordsParam > 0) { request.maxRecords(maxRecordsParam); }
		
		Collection<Region> regions = request.execute();
		
		Element result = new Element(REGIONS_EL);
		result.setAttribute(COUNT_ATT, Integer.toString(regions.size()));
		for (Region region : regions) {
		    result.addContent(toElement(region));
        }
		
		return result;
	}

    static Element toElement(Region region) throws TransformException, FactoryException {
        Element regionEl = new Element(REGION_EL);
        
        regionEl.setAttribute(ID_ATT, region.getId());
        regionEl.setAttribute(CATEGORY_ID_ATT, region.getCategoryId());
        regionEl.setAttribute(HAS_GEOM_ATT, Boolean.toString(region.hasGeom()));
        
        ReferencedEnvelope bbox = region.getLatLongBBox();
        regionEl.addContent(new Element(NORTH_EL).setText(Double.toString(bbox.getMaxY())));
        regionEl.addContent(new Element(SOUTH_EL).setText(Double.toString(bbox.getMinY())));
        regionEl.addContent(new Element(WEST_EL).setText(Double.toString(bbox.getMinX())));
        regionEl.addContent(new Element(EAST_EL).setText(Double.toString(bbox.getMaxX())));
        
        Element labelEl = new Element(LABEL_EL);
        regionEl.addContent(labelEl);
        for (Map.Entry<String, String> entry : region.getLabels().entrySet()) {
            labelEl.addContent(new Element(entry.getKey()).setText(entry.getValue()));
        }

        Element categoryEl = new Element(CATEGORY_EL);
        regionEl.addContent(categoryEl);
        for (Map.Entry<String, String> entry : region.getCategoryLabels().entrySet()) {
            categoryEl.addContent(new Element(entry.getKey()).setText(entry.getValue()));
        }
        
        return regionEl;
    }
}

//=============================================================================

