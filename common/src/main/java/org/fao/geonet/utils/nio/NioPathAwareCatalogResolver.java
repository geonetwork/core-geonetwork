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

package org.fao.geonet.utils.nio;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import org.apache.xml.resolver.CatalogManager;
import org.apache.xml.resolver.tools.CatalogResolver;
import org.fao.geonet.Constants;
import org.fao.geonet.utils.IO;
import org.fao.geonet.utils.ResolverRewriteDirective;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.sax.SAXSource;

/**
 * @author Jesse on 11/4/2014.
 */
public class NioPathAwareCatalogResolver extends CatalogResolver {
    private static final Map<Object, ResolverRewriteDirective> urlRewriteDirectives = Maps.newHashMap();
    private final Set<Path> catalogPaths = Sets.newHashSet();

    public NioPathAwareCatalogResolver(CatalogManager catMan) {
        super(catMan);
        final Vector catalogFiles = catMan.getCatalogFiles();
        // any catalogFiles that are paths and not files must be handled by this child.
        for (Object catalogFile : catalogFiles) {
            final String path = catalogFile.toString();
            if (!new File(path).exists() && Files.exists(IO.toPath(path))) {
                try {
                    final String xml = new String(Files.readAllBytes(IO.toPath(path)), Constants.CHARSET);
                    final Element element = Xml.loadString(xml, false);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public static void addRewriteDirective(ResolverRewriteDirective urlRewrite) {
        urlRewriteDirectives.put(urlRewrite.getKey(), urlRewrite);
    }

    @Override
    public String getResolvedEntity(String publicId, String systemId) {
        return super.getResolvedEntity(publicId, systemId);
    }

    @Override
    public InputSource resolveEntity(String publicId, String systemId) {
        try {
            final InputSource inputSource = Xml.PATH_RESOLVER.resolveEntity(publicId, systemId);
            if (inputSource != null) {
                return inputSource;
            }
        } catch (SAXException | IOException e) {
            throw new RuntimeException(e);
        }
        return super.resolveEntity(publicId, systemId);
    }

    @Override
    public Source resolve(String href, String base) throws TransformerException {
        // For http resources delegate to parent class
        if (href.startsWith("http")) {
            return super.resolve(href, base);
        }

        for (ResolverRewriteDirective urlRewrite : urlRewriteDirectives.values()) {
            if (urlRewrite.appliesTo(href)) {
                href = urlRewrite.rewrite(href);
                break;
            }
        }
        try {
            Path resolvedResource;
            try {
                resolvedResource = IO.toPath(new URI(href));
                if (Files.isRegularFile(resolvedResource)) {
                    return toPathInputSource(resolvedResource);
                } else {
                    SAXSource source = new SAXSource();
                    source.setSystemId(href);
                    return source;
                }
            } catch (URISyntaxException | IllegalArgumentException e) {
                final Path basePath = IO.toPath(new URI(base));
                Path parent = basePath.getParent();
                if (parent == null) {
                    throw new RuntimeException(basePath.getFileName() +
                        " does not have parent");
                }
                resolvedResource = parent.resolve(href);

                if (Files.isRegularFile(resolvedResource)) {
                    return toPathInputSource(resolvedResource);
                }
            }
        } catch (RuntimeException e) {
            throw e;

        } catch (Exception e) {
            // ignore
        }

        return super.resolve(href, base);
    }

    private PathStreamSource toPathInputSource(Path resolvedResource) {
        final PathStreamSource pathInputSource = new PathStreamSource(resolvedResource);
        pathInputSource.setSystemId(resolvedResource.toUri().toASCIIString());
        return pathInputSource;
    }
}
