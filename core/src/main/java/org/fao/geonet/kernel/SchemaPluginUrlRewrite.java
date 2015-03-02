package org.fao.geonet.kernel;

import org.fao.geonet.constants.Geonet;
import org.fao.geonet.utils.IO;
import org.fao.geonet.utils.PrefixUrlRewrite;
import org.fao.geonet.utils.ResolverRewriteDirective;
import org.jdom.Element;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

/**
 * In the case that the schema_plugins are not files (IE they are in paths like the test fixture) we will
 * add the rewrite objects in {@link org.fao.geonet.utils.nio.NioPathAwareCatalogResolver} so that the resolving will
 * still work.  Normal CatalogManager can't handle java.nio.file.Path objects.
 *
 * @author Jesse on 2/28/2015.
 */
public class SchemaPluginUrlRewrite implements ResolverRewriteDirective {
    List<PrefixUrlRewrite> rewrites = new ArrayList<>();
    public SchemaPluginUrlRewrite(Element root) {
        @SuppressWarnings("unchecked")
        final List<Element> rewriteURIs = root.getChildren("rewriteURI", Geonet.Namespaces.OASIS_CATALOG);

        for (Element rewriteURI : rewriteURIs) {
            // only handle Paths
            final String prefix = rewriteURI.getAttributeValue("uriStartString");
            final String replacement = rewriteURI.getAttributeValue("rewritePrefix");
            try {
                if (Files.exists(IO.toPath(new URI(replacement)))) {
                    rewrites.add(new PrefixUrlRewrite(prefix, replacement));
                }
            } catch (URISyntaxException e) {
                // don't add because it is not a path
            }
        }
    }

    @Override
    public boolean appliesTo(String href) {
        for (PrefixUrlRewrite rewrite : rewrites) {
            if (rewrite.appliesTo(href)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String rewrite(String href) {
        for (PrefixUrlRewrite rewrite : rewrites) {
            if (rewrite.appliesTo(href)) {
                return rewrite.rewrite(href);
            }
        }
        return null;
    }

    @Override
    public Object getKey() {
        return "schema_plugin_url_rewrite";
    }
}
