package org.geonetwork.map.wfs;

import org.geotools.data.ows.Specification;
import org.geotools.ows.ServiceException;
import org.geotools.styling.SLDParser;
import org.geotools.styling.Style;
import org.geotools.styling.StyleFactory;
import org.geotools.styling.StyleFactoryImpl;
import org.geotools.data.wms.WMS1_1_1;
import org.geotools.data.wms.WebMapServer;
import org.geotools.data.wms.request.GetStylesRequest;
import org.geotools.data.wms.response.GetStylesResponse;


import javax.mail.internet.ContentType;
import javax.mail.internet.ParseException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

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
}
