package org.fao.geonet.services.region.geocat;

import java.io.IOException;

import org.fao.geonet.constants.Geonet;
import org.fao.geonet.services.region.Region;
import org.geotools.data.DataStore;
import org.geotools.data.simple.SimpleFeatureSource;
import org.jdom.JDOMException;
import org.opengis.feature.simple.SimpleFeature;

public class GemeindenMapper extends DatastoreMapper {

	private static final String GEMEINDEN_DESC = "DESC";
	private static final String GEMEINDEN_NAME = "GEMNAME";
	private static final String GEMEINDEN_ID = "OBJECTVAL";
	private static final String CATEGORY_ID = "gemeinden";
	private static final String PREFIX = CATEGORY_ID + ":";
	private static final String CH1903_BACKING_DS = "gemeindenBB";
	private static final String WGS84_BACKING_DS = "gemeinden_search";

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
	public String[] propNames(boolean simplified, boolean includeGeom, boolean inLatLong) {
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
	public Region constructRegion(MapperState state, SimpleFeature next) throws JDOMException,
			IOException {
		return super.constructRegion(state, next, PREFIX, GEMEINDEN_NAME, CATEGORY_ID);
	}

	@Override
	public String getBackingDatastoreName(boolean simplified, boolean inLatLong) {
		return inLatLong ? WGS84_BACKING_DS : CH1903_BACKING_DS;
	}

	@Override
	protected SimpleFeatureSource getFeatureSource(MapperState state, boolean simplified, boolean inLatLong) throws IOException {
		String typeName = getBackingDatastoreName(simplified, inLatLong);
		DataStore ds = state.context.getApplicationContext().getBean(Geonet.BeanId.DATASTORE, DataStore.class);
		return ds.getFeatureSource(typeName);
	}

}
