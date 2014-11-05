package org.fao.geonet;

import com.google.common.collect.Lists;
import com.vividsolutions.jts.geom.MultiPolygon;
import jeeves.constants.ConfigFile;
import jeeves.server.ServiceConfig;
import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;
import jeeves.server.sources.ServiceRequest;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.ISODate;
import org.fao.geonet.domain.MetadataType;
import org.fao.geonet.domain.Pair;
import org.fao.geonet.domain.Profile;
import org.fao.geonet.domain.Source;
import org.fao.geonet.domain.User;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.GeonetworkDataDirectory;
import org.fao.geonet.kernel.SchemaManager;
import org.fao.geonet.kernel.mef.Importer;
import org.fao.geonet.kernel.search.LuceneConfig;
import org.fao.geonet.kernel.search.SearchManager;
import org.fao.geonet.kernel.search.index.DirectoryFactory;
import org.fao.geonet.kernel.search.spatial.SpatialIndexWriter;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.languages.LanguageDetector;
import org.fao.geonet.repository.AbstractSpringDataTest;
import org.fao.geonet.repository.SourceRepository;
import org.fao.geonet.repository.UserRepository;
import org.fao.geonet.util.ThreadUtils;
import org.fao.geonet.utils.IO;
import org.fao.geonet.utils.Log;
import org.fao.geonet.utils.TransformerFactoryFactory;
import org.fao.geonet.utils.Xml;
import org.geotools.data.DataStore;
import org.geotools.data.FeatureStore;
import org.geotools.feature.AttributeTypeBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.Name;
import org.opengis.filter.Filter;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.sql.DataSource;

import static org.junit.Assert.assertTrue;

/**
 * A helper class for testing services.  This super-class loads in the spring beans for Spring-data repositories and mocks for
 * some of the system that is required by services.
 * <p/>
 * User: Jesse
 * Date: 10/12/13
 * Time: 8:31 PM
 */
@ContextConfiguration(inheritLocations = true, locations = "classpath:core-repository-test-context.xml")
public abstract class AbstractCoreIntegrationTest extends AbstractSpringDataTest {
    private static final String DATA_DIR_LOCK_NAME = "lock";
    @Autowired
    protected ConfigurableApplicationContext _applicationContext;
    @PersistenceContext
    protected EntityManager _entityManager;
    @Autowired
    protected DataStore _datastore;
    @Autowired
    protected UserRepository _userRepo;
    @Autowired
    protected DirectoryFactory _directoryFactory;

    /**
     * Contain all datadirectories for all nodes.
     */
    protected static Path _dataDirContainer;
    private static Path _dataDirLockFile;

    /**
     * Default node data directory
     */
    protected static Path _dataDirectory;

    @AfterClass
    public static void tearDown() throws Exception {
        if (_dataDirLockFile != null) {
            Files.deleteIfExists(_dataDirLockFile);
        }
    }

    @Before
    public void configureAppContext() throws Exception {

        synchronized (AbstractCoreIntegrationTest.class) {
            setUpDataDirectory();

            if (!Files.exists(_dataDirLockFile)) {
                IO.touch(_dataDirLockFile);
                Runtime.getRuntime().addShutdownHook(new Thread() {
                    public void run()
                    {
                        try {
                            Files.deleteIfExists(_dataDirLockFile);
                        } catch (IOException e) {
                            throw new Error(e);
                        }
                    }
                });
            }
        }

        System.setProperty(LuceneConfig.USE_NRT_MANAGER_REOPEN_THREAD, Boolean.toString(true));
        // clear out datastore
        for (Name name : _datastore.getNames()) {
            ((FeatureStore<?, ?>) _datastore.getFeatureSource(name)).removeFeatures(Filter.INCLUDE);
        }
        final String initializedString = "initialized";
        final Path webappDir = getWebappDir(getClass());
        LanguageDetector.init(webappDir.resolve(_applicationContext.getBean(Geonet.Config.LANGUAGE_PROFILES_DIR, String.class)));

        final GeonetworkDataDirectory geonetworkDataDirectory = _applicationContext.getBean(GeonetworkDataDirectory.class);

        Files.createDirectories(_dataDirContainer);
        IO.copyDirectoryOrFile(webappDir.resolve("WEB-INF/data"), _dataDirectory);
        final ArrayList<Element> params = getServiceConfigParameterElements();

        final ServiceConfig serviceConfig = new ServiceConfig(params);

        try {
            _applicationContext.getBean(initializedString);
        } catch (NoSuchBeanDefinitionException e) {
            SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
            AttributeDescriptor geomDescriptor = new AttributeTypeBuilder().crs(DefaultGeographicCRS.WGS84).binding(MultiPolygon.class)
                    .buildDescriptor("the_geom");
            builder.setName("spatialIndex");
            builder.add(geomDescriptor);
            builder.add(SpatialIndexWriter._IDS_ATTRIBUTE_NAME, String.class);
            _datastore.createSchema(builder.buildFeatureType());

            _applicationContext.getBeanFactory().registerSingleton("serviceConfig", serviceConfig);
            _applicationContext.getBeanFactory().registerSingleton(initializedString, initializedString);
        }

        NodeInfo nodeInfo = _applicationContext.getBean(NodeInfo.class);
        nodeInfo.setId(getGeonetworkNodeId());
        nodeInfo.setDefaultNode(isDefaultNode());

        TransformerFactoryFactory.init("net.sf.saxon.TransformerFactoryImpl");

        geonetworkDataDirectory.init("geonetwork", webappDir, _dataDirectory, serviceConfig, null);

        _directoryFactory.resetIndex();

        final Path schemaPluginsDir = geonetworkDataDirectory.getSchemaPluginsDir();

        final Path resourcePath = geonetworkDataDirectory.getResourcesDir();

        final SchemaManager schemaManager = _applicationContext.getBean(SchemaManager.class);
        Files.deleteIfExists(_dataDirectory.resolve("config/schemaplugin-uri-catalog.xml"));
        Path schemaPluginsCatalogFile = schemaPluginsDir.resolve("schemaplugin-uri-catalog.xml");
        deploySchema(webappDir, schemaPluginsDir);

        _applicationContext.getBean(LuceneConfig.class).configure("WEB-INF/config-lucene.xml");
        SchemaManager.registerXmlCatalogFiles(webappDir, schemaPluginsCatalogFile);

        schemaManager.configure(_applicationContext, webappDir, resourcePath,
                schemaPluginsCatalogFile, schemaPluginsDir, "eng", "iso19139", true);

        assertTrue(schemaManager.existsSchema("iso19139"));
        assertTrue(schemaManager.existsSchema("iso19115"));
        assertTrue(schemaManager.existsSchema("dublin-core"));

        _applicationContext.getBean(SearchManager.class).init(false, false, "", 100);
        _applicationContext.getBean(DataManager.class).init(createServiceContext(), false);

        String siteUuid = _dataDirectory.getFileName().toString();
        _applicationContext.getBean(SettingManager.class).setSiteUuid(siteUuid);
        final SourceRepository sourceRepository = _applicationContext.getBean(SourceRepository.class);
        List<Source> sources = sourceRepository.findAll();
        if (sources.isEmpty()) {
            sources = new ArrayList<Source>(1);
            sources.add(sourceRepository.save(new Source().setLocal(true).setName("Name").setUuid(siteUuid)));
        }
        final DataSource dataSource = _applicationContext.getBean(DataSource.class);
        Connection conn = null;
        try {
            conn = dataSource.getConnection();
            ThreadUtils.init(conn.getMetaData().getURL(), _applicationContext.getBean(SettingManager.class));
        } finally {
            if (conn != null) {
                conn.close();
            }
        }

    }

    private void setUpDataDirectory() throws IOException {
        if (_dataDirLockFile != null && Files.exists(_dataDirLockFile) &&
            Files.getLastModifiedTime(_dataDirLockFile).toMillis() < twoHoursAgo()) {
            Files.delete(_dataDirLockFile);
        }
        if (_dataDirectory == null || _dataDirLockFile == null || !Files.exists(_dataDirLockFile)) {
            Path dir = getClassFile(getClass()).getParentFile().toPath();
            final String pathToTargetDir = "core/target";
            while(!Files.exists(dir.resolve(pathToTargetDir))) {
                dir = dir.getParent();
            }
            dir = dir.resolve(pathToTargetDir).resolve("integration-test-datadirs");

            int i = 0;
            while (Files.exists(dir.resolve(String.valueOf(i)).resolve(DATA_DIR_LOCK_NAME))) {
                i++;
            }

            while (i < 1000) {
                try {
                    Files.createDirectories(dir.resolve(String.valueOf(i)));
                    break;
                } catch (IOException e) {
                    i++;
                }
            }


            _dataDirContainer = dir.resolve(String.valueOf(i));

            _dataDirectory = _dataDirContainer.resolve("defaultDataDir");
            _dataDirLockFile = _dataDirContainer.resolve(DATA_DIR_LOCK_NAME);
        }
    }

    private long twoHoursAgo() {
        final Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR_OF_DAY, -2);
        return calendar.getTimeInMillis();
    }

    private void deploySchema(Path srcDataDir, Path schemaPluginPath) throws IOException {
        // Copy schema plugin
        final String schemaModulePath = "schemas";
        Path schemaModuleDir = srcDataDir.resolve("../../../../").resolve(schemaModulePath).normalize();
        if (Files.exists(schemaModuleDir)) {
            try (DirectoryStream<Path> paths = Files.newDirectoryStream(schemaModuleDir)) {
                for (Path path : paths) {
                    final Path srcSchemaPluginDir = path.resolve("src/main/plugin").resolve(path.getFileName());
                    if (Files.exists(srcSchemaPluginDir)) {
                        Path destPath = schemaPluginPath.resolve(path.getFileName());
                        IO.copyDirectoryOrFile(srcSchemaPluginDir, destPath);
                    }
                }
            }
        } else {
            throw new AssertionError("Schemas module not found.  this must be a programming error");
        }
    }

    @After
    public void deleteNonDefaultNodeDataDirectories() throws IOException {
        synchronized (AbstractCoreIntegrationTest.class) {
            int i = 0;
            while(i < 30) {
                try {
                    IO.deleteFileOrDirectory(_dataDirContainer);
                    i = 100;
                } catch (Exception e) {
                    i ++;
                }
            }
            Files.deleteIfExists(_dataDirLockFile);
        }

    }

    protected boolean isDefaultNode() {
        return true;
    }

    /**
     * Get the elements in the service config object.
     */
    protected ArrayList<Element> getServiceConfigParameterElements() {
        return Lists.newArrayList(createServiceConfigParam("preferredSchema", "iso19139"));
    }

    protected static Element createServiceConfigParam(String name, String value) {
        return new Element("param")
                .setAttribute(ConfigFile.Param.Attr.NAME, name)
                .setAttribute(ConfigFile.Param.Attr.VALUE, value);
    }

    /**
     * Get the node id of the geonetwork node under test.  This hook is here primarily for the GeonetworkDataDirectory tests
     * but also useful for any other tests that want to test multi node support.
     *
     * @return the node id to put into the ApplicationContext.
     */
    protected String getGeonetworkNodeId() {
        return "srv";
    }

    /**
     * Create a Service context without a user session but otherwise ready to use.
     */
    protected ServiceContext createServiceContext() throws Exception {
        final HashMap<String, Object> contexts = new HashMap<String, Object>();
        final Constructor<?> constructor = GeonetContext.class.getDeclaredConstructors()[0];
        constructor.setAccessible(true);
        GeonetContext gc = (GeonetContext) constructor.newInstance(_applicationContext, false, null, null);


        contexts.put(Geonet.CONTEXT_NAME, gc);
        final ServiceContext context = new ServiceContext("mockService", _applicationContext, contexts, _entityManager);
        context.setAsThreadLocal();
        context.setInputMethod(ServiceRequest.InputMethod.GET);
        context.setIpAddress("127.0.1");
        context.setLanguage("eng");
        context.setLogger(Log.createLogger("Test"));
        context.setMaxUploadSize(100);
        context.setOutputMethod(ServiceRequest.OutputMethod.DEFAULT);
        context.setBaseUrl("geonetwork");

        return context;
    }

    /**
     * Check if an element exists and if it has the expected test.
     *
     * @param expected   the expected text
     * @param xml        the xml to search
     * @param xpath      the xpath to the element to check
     * @param namespaces the namespaces required for xpath
     */
    protected void assertEqualsText(String expected, Element xml, String xpath, Namespace... namespaces) throws JDOMException {
        Assert.assertEqualsText(expected, xml, xpath, namespaces);
    }

    /**
     * Create an xml params Element in the form most services expect.
     *
     * @param params the params map to convert to Element
     */
    protected Element createParams(Pair<String, ? extends Object>... params) {
        final Element request = new Element("request");
        for (Pair<String, ?> param : params) {
            request.addContent(new Element(param.one()).setText(param.two().toString()));
        }
        return request;
    }

    public Path getStyleSheets() {
        final Path file = getWebappDir(getClass());

        return file.resolve("xsl/conversion");
    }

    /**
     * Look up the webapp directory.
     *
     * @return
     */
    public static Path getWebappDir(Class<?> cl) {
        Path here = getClassFile(cl).toPath();
        while (!Files.exists(here.resolve("pom.xml")) && !Files.exists(here.getParent().resolve("web/src/main/webapp/"))) {
//            System.out.println("Did not find pom file in: "+here);
            here = here.getParent();
        }

        return here.getParent().resolve("web/src/main/webapp/");
    }

    protected static File getClassFile(Class<?> cl) {
        final String testClassName = cl.getSimpleName();
        return new File(cl.getResource(testClassName + ".class").getFile());
    }

    public User loginAsAdmin(ServiceContext context) {
        final User admin = _userRepo.findAllByProfile(Profile.Administrator).get(0);
        UserSession userSession = new UserSession();
        userSession.loginAs(admin);
        context.setUserSession(userSession);
        return admin;
    }

    public Element getSampleMetadataXml() throws IOException, JDOMException {
        final URL resource = AbstractCoreIntegrationTest.class.getResource("kernel/valid-metadata.iso19139.xml");
        return Xml.loadStream(resource.openStream());
    }

    /**
     *
     * @param uuidAction  Either: Params.GENERATE_UUID, Params.NOTHING, or Params.OVERWRITE
     * @return
     * @throws Exception
     */
    public int importMetadataXML(ServiceContext context, String uuid, InputStream xmlInputStream, MetadataType metadataType,
                                 int groupId, String uuidAction) throws Exception {
        final Element metadata = Xml.loadStream(xmlInputStream);
        final DataManager dataManager = _applicationContext.getBean(DataManager.class);
        String schema = dataManager.autodetectSchema(metadata);
        final SourceRepository sourceRepository = _applicationContext.getBean(SourceRepository.class);
        List<Source> sources = sourceRepository.findAll();

        if (sources.isEmpty()) {
            final Source source = sourceRepository.save(new Source().setLocal(true).setName("localsource").setUuid("uuidOfLocalSorce"));
            sources = Lists.newArrayList(source);
        }

        Source source = sources.get(0);
        ArrayList<String> id = new ArrayList<String>(1);
        String createDate = new ISODate().getDateAndTime();
        Importer.importRecord(uuid,
                uuidAction, Lists.newArrayList(metadata), schema, 0,
                source.getUuid(), source.getName(), context,
                id, createDate, createDate,
                "" + groupId, metadataType);

        dataManager.indexMetadata(id.get(0), true);
        return Integer.parseInt(id.get(0));
    }

    private class SyncReport {
        public boolean updateSchemaManager = false;
    }
}
