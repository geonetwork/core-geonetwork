package org.fao.geonet;

import com.google.common.collect.Lists;
import com.vividsolutions.jts.geom.MultiPolygon;
import jeeves.constants.ConfigFile;
import jeeves.server.ServiceConfig;
import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;
import jeeves.server.sources.ServiceRequest;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.Pair;
import org.fao.geonet.domain.Profile;
import org.fao.geonet.domain.User;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.GeonetworkDataDirectory;
import org.fao.geonet.kernel.SchemaManager;
import org.fao.geonet.kernel.search.LuceneConfig;
import org.fao.geonet.kernel.search.SearchManager;
import org.fao.geonet.kernel.search.spatial.SpatialIndexWriter;
import org.fao.geonet.repository.AbstractSpringDataTest;
import org.fao.geonet.repository.UserRepository;
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
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.Name;
import org.opengis.filter.Filter;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextConfiguration;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import static org.apache.commons.io.FileUtils.copyDirectory;
import static org.junit.Assert.assertEquals;
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
    @Autowired
    protected ConfigurableApplicationContext _applicationContext;
    @PersistenceContext
    protected EntityManager _entityManager;
    @Autowired
    protected DataStore _datastore;
    @Autowired
    protected UserRepository _userRepo;

    @Rule
    public TemporaryFolder _testTemporaryFolder = new TemporaryFolder();

    @Before
    public void configureAppContext() throws Exception {
        System.setProperty(LuceneConfig.USE_NRT_MANAGER_REOPEN_THREAD, Boolean.toString(true));
        // clear out datastore
        for (Name name : _datastore.getNames()) {
            ((FeatureStore<?,?>) _datastore.getFeatureSource(name)).removeFeatures(Filter.INCLUDE);
        }
        final String initializedString = "initialized";
        final String webappDir = getWebappDir(getClass());
        final File templateDataDir = new File(webappDir, "WEB-INF/data");
        final GeonetworkDataDirectory geonetworkDataDirectory = _applicationContext.getBean(GeonetworkDataDirectory.class);

        final ArrayList<Element> params = getServiceConfigParameterElements();

        final ServiceConfig serviceConfig = new ServiceConfig(params);

        try {
            _applicationContext.getBean(initializedString);
        } catch (NoSuchBeanDefinitionException e) {
            SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
            AttributeDescriptor geomDescriptor = new AttributeTypeBuilder().crs(DefaultGeographicCRS.WGS84).binding(MultiPolygon.class).buildDescriptor("the_geom");
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


        final File dataDir = _testTemporaryFolder.getRoot();
        copyDirectory(templateDataDir, dataDir);

        final File configDir = new File(dataDir, "config");
        final String schemaPluginsDir = new File(configDir, "schema_plugins").getPath();
        final String resourcePath = new File(dataDir, "data/resources").getPath();
        final String schemaPluginsCatalogFile = new File(schemaPluginsDir, "/schemaplugin-uri-catalog.xml").getPath();

        TransformerFactoryFactory.init("net.sf.saxon.TransformerFactoryImpl");

        geonetworkDataDirectory.init("geonetwork", webappDir, dataDir.getAbsolutePath(),
                serviceConfig, null);

        _applicationContext.getBean(LuceneConfig.class).configure("luceneConfig.xml");
        SchemaManager.registerXmlCatalogFiles(webappDir, schemaPluginsCatalogFile);

        final SchemaManager schemaManager = _applicationContext.getBean(SchemaManager.class);
        schemaManager.configure(webappDir, resourcePath,
                schemaPluginsCatalogFile, schemaPluginsDir, "eng", "iso19139", true);

        _applicationContext.getBean(SearchManager.class).init(false, false, "", 100);
        _applicationContext.getBean(DataManager.class).init(createServiceContext(), false);

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
     * @param expected the expected text
     * @param xml      the xml to search
     * @param xpath    the xpath to the element to check
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

        return new File(here.getParentFile(), "web/src/main/webapp/").getAbsolutePath()+File.separator;
    }

    private static File getClassFile(Class<?> cl) {
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

    protected Element getSampleMetadataXml() throws IOException, JDOMException {
        final URL resource = AbstractCoreIntegrationTest.class.getResource("kernel/valid-metadata.iso19139.xml");
        return Xml.loadStream(resource.openStream());
    }


}
