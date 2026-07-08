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

import net.sf.saxon.*;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.StaticContext;
import net.sf.saxon.functions.FunctionLibrary;
import net.sf.saxon.om.StructuredQName;
import org.apache.xml.resolver.tools.CatalogResolver;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.trans.UnparsedTextURIResolver;
import net.sf.saxon.trans.XPathException;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.transform.JDOMSource;

import javax.xml.XMLConstants;
import javax.xml.transform.*;
import javax.xml.transform.stream.StreamSource;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class XmlTransformer {
    private TransformerFactory factory;
    private TransformerFactory untrustredFactory;
    private Map<String, Object> properties = new HashMap<>();
    private Map<String, Object> transformerParameters = null;
    private final boolean useSaxonParser;

    private XmlTransformer() throws TransformerConfigurationException {
        factory = TransformerFactoryFactory.newTransformerFactory();
        untrustredFactory = TransformerFactoryFactory.newTransformerFactory();
        useSaxonParser = factory instanceof TransformerFactoryImpl;
    }

    public static XmlTransformer createWithJeevesUIResolver() throws TransformerConfigurationException {
        XmlTransformer t = new XmlTransformer();
        t.factory.setURIResolver(new JeevesURIResolver(false));
        t.untrustredFactory.setURIResolver(new JeevesURIResolver(true));
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
        boolean isUntrusted = Files.exists(sourcePath.getParent().resolve("untrusted_formatter_bundle"));
        final TransformerFactory _factory = isUntrusted ? untrustredFactory : this.factory;
        try {
            initialiseCommonConfiguration(_factory);
            if (isUntrusted) {
                initialiseUntrustedFormatterBundle(_factory);
            }
            properties.entrySet().forEach(e -> _factory.setAttribute(e.getKey(), e.getValue()));
        } catch (IllegalArgumentException e) {
            Log.warning(Log.ENGINE, "WARNING: transformerfactory does not like saxon attributes!", e);
        } finally {
            try (InputStream in = IO.newInputStream(sourcePath)) {
                Source source = new StreamSource(in, sourcePath.toUri().toASCIIString());
                Transformer t = _factory.newTransformer(source);
                if (transformerParameters != null) {
                    initialiseTransformerParameters(t);
                }
                Source srcXml = new JDOMSource(new Document((Element) xml.detach()));
                if (useSaxonParser && isUntrusted) {
                    ((Controller) t).setUnparsedTextURIResolver(new UnparsedTextURIResolver() {
                        @Override
                        public Reader resolve(URI uri, String s, Configuration configuration) throws XPathException {
                            throw new XPathException("Unparsed Text URI resolution not supported");
                        }
                    });
                }
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

    private void initialiseCommonConfiguration(TransformerFactory factory) {
        factory.setAttribute(FeatureKeys.VERSION_WARNING, false);
        factory.setAttribute(FeatureKeys.LINE_NUMBERING, true);
        // Dear old saxon likes to yell loudly about each and every XSLT 1.0
        // stylesheet so switch it off but trap any exceptions because this
        // code is run on transformers other than saxon
        factory.setAttribute(FeatureKeys.RECOVERY_POLICY, Configuration.RECOVER_SILENTLY);

        // Add the following to get timing info on xslt transformations
        //transFact.setAttribute(FeatureKeys.TIMING,true);
    }

    private void initialiseUntrustedFormatterBundle(TransformerFactory factory) throws TransformerConfigurationException {
        TransformerFactoryImpl transformerFactory = (TransformerFactoryImpl) factory;
        PatchedConfiguration patchedConfiguration = new PatchedConfiguration(transformerFactory.getConfiguration());
        transformerFactory.setConfiguration(patchedConfiguration);

        factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        if (AllowlistedFunctionLibrary.ALLOW_LIST != null && AllowlistedFunctionLibrary.ALLOW_LIST.isEmpty()) {
            factory.setAttribute(FeatureKeys.ALLOW_EXTERNAL_FUNCTIONS, false);
        } else {
            // custom whitelist
            factory.setAttribute(FeatureKeys.ALLOW_EXTERNAL_FUNCTIONS, true);
            patchedConfiguration.setExtensionBinder("java", new AllowlistedFunctionLibrary(patchedConfiguration.getExtensionBinder("java")));
        }
        factory.setAttribute(FeatureKeys.COLLECTION_URI_RESOLVER, new CollectionURIResolver() {
            @Override
            public SequenceIterator resolve(String s, String s1, XPathContext xPathContext) throws XPathException {
                throw new XPathException("Collection URI resolution not supported");
            }
        });
    }

    private static class JeevesURIResolver implements URIResolver {
        private final boolean isUntrusted;

        public JeevesURIResolver(boolean isUntrusted) {
            this.isUntrusted = isUntrusted;
        }

        public Source resolve(String href, String base) throws TransformerException {
            Resolver resolver = ResolverWrapper.getInstance();
            CatalogResolver catResolver = resolver.getCatalogResolver();
            if (Log.isDebugEnabled(Log.XML_RESOLVER)) {
                Log.debug(Log.XML_RESOLVER, "Trying to resolve " + href + ":" + base);
            }
            Source s = catResolver.resolve(href, base);

            boolean isFile;
            Path file;
            try {
                file = IO.toPath(new URI(s.getSystemId()));
                isFile = Files.isRegularFile(file);
            } catch (Exception e) {
                file = null;
                isFile = false;
            }

            if (isFile && isUntrusted) {
                boolean isOutsideWebappDir = isFileOutsideDir(file, Xml.webappDir);
                boolean isOutsideSchemaPluginsDir = isFileOutsideDir(file, Xml.schemaPluginsDir);
                boolean isOutsideThesauriDir = isFileOutsideDir(file, Xml.thesauriDir);
                if (isOutsideWebappDir && isOutsideSchemaPluginsDir && isOutsideThesauriDir) {
                    throw new XPathException("File " + file + " is not in webapp or schemaPlugins or thesauri directory");
                }
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

        private static boolean isFileOutsideDir(Path file, Path dir) {
            try {
                return dir.relativize(file).toString().contains("..");
            } catch (RuntimeException e) {
                return true;
            }
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

    public static class AllowlistedFunctionLibrary implements FunctionLibrary {
        private final FunctionLibrary delegate;
        private static final Set<String> ALLOW_LIST = Stream.of(
            "{java:org.fao.geonet.util.XslUtil}parseGml",
            "{java:org.fao.geonet.util.XslUtil}gmlToGeoJson",
            "{java:org.fao.geonet.util.XslUtil}addToList",
            "{java:org.fao.geonet.util.XslUtil}toMultiPolygon",
            "{java:org.fao.geonet.util.XslUtil}clean",
            "{java:org.fao.geonet.util.XslUtil}countryMatch",
            "{java:org.fao.geonet.util.XslUtil}replace",
            "{java:org.fao.geonet.util.XslUtil}isCasEnabled",
            "{java:org.fao.geonet.util.XslUtil}getConfigValue",
            "{java:org.fao.geonet.util.XslUtil}getBuildNumber",
            "{java:org.fao.geonet.util.XslUtil}getUiConfiguration",
            "{java:org.fao.geonet.util.XslUtil}getUiConfigurationJsonProperty",
            "{java:org.fao.geonet.util.XslUtil}toUiConfigArg",
            "{java:org.fao.geonet.util.XslUtil}escapeForScriptContext",
            "{java:org.fao.geonet.util.XslUtil}getSettingValue",
            "{java:org.fao.geonet.util.XslUtil}getNodeName",
            "{java:org.fao.geonet.util.XslUtil}getNodeId",
            "{java:org.fao.geonet.util.XslUtil}getNodeLogo",
            "{java:org.fao.geonet.util.XslUtil}getDiscoveryServiceUuid",
            "{java:org.fao.geonet.util.XslUtil}getSource",
            "{java:org.fao.geonet.util.XslUtil}getJsonSettingValue",
            "{java:org.fao.geonet.util.XslUtil}isAuthenticated",
            "{java:org.fao.geonet.util.XslUtil}isDisableLoginForm",
            "{java:org.fao.geonet.util.XslUtil}isShowLoginAsLink",
            "{java:org.fao.geonet.util.XslUtil}isUserProfileUpdateEnabled",
            "{java:org.fao.geonet.util.XslUtil}isUserGroupUpdateEnabled",
            "{java:org.fao.geonet.util.XslUtil}getSecurityProvider",
            "{java:org.fao.geonet.util.XslUtil}getResourceContainerDescription",
            "{java:org.fao.geonet.util.XslUtil}getResourceManagementExternalProperties",
            "{java:org.fao.geonet.util.XslUtil}isAccessibleService",
            "{java:org.fao.geonet.util.XslUtil}takeUntil",
            "{java:org.fao.geonet.util.XslUtil}xmlToJson",
            "{java:org.fao.geonet.util.XslUtil}htmlElement2textReplacer",
            "{java:org.fao.geonet.util.XslUtil}html2text",
            "{java:org.fao.geonet.util.XslUtil}html2textNormalized",
            "{java:org.fao.geonet.util.XslUtil}toWktCoords",
            "{java:org.fao.geonet.util.XslUtil}posListToWktCoords",
            "{java:org.fao.geonet.util.XslUtil}wktGeomToBbox",
            "{java:org.fao.geonet.util.XslUtil}geoJsonGeomToBbox",
            "{java:org.fao.geonet.util.XslUtil}getIndexField",
            "{java:org.fao.geonet.util.XslUtil}getIndexFieldById",
            "{java:org.fao.geonet.util.XslUtil}getCodelistTranslation",
            "{java:org.fao.geonet.util.XslUtil}iso639_2B_to_iso639_2T",
            "{java:org.fao.geonet.util.XslUtil}iso639_2T_to_iso639_2B",
            "{java:org.fao.geonet.util.XslUtil}twoCharLangCode",
            "{java:org.fao.geonet.util.XslUtil}threeCharLangCode",
            "{java:org.fao.geonet.util.XslUtil}match",
            "{java:org.fao.geonet.util.XslUtil}setNoScript",
            "{java:org.fao.geonet.util.XslUtil}allowScripting",
            "{java:org.fao.geonet.util.XslUtil}getUserDetails",
            "{java:org.fao.geonet.util.XslUtil}reprojectCoords",
            "{java:org.fao.geonet.util.XslUtil}geomToBbox",
            "{java:org.fao.geonet.util.XslUtil}getRecord",
            "{java:org.fao.geonet.util.XslUtil}evaluate",
            "{java:org.fao.geonet.util.XslUtil}getSiteUrl",
            "{java:org.fao.geonet.util.XslUtil}getPermalink",
            "{java:org.fao.geonet.util.XslUtil}getDefaultUrl",
            "{java:org.fao.geonet.util.XslUtil}getDefaultLangCode",
            "{java:org.fao.geonet.util.XslUtil}getLanguage",
            "{java:org.fao.geonet.util.XslUtil}encodeForJavaScript",
            "{java:org.fao.geonet.util.XslUtil}encodeForHTML",
            "{java:org.fao.geonet.util.XslUtil}md5Hex",
            "{java:org.fao.geonet.util.XslUtil}encodeForURL",
            "{java:org.fao.geonet.util.XslUtil}decodeURLParameter",
            "{java:org.fao.geonet.util.XslUtil}randomId",
            "{java:org.fao.geonet.util.XslUtil}getMax",
            "{java:org.fao.geonet.util.XslUtil}getThesaurusDir",
            "{java:org.fao.geonet.util.XslUtil}getThesaurusIdByTitle",
            "{java:org.fao.geonet.util.XslUtil}getThesaurusTitleByKey",
            "{java:org.fao.geonet.util.XslUtil}getThesaurusUriByKey",
            "{java:org.fao.geonet.util.XslUtil}getIsoLanguageLabel",
            "{java:org.fao.geonet.util.XslUtil}getKeywordHierarchy",
            "{java:org.fao.geonet.util.XslUtil}getKeywordValueByUri",
            "{java:org.fao.geonet.util.XslUtil}getKeywordUri",
            "{java:org.fao.geonet.util.XslUtil}buildRecordLink",
            "{java:org.fao.geonet.util.XslUtil}escapeForJson",
            "{java:org.fao.geonet.util.XslUtil}escapeForEcmaScript",
            "{java:org.fao.geonet.util.XslUtil}getWebAnalyticsService",
            "{java:org.fao.geonet.util.XslUtil}getWebAnalyticsJavascriptCode",
            "{java:org.fao.geonet.api.records.MetadataUtils}getAssociatedAsXml",
            "{java:org.fao.geonet.api.records.MetadataUtils}isMetadataFieldValueExistingInOtherRecords",
            "{java:org.fao.geonet.api.records.formatters.SchemaLocalizations}create",
            "{java:org.fao.geonet.api.records.formatters.SchemaLocalizations}codelist-value-label",
            "{java:org.fao.geonet.api.records.formatters.SchemaLocalizations}codelist-value-desc",
            "{java:org.fao.geonet.api.records.formatters.SchemaLocalizations}nodeDesc",
            "{java:org.fao.geonet.api.records.formatters.SchemaLocalizations}nodeLabel").collect(Collectors.toSet());

        public AllowlistedFunctionLibrary(FunctionLibrary delegate) {
            this.delegate = delegate;
        }

        @Override
        public boolean isAvailable(StructuredQName structuredQName, int i) {
            if (ALLOW_LIST.contains(structuredQName.getClarkName())) {
                return delegate.isAvailable(structuredQName, i);
            } else {
                return false;
            }
        }

        @Override
        public Expression bind(StructuredQName structuredQName, Expression[] expressions, StaticContext staticContext) throws XPathException {
            if (ALLOW_LIST.contains(structuredQName.getClarkName())) {
                return delegate.bind(structuredQName, expressions, staticContext);
            } else {
                return null;
            }
        }

        @Override
        public FunctionLibrary copy() {
            return delegate.copy();
        }
    }
}
