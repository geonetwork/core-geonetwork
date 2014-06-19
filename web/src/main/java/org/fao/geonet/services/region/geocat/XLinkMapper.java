package org.fao.geonet.services.region.geocat;

import java.io.IOException;

import org.fao.geonet.constants.Geonet;
import org.fao.geonet.services.region.Region;
import org.geotools.data.DataStore;
import org.geotools.data.simple.SimpleFeatureSource;
import org.jdom.JDOMException;
import org.opengis.feature.simple.SimpleFeature;

public class XLinkMapper extends DatastoreMapper {
	private static final String DESC_COLUMN = "DESC";
	private static final String NAME_COLUMN = "GEO_ID";
	private static final String ID_COLUMN = "ID";

	private final String typeName;
	private final String prefix;

	public XLinkMapper(String typeName) {
		this.typeName = typeName;
		this.prefix = typeName+":";
	}
	
	@Override
	public boolean accepts(String regionId) {
		return regionId.startsWith(prefix);
	}

	@Override
	public String categoryId() {
		return typeName;
	}

	@Override
	protected String idPropertyName() {
		return ID_COLUMN;
	}
	
	@Override
	public String[] propNames(boolean simplified, boolean includeGeom, boolean inLatLong) {
		if(simplified) {
			if(includeGeom) {
				return new String[]{ID_COLUMN, NAME_COLUMN, DESC_COLUMN, THE_GEOM};
			} else {
				return new String[]{ID_COLUMN, NAME_COLUMN, DESC_COLUMN};
			}
		} else {
			if(includeGeom) {
				return new String[]{ID_COLUMN, NAME_COLUMN, THE_GEOM};
			} else {
				return new String[]{ID_COLUMN, NAME_COLUMN};
			}
		}
	}

	@Override
	public Region constructRegion(MapperState state, SimpleFeature next) throws JDOMException, IOException {
		return super.constructRegion(state, next, prefix,NAME_COLUMN, typeName);
	}

	@Override
	public String getBackingDatastoreName(boolean simplified, boolean inLatLong) {
		return typeName;
	}

	@Override
	protected SimpleFeatureSource getFeatureSource(MapperState state, boolean simplified, boolean inLatLong) throws IOException {
		DataStore ds = state.context.getApplicationContext().getBean(Geonet.BeanId.DATASTORE, DataStore.class);
		return ds.getFeatureSource(typeName);

	}

}
