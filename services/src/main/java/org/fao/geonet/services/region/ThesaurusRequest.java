package org.fao.geonet.services.region;

import jeeves.server.context.ServiceContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.KeywordBean;
import org.fao.geonet.kernel.SingleThesaurusFinder;
import org.fao.geonet.kernel.Thesaurus;
import org.fao.geonet.kernel.rdf.Query;
import org.fao.geonet.kernel.rdf.QueryBuilder;
import org.fao.geonet.kernel.rdf.Selectors;
import org.fao.geonet.kernel.region.Region;
import org.fao.geonet.kernel.region.Request;
import org.fao.geonet.kernel.search.keyword.KeywordRelation;
import org.fao.geonet.kernel.search.keyword.KeywordSearchParamsBuilder;
import org.fao.geonet.kernel.search.keyword.KeywordSearchType;
import org.fao.geonet.util.LangUtils;
import org.fao.geonet.utils.Log;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.jdom.JDOMException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import java.util.*;
import java.util.List;
import java.util.Map.Entry;

public class ThesaurusRequest extends Request {

    
    private static final String NO_CATEGORY = "_none_";

    private static final CoordinateReferenceSystem WGS84 = Region.WGS84;

    private WeakHashMap<String, Map<String, String>> categoryTranslations;
    private ServiceContext serviceContext;
    private KeywordSearchParamsBuilder searchBuilder;
    private Thesaurus thesaurus;
    private SingleThesaurusFinder finder;

    private Set<String> localesToLoad;
    
    
    public ThesaurusRequest(ServiceContext context, WeakHashMap<String, Map<String, String>> categoryTranslations, Set<String> localesToLoad, Thesaurus thesaurus) {
        this.localesToLoad = localesToLoad;
        this.serviceContext = context;
        this.categoryTranslations = categoryTranslations;
        this.thesaurus = thesaurus;
        this.searchBuilder = new KeywordSearchParamsBuilder(thesaurus.getIsoLanguageMapper());
        for (String lang : localesToLoad) {
            searchBuilder.addLang(lang);
        }
        searchBuilder.addThesaurus(thesaurus.getKey());
        searchBuilder.requireBoundedBy(true);
        this.finder = new SingleThesaurusFinder(thesaurus);
    }

    @Override
    public Request label(String labelParam) {
        searchBuilder.keyword(labelParam, KeywordSearchType.CONTAINS, true);
        return this;
    }

    @Override
    public Request categoryId(String categoryIdParam) {
        if(categoryIdParam.equals(NO_CATEGORY)) {
            categoryIdParam = "";
        }
        searchBuilder.relationship(categoryIdParam, KeywordRelation.BROADER, KeywordSearchType.MATCH, false);
        return this;
    }
    
    
    /**
     * Return the first broader term found.
     * 
     * @param keywordId
     * @return
     */
    public String getCategoryId(String keywordId) {
        String categoryIdParam = "";
        Query<KeywordBean> query = QueryBuilder.keywordQueryBuilder(thesaurus.getIsoLanguageMapper())
            .select(Selectors.related(keywordId, KeywordRelation.NARROWER), true).build();
        try {
            List<KeywordBean> results = query.execute(thesaurus);
            // Return the first one or the default
            if (results.size() == 1) {
                categoryIdParam = results.get(0).getUriCode();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return categoryIdParam;
    }
    
    @Override
    public Request maxRecords(int maxRecordsParam) {
        searchBuilder.maxResults(maxRecordsParam);
        return this;
    }

    @Override
    public Collection<Region> execute() throws Exception {
       
        List<KeywordBean> keywords = searchBuilder.build().search(finder);
        List<Region> regions = new ArrayList<Region>(keywords.size());
        for (KeywordBean keywordBean : keywords) {
            String id = keywordBean.getUriCode();
            Map<String, String> unfilteredLabels = keywordBean.getValues();
            Map<String, String> labels = new HashMap<String, String>((int)(unfilteredLabels.size() * 1.25));
            Iterator<Entry<String, String>> iter = unfilteredLabels.entrySet().iterator();
            while(iter.hasNext()) {
                Entry<String, String> next = iter.next();
                if(!next.getValue().trim().isEmpty()) {
                    labels.put(next.getKey(), next.getValue());
                }
            }
            String categoryId = getCategoryId(keywordBean.getUriCode());
            
            if(categoryId.trim().isEmpty()) {
                categoryId = NO_CATEGORY;
            }
            String categoryLabelKey = categoryId;
            if (categoryLabelKey.equals(NO_CATEGORY)) {
                categoryLabelKey = "none";
            }
            if(categoryLabelKey.indexOf('#') > -1) {
                categoryLabelKey = categoryLabelKey.substring(categoryLabelKey.lastIndexOf('#')+1);
            }
            Map<String, String> categoryLabels = categoryTranslations.get(categoryLabelKey);
            if(categoryLabels == null) {
                try {
                    categoryLabels = LangUtils.translate(serviceContext.getApplicationContext(), "categories", categoryLabelKey);
                    categoryTranslations.put(categoryLabelKey, categoryLabels);
                } catch (JDOMException e) {
                    Log.debug(Geonet.THESAURUS_MAN,
                            String.format("Category key %s is not valid for JDOM element." +
                                            "Region thesaurus should use rdf:about element " +
                                            "with the following structure <prefix>#<id> " +
                                            "where the id could be a valid XML element name. " +
                                            "Error is %s.",
                                    categoryLabelKey, e.getMessage()));
                    categoryLabels = new WeakHashMap<String, String>();
                }
            }
            if (categoryLabels.isEmpty()) {
                for (String loc : localesToLoad) {
                    categoryLabels.put(loc, categoryLabelKey);
                }
            }
            boolean hasGeom = false;
            double west = Double.parseDouble(keywordBean.getCoordWest());
            double east = Double.parseDouble(keywordBean.getCoordEast());
            double north = Double.parseDouble(keywordBean.getCoordNorth());
            double south = Double.parseDouble(keywordBean.getCoordSouth());
            ReferencedEnvelope bbox = new ReferencedEnvelope(west, east, south, north, WGS84);
            Region region = new Region(id, labels, categoryId, categoryLabels, hasGeom, bbox);
            regions.add(region);
        }
        return regions;
    }

    @Override
    public Request id(String regionId) {
        searchBuilder.uri(regionId);
        return this;
    }

}
