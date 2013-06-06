package org.fao.geonet.lib;

import jeeves.resources.dbms.Dbms;

public enum DatabaseType {
	defaultType(null), sqlserver("jdbc:sqlserver:"), postgis("jdbc:postgresql_postGIS:"), postgres("jdbc:postgresql:"), mysql("jdbc:mysql:"), db2("jdbc:db2:"), mckoi("jdbc:mckoi:"), oracle("jdbc:oracle:");
	
	private final String urlPrefix;

	private DatabaseType(String urlPrefix) {
		this.urlPrefix = urlPrefix;
	}
	
	public static DatabaseType lookup(Dbms dbms) {
		String url = dbms.getURL();
		if(url == null) {
			return defaultType;
		}
		for (DatabaseType dbType : values()) {
			if(dbType != defaultType && url.startsWith(dbType.urlPrefix)) {
				return dbType;
			}
		}
		
		return defaultType;
	}
	
	@Override
	public String toString() {
		if(this == defaultType) {
			return "default";
		} else {
			return super.toString();
		}
	}
}
