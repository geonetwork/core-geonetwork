package org.fao.geonet.services.region;

import java.io.IOException;
import java.util.Map;
import java.util.WeakHashMap;

import jeeves.server.context.ServiceContext;

import org.geotools.data.simple.SimpleFeatureSource;
import org.jdom.JDOMException;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.filter.FilterFactory2;

public class KantoneMapper extends DatastoreMapper {
	private static final String KANTONE_NAME = "NAME";
	private static final String KANTONE_ID = "KANTONSNR";
	private static final String[] PROPS_WITH_GEOM = new String[] {
		KANTONE_ID, KANTONE_NAME, THE_GEOM };
	private static final String[] PROPS_WITHOUT_GEOM = new String[] { KANTONE_ID, KANTONE_NAME };
	private static final String CATEGORY_ID = "kanton";
	private static final String PREFIX = CATEGORY_ID + ":";
	private static final String SIMPLIFIED_BACKING_DS = "kantoneBB";
	private static final String BACKING_DS = "kantone_searching";

	public KantoneMapper(ServiceContext context, DatastoreCache datastoreCache,
			FilterFactory2 filterFactory,
			WeakHashMap<String, Map<String, String>> categoryIdMap) {
		super(context, categoryIdMap, filterFactory, datastoreCache);
	}

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
	public String[] propNames(boolean simplified, boolean includeGeom) {
		if (includeGeom) {
			return PROPS_WITH_GEOM;
		} else {
			return PROPS_WITHOUT_GEOM;
		}
	}

	@Override
	public Region constructRegion(SimpleFeature next) throws JDOMException,
			IOException {
		return super.constructRegion(next, PREFIX, KANTONE_NAME, CATEGORY_ID);
	}

	@Override
	public String getBackingDatastoreName(boolean simplified) {
		return simplified ? SIMPLIFIED_BACKING_DS : BACKING_DS;
	}

	@Override
	protected SimpleFeatureSource getFeatureSource(boolean simplified)
			throws IOException {
		return datastoreCache.getCached(context, this, simplified);
	}

}
