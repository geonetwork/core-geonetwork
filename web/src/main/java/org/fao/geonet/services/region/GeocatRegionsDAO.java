package org.fao.geonet.services.region;

import java.util.Map;
import java.util.WeakHashMap;

import org.geotools.factory.CommonFactoryFinder;
import org.opengis.filter.FilterFactory2;

import jeeves.server.context.ServiceContext;

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
	public Geometry getGeom(ServiceContext context, String id,
			boolean simplified) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

}
