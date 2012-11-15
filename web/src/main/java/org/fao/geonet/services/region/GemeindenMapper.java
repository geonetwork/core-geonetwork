package org.fao.geonet.services.region;

import java.io.IOException;
import java.util.Map;
import java.util.WeakHashMap;

import jeeves.server.context.ServiceContext;

import org.fao.geonet.constants.Geonet;
import org.geotools.data.DataStore;
import org.geotools.data.simple.SimpleFeatureSource;
import org.jdom.JDOMException;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.filter.FilterFactory2;

public class GemeindenMapper extends DatastoreMapper {

	private static final String GEMEINDEN_DESC = "DESC";
	private static final String GEMEINDEN_NAME = "GEMNAME";
	private static final String GEMEINDEN_ID = "OBJECTVAL";
	private static final String CATEGORY_ID = "gemeinden";
	private static final String PREFIX = CATEGORY_ID + ":";
	private static final String SIMPLIFIED_BACKING_DS = "gemeindenBB";
	private static final String BACKING_DS = "gemeinden_search";

	public GemeindenMapper(ServiceContext context,
			DatastoreCache datastoreCache, FilterFactory2 filterFactory,
			WeakHashMap<String, Map<String, String>> categoryIdMap) {
		super(context, categoryIdMap, filterFactory, datastoreCache);
	}

	@Override
	public boolean accepts(String regionId) {
		return regionId.startsWith(PREFIX);
	}

	@Override
	protected String idPropertyName() {
		return GEMEINDEN_ID;
	}
	@Override
	public String categoryId() {
		return CATEGORY_ID;
	}

	@Override
	public String[] propNames(boolean simplified, boolean includeGeom) {
		if(simplified) {
			if(includeGeom) {
				return new String[]{GEMEINDEN_ID, GEMEINDEN_NAME, GEMEINDEN_DESC, THE_GEOM};
			} else {
				return new String[]{GEMEINDEN_ID, GEMEINDEN_NAME, GEMEINDEN_DESC};
			}
		} else {
			if(includeGeom) {
				return new String[]{GEMEINDEN_ID, GEMEINDEN_NAME, THE_GEOM};
			} else {
				return new String[]{GEMEINDEN_ID, GEMEINDEN_NAME};
			}
		}
	}
	@Override
	public Region constructRegion(SimpleFeature next) throws JDOMException,
			IOException {
		return super.constructRegion(next, PREFIX, GEMEINDEN_NAME, CATEGORY_ID);
	}

	@Override
	public String getBackingDatastoreName(boolean simplified) {
		return simplified ? SIMPLIFIED_BACKING_DS: BACKING_DS ;
	}

	@Override
	protected SimpleFeatureSource getFeatureSource(boolean simplified) throws IOException {
		String typeName = getBackingDatastoreName(simplified);
		DataStore ds = context.getApplicationContext().getBean(Geonet.BeanId.DATASTORE, DataStore.class);
		return ds.getFeatureSource(typeName);
	}

}
