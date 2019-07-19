package org.fao.geonet.harvester.wfsfeatures.worker;

import org.geotools.data.DataSourceException;
import org.geotools.data.ows.HTTPClient;
import org.geotools.data.ows.HTTPResponse;
import org.geotools.data.ows.SimpleHttpClient;
import org.geotools.data.wfs.WFSDataStore;
import org.geotools.data.wfs.WFSDataStoreFactory;
import org.geotools.data.wfs.protocol.http.HttpMethod;
import org.geotools.data.wfs.protocol.wfs.Version;
import org.geotools.data.wfs.v1_0_0.WFS100ProtocolHandler;
import org.geotools.data.wfs.v1_0_0.WFS_1_0_0_DataStore;
import org.geotools.data.wfs.v1_1_0.MapServerStrategy;
import org.geotools.data.wfs.v1_1_0.WFSStrategy;
import org.geotools.data.wfs.v1_1_0.WFS_1_1_0_DataStore;
import org.geotools.data.wfs.v1_1_0.WFS_1_1_0_Protocol;
import org.geotools.wfs.WFS;
import org.geotools.wfs.protocol.ConnectionFactory;
import org.geotools.wfs.protocol.DefaultConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Map;

import static org.geotools.data.wfs.protocol.http.HttpMethod.GET;
import static org.geotools.data.wfs.protocol.http.HttpMethod.POST;

public class WFSDataStoreStrategyWithNameSpaceForMapServer extends WFSDataStoreFactory {

    private static Logger LOGGER =  LoggerFactory.getLogger(WFSHarvesterRouteBuilder.LOGGER_NAME);
    private String describeFeatureTypeUrl;

    public void init (String url, String typeName) throws Exception {
        this.describeFeatureTypeUrl = new OwsUtils().getDescribeFeatureTypeUrl(url, typeName, "1.1.0");
    }

    @Override
    public WFSDataStore createDataStore(Map params) throws IOException {
        HTTPClient http = new SimpleHttpClient();
        setHTTPClient(http);
        final java.net.URL getCapabilitiesRequest = (URL) URL.lookUp(params);
        final Boolean protocol = (Boolean) PROTOCOL.lookUp(params);
        final String user = (String) USERNAME.lookUp(params);
        final String pass = (String) PASSWORD.lookUp(params);
        final int timeoutMillis = (Integer) TIMEOUT.lookUp(params);
        final int buffer = (Integer) BUFFER_SIZE.lookUp(params);
        final boolean tryGZIP = (Boolean) TRY_GZIP.lookUp(params);
        final boolean lenient = (Boolean) LENIENT.lookUp(params);
        final String encoding = (String) ENCODING.lookUp(params);
        final Integer maxFeatures = (Integer) MAXFEATURES.lookUp(params);
        final Charset defaultEncoding = Charset.forName(encoding);
        final String wfsStrategy = (String) WFS_STRATEGY.lookUp(params);
        final Integer filterCompliance = (Integer) FILTER_COMPLIANCE.lookUp(params);
        final String namespaceOverride = (String) NAMESPACE.lookUp(params);
        final Boolean useDefaultSRS = (Boolean) USEDEFAULTSRS.lookUp(params);
        final String axisOrder = (String) AXIS_ORDER.lookUp(params);
        final String axisOrderFilter = (String) AXIS_ORDER_FILTER.lookUp(params) == null ? (String) AXIS_ORDER
                .lookUp(params) : (String) AXIS_ORDER_FILTER.lookUp(params);


        final String outputFormat = (String) OUTPUTFORMAT.lookUp(params);

        if (((user == null) && (pass != null)) || ((pass == null) && (user != null))) {
            throw new IOException(
                    "Cannot define only one of USERNAME or PASSWORD, must define both or neither");
        }

        final WFSDataStore dataStore;

        http.setTryGzip(tryGZIP);
        http.setUser(user);
        http.setPassword(pass);
        http.setConnectTimeout(timeoutMillis / 1000);
        http.setReadTimeout(timeoutMillis / 1000);

        final byte[] wfsCapabilitiesRawData = loadCapabilities(getCapabilitiesRequest, http);
        final Document capsDoc = parseCapabilities(wfsCapabilitiesRawData);
        final Element rootElement = capsDoc.getDocumentElement();

        final String capsVersion = rootElement.getAttribute("version");
        final Version version = Version.find(capsVersion);

        if (Version.v1_0_0 == version) {
            final ConnectionFactory connectionFac = new DefaultConnectionFactory(tryGZIP, user,
                    pass, defaultEncoding, timeoutMillis);
            InputStream reader = new ByteArrayInputStream(wfsCapabilitiesRawData);
            final WFS100ProtocolHandler protocolHandler = new WFS100ProtocolHandler(reader,
                    connectionFac);

            try {
                HttpMethod prefferredProtocol = Boolean.TRUE.equals(protocol) ? POST : GET;
                dataStore = new WFS_1_0_0_DataStore(prefferredProtocol, protocolHandler,
                        timeoutMillis, buffer, lenient, wfsStrategy, filterCompliance);
            } catch (SAXException e) {
                    LOGGER.warn(e.toString());
                throw new IOException(e.toString());
            }
        } else {
            InputStream capsIn = new ByteArrayInputStream(wfsCapabilitiesRawData);

            WFSStrategy strategy = determineCorrectStrategy(http);


            WFS_1_1_0_Protocol wfs = new WFS_1_1_0_Protocol(capsIn, http, defaultEncoding, strategy);

            dataStore = new WFS_1_1_0_DataStore(wfs);
            dataStore.setMaxFeatures(maxFeatures);
            dataStore.setPreferPostOverGet(protocol);
            dataStore.setUseDefaultSRS(useDefaultSRS);
            ((WFS_1_1_0_DataStore) dataStore).setAxisOrder(axisOrder,
                    axisOrderFilter);
            ((WFS_1_1_0_DataStore) dataStore).setGetFeatureOutputFormat(outputFormat);
            ((WFS_1_1_0_DataStore) dataStore).setMappedURIs(strategy
                    .getNamespaceURIMappings());
        }
        dataStore.setNamespaceOverride(namespaceOverride);


        return dataStore;
    }

    protected WFSStrategy determineCorrectStrategy(HTTPClient httpClient) {
        WFSStrategy strategy = new MapServerStrategy();
        try {
            HTTPResponse httpResponse = httpClient.get(new URL(describeFeatureTypeUrl));
            InputStream inputStream = httpResponse.getResponseStream();

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buff = new byte[1024];
            int readCount;
            while ((readCount = inputStream.read(buff)) != -1) {
                out.write(buff, 0, readCount);
            }
            String responsePayload = out.toString("UTF-8");
            if (responsePayload.contains("targetNamespace=\"http://www.qgis.org/gml\"")) {
                strategy = new QgisStrategy();
            }
        } catch (Exception e) {

        }
        return strategy;
    }

    byte[] loadCapabilities(final URL capabilitiesUrl, HTTPClient http) throws IOException {
        byte[] wfsCapabilitiesRawData;

        HTTPResponse httpResponse = http.get(capabilitiesUrl);
        InputStream inputStream = httpResponse.getResponseStream();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buff = new byte[1024];
        int readCount;
        while ((readCount = inputStream.read(buff)) != -1) {
            out.write(buff, 0, readCount);
        }
        wfsCapabilitiesRawData = out.toByteArray();
        return wfsCapabilitiesRawData;
    }

    private Document parseCapabilities(final byte[] wfsCapabilitiesRawData)
            throws IOException, DataSourceException {
        Document capsDoc;
        {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(wfsCapabilitiesRawData);
            capsDoc = parseCapabilities(inputStream);
            Element root = capsDoc.getDocumentElement();
            String localName = root.getLocalName();
            String namespace = root.getNamespaceURI();
            if (!WFS.NAMESPACE.equals(namespace)
                    || !WFS.WFS_Capabilities.getLocalPart().equals(localName)) {
                if ("http://www.opengis.net/ows".equals(namespace)
                        && "ExceptionReport".equals(localName)) {
                    StringBuffer message = new StringBuffer();
                    Element exception = (Element) capsDoc.getElementsByTagNameNS("*", "Exception")
                            .item(0);
                    if (exception == null) {
                        throw new DataSourceException(
                                "Exception Report when requesting capabilities");
                    }
                    Node exceptionCode = exception.getAttributes().getNamedItem("exceptionCode");
                    Node locator = exception.getAttributes().getNamedItem("locator");
                    Node exceptionText = exception.getElementsByTagNameNS("*", "ExceptionText")
                            .item(0);

                    message.append("Exception Report ");
                    String text = exceptionText.getTextContent();
                    if (text != null) {
                        message.append(text.trim());
                    }
                    message.append(" Exception Code:");
                    message.append(exceptionCode == null ? "" : exceptionCode.getTextContent());
                    message.append(" Locator: ");
                    message.append(locator == null ? "" : locator.getTextContent());
                    throw new DataSourceException(message.toString());
                }
                throw new DataSourceException("Expected " + WFS.WFS_Capabilities + " but was "
                        + namespace + "#" + localName);
            }
        }
        return capsDoc;
    }

    Document parseCapabilities(InputStream inputStream) throws IOException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        DocumentBuilder documentBuilder;
        try {
            documentBuilder = dbf.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        }
        Document document;
        try {
            document = documentBuilder.parse(inputStream);
        } catch (SAXException e) {
            throw new DataSourceException("Error parsing capabilities document", e);
        }
        return document;
    }

}
