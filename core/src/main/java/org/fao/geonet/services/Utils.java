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

package org.fao.geonet.services;

import java.io.IOException;
import java.util.Collections;
import java.util.Set;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.Util;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.constants.Params;
import org.fao.geonet.domain.AbstractMetadata;
import org.fao.geonet.exceptions.MissingParameterEx;
import org.fao.geonet.kernel.AccessManager;
import org.fao.geonet.kernel.datamanager.IMetadataUtils;
import org.fao.geonet.kernel.search.IndexAndTaxonomy;
import org.fao.geonet.kernel.search.SearchManager;
import org.fao.geonet.kernel.search.index.GeonetworkMultiReader;
import org.fao.geonet.repository.MetadataDraftRepository;
import org.jdom.Element;

import jeeves.server.context.ServiceContext;

public class Utils {

    /**
     * Search for a UUID or an internal identifier parameter and return an internal identifier using
     * default UUID and identifier parameter names (ie. uuid and id).
     *
     * @param params        The params to search ids in
     * @param context       The service context
     * @param uuidParamName UUID parameter name
     * @param uuidParamName Id parameter name
     */
    public static String getIdentifierFromParameters(Element params,
                                                     ServiceContext context, String uuidParamName, String idParamName)
        throws Exception {

        // the metadata ID
        String id;
        GeonetContext gc = (GeonetContext) context
            .getHandlerContext(Geonet.CONTEXT_NAME);
        IMetadataUtils dm = gc.getBean(IMetadataUtils.class);

        id = lookupByFileId(params, gc);
        if (id == null) {
            // does the request contain a UUID ?
            try {
                String uuid = Util.getParam(params, uuidParamName);
                // lookup ID by UUID
                id = dm.getMetadataId(uuid);
                
                //Do we want the draft version?
                Boolean approved = Util.getParam(params, "approved", true);
                if(!approved) {
                    //Is the user editor for this metadata?
                	AccessManager am = context.getBean(AccessManager.class);
                	if(am.canEdit(context, id)) {
                		AbstractMetadata draft = gc.getBean(MetadataDraftRepository.class).findOneByUuid(uuid);
                		if(draft != null) {
                			id = String.valueOf(draft.getId());
                		}	
                	}
                }
                
                
            } catch (MissingParameterEx x) {
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
        }
        return id;
    }

    private static String lookupByFileId(Element params, GeonetContext gc) throws Exception {
        String fileId = Util.getParam(params, "fileIdentifier", null);
        if (fileId == null) {
            return null;
        }

        return lookupMetadataIdFromFileId(gc, fileId);
    }

    public static String lookupMetadataIdFromFileId(GeonetContext gc, String fileId) throws IOException,
        InterruptedException {
        SearchManager searchManager = gc.getBean(SearchManager.class);

        return lookupMetadataIdFromFileId(fileId, searchManager);
    }

    public static String lookupMetadataIdFromFileId(String fileId, SearchManager searchManager) throws IOException, InterruptedException {
        TermQuery query = new TermQuery(new Term("fileId", fileId));

        IndexAndTaxonomy indexAndTaxonomy = searchManager.getIndexReader(null, -1);
        GeonetworkMultiReader reader = indexAndTaxonomy.indexReader;

        try {
            IndexSearcher searcher = new IndexSearcher(reader);
            TopDocs tdocs = searcher.search(query, 1);

            if (tdocs.totalHits > 0) {

                Set<String> id = Collections.singleton("_id");
                Document element = reader.document(tdocs.scoreDocs[0].doc, id);
                return element.get("_id");
            }

            return null;
        } finally {
            searchManager.releaseIndexReader(indexAndTaxonomy);
        }
    }

    /**
     * Search for a UUID or an internal identifier parameter and return an internal identifier using
     * default UUID and identifier parameter names (ie. uuid and id).
     *
     * @param params  The params to search ids in
     * @param context The service context
     */
    public static String getIdentifierFromParameters(Element params,
                                                     ServiceContext context) throws Exception {
        return getIdentifierFromParameters(params, context, Params.UUID, Params.ID);
    }

}
