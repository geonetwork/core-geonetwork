package org.fao.geonet.services.region.geocat;

import java.io.IOException;

import org.fao.geonet.services.region.Region;
import org.geotools.data.simple.SimpleFeatureSource;
import org.jdom.JDOMException;
import org.opengis.feature.simple.SimpleFeature;

public class KantoneMapper extends DatastoreMapper {
	private static final String KANTONE_NAME = "NAME";
	private static final String KANTONE_ID = "KANTONSNR";
	private static final String[] PROPS_WITH_GEOM = new String[] {
		KANTONE_ID, KANTONE_NAME, THE_GEOM, SEARCH };
	private static final String[] PROPS_WITHOUT_GEOM = new String[] { KANTONE_ID, KANTONE_NAME, SEARCH };
	private static final String CATEGORY_ID = "kantone";
	private static final String PREFIX = CATEGORY_ID + ":";
	private static final String CH1903_BACKING_DS = "kantoneBB";
	private static final String WGS84_BACKING_DS = "kantone_search";

	@Override
	public boolean accepts(String regionId) {
		return regionId.startsWith(PREFIX);
	}

	@Override
	protected String idPropertyName() {
		return KANTONE_ID;
	}

	@Override
	public String categoryId() {
		return CATEGORY_ID;
	}

	@Override
	public String[] propNames(boolean simplified, boolean includeGeom, boolean inLatLong) {
		if (includeGeom) {
			return PROPS_WITH_GEOM;
		} else {
			return PROPS_WITHOUT_GEOM;
		}
	}

	@Override
	public Region constructRegion(MapperState state, SimpleFeature next) throws JDOMException,
			IOException {
		return super.constructRegion(state, next, PREFIX, KANTONE_NAME, CATEGORY_ID);
	}

	@Override
	public String getBackingDatastoreName(boolean simplified, boolean inLatLong) {
		return inLatLong ? WGS84_BACKING_DS : CH1903_BACKING_DS;
	}

	@Override
	protected SimpleFeatureSource getFeatureSource(MapperState state, boolean simplified, boolean inLatLong)
			throws IOException {
		return state.datastoreCache.getCached(state.context, this, simplified, inLatLong);
	}

}
