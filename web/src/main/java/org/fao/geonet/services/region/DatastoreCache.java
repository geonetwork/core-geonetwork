package org.fao.geonet.services.region;

import static org.fao.geonet.services.region.GeocatRegionsRequest.*;
import java.io.IOException;

import jeeves.server.context.ServiceContext;

import org.fao.geonet.constants.Geonet;
import org.geotools.data.DataStore;
import org.geotools.data.Query;
import org.geotools.data.memory.MemoryDataStore;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.data.simple.SimpleFeatureStore;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.opengis.filter.Filter;

public class DatastoreCache {
	private static final String THE_GEOM = "the_geom";
	private MemoryDataStore cache = null; 
	
	public synchronized SimpleFeatureSource getKantons(ServiceContext context) throws IOException {
		SimpleFeatureSource featureSource = cache.getFeatureSource(KANTON);
		if(featureSource == null) {
			featureSource = loadIntoMemory(context, "kantoneBB", KANTON, new String[]{KANTONE_NAME, THE_GEOM});
		}
		return featureSource;
	}

	public synchronized SimpleFeatureSource getCountries(ServiceContext context) throws IOException {
		SimpleFeatureSource featureSource = cache.getFeatureSource(COUNTRY);
		if(featureSource == null) {
			featureSource = loadIntoMemory(context, "country", COUNTRY, new String[]{COUNTRY_NAME, COUNTRY_DESC, THE_GEOM});
		}
		return featureSource;
	}

	public synchronized SimpleFeatureSource getGemeindens(ServiceContext context) throws IOException {
		DataStore postgis = (DataStore) context.getApplicationContext().getBean(Geonet.BeanId.DATASTORE);
		return postgis.getFeatureSource("gemeindenBB");
	}
	

	private SimpleFeatureSource loadIntoMemory(ServiceContext context, String sourceTypeName, String cacheTypeName, String[] attributes)
			throws IOException {
		SimpleFeatureSource featureSource;
		DataStore postgis = (DataStore) context.getApplicationContext().getBean(Geonet.BeanId.DATASTORE);
		SimpleFeatureSource kantonBB = postgis.getFeatureSource(sourceTypeName);
		SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
		for (String att : attributes) {
			builder.add(kantonBB.getSchema().getDescriptor(att));
		}
		builder.setName(cacheTypeName);
		cache.createSchema(builder.buildFeatureType());
		SimpleFeatureStore featureStore = (SimpleFeatureStore) cache.getFeatureSource(cacheTypeName);
		featureSource = featureStore;
		featureStore.addFeatures(kantonBB.getFeatures(new Query(sourceTypeName, Filter.INCLUDE, attributes)));
		return featureSource;
	}
	
}
