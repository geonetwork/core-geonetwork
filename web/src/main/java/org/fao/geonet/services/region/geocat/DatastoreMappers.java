package org.fao.geonet.services.region.geocat;

public enum DatastoreMappers {

	KANTONE(new KantoneMapper()), 
	GEMEINDEN(new GemeindenMapper()), 
	COUNTRY(new CountryMapper()),
	XLINKS(new XLinkMapper("xlinks")),
	NON_VALIDATED(new XLinkMapper("non_validated"));

	public static DatastoreMapper find(String regionId) {
		for (DatastoreMappers mapper : DatastoreMappers.values()) {
			if(mapper.mapper.accepts(regionId)) {
				return mapper.mapper;
			}
		}
		throw new IllegalArgumentException(regionId+" does not have a acceptable id.");
	}
	
	public final DatastoreMapper mapper;

	private DatastoreMappers(DatastoreMapper mapper) {
		this.mapper = mapper;
	}
	
}
