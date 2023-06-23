//=============================================================================
//===
//=== SchemaManager
//===
//=============================================================================
//=== Copyright (C) 2001-2011 Food and Agriculture Organization of the
//=== United Nations (FAO-UN), United Nations World Food Programme (WFP)
//=== and United Nations Environment Programme (UNEP)
//===
//===	This program is free software; you can redistribute it and/or modify
//===	it under the terms of the GNU General Public License as published by
//===	the Free Software Foundation; either version 2 of the License, or (at
//===	your option) any later version.
//===
//===	This program is distributed in the hope that it will be useful, but
//===	WITHOUT ANY WARRANTY; without even the implied warranty of
//===	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
//===	General Public License for more details.
//===
//===	You should have received a copy of the GNU General Public License
//===	along with this program; if not, write to the Free Software
//===	Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
//===
//===	Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
//===	Rome - Italy. email: geonetwork@osgeo.org
//==============================================================================

package org.fao.geonet.kernel;

import com.google.common.annotations.VisibleForTesting;
import jeeves.server.context.ServiceContext;
import jeeves.server.dispatchers.guiservices.XmlFile;
import org.apache.commons.lang.StringUtils;
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.Constants;
import org.fao.geonet.SystemInfo;
import org.fao.geonet.ZipUtil;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.constants.Geonet.Namespaces;
import org.fao.geonet.domain.Pair;
import org.fao.geonet.exceptions.NoSchemaMatchesException;
import org.fao.geonet.exceptions.OperationAbortedEx;
import org.fao.geonet.exceptions.SchemaMatchConflictException;
import org.fao.geonet.kernel.schema.MetadataSchema;
import org.fao.geonet.kernel.schema.SchemaLoader;
import org.fao.geonet.kernel.schema.SchemaPlugin;
import org.fao.geonet.kernel.setting.SettingInfo;
import org.fao.geonet.repository.SchematronCriteriaGroupRepository;
import org.fao.geonet.repository.SchematronRepository;
import org.fao.geonet.schema.iso19139.ISO19139SchemaPlugin;
import org.fao.geonet.utils.IO;
import org.fao.geonet.utils.Log;
import org.fao.geonet.utils.PrefixUrlRewrite;
import org.fao.geonet.utils.ResolverWrapper;
import org.fao.geonet.utils.Version;
import org.fao.geonet.utils.Xml;
import org.fao.geonet.utils.nio.NioPathAwareCatalogResolver;
import org.jdom.Attribute;
import org.jdom.Content;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.filter.ElementFilter;
import org.springframework.context.ApplicationContext;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Class that handles all functions relating to metadata schemas. This includes
 * inserting/removing/updating metadata schemas for use in GeoNetwork as well as determining whether
 * a metadata record belongs to any of the metadata schemas known to GeoNetwork.
 *
 * The Schema class holds all information describing a schema in GeoNetwork. The SchemaManager holds
 * a map of Schema objects known to GeoNetwork.
 */
public class SchemaManager {

    private static final int MODE_NEEDLE = 0;
    private static final int MODE_ROOT = 1;
    private static final int MODE_NEEDLEWITHVALUE = 2;
    private static final int MODE_ATTRIBUTEWITHVALUE = 3;
    private static final int MODE_NAMESPACE = 4;

    private static final String GEONET_SCHEMA_URI = "http://geonetwork-opensource.org/schemas/schema-ident";
    private static final Namespace GEONET_SCHEMA_PREFIX_NS = Namespace.getNamespace("gns", GEONET_SCHEMA_URI);
    private static final Namespace GEONET_SCHEMA_NS = Namespace.getNamespace(GEONET_SCHEMA_URI);
    /**
     * Active readers count
     */
    private static int activeReaders = 0;
    /**
     * Active writers count
     */
    private static int activeWriters = 0;
    private Map<String, Schema> hmSchemas = new HashMap<String, Schema>();
    private Map<String, Namespace> hmSchemasTypenames = new HashMap<String, Namespace>();
    private String[] fnames = {"labels.xml", "codelists.xml", "strings.xml"};
    private Path schemaPluginsDir;
    private Path schemaPluginsCat;
    private boolean createOrUpdateSchemaCatalog;
    private String defaultLang;
    private String defaultSchema;
    private Path basePath;
    private Path resourcePath;
    private Path schemaPublicationDir;
    private int numberOfCoreSchemasAdded = 0;

    public static Path registerXmlCatalogFiles(Path webappDir, Path schemapluginUriCatalog) {
        Path webInf = webappDir.resolve("WEB-INF");

        //--- Set jeeves.xml.catalog.files property
        //--- this is critical to schema support so must be set correctly
        String catalogProp = System.getProperty(Constants.XML_CATALOG_FILES);
        if (catalogProp == null) {
            catalogProp = "";
        }
        if (!catalogProp.equals("")) {
            Log.info(Geonet.SCHEMA_MANAGER, "Overriding " + Constants.XML_CATALOG_FILES + " property (was set to " + catalogProp + ")");
        }
        catalogProp = webInf.resolve("oasis-catalog.xml") + ";" + schemapluginUriCatalog;
        System.setProperty(Constants.XML_CATALOG_FILES, catalogProp);
        Log.info(Geonet.SCHEMA_MANAGER, Constants.XML_CATALOG_FILES + " property set to " + catalogProp);

        Path blankXSLFile = webappDir.resolve("xsl").resolve("blanks.xsl");
        System.setProperty(Constants.XML_CATALOG_BLANKXSLFILE, blankXSLFile.toUri().toASCIIString());
        Log.info(Geonet.SCHEMA_MANAGER, Constants.XML_CATALOG_BLANKXSLFILE + " property set to " + blankXSLFile);

        return webInf;
    }

    public static SchemaPlugin getSchemaPlugin(String schemaIdentifier) {
        String schemaBeanIdentifier = schemaIdentifier + "SchemaPlugin";
        SchemaPlugin schemaPlugin = null;
        try {
            if (ApplicationContextHolder.get() != null) {
                schemaPlugin = (SchemaPlugin) ApplicationContextHolder.
                    get().
                    getBean(schemaBeanIdentifier);

                if (schemaPlugin == null &&
                    schemaIdentifier.startsWith(ISO19139SchemaPlugin.IDENTIFIER)) {
                    // For ISO19139 profiles, get the ISO19139 bean if no custom one defined
                    // Can't depend here on ISO19139SchemaPlugin to avoid to introduce
                    // circular ref.
                    schemaBeanIdentifier = ISO19139SchemaPlugin.IDENTIFIER + "SchemaPlugin";
                    schemaPlugin = (SchemaPlugin) ApplicationContextHolder.
                        get().
                        getBean(schemaBeanIdentifier);
                }
            }
        } catch (Exception e) {
            // No bean for this schema
            if (Log.isDebugEnabled(Geonet.SCHEMA_MANAGER)) {
                Log.debug(Geonet.SCHEMA_MANAGER, "No bean defined for the schema plugin '" +
                    schemaIdentifier + "'. " +
                    e.getMessage());
            }
        }
        return schemaPlugin;
    }

    @VisibleForTesting
    public void configureFrom(SchemaManager schemaManager, Path basePath, GeonetworkDataDirectory dataDir) {
        this.basePath = basePath;
        this.resourcePath = dataDir.getResourcesDir();
        this.schemaPublicationDir = dataDir.getSchemaPublicationDir();
        this.schemaPluginsDir = dataDir.getSchemaPluginsDir();
        this.schemaPluginsCat = schemaPluginsDir.resolve("schemaplugin-uri-catalog.xml");
        this.defaultLang = schemaManager.defaultLang;
        this.defaultSchema = schemaManager.defaultSchema;
        this.createOrUpdateSchemaCatalog = schemaManager.createOrUpdateSchemaCatalog;

        addResolverRewriteDirectives(dataDir);

        this.hmSchemas.clear();
        this.hmSchemas.putAll(schemaManager.hmSchemas);


        fnames = new String[schemaManager.fnames.length];
        System.arraycopy(schemaManager.fnames, 0, fnames, 0, fnames.length);
        numberOfCoreSchemasAdded = schemaManager.numberOfCoreSchemasAdded;

    }

    private void addResolverRewriteDirectives(GeonetworkDataDirectory dataDir) {
        NioPathAwareCatalogResolver.addRewriteDirective(new PrefixUrlRewrite("sharedFormatterDir/",
            dataDir.getFormatterDir().toAbsolutePath().toUri() + "/"));
    }

    /**
     * initialize and configure schema manager. should only be on startup.
     *
     * @param basePath         the web app base path
     * @param schemaPluginsCat the schema catalogue file
     * @param sPDir            the schema plugin directory
     * @param defaultLang      the default language (taken from context)
     * @param defaultSchema    the default schema (taken from config.xml)
     */
    public void configure(ApplicationContext applicationContext, Path basePath, Path resourcePath, Path schemaPublicationDir, Path schemaPluginsCat,
                          Path sPDir, String defaultLang, String defaultSchema, boolean createOrUpdateSchemaCatalog) throws Exception {

        hmSchemas.clear();

        this.basePath = basePath;
        this.resourcePath = resourcePath;
        this.schemaPublicationDir = schemaPublicationDir;
        this.schemaPluginsDir = sPDir;
        this.schemaPluginsCat = schemaPluginsCat;
        this.defaultLang = defaultLang;
        this.defaultSchema = defaultSchema;
        this.createOrUpdateSchemaCatalog = createOrUpdateSchemaCatalog;

        Element schemaPluginCatRoot = getSchemaPluginCatalogTemplate();

        addResolverRewriteDirectives(applicationContext.getBean(GeonetworkDataDirectory.class));

        // -- check the plugin directory and add any schemas already in there
        try (DirectoryStream<Path> saSchemas = Files.newDirectoryStream(this.schemaPluginsDir)) {
            for (Path schemaDir : saSchemas) {
                if (!schemaDir.getFileName().toString().equals("CVS") && !schemaDir.getFileName().startsWith(".")) {
                    if (Files.isDirectory(schemaDir)) {
                        Log.info(Geonet.SCHEMA_MANAGER, "Loading schema " + schemaDir.getFileName() + "...");
                        processSchema(applicationContext, schemaDir, schemaPluginCatRoot);
                    }
                }
            }

            checkAppSupported(schemaPluginCatRoot);

            checkDependencies(schemaPluginCatRoot);
        }

        writeSchemaPluginCatalog(schemaPluginCatRoot);

    }

    //--------------------------------------------------------------------------
    //---
    //--- API methods
    //---
    //--------------------------------------------------------------------------

    /**
     * Ensures singleton-ness by preventing cloning.
     */
    @Override
    public Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }

    /**
     * Return the MetadataSchema objects
     *
     * @param name the metadata schema we want the MetadataSchema for
     */
    public MetadataSchema getSchema(String name) {

        beforeRead();
        try {
            Schema schema = hmSchemas.get(name);

            if (schema == null)
                throw new IllegalArgumentException("Schema not registered : " + name);

            final MetadataSchema mds = schema.getMetadataSchema();
            return mds;
        } finally {
            afterRead();
        }
    }

    /**
     * Return the list of dependent schemas
     *
     * @param name the metadata schema we want the list of dependencies for
     */
    public Set<String> getDependencies(String name) {

        Set<String> dependencies = new HashSet<String>();

        beforeRead();
        try {
            Schema schema = hmSchemas.get(name);
            if (schema != null) { // if it is null then that is a config error
                List<Element> dependsList = schema.getDependElements();
                for (Element depends : dependsList) {
                    String depSchemaName = depends.getText();
                    dependencies.add(depSchemaName);
                }
            }
            return dependencies;
        } finally {
            afterRead();
        }
    }

    /**
     * Return the Id and Version of the schema
     *
     * @param name the metadata schema we want the MetadataSchema for
     * @return Pair with schema Id and Version
     */
    public Pair<String, String> getIdVersion(String name) {

        beforeRead();
        try {
            Schema schema = hmSchemas.get(name);

            if (schema == null)
                throw new IllegalArgumentException("Schema not registered : " + name);

            return Pair.read(schema.getId(), schema.getVersion());
        } finally {
            afterRead();
        }
    }

    /**
     * Adds a plugin schema to the list of schemas registered here.
     *
     * @param name  the metadata schema we want to add
     * @param zipFs A filesystem (probably a ZipFileSystem) to copy files from.
     */
    public void addPluginSchema(ApplicationContext applicationContext, String name, FileSystem zipFs) throws Exception {

        beforeWrite();
        try {
            realAddPluginSchema(applicationContext, name, zipFs);
        } finally {
            afterWrite();
        }
    }

    /**
     * Updates a plugin schema in the list of schemas registered here.
     *
     * @param name  the metadata schema we want to update
     * @param zipFs A filesystem (probably a ZipFileSystem) to copy files from.
     */
    public void updatePluginSchema(ApplicationContext applicationContext, String name, FileSystem zipFs) throws Exception {

        beforeWrite();
        try {
            // -- delete schema, trap any exception here as we need to say
            // -- why the update failed
            try {
                boolean doDependencies = false;
                realDeletePluginSchema(name, doDependencies);
            } catch (Exception e) {
                String errStr = "Could not update schema " + name + ", remove of outdated schema failed. Exception message if any is " + e.getMessage();
                Log.error(Geonet.SCHEMA_MANAGER, errStr, e);
                throw new OperationAbortedEx(errStr, e);
            }

            // -- add the new one
            realAddPluginSchema(applicationContext, name, zipFs);
        } finally {
            afterWrite();
        }
    }

    /**
     * Returns the schema directory.
     *
     * @param name the metadata schema we want the directory for
     */
    public Path getSchemaDir(String name) {

        beforeRead();
        try {
            Schema schema = hmSchemas.get(name);

            if (schema == null)
                throw new IllegalArgumentException("Schema not registered : " + name);

            return schema.getDir();
        } finally {
            afterRead();
        }
    }

    /**
     * Returns the schema location as a JDOM attribute - this can be  either an xsi:schemaLocation
     * or xsi:noNamespaceSchemaLocation depending on the schema.
     *
     * @param name the metadata schema we want the schemaLocation for
     */
    public Attribute getSchemaLocation(String name, ServiceContext context) {

        Attribute out = null;

        beforeRead();
        try {
            Schema schema = hmSchemas.get(name);

            if (schema == null)
                throw new IllegalArgumentException("Schema not registered : " + name);

            String nsUri = schema.getMetadataSchema().getPrimeNS();
            String schemaLoc = schema.getSchemaLocation();
            Path schemaFile = schema.getDir().resolve("schema.xsd");

            if (schemaLoc.equals("")) {
                if (Files.exists(schemaFile)) { // build one
                    String schemaUrl = getSchemaUrl(context, name);
                    if (nsUri == null || nsUri.equals("")) {
                        out = new Attribute("noNamespaceSchemaLocation", schemaUrl, Geonet.Namespaces.XSI);
                    } else {
                        schemaLoc = nsUri + " " + schemaUrl;
                        out = new Attribute("schemaLocation", schemaLoc, Geonet.Namespaces.XSI);
                    }
                } // else return null - no schema xsd exists - could be dtd
            } else {
                if (nsUri == null || nsUri.equals("")) {
                    out = new Attribute("noNamespaceSchemaLocation", schemaLoc, Geonet.Namespaces.XSI);
                } else {
                    out = new Attribute("schemaLocation", schemaLoc, Geonet.Namespaces.XSI);
                }
            }
            return out;
        } finally {
            afterRead();
        }
    }

    /**
     * Returns the schema templatesdirectory.
     *
     * @param name the metadata schema we want the templates directory for
     */
    public Path getSchemaTemplatesDir(String name) {

        beforeRead();
        try {
            Path dir = getSchemaDir(name);

            dir = dir.resolve("templates");
            if (!Files.exists(dir)) {
                return null;
            }
            return dir;
        } finally {
            afterRead();
        }
    }

    /**
     * Returns the schema sample data directory.
     *
     * @param name the metadata schema we want the sample data directory for
     */
    public Path getSchemaSampleDataDir(String name) {

        beforeRead();
        try {
            Path dir = getSchemaDir(name);

            dir = dir.resolve("sample-data");
            if (!Files.exists(dir)) {
                return null;
            }
            return dir;
        } finally {
            afterRead();
        }
    }

    /**
     * Returns the schema csw presentation directory.
     *
     * @param name the metadata schema we want the csw present info directory
     */
    public Path getSchemaCSWPresentDir(String name) {

        beforeRead();
        try {
            Path dir = getSchemaDir(name);

            dir = dir.resolve("present").resolve("csw");

            return dir;
        } finally {
            afterRead();
        }
    }

    /**
     * Return the schema information (usually localized codelists, labels etc) XmlFile objects.
     *
     * @param name the metadata schema we want schema info for
     */
    public Map<String, XmlFile> getSchemaInfo(String name) {

        beforeRead();
        try {
            Schema schema = hmSchemas.get(name);

            if (schema == null)
                throw new IllegalArgumentException("Schema not registered : " + name);

            return schema.getInfo();
        } finally {
            afterRead();
        }
    }

    /**
     * Returns the list of schema names that have been registered.
     */
    public Set<String> getSchemas() {

        beforeRead();
        try {
            return hmSchemas.keySet();
        } finally {
            afterRead();
        }
    }

    /**
     * Returns the schema converter elements for a schema (as a list of cloned elements).
     *
     * @param name the metadata schema we want search
     * @throws Exception if schema is not registered
     */
    public List<Element> getConversionElements(String name) throws Exception {

        beforeRead();
        try {
            Schema schema = hmSchemas.get(name);
            List<Element> childs = schema.getConversionElements();
            List<Element> dChilds = new ArrayList<Element>();
            for (Element child : childs) {
                if (child != null) dChilds.add((Element) child.clone());
            }
            return dChilds;
        } finally {
            afterRead();
        }
    }

    /**
     * Return the schema converter(s) that produce the specified namespace.
     *
     * @param name         the metadata schema we want search
     * @param namespaceUri the namespace URI we are looking for
     * @return List of XSLTs that produce this namespace URI (full pathname)
     * @throws Exception if schema is not registered
     */
    public List<Path> existsConverter(String name, String namespaceUri) throws Exception {

        List<Path> result = new ArrayList<>();

        beforeRead();
        try {
            Schema schema = hmSchemas.get(name);
            List<Element> converterElems = schema.getConversionElements();
            for (Element elem : converterElems) {
                String nsUri = elem.getAttributeValue("nsUri");
                if (nsUri != null && nsUri.equals(namespaceUri)) {
                    String xslt = elem.getAttributeValue("xslt");
                    if (xslt != null) {
                        result.add(schema.getDir().resolve(xslt));
                    }
                }
            }
            return result;
        } finally {
            afterRead();
        }
    }

    /**
     * Whether the schema named in the parameter exist.
     *
     * @param name the metadata schema we want to check existence of
     */
    public boolean existsSchema(String name) {

        beforeRead();
        try {
            return hmSchemas.containsKey(name);
        } finally {
            afterRead();
        }
    }


    /**
     * Deletes the schema from the schema information hash tables.
     *
     * @param name the metadata schema we want to delete - can only be a plugin schema
     */
    public void deletePluginSchema(String name) throws Exception {

        beforeWrite();
        try {
            boolean doDependencies = true;
            realDeletePluginSchema(name, doDependencies);

        } finally {
            afterWrite();
        }
    }

    /**
     * Gets the SchemaSuggestions class for the supplied schema name.
     *
     * @param name the metadata schema whose suggestions class we want
     */
    public SchemaSuggestions getSchemaSuggestions(String name) {

        beforeRead();
        try {
            Schema schema = hmSchemas.get(name);

            if (schema == null)
                throw new IllegalArgumentException("Schema suggestions not registered : " + name);

            return schema.getSuggestions();
        } finally {
            afterRead();
        }
    }

    /**
     * Gets the namespace URI from the schema information (XSD) for the supplied prefix.
     *
     * @param name   the metadata schema whose namespaces we are searching
     * @param prefix the namespace prefix we want the URI for
     */
    public String getNamespaceURI(String name, String prefix) {

        beforeRead();
        try {
            Schema schema = hmSchemas.get(name);

            if (schema == null)
                throw new IllegalArgumentException("Schema not registered : " + name);

            MetadataSchema mds = schema.getMetadataSchema();
            return mds.getNS(prefix);
        } finally {
            afterRead();
        }
    }

    /**
     * Gets the namespaces from schema information (XSD) as a string for use as a list of
     * namespaces.
     *
     * @param name the metadata schema whose namespaces we want
     */
    public String getNamespaceString(String name) {

        beforeRead();
        try {
            Schema schema = hmSchemas.get(name);

            if (schema == null)
                throw new IllegalArgumentException("Schema not registered : " + name);

            MetadataSchema mds = schema.getMetadataSchema();
            StringBuilder sb = new StringBuilder();
            for (Namespace ns : mds.getSchemaNS()) {
                if (ns.getPrefix().length() != 0 && ns.getURI().length() != 0) {
                    sb.append("xmlns:" + ns.getPrefix() + "=\"" + ns.getURI() + "\" ");
                }
            }
            return sb.toString().trim();
        } finally {
            afterRead();
        }
    }

    /**
     * Used to detect the schema of an imported metadata file.
     *
     * @param md the imported metadata file
     */
    public String autodetectSchema(Element md) throws SchemaMatchConflictException, NoSchemaMatchesException {
        return autodetectSchema(md, defaultSchema);
    }

    /**
     *
     * @param md
     * @param defaultSchema
     * @return
     * @throws SchemaMatchConflictException
     * @throws NoSchemaMatchesException
     */
    public String autodetectSchema(Element md, String defaultSchema) throws SchemaMatchConflictException, NoSchemaMatchesException {

        beforeRead();
        try {
            String schema;

            // -- check the autodetect elements for all schemas with the most
            // -- specific test first, then in order of increasing generality,
            // -- first match wins
            schema = compareElementsAndAttributes(md, MODE_ATTRIBUTEWITHVALUE);
            if (schema != null) {
                if (Log.isDebugEnabled(Geonet.SCHEMA_MANAGER))
                    Log.debug(Geonet.SCHEMA_MANAGER, "  => Found schema " + schema + " using AUTODETECT(attributes) examination");
            }

            if (schema == null) {
                schema = compareElementsAndAttributes(md, MODE_NEEDLEWITHVALUE);
                if (schema != null) {
                    if (Log.isDebugEnabled(Geonet.SCHEMA_MANAGER))
                        Log.debug(Geonet.SCHEMA_MANAGER, "  => Found schema " + schema + " using AUTODETECT(elements with value) examination");
                }
            }

            if (schema == null) {
                schema = compareElementsAndAttributes(md, MODE_NEEDLE);
                if (schema != null) {
                    if (Log.isDebugEnabled(Geonet.SCHEMA_MANAGER))
                        Log.debug(Geonet.SCHEMA_MANAGER, "  => Found schema " + schema + " using AUTODETECT(elements) examination");
                }
            }

            if (schema == null) {
                schema = compareElementsAndAttributes(md, MODE_ROOT);
                if (schema != null) {
                    if (Log.isDebugEnabled(Geonet.SCHEMA_MANAGER))
                        Log.debug(Geonet.SCHEMA_MANAGER, "  => Found schema " + schema + " using AUTODETECT(elements with root) examination");
                }
            }

            if (schema == null) {
                schema = compareElementsAndAttributes(md, MODE_NAMESPACE);
                if (schema != null) {
                    if (Log.isDebugEnabled(Geonet.SCHEMA_MANAGER))
                        Log.debug(Geonet.SCHEMA_MANAGER, "  => Found schema " + schema + " using AUTODETECT(namespaces) examination");
                }
            }

            // -- If nothing has matched by this point choose defaultSchema supplied
            // -- as argument to this method as long as its reasonable
            if (schema == null && defaultSchema != null) {
                String defaultSchemaOrDependencySchema = checkNamespace(md, defaultSchema);
                if (defaultSchemaOrDependencySchema != null) {
                    Log.warning(Geonet.SCHEMA_MANAGER, "  Autodetecting schema failed for " + md.getName() + " in namespace " + md.getNamespace()
                        + ". Using default schema or one of its dependency: " + defaultSchemaOrDependencySchema);
                    schema = defaultSchemaOrDependencySchema;
                }
            }

            // -- if the default schema failed then throw an exception
            if (schema == null) {
                throw new NoSchemaMatchesException("Autodetecting schema failed for metadata record with root element " + md.getName() + " in namespace " + md.getNamespace() + ".");
            }

            return schema;
        } finally {
            afterRead();
        }
    }

    //--------------------------------------------------------------------------
    // -- Private methods
    //--------------------------------------------------------------------------

    /**
     * Check that schema is present and that the record can be assigned to it - namespace of
     * metadata schema is compared with prime namespace of metadata record.
     *
     * @param md     the metadata record being checked for prime namespace equality
     * @param schema the name of the metadata schema we want to test
     */
    private String checkNamespace(Element md, String schema) {
        String result = null;

        try {
            MetadataSchema mds = getSchema(schema);
            if (mds != null) {
                String primeNs = mds.getPrimeNS();
                if (Log.isDebugEnabled(Geonet.SCHEMA_MANAGER))
                    Log.debug(Geonet.SCHEMA_MANAGER, "  primeNs " + primeNs + " for schema " + schema);
                if (md.getNamespace().getURI().equals(primeNs)) {
                    result = schema;
                } else {
                    // Check if the metadata could match a schema dependency
                    // (If preferredSchema is an ISO profil a fragment or subtemplate
                    // may match ISO core schema and should not be rejected).
                    Schema sch = hmSchemas.get(schema);
                    List<Element> dependsList = sch.getDependElements();
                    for (Element depends : dependsList) {
                        if (Log.isDebugEnabled(Geonet.SCHEMA_MANAGER))
                            Log.debug(Geonet.SCHEMA_MANAGER, "  checkNamespace for dependency: " + depends.getText());
                        return checkNamespace(md, depends.getText());
                    }
                }
            }
        } catch (Exception e) {
            Log.warning(Geonet.SCHEMA_MANAGER, "Schema " + schema + " not registered?");
        }

        return result;
    }


    /**
     * Invoked just before reading, waits until reading is allowed.
     */
    private synchronized void beforeRead() {
        while (activeWriters > 0) {
            try {
                wait();
            } catch (InterruptedException iex) {
                // TODO what to do
            }
        }
        ++activeReaders;
    }

    /**
     * Invoked just after reading.
     */
    private synchronized void afterRead() {
        --activeReaders;
        notifyAll();
    }

    /**
     * Invoked just before writing, waits until writing is allowed.
     */
    private synchronized void beforeWrite() {
        while (activeReaders > 0 || activeWriters > 0) {
            try {
                wait();
            } catch (InterruptedException iex) {
            }
        }
        ++activeWriters;
    }

    /**
     * Invoked just after writing.
     */
    private synchronized void afterWrite() {
        --activeWriters;
        notifyAll();
    }

    /**
     * Really delete the schema from the schema information hash tables.
     *
     * @param name the metadata schema we want to delete - can only be a plugin schema
     * @throws Exception when something goes wrong
     */
    private void realDeletePluginSchema(String name, boolean doDependencies) throws Exception {

        Schema schema = hmSchemas.get(name);
        if (schema != null) {
            if (doDependencies) {
                List<String> dependsOnMe = getSchemasThatDependOnMe(name);
                if (dependsOnMe.size() > 0) {
                    String errStr = "Cannot remove schema " + name + " because the following schemas list it as a dependency: " + dependsOnMe;
                    Log.error(Geonet.SCHEMA_MANAGER, errStr);
                    throw new OperationAbortedEx(errStr);
                }
            }

            removeSchemaInfo(name);
        }
    }

    /**
     * Really add a plugin schema to the list of schemas registered here.
     *
     * @param name  the metadata schema we want to add
     * @param zipFs A filesystem (probably a ZipFileSystem) to copy files from.
     */
    private void realAddPluginSchema(ApplicationContext applicationContext, String name, FileSystem zipFs) throws Exception {
        Element schemaPluginCatRoot = getSchemaPluginCatalog();

        // -- create schema directory
        Path schemaDir = buildSchemaFolderPath(name);
        Files.createDirectories(schemaDir);

        try {
            ZipUtil.extract(zipFs, schemaDir);

            // -- add schema using the addSchema method
            processSchema(applicationContext, schemaDir, schemaPluginCatRoot);

            // -- check that dependent schemas are already loaded
            Schema schema = hmSchemas.get(name);
            checkDepends(name, schema.getDependElements());

            writeSchemaPluginCatalog(schemaPluginCatRoot);
        } catch (Exception e) {
            Log.error(Geonet.SCHEMA_MANAGER, e.getMessage(), e);
            hmSchemas.remove(name);
            IO.deleteFileOrDirectory(schemaDir);
            throw new OperationAbortedEx("Failed to add schema " + name + " : " + e.getMessage(), e);
        }
    }

    /**
     * Loads the metadata schema from disk and adds it to the pool.
     *
     * @param xmlSchemaFile        name of XML schema file (usually schema.xsd)
     * @param xmlSuggestFile       name of schema suggestions file
     * @param xmlSubstitutionsFile name schema substitutions file
     * @param xmlIdFile            name of XML file that identifies the schema
     * @param oasisCatFile         name of XML OASIS catalog file
     * @param conversionsFile      name of XML conversions file
     */
    private void addSchema(ApplicationContext applicationContext, Path schemaDir, Element schemaPluginCatRoot, Path xmlSchemaFile,
                           Path xmlSuggestFile, Path xmlSubstitutionsFile, Path xmlIdFile, Path oasisCatFile, Path conversionsFile)
        throws Exception {
        Path path = schemaDir;

        // -- add any oasis catalog files to Jeeves.XML_CATALOG_FILES system
        // -- property for resolver to pick up
        if (Files.exists(oasisCatFile)) {
            String catalogProp = System.getProperty(Constants.XML_CATALOG_FILES);
            if (catalogProp == null)
                catalogProp = ""; // shouldn't happen
            if (catalogProp.equals("")) {
                catalogProp = oasisCatFile.toString();
            } else {
                catalogProp = catalogProp + ";" + oasisCatFile;
            }
            System.setProperty(Constants.XML_CATALOG_FILES, catalogProp);
            Xml.resetResolver();
        }

        SchematronRepository schemaRepo = applicationContext.getBean(SchematronRepository.class);
        SchematronCriteriaGroupRepository criteriaGroupRepository = applicationContext.getBean(SchematronCriteriaGroupRepository.class);
        MetadataSchema mds = new SchemaLoader().load(xmlSchemaFile, xmlSubstitutionsFile, schemaRepo, criteriaGroupRepository);

        mds.setName(path.getFileName().toString());
        mds.setSchemaDir(path);
        mds.loadSchematronRules(basePath);

        if (mds.getSchemaPlugin() != null && mds.getSchemaPlugin().getCswTypeNames() != null) {
            hmSchemasTypenames.putAll(mds.getSchemaPlugin().getCswTypeNames());
        }

        // -- add cached xml files (schema codelists and label files)
        // -- as Jeeves XmlFile objects (they need not exist)

        final String schemaName = schemaDir.getFileName().toString();
        Path locBase = schemaDir.resolve("loc");
        Map<String, XmlFile> xfMap = new HashMap<String, XmlFile>();

        for (String fname : fnames) {
            Path filePath = path.resolve("loc").resolve(defaultLang).resolve(fname);
            if (Log.isDebugEnabled(Geonet.SCHEMA_MANAGER))
                Log.debug(Geonet.SCHEMA_MANAGER, "Searching for " + filePath);
            if (Files.exists(filePath)) {
                Element config = new Element("xml");
                config.setAttribute("name", schemaName);
                config.setAttribute("base", locBase.toUri().toString());
                config.setAttribute("file", fname);
                if (Log.isDebugEnabled(Geonet.SCHEMA_MANAGER))
                    Log.debug(Geonet.SCHEMA_MANAGER, "Adding XmlFile " + Xml.getString(config));
                XmlFile xf = new XmlFile(config, defaultLang, true);
                xfMap.put(fname, xf);
            } else {
                Log.warning(Geonet.SCHEMA_MANAGER, "Unable to load loc file: " + filePath);
            }
        }


        Pair<String, String> idInfo = extractIdInfo(xmlIdFile, schemaName);

        extractMetadata(mds, xmlIdFile);
        mds.setReadwriteUUID(extractReadWriteUuid(xmlIdFile));
        mds.setOperationFilters(extractOperationFilters(xmlIdFile));

        Log.debug(Geonet.SCHEMA_MANAGER, "  UUID is read/write mode: " + mds.isReadwriteUUID());

        putSchemaInfo(
            schemaName,
            idInfo.one(), // uuid of schema
            idInfo.two(), // version of schema
            mds,
            path,
            new SchemaSuggestions(xmlSuggestFile),
            extractADElements(xmlIdFile),
            xfMap,
            true, // all schemas are plugin schemas now
            extractSchemaLocation(xmlIdFile),
            extractConvElements(conversionsFile),
            extractDepends(xmlIdFile));

        if (Log.isDebugEnabled(Geonet.SCHEMA_MANAGER)) {
            Log.debug(Geonet.SCHEMA_MANAGER, "Property " + Constants.XML_CATALOG_FILES + " is " + System.getProperty(Constants.XML_CATALOG_FILES));
        }

        // -- Add entry for presentation xslt to schemaPlugins catalog
        // -- if this schema is a plugin schema
        int baseNrInt = getHighestSchemaPluginCatalogId(schemaName, schemaPluginCatRoot);
        if (baseNrInt == 0)
            baseNrInt = numberOfCoreSchemasAdded;
        if (baseNrInt != -1) {
            createUriEntryInSchemaPluginCatalog(schemaName, baseNrInt, schemaPluginCatRoot);
        }

        // -- copy schema.xsd and schema directory from schema to
        // -- <web_app_dir>/xml/schemas/<schema_name>
        copySchemaXSDsToWebApp(schemaName, path);

    }

    /**
     * Reload a schema.
     *
     * Compile validation rules (conversion from SCH to XSL).
     *
     * @param schemaIdentifier The schema identifier.
     */
    public void reloadSchema(String schemaIdentifier) {
        MetadataSchema metadataSchema = this.getSchema(schemaIdentifier);
        metadataSchema.loadSchematronRules(basePath);
    }


    /**
     * Read the elements from the schema plugins catalog for use by other methods.
     */
    private Element getSchemaPluginCatalog() throws Exception {
        // -- open schemaPlugins catalog, get children named uri
        return Xml.loadFile(schemaPluginsCat);
    }

    /**
     * Read the empty template for the schema plugins oasis catalog.
     */
    private Element getSchemaPluginCatalogTemplate() throws Exception {
        return Xml.loadFile(basePath.resolve("WEB-INF").resolve(Geonet.File.SCHEMA_PLUGINS_CATALOG));
    }

    /**
     * Build a path to the schema plugin folder
     *
     * @param name the name of the schema to use
     */
    private Path buildSchemaFolderPath(String name) {
        return schemaPluginsDir.resolve(name);
    }

    /**
     * Deletes the presentation xslt from the schemaplugin oasis catalog.
     *
     * @param root the list of elements from the schemaplugin-uri-catalog
     * @param name the name of the schema to use
     */
    private Element deleteSchemaFromPluginCatalog(String name, Element root) throws Exception {
        @SuppressWarnings(value = "unchecked")
        List<Content> contents = root.getContent();

        Path ourUri = buildSchemaFolderPath(name);

        int index = -1;
        for (Content content : contents) {
            Element uri;

            if (content instanceof Element) uri = (Element) content;
            else continue; // skip this

            if (!uri.getName().equals("uri") || !uri.getNamespace().equals(Namespaces.OASIS_CATALOG)) {
                if (Log.isDebugEnabled(Geonet.SCHEMA_MANAGER))
                    Log.debug(Geonet.SCHEMA_MANAGER, "Skipping element " + uri.getQualifiedName() + ":" + uri.getNamespace());
                continue;
            }

            // -- if already mapped then exit
            if (uri.getAttributeValue("uri").equals(ourUri.toString())) index = root.indexOf(uri);
        }

        if (index != -1) root.removeContent(index);
        return root;
    }

    /**
     * Gets the next available blank number that can be used to map the presentation xslt used by
     * the schema (see metadata-utils.xsl and Geonet.File.METADATA_MAX_BLANKS). If the presentation
     * xslt is already mapped then we exit early with return value -1.
     *
     * @param root the list of elements from the schemaplugin-uri-catalog
     * @param name the name of the schema to use
     */
    private int getHighestSchemaPluginCatalogId(String name, Element root) throws Exception {
        @SuppressWarnings("unchecked")
        List<Content> contents = root.getContent();

        String baseBlank = Geonet.File.METADATA_BASEBLANK;
        Path ourUri = buildSchemaFolderPath(name);

        for (Content content : contents) {
            Element uri = null;

            if (content instanceof Element) uri = (Element) content;
            else continue; // skip this

            if (!uri.getName().equals("rewriteURI") || !uri.getNamespace().equals(Namespaces.OASIS_CATALOG)) {
                if (Log.isDebugEnabled(Geonet.SCHEMA_MANAGER))
                    Log.debug(Geonet.SCHEMA_MANAGER, "Skipping element " + uri.getQualifiedName() + ":" + uri.getNamespace());
                continue;
            }

            // -- if already mapped then exit
            if (uri.getAttributeValue("rewritePrefix").equals(ourUri.toString())) return -1;

            String nameAttr = uri.getAttributeValue("uriStartString");
            if (nameAttr.startsWith(Geonet.File.METADATA_BLANK)) {
                if (nameAttr.compareTo(baseBlank) > 0) baseBlank = nameAttr;
            }
        }

        // -- get highest appropriate number
        String baseNr = baseBlank.replace(Geonet.File.METADATA_BLANK, "");
        int baseNrInt = 0;
        try {
            baseNrInt = Integer.parseInt(baseNr);
        } catch (NumberFormatException nfe) {
            Log.error(Geonet.SCHEMA_MANAGER, "Cannot decode blank number from " + baseBlank, nfe);
            throw new IllegalArgumentException("Cannot decode blank number from " + baseBlank);
        }
        return baseNrInt;
    }

    /**
     * Creates a uri remap entry in the schema plugins catalog for the presentation xslt used by the
     * schema.
     *
     * @param name      the name of the schema to use
     * @param baseNrInt the number of the plugin schema to map the presentation xslt to
     * @param root      the list of elements from the schemaplugin-uri-catalog
     */
    private void createUriEntryInSchemaPluginCatalog(String name, int baseNrInt, Element root) throws Exception {

        baseNrInt = baseNrInt + 1;

        Element newBlank = new Element("rewriteURI", Namespaces.OASIS_CATALOG);
        //Element newBlank = new Element("uri", Geonet.OASIS_CATALOG_NAMESPACE);
        if (baseNrInt <= Geonet.File.METADATA_MAX_BLANKS) {
            String zero = "";
            if (baseNrInt < 10) zero = "0";
            newBlank.setAttribute("uriStartString", Geonet.File.METADATA_BLANK + zero + baseNrInt);
            final Path schemaFolderPath = buildSchemaFolderPath(name);
            try {
                newBlank.setAttribute("rewritePrefix", schemaFolderPath.toFile().toString());
            } catch (UnsupportedOperationException e) {
                newBlank.setAttribute("rewritePrefix", schemaFolderPath.toUri().toString());
            }
        } else {
            throw new IllegalArgumentException("Exceeded maximum number of plugin schemas " + Geonet.File.METADATA_MAX_BLANKS);
        }

        // -- write out new schemaPlugins catalog and re-init the resolvers that
        // -- use this catalog

        root.addContent(newBlank);
    }


    /**
     * Writes the schema plugin catalog out.
     *
     * @param root the list of elements from the schemaplugin-uri-catalog
     */
    private void writeSchemaPluginCatalog(Element root) throws Exception {
        if (createOrUpdateSchemaCatalog) {
            NioPathAwareCatalogResolver.addRewriteDirective(new SchemaPluginUrlRewrite(root));
            try (OutputStream out = Files.newOutputStream(schemaPluginsCat)) {
                Xml.writeResponse(new Document((Element) root.detach()), out);
                Xml.resetResolver();
                Xml.clearTransformerFactoryStylesheetCache();
            }
        }
    }

    /**
     * Puts information into the schema information hashtables.
     *
     * @param id             schema id (uuid)
     * @param version        schema version
     * @param name           schema name
     * @param mds            MetadataSchema object with details of XML schema info
     * @param schemaDir      path name of schema directory
     * @param sugg           SchemaSuggestions object
     * @param adElems        List of autodetect XML elements (as JDOM Elements)
     * @param xfMap          Map containing XML localized info files (as Jeeves XmlFiles)
     * @param isPlugin       true if schema is a plugin schema
     * @param schemaLocation namespaces and URLs of their xsds
     * @param convElems      List of elements in conversion file
     * @param dependElems    List of depend XML elements (as JDOM Elements)
     */
    private void putSchemaInfo(String name, String id, String version, MetadataSchema mds, Path schemaDir,
                               SchemaSuggestions sugg, List<Element> adElems, Map<String, XmlFile> xfMap,
                               boolean isPlugin, String schemaLocation, List<Element> convElems, List<Element> dependElems) {

        Schema schema = new Schema();

        schema.setId(id);
        schema.setVersion(version);
        schema.setMetadataSchema(mds);
        schema.setDir(schemaDir);
        schema.setSuggestions(sugg);
        schema.setAutodetectElements(adElems);
        schema.setInfo(xfMap);
        schema.setPluginSchema(isPlugin);
        schema.setSchemaLocation(schemaLocation);
        schema.setConversionElements(convElems);
        schema.setDependElements(dependElems);

        hmSchemas.put(name, schema);
    }

    /**
     * Deletes information from the schema information hashtables, the schema directory itself and
     * the mapping for the schema presentation xslt from the schemaplugins oasis catalog.
     *
     * @param name schema name
     */
    private void removeSchemaInfo(String name) throws Exception {
        Schema schema = hmSchemas.get(name);

        removeSchemaDir(schema.getDir(), name);
        hmSchemas.remove(name);

        Element schemaPluginCatRoot = getSchemaPluginCatalog();
        schemaPluginCatRoot = deleteSchemaFromPluginCatalog(name, schemaPluginCatRoot);
        writeSchemaPluginCatalog(schemaPluginCatRoot);

    }

    /**
     * Deletes information in the schema directory and removes published schemas from the webapp.
     *
     * @param schemaDir schema directory to remove
     * @param name      of schema being removed
     */
    private void removeSchemaDir(Path schemaDir, String name) {
        // -- FIXME: get schema directory and zip it up into the deleted metadata
        // -- directory?

        if (Log.isDebugEnabled(Geonet.SCHEMA_MANAGER)) {
            Log.debug(Geonet.SCHEMA_MANAGER, "Removing schema directory " + schemaDir);
        }
        deleteDir(schemaDir);

        Path pubSchemaDir = schemaPublicationDir.resolve(Geonet.Path.SCHEMAS).resolve(name);
        if (Log.isDebugEnabled(Geonet.SCHEMA_MANAGER)) {
            Log.debug(Geonet.SCHEMA_MANAGER, "Removing published schemas directory " + pubSchemaDir);
        }
        deleteDir(pubSchemaDir);
    }

    /**
     * Processes schemas in either web/xml/schemas or schema plugin directory.
     *
     * @param schemasDir path name of directory containing schemas
     */
    private void processSchema(ApplicationContext applicationContext, Path schemasDir, Element schemaPluginCatRoot) throws OperationAbortedEx {

        Path schemaFile = schemasDir.resolve(Geonet.File.SCHEMA);
        Path suggestFile = schemasDir.resolve(Geonet.File.SCHEMA_SUGGESTIONS);
        Path substitutesFile = schemasDir.resolve(Geonet.File.SCHEMA_SUBSTITUTES);
        Path idFile = schemasDir.resolve(Geonet.File.SCHEMA_ID);
        Path oasisCatFile = schemasDir.resolve(Geonet.File.SCHEMA_OASIS);
        Path conversionsFile = schemasDir.resolve(Geonet.File.SCHEMA_CONVERSIONS);

        if (!Files.exists(idFile)) {
            Log.error(Geonet.SCHEMA_MANAGER, "    Skipping : " + schemasDir.getFileName() + " as it doesn't have " +
                Geonet.File.SCHEMA_ID);
            return;
        }

        Log.info(Geonet.SCHEMA_MANAGER, "    Adding xml schema : " + schemasDir.getFileName());

        String stage = "";
        try {
            // validate the schema-ident file before reading it
            stage = "reading schema-ident file " + idFile;
            Element root = Xml.loadFile(idFile);
            stage = "validating schema-ident file " + idFile;
            Xml.validate(root);

            final String schemaName = schemasDir.getFileName().toString();
            if (hmSchemas.containsKey(schemaName)) { // exists so ignore it
                Log.error(Geonet.SCHEMA_MANAGER, "Schema " + schemaName + " already exists - cannot add!");
            } else {
                stage = "adding the schema information";
                addSchema(applicationContext, schemasDir, schemaPluginCatRoot, schemaFile, suggestFile, substitutesFile,
                    idFile, oasisCatFile, conversionsFile);
                ResolverWrapper.createResolverForSchema(schemasDir.getFileName().toString(), oasisCatFile);
            }
        } catch (Exception e) {
            String errStr = "Failed whilst " + stage + ". Exception message if any is " + e.getMessage();
            Log.error(Geonet.SCHEMA_MANAGER, errStr, e);
            throw new OperationAbortedEx(errStr, e);
        }

    }

    /**
     * Check dependencies for all schemas - remove those that fail.
     */
    private void checkDependencies(Element schemaPluginCatRoot) throws Exception {
        List<String> removes = new ArrayList<String>();

        // process each schema to see whether its dependencies are present
        for (String schemaName : hmSchemas.keySet()) {
            Schema schema = hmSchemas.get(schemaName);
            try {
                checkDepends(schemaName, schema.getDependElements());
            } catch (Exception e) {
                Log.error(Geonet.SCHEMA_MANAGER, "check dependencies failed: " + e.getMessage());
                // add the schema to list for removal
                removes.add(schemaName);
            }
        }

        // now remove any that failed the dependency test
        for (String removeSchema : removes) {
            hmSchemas.remove(removeSchema);
            deleteSchemaFromPluginCatalog(removeSchema, schemaPluginCatRoot);
        }

    }

    private void checkAppSupported(Element schemaPluginCatRoot) throws Exception {
        List<String> removes = new ArrayList<String>();

        final SystemInfo systemInfo = ApplicationContextHolder.get().getBean(SystemInfo.class);

        String version = systemInfo.getVersion();
        Version appVersion = Version.parseVersionNumber(version);

        // process each schema to see whether its dependencies are present
        for (String schemaName : hmSchemas.keySet()) {
            Schema schema = hmSchemas.get(schemaName);
            String minorAppVersionSupported = schema.getMetadataSchema().getAppMinorVersionSupported();

            Version schemaMinorAppVersion = Version.parseVersionNumber(minorAppVersionSupported);

            if (appVersion.compareTo(schemaMinorAppVersion) < 0) {
                Log.error(Geonet.SCHEMA_MANAGER, "Schema " + schemaName +
                    " requires min Geonetwork version: " + minorAppVersionSupported + ", current is: " +
                    version + ". Skip load schema.");
                removes.add(schemaName);
                continue;
            }

            String majorAppVersionSupported = schema.getMetadataSchema().getAppMajorVersionSupported();
            if (StringUtils.isNotEmpty(majorAppVersionSupported)) {
                Version schemaMajorAppVersion = Version.parseVersionNumber(majorAppVersionSupported);

                if (appVersion.compareTo(schemaMajorAppVersion) > 0) {
                    Log.error(Geonet.SCHEMA_MANAGER, "Schema " + schemaName +
                        " requires max Geonetwork version: " + majorAppVersionSupported + ", current is: " +
                        version + ". Skip load schema.");
                    removes.add(schemaName);
                    continue;
                }
            }

        }

        // now remove any that failed the app version test
        for (String removeSchema : removes) {
            hmSchemas.remove(removeSchema);
            deleteSchemaFromPluginCatalog(removeSchema, schemaPluginCatRoot);
        }

    }



    /**
     * Get list of schemas that depend on supplied schema name.
     *
     * @param schemaName Schema being checked
     * @return List of schemas that depend on schemaName.
     */
    public List<String> getSchemasThatDependOnMe(String schemaName) {

        List<String> myDepends = new ArrayList<String>();

        // process each schema to see whether its dependencies are present
        for (String schemaNameToTest : hmSchemas.keySet()) {
            if (schemaNameToTest.equals(schemaName)) continue;

            Schema schema = hmSchemas.get(schemaNameToTest);
            List<Element> dependsList = schema.getDependElements();
            for (Element depends : dependsList) {
                if (depends.getText().equals(schemaName)) {
                    myDepends.add(schemaNameToTest);
                }
            }
        }

        return myDepends;
    }

    /**
     * Check schema dependencies (depend elements).
     *
     * @param thisSchema  Schema being checked
     * @param dependsList List of depend elements for schema.
     */
    private void checkDepends(String thisSchema, List<Element> dependsList) throws Exception {
        // process each dependency to see whether it is present
        for (Element depends : dependsList) {
            String schema = depends.getText();
            if (schema.length() > 0) {
                if (!hmSchemas.containsKey(schema)) {
                    throw new IllegalArgumentException("Schema " + thisSchema + " depends on " + schema + ", but that schema is not loaded");
                }
            }
        }
    }

    /**
     * Extract schema dependencies (depend elements).
     *
     * @param xmlIdFile name of schema XML identification file
     * @return depends elements as a List
     */
    @SuppressWarnings("unchecked")
    private List<Element> extractDepends(Path xmlIdFile) throws Exception {
        Element root = Xml.loadFile(xmlIdFile);

        // get list of depends elements from schema-ident.xml
        List<Element> dependsList = root.getChildren("depends", GEONET_SCHEMA_NS);
        if (dependsList.size() == 0) {
            dependsList = root.getChildren("depends", GEONET_SCHEMA_PREFIX_NS);
        }
        return dependsList;
    }

    /**
     * true if schema requires to synch the uuid column schema info with the uuid in the metadata
     * record (updated on editing or in UFO).
     */
    private boolean extractReadWriteUuid(Path xmlIdFile) throws Exception {
        Element root = Xml.loadFile(xmlIdFile);

        String id = root.getChildText("readwriteUuid", GEONET_SCHEMA_NS);
        if (id == null) {
            return false;
        } else {
            if ("true".equals(id)) {
                return true;
            }
        }
        return false;
    }


    /**
     * Extract metadata schema informations (eg. title, description, url).
     */
    private void extractMetadata(MetadataSchema mds, Path xmlIdFile) throws JDOMException, NoSuchFileException {
        Element root = Xml.loadFile(xmlIdFile);
        mds.setStandardUrl(root.getChildText("standardUrl", GEONET_SCHEMA_NS));
        mds.setTitles(getSchemaIdentMultilingualProperty(root, "title"));
        mds.setDescriptions(getSchemaIdentMultilingualProperty(root, "description"));

        mds.setVersion(root.getChildText("version", GEONET_SCHEMA_NS));
        mds.setAppMinorVersionSupported(root.getChildText("appMinorVersionSupported", GEONET_SCHEMA_NS));
        mds.setAppMajorVersionSupported(root.getChildText("appMajorVersionSupported", GEONET_SCHEMA_NS));
        mds.setDependsOn(root.getChildText("depends", GEONET_SCHEMA_NS));
    }

    private Map<String, String> getSchemaIdentMultilingualProperty(Element root, String propName) {
        Map<String, String> props = new HashMap<>();
        root.getChildren(propName, GEONET_SCHEMA_NS).forEach(o -> {
            if (o instanceof Element) {
                Element e = (Element) o;
                String lang = e.getAttributeValue("lang", Namespaces.XML);
                if (lang != null) {
                    props.put(lang, e.getTextNormalize());
                }
            }
        });
        return props;
    }

    /**
     * true if schema requires to synch the uuid column schema info with the uuid in the metadata
     * record (updated on editing or in UFO).
     */
    private Map<String, Pair<String, Element>> extractOperationFilters(Path xmlIdFile) throws Exception {
        Element root = Xml.loadFile(xmlIdFile);
        Element filters = root.getChild("filters", GEONET_SCHEMA_NS);
        Map<String, Pair<String, Element>> filterRules =
            new HashMap<String, Pair<String, Element>>();
        if (filters == null) {
            return filterRules;
        } else {
            for (Object rule : filters.getChildren("filter", GEONET_SCHEMA_NS)) {
                if (rule instanceof Element) {
                    Element ruleElement = (Element) rule;
                    String xpath = ruleElement.getAttributeValue("xpath");
                    String ifNotOperation = ruleElement.getAttributeValue("ifNotOperation");
                    Element markedElement = ruleElement.getChild("keepMarkedElement", GEONET_SCHEMA_NS);
                    if (StringUtils.isNotBlank(ifNotOperation) &&
                        StringUtils.isNotBlank(xpath)) {
                        filterRules.put(ifNotOperation, Pair.read(xpath, markedElement));
                    }
                }
            }
        }
        return filterRules;
    }

    /**
     * Extract schema version and uuid info from identification file and compare specified name with
     * name in identification file.
     *
     * @param xmlIdFile name of schema XML identification file
     */
    private Pair<String, String> extractIdInfo(Path xmlIdFile, String name) throws Exception {
        // FIXME: should be validating parser
        Element root = Xml.loadFile(xmlIdFile);

        Element id = root.getChild("id", GEONET_SCHEMA_NS);
        if (id == null) id = root.getChild("id", GEONET_SCHEMA_PREFIX_NS);

        Element version = root.getChild("version", GEONET_SCHEMA_NS);
        if (version == null) version = root.getChild("version", GEONET_SCHEMA_PREFIX_NS);

        Element idName = root.getChild("name", GEONET_SCHEMA_NS);
        if (idName == null) idName = root.getChild("name", GEONET_SCHEMA_PREFIX_NS);

        if (!idName.getText().equals(name))
            throw new IllegalArgumentException("Schema name supplied " + name + " does not match the name of the schema in the schema-id.xml file " + idName.getText());

        return Pair.read(id.getText(), version.getText());
    }

    /**
     * Extracts schema autodetect info from identification file.
     *
     * @param xmlIdFile name of schema XML identification file
     */
    private List<Element> extractADElements(Path xmlIdFile) throws Exception {
        Element root = Xml.loadFile(xmlIdFile);
        Element autodetect = root.getChild("autodetect", GEONET_SCHEMA_NS);
        if (autodetect == null) autodetect = root.getChild("autodetect", GEONET_SCHEMA_PREFIX_NS);
        @SuppressWarnings("unchecked")
        List<Element> children = autodetect.getChildren();
        return children;
    }

    /**
     * Extract conversion elements from conversions file.
     *
     * @param xmlConvFile name of schema XML conversions file
     */
    private List<Element> extractConvElements(Path xmlConvFile) throws Exception {
        if (!Files.exists(xmlConvFile)) {
            if (Log.isDebugEnabled(Geonet.SCHEMA_MANAGER))
                Log.debug(Geonet.SCHEMA_MANAGER, "Schema conversions file not present");
            return new ArrayList<Element>();
        } else {
            Element root = Xml.loadFile(xmlConvFile);
            if (root.getName() != "conversions")
                throw new IllegalArgumentException("Schema conversions file " + xmlConvFile + " is invalid, no <conversions> root element");
            @SuppressWarnings("unchecked")
            List<Element> result = root.getChildren();
            return result;
        }
    }

    /**
     * Extract schemaLocation info from identification file.
     *
     * @param xmlIdFile name of schema XML identification file
     */
    private String extractSchemaLocation(Path xmlIdFile) throws Exception {
        Element root = Xml.loadFile(xmlIdFile);
        Element schemaLocElem = root.getChild("schemaLocation", GEONET_SCHEMA_NS);
        if (schemaLocElem == null)
            schemaLocElem = root.getChild("schemaLocation", GEONET_SCHEMA_PREFIX_NS);
        return schemaLocElem.getTextNormalize();
    }

    /**
     * Search all available schemas for one which contains the element(s) or attributes specified in
     * the autodetect info.
     *
     * @param md the XML record whose schema we are trying to find
     */
    private String compareElementsAndAttributes(Element md, int mode) throws SchemaMatchConflictException {
        String returnVal = null;
        Set<String> allSchemas = getSchemas();
        List<String> matches = new ArrayList<>();

        if (Log.isDebugEnabled(Geonet.SCHEMA_MANAGER))
            Log.debug(Geonet.SCHEMA_MANAGER, "Schema autodetection starting on " + md.getName() + " (Namespace: " + md.getNamespace() + ") using mode: " + mode + "...");

        for (String schemaName : allSchemas) {
            if (Log.isDebugEnabled(Geonet.SCHEMA_MANAGER))
                Log.debug(Geonet.SCHEMA_MANAGER, "	Doing schema " + schemaName);
            Schema schema = hmSchemas.get(schemaName);
            List<Element> adElems = schema.getAutodetectElements();

            for (Element elem : adElems) {
                if (Log.isDebugEnabled(Geonet.SCHEMA_MANAGER))
                    Log.debug(Geonet.SCHEMA_MANAGER, "		Checking autodetect element " + Xml.getString(elem) + " with name " + elem.getName());

                @SuppressWarnings("unchecked")
                List<Element> elemKids = elem.getChildren();
                boolean match = false;

                Attribute type = elem.getAttribute("type");

                // --- try and find the attribute and value in md
                if (mode == MODE_ATTRIBUTEWITHVALUE && elem.getName() == "attributes") {
                    @SuppressWarnings("unchecked")
                    List<Attribute> atts = elem.getAttributes();
                    for (Attribute searchAtt : atts) {
                        if (Log.isDebugEnabled(Geonet.SCHEMA_MANAGER))
                            Log.debug(Geonet.SCHEMA_MANAGER, "				Finding attribute " + searchAtt.toString());

                        if (isMatchingAttributeInMetadata(searchAtt, md)) {
                            match = true;
                        } else {
                            match = false;
                            break;
                        }
                    }

                    // --- try and find the namespace in md
                } else if (mode == MODE_NAMESPACE && elem.getName().equals("namespaces")) {
                    @SuppressWarnings("unchecked")
                    List<Namespace> nss = elem.getAdditionalNamespaces();
                    for (Namespace ns : nss) {
                        if (Log.isDebugEnabled(Geonet.SCHEMA_MANAGER))
                            Log.debug(Geonet.SCHEMA_MANAGER, "				Finding namespace " + ns.toString());

                        if (isMatchingNamespaceInMetadata(ns, md)) {
                            match = true;
                        } else {
                            match = false;
                            break;
                        }
                    }
                } else {
                    for (Element kid : elemKids) {

                        // --- is the kid the same as the root of the md
                        if (mode == MODE_ROOT && type != null && "root".equals(type.getValue())) {
                            if (Log.isDebugEnabled(Geonet.SCHEMA_MANAGER))
                                Log.debug(Geonet.SCHEMA_MANAGER, "				Comparing " + Xml.getString(kid) + " with " + md.getName() + " with namespace " + md.getNamespace() + " : " + (kid.getName().equals(md.getName()) && kid.getNamespace().equals(md.getNamespace())));
                            if (kid.getName().equals(md.getName()) &&
                                kid.getNamespace().equals(md.getNamespace())) {
                                match = true;
                                break;
                            } else {
                                match = false;
                            }
                            // --- try and find the kid in the md (kid only, not value)
                        } else if (mode == MODE_NEEDLE && type != null && "search".equals(type.getValue())) {
                            if (Log.isDebugEnabled(Geonet.SCHEMA_MANAGER))
                                Log.debug(Geonet.SCHEMA_MANAGER, "				Comparing " + Xml.getString(kid) + " with " + md.getName() + " with namespace " + md.getNamespace() + " : " + (kid.getName().equals(md.getName()) && kid.getNamespace().equals(md.getNamespace())));

                            if (isMatchingElementInMetadata(kid, md, false)) {
                                match = true;
                            } else {
                                match = false;
                                break;
                            }
                            // --- try and find the kid in the md (kid + value)
                        } else if (mode == MODE_NEEDLEWITHVALUE) {
                            if (isMatchingElementInMetadata(kid, md, true)) {
                                match = true;
                            } else {
                                match = false;
                                break;
                            }
                        }
                    }
                }
                if (match && (!matches.contains(schemaName))) matches.add(schemaName);
            }
        }

        if (matches.size() > 1) {
            throw new SchemaMatchConflictException("Metadata record with " + md.getName() + " (Namespace " + md.getNamespace() + " matches more than one schema - namely: " + matches.toString() + " - during schema autodetection mode " + mode);
        } else if (matches.size() == 1) {
            returnVal = matches.get(0);
        }

        return returnVal;
    }

    /**
     * This method searches an entire metadata file for an attribute that matches the "needle"
     * metadata attribute arg - A matching attribute has the same name and value.
     *
     * @param needle   the XML attribute we are trying to find
     * @param haystack the XML metadata record we are searching
     */
    private boolean isMatchingAttributeInMetadata(Attribute needle, Element haystack) {
        boolean returnVal = false;
        @SuppressWarnings("unchecked")
        Iterator<Element> haystackIterator = haystack.getDescendants(new ElementFilter());

        if (Log.isDebugEnabled(Geonet.SCHEMA_MANAGER))
            Log.debug(Geonet.SCHEMA_MANAGER, "Matching " + needle.toString());

        while (haystackIterator.hasNext()) {
            Element tempElement = haystackIterator.next();
            Attribute tempAtt = tempElement.getAttribute(needle.getName());
            if (tempAtt.equals(needle)) {
                returnVal = true;
                break;
            }
        }
        return returnVal;
    }

    /**
     * This method searches all elements of a metadata for a namespace that matches the "needle"
     * namespace arg. (Note: matching namespaces have the same URI, prefix is ignored).
     *
     * @param needle   the XML namespace we are trying to find
     * @param haystack the XML metadata record we are searching
     */
    private boolean isMatchingNamespaceInMetadata(Namespace needle, Element haystack) {
        if (Log.isDebugEnabled(Geonet.SCHEMA_MANAGER))
            Log.debug(Geonet.SCHEMA_MANAGER, "Matching " + needle.toString());

        if (checkNamespacesOnElement(needle, haystack)) return true;

        @SuppressWarnings("unchecked")
        Iterator<Element> haystackIterator = haystack.getDescendants(new ElementFilter());
        while (haystackIterator.hasNext()) {
            Element tempElement = haystackIterator.next();
            if (checkNamespacesOnElement(needle, tempElement)) return true;
        }

        return false;
    }

    /**
     * This method searches an elements and its namespaces for a match with an input namespace.
     *
     * @param ns   the XML namespace we are trying to find
     * @param elem the XML metadata element whose namespaces are to be searched
     */
    private boolean checkNamespacesOnElement(Namespace ns, Element elem) {
        if (elem.getNamespace().equals(ns)) return true;
        @SuppressWarnings("unchecked")
        List<Namespace> nss = elem.getAdditionalNamespaces();
        for (Namespace ans : nss) {
            if (ans.equals(ns)) return true;
        }
        return false;
    }

    /**
     * This method searches an entire metadata file for an element that matches the "needle"
     * metadata element arg - A matching element has the same name, namespace and value.
     *
     * @param needle     the XML element we are trying to find
     * @param haystack   the XML metadata record we are searching
     * @param checkValue compare the value of the needle with the value of the element we find in
     *                   the md
     */
    private boolean isMatchingElementInMetadata(Element needle, Element haystack, boolean checkValue) {
        boolean returnVal = false;
        @SuppressWarnings("unchecked")
        Iterator<Element> haystackIterator = haystack.getDescendants(new ElementFilter());

        String needleName = needle.getName();
        Namespace needleNS = needle.getNamespace();
        if (Log.isDebugEnabled(Geonet.SCHEMA_MANAGER))
            Log.debug(Geonet.SCHEMA_MANAGER, "Matching " + Xml.getString(needle));

        while (haystackIterator.hasNext()) {
            Element tempElement = haystackIterator.next();

            if (tempElement.getName().equals(needleName) && tempElement.getNamespace().equals(needleNS)) {
                if (checkValue) {
                    if (Log.isDebugEnabled(Geonet.SCHEMA_MANAGER))
                        Log.debug(Geonet.SCHEMA_MANAGER, "  Searching value for element: " + tempElement.getName());

                    String needleVal = StringUtils.deleteWhitespace(needle.getValue());
                    String tempVal = StringUtils.deleteWhitespace(tempElement.getValue());
                    returnVal = Pattern.matches(needleVal, tempVal);
                    if (Log.isDebugEnabled(Geonet.SCHEMA_MANAGER)) {
                        Log.debug(Geonet.SCHEMA_MANAGER, "    Pattern " + needleVal + " applied to value " + tempVal + " match: " + returnVal);
                    }
                    if (returnVal) {
                        break;
                    }
                } else {
                    returnVal = true;
                    break;
                }
            }
        }
        return returnVal;
    }

    /**
     * This method deletes all the files and directories inside another the schema dir and then the
     * schema dir itself.
     *
     * @param dir the dir whose contents are to be deleted
     */
    private void deleteDir(Path dir) {
        try {
            IO.deleteFileOrDirectory(dir);
        } catch (IOException e) {
            Log.warning(Geonet.SCHEMA_MANAGER, "Unable to delete directory: " + dir);
        }
    }

    /**
     * Create a URL that can be used to point to a schema XSD delivered by GeoNetwork.
     *
     * @param context    the ServiceContext used to get setting manager and appPath
     * @param schemaName the name of the schema
     */
    private String getSchemaUrl(ServiceContext context, String schemaName) {
        SettingInfo si = context.getBean(SettingInfo.class);

        String relativePath = Geonet.Path.SCHEMAS + schemaName + "/schema.xsd";
        return si.getSiteUrl() + context.getBaseUrl() + "/" + relativePath;
    }

    public String getDefaultSchema() {
        return defaultSchema;
    }

    /**
     * Copy the schema.xsd file and the schema directory from the schema plugin directory to the
     * webapp.
     *
     * @param name            the name of the schema
     * @param schemaPluginDir the directory containing the schema plugin
     */
    private void copySchemaXSDsToWebApp(String name, Path schemaPluginDir) throws Exception {
        Path schemasDir = schemaPublicationDir.resolve(Geonet.Path.SCHEMAS);
        Files.createDirectories(schemasDir);

        Path webAppDirSchemaXSD = schemasDir.resolve(name);
        IO.deleteFileOrDirectory(webAppDirSchemaXSD, true);
        if (!Files.exists(webAppDirSchemaXSD)) {
            Files.createDirectories(webAppDirSchemaXSD);
        }

        // copy all XSDs from schema plugin dir to webapp schema dir

        DirectoryStream.Filter<? super Path> xsdFilter = new DirectoryStream.Filter<Path>() {
            @Override
            public boolean accept(Path entry) throws IOException {
                return entry.getFileName().toString().toLowerCase().endsWith(".xsd");
            }
        };
        try (DirectoryStream<Path> schemaplugins = Files.newDirectoryStream(schemaPluginDir, xsdFilter)) {
            boolean missingXsdFiles = true;
            for (Path schemaplugin : schemaplugins) {
                IO.copyDirectoryOrFile(schemaplugin, webAppDirSchemaXSD.resolve(schemaplugin), false);
                missingXsdFiles = false;
            }

            if (missingXsdFiles) {
                Log.error(Geonet.SCHEMA_MANAGER, "Schema " + name + " does not have any XSD files!");
            }
        }

        Path fileSchemaDir = schemaPluginDir.resolve("schema");
        if (Files.exists(fileSchemaDir)) {
            IO.copyDirectoryOrFile(fileSchemaDir, webAppDirSchemaXSD.resolve("schema"), false);
        }

    }


    /**
     * Return the list of typenames declared in all schema plugins.
     */
    public Map<String, Namespace> getHmSchemasTypenames() {
        return hmSchemasTypenames;
    }

    /**
     * Return the list of namespace URI of all typenames declared in all schema plugins.
     */
    public List<String> getListOfOutputSchemaURI() {
        Iterator<String> iterator = hmSchemasTypenames.keySet().iterator();
        List<String> listOfSchemaURI = new ArrayList<>();
        while (iterator.hasNext()) {
            String typeLocalName = iterator.next();
            Namespace ns = hmSchemasTypenames.get(typeLocalName);
            listOfSchemaURI.add(ns.getURI());
        }
        return listOfSchemaURI;
    }

    /**
     * Return the list of typenames (with prefix) declared in all schema plugin.
     */
    public List<String> getListOfTypeNames() {
        Iterator<String> iterator = hmSchemasTypenames.keySet().iterator();
        List<String> listOfTypenames = new ArrayList<>();
        while (iterator.hasNext()) {
            String typeName = iterator.next();
            Namespace ns = hmSchemasTypenames.get(typeName);
            listOfTypenames.add(typeName);
        }
        return listOfTypenames;
    }
}
