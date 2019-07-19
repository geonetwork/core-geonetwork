package org.fao.geonet.harvester.wfsfeatures.worker;

import org.geotools.data.ows.HTTPClient;
import org.geotools.data.ows.HTTPResponse;
import org.geotools.data.wfs.v1_1_0.MapServerStrategy;
import org.geotools.data.wfs.v1_1_0.WFSStrategy;
import org.hamcrest.Matcher;
import org.junit.Test;
import org.mockito.ArgumentMatcher;
import org.mockito.Mockito;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class WFSDataStoreStrategyWithNameSpaceForMapserverTest {

    @Test
    public void defaultIsMapServer() throws Exception {
        HTTPClient mockHttpClient = mock(HTTPClient.class);
        WFSDataStoreStrategyWithNameSpaceForMapServer toTest = new WFSDataStoreStrategyWithNameSpaceForMapServer();
        toTest.init ("http://url", "typeName");

        WFSStrategy strategy = toTest.determineCorrectStrategy(mockHttpClient);

        assertTrue(strategy instanceof MapServerStrategy);
    }

    @Test
    public void qgisWhenResponseContainQGisNameSpace() throws Exception {
        ByteArrayInputStream responsePayload = new ByteArrayInputStream("targetNamespace=\"http://www.qgis.org/gml\"".getBytes());
        HTTPClient mockHttpClient = mock(HTTPClient.class);
        HTTPResponse mockHttpResponse = mock(HTTPResponse.class);
        when(mockHttpResponse.getResponseStream()).thenReturn(responsePayload);
        when(mockHttpClient.get(argThat(new ArgumentMatcher<URL>() {

            @Override
            public boolean matches(Object url) {
                if ((url).toString().equalsIgnoreCase("http://visi-sxt-docker.ifremer.fr:9080/ows/p/wfs/surval?REQUEST=DescribeFeatureType&SERVICE=WFS&VERSION=1.1.0&TYPENAME=surval_30140_all_point_QGIS3")) {
                    return true;
                }
                return false;
            }
        }))).thenReturn(mockHttpResponse);
        WFSDataStoreStrategyWithNameSpaceForMapServer toTest = new WFSDataStoreStrategyWithNameSpaceForMapServer();
        toTest.init ("http://visi-sxt-docker.ifremer.fr:9080/ows/p/wfs/surval", "surval_30140_all_point_QGIS3");

        WFSStrategy strategy = toTest.determineCorrectStrategy(mockHttpClient);

        assertTrue(strategy instanceof QgisStrategy);
    }

    @Test
    public void questionMarkInUrl() throws Exception {
        ByteArrayInputStream responsePayload = new ByteArrayInputStream("targetNamespace=\"http://www.qgis.org/gml\"".getBytes());
        HTTPClient mockHttpClient = mock(HTTPClient.class);
        HTTPResponse mockHttpResponse = mock(HTTPResponse.class);
        when(mockHttpResponse.getResponseStream()).thenReturn(responsePayload);
        when(mockHttpClient.get(argThat(new ArgumentMatcher<URL>() {

            @Override
            public boolean matches(Object url) {
                if ((url).toString().equalsIgnoreCase("http://visi-sxt-docker.ifremer.fr:9080/ows/p/wfs/surval?dummy=1&REQUEST=DescribeFeatureType&SERVICE=WFS&VERSION=1.1.0&TYPENAME=surval_30140_all_point_QGIS3")) {
                    return true;
                }
                return false;
            }
        }))).thenReturn(mockHttpResponse);
        WFSDataStoreStrategyWithNameSpaceForMapServer toTest = new WFSDataStoreStrategyWithNameSpaceForMapServer();
        toTest.init ("http://visi-sxt-docker.ifremer.fr:9080/ows/p/wfs/surval?dummy=1", "surval_30140_all_point_QGIS3");

        WFSStrategy strategy = toTest.determineCorrectStrategy(mockHttpClient);

        assertTrue(strategy instanceof QgisStrategy);
    }
}