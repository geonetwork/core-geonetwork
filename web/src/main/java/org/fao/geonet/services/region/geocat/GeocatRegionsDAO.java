package org.fao.geonet.services.region.geocat;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.WeakHashMap;

import jeeves.server.context.ServiceContext;

import org.fao.geonet.services.region.Region;
import org.fao.geonet.services.region.RegionsDAO;
import org.fao.geonet.services.region.Request;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.opengis.filter.FilterFactory2;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;

import com.vividsolutions.jts.geom.Geometry;

public class GeocatRegionsDAO extends RegionsDAO {

	private DatastoreCache datastoreCache = new DatastoreCache();
	private WeakHashMap<String, Map<String, String>> categoryIdMap = new WeakHashMap<String, Map<String, String>>();
	FilterFactory2 filterFactory = CommonFactoryFinder.getFilterFactory2();

	@Override
	public Request createSearchRequest(ServiceContext context) throws Exception {
		MapperState state = new MapperState(context, categoryIdMap, filterFactory, datastoreCache);
		return new GeocatRegionsRequest(state);
	}

	@Override
	public Geometry getGeom(ServiceContext context, String regionId,
			boolean simplified, CoordinateReferenceSystem projection) throws Exception {

		boolean isLatLong = CRS.equalsIgnoreMetadata(Region.WGS84, projection);
		try {
			MapperState state = new MapperState(context, categoryIdMap, filterFactory, datastoreCache);
			DatastoreMapper mapper = DatastoreMappers.find(regionId);
			Geometry geom = mapper.getGeometry(state, simplified, regionId, isLatLong);

			CoordinateReferenceSystem sourceSRS = (CoordinateReferenceSystem) geom.getUserData();
	        Integer sourceCode = CRS.lookupEpsgCode(sourceSRS, false);
	        Integer desiredCode = CRS.lookupEpsgCode(projection, false);
	        if ((sourceCode == null || desiredCode == null || desiredCode != sourceCode) && !CRS.equalsIgnoreMetadata(sourceSRS, projection)) {
	            MathTransform transform = CRS.findMathTransform(sourceSRS, projection, true);
	            geom = JTS.transform(geom, transform);
	        }

	        return geom;
		} catch (IllegalArgumentException e) {
			return null;
		}

	}

	@Override
	public Collection<String> getRegionCategoryIds(ServiceContext context) {
		LinkedList<String> ids = new LinkedList<String>();
		for (DatastoreMappers mapper : DatastoreMappers.values()) {
			ids.add(mapper.mapper.categoryId());
		}
		return ids;
	}

}
