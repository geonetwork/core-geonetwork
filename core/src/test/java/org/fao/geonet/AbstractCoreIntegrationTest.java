package org.fao.geonet;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import jeeves.constants.ConfigFile;
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
import org.fao.geonet.kernel.mef.Importer;
import org.fao.geonet.repository.AbstractSpringDataTest;
import org.fao.geonet.repository.SourceRepository;
import org.fao.geonet.repository.UserRepository;
import org.fao.geonet.util.ThreadPool;
import org.fao.geonet.utils.Log;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.junit.After;
import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import static java.lang.Math.round;
import static org.junit.Assert.assertTrue;

/**
 * A helper class for testing services.  This super-class loads in the spring beans for Spring-data repositories and mocks for
 * some of the system that is required by services.
 * <p/>
 * User: Jesse
 * Date: 10/12/13
 * Time: 8:31 PM
 */
@ContextConfiguration(
    inheritLocations = true,
    locations = {"classpath:core-repository-test-context.xml", "classpath:web-test-context.xml"}
    )
    public abstract class AbstractCoreIntegrationTest extends AbstractSpringDataTest {
        @Autowired
        protected ConfigurableApplicationContext _applicationContext;
    @PersistenceContext
    protected EntityManager _entityManager;
    @Autowired
    protected GeonetTestFixture testFixture;
    @Autowired
    protected UserRepository _userRepo;

    @Before
    public void setup() throws Exception {
        testFixture.setup(this);
    }

    @After
    public void tearDown() throws Exception {
        testFixture.tearDown();
    }

    protected void assertDataDirInMemoryFS(ServiceContext context) {
        final Path systemDataDir = context.getBean(GeonetworkDataDirectory.class).getSystemDataDir();
        assertTrue(systemDataDir.getFileSystem() != FileSystems.getDefault());
    }

    protected String lineInMethod(Throwable e, Method method) {
        for (StackTraceElement stackTraceElement : e.getStackTrace()) {
            if (stackTraceElement.getMethodName().equals(method.getName())) {
                return stackTraceElement.toString();
            }
        }
        throw new Error("No Method " + method.getName() + " found in " + e, e);
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
        GeonetContext gc = (GeonetContext) constructor.newInstance(_applicationContext, false, null, new ThreadPool(){
            @Override
            public void runTask(Runnable task, int delayBeforeStart, TimeUnit unit) {
                task.run();
            }
        });


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

        assertDataDirInMemoryFS(context);

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

    public static File getClassFile(Class<?> cl) {
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

    public MockHttpSession loginAsAdmin() {
        final User user = _userRepo.findAllByProfile(Profile.Administrator)
                .get(0);
        MockHttpSession session = new MockHttpSession();

        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                user, null, user.getAuthorities());
        auth.setDetails(user);
        SecurityContextHolder.getContext().setAuthentication(auth);
          
        return session;
    }

    public MockHttpSession loginAs(User user) {
        MockHttpSession session = new MockHttpSession();

        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                user, null, user.getAuthorities());
        auth.setDetails(user);
        SecurityContextHolder.getContext().setAuthentication(auth);
          
        return session;
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
                source.getUuid(), source.getName(), Maps.<String, String>newHashMap(), context,
                id, createDate, createDate,
                "" + groupId, metadataType);

        dataManager.indexMetadata(id.get(0), true);
        return Integer.parseInt(id.get(0));
    }

    protected void measurePerformance(TestFunction testFunction) throws Exception {
        long start = System.nanoTime();
        final long fiveSec = TimeUnit.SECONDS.toNanos(5);
        while (System.nanoTime() - start < fiveSec) {
            testFunction.exec();
        }
        System.out.println("Starting big run");
        final int secondsRan = 30;
        final long thirtySec = TimeUnit.SECONDS.toNanos(secondsRan);
        start = System.nanoTime();
        double executions = 0;
        while (System.nanoTime() - start < thirtySec) {
            testFunction.exec();
            executions++;
        }
        long end = System.nanoTime();

        final long duration = end - start;
        System.out.println("Executed " + executions + " in "+ (TimeUnit.NANOSECONDS.toSeconds(duration * 1000) / 1000)+" seconds.");
        System.out.println("   Average of " + round(((double) TimeUnit.NANOSECONDS.toMillis(duration)) / executions) + "ms per execution;");
        System.out.println("   Average of " + round(executions / TimeUnit.NANOSECONDS.toSeconds(duration)) + " executions per second;");
    }


    protected void addTestSpecificData(GeonetworkDataDirectory geonetworkDataDirectory) throws IOException {

    }

    public boolean resetLuceneIndex() {
        return true;
    }
}
