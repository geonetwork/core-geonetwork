package org.geonetwork.map.wfs;
import org.geotools.data.ows.Specification;
import org.geotools.data.wms.WMS1_1_1;
import org.geotools.data.wms.WebMapServer;
import org.geotools.data.wms.request.GetStylesRequest;
import org.geotools.data.wms.response.GetStylesResponse;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.ows.ServiceException;
import org.geotools.styling.*;
import org.geotools.styling.builder.NamedLayerBuilder;
import org.geotools.styling.builder.StyleBuilder;
import org.geotools.styling.builder.StyledLayerDescriptorBuilder;
import org.json.JSONObject;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory2;

import javax.mail.internet.ContentType;
import javax.mail.internet.ParseException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
/**
 * Created by Vampouille on 11/2/15.
 */
public class SLDUtil {

    /**
     * Issue a GetStyle query on specified server for specified layers, parse XML result and construct an array of Style
     *
     * @param url base url of WMS server with no parameter. ex: http://demo.boundlessgeo.com/geoserver/wms
     * @param layers comma separated list of layers
     * @return array an array of Style corresponding to parsed SLD
     * @throws IOException if there is an error communicating with the server
     * @throws ServiceException if the server responds with an error
     * @throws ParseException if unable to parse content type header from server
     */
    public static Style[] parseSLD(URL url, String layers) throws IOException, ServiceException, ParseException {
        GetStylesResponse wmsResponse = null;
        GetStylesRequest wmsRequest = null;
        StyleFactory styleFactory = new StyleFactoryImpl();

        WebMapServer server = new WebMapServer(url) {
            // GetStyle is only implemented in WMS 1.1.1
            protected void setupSpecifications() {
                specs = new Specification[1];
                specs[0] = new WMS1_1_1();
            }
        };

        wmsRequest = server.createGetStylesRequest();
        wmsRequest.setLayers(layers);
        wmsResponse = server.issueRequest(wmsRequest);

        // Set encoding of response from HTTP content-type header
        ContentType contentType = new ContentType(wmsResponse.getContentType());
        InputStreamReader stream;
        if(contentType.getParameter("charset") != null)
            stream = new InputStreamReader(wmsResponse.getInputStream(), contentType.getParameter("charset"));
        else
            stream = new InputStreamReader(wmsResponse.getInputStream());

        return (new SLDParser(styleFactory, stream)).readXML();

    }


    /**
     * Contruct a SLD document from list of styles and name
     *
     * @param styles list iof styles to include in SLd
     * @param namedLayerName name of namedLayer entity
     * @return an instance of StyledLayerDescriptor that can be used with {@link org.geotools.styling.SLDTransformer#transform(Object)}
     */

    public static StyledLayerDescriptor buildSLD(Style[] styles, String namedLayerName){

        StyledLayerDescriptorBuilder SLDBuilder = new StyledLayerDescriptorBuilder();

        NamedLayerBuilder namedLayerBuilder = SLDBuilder.namedLayer();
        namedLayerBuilder.name(namedLayerName);
        StyleBuilder styleBuilder = namedLayerBuilder.style();

        for(int i =0; i<styles.length; i++){
            styleBuilder.reset(styles[i]);
            styles[i] = styleBuilder.build();
        }

        NamedLayer namedLayer = namedLayerBuilder.build();

        for(Style style: styles)
            namedLayer.addStyle(style);

        SLDTransformer styleTransform = new SLDTransformer();

        StyledLayerDescriptor sld = (new StyledLayerDescriptorBuilder()).build();
        sld.addStyledLayer(namedLayer);
        return sld;

    }



    /**
     * Merge new filter to an existing list of Style. This method add a new filter into SLD document, if there is
     * already some filter define on a Rule then a 'And' filter will be created with new filter and the original one.
     *
     * @param styles original styles
     * @param newFilter new filter to add in original styles
     * @return originals filters merged with new filter with 'And' operator
     */

    public static Style[] addAndFilter(Style[] styles, Filter newFilter) {

        StyleFactory sf = CommonFactoryFinder.getStyleFactory();
        FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2();

        List<Style> res = new LinkedList<Style>();

        for (Style style : styles) {

            Style newStyle = sf.createStyle();

            // Copy name, title and abstract
            newStyle.setName(style.getName());
            newStyle.getDescription().setTitle(style.getDescription().getTitle());
            newStyle.getDescription().setAbstract(style.getDescription().getAbstract());

            for (FeatureTypeStyle feature : style.featureTypeStyles()) {
                FeatureTypeStyle newFeatureTypeStyle = sf.createFeatureTypeStyle();

                // Copy name, title and abstract
                newFeatureTypeStyle.setName(feature.getName());
                newFeatureTypeStyle.getDescription().setTitle(feature.getDescription().getTitle());
                newFeatureTypeStyle.getDescription().setAbstract(feature.getDescription().getAbstract());

                for (Rule rule : feature.rules()) {

                    Rule newRule = sf.createRule();

                    newRule.setName(rule.getName());
                    newRule.getDescription().setTitle(rule.getDescription().getTitle());
                    newRule.getDescription().setAbstract(rule.getDescription().getAbstract());
                    newRule.setMinScaleDenominator(rule.getMinScaleDenominator());
                    newRule.setMaxScaleDenominator(rule.getMaxScaleDenominator());

                    if(rule.getFilter() != null)
                        // merge new filter and original one with 'And' operator
                        newRule.setFilter(ff.and(rule.getFilter(), newFilter));
                    else
                        // Just copy new filter if original rule have no filter
                        newRule.setFilter(newFilter);
                    for (Symbolizer sym : rule.getSymbolizers())
                        newRule.symbolizers().add(sym);

                    newFeatureTypeStyle.rules().add(newRule);
                }

                newStyle.featureTypeStyles().add(newFeatureTypeStyle);
            }
            res.add(newStyle);
        }
        return res.toArray(new Style[0]);
    }
    

}
