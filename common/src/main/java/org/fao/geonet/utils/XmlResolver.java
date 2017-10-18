/*
 * Copyright (C) 2001-2016 Food and Agriculture Organization of the
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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.jcs.access.exception.CacheException;
import org.apache.xerces.dom.DOMInputImpl;
import org.apache.xerces.util.XMLCatalogResolver;
import org.fao.geonet.JeevesJCS;
import org.jdom.Element;
import org.w3c.dom.ls.LSInput;

/* Resolves system and public ids as well as URIs using oasis catalog
   as per XMLCatalogResolver, but goes further and retrieves any
	 external references since we need to use config'd proxy details on
	 any http connection we make and Xerces doesn't do this (why?)
	 hence this extension.  */

public class XmlResolver extends XMLCatalogResolver {

    public static final String XMLRESOLVER_JCS = "XmlResolver";
    private ProxyParams proxyParams;


    //--------------------------------------------------------------------------

    /**
     * <p>Constructs a catalog resolver with the given list of entry files.</p>
     *
     * @param catalogs    an ordered array list of absolute URIs
     * @param proxyParams proxy parameters when connecting to external sites
     */
    public XmlResolver(String[] catalogs, ProxyParams proxyParams) {
        super(catalogs, true);
        this.proxyParams = proxyParams;
    }

    //--------------------------------------------------------------------------

    /**
     * <p>Resolves any public and system ids as well as URIs from the catalog - also retrieves any
     * external references using Jeeves XmlRequest so that config'd proxy details can be used on the
     * http connection.</p>
     *
     * @param type         the type of the resource being resolved (usually XML schema)
     * @param namespaceURI the namespace of the resource being resolved, or <code>null</code> if
     *                     none was supplied
     * @param publicId     the public identifier of the resource being resolved, or
     *                     <code>null</code> if none was supplied
     * @param systemId     the system identifier of the resource being resolved, or
     *                     <code>null</code> if none was supplied
     * @param baseURI      the absolute base URI of the resource being parsed, or <code>null</code>
     *                     if there is no base URI
     */
    public LSInput resolveResource(String type, String namespaceURI, String publicId, String systemId, String baseURI) {

        if (Log.isDebugEnabled(Log.XML_RESOLVER))
            Log.debug(Log.XML_RESOLVER, "Jeeves XmlResolver: Before resolution: Type: " + type + " NamespaceURI :" + namespaceURI + " " +
                "PublicId :" + publicId + " SystemId :" + systemId + " BaseURI:" + baseURI);
        LSInput result = null;

        try {
            result = tryToResolveOnFs(publicId, systemId, baseURI);
            if (result != null) {
                return result;
            }
        } catch (FileSystemNotFoundException e) {
            //Do nothing, just continue
        }

        result = super.resolveResource(type, namespaceURI, publicId, systemId, baseURI);

        if (result != null) { // some changes made so update
            publicId = result.getPublicId();
            systemId = result.getSystemId();
            baseURI = result.getBaseURI();
        }

        if (Log.isDebugEnabled(Log.XML_RESOLVER))
            Log.debug(Log.XML_RESOLVER, "Jeeves XmlResolver: After resolution: PublicId :" + publicId + " SystemId :" + systemId + " " +
                "BaseURI:" + baseURI);

        URL externalRef = null;
        try {

            if (publicId != null && publicId.startsWith("http://")) {
                externalRef = new URL(publicId);
            } else if (systemId != null && systemId.startsWith("http://")) {
                externalRef = new URL(systemId);
            } else if (systemId != null && baseURI != null) {
                if (baseURI.startsWith("http://")) {
                    URL ref = new URL(baseURI);
                    String thePath = new File(ref.getPath()).getParent().replace('\\', '/');
                    externalRef = new URI(ref.getProtocol(), null, ref.getHost(), ref.getPort(), thePath + "/" + systemId, null,
                        null).toURL();
                }
            }
        } catch (MalformedURLException e) { // leave this to someone else?
            Log.error(Log.XML_RESOLVER, e.getMessage(), e);
            return result;
        } catch (URISyntaxException e) { // leave this to someone else?
            Log.error(Log.XML_RESOLVER, e.getMessage(), e);
            return result;
        }

        if (externalRef != null) {

            Element elResult = null;

            try {
                elResult = isXmlInCache(externalRef.toString());
            } catch (CacheException e) {
                Log.error(Log.XML_RESOLVER, "Request to cache for " + externalRef + " failed.", e);
            }

            if (elResult == null) { // use XMLRequest to get the XML
                XmlRequest xml = new GeonetHttpRequestFactory().createXmlRequest(externalRef);
                if (proxyParams.useProxy) {
                    xml.setUseProxy(true);
                    xml.setProxyHost(proxyParams.proxyHost);
                    xml.setProxyPort(proxyParams.proxyPort);
                    if (proxyParams.useProxyAuth) {
                        xml.setProxyCredentials(proxyParams.username, proxyParams.password);
                    }
                }

                elResult = null;
                try {
                    elResult = xml.execute();
                    addXmlToCache(externalRef.toString(), elResult);
                    if (Log.isDebugEnabled(Log.XML_RESOLVER)) {
                        Log.debug(Log.XML_RESOLVER, "Retrieved: \n" + Xml.getString(elResult));
                    }
                } catch (Exception e) {
                    Log.error(Log.XML_RESOLVER, "Request on " + externalRef + " failed." + e.getMessage());
                }

            }

            if (result == null) {
                result = new DOMInputImpl(publicId, systemId, baseURI);
            }
            if (elResult != null) {
                result.setStringData(Xml.getString(elResult));
            }
        }
        return result;
    }

    private LSInput tryToResolveOnFs(String publicId, String systemId, String baseURI) {
        if (baseURI != null && systemId != null) {
            try {
                Path basePath = IO.toPath(new URI(baseURI));
                Path parent = basePath.getParent();
                if (parent == null) {
                    throw new RuntimeException(basePath.toUri() + " does not have parent");
                }
                final Path resolved = parent.resolve(systemId);
                if (Files.isRegularFile(resolved)) {
                    try {
                        final String uri = resolved.normalize().toUri().toASCIIString();
                        return new DOMInputImpl(publicId, uri, uri, new ByteArrayInputStream(Files.readAllBytes(resolved)), null);
                    } catch (IOException e) {
                        Log.error(Log.JEEVES, "Error opening resource: " + resolved + " for reading", e);
                    }
                }
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }
        }
        return null;
    }


    //--------------------------------------------------------------------------

    /**
     * Clear the cache.
     */
    public void clearCache() throws CacheException {
        JeevesJCS.getInstance(XMLRESOLVER_JCS).clear();
    }

    //--------------------------------------------------------------------------

    /**
     * Add an XLink to the cache.
     */
    public void addXmlToCache(String uri, Element xml) throws CacheException {
        JeevesJCS xmlCache = JeevesJCS.getInstance(XMLRESOLVER_JCS);
        Element cachedXml = (Element) xmlCache.get(uri.toLowerCase());
        if (cachedXml == null) {
            Log.info(Log.XML_RESOLVER, "Caching " + uri.toLowerCase());
            xmlCache.put(uri.toLowerCase(), xml);
        }
    }

    //--------------------------------------------------------------------------

    /**
     * Resolves an xlink
     */
    public Element isXmlInCache(String uri) throws CacheException {
        JeevesJCS xmlCache = JeevesJCS.getInstance(XMLRESOLVER_JCS);
        Element xml = (Element) xmlCache.get(uri.toLowerCase());

        if (xml == null) {
            Log.info(Log.XML_RESOLVER, "cache MISS on " + uri.toLowerCase());
        } else {
            Log.info(Log.XML_RESOLVER, "cache HIT on " + uri.toLowerCase());
        }
        return xml;
    }

}
