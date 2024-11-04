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

package org.fao.geonet.api.regions;

import jeeves.JeevesCacheManager;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.KeywordBean;
import org.fao.geonet.kernel.Thesaurus;
import org.fao.geonet.kernel.ThesaurusManager;
import org.fao.geonet.kernel.rdf.QueryBuilder;
import org.fao.geonet.kernel.rdf.ResultInterpreter;
import org.fao.geonet.kernel.rdf.Selectors;
import org.fao.geonet.kernel.region.Region;
import org.fao.geonet.kernel.region.RegionsDAO;
import org.fao.geonet.kernel.region.Request;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.geotools.api.referencing.crs.CoordinateReferenceSystem;
import org.openrdf.model.Value;
import org.openrdf.sesame.query.QueryResultsTable;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.Callable;

public class ThesaurusBasedRegionsDAO extends RegionsDAO {

    private static final ResultInterpreter<String> CATEGORY_ID_READER = new ResultInterpreter<String>() {

        @Override
        public String createFromRow(Thesaurus thesaurus, QueryResultsTable resultTable, int rowIndex) {
            Value value = resultTable.getValue(rowIndex, 0);
            return value.toString();
        }
    };
    private static final String CATEGORY_ID_CACHE_KEY = "CATEGORY_ID_CACHE_KEY";

    private final Set<String> localesToLoad;
    private final WeakHashMap<String, Map<String, String>> categoryIdMap = new WeakHashMap<String, Map<String, String>>();
    private final GeometryFactory factory = new GeometryFactory();
    private String thesaurusName = "external.place.regions";

    public ThesaurusBasedRegionsDAO(Set<String> localesToLoad) {
        this.localesToLoad = Collections.unmodifiableSet(localesToLoad);
    }

    @Override
    public Request createSearchRequest(ServiceContext context) throws Exception {
        Thesaurus thesaurus = getThesaurus(context);

        return new ThesaurusRequest(context, this.categoryIdMap, localesToLoad, thesaurus);
    }

    public synchronized void setThesaurusName(String thesaurusName) {
        super.clearCaches();
        this.thesaurusName = thesaurusName;
    }

    private synchronized Thesaurus getThesaurus(ServiceContext context) throws Exception {
        ThesaurusManager th = context.getBean(ThesaurusManager.class);
        Thesaurus regions = th.getThesaurusByName(thesaurusName);
        if (regions != null) {
            return regions;
        }
        Set<Entry<String, Thesaurus>> all = th.getThesauriMap().entrySet();
        for (Entry<String, Thesaurus> entry : all) {
            if (entry.getKey().contains("regions")) {
                return entry.getValue();
            }
        }

        return null;
    }

    @Override
    public Geometry getGeom(ServiceContext context, String id, boolean simplified, CoordinateReferenceSystem projection) throws Exception {
        Region region = createSearchRequest(context).id(id).get();
        if (region == null) {
            return null;
        }

        Geometry geometry = factory.toGeometry(region.getBBox(projection));
        geometry.setUserData(region.getBBox().getCoordinateReferenceSystem());

        return geometry;
    }

    @Override
    public Collection<String> getRegionCategoryIds(final ServiceContext context) throws Exception {
        return JeevesCacheManager.findInTenSecondCache(CATEGORY_ID_CACHE_KEY, new Callable<Collection<String>>() {

            @Override
            public Collection<String> call() throws Exception {
                Thesaurus thesaurus = getThesaurus(context);
                if (thesaurus != null) {
                    QueryBuilder<String> queryBuilder = QueryBuilder.builder().interpreter(CATEGORY_ID_READER);
                    queryBuilder.distinct(true);
                    queryBuilder.select(Selectors.BROADER, true);
                    return queryBuilder.build().execute(thesaurus);
                } else {
                    return null;
                }
            }

        });
    }

    public java.util.List<KeywordBean> getRegionTopConcepts(final ServiceContext context) throws Exception {
        return JeevesCacheManager.findInTenSecondCache(CATEGORY_ID_CACHE_KEY + context.getLanguage() + thesaurusName,
            new Callable<java.util.List<KeywordBean>>() {

                @Override
                public java.util.List<KeywordBean> call() throws Exception {
                    Thesaurus thesaurus = getThesaurus(context);
                    if (thesaurus != null) {
                        return thesaurus.getTopConcepts(context.getLanguage(), Geonet.DEFAULT_LANGUAGE);
                    } else {
                        return null;
                    }
                }

            });
    }
}
