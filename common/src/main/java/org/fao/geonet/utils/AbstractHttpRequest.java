package org.fao.geonet.utils;

import com.google.common.base.Function;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.apache.http.message.BasicNameValuePair;
import org.fao.geonet.exceptions.BadSoapResponseEx;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.ClientHttpResponse;

import javax.annotation.Nullable;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: Jesse
 * Date: 10/21/13
 * Time: 12:03 PM
 * To change this template use File | Settings | File Templates.
 */
public class AbstractHttpRequest {
    protected final GeonetHttpRequestFactory requestFactory;
    protected String host;
    protected int port;
    protected String protocol;
    protected boolean useSOAP;
    protected String sentData;
    private String address;
    private String query;
    private Method method;
    private Element postParams;
    private boolean useProxy;
    private String proxyHost;
    private int proxyPort;
    private ArrayList<NameValuePair> alSimpleParams = new ArrayList<NameValuePair>();
    private String postData;
    private UsernamePasswordCredentials credentials;
    private UsernamePasswordCredentials proxyCredentials;

    public AbstractHttpRequest(String protocol, String host, int port, GeonetHttpRequestFactory requestFactory) {
        this.port = port;
        this.protocol = protocol;
        this.requestFactory = requestFactory;
        this.host = host;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getAddress() {
        return address;
    }

    public Method getMethod() {
        return method;
    }

    public String getSentData() {
        return sentData;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public void setUrl(URL url) {
        host = url.getHost();
        port = (url.getPort() == -1) ? url.getDefaultPort() : url.getPort();
        protocol = url.getProtocol();
        address = url.getPath();
        query = url.getQuery();
    }

    public void setMethod(Method m) {
        method = m;
    }

    public void setUseSOAP(boolean yesno) {
        useSOAP = yesno;
    }

    public void setUseProxy(boolean yesno) {
        useProxy = yesno;
    }

    public void setProxyHost(String host) {
        proxyHost = host;
    }

    public void setProxyPort(int port) {
        proxyPort = port;
    }

    public void setProxyCredentials(String username, String password) {
        if (username == null || username.trim().length() == 0)
            return;

        this.proxyCredentials = new UsernamePasswordCredentials(username, password);
    }

    public void clearParams() {
        alSimpleParams.clear();
        postParams = null;
    }

    public void addParam(String name, Object value) {
        if (value != null) {
            alSimpleParams.add(new BasicNameValuePair(name, value.toString()));
        }

        method = Method.GET;
    }

    public void setRequest(Element request) {
        postParams = (Element) request.detach();
        method = Method.POST;
    }

    /**
     * Sends the content of a file using a POST request and gets the response in
     * xml format.
     */
//	public final Element send(String name, File inFile) throws IOException, BadXmlResponseEx, BadSoapResponseEx
//	{
//        FileEntity fileEntity = new FileEntity(inFile);
//
//		Part[] parts = new Part[alSimpleParams.size()+1];
//
//		int partsIndex = 0;
//
//		parts[partsIndex] = new FilePart(name, inFile);
//
//		for (NameValuePair nv : alSimpleParams)
//			parts[++partsIndex] = new StringPart(nv.getName(), nv.getValue());
//
//		PostMethod post = new PostMethod();
//		post.setRequestEntity(new MultipartRequestEntity(parts, post.getParams()));
//		post.addRequestHeader("Accept", !useSOAP ? "application/xml" : "application/soap+xml");
//		post.setPath(address);
//		post.setDoAuthentication(useAuthent());
//
//		//--- execute request
//
//		Element response = doExecute(post);
//
//		if (useSOAP)
//			response = soapUnembed(response);
//
//		return response;
//	}

    //---------------------------------------------------------------------------
    public void setCredentials(String username, String password) {

        this.credentials = new UsernamePasswordCredentials(username, password);
    }

    protected ClientHttpResponse doExecute(final HttpRequestBase httpMethod) throws IOException {
        return requestFactory.execute(httpMethod, new Function<HttpClientBuilder, Void>() {
            @Nullable
            @Override
            public Void apply(@Nullable HttpClientBuilder input) {
                final BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();
                if (credentials != null) {
                    final URI uri = httpMethod.getURI();
                    credentialsProvider.setCredentials(new AuthScope(new HttpHost(uri.getHost())), credentials);
                }

                if (useProxy) {
                    final HttpHost proxy = new HttpHost(proxyHost, proxyPort);
                    input.setProxy(proxy);
                    if (proxyCredentials != null) {
                        credentialsProvider.setCredentials(new AuthScope(proxy), proxyCredentials);
                    }
                }
                input.setDefaultCredentialsProvider(credentialsProvider);

                input.setRedirectStrategy(new LaxRedirectStrategy());
                return null;
            }
        });
    }

    protected HttpRequestBase setupHttpMethod() throws IOException {
        HttpRequestBase httpMethod;

        if (method == Method.GET) {
            HttpGet get = new HttpGet();

            get.addHeader("Accept", !useSOAP ? "application/xml" : "application/soap+xml");
            httpMethod = get;
        } else {
            HttpPost post = new HttpPost();

            if (!useSOAP) {
                postData = (postParams == null) ? "" : Xml.getString(new Document(postParams));
                HttpEntity entity = new StringEntity(postData, ContentType.create("application/xml", "UTF-8"));
                post.setEntity(entity);
            } else {
                postData = Xml.getString(new Document(soapEmbed(postParams)));
                HttpEntity entity = new StringEntity(postData, ContentType.create("application/xml", "UTF-8"));
                post.setEntity(entity);
            }

            httpMethod = post;
        }

        String queryString = query;

        if (query == null || query.trim().isEmpty()) {
            StringBuilder b = new StringBuilder();

            for (NameValuePair alSimpleParam : alSimpleParams) {
                if (b.length() > 0) {
                    b.append("&");
                }
                b.append(alSimpleParam.getName()).append('=').append(alSimpleParam.getValue());
            }
            if (b.length() > 0) {
                queryString = b.toString();
            }
        }

        try {
            URI uri = new URI(protocol, null, host, port, address, queryString, null);
            httpMethod.setURI(uri);
        } catch (URISyntaxException e) {
            throw new IOException(e);
        }

        final RequestConfig.Builder builder = RequestConfig.custom();
        builder.setAuthenticationEnabled(credentials != null);
        builder.setRedirectsEnabled(true);
        builder.setRelativeRedirectsAllowed(true);
        builder.setCircularRedirectsAllowed(true);
        builder.setMaxRedirects(3);
        builder.setCookieSpec(CookieSpecs.BROWSER_COMPATIBILITY);

        httpMethod.setConfig(builder.build());
        return httpMethod;
    }

    protected String getSentData(HttpRequestBase httpMethod) {
        URI uri = httpMethod.getURI();
        StringBuilder sentData = new StringBuilder(httpMethod.getMethod()).append(" ").append(uri.getPath());

        if (uri.getQuery() != null) {
            sentData.append("?" + uri.getQuery());
        }

        sentData.append("\r\n");

        for (Header h : httpMethod.getAllHeaders()) {
            sentData.append(h);
        }

        sentData.append("\r\n");

        if (httpMethod instanceof HttpPost) {
            sentData.append(postData);
        }

        return sentData.toString();
    }

    private String getReceivedData(ClientHttpResponse response, byte[] data) {
        StringBuilder receivedData = new StringBuilder();

        try {
            //--- if there is a connection error (the server is unreachable) this
            //--- call causes a NullPointerEx

            receivedData.append(response.getStatusText()).append("\r\r");

            final HttpHeaders headers = response.getHeaders();
            for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
                receivedData.append(entry.getKey());
                receivedData.append('=');
                receivedData.append(entry.getValue());
                receivedData.append('&');
            }

            receivedData.append("\r\n");

            if (response != null) {
                receivedData.append(new String(data, "UTF8"));
            }
        } catch (Exception e) {
            receivedData.setLength(0);
        }

        return receivedData.toString();
    }

    private Element soapEmbed(Element elem) {
        Element envl = new Element("Envelope", SOAPUtil.NAMESPACE_ENV);
        Element body = new Element("Body", SOAPUtil.NAMESPACE_ENV);

        envl.addContent(body);
        body.addContent(elem);

        return envl;
    }

    @SuppressWarnings("unchecked")
    protected Element soapUnembed(Element envelope) throws BadSoapResponseEx {
        Namespace ns = envelope.getNamespace();
        Element body = envelope.getChild("Body", ns);

        if (body == null)
            throw new BadSoapResponseEx(envelope);

        List<Element> list = body.getChildren();

        if (list.size() == 0)
            throw new BadSoapResponseEx(envelope);

        return list.get(0);
    }

    public String getProtocol() {
        return protocol;
    }

    public enum Method {GET, POST}
}
