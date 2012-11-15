package org.fao.geonet.services.region;

import java.io.IOException;
import java.util.Map;
import java.util.WeakHashMap;

import jeeves.server.context.ServiceContext;

import org.geotools.data.simple.SimpleFeatureSource;
import org.jdom.JDOMException;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.filter.FilterFactory2;

public class CountryMapper extends DatastoreMapper {
	private static final String COUNTRY_DESC = "DESC";
	private static final String COUNTRY_NAME = "LAND";
	private static final String COUNTRY_ID = "ID";
	private static final String CATEGORY_ID = "country";
	private static final String PREFIX = CATEGORY_ID+":";
	private static final String CH1903_BACKING_DS = "countries";
	private static final String WGS84_BACKING_DS = "countries_search";

	public CountryMapper(ServiceContext context, DatastoreCache datastoreCache, FilterFactory2 filterFactory, WeakHashMap<String,Map<String,String>> categoryIdMap) {
		super(context, categoryIdMap, filterFactory, datastoreCache);
	}
	@Override
	public boolean accepts(String regionId) {
		return regionId.startsWith(PREFIX);
	}

	@Override
	public String categoryId() {
		return CATEGORY_ID;
	}

	@Override
	protected String idPropertyName() {
		return COUNTRY_ID;
	}
	
	@Override
	public String[] propNames(boolean simplified, boolean includeGeom) {
		if(simplified) {
			if(includeGeom) {
				return new String[]{COUNTRY_ID, COUNTRY_NAME, COUNTRY_DESC, THE_GEOM};
			} else {
				return new String[]{COUNTRY_ID, COUNTRY_NAME, COUNTRY_DESC};
			}
		} else {
			if(includeGeom) {
				return new String[]{COUNTRY_ID, COUNTRY_NAME, THE_GEOM};
			} else {
				return new String[]{COUNTRY_ID, COUNTRY_NAME};
			}
		}
	}

	@Override
	public Region constructRegion(SimpleFeature next) throws JDOMException, IOException {
		return super.constructRegion(next, PREFIX,COUNTRY_NAME, CATEGORY_ID);
	}

	@Override
	public String getBackingDatastoreName(boolean simplified, boolean inLatLong) {
		return inLatLong ? WGS84_BACKING_DS : CH1903_BACKING_DS;
	}

	@Override
	protected SimpleFeatureSource getFeatureSource(boolean simplified, boolean inLatLong) throws IOException {
		return datastoreCache.getCached(context, this, simplified, inLatLong);
	}

}
