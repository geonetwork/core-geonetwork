package org.fao.geonet;

import com.google.common.collect.Lists;
import com.vividsolutions.jts.geom.MultiPolygon;
import jeeves.constants.ConfigFile;
import jeeves.server.ServiceConfig;
import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.Pair;
import org.fao.geonet.domain.Profile;
import org.fao.geonet.domain.User;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.GeonetworkDataDirectory;
import org.fao.geonet.kernel.SchemaManager;
import org.fao.geonet.kernel.search.LuceneConfig;
import org.fao.geonet.kernel.search.SearchManager;
import org.fao.geonet.repository.AbstractSpringDataTest;
import org.fao.geonet.repository.UserRepository;
import org.fao.geonet.utils.TransformerFactoryFactory;
import org.fao.geonet.utils.Xml;
import org.geotools.data.DataStore;
import org.geotools.data.FeatureStore;
import org.geotools.data.memory.MemoryDataStore;
import org.geotools.feature.AttributeTypeBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.junit.Before;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.Name;
import org.opengis.filter.Filter;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.io.File;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;

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
public abstract class AbstractCoreTest extends AbstractSpringDataTest {
    @Autowired
    ConfigurableApplicationContext _applicationContext;
    @PersistenceContext
    EntityManager _entityManager;
    @Autowired
    DataStore _datastore;
    @Autowired
    UserRepository _userRepo;

    @Before
    public void configureAppContext() throws Exception {
        // clear out datastore
        for (Name name : _datastore.getNames()) {
            ((FeatureStore<?,?>) _datastore.getFeatureSource(name)).removeFeatures(Filter.INCLUDE);
        }
        final String initializedString = "initialized";
        try {
            _applicationContext.getBean(initializedString);
        } catch (NoSuchBeanDefinitionException e) {
            SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
            AttributeDescriptor geomDescriptor = new AttributeTypeBuilder().crs(DefaultGeographicCRS.WGS84).binding(MultiPolygon.class).buildDescriptor("the_geom");
            builder.setName("spatialIndex");
            builder.add(geomDescriptor);
            builder.add("id", String.class);
            _datastore.createSchema(builder.buildFeatureType());

            final ArrayList<Element> params = Lists.newArrayList(new Element("param")
                    .setAttribute(ConfigFile.Param.Attr.NAME, "preferredSchema")
                    .setAttribute(ConfigFile.Param.Attr.VALUE, "iso19139"));
            final ServiceConfig serviceConfig = new ServiceConfig(params);

            _applicationContext.getBeanFactory().registerSingleton("serviceConfig", serviceConfig);

            final File webappDir = getWebappDir();
            final File dataDir = new File(webappDir, "WEB-INF/data");
            final File configDir = new File(dataDir, "config");
            final String schemaPluginsCatalogFile = new File(getClassFile(), "../schemaplugin-uri-catalog.xml").getPath();
            final String schemaPluginsDir = new File(configDir, "schema_plugins").getPath();
            final String resourcePath = new File(dataDir, "data/resources").getPath();

            _applicationContext.getBean(GeonetworkDataDirectory.class).init("geonetwork", webappDir.getPath(), serviceConfig, null);
            _applicationContext.getBean(LuceneConfig.class).configure("luceneConfig.xml");

            SchemaManager.registerXmlCatalogFiles(webappDir.getPath() + "/", schemaPluginsCatalogFile);

            TransformerFactoryFactory.init("net.sf.saxon.TransformerFactoryImpl");

            final SchemaManager schemaManager = _applicationContext.getBean(SchemaManager.class);
            schemaManager.configure(webappDir.getPath(), resourcePath,
                    schemaPluginsCatalogFile, schemaPluginsDir, "eng", "iso19139", true);
            _applicationContext.getBeanFactory().registerSingleton(initializedString, initializedString);

            _applicationContext.getBean(SearchManager.class).init(false, false, "", 100);
            _applicationContext.getBean(DataManager.class).init(createServiceContext(), false);
        }
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
        return new ServiceContext("mockService", _applicationContext, contexts, _entityManager);
    }

    /**
     * Check if an element exists and if it has the expected test.
     *
     * @param expected the expected text
     * @param xml      the xml to search
     * @param xpath    the xpath to the element to check
     */
    protected void assertEqualsText(String expected, Element xml, String xpath) throws JDOMException {
        final Element element = Xml.selectElement(xml, xpath);
        assertNotNull("No element found at: " + xpath + " in \n" + Xml.getString(xml), element);
        assertEquals(expected, element.getText());
    }

    /**
     * Create an xml params Element in the form most services expect.
     *
     * @param params the params map to convert to Element
     */
    protected Element createParams(Pair<String, String>... params) {
        final Element request = new Element("request");
        for (Pair<String, String> param : params) {
            request.addContent(new Element(param.one()).setText(param.two()));
        }
        return request;
    }

    protected String getStyleSheets() {
        final File file = getWebappDir();

        return new File(file, "xsl/conversion").getPath();
    }

    private File getWebappDir() {
        File here = getClassFile();
        while (!new File(here, "pom.xml").exists()) {
            here = here.getParentFile();
        }

        return new File(here.getParentFile(), "web/src/main/webapp/").getAbsoluteFile();
    }

    private File getClassFile() {
        final String testClassName = AbstractCoreTest.class.getSimpleName();
        return new File(AbstractCoreTest.class.getResource(testClassName + ".class").getFile());
    }

    protected User loginAsAdmin(ServiceContext context) {
        final User admin = _userRepo.findAllByProfile(Profile.Administrator).get(0);
        UserSession userSession = new UserSession();
        userSession.loginAs(admin);
        context.setUserSession(userSession);
        return admin;
    }
}
