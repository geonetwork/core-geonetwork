package org.fao.geonet.services.region.geocat;

import org.fao.geonet.services.region.Region;
import org.geotools.data.simple.SimpleFeatureSource;
import org.jdom.JDOMException;
import org.opengis.feature.simple.SimpleFeature;

import java.io.IOException;

public class CountryMapper extends DatastoreMapper {
	private static final String COUNTRY_DESC = "DESC";
	private static final String COUNTRY_NAME = "LAND";
	private static final String COUNTRY_ID = "ID";
	private static final String CATEGORY_ID = "country";
	private static final String PREFIX = CATEGORY_ID+":";
	private static final String CH1903_BACKING_DS = "countries";
	private static final String WGS84_BACKING_DS = "countries_search";

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
	public String[] propNames(boolean simplified, boolean includeGeom, boolean inLatLong) {
		if(inLatLong) {
			if(includeGeom) {
				return new String[]{COUNTRY_ID, COUNTRY_NAME, COUNTRY_DESC, SEARCH, THE_GEOM};
			} else {
				return new String[]{COUNTRY_ID, COUNTRY_NAME, COUNTRY_DESC, SEARCH};
			}
		} else {
			if(includeGeom) {
				return new String[]{COUNTRY_ID, COUNTRY_NAME, SEARCH, THE_GEOM};
			} else {
				return new String[]{COUNTRY_ID, COUNTRY_NAME, SEARCH};
			}
		}
	}

	@Override
	public Region constructRegion(MapperState state, SimpleFeature next) throws JDOMException, IOException {
		return super.constructRegion(state, next, PREFIX,COUNTRY_NAME, CATEGORY_ID);
	}

	@Override
	public String getBackingDatastoreName(boolean simplified, boolean inLatLong) {
		return inLatLong ? WGS84_BACKING_DS : CH1903_BACKING_DS;
	}

	@Override
	protected SimpleFeatureSource getFeatureSource(MapperState state, boolean simplified, boolean inLatLong) throws IOException {
		return state.datastoreCache.getCached(state.context, this, simplified, inLatLong);
	}

}
