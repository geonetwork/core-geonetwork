package org.fao.geonet.services.region.geocat;

import com.vividsolutions.jts.geom.Geometry;
import org.fao.geonet.services.region.Region;
import org.fao.geonet.util.LangUtils;
import org.geotools.data.Query;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.filter.Filter;
import org.opengis.filter.expression.Expression;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public abstract class DatastoreMapper {

	protected static final String THE_GEOM = "the_geom";
	public static final String SEARCH = "SEARCH";

	public static final String SIMPLIFIED_ATT = "simplified";
	
	public abstract boolean accepts(String regionId);

	protected abstract String idPropertyName();

	public abstract String categoryId();
	
	protected abstract SimpleFeatureSource getFeatureSource(MapperState state, boolean simplified, boolean inLatLong) throws IOException;

	public abstract  String[] propNames(boolean simplified, boolean includeGeom, boolean inLatLong);

	public abstract Region constructRegion(MapperState state, SimpleFeature next) throws JDOMException, IOException;

	public abstract String getBackingDatastoreName(boolean simplified, boolean inLatLong);

	public final Filter idFilter(MapperState state, String regionId) {
		Expression propertyExpression = state.filterFactory.property(idPropertyName());
		Expression requiredValue = state.filterFactory.literal(regionId.substring(categoryId().length()+1));
		return state.filterFactory.equal(propertyExpression, requiredValue, false);
	}

	public final void loadRegions(MapperState state, Collection<Region> results, int maxRegions,Filter filter) throws IOException,
			JDOMException {
		boolean simplified = true;
		boolean inLatLong = false;
		SimpleFeatureSource featureSource = getFeatureSource(state, simplified, inLatLong);
		Query query = createQuery(state, simplified, maxRegions, filter, inLatLong);
		SimpleFeatureIterator features = featureSource.getFeatures(query).features();
		try {
			while (features.hasNext()) {
				SimpleFeature next = features.next();
				results.add(constructRegion(state, next));
			}
		} finally {
			features.close();
		}
	}

	private Query createQuery(MapperState state, boolean simplified, int maxRegions, Filter filter, boolean inLatLong) {
		Query query = new Query(getBackingDatastoreName(simplified, inLatLong), filter);
		query.setMaxFeatures(maxRegions);
		query.setPropertyNames(propNames(simplified, true, inLatLong));
		return query;
	}

	public Region constructRegion(MapperState state, SimpleFeature feature, String prefix, String labelAttName,
			String categoryId) throws JDOMException, IOException {

		String id = prefix+feature.getAttribute(this.idPropertyName());
		Map<String, String> labels = new HashMap<String, String>();
        final Object labelAtt = feature.getAttribute(labelAttName);
        Element translations;
        if (labelAtt != null) {
            String label = labelAtt.toString();
            translations = LangUtils.loadInternalMultiLingualElem(label);
        } else {
            translations = new Element("EmptyTranslations").setText("---- " + id + " ----");
        }
        if(translations.getText()!=null) {
			labels.put("eng", translations.getText());
			labels.put("ger", translations.getText());
			labels.put("fre", translations.getText());
			labels.put("ita", translations.getText());			
		} else {
			labels.put("eng", translations.getChildText("EN"));
			labels.put("ger", translations.getChildText("DE"));
			labels.put("fre", translations.getChildText("FR"));
			labels.put("ita", translations.getChildText("IT"));
		}
		boolean hasGeom = true;
		ReferencedEnvelope bbox = new ReferencedEnvelope(feature.getBounds());
		Map<String, String> kantonLabels = state.categoryIdMap.get(categoryId);
		if(kantonLabels == null) {
			kantonLabels = LangUtils.translate(state.context, categoryId);
			state.categoryIdMap.put(categoryId, kantonLabels);
		}
		return new Region(id, labels, categoryId, kantonLabels, hasGeom, bbox);
		
	}

	public Geometry getGeometry(MapperState state, boolean simplified, String regionId, boolean inLatLong) throws IOException {
		SimpleFeatureSource featureSource = getFeatureSource(state, simplified, inLatLong);
		SimpleFeatureIterator features = featureSource.getFeatures(idFilter(state, regionId)).features();
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
