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

package org.fao.geonet.kernel.search;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.fao.geonet.GeonetContext;
import org.fao.geonet.Util;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.AbstractMetadata;
import org.fao.geonet.domain.ISODate;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.domain.MetadataType;
import org.fao.geonet.domain.OperationAllowed;
import org.fao.geonet.domain.ReservedGroup;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.datamanager.IMetadataUtils;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.repository.OperationAllowedRepository;
import org.fao.geonet.repository.specification.MetadataSpecs;
import org.fao.geonet.repository.specification.OperationAllowedSpecs;
import org.jdom.Document;
import org.jdom.Element;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.domain.Specifications;

import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;

//==============================================================================

class UnusedSearcher extends MetaSearcher {
    private ArrayList<String> alResult;
    private Element elSummary;

    //--------------------------------------------------------------------------
    //---
    //--- Constructor
    //---
    //--------------------------------------------------------------------------

    public UnusedSearcher() {
    }

    //--------------------------------------------------------------------------
    //---
    //--- MetaSearcher Interface
    //---
    //--------------------------------------------------------------------------

    public void search(ServiceContext context, Element request,
                       ServiceConfig config) throws Exception {
        GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
        SettingManager sm = gc.getBean(SettingManager.class);

        String siteId = sm.getSiteId();

        alResult = new ArrayList<String>();

        //--- get maximun delta in minutes

        int maxDiff = Integer.parseInt(Util.getParam(request, "maxDiff", "5"));

        context.info("UnusedSearcher : using maxDiff=" + maxDiff);

        //--- proper search
        final Specifications<Metadata> spec = 
        		Specifications.where((Specification<Metadata>)MetadataSpecs.isType(MetadataType.TEMPLATE))
        		.and((Specification<Metadata>)MetadataSpecs.isHarvested(false))
        		.and((Specification<Metadata>)MetadataSpecs.hasSource(siteId));

        final List<? extends AbstractMetadata> list = context.getBean(IMetadataUtils.class).findAll(spec);

        for (AbstractMetadata rec : list) {
            int id = rec.getId();

            ISODate createDate = rec.getDataInfo().getCreateDate();
            ISODate changeDate = rec.getDataInfo().getChangeDate();

            if (changeDate.timeDifferenceInSeconds(createDate) / 60 < maxDiff) {
                if (!hasInternetGroup(context, id)) {
                    alResult.add("" + id);
                }
            }
        }

        //--- build summary

        makeSummary();

        initSearchRange(context);
    }

    //--------------------------------------------------------------------------------

    public List<Document> presentDocuments(ServiceContext srvContext, Element request, ServiceConfig config) throws Exception {
        throw new UnsupportedOperationException("Not supported by Unused searcher");
    }

    //--------------------------------------------------------------------------

    public Element present(ServiceContext srvContext, Element request,
                           ServiceConfig config) throws Exception {
        updateSearchRange(request);

        GeonetContext gc = (GeonetContext) srvContext.getHandlerContext(Geonet.CONTEXT_NAME);

        //--- build response

        Element response = new Element("response");
        response.setAttribute("from", getFrom() + "");
        response.setAttribute("to", getTo() + "");

        response.addContent((Element) elSummary.clone());

        if (getTo() > 0) {
            for (int i = getFrom() - 1; i < getTo(); i++) {
                String id = alResult.get(i);
                boolean forEditing = false, withValidationErrors = false, keepXlinkAttributes = false;
                Element md = gc.getBean(DataManager.class).getMetadata(srvContext, id, forEditing, withValidationErrors, keepXlinkAttributes);
                response.addContent(md);
            }
        }

        return response;
    }

    //--------------------------------------------------------------------------

    public int getSize() {
        return alResult.size();
    }

    //--------------------------------------------------------------------------

    public Element getSummary() throws Exception {
        Element response = new Element("response");
        response.addContent((Element) elSummary.clone());

        return response;
    }

    //--------------------------------------------------------------------------

    public void close() {
    }

    //--------------------------------------------------------------------------
    //---
    //--- Private methods
    //---
    //--------------------------------------------------------------------------

    private boolean hasInternetGroup(ServiceContext context, int id) throws SQLException {
        OperationAllowedRepository operationAllowedRepository = context.getBean(OperationAllowedRepository.class);

        final Specification<OperationAllowed> hasGroupId = OperationAllowedSpecs.hasGroupId(ReservedGroup.all.getId());
        final Specification<OperationAllowed> hasMetadataId = OperationAllowedSpecs.hasMetadataId(id);
        final Specifications<OperationAllowed> spec = Specifications.where(hasGroupId).and(hasMetadataId);
        List<OperationAllowed> opsAllowed = operationAllowedRepository.findAll(spec);
        return !opsAllowed.isEmpty();
    }

    //--------------------------------------------------------------------------

    private void makeSummary() throws Exception {
        elSummary = new Element("summary");

        elSummary.setAttribute("count", getSize() + "");
        elSummary.setAttribute("type", "local");

        Element elKeywords = new Element("keywords");
        elSummary.addContent(elKeywords);

        Element elCategories = new Element("categories");
        elSummary.addContent(elCategories);
    }
    
    
    public long getVersionToken() {
    	return -1;
    };    
}

//==============================================================================


