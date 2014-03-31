package org.fao.geonet;

import com.google.common.collect.Lists;
import com.google.common.collect.TreeTraverser;
import com.google.common.io.Files;
import com.vividsolutions.jts.geom.MultiPolygon;
import jeeves.constants.ConfigFile;
import jeeves.server.ServiceConfig;
import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;
import jeeves.server.sources.ServiceRequest;
import org.apache.commons.io.FileUtils;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.*;
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
import org.fao.geonet.repository.*;
import org.fao.geonet.util.ThreadUtils;
import org.fao.geonet.utils.BinaryFile;
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
import org.junit.Before;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.Name;
import org.opengis.filter.Filter;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.test.context.ContextConfiguration;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.sql.DataSource;
import java.io.*;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertTrue;

import static org.junit.Assert.assertNotNull;

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
    protected static File _dataDirContainer;
    private static File _dataDirLockFile;

    /**
     * Default node data directory
     */
    protected static File _dataDirectory;
    @Before
    public void configureAppContext() throws Exception {

        synchronized (AbstractCoreIntegrationTest.class) {
            setUpDataDirectory();

            if (!_dataDirLockFile.exists()) {
                FileUtils.touch(_dataDirLockFile);
                _dataDirLockFile.deleteOnExit();
            }
        }

        System.setProperty(LuceneConfig.USE_NRT_MANAGER_REOPEN_THREAD, Boolean.toString(true));
        // clear out datastore
        for (Name name : _datastore.getNames()) {
            ((FeatureStore<?, ?>) _datastore.getFeatureSource(name)).removeFeatures(Filter.INCLUDE);
        }
        final String initializedString = "initialized";
        final String webappDir = getWebappDir(getClass());
        LanguageDetector.init(webappDir + _applicationContext.getBean(Geonet.Config.LANGUAGE_PROFILES_DIR, String.class));

        final GeonetworkDataDirectory geonetworkDataDirectory = _applicationContext.getBean(GeonetworkDataDirectory.class);

        final SyncReport syncReport = synchronizeDataDirectory(new File(webappDir, "WEB-INF/data"));

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

        geonetworkDataDirectory.init("geonetwork", webappDir, _dataDirectory.getAbsolutePath(),
                serviceConfig, null);

        _directoryFactory.resetIndex();

        final String schemaPluginsDir = geonetworkDataDirectory.getSchemaPluginsDir().getPath();
        final String resourcePath = geonetworkDataDirectory.getResourcesDir().getPath();

        final SchemaManager schemaManager = _applicationContext.getBean(SchemaManager.class);
        if (syncReport.updateSchemaManager || !schemaManager.existsSchema("iso19139")) {
            new File(_dataDirectory, "config/schemaplugin-uri-catalog.xml").delete();
            final String schemaPluginsCatalogFile = new File(schemaPluginsDir, "/schemaplugin-uri-catalog.xml").getPath();

            _applicationContext.getBean(LuceneConfig.class).configure("WEB-INF/config-lucene.xml");
            SchemaManager.registerXmlCatalogFiles(webappDir, schemaPluginsCatalogFile);

            schemaManager.configure(_applicationContext, webappDir, resourcePath,
                    schemaPluginsCatalogFile, schemaPluginsDir, "eng", "iso19139", true);
        }

        assertTrue(schemaManager.existsSchema("iso19139"));
        assertTrue(schemaManager.existsSchema("iso19115"));
        assertTrue(schemaManager.existsSchema("dublin-core"));

        _applicationContext.getBean(SearchManager.class).init(false, false, "", 100);
        _applicationContext.getBean(DataManager.class).init(createServiceContext(), false);

        String siteUuid = _dataDirectory.getName();
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

    private void setUpDataDirectory() {
        if (_dataDirLockFile != null && _dataDirLockFile.exists() &&
            _dataDirLockFile.lastModified() < twoHoursAgo()) {
            _dataDirLockFile.delete();
        }
        if (_dataDirectory == null || _dataDirLockFile.exists()) {
            File dir = getClassFile(getClass()).getParentFile();
            final String pathToTargetDir = "core/target";
            while(!new File(dir, pathToTargetDir).exists()) {
                dir = dir.getParentFile();
            }
            dir = new File(dir, pathToTargetDir+"/integration-test-datadirs");

            int i = 0;
            while (new File(dir.getPath()+i, DATA_DIR_LOCK_NAME).exists() && new File(dir.getPath()+i, DATA_DIR_LOCK_NAME).exists()) {
                i++;
            }

            while (!new File(dir.getPath()+i).exists() && !new File(dir.getPath()+i).mkdirs()) {
                i++;
                if (i > 1000) {
                    throw new Error("Unable to make test data directory");
                }
            }

            _dataDirContainer = new File(dir.getPath()+i);


            _dataDirectory = new File(_dataDirContainer, "defaultDataDir");
            _dataDirLockFile = new File(_dataDirContainer, DATA_DIR_LOCK_NAME);
        }
    }

    private long twoHoursAgo() {
        final Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR_OF_DAY, -2);
        return calendar.getTimeInMillis();
    }

    private SyncReport synchronizeDataDirectory(File srcDataDir) throws IOException {
        SyncReport report = new SyncReport();

        boolean deleteNewFilesFromDataDir = _dataDirectory.exists();

        final TreeTraverser<File> fileTreeTraverser = Files.fileTreeTraverser();


        if (deleteNewFilesFromDataDir ) {

            final int prefixPathLength2 = _dataDirectory.getPath().length();
            for (File dataDirFile : fileTreeTraverser.postOrderTraversal(_dataDirectory)) {
                String relativePath = dataDirFile.getPath().substring(prefixPathLength2);
                final File srcFile = new File(srcDataDir, relativePath);
                if (!srcFile.exists()) {
                    if (srcFile.getParent().endsWith("schematron") && relativePath.contains("schema_plugins") && relativePath.endsWith(".xsl")) {
                        // don't copy because the schematron xsl files are generated.
                        // normally they shouldn't be here because they don't need to be in the
                        // repository but some tests can generate them into the schemtrons folder
                        // so ignore them here.
                        continue;
                    }

                    if (relativePath.endsWith("schemaplugin-uri-catalog.xml")) {
                        // we will handle this special case later.
                        continue;
                    }

                    if (relativePath.contains("resources" + File.separator + "xml" + File.separator + "schemas")) {
                        // the schemas xml directory is copied by schema manager but since it is schemas we can reuse the directory.
                        continue;
                    }

                    if (dataDirFile.isFile() || dataDirFile.list().length == 0) {
                        if (!dataDirFile.delete()) {
                            // a file is holding on to a reference so we can't properly clean the data directory.
                            // this means we need a new one.
                            _dataDirectory = null;
                            setUpDataDirectory();
                            break;
                        }
                    }
                    report.updateSchemaManager |= relativePath.contains("schema_plugins");
                }
            }
        }

        final int prefixPathLength = srcDataDir.getPath().length();
        for (File file : fileTreeTraverser.preOrderTraversal(srcDataDir)) {
            String relativePath = file.getPath().substring(prefixPathLength);
            final File dataDirFile = new File(_dataDirectory, relativePath);
            if (file.isFile() && (!dataDirFile.exists() || dataDirFile.lastModified() != file.lastModified())) {
                if (file.getParent().endsWith("schematron") && relativePath.contains("schema_plugins") && relativePath.endsWith(".xsl")) {
                    // don't copy because the schematron xsl files are generated.
                    // normally they shouldn't be here because they don't need to be in the
                    // repository but some tests can generate them into the schemtrons folder
                    // so ignore them here.
                    continue;
                }

                if (relativePath.endsWith("schemaplugin-uri-catalog.xml")) {
                    // we will handle this special case later.
                    continue;
                }

                if (!dataDirFile.getParentFile().exists()) {
                    Files.createParentDirs(dataDirFile);
                }
                BinaryFile.copy(file, dataDirFile);
                dataDirFile.setLastModified(file.lastModified());

                report.updateSchemaManager |= relativePath.contains("schema_plugins");
            }
        }

        return report;
    }

    @After
    public void deleteNonDefaultNodeDataDirectories() throws IOException {
        synchronized (AbstractCoreIntegrationTest.class) {
            final Iterable<File> children = Files.fileTreeTraverser().children(_dataDirContainer);
            for (File child : children) {
                if (!child.equals(_dataDirectory)) {
                    for (File file : Files.fileTreeTraverser().postOrderTraversal(child)) {
                        FileUtils.deleteQuietly(file);
                    }
                }
            }
            _dataDirLockFile.delete();
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
        context.setAppPath(getWebappDir(getClass()));
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

    public String getStyleSheets() {
        final String file = getWebappDir(getClass());

        return new File(file, "xsl/conversion").getPath();
    }

    /**
     * Look up the webapp directory.
     *
     * @return
     */
    public static String getWebappDir(Class<?> cl) {
        File here = getClassFile(cl);
        while (!new File(here, "pom.xml").exists() && !new File(here.getParentFile(), "web/src/main/webapp/").exists()) {
//            System.out.println("Did not find pom file in: "+here);
            here = here.getParentFile();
        }

        return new File(here.getParentFile(), "web/src/main/webapp/").getAbsolutePath() + File.separator;
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
