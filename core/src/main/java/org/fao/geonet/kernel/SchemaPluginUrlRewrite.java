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

package org.fao.geonet.kernel;

import org.fao.geonet.constants.Geonet;
import org.fao.geonet.utils.IO;
import org.fao.geonet.utils.PrefixUrlRewrite;
import org.fao.geonet.utils.ResolverRewriteDirective;
import org.jdom.Element;

import java.net.URI;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

/**
 * In the case that the schema_plugins are not files (IE they are in paths like the test fixture) we
 * will add the rewrite objects in {@link org.fao.geonet.utils.nio.NioPathAwareCatalogResolver} so
 * that the resolving will still work.  Normal CatalogManager can't handle java.nio.file.Path
 * objects.
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
            } catch (Exception e) {
                try {
                    if (Files.exists(IO.toPath(replacement))) {
                        rewrites.add(new PrefixUrlRewrite(prefix, IO.toPath(replacement).toUri().toString()));
                    }
                } catch (Exception e2) {
                    // don't add because it is not a path
                }
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
