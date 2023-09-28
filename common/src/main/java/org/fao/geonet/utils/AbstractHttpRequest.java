/*
 * Copyright (C) 2001-2023 Food and Agriculture Organization of the
 * United Nations (FAO-UN), United Nations World Food Programme (WFP)
 * and United Nations Environment Programme (UNEP)
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or (at
 * your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
 *
 * Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
 * Rome - Italy. email: geonetwork@osgeo.org
 */

package org.fao.geonet.utils;

import com.google.common.base.Function;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.CookieStore;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HttpContext;
import org.fao.geonet.exceptions.BadSoapResponseEx;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Super class for classes that encapsulate requests.
 *
 * User: Jesse Date: 10/21/13 Time: 12:03 PM
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
    private ArrayList<NameValuePair> alSimpleParams = new ArrayList<>();
    private String postData;
    private boolean preemptiveBasicAuth;
    private HttpClientContext httpClientContext;
    private CookieStore cookieStore;
    private UsernamePasswordCredentials credentials;
    private UsernamePasswordCredentials proxyCredentials;
    private String fragment;
    private String userInfo;

    public AbstractHttpRequest(String protocol, String host, int port, GeonetHttpRequestFactory requestFactory) {
        if (!(protocol.equals("http") || protocol.equals("https"))) {
            throw new IllegalArgumentException("Currently only http and https requests are supported.  Protocol given: '" + protocol + "'");
        }
        this.port = port;
        this.protocol = protocol;
        this.requestFactory = requestFactory;
        this.host = host;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        if (!address.startsWith("/")) {
            throw new IllegalArgumentException("address must start with /");
        }
        this.address = address;
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method m) {
        method = m;
    }

    public String getSentData() {
        return sentData;
    }

    public void setUrl(URL url) {
        host = url.getHost();
        port = url.getPort();
        protocol = url.getProtocol();
        address = url.getPath();
        query = url.getQuery();
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
     * Sends the content of a file using a POST request and gets the response in xml format.
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
    public boolean isPreemptiveBasicAuth() {
        return preemptiveBasicAuth;
    }

    public void setPreemptiveBasicAuth(boolean preemptiveBasicAuth) {
        this.preemptiveBasicAuth = preemptiveBasicAuth;
    }

    public HttpClientContext getHttpClientContext() {
        return httpClientContext;
    }

    public void setCredentials(String username, String password) {

        this.credentials = new UsernamePasswordCredentials(username, password);
    }

    protected ClientHttpResponse doExecute(final HttpRequestBase httpMethod) throws IOException {
        return requestFactory.execute(httpMethod, new Function<HttpClientBuilder, Void>() {
            @Nullable
            @Override
            public Void apply(@Nonnull HttpClientBuilder input) {
                final BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();
                if (credentials != null) {
                    final URI uri = httpMethod.getURI();
                    HttpHost hh = new HttpHost(
                        uri.getHost(),
                        uri.getPort(),
                        uri.getScheme());
                    credentialsProvider.setCredentials(new AuthScope(hh), credentials);

                    // Preemptive authentication
                    if (isPreemptiveBasicAuth()) {
                        // Create AuthCache instance
                        AuthCache authCache = new BasicAuthCache();
                        // Generate BASIC scheme object and add it to the local auth cache
                        BasicScheme basicAuth = new BasicScheme();
                        authCache.put(hh, basicAuth);

                        // Add AuthCache to the execution context
                        httpClientContext = HttpClientContext.create();
                        httpClientContext.setCredentialsProvider(credentialsProvider);
                        httpClientContext.setAuthCache(authCache);
                    } else {
                        input.setDefaultCredentialsProvider(credentialsProvider);
                    }
                } else {
                    input.setDefaultCredentialsProvider(credentialsProvider);
                }

                if (useProxy) {
                    final HttpHost proxy = new HttpHost(proxyHost, proxyPort);
                    input.setProxy(proxy);
                    if (proxyCredentials != null) {
                        credentialsProvider.setCredentials(new AuthScope(proxy), proxyCredentials);
                    }
                }
                input.setRedirectStrategy(new LaxRedirectStrategy());
                return null;
            }
        }, this);
    }

    protected HttpRequestBase setupHttpMethod() throws IOException {
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

        if (host == null || protocol == null) {
            throw new IllegalStateException(String.format("%s is not ready to be executed: \n\tprotocol: '%s' " +
                "\n\tuserinfo: '%s'\n\thost: '%s' \n\tport: '%s' \n\taddress: '%s'\n\tquery '%s'" +
                "\n\tfragment: '%s'", getClass().getSimpleName(), protocol, userInfo, host, port, address, query, fragment));
        }

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

        try {
            URI uri = new URI(protocol, userInfo, host, port, address, queryString, fragment);
            httpMethod.setURI(uri);
        } catch (URISyntaxException e) {
            throw new IOException(e);
        }

        final RequestConfig.Builder builder = RequestConfig.custom();
        builder.setAuthenticationEnabled((credentials != null) || (proxyCredentials != null));
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
        StringBuilder sentDataValue = new StringBuilder(httpMethod.getMethod()).append(" ").append(uri.getPath());

        if (uri.getQuery() != null) {
            sentDataValue.append("?" + uri.getQuery());
        }

        sentDataValue.append("\r\n");

        for (Header h : httpMethod.getAllHeaders()) {
            sentDataValue.append(h);
        }

        sentDataValue.append("\r\n");

        if (httpMethod instanceof HttpPost) {
            sentDataValue.append(postData);
        }

        return sentDataValue.toString();
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

        if (list.isEmpty())
            throw new BadSoapResponseEx(envelope);

        return list.get(0);
    }

    public String getProtocol() {
        return protocol;
    }

    public String getFragment() {
        return fragment;
    }

    public void setFragment(String fragment) {
        this.fragment = fragment;
    }

    public String getUserInfo() {
        return userInfo;
    }

    public void setUserInfo(String userInfo) {
        this.userInfo = userInfo;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public CookieStore getCookieStore() {
        return cookieStore;
    }

    public void setCookieStore(CookieStore cookieStore) {
        this.cookieStore = cookieStore;
        HttpContext context = getHttpClientContext();
        if (context == null) {
            httpClientContext = HttpClientContext.create();
        }
        httpClientContext.setAttribute(HttpClientContext.COOKIE_STORE, cookieStore);
    }

    public enum Method {GET, POST}
}
