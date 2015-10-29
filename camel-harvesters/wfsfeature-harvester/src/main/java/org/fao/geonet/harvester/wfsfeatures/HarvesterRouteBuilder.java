package org.fao.geonet.harvester.wfsfeatures;

import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.builder.xml.Namespaces;
import org.w3c.dom.Document;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by francois on 28/10/15.
 */
public class HarvesterRouteBuilder extends RouteBuilder {
    private static final String LOGGER_NAME = "harvester.wfsfeature";

    @Override
    public void configure() throws Exception {
        Namespaces WFS_1_0_0 = new Namespaces("wfs", "http://www.opengis.net/wfs");
        Namespaces GML = new Namespaces("gml", "http://www.opengis.net/gml");
        final Map<String, String> listOfNamespaces = new HashMap<>();
        listOfNamespaces.put("gml", "http://www.opengis.net/gml");
        listOfNamespaces.put("gml32", "http://www.opengis.net/gml/3.2");
        listOfNamespaces.put("wfs", "http://www.opengis.net/wfs");
        listOfNamespaces.put("wfs2", "http://www.opengis.net/wfs/2.0");

        final String url = "http4://geoservices.brgm.fr/risques?SERVICE=WFS&VERSION=1.0.0&REQUEST=GetFeature&TYPENAME=BASIAS_LOCALISE&maxFeatures=10000";
//        final String url = "http4://visi-sextant.ifremer.fr/cgi-bin/sextant/wfs/bgmb?SERVICE=WFS&VERSION=1.0.0&REQUEST=GetFeature&TYPENAME=SISMER_mesures";
//        final String url = "http4://www.geopicardie.fr/geoserver/ows?service=wfs&version=2.0.0&request=GetFeature&TYPENAME=autres:osm_communes_2015";

        from("timer://start?repeatCount=1")
                .log(LoggingLevel.DEBUG, LOGGER_NAME,
                        String.format("Harvesting features from WFS service URL '%s'", url))
                .to(url)
                .convertBodyTo(Document.class)
                .split()
                    .xpath("//gml:featureMember/*|//wfs2:member/*", listOfNamespaces)
                    .parallelProcessing()
                    .bean(FeatureIndexer.class, "featureToIndexDocument");
    }
}
