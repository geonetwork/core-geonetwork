package org.fao.geonet.services.region;

import java.util.Map;
import java.util.WeakHashMap;

import jeeves.server.context.ServiceContext;

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
		return new GeocatRegionsRequest(context, datastoreCache, categoryIdMap, filterFactory);
	}

	@Override
	public Geometry getGeom(ServiceContext context, String regionId,
			boolean simplified, CoordinateReferenceSystem projection) throws Exception {
		DatastoreMapper countryMapper = new CountryMapper(context, datastoreCache, filterFactory, categoryIdMap);
		DatastoreMapper kantoneMapper = new KantoneMapper(context, datastoreCache, filterFactory, categoryIdMap);
		DatastoreMapper gemeindenMapper = new GemeindenMapper(context, datastoreCache, filterFactory, categoryIdMap);

		boolean isLatLong = CRS.equalsIgnoreMetadata(Region.WGS84, projection);
		Geometry geom;
		if(gemeindenMapper.accepts(regionId)) {
			geom = gemeindenMapper.getGeometry(simplified, regionId, isLatLong);
		} else if(kantoneMapper.accepts(regionId)) {
			geom = kantoneMapper.getGeometry(simplified, regionId, isLatLong);
		} else if(countryMapper.accepts(regionId)) {
			geom = countryMapper.getGeometry(simplified, regionId, isLatLong);
		} else {
			geom = null;
		}
		if(geom == null) {
			return null;
		}

		CoordinateReferenceSystem sourceSRS = (CoordinateReferenceSystem) geom.getUserData();
        Integer sourceCode = CRS.lookupEpsgCode(sourceSRS, false);
        Integer desiredCode = CRS.lookupEpsgCode(projection, false);
        if ((sourceCode == null || desiredCode == null || desiredCode != sourceCode) && !CRS.equalsIgnoreMetadata(sourceSRS, projection)) {
            MathTransform transform = CRS.findMathTransform(sourceSRS, projection, true);
            geom = JTS.transform(geom, transform);
        }

        return geom;
	}

}
