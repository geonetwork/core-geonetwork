package org.fao.geonet.services.region;

import java.io.IOException;

import jeeves.server.context.ServiceContext;

import org.fao.geonet.constants.Geonet;
import org.geotools.data.DataStore;
import org.geotools.data.Query;
import org.geotools.data.SchemaNotFoundException;
import org.geotools.data.memory.MemoryDataStore;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.data.simple.SimpleFeatureStore;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.filter.Filter;

public class DatastoreCache {
	private MemoryDataStore cache = new MemoryDataStore(); 

	private SimpleFeatureSource loadIntoMemory(ServiceContext context, DatastoreMapper mapper, boolean simplified, boolean inLatLong)
			throws IOException {
		String[] propNames = mapper.propNames(simplified, true);
		String sourceTypeName = mapper.getBackingDatastoreName(simplified, inLatLong);
		String cacheTypeName = mapper.getBackingDatastoreName(simplified, inLatLong);

		DataStore postgis = (DataStore) context.getApplicationContext().getBean(Geonet.BeanId.DATASTORE);
		SimpleFeatureSource kantonBB = postgis.getFeatureSource(sourceTypeName);
		SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
		for (String att : propNames) {
			AttributeDescriptor descriptor = kantonBB.getSchema().getDescriptor(att);
			builder.add(descriptor);
		}
		builder.setName(cacheTypeName);
		cache.createSchema(builder.buildFeatureType());
		SimpleFeatureStore featureStore = (SimpleFeatureStore) cache.getFeatureSource(cacheTypeName);
		SimpleFeatureCollection features = kantonBB.getFeatures(new Query(sourceTypeName, Filter.INCLUDE, propNames));
		featureStore.addFeatures(features);
		return featureStore;
	}

	public SimpleFeatureSource getCached(ServiceContext context, DatastoreMapper mapper, boolean simplified, boolean inLatLong) throws IOException {
		SimpleFeatureSource featureSource;
		String cacheTypeName = mapper.getBackingDatastoreName(simplified, inLatLong);
		try {
			featureSource = cache.getFeatureSource(cacheTypeName);
		} catch (SchemaNotFoundException e) {
			featureSource = loadIntoMemory(context, mapper, simplified, inLatLong);
		}
		return featureSource;
	}

	
}
