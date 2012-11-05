package org.fao.geonet.services.region;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.fao.geonet.kernel.KeywordBean;
import org.fao.geonet.kernel.Thesaurus;
import org.fao.geonet.kernel.rdf.Query;
import org.fao.geonet.kernel.rdf.QueryBuilder;
import org.fao.geonet.kernel.rdf.Selectors;
import org.fao.geonet.kernel.search.keyword.KeywordRelation;
import org.fao.geonet.languages.IsoLanguagesMapper;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.openrdf.sesame.config.AccessDeniedException;
import org.openrdf.sesame.query.MalformedQueryException;
import org.openrdf.sesame.query.QueryEvaluationException;

public class ThesaurusRequest implements Request {

    
    private final java.util.List<String> localesToLoad;
    private final Thesaurus thesaurus;

    private String labelParam;
    private String categoryIdParam;
    private int maxRecordsParam = -1;
    
    
    public ThesaurusRequest(Set<String> localesToLoad, Thesaurus thesaurus) {
        this.localesToLoad = new ArrayList<String>(localesToLoad);
        this.thesaurus = thesaurus;
    }

    @Override
    public Request setLabel(String labelParam) {
        this.labelParam = labelParam;
        return this;
    }

    @Override
    public Request setCategoryId(String categoryIdParam) {
        this.categoryIdParam = categoryIdParam;
        return this;
    }

    @Override
    public Request setMaxRecords(int maxRecordsParam) {
        this.maxRecordsParam = maxRecordsParam;
        return this;
    }

    @Override
    public Collection<Region> execute() throws Exception {
        IsoLanguagesMapper languageMapper = thesaurus.getIsoLanguageMapper();
        QueryBuilder<KeywordBean> queryBuilder = QueryBuilder.keywordQueryBuilder(languageMapper, this.localesToLoad);
        if(labelParam != null) {
            for(String locale: localesToLoad) {
                queryBuilder.select(Selectors.prefLabel(locale, languageMapper), false);
            }
        }
        if(categoryIdParam != null) {
            queryBuilder.select(Selectors.related(categoryIdParam, KeywordRelation.BROADER), true);
        }
        if(maxRecordsParam > 0) {
            queryBuilder.limit(maxRecordsParam);
        }

        Query<KeywordBean> query = queryBuilder.build();
        List<KeywordBean> keywords = query.execute(this.thesaurus);
        List<Region> regions = new ArrayList<Region>(keywords.size());
        for (KeywordBean keywordBean : keywords) {
            String id = keywordBean.getUriCode();
            Map<String, String> labels;
            String categoryId;
            Map<String, String> categoryLabels;
            boolean hasGeom = false;
            double west = Double.parseDouble(keywordBean.getCoordWest());
            double east = Double.parseDouble(keywordBean.getCoordEast());
            double north = Double.parseDouble(keywordBean.getCoordNorth());
            double south = Double.parseDouble(keywordBean.getCoordSouth());
            ReferencedEnvelope bbox = new ReferencedEnvelope(west, east, south, north, DefaultGeographicCRS.WGS84);
            Region region = new Region(id, labels, categoryId, categoryLabels, hasGeom, bbox);
            regions.add(region);
        }
        return null;
    }

}
