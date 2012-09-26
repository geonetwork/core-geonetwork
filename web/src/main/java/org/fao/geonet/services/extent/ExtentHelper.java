package org.fao.geonet.services.extent;

import java.io.IOException;
import java.io.StringReader;
import java.util.*;

import com.vividsolutions.jts.geom.*;
import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;
import jeeves.utils.Xml;

import org.fao.geonet.kernel.search.spatial.Pair;
import org.fao.geonet.services.extent.Source.FeatureType;
import org.fao.geonet.util.LangUtils;
import org.fao.geonet.util.XslUtil;
import org.geotools.data.FeatureStore;
import org.geotools.data.Query;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.factory.GeoTools;
import org.geotools.feature.FeatureIterator;
import org.geotools.filter.SortByImpl;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.jdom.Content;
import org.jdom.Element;
import org.jdom.Text;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory2;
import org.opengis.filter.sort.SortBy;
import org.opengis.filter.sort.SortOrder;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;

public class ExtentHelper
{

    public static final String DESC                    = "desc";
    public static final String GEO_ID                  = "geoId";
    public static final String ID                      = "id";
    public static final String FEATURE                 = "feature";
    public static final String SOURCE                  = "wfs";
    public static final String DESC_COLUMN             = "descColumn";
    public static final String GEO_ID_COLUMN           = "geoIdColumn";
    public static final String ID_COLUMN               = "idColumn";
    public static final String TYPENAME                = "typename";
    public static final String FEATURE_TYPE            = "featureType";
    public static final String RESPONSE                = "response";

    public static final String DEFAULT_SOURCE_ID = "default";
    public static final String DEFAULT_ID_COLUMN       = "id";
    public static final String MODIFIABLE_FEATURE_TYPE = "modifiable";
    public static final String GEOM                    = "geom";
    public static final String FORMAT                  = "format";
    public static final String SELECTION               = "extent.selection";
    public static final String SELECTED                = "selected";
    public static final String NUM_RESULTS             = "numresults";
    public static final String CLEAR_SELECTION         = "clearselection";
    public static final String CRS_PARAM = "crs";
    public static final String EXTENT_TYPE_CODE        = "extenttypecode";
    public static final int COORD_DIGITS = 3;
    public static final CoordinateReferenceSystem WGS84 = DefaultGeographicCRS.WGS84;
    public static final CoordinateReferenceSystem CH03;
    public static final MathTransform CH03_TO_WGS84;
    public static final MathTransform WGS84_TO_CH03;

    static {
        try {
            CH03 = CRS.decode("EPSG:21781");
            CH03_TO_WGS84 = CRS.findMathTransform(CH03, WGS84, true);
            WGS84_TO_CH03 = CH03_TO_WGS84.inverse();
        } catch (Exception e) {
            throw new Error(e);
        }
    }

    public static Element error(String msg)
    {
        final Element error = new Element("error");
        error.addContent(msg);
        return error;
    }

    public static ExtentSelection getSelection(ServiceContext context)
    {
        final UserSession userSession = context.getUserSession();
        ExtentSelection selection = (ExtentSelection) userSession.getProperty(SELECTION);
        if (selection == null) {
            selection = new ExtentSelection();
            userSession.setProperty(SELECTION, selection);
        }
        return selection;
    }

    private static Geometry transformToCompatibleGeometry(Geometry geometry, Class<?> geometryType)
    {
        if (geometryType.isAssignableFrom(geometry.getClass())) {
            return geometry;
        }




        final Geometry finalGeom;
        final GeometryFactory factory = geometry.getFactory();
        if (MultiPolygon.class.isAssignableFrom(geometryType) && geometry instanceof Polygon) {
            finalGeom = factory.createMultiPolygon(new Polygon[] { (Polygon) geometry });
        } else if (MultiLineString.class.isAssignableFrom(geometryType) && geometry instanceof LineString) {
            finalGeom = factory.createMultiLineString(new LineString[] { (LineString) geometry });
        } else if (MultiPoint.class.isAssignableFrom(geometryType) && geometry instanceof Point) {
            finalGeom = factory.createMultiPoint(new Point[] { (Point) geometry });
        } else if (Polygon.class.isAssignableFrom(geometryType) && geometry instanceof MultiPolygon
                && geometry.getNumGeometries() == 1) {
            finalGeom = geometry.getGeometryN(0);
        } else if (LineString.class.isAssignableFrom(geometryType) && geometry instanceof LineString
                && geometry.getNumGeometries() == 1) {
            finalGeom = geometry.getGeometryN(0);
        } else if (Point.class.isAssignableFrom(geometryType) && geometry instanceof Point && geometry.getNumGeometries() == 1) {
            finalGeom = geometry.getGeometryN(0);
        } else if (GeometryCollection.class.isAssignableFrom(geometryType)) {
            finalGeom = factory.createGeometryCollection(new Geometry[] { geometry });
        } else {
            throw new IllegalArgumentException(geometry + " cannot be converted to " + geometryType);
        }

        finalGeom.setSRID(geometry.getSRID());
        return finalGeom;
    }

    public static int tosrid(String srs)
    {

        int index = srs.indexOf(':');
        if (index == -1) {
            index = 0;
        }
        String number = srs.substring(index + 1);
        try {
            return Integer.parseInt(number);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public static String findNextId(FeatureStore<SimpleFeatureType, SimpleFeature> store, FeatureType featureType)
            throws IOException
    {
        final Query query = featureType.createQuery(new String[] { featureType.idColumn });
        final FilterFactory2 filterFactory = CommonFactoryFinder.getFilterFactory2(GeoTools.getDefaultHints());
        final SortBy[] sortBy = { new SortByImpl(filterFactory.property(featureType.idColumn), SortOrder.ASCENDING) };
        query.setSortBy(sortBy);
        final FeatureIterator<SimpleFeature> features = store.getFeatures(query).features();
        int max = 0;
        try {
            while (features.hasNext()) {
                // double because that is how it is represented in postgis
                int i = (int) Double.parseDouble(features.next().getAttribute(featureType.idColumn).toString());
                if (i > max) {
                    max = i;
                }
            }
        } finally {
            features.close();
        }
        return String.valueOf(max + 1);
    }

    public static Geometry prepareGeometry(final String requestCrsCode, final FeatureType featureType,
            Geometry geometry, final SimpleFeatureType schema) throws NoSuchAuthorityCodeException, FactoryException,
            TransformException
    {
        String featureTypeSrs = featureType.srs();
        if (!featureTypeSrs.equalsIgnoreCase(requestCrsCode)) {
            CoordinateReferenceSystem requestCrs = CRS.decode(requestCrsCode);
            final CoordinateReferenceSystem targetCrs = featureType
                    .projection();
            geometry = JTS.transform(geometry, CRS.findMathTransform(requestCrs, targetCrs, true));
        }

        geometry = transformToCompatibleGeometry(geometry, schema.getGeometryDescriptor().getType().getBinding());

        geometry = validateGeom(geometry);
        geometry.setSRID(tosrid(featureTypeSrs));
        return geometry;
    }

    private static Geometry validateGeom(Geometry geometry) {
        Geometry valid = geometry.buffer(0);
        valid.setSRID(geometry.getSRID());
        if(valid instanceof Polygon) {
            final MultiPolygon multiPolygon = valid.getFactory().createMultiPolygon(new Polygon[]{(Polygon) valid});
            multiPolygon.setSRID(geometry.getSRID());
            return multiPolygon;
        }
        return valid;
    }

    /**
     * Adds a gml ID to the user data of geometry and all subgeoms so that GML
     * encoding will produce valid geoms
     * 
     * @param geometry
     */
    public static void addGmlId(Geometry geometry)
    {
        HashMap<String, String> map = new HashMap<String, String>();
        Object userData = geometry.getUserData();
        String srs = "4326";
        if (userData instanceof String) {
            srs = (String) userData;

        } else if (userData instanceof CoordinateReferenceSystem) {
            try {
            srs = ""+CRS.lookupEpsgCode((CoordinateReferenceSystem)userData, false);
            } catch (Exception e) {
                //Assume latlong
            }

        }
        map.put("srs", srs);
        map.put("gml:id", 'N' + UUID.randomUUID().toString().replaceAll("-", ""));
        geometry.setUserData(map);
        if (geometry instanceof GeometryCollection && geometry.getNumGeometries() > 0) {
            for (int i = 0; i < geometry.getNumGeometries(); i++) {
                addGmlId(geometry.getGeometryN(i));
            }
        }
    }

    public static String encodeDescription(String description)
    {
        if (description == null) {
            return null;
        }
        final String cleaned;
        if(description.trim().startsWith("<![CDATA[")) {
            cleaned = description;
        } else {
            cleaned = "<![CDATA["+description+"]]>";
        }
        return cleaned;
    }

    public static String decodeDescription(String description)
    {
        String decoded = description;
        if (decoded == null) {
            return null;
        }
        while (decoded.startsWith("\"") && decoded.endsWith("\"")) {
            decoded = decoded.substring(1, decoded.length() - 1);
        }
        decoded = decoded.replaceAll("@dquot;", "\"").replaceAll("@squot;", "'").replace("<![CDATA[","").replace("]]>","");

        return decoded;
    }

    public static String reduceDesc(String desc) throws Exception
    {

        StringBuilder strings = new StringBuilder();
        Element parsed = Xml.loadString("<desc>" + desc + "</desc>", false);
        Iterator iter = parsed.getDescendants();
        while (iter.hasNext()) {
            Content next = (Content) iter.next();
            if (next instanceof Text) {
                strings.append(((Text) next).getText());
                strings.append(" ");
            }
        }

        StringBuilder result = new StringBuilder();
        for (String s : LangUtils.analyzeForSearch(new StringReader(strings.toString()))) {
            result.append(" ").append(s);
        }
        return result.toString();
    }

    public static Geometry reducePrecision(Geometry geometry, CoordinateReferenceSystem crs) {

        int decimals = COORD_DIGITS;
        // should check if meters or degrees but this is a quick hack that should work
        try {
            int code = CRS.lookupEpsgCode(crs, false);
            if(code == 21781) {
              decimals = 0;
            }
        } catch (Exception e) {
            // its ok
        }
        
        Coordinate[] coords = geometry.getCoordinates();
        for (Coordinate coord : coords) {
            coord.x = reducePrecision(coord.x, decimals);
            coord.y = reducePrecision(coord.y, decimals);
            coord.z = Double.NaN;
        }

        return geometry;
    }

    public static double reducePrecision(double x, int decimals) {
        if(decimals == 0) {
            return (long)x;
        }
        final double precision = Math.pow(10,decimals);
        long i = Math.round(x * precision);
        final double newVal = ((double) i) / precision;
        return newVal;
    }

    public static double decimal(Element child)
    {
        Element decimalElem = child.getChild("Decimal", XslUtil.GCO_NAMESPACE);
        if (decimalElem == null)
            throw new NoSuchElementException(child.getName() + " does not have a valid decimal child element");

        final String dec = decimalElem.getTextTrim();
        if(dec.endsWith("ch")) {
            return reducePrecision(Double.valueOf(dec.substring(0,dec.length()-2)), 0);
        } else {
            return reducePrecision(Double.valueOf(dec), COORD_DIGITS);
        }
    }

    public static Pair<ExtentTypeCode, MultiPolygon> diff(GeometryFactory fac, MultiPolygon inclusion,
            MultiPolygon exclusion)
    {

        try {
            if(exclusion.getSRID() == 21781 && inclusion.getSRID() != 21781) {
                exclusion = (MultiPolygon) JTS.transform(exclusion, CH03_TO_WGS84);
                exclusion.setSRID(4326);
            } else if(inclusion.getSRID() == 21781 && exclusion.getSRID() != 21781) {
                inclusion = (MultiPolygon) JTS.transform(inclusion, CH03_TO_WGS84);
                inclusion.setSRID(4326);
            }
        } catch (TransformException e) {
            throw new RuntimeException(e);
        }

        final ExtentTypeCode typeCode;
        final Geometry geom;
        if (exclusion.contains(inclusion)) {
            geom = exclusion.difference(inclusion);
            typeCode = ExtentTypeCode.EXCLUDE;
        } else {
            geom = inclusion.difference(exclusion);
            typeCode = ExtentTypeCode.INCLUDE;
        }
    
        if (geom instanceof Polygon) {
            Polygon polygon = (Polygon) geom;
            return Pair.read(typeCode, fac.createMultiPolygon(new Polygon[] { polygon }));
        }
        return Pair.read(typeCode, (MultiPolygon) geom);
    }

    public static MultiPolygon joinPolygons(GeometryFactory fac, Collection<Polygon> collection)
    {
        final Collection<Polygon> transformedCollection = transformGeometry(collection);
        final Geometry geometry = fac.buildGeometry(transformedCollection);
        geometry.setSRID(transformedCollection.iterator().next().getSRID());

        Geometry geom = validateGeom(geometry);
        return (MultiPolygon) geom;
    }

    /**
     * converts all geoms to same CRS if one is 4326 then all go to 4326 otherwise all stay the same
     * This assumes only options are ch03 and wgs84
     */
    private static Collection<Polygon> transformGeometry(Collection<Polygon> collection) {
        boolean hasWGS84 = false;
        boolean hasCH03 = false;

        for (Polygon polygon : collection) {
            if(polygon.getSRID() == 21781) {
                hasCH03 = true;
            } else {
                hasWGS84 = true;
            }
        }

        if(hasWGS84 && hasCH03) {
            ArrayList<Polygon> newCollection = new ArrayList<Polygon>();
            for (Polygon polygon : collection) {
                if(polygon.getSRID() == 21781) {
                    try {
                        Geometry newP = JTS.transform(polygon, CH03_TO_WGS84);
                        newP.setSRID(21781);
                        newCollection.add((Polygon) newP);
                    } catch (TransformException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    newCollection.add(polygon);
                }
            }
            return newCollection;
        } else {
            return collection;
        }

    }

    public static int bboxSrid(Element bboxElem) {
        Element elem = bboxElem.getChild("westBoundLongitude", XslUtil.GMD_NAMESPACE);
        if(elem == null)throw new NoSuchElementException(bboxElem.getName() + " does not have wesBoundLongitude");

        Element decElem = elem.getChild("Decimal", XslUtil.GCO_NAMESPACE);
        if (decElem == null)
            throw new NoSuchElementException(elem.getName() + " does not have a valid decimal child element");
        return decElem.getTextTrim().endsWith("ch")?21781:4326;
    }

    public enum ExtentTypeCode
    {
        INCLUDE, EXCLUDE, NA
    }

    public static double tolerance() {
        final double precision = Math.pow(10,COORD_DIGITS);
        
        return 1/precision;
    }

}
