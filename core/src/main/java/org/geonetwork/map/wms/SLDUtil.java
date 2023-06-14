package org.geonetwork.map.wms;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.DefaultHttpClient;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.utils.Xml;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.filter.v1_1.OGCConfiguration;
import org.geotools.ows.ServiceException;
import org.geotools.xsd.Configuration;
import org.geotools.xsd.Encoder;
import org.jdom.Content;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory2;

import javax.mail.internet.ContentType;
import javax.mail.internet.ParseException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

public class SLDUtil {

    public static final String LOGGER = Geonet.GEONETWORK + ".util.sld";

    /**
     * Issue a GetStyle query on specified server for specified layers, parse XML result and return
     * the SLD as a String.
     *
     * @param url base url of WMS server with no parameter. ex: http://demo.boundlessgeo.com/geoserver/wms
     * @param layers comma separated list of layers
     * @return String the sld of the layers from the server url
     * @throws IOException if there is an error communicating with the server
     * @throws ServiceException if the server responds with an error
     * @throws ParseException if unable to parse content type header from server
     */
    public static Map<String, String> parseSLD(URI url, String layers) throws URISyntaxException, IOException, ParseException {
        Map<String, String> hash = new HashMap<String, String>();

        String requestUrl = SLDUtil.getGetStyleRequest(url, layers);

        HttpGet httpGet = new HttpGet(requestUrl);
        HttpClient client = new DefaultHttpClient();
        final HttpResponse httpResponse = client.execute(httpGet);

        // Set encoding of response from HTTP content-type header
        ContentType contentType = new ContentType(httpResponse.getEntity().getContentType().getValue());
        String charset = contentType.getParameter("charset");
        hash.put("charset", charset);
        hash.put("content", IOUtils.toString(httpResponse.getEntity().getContent(), charset).trim());

        return hash;
    }


    public static String getGetStyleRequest(URI uri, String layers) throws URISyntaxException {
        URIBuilder builder = new URIBuilder(uri);
        builder.addParameter("service", "WMS");
        builder.addParameter("request", "GetStyles");
        builder.addParameter("version", "1.1.1");
        builder.addParameter("layers", layers);

        return builder.build().toString();
    }

    /**
     * Insert a new filter in all rules of the sld document. For each rule,
     * the ogc:Filter ellement is added if not exist, or merge with an ogc:And
     * tag with the giver filter.
     *
     * @param doc Xml docuement representing the SLD
     * @param filter the new filter to insert
     * @throws JDOMException
     * @throws IOException
     */
    public static void insertFilter(Element doc, Filter filter) throws JDOMException, IOException {

        String sFilter = SLDUtil.encodeFilter(filter);
        Element newFilterElt = Xml.loadString(sFilter, false);
        List<Element> newFilterChildren = (List<Element>) ((Element)newFilterElt.clone()).getChildren();

        if(newFilterChildren.size() > 0) {
            Content newFilterContent = newFilterChildren.get(0).detach();

            // Check rules in both se and sld namespaces
            List<Element> rules = (List<Element>) Xml.selectNodes(doc, "*//sld:Rule", Arrays.asList(Geonet.Namespaces.SLD));
            if(rules.size() == 0) {
                rules = (List<Element>) Xml.selectNodes(doc, "*//se:Rule", Arrays.asList(Geonet.Namespaces.SE));
            }

            for (Element rule : rules) {
                List<Element> filters = (List<Element>) Xml.selectNodes(rule, "ogc:Filter", Arrays.asList(Geonet.Namespaces.OGC));
                if(filters.size() == 0) {
                    rule.addContent((Element)newFilterElt.clone());
                }
                else if (filters.size() == 1) {
                    Element sldFilterElt = filters.get(0);
                    Element filterContent = (Element)sldFilterElt.getChildren().get(0);
                    filterContent.detach();
                    sldFilterElt.removeContent();
                    Element andElt = new Element("And", Geonet.Namespaces.OGC);
                    andElt.addContent(filterContent);
                    andElt.addContent((Content)newFilterContent.clone());
                    sldFilterElt.addContent(andElt);
                }
                else {
                    throw new JDOMException("A rule must have maximum one ogc:filter element");
                }
            }
        }
    }

    /**
     * Encode into a string the given OGC Filter
     *
     * @param filter the OGC filter object
     * @return String the filter object to String
     * @throws IOException
     */
    public static String encodeFilter(Filter filter) throws IOException {
        OutputStream outputStream = new ByteArrayOutputStream();

        Configuration configuration = new OGCConfiguration();
        Encoder encoder = new Encoder(configuration);
        encoder.encode( filter, org.geotools.filter.v1_1.OGC.Filter, outputStream);

        return outputStream.toString();
    }

    /**
     * Generate a SLD Filter from filters defined in a JSON
     *
     * JSON example :
     * <pre>
     * {
     *     "baseStyle" : "Test:MuiltiRoad",
     *     "filters": [ {"field_name": "longueur",
     *                   "filter": [ { "filter_type": "PropertyIsBetween",
     *                                 "params": [0,500]},
     *                               { "filter_type": "PropertyIsBetween",
     *                                 "params": [500,5000]}]},
     *                  {"field_name": "departement",
     *                   "filter": [ { "filter_type": "PropertyIsEqualTo",
     *                                 "params": ["Ain"]}]},
     *                  {"field_name": "date_renovation",
     *                   "filter": [ { "filter_type": "PropertyIsBetween",
     *                                 "params": ["2015-07-01", "2015-08-31"]},
     *                               { "filter_type": "PropertyIsBetween",
     *                                 "params": ["2014-07-01", "2014-08-31"]}]}
     *                ]
     * }</pre>
     *
     * @param userFilters JSON representation of filters
     * @return Filter instance that represent combination of filters specified in JSON
     * @throws JSONException if some have wrong parameter count or malformed JSON
     */

    public static Filter generateCustomFilter(JSONObject userFilters) throws JSONException {
        FilterFactory2 ff2 = CommonFactoryFinder.getFilterFactory2();

        JSONArray filters = userFilters.getJSONArray("filters");
        List<Filter> res = new LinkedList<Filter>();

        for(int i=0;i<filters.length();i++)
            res.add(SLDUtil.generateFilter(filters.getJSONObject(i)));

        if(res.size() > 1)
            return ff2.and(res);
        else
            return res.get(0);

    }

    private static Filter generateFilter(JSONObject jsonObject) throws JSONException {

        FilterFactory2 ff2 = CommonFactoryFinder.getFilterFactory2();

        String fieldName = jsonObject.getString("field_name");

        List<Filter> res = new LinkedList<Filter>();

        JSONArray filters = jsonObject.getJSONArray("filter");
        for(int i=0;i<filters.length();i++)
            res.add(SLDUtil.generateFilter2(fieldName, filters.getJSONObject(i)));

        if(res.size() > 1)
            return ff2.or(res);
        else
            return  res.get(0);
    }

    private static Filter generateFilter2(String fieldName, JSONObject jsonObject) throws JSONException {

        FilterFactory2 ff2 = CommonFactoryFinder.getFilterFactory2();

        String filterType = jsonObject.getString("filter_type");

        List parameters = new LinkedList();

        JSONArray params = jsonObject.getJSONArray("params");
        for(int i=0;i<params.length();i++)
            parameters.add(params.get(i));

        if(filterType.equals("PropertyIsEqualTo")) {
            if(parameters.size() != 1) throw new JSONException("Invalid parameter count");
            return ff2.equals(ff2.property(fieldName), ff2.literal(parameters.get(0)));
        } else if(filterType.equals("PropertyIsNotEqualTo")) {
            if (parameters.size() != 1) throw new JSONException("Invalid parameter count");
            return ff2.notEqual(ff2.property(fieldName), ff2.literal(parameters.get(0)));
        } else if(filterType.equals("PropertyIsLessThan")) {
            if (parameters.size() != 1) throw new JSONException("Invalid parameter count");
            return ff2.less(ff2.property(fieldName), ff2.literal(parameters.get(0)));
        } else if(filterType.equals("PropertyIsLessThanOrEqualTo")) {
            if (parameters.size() != 1) throw new JSONException("Invalid parameter count");
            return ff2.lessOrEqual(ff2.property(fieldName), ff2.literal(parameters.get(0)));
        } else if(filterType.equals("PropertyIsGreaterThan")) {
            if (parameters.size() != 1) throw new JSONException("Invalid parameter count");
            return ff2.greater(ff2.property(fieldName), ff2.literal(parameters.get(0)));
        } else if(filterType.equals("PropertyIsGreaterThanOrEqualTo")) {
            if (parameters.size() != 1) throw new JSONException("Invalid parameter count");
            return ff2.greaterOrEqual(ff2.property(fieldName), ff2.literal(parameters.get(0)));
        } else if(filterType.equals("PropertyIsLike")) {
            if (parameters.size() != 1) throw new JSONException("Invalid parameter count");
            return ff2.like(ff2.property(fieldName), (String) parameters.get(0));
        } else if(filterType.equals("PropertyIsNull")) {
            if (parameters.size() != 0) throw new JSONException("Invalid parameter count");
            return ff2.isNull(ff2.property(fieldName));
        } else if(filterType.equals("PropertyIsBetween")) {
            if (parameters.size() != 2) throw new JSONException("Invalid parameter count");
            return ff2.between(ff2.property(fieldName), ff2.literal(parameters.get(0)), ff2.literal(parameters.get(1)));
        } else if(filterType.equals("PropertyIsBetweenExclusive")) {
            if (parameters.size() != 2) throw new JSONException("Invalid parameter count");
            return ff2.and(
                ff2.greater(ff2.property(fieldName), ff2.literal(parameters.get(0))),
                ff2.less(ff2.property(fieldName), ff2.literal(parameters.get(1)))
            );
        } else {
            // Currently, no implementation of topological or distance operators
            throw new JSONException("No implementation for filter type : " + filterType);
        }

    }

}
