package org.fao.geonet.services.region;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

import jeeves.server.context.ServiceContext;

import org.fao.geonet.util.LangUtils;
import org.geotools.data.Query;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.jdom.JDOMException;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory2;
import org.opengis.filter.expression.Expression;

import com.vividsolutions.jts.geom.Geometry;

public abstract class DatastoreMapper {

	protected static final String THE_GEOM = "the_geom";

	public static final String SIMPLIFIED_ATT = "simplified";
	
	protected final ServiceContext context;
	protected final WeakHashMap<String, Map<String, String>> categoryIdMap;
	protected final FilterFactory2 filterFactory;
	protected DatastoreCache datastoreCache;

	public DatastoreMapper(ServiceContext context,
			WeakHashMap<String, Map<String, String>> categoryIdMap, FilterFactory2 filterFactory, DatastoreCache datastoreCache) {
		this.context = context;
		this.categoryIdMap = categoryIdMap;
		this.filterFactory = filterFactory;
		this.datastoreCache = datastoreCache;
	}

	public abstract boolean accepts(String regionId);

	protected abstract String idPropertyName();

	public abstract String categoryId();
	
	protected abstract SimpleFeatureSource getFeatureSource(boolean simplified, boolean inLatLong) throws IOException;

	public abstract  String[] propNames(boolean simplified, boolean includeGeom);

	public abstract Region constructRegion(SimpleFeature next) throws JDOMException, IOException;

	public abstract String getBackingDatastoreName(boolean simplified, boolean inLatLong);

	public final Filter idFilter(String regionId) {
		Expression propertyExpression = filterFactory.property(idPropertyName());
		Expression requiredValue = filterFactory.literal(regionId.substring(categoryId().length()+1));
		return filterFactory.equal(propertyExpression, requiredValue, false);
	}

	public final void loadRegions(Collection<Region> results, int maxRegions,Filter filter) throws IOException,
			JDOMException {
		boolean simplified = true;
		boolean inLatLong = false;
		SimpleFeatureSource featureSource = getFeatureSource(simplified, inLatLong);
		Query query = createQuery(simplified, maxRegions, filter, inLatLong);
		SimpleFeatureIterator features = featureSource.getFeatures(query).features();
		try {
			while (features.hasNext()) {
				SimpleFeature next = features.next();
				results.add(constructRegion(next));
			}
		} finally {
			features.close();
		}
	}

	private Query createQuery(boolean simplified, int maxRegions, Filter filter, boolean inLatLong) {
		Query query = new Query(getBackingDatastoreName(simplified, inLatLong), filter);
		query.setMaxFeatures(maxRegions);
		query.setPropertyNames(propNames(simplified, false));
		return query;
	}

	public Region constructRegion(SimpleFeature feature, String prefix, String labelAttName,
			String categoryId) throws JDOMException, IOException {

		String id = prefix+feature.getID();
		Map<String, String> labels = new HashMap<String, String>();
		String label = feature.getAttribute(labelAttName).toString();
		labels.put("eng", label);
		labels.put("ger", label);
		labels.put("fre", label);
		labels.put("ita", label);
		boolean hasGeom = true;
		ReferencedEnvelope bbox = new ReferencedEnvelope(feature.getBounds());
		Map<String, String> kantonLabels = categoryIdMap.get(categoryId);
		if(kantonLabels == null) {
			kantonLabels = LangUtils.translate(context, categoryId);
			categoryIdMap.put(categoryId, kantonLabels);
		}
		return new Region(id, labels, categoryId, kantonLabels, hasGeom, bbox);
		
	}

	public Geometry getGeometry(boolean simplified, String regionId, boolean inLatLong) throws IOException {
		SimpleFeatureSource featureSource = getFeatureSource(simplified, inLatLong);
		SimpleFeatureIterator features = featureSource.getFeatures(idFilter(regionId)).features();
		try {
			if(features.hasNext()) {
				SimpleFeature feature = features.next();
				if(features.hasNext()) {
					throw new IllegalStateException("there is more than one region found");
				}
				Geometry geometry;
				if (simplified && feature.getAttribute(SIMPLIFIED_ATT)!=null) {
					geometry = (Geometry) feature.getAttribute(SIMPLIFIED_ATT);
				} else {
					geometry = (Geometry) feature.getDefaultGeometry();
				}
				geometry.setUserData(featureSource.getSchema().getGeometryDescriptor().getCoordinateReferenceSystem());
				return geometry;
			} else {
				return null;
			}
			
		} finally {
			features.close();
		}
	}


}
