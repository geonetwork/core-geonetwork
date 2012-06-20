package org.fao.geonet.constants;

import com.vividsolutions.jts.geom.Geometry;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.opengis.feature.simple.SimpleFeatureType;

/**
 * Geocat constants
 */
public class Geocat {
    public static class Params {
        public static final String ACTION 		= "action";
        public static final String POSITIONNAME = "positionname";
    	public static final String STREETNUMBER = "streetnumber";
    	public static final String STREETNAME   = "streetname";
    	public static final String POSTBOX      = "postbox";
    	public static final String STATE        = "state";
    	public static final String ZIP          = "zip";
    	public static final String COUNTRY      = "country";
    	public static final String IS_UPLOAD    = "isUpload";
    	public static final String EMAIL        = "email";
    	public static final String IMAGE        = "image";
    	public static final String PHONE        = "phone";
    	public static final String FAC          = "facsimile";
    	public static final String ORG          = "org";
    	public static final String ONLINE       = "onlineresource";
    	public static final String HOURSOFSERV  = "hoursofservice";
    	public static final String CONTACTINST  = "contactinstructions";
    	public static final String PUBLICACC    = "publicaccess";
    	public static final String ORGACRONYM   = "orgacronym";
    	public static final String DIRECTNUMBER = "directnumber";
    	public static final String MOBILE 		= "mobile";
    	
    	public static final String WEBSITE 		= "website";
        public static final String VALIDATED    = "validated";

    }

    public static class Spatial {
        public static final String IDS_ATTRIBUTE_NAME = "id";
        public static final String GEOM_ATTRIBUTE_NAME = "the_geom";
        public static final String SPATIAL_INDEX_TYPENAME = "spatialindex";

        public static final String SPATIAL_FILTER_JCS = "SpatialFilterCache";
        public static final SimpleFeatureType FEATURE_TYPE;

        static {
            SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
            builder.add(GEOM_ATTRIBUTE_NAME, Geometry.class, DefaultGeographicCRS.WGS84);
            builder.setDefaultGeometry(GEOM_ATTRIBUTE_NAME);
            builder.setName(SPATIAL_INDEX_TYPENAME);
            FEATURE_TYPE = builder.buildFeatureType();
        }

    }

    public class Profile {
        public static final String SHARED = "Shared";
    }

    public class Config {
        public static final String EXTENT_CONFIG = "extent";
        public static final String REUSABLE_OBJECT_CONFIG = "reusable";
    }

    public class Module {
        public static final String EXTENT = "extent";
        public static final String REUSABLE = "reusable";
        public static final String MONITORING = "monitoring";
    }

	public static final String DEFAULT_LANG = "eng";
	public static final String LUCENE_LOCALE_KEY = "_lucene";
    public static final String INSPIRE_SCHEMATRON_ID = "schematron-rules-inspire";
    
}
