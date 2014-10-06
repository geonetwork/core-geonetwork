package org.fao.geonet.util;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.io.Resources;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.io.WKTWriter;
import jeeves.exceptions.JeevesException;
import jeeves.server.ProfileManager;
import jeeves.server.context.ServiceContext;
import jeeves.utils.Log;
import jeeves.utils.TransformerFactoryFactory;
import net.sf.saxon.Configuration;
import net.sf.saxon.om.Axis;
import net.sf.saxon.om.AxisIterator;
import net.sf.saxon.om.DocumentInfo;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.SingletonIterator;
import net.sf.saxon.om.UnfailingIterator;
import net.sf.saxon.type.Type;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.search.LuceneSearcher;
import org.fao.geonet.kernel.search.spatial.Pair;
import org.fao.geonet.kernel.search.spatial.SpatialIndexWriter;
import org.fao.geonet.languages.IsoLanguagesMapper;
import org.fao.geonet.services.extent.ExtentHelper;
import org.fao.geonet.services.extent.ExtentHelper.ExtentTypeCode;
import org.geotools.gml3.GMLConfiguration;
import org.geotools.xml.Encoder;
import org.geotools.xml.Parser;
import org.jdom.Namespace;
import org.json.XML;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

/**
 * These are all extension methods for calling from xsl docs. Note: All params
 * are Objects because it is hard to determine what is passed in from XSLT. Most
 * are converted to string by calling tostring.
 *
 * @author jesse
 */
public final class XslUtil {

    private static final GMLConfiguration GML3_CONFIG = new org.geotools.gml3.GMLConfiguration();
    static{
    	GML3_CONFIG.getProperties().add(org.geotools.gml3.GMLConfiguration.NO_SRS_DIMENSION);
    }
    private static final org.geotools.gml2.GMLConfiguration GML2_CONFIG = new org.geotools.gml2.GMLConfiguration();
    private static final Random RANDOM = new Random();

    public static final Namespace GMD_NAMESPACE = Namespace.getNamespace("gmd",
            "http://www.isotc211.org/2005/gmd");
    public static final Namespace SRV_NAMESPACE = Namespace.getNamespace("srv",
            "http://www.isotc211.org/2005/srv");
    public static final Namespace CHE_NAMESPACE = Namespace.getNamespace("che",
            "http://www.geocat.ch/2008/che");
    public static final Namespace GCO_NAMESPACE = Namespace.getNamespace("gco",
            "http://www.isotc211.org/2005/gco");
    public static final Namespace XSI_NAMESPACE = Namespace.getNamespace("xsi",
            "http://www.w3.org/2001/XMLSchema-instance");

    private static final char TS_DEFAULT = ' ';
    private static final char CS_DEFAULT = ',';
    private static final char TS_WKT = ',';
    private static final char CS_WKT = ' ';

    private static Parser gml3Parser() {
        return new Parser(GML3_CONFIG);
    }

    private static Parser gml2Parser() {
        return new Parser(GML2_CONFIG);
    }


    /**
     * clean the src of ' and <>
     */
    public static String clean(Object src) {
        String result = src.toString().replaceAll("'", "\'").replaceAll("[><\n\r]", " ");
        return result;
    }

    /**
     * Convert 1E3 to 1000 for example
     */
    public static String expandScientific(Object src) {
        try {
            String original = src.toString().toUpperCase();
            String result = original;
            if (original.contains("E")) {
                String[] parts = original.split("E");
                double coefficient = Double.parseDouble(parts[0]);
                double exponentRaw = Double.parseDouble(parts[1]);
                double multiplier = Math.pow(10.0, exponentRaw);
                double expanded = coefficient * multiplier;
                result = String.valueOf(expanded);
            }
            return result;
        } catch (NumberFormatException e) {
            return src.toString();
        }
    }

    /**
     * Returns 'true' if the pattern match the src
     */
    public static String countryMatch(Object src, Object pattern) {
        if (src.toString().trim().length() == 0) {
            return "false";
        }
        boolean result = src.toString().toLowerCase().contains(pattern.toString().toLowerCase());
        return String.valueOf(result);
    }

    /**
     * Replace the pattern with the substitution
     */
    public static String replace(Object src, Object pattern, Object substitution) {
        String result = src.toString().replaceAll(pattern.toString(), substitution.toString());
        return result;
    }
    
    public static boolean isCasEnabled() {
		return ProfileManager.isCasEnabled();
	}
    /** 
	 * Check if bean is defined in the context
	 * 
	 * @param beanId id of the bean to look up
	 */
	public static boolean existsBean(String beanId) {
		return ProfileManager.existsBean(beanId);
	}
    /**
	 * Optimistically check if user can access a given url.  If not possible to determine then
	 * the methods will return true.  So only use to show url links, not check if a user has access
	 * for certain.  Spring security should ensure that users cannot access restricted urls though.
	 *  
	 * @param serviceName the raw services name (main.home) or (admin) 
	 * 
	 * @return true if accessible or system is unable to determine because the current
	 * 				thread does not have a ServiceContext in its thread local store
	 */
	public static boolean isAccessibleService(Object serviceName) {
		return ProfileManager.isAccessibleService(serviceName);
	}
    /**
     * Takes the characters until the pattern is matched
     */
    public static String takeUntil(Object src, Object pattern)
    {
        String src2 = src.toString();
        Matcher matcher = Pattern.compile(pattern.toString()).matcher(src2);

        if( !matcher.find() )
            return src2;

        int index = matcher.start();

        if( index==-1 ){
            return src2;
        }
        return src2.substring(0,index);
    }

    /**
     * Get field value for metadata identified by uuid. "" if uuid not found.
     *
     * @param appName Name of the webapplication to allow lookup of lucene index directory
     * @param uuid    Metadata uuid
     * @param field   Field name
     * @param lang    Language of the index to search in
     * @return metadata title
     */
    public static String getIndexField(Object appName, Object uuid, Object field, Object lang) {
        String webappName = appName.toString();
        String id = uuid.toString();
        String fieldname = field.toString();
        String language = (lang.toString().equals("") ? null : lang.toString());
        try {
            String fieldValue = LuceneSearcher.getMetadataFromIndex(webappName, language, id, fieldname);
            if(fieldValue == null) {
                return getIndexFieldById(appName,uuid,field,lang);
            }
            return fieldValue == null ? "" : fieldValue;
        } catch (Exception e) {
            Log.error(Geonet.GEONETWORK, "Failed to get index field value caused by " + e.getMessage());
            return "";
        }
    }

    public static String getIndexFieldById(Object appName, Object id, Object field, Object lang) {
        String webappName = appName.toString();
        String fieldname = field.toString();
        String language = (lang.toString().equals("") ? null : lang.toString());
        try {
            String fieldValue = LuceneSearcher.getMetadataFromIndexById(webappName, language, id.toString(), fieldname);
            return fieldValue == null ? "" : fieldValue;
        } catch (Exception e) {
            Log.error(Geonet.GEONETWORK, "Failed to get index field value caused by " + e.getMessage());
            return "";
        }
    }

    /**
     * convert gml geometry to WKT
     */
    public static String gmlToWKT(Node next) throws Exception {
        String writeXml = writeXml(next).replace("&lt;", "<").replace("&gt;", ">");
        if (writeXml.startsWith("<?xml")) {
            writeXml = writeXml.substring(writeXml.indexOf('>') + 1);
        }

        // make sure gml namespace is present since it sometimes gets dropped
        String firstTag = writeXml.substring(0, writeXml.indexOf('>'));
        if (!firstTag.contains("xmlns:gml")) {
            writeXml = firstTag + " xmlns:gml=\"http://www.opengis.net/gml\">" + writeXml.substring(firstTag.length() + 1);
        }

        Object value;
        try {
            value = gml3Parser().parse(new StringReader(writeXml));
        } catch (Exception e) {
            try {
                value = gml2Parser().parse(new StringReader(writeXml));
            } catch (Exception e2) {
                Log.error(Log.WEBAPP, "Unable to parse gml:" + writeXml + " problem: " + e2.getMessage());
                return "";
            }
        }
        Geometry geom = null;
        if (value instanceof HashMap) {

            // This section is unlikely more likely is a single polygon or multipolygon but this is for completeness

            @SuppressWarnings("rawtypes")
            HashMap map = (HashMap) value;

            List<Polygon> geoms = new ArrayList<Polygon>();
            for (Object entry : map.values()) {
                SpatialIndexWriter.addToList(geoms, entry);
            }

            if (geoms.isEmpty()) {
                geom = null;
            } else if (geoms.size() > 1) {
                GeometryFactory fac = geoms.get(0).getFactory();
                geom = fac.buildGeometry(geoms);
            } else {
                geom = geoms.get(0);
            }

        } else if (value == null) {
            geom = null;
        } else {
            geom = (Geometry) value;
        }

        if (geom == null) {
            return "";
        } else {
            return new WKTWriter().write(geom).replaceAll("\\s+", " "); // replace all is to work around bugs in open layers
        }

    }

    public static String trimPosList(Object coords) {
        String[] coordsString = coords.toString().split(" ");

        StringBuilder results = new StringBuilder();

        for (int i = 0; i < coordsString.length; i++) {
            if (i > 0) {
                results.append(' ');
            }
            results.append(reduceDecimals(coordsString[i]));
        }

        return results.toString();
    }

    private static String reduceDecimals(String number) {
        int DECIMALS = 6;
        try {
            // verify this is a number
            Double.parseDouble(number);

            String[] parts = number.split("\\.");

            if (parts.length > 1 && parts[1].length() > DECIMALS) {
                return parts[0] + '.' + parts[1].substring(0, DECIMALS);
            } else {
                return number;
            }
        } catch (Exception e) {
            return number;
        }
    }

    /**
     * Converts the seperators of the coords to the WKT from ts and cs
     *
     * @param coords the coords string to convert
     * @param ts the separator that separates 2 coordinates
     * @param cs the separator between 2 numbers in a coordinate
     */
    public static String toWktCoords(Object coords, Object ts, Object cs){
        String coordsString = coords.toString();
        char tsString;
        if( ts==null || ts.toString().length()==0){
            tsString = TS_DEFAULT;
        }else{
            tsString = ts.toString().charAt(0);
        }
        char csString;
        if( cs==null || cs.toString().length()==0){
            csString = CS_DEFAULT;
        }else{
            csString = cs.toString().charAt(0);
        }

        if( tsString == TS_WKT && csString == CS_WKT ){
            return coordsString;
        }

        if( tsString == CS_WKT ){
            tsString=';';
            coordsString = coordsString.replace(CS_WKT, tsString);
        }
        coordsString = coordsString.replace(csString, CS_WKT);
        String result = coordsString.replace(tsString, TS_WKT);
        char lastChar = result.charAt(result.length()-1);
        if(result.charAt(result.length()-1)==TS_WKT || lastChar==CS_WKT ){
            result = result.substring(0, result.length()-1);
        }
        return result;
    }


    public static String posListToWktCoords(Object coords, Object dim){
        String[] coordsString = coords.toString().split(" ");

        int dimension;
        if( dim==null ){
            dimension = 2;
        }else{
            try{
                dimension=Integer.parseInt(dim.toString());
            }catch (NumberFormatException e) {
                dimension=2;
            }
        }
        StringBuilder results = new StringBuilder();

        for (int i = 0; i < coordsString.length; i++) {
            if( i>0 && i%dimension==0 ){
                results.append(',');
            }else if( i>0 ){
                results.append(' ');
            }
            results.append(coordsString[i]);
        }

        return results.toString();
    }

    public static Object posListToGM03Coords(Object node, Object coords, Object dim) {

        String[] coordsString = coords.toString().split("\\s+");

        if (coordsString.length % 2 != 0) {
            return "Error following data is not correct:" + coords.toString();
        }

        int dimension;
        if (dim == null) {
            dimension = 2;
        } else {
            try {
                dimension = Integer.parseInt(dim.toString());
            } catch (NumberFormatException e) {
                dimension = 2;
            }
        }
        StringBuilder results = new StringBuilder("<POLYLINE  xmlns=\"http://www.interlis.ch/INTERLIS2.3\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">");

        for (int i = 0; i < coordsString.length; i++) {
            if (i % dimension == 0) {
                results.append("<COORD><C1>");
                results.append(coordsString[i]);
                results.append("</C1>");
            } else if (i > 0) {
                results.append("<C2>");
                results.append(coordsString[i]);
                results.append("</C2></COORD>");
            }
        }

        results.append("</POLYLINE>");
        try {
            Source source = new StreamSource(new ByteArrayInputStream(results.toString().getBytes("UTF-8")));
            DocumentInfo d = ((NodeInfo)node).getConfiguration().buildDocument(source);
            return SingletonIterator.makeIterator(d.iterateAxis(Axis.CHILD).next());
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    public static String randomId() {
        return "N" + Math.abs(RANDOM.nextLong());
    }


    public static Object bbox(Object description, Object src) throws Exception {

        final NodeInfo ni = (NodeInfo) src;
        return combineAndWriteGeom(description, SingletonIterator.makeIterator(ni), new GeomWriter() {

            public Object write(ExtentTypeCode code, MultiPolygon geometry) throws Exception {

                Envelope bbox = geometry.getEnvelopeInternal();

                String template = "<gmd:EX_GeographicBoundingBox xmlns:gml=\"http://www.opengis.net/gml\" xmlns:gco=\"http://www.isotc211.org/2005/gco\" xmlns:gmd=\"http://www.isotc211.org/2005/gmd\">"+
                "<gmd:extentTypeCode>"+
                  "<gco:Boolean>%s</gco:Boolean>"+
                "</gmd:extentTypeCode>"+
                "<gmd:westBoundLongitude>"+
                  "<gco:Decimal>%s</gco:Decimal>"+
                "</gmd:westBoundLongitude>"+
                "<gmd:eastBoundLongitude>"+
                "<gco:Decimal>%s</gco:Decimal>"+
                "</gmd:eastBoundLongitude>"+
                "<gmd:southBoundLatitude>"+
                  "<gco:Decimal>%s</gco:Decimal>"+
                "</gmd:southBoundLatitude>"+
                "<gmd:northBoundLatitude>"+
                  "<gco:Decimal>%s</gco:Decimal>"+
                "</gmd:northBoundLatitude>"+
              "</gmd:EX_GeographicBoundingBox>";

                String extentTypeCode = code == ExtentTypeCode.EXCLUDE ? "false" : "true";
                String xml = String.format(template, extentTypeCode, bbox.getMinX(), bbox.getMinY(), bbox.getMaxX(), bbox.getMaxY());

                Source source = new StreamSource(new ByteArrayInputStream(xml.getBytes("UTF-8")));
                DocumentInfo doc = ni.getConfiguration().buildDocument(source);
                return SingletonIterator.makeIterator(doc);
            }
        });
    }


    public static Object multipolygon(Object description, Object src) throws Exception {

        final NodeInfo ni = ((NodeInfo) src);
        return combineAndWriteGeom(description, SingletonIterator.makeIterator(ni), new GeomWriter() {

            public Object write(ExtentTypeCode code, MultiPolygon geometry) throws Exception {
            	geometry.setUserData(null);
                final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                final Encoder encoder = new Encoder(GML3_CONFIG);
                encoder.setIndenting(false);
                encoder.setOmitXMLDeclaration(true);
                encoder.setEncoding(Charset.forName("UTF-8"));
                ExtentHelper.addGmlId(geometry);
                encoder.encode(geometry, org.geotools.gml3.GML.geometryMember, outputStream);

                StringBuilder builder = new StringBuilder("<gmd:EX_BoundingPolygon xmlns:gml=\"http://www.opengis.net/gml\" xmlns:gco=\"http://www.isotc211.org/2005/gco\" xmlns:gmd=\"http://www.isotc211.org/2005/gmd\"><gmd:extentTypeCode><gco:Boolean>").
                    append(code == ExtentTypeCode.EXCLUDE ? "false" : "true").
                    append("</gco:Boolean></gmd:extentTypeCode><gmd:polygon>");

                Source xml1 = new StreamSource(new ByteArrayInputStream(outputStream.toByteArray()));
                DocumentInfo doc1 = ni.getConfiguration().buildDocument(xml1);
                AxisIterator iter = doc1.iterateAxis(Axis.CHILD);
                NodeInfo next = (NodeInfo) iter.next();

                while(next !=null) {
                    AxisIterator iter2 = next.iterateAxis(Axis.CHILD);
                    Item next2 = iter2.next();

                    while(next2 !=null) {
                        if (next2 instanceof NodeInfo & ((NodeInfo)next2).getNodeKind() == Type.ELEMENT) {
                            NodeInfo info = (NodeInfo) next2;

                            String nodeXml = writeXml(info).replaceAll("LinearRing srsDimension=\"\\d\"", "LinearRing");
                            builder.append(nodeXml);
                        }
                        next2 = iter2.next();
                    }
                    next = (NodeInfo) iter.next();
                }

                builder.append("</gmd:polygon></gmd:EX_BoundingPolygon>");

                Source xmlSource = new StreamSource(new ByteArrayInputStream(builder.toString().getBytes("UTF-8")));
                DocumentInfo doc = ni.getConfiguration().buildDocument(xmlSource);

                return SingletonIterator.makeIterator(doc);
            }
        });

    }

    private interface GeomWriter {
        Object write(ExtentTypeCode code, MultiPolygon geometry) throws Exception;
    }


    public static Object combineAndWriteGeom(Object description, UnfailingIterator src, GeomWriter writer) throws Exception {

        try {
            Multimap<Boolean, Polygon> geoms = ArrayListMultimap.create();

            NodeInfo next = (NodeInfo) src.next();

            while (next != null) {
            	if (!next.getLocalPart().equalsIgnoreCase("geographicElement"))
            	{
            		AxisIterator childNodes = next.iterateAxis(Axis.CHILD);

            		NodeInfo nextChild = (NodeInfo) childNodes.next();
            		while (nextChild != null)
            		{
            			geoms.putAll(geometries(nextChild));
                    	nextChild = (NodeInfo) childNodes.next();
            		}

            	}
            	next = (NodeInfo) src.next();
            }

            GeometryFactory fac = new GeometryFactory();

            MultiPolygon inclusion = null;
            if (!geoms.get(true).isEmpty()) {
                inclusion = ExtentHelper.joinPolygons(fac, geoms.get(true));
            }
            MultiPolygon exclusion = null;
            if (!geoms.get(false).isEmpty()) {
                exclusion = ExtentHelper.joinPolygons(fac, geoms.get(false));
            }

            final Object result;

            if (inclusion == null && exclusion == null) {
                result = src;
            } else if (inclusion == null && exclusion != null) {
                result = writer.write(ExtentTypeCode.EXCLUDE, exclusion);
            } else if (inclusion != null && exclusion == null) {
                result = writer.write(ExtentTypeCode.INCLUDE, inclusion);
            } else {
                Pair<ExtentTypeCode, MultiPolygon> diff = ExtentHelper.diff(fac, inclusion, exclusion);
                result = writer.write(diff.one(), diff.two());
            }

            return result;
        } catch (Throwable t) {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            DOMImplementation impl = builder.getDOMImplementation();

            Document ownerDocument = impl.createDocument(null, null, null);
            Element root = ownerDocument.createElement("ERROR");
            root.setAttribute("msg", t.toString().replaceAll("\"", "'"));
            StackTraceElement[] trace = t.getStackTrace();
            for (StackTraceElement stackTraceElement : trace) {
                Element traceElem = ownerDocument.createElement("trace");
                traceElem.setTextContent(stackTraceElement.toString());
                root.appendChild(traceElem);
            }
            return root;
        }
    }

    private static Multimap<Boolean, Polygon> geometries(NodeInfo next) throws Exception {
        Boolean inclusion = inclusion(next);
        inclusion = inclusion == null ? true : inclusion;
        Polygon geom = geom(next);
        Multimap<Boolean, Polygon> geoms = ArrayListMultimap.create();
        geoms.put(inclusion, geom);
        return geoms;
    }



    private static Node findElem(Node next, String name) {
        if (name.equals(next.getLocalName())) {
            return next;
        }
        NodeList childNodes = next.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node node = childNodes.item(i);
            Node result = findElem(node, name);
            if (result != null) {
                return result;
            }
        }
        return null;
    }

    private static Polygon geom(NodeInfo next) throws Exception {

        if ("Polygon".equals(next.getLocalPart())) {
            return parsePolygon(next);
        }
        AxisIterator childNodes = next.iterateAxis(Axis.CHILD);
        NodeInfo curChildNode =  (NodeInfo) childNodes.next();

        while (curChildNode != null) {
        	Polygon geom = geom(curChildNode);
        	if (geom != null)
        	{
        		return geom;
        	}
        	curChildNode = (NodeInfo) childNodes.next();
        }
        return null;
    }

    @SuppressWarnings("rawtypes")
    private static Polygon parsePolygon(NodeInfo next) throws Exception {
        String writeXml = writeXml(next);

        Object value = gml3Parser().parse(new StringReader(writeXml));
        if (value instanceof HashMap) {
            HashMap map = (HashMap) value;
            List<Polygon> geoms = new ArrayList<Polygon>();
            for (Object entry : map.values()) {
                SpatialIndexWriter.addToList(geoms, entry);
            }
            if (geoms.isEmpty()) {
                return null;
            } else if (geoms.size() > 1) {
                throw new AssertionError("Some how multiple polygons were parsed");
            } else {
                return geoms.get(0);
            }

        } else if (value == null) {
            return null;
        } else {
            return (Polygon) value;
        }
    }

    private static Boolean inclusion(NodeInfo next) {
        if ("extentTypeCode".equals(next.getLocalPart())) {
            return booleanText(next);
        }
        AxisIterator childNodes = next.iterateAxis(Axis.CHILD);
        NodeInfo nextChild = (NodeInfo) childNodes.next();

        while (nextChild != null)
        {
            Boolean inclusion = inclusion(nextChild);
            if (inclusion != null) {
                return inclusion;
            }
        	nextChild = (NodeInfo) childNodes.next();

        }
        return null;
    }

    private static Boolean booleanText(NodeInfo next) {
        AxisIterator childNodes = next.iterateAxis(Axis.CHILD);

        NodeInfo nextChild = (NodeInfo) childNodes.next();

        while (nextChild != null)
        {
        	 if ("Boolean".equals(nextChild.getLocalPart())) {
                 Item firstChild = nextChild.iterateAxis(Axis.CHILD).next();
                if (firstChild != null) {
                     String textContent = firstChild.getStringValue();
                     return "1".equals(textContent) || "true".equalsIgnoreCase(textContent);
                 }
             }
        	 nextChild = (NodeInfo) childNodes.next();
        }
        return true;
    }
    public static String writeXml(Node doc) throws Exception {
        // Prepare the DOM document for writing
        Source source = new DOMSource(doc);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        // Prepare the output file
        Result result = new StreamResult(out);

        // Write the DOM document to the file
        Transformer xformer = TransformerFactoryFactory.getTransformerFactory().newTransformer();
        xformer.transform(source, result);
        return out.toString("utf-8");
    }

    public static String writeXml(NodeInfo doc) throws Exception {
		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			// Prepare the output file
			Result result = new StreamResult(out);
			// Write the DOM document to the file
			Transformer xformer = TransformerFactoryFactory.getTransformerFactory().newTransformer();

			xformer.transform(doc, result);
			return out.toString("utf-8").replaceFirst("<\\?xml.+?>", "");
		} catch (Exception e) {
    		return doc.getStringValue();
    	}
    }


    static Pattern LINK_PATTERN = Pattern.compile("(mailto:|https://|http://|ftp://|ftps://)[^\\s<>]*\\w");
    static Pattern NODE_PATTERN = Pattern.compile("<.+?>");
    /**
     * For all text split the lines to a specified size and add hyperlinks when appropriate
     */
    public static Object toHyperlinks(NodeInfo text) throws Exception {

        String textString = writeXml(text.getRoot());

        String linked = toHyperlinksSplitNodes(textString, text.getConfiguration());

        if (linked.equals(text)) {
            return text;
        }
        Object nodes = parse(text.getConfiguration(), linked, true);

        if (nodes == null) {
            return text;
        } else {
            return nodes;
        }
    }    
    /**
     * Sometimes nodes can have urls in their attributes (namespace declarations)
     * So nodes themselves should not be processed.  Also if a node is a
     * anchor node then the text within should not be processed.
     * @param configuration
     */
    public static String toHyperlinksSplitNodes(String textString, Configuration configuration) throws Exception {
        Matcher nodes = NODE_PATTERN.matcher(textString);
        if (nodes.find()) {
            StringBuilder builder = new StringBuilder();
            boolean inAnchor = false;
            int i = 0;
            do {
                String beforeNode = textString.substring(i, nodes.start());
                if (inAnchor) {
                    // node is an anchor so just break lines
                    builder.append(insertBR(beforeNode));
                } else {
                    builder.append(toHyperlinksFromText(configuration,beforeNode));
                }

                if (!nodes.group().startsWith("<?xml")) {
                    builder.append(nodes.group());
                }

                if (nodes.group().startsWith("<a ")) {
                    // entering an anchor

                    inAnchor = true;
                }
                if (inAnchor && nodes.group().matches("</\\s*a\\s+.*")) {
                    // exiting anchor
                    inAnchor = false;
                }

                i = nodes.end();
            } while (nodes.find());

            builder.append(toHyperlinksFromText(configuration,textString.substring(i)));

            return builder.toString();
        } else {
            return toHyperlinksFromText(configuration,textString);
        }
    }

    /**
     * Add hyperlinks and split long lines
     * @param configuration
     */
    private static String toHyperlinksFromText(Configuration configuration, String textString) throws Exception {
        StringBuilder builder = new StringBuilder();

        int i = 0;
        Matcher matcher = LINK_PATTERN.matcher(textString);


        if (!matcher.find()) return textString;

        do {
            builder.append(insertBR(textString.substring(i, matcher.start())));

            String linkText = insertBR(matcher.group());
            final int maxLength = 80;
			if (linkText.length() > maxLength) {
				StringBuilder newText = new StringBuilder();
				newText.append(linkText.substring(0, maxLength));

            	String remaining = linkText.substring(80);
            	while (remaining.length() > maxLength) {
            		newText.append("<br/>");
            		newText.append(remaining.substring(0, maxLength));
            		remaining = remaining.substring(80);
            	}
            	newText.append("<br/>");
        		newText.append(remaining);
            	linkText = newText.toString();
            }
            String tag = "<a href=\"" + matcher.group() + "\" target=\"_newtab\">" + linkText + "</a>";

            // do a test to make sure the new text makes a valid document
            if (parse(configuration, builder.toString() + tag + textString.substring(matcher.end()), false) != null) {
                builder.append(tag);
            } else {
                builder.append(insertBR(textString.substring(matcher.start(), matcher.end())));
            }
            i = matcher.end();
        } while (matcher.find());

        builder.append(insertBR(textString.substring(i, textString.length())));

        return builder.toString();
    }

    /**
     * Insert line breaks
     */
    private static String insertBR(String word) {

        Matcher nodes = NODE_PATTERN.matcher(word);

        if (nodes.find()) {
            StringBuilder b = new StringBuilder();
            int i = 0;
            do {
                b.append(insertBR(word.substring(i, nodes.start())));
                b.append(nodes.group());
                i = nodes.end();
            } while (nodes.find());
            return b.toString();
        }

        return word;
    }

    public static UnfailingIterator parse(Configuration configuration, String string, boolean printError)
            throws Exception {
        String resultString = "<div>" + string + "</div>";

        try {
            Source xmlSource = new StreamSource(new ByteArrayInputStream(resultString.getBytes("UTF-8")));
            DocumentInfo doc = configuration.buildDocument(xmlSource);
            return SingletonIterator.makeIterator(doc);
        } catch (Exception e) {
            org.jdom.Element error = JeevesException.toElement(e);
            Log.warning(Log.SERVICE, e.getMessage() + XML.toString(error));
            return null;
        }

    }
    public static String loadTranslationFile(Object filePattern, String language) throws IOException {
        if (filePattern != null) {
            final ServiceContext serviceContext = ServiceContext.get();
            if (serviceContext != null) {
                final Charset charset = Charset.forName("UTF-8");

                String desiredPath = String.format(filePattern.toString(), twoCharLangCode(language.toString()));
                URL resource = serviceContext.getServlet().getServletContext().getResource(desiredPath);
                if (resource != null) {
                    return Resources.toString(resource, charset);
                }
                String defaultPath = String.format(filePattern.toString(), "en");
                resource = serviceContext.getServlet().getServletContext().getResource(defaultPath);
                if (resource != null) {
                    return Resources.toString(resource, charset);
                }
            }
        }

        return "{}";
    }
    /**
     * Return 2 iso lang code from a 3 iso lang code. If any error occurs return "".
     *
     * @param iso3LangCode   The 2 iso lang code
     * @return The related 3 iso lang code
     */
    public static String twoCharLangCode(String iso3LangCode) {
    	if(iso3LangCode==null || iso3LangCode.length() == 0) {
    		return Geonet.DEFAULT_LANGUAGE;
    	}
    	
    	if(iso3LangCode.equalsIgnoreCase("FRA")) {
    		return "FR";
    	}
    	
    	if(iso3LangCode.equalsIgnoreCase("DEU")) {
    		return "DE";
    	}
        String iso2LangCode = "";

        try {
            if (iso3LangCode.length() == 2){
                iso2LangCode = iso3LangCode;
            } else {
                iso2LangCode = IsoLanguagesMapper.getInstance().iso639_2_to_iso639_1(iso3LangCode);
            }
        } catch (Exception ex) {
            Log.error(Geonet.GEONETWORK, "Failed to get iso 2 language code for " + iso3LangCode + " caused by " + ex.getMessage());
            
        }

        if(iso2LangCode == null) {
        	return iso3LangCode.substring(0,2);
        } else {
        	return iso2LangCode;
        }
    }
    /**
     * Return '' or error message if error occurs during URL connection.
     * 
     * @param url   The URL to ckeck
     * @return
     */
    public static String getUrlStatus(String url){
        URL u;
        URLConnection conn;
        int connectionTimeout = 500;
        try {
            u = new URL(url);
            conn = u.openConnection();
            conn.setConnectTimeout(connectionTimeout);

            // TODO : set proxy

            if (conn instanceof HttpURLConnection) {
               HttpURLConnection httpConnection = (HttpURLConnection) conn;
               httpConnection.setInstanceFollowRedirects(true);
               httpConnection.connect();
               httpConnection.disconnect();
               // FIXME : some URL return HTTP200 with an empty reply from server
               // which trigger SocketException unexpected end of file from server
               int code = httpConnection.getResponseCode();

               if (code == HttpURLConnection.HTTP_OK) {
                   return "";
               } else {
                   return "Status: " + code;
               }
            } // TODO : Other type of URLConnection
        } catch (Exception e) {
            e.printStackTrace();
            return e.toString();
        }

        return "";
    }
    
	public static String threeCharLangCode(String langCode) {
	    if(langCode == null || langCode.length() < 2) return Geonet.DEFAULT_LANGUAGE;

		if(langCode.length() == 3) return langCode;

		return IsoLanguagesMapper.getInstance().iso639_1_to_iso639_2(langCode, langCode);
	}

	public static boolean match(Object src, Object pattern) {
		if (src == null || src.toString().trim().isEmpty()) {
			return false;
		}
		return src.toString().matches(pattern.toString());
	}

    private static ThreadLocal<Boolean> allowScripting = new InheritableThreadLocal<Boolean>();
    public static void setNoScript() {
        allowScripting.set(false);
    }
    public static boolean allowScripting() {
        return allowScripting.get() == null || allowScripting.get();
    }

}