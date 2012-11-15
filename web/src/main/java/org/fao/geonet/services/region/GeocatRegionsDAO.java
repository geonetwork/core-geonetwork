package org.fao.geonet.services.region;

import java.util.Map;
import java.util.WeakHashMap;

import jeeves.server.context.ServiceContext;

import org.geotools.factory.CommonFactoryFinder;
import org.opengis.filter.FilterFactory2;

import com.vividsolutions.jts.geom.Geometry;

public class GeocatRegionsDAO extends RegionsDAO {

	private DatastoreCache datastoreCache = new DatastoreCache();
	private WeakHashMap<String, Map<String, String>> categoryIdMap = new WeakHashMap<String, Map<String, String>>();
	FilterFactory2 filterFactory = CommonFactoryFinder.getFilterFactory2();

	@Override
	public Request createSearchRequest(ServiceContext context) throws Exception {
		return new GeocatRegionsRequest(context, datastoreCache, categoryIdMap, filterFactory);
	}

	@Override
	public Geometry getGeom(ServiceContext context, String regionId,
			boolean simplified) throws Exception {
		DatastoreMapper countryMapper = new CountryMapper(context, datastoreCache, filterFactory, categoryIdMap);
		DatastoreMapper kantoneMapper = new KantoneMapper(context, datastoreCache, filterFactory, categoryIdMap);
		DatastoreMapper gemeindenMapper = new GemeindenMapper(context, datastoreCache, filterFactory, categoryIdMap);
		
		if(gemeindenMapper.accepts(regionId)) {
			return gemeindenMapper.getGeometry(simplified, regionId);
		} else if(kantoneMapper.accepts(regionId)) {
			return kantoneMapper.getGeometry(simplified, regionId);
		} else if(countryMapper.accepts(regionId)) {
			return countryMapper.getGeometry(simplified, regionId);
		} else {
			return null;
		}
	}

}
