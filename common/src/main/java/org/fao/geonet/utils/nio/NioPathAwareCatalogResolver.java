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
                if (Files.isRegularFile(resolvedResource)) {
                    return toPathInputSource(resolvedResource);
                } else {
                    SAXSource source = new SAXSource();
                    source.setSystemId(href);
                    return source;
                }
            } catch (URISyntaxException | IllegalArgumentException e) {
                final Path basePath = IO.toPath(new URI(base));
                resolvedResource = basePath.getParent().resolve(href);

                if (Files.isRegularFile(resolvedResource)) {
                    return toPathInputSource(resolvedResource);
                }
            }
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

    public static void addRewriteDirective(ResolverRewriteDirective urlRewrite) {
        urlRewriteDirectives.put(urlRewrite.getKey(), urlRewrite);
    }
}
