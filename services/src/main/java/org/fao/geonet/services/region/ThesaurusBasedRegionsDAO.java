package org.fao.geonet.services.region;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import jeeves.JeevesCacheManager;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.Thesaurus;
import org.fao.geonet.kernel.ThesaurusManager;
import org.fao.geonet.kernel.rdf.QueryBuilder;
import org.fao.geonet.kernel.rdf.ResultInterpreter;
import org.fao.geonet.kernel.rdf.Selectors;
import org.fao.geonet.kernel.region.Region;
import org.fao.geonet.kernel.region.RegionsDAO;
import org.fao.geonet.kernel.region.Request;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
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
    private WeakHashMap<String, Map<String, String>> categoryIdMap = new WeakHashMap<String, Map<String, String>>();
    private GeometryFactory factory = new GeometryFactory();
    private String thesaurusName = "external.place.regions";
    public ThesaurusBasedRegionsDAO(Set<String> localesToLoad) {
        this.localesToLoad = Collections.unmodifiableSet(localesToLoad);
    }
    
    @Override
    public Request createSearchRequest(ServiceContext context) throws Exception {
        Thesaurus thesaurus = getThesaurus(context);

        return new ThesaurusRequest(context, this.categoryIdMap , localesToLoad, thesaurus);
    }
    
    public synchronized void setThesaurusName(String thesaurusName) {
        super.clearCaches();
        this.thesaurusName = thesaurusName;
    }

    private synchronized Thesaurus getThesaurus(ServiceContext context) throws Exception {
        GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
        ThesaurusManager th = gc.getBean(ThesaurusManager.class);
        Thesaurus regions = th.getThesaurusByName(thesaurusName);
        if(regions != null) {
            return regions;
        }
        Set<Entry<String, Thesaurus>> all = th.getThesauriMap().entrySet();
        for (Entry<String, Thesaurus> entry : all) {
            if(entry.getKey().contains("regions")) {
                return entry.getValue();
            }
        }
        
        return null;
    }

    @Override
    public Geometry getGeom(ServiceContext context, String id, boolean simplified, CoordinateReferenceSystem projection) throws Exception {
        Region region = createSearchRequest(context).id(id).get();
        if(region == null) {
            return null;
        }
       
        Geometry geometry = factory.toGeometry(region.getBBox(projection));
        geometry.setUserData(region.getBBox().getCoordinateReferenceSystem());
        
        return geometry;
    }

	@Override
	public Collection<String> getRegionCategoryIds(final ServiceContext context) throws Exception{
	    return JeevesCacheManager.findInTenSecondCache(CATEGORY_ID_CACHE_KEY, new Callable<Collection<String>>(){

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

}
