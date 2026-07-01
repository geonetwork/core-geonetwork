//=============================================================================
//===	Copyright (C) 2001-2026 Food and Agriculture Organization of the
//===	United Nations (FAO-UN), United Nations World Food Programme (WFP)
//===	and United Nations Environment Programme (UNEP)
//===
//===	This library is free software; you can redistribute it and/or
//===	modify it under the terms of the GNU Lesser General Public
//===	License as published by the Free Software Foundation; either
//===	version 2.1 of the License, or (at your option) any later version.
//===
//===	This library is distributed in the hope that it will be useful,
//===	but WITHOUT ANY WARRANTY; without even the implied warranty of
//===	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
//===	Lesser General Public License for more details.
//===
//===	You should have received a copy of the GNU Lesser General Public
//===	License along with this library; if not, write to the Free Software
//===	Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
//===
//===	Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
//===	Rome - Italy. email: geonetwork@osgeo.org
//==============================================================================

package org.fao.geonet.utils;

import net.sf.saxon.Configuration;
import net.sf.saxon.Controller;
import net.sf.saxon.FeatureKeys;
import org.apache.xml.resolver.tools.CatalogResolver;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.transform.JDOMSource;

import javax.xml.transform.*;
import javax.xml.transform.stream.StreamSource;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class XmlTransformer {
    private TransformerFactory factory;
    private Map<String, Object> properties = new HashMap<>();
    private Map<String, Object> transformerParameters = null;

    private XmlTransformer() throws TransformerConfigurationException {
        factory = TransformerFactoryFactory.getTransformerFactory();
    }

    public static XmlTransformer createWithJeevesUIResolver() throws TransformerConfigurationException {
        XmlTransformer t = new XmlTransformer();
        t.factory.setURIResolver(new JeevesURIResolver());
        return t;
    }

    public XmlTransformer withExtraProperties(String key, Object value) {
        properties.put(key, value);
        return this;
    }

    public XmlTransformer withTransformerParameters(Map<String, Object> params) {
        this.transformerParameters = params;
        return this;
    }

    public void transform(Element xml, Path sourcePath, Result result) throws TransformerException, IOException {
        try {
            initialiseCommonConfiguration(factory);
            properties.entrySet().forEach(e -> factory.setAttribute(e.getKey(), e.getValue()));
        } catch (IllegalArgumentException e) {
            Log.warning(Log.ENGINE, "WARNING: transformerfactory does not like saxon attributes!", e);
        } finally {
            try (InputStream in = IO.newInputStream(sourcePath)) {
                Source source = new StreamSource(in, sourcePath.toUri().toASCIIString());

                Transformer t = factory.newTransformer(source);
                if (transformerParameters != null) {
                    initialiseTransformerParameters(t);
                }
                Source srcXml = new JDOMSource(new Document((Element) xml.detach()));
                t.transform(srcXml, result);
            }
        }
    }

    private void initialiseTransformerParameters(Transformer t) {
        transformerParameters.entrySet().forEach(e -> t.setParameter(e.getKey(), e.getValue()));

        if (transformerParameters.containsKey("geonet-force-xml")) {
            ((Controller) t).setOutputProperty("indent", "yes");
            ((Controller) t).setOutputProperty("method", "xml");
            ((Controller) t).setOutputProperty("{http://saxon.sf.net/}indent-spaces", "2");
        }
    }

    private void initialiseCommonConfiguration(TransformerFactory factory) throws TransformerConfigurationException {
        factory.setAttribute(FeatureKeys.VERSION_WARNING, false);
        factory.setAttribute(FeatureKeys.LINE_NUMBERING, true);
        // Dear old saxon likes to yell loudly about each and every XSLT 1.0
        // stylesheet so switch it off but trap any exceptions because this
        // code is run on transformers other than saxon
        factory.setAttribute(FeatureKeys.RECOVERY_POLICY, Configuration.RECOVER_SILENTLY);

        // Add the following to get timing info on xslt transformations
        //transFact.setAttribute(FeatureKeys.TIMING,true);
    }

    private static class JeevesURIResolver implements URIResolver {
        public Source resolve(String href, String base) throws TransformerException {
            Resolver resolver = ResolverWrapper.getInstance();
            CatalogResolver catResolver = resolver.getCatalogResolver();
            if (Log.isDebugEnabled(Log.XML_RESOLVER)) {
                Log.debug(Log.XML_RESOLVER, "Trying to resolve " + href + ":" + base);
            }
            Source s = catResolver.resolve(href, base);

            boolean isFile;
            try {
                final Path file = IO.toPath(new URI(s.getSystemId()));
                isFile = Files.isRegularFile(file);
            } catch (Exception e) {
                isFile = false;
            }

            // If resolver has a blank XSL file use it to replace
            // resolved file that doesn't exist...
            String blankXSLFile = resolver.getBlankXSLFile();
            if (blankXSLFile != null && s.getSystemId().endsWith(".xsl") && !isFile) {
                try {
                    if (Log.isDebugEnabled(Log.XML_RESOLVER)) {
                        Log.debug(Log.XML_RESOLVER, "  Check if exist " + s.getSystemId());
                    }

                    Path f;
                    f = resolvePath(s);
                    if (Log.isDebugEnabled(Log.XML_RESOLVER))
                        Log.debug(Log.XML_RESOLVER, "Check on " + f + " exists returned: " + Files.exists(f));
                    // If the resolved resource does not exist, set it to blank file path to not trigger FileNotFound Exception

                    if (!Files.exists(f)) {
                        if (Log.isDebugEnabled(Log.XML_RESOLVER)) {
                            Log.debug(Log.XML_RESOLVER, "  Resolved resource " + s.getSystemId() + " does not exist. blankXSLFile returned instead.");
                        }
                        s.setSystemId(blankXSLFile);
                    } else {
                        s.setSystemId(f.toUri().toASCIIString());
                    }
                } catch (URISyntaxException e) {
                    Log.warning(Log.XML_RESOLVER, "URI syntax problem: " + e.getMessage(), e);
                }
            }

            if (Log.isDebugEnabled(Log.XML_RESOLVER) && s != null) {
                Log.debug(Log.XML_RESOLVER, "Resolved as " + s.getSystemId());
            }
            return s;
        }

        private Path resolvePath(Source s) throws URISyntaxException {
            Path f;
            final String systemId = s.getSystemId().replaceAll("%5C", "/");
            try {
                f = IO.toPath(new URI(systemId));
            } catch (FileSystemNotFoundException e) {
                f = IO.toPath(systemId);
            }
            return f;
        }
    }
}
