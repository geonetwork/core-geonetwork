package org.fao.geonet;

import com.google.common.collect.Lists;
import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import com.vividsolutions.jts.geom.MultiPolygon;
import jeeves.server.ServiceConfig;
import org.fao.geonet.domain.Source;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.GeonetworkDataDirectory;
import org.fao.geonet.kernel.SchemaManager;
import org.fao.geonet.kernel.schema.MetadataSchema;
import org.fao.geonet.kernel.search.LuceneConfig;
import org.fao.geonet.kernel.search.SearchManager;
import org.fao.geonet.kernel.search.index.DirectoryFactory;
import org.fao.geonet.kernel.search.spatial.SpatialIndexWriter;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.languages.LanguageDetector;
import org.fao.geonet.repository.SourceRepository;
import org.fao.geonet.util.ThreadUtils;
import org.fao.geonet.utils.IO;
import org.fao.geonet.utils.TransformerFactoryFactory;
import org.fao.geonet.utils.Xml;
import org.geotools.data.DataStore;
import org.geotools.feature.AttributeTypeBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.jdom.Element;
import org.opengis.feature.type.AttributeDescriptor;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystem;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.sql.DataSource;

import static org.fao.geonet.constants.Geonet.Config.LANGUAGE_PROFILES_DIR;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Jesse on 11/5/2014.
 */
public class GeonetTestFixture {

    private volatile static FileSystem templateFs;
    private volatile static Path templateDataDirectory;
    private volatile static SchemaManager templateSchemaManager;
    @Autowired
    private ConfigurableApplicationContext _applicationContext;
    @Autowired
    protected DirectoryFactory _directoryFactory;
    @Autowired
    protected DataStore dataStore;


    private FileSystem currentFs;
    /**
     * Contain all datadirectories for all nodes.
     */
    private Path _dataDirContainer;

    /**
     * Default node data directory
     */
    private Path _dataDirectory;
    private static LuceneConfig templateLuceneConfig;
    private static SearchManager templateSearchManager;

    public void tearDown() throws IOException {
        IO.setFileSystemThreadLocal(null);
        if (currentFs != null) {
            currentFs.close();
        }
    }
    public void setup(AbstractCoreIntegrationTest test) throws Exception {
        final Path webappDir = AbstractCoreIntegrationTest.getWebappDir(test.getClass());
        TransformerFactoryFactory.init("de.fzi.dbs.xml.transform.CachingTransformerFactory");
//        TransformerFactoryFactory.init("net.sf.saxon.TransformerFactoryImpl");

        if (templateFs == null) {
            synchronized (GeonetTestFixture.class) {
                if (templateFs == null) {
                    templateFs = Jimfs.newFileSystem("template", Configuration.unix());
                    IO.setFileSystemThreadLocal(templateFs);

                    templateDataDirectory = templateFs.getPath("data");
                    IO.copyDirectoryOrFile(webappDir.resolve("WEB-INF/data"), templateDataDirectory, true, new DirectoryStream.Filter<Path>() {
                        @Override
                        public boolean accept(Path entry) throws IOException {
                            return !entry.toString().contains("schema_plugins") &&
                                   !entry.getFileName().toString().startsWith(".") &&
                                   !entry.getFileName().toString().endsWith(".iml") &&
                                   !entry.toString().contains("metadata_data") &&
                                   !entry.toString().contains("metadata_subversion") &&
                                   !entry.toString().contains("upload") &&
                                   !entry.toString().contains("resources" + File.separator + "xml");
                        }
                    });
                    Path schemaPluginsDir = templateDataDirectory.resolve("config/schema_plugins");
                    deploySchema(webappDir, schemaPluginsDir);
                    LanguageDetector.init(AbstractCoreIntegrationTest.getWebappDir(test.getClass()).resolve(_applicationContext.getBean
                            (LANGUAGE_PROFILES_DIR, String.class)));

                    final GeonetworkDataDirectory geonetworkDataDirectory = _applicationContext.getBean(GeonetworkDataDirectory.class);
                    final ServiceConfig serviceConfig = new ServiceConfig(Lists.<Element>newArrayList());
                    geonetworkDataDirectory.init("geonetwork", webappDir, templateDataDirectory, serviceConfig, null);

                    templateSchemaManager = initSchemaManager(webappDir, geonetworkDataDirectory);

                    _applicationContext.getBean(LuceneConfig.class).configure("WEB-INF/config-lucene.xml");
                    _applicationContext.getBean(SearchManager.class).init(false, false, "", 100);
                    Files.createDirectories(templateDataDirectory.resolve("data/resources/htmlcache"));
                }
            }
        }

        final String fsName = test.getClass().getSimpleName().replaceAll("[^a-z0-9A-Z]", "") + UUID.randomUUID().toString();
        currentFs = Jimfs.newFileSystem(fsName, Configuration.unix());
        IO.setFileSystemThreadLocal(currentFs);
        _dataDirContainer = currentFs.getPath("nodes");
        _dataDirectory = _dataDirContainer.resolve("default_data_dir");
        Files.createDirectories(_dataDirContainer);

        IO.copyDirectoryOrFile(templateDataDirectory, _dataDirectory, true);

        assertTrue(Files.isDirectory(_dataDirectory.resolve("config")));
        assertTrue(Files.isDirectory(_dataDirectory.resolve("data")));

        System.setProperty(LuceneConfig.USE_NRT_MANAGER_REOPEN_THREAD, Boolean.toString(true));
        configureNodeId(test);


        final GeonetworkDataDirectory dataDir = configureDataDir(test, webappDir, _dataDirectory);
        configureNewSchemaManager(dataDir, webappDir);

        _directoryFactory.resetIndex();

        _applicationContext.getBean(LuceneConfig.class).configure("WEB-INF/config-lucene.xml");
        _applicationContext.getBean(SearchManager.class).initNonStaticData(false, false, "", 100);
        _applicationContext.getBean(DataManager.class).init(test.createServiceContext(), false);


        addSourceUUID();

        final DataSource dataSource = _applicationContext.getBean(DataSource.class);
        try (Connection conn = dataSource.getConnection()) {
            ThreadUtils.init(conn.getMetaData().getURL(), _applicationContext.getBean(SettingManager.class));
        }
    }


    protected void configureNewSchemaManager(GeonetworkDataDirectory dataDir, Path webappDir) throws IOException {
        final SchemaManager schemaManager = _applicationContext.getBean(SchemaManager.class);
        schemaManager.configureFrom(templateSchemaManager, webappDir, dataDir);
        assertRequiredSchemas(schemaManager);
    }

    protected void addSourceUUID() {
        String siteUuid = _dataDirectory.getFileName().toString();
        _applicationContext.getBean(SettingManager.class).setSiteUuid(siteUuid);
        final SourceRepository sourceRepository = _applicationContext.getBean(SourceRepository.class);
        List<Source> sources = sourceRepository.findAll();
        if (sources.isEmpty()) {
            sources = new ArrayList<>(1);
            sources.add(sourceRepository.save(new Source().setLocal(true).setName("Name").setUuid(siteUuid)));
        }
    }

    protected SchemaManager initSchemaManager(Path webappDir, GeonetworkDataDirectory geonetworkDataDirectory) throws Exception {
        final Path schemaPluginsDir = geonetworkDataDirectory.getSchemaPluginsDir();
        final Path resourcePath = geonetworkDataDirectory.getResourcesDir();
        Path schemaPluginsCatalogFile = schemaPluginsDir.resolve("schemaplugin-uri-catalog.xml");

        final SchemaManager schemaManager = _applicationContext.getBean(SchemaManager.class);

        SchemaManager.registerXmlCatalogFiles(webappDir, schemaPluginsCatalogFile);
        schemaManager.configure(_applicationContext, webappDir, resourcePath,
                schemaPluginsCatalogFile, schemaPluginsDir, "eng", "iso19139", true);

        assertRequiredSchemas(schemaManager);
        SchemaManager template = new SchemaManager();
        template.configureFrom(schemaManager, webappDir, geonetworkDataDirectory);
        return template;
    }

    private void assertRequiredSchemas(SchemaManager schemaManager) {
        assertTrue(schemaManager.existsSchema("iso19139"));
        assertTrue(schemaManager.existsSchema("iso19115"));
        assertTrue(schemaManager.existsSchema("dublin-core"));
    }

    protected GeonetworkDataDirectory configureDataDir(AbstractCoreIntegrationTest test, Path webappDir, Path dataDirectory) throws IOException {
        final GeonetworkDataDirectory geonetworkDataDirectory = _applicationContext.getBean(GeonetworkDataDirectory.class);
        final ServiceConfig serviceConfig = registerServiceConfigAndInitDatastoreTable(test);
        geonetworkDataDirectory.init("geonetwork", webappDir, dataDirectory.toAbsolutePath(), serviceConfig, null);
        return geonetworkDataDirectory;
    }

    protected void configureNodeId(AbstractCoreIntegrationTest test) {
        NodeInfo nodeInfo = _applicationContext.getBean(NodeInfo.class);
        nodeInfo.setId(test.getGeonetworkNodeId());
        nodeInfo.setDefaultNode(test.isDefaultNode());
    }

    protected ServiceConfig registerServiceConfigAndInitDatastoreTable(AbstractCoreIntegrationTest test) throws IOException {
        final ArrayList<Element> params = test.getServiceConfigParameterElements();
        final ServiceConfig serviceConfig = new ServiceConfig(params);
        final String initializedString = "initialized";
        try {
            _applicationContext.getBean(initializedString);
        } catch (NoSuchBeanDefinitionException e) {
            _applicationContext.getBeanFactory().registerSingleton("serviceConfig", serviceConfig);
            _applicationContext.getBeanFactory().registerSingleton(initializedString, initializedString);
            SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
            AttributeDescriptor geomDescriptor = new AttributeTypeBuilder().crs(DefaultGeographicCRS.WGS84).binding(MultiPolygon.class)
                    .buildDescriptor("the_geom");
            builder.setName("spatialIndex");
            builder.add(geomDescriptor);
            builder.add(SpatialIndexWriter._IDS_ATTRIBUTE_NAME, String.class);
            this.dataStore.createSchema(builder.buildFeatureType());

        }
        return serviceConfig;
    }

    private void deploySchema(Path srcDataDir, Path schemaPluginPath) throws IOException {
        Files.createDirectories(schemaPluginPath);
        // Copy schema plugin
        final String schemaModulePath = "schemas";
        Path schemaModuleDir = srcDataDir.resolve("../../../../").resolve(schemaModulePath).normalize();
        if (Files.exists(schemaModuleDir)) {
            try (DirectoryStream<Path> paths = Files.newDirectoryStream(schemaModuleDir, IO.DIRECTORIES_FILTER)) {
                for (Path path : paths) {
                    final Path srcSchemaPluginDir = path.resolve("src/main/plugin").resolve(path.getFileName());
                    if (Files.exists(srcSchemaPluginDir)) {
                        Path destPath = schemaPluginPath.resolve(path.getFileName().toString());
                        IO.copyDirectoryOrFile(srcSchemaPluginDir, destPath, true);
                    }
                }
            }
        } else {
            throw new AssertionError("Schemas module not found.  this must be a programming error");
        }
    }

    public Path getDataDirContainer() {
        return this._dataDirContainer;
    }

    public void assertCorrectDataDir() throws Exception {
        synchronized (GeonetTestFixture.class) {
            final GeonetworkDataDirectory dataDirectory = _applicationContext.getBean(GeonetworkDataDirectory.class);
            final SchemaManager newSM = initSchemaManager(dataDirectory.getWebappDir(), dataDirectory);
            final SchemaManager thisContextSM = _applicationContext.getBean(SchemaManager.class);


            Xml.loadFile(templateSchemaManager.getSchemaDir("iso19139").resolve("schematron/criteria-type.xml"));
            Xml.loadFile(newSM.getSchemaDir("iso19139").resolve("schematron/criteria-type.xml"));
            Xml.loadFile(thisContextSM.getSchemaDir("iso19139").resolve("schematron/criteria-type.xml"));

            assertEquals("Expected Schemas: " + templateSchemaManager.getSchemas() + "\nActual Schemas: "+ newSM.getSchemas(),
                    templateSchemaManager.getSchemas().size(), newSM.getSchemas().size());
            assertEquals("Expected Schemas: " + templateSchemaManager.getSchemas() + "\nActual Schemas: "+ thisContextSM.getSchemas(),
                    templateSchemaManager.getSchemas().size(), thisContextSM.getSchemas().size());
            for (String templateName : templateSchemaManager.getSchemas()) {
                assertTrue(templateName, newSM.existsSchema(templateName));
                assertTrue(templateName, thisContextSM.existsSchema(templateName));
                Path thisContextSchemaDir = thisContextSM.getSchemaDir(templateName);
                final Path templateSchemaDir = templateSchemaManager.getSchemaDir(templateName);
                Files.walkFileTree(thisContextSchemaDir, new CompareDataDirectory(thisContextSchemaDir, templateSchemaDir));
                Files.walkFileTree(templateSchemaDir, new CompareDataDirectory(templateSchemaDir, thisContextSchemaDir));
            }

            Files.walkFileTree(templateDataDirectory, new CompareDataDirectory(templateDataDirectory, _dataDirectory));
            Files.walkFileTree(_dataDirectory, new CompareDataDirectory(_dataDirectory, templateDataDirectory));

        }
    }

    private static final class CompareDataDirectory extends SimpleFileVisitor<Path> {
        private final Path _dataDirectory;
        private final Path templateDataDirectory;

        public CompareDataDirectory(Path templateDataDirectory, Path dataDirectory) {
            this._dataDirectory = dataDirectory;
            this.templateDataDirectory = templateDataDirectory;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            final Path path = IO.relativeFile(templateDataDirectory, file, _dataDirectory);
            assertTrue(file.toString(), Files.exists(path));
            if (file.getFileName().toString().equals("schemaplugin-uri-catalog.xml")) {
                // this file has different paths so it is naturally different.  check it is non-null and move on
                assertTrue(Files.size(file) > 0);
            } else {
                assertEquals(file.toString(), Files.size(file), Files.size(path));
            }
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
            if (!dir.getFileName().toString().contains("upload")) {
                final Path path = IO.relativeFile(templateDataDirectory, dir, _dataDirectory);
                assertTrue(dir.toString() + " does not exist as expected: " + path, Files.exists(path));
                return FileVisitResult.CONTINUE;
            } else {
                return FileVisitResult.SKIP_SUBTREE;
            }
        }
    }
}
