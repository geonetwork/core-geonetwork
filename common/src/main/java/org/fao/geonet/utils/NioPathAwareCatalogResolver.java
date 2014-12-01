package org.fao.geonet.utils;

import com.google.common.collect.Maps;
import org.apache.xml.resolver.CatalogManager;
import org.apache.xml.resolver.tools.CatalogResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamSource;

/**
* @author Jesse on 11/4/2014.
*/
public class NioPathAwareCatalogResolver extends CatalogResolver {
    private static final Map<Object, ResolverRewriteDirective> urlRewriteDirectives = Maps.newHashMap();
    public NioPathAwareCatalogResolver(CatalogManager catMan) {
        super(catMan);
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
            } catch (URISyntaxException | IllegalArgumentException e) {
                final Path basePath = IO.toPath(new URI(base));
                resolvedResource = basePath.getParent().resolve(href);
            }

            if (Files.isRegularFile(resolvedResource)) {
                return new StreamSource(Files.newInputStream(resolvedResource), resolvedResource.toUri().toASCIIString());
            }
        } catch (Exception e) {
            // ignore
        }

        return super.resolve(href, base);
    }

    public static void addRewriteDirective(ResolverRewriteDirective urlRewrite) {
        urlRewriteDirectives.put(urlRewrite.getKey(), urlRewrite);
    }
}
