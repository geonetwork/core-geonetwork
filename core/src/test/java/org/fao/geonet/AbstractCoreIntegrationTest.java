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

package org.fao.geonet;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import jeeves.constants.ConfigFile;
import jeeves.constants.Jeeves;
import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;
import jeeves.server.dispatchers.ServiceManager;
import jeeves.server.sources.ServiceRequest;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.AbstractMetadata;
import org.fao.geonet.domain.ISODate;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.domain.MetadataType;
import org.fao.geonet.domain.Pair;
import org.fao.geonet.domain.Profile;
import org.fao.geonet.domain.Source;
import org.fao.geonet.domain.SourceType;
import org.fao.geonet.domain.User;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.GeonetworkDataDirectory;
import org.fao.geonet.kernel.SchemaManager;
import org.fao.geonet.kernel.UpdateDatestamp;
import org.fao.geonet.kernel.datamanager.IMetadataManager;
import org.fao.geonet.kernel.mef.Importer;
import org.fao.geonet.kernel.mef.MEFLib;
import org.fao.geonet.kernel.search.IndexingMode;
import org.fao.geonet.repository.AbstractSpringDataTest;
import org.fao.geonet.repository.GroupRepository;
import org.fao.geonet.repository.SourceRepository;
import org.fao.geonet.repository.UserGroupRepository;
import org.fao.geonet.repository.UserRepository;
import org.fao.geonet.schema.iso19115_3_2018.ISO19115_3_2018SchemaPlugin;
import org.fao.geonet.schema.iso19139.ISO19139SchemaPlugin;
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
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static java.lang.Math.round;
import static org.junit.Assert.assertTrue;
import static org.owasp.esapi.crypto.CryptoToken.ANONYMOUS_USER;
import static org.springframework.security.core.authority.AuthorityUtils.createAuthorityList;

/**
 * A helper class for testing services.  This super-class loads in the spring beans for Spring-data
 * repositories and mocks for some of the system that is required by services.
 * <p/>
 * User: Jesse Date: 10/12/13 Time: 8:31 PM
 */
@ContextConfiguration(
    inheritLocations = true,
    locations = {"classpath:core-repository-test-context.xml"}
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
    @Autowired
    protected UserGroupRepository _userGroupRepo;
    @Autowired
    protected GroupRepository _groupRepo;
    @Autowired
    private SchemaManager schemaManager;
    @Autowired
    private IMetadataManager metadataManager;
    @Autowired
    private SourceRepository sourceRepository;

    protected static Element createServiceConfigParam(String name, String value) {
        return new Element("param")
            .setAttribute(ConfigFile.Param.Attr.NAME, name)
            .setAttribute(ConfigFile.Param.Attr.VALUE, value);
    }

    /**
     * Look up the webapp directory.
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
        try {
            return new File(URLDecoder.decode(cl.getResource(testClassName + ".class").getFile(), Constants.ENCODING));
        } catch (UnsupportedEncodingException e) {
            throw new Error(e);
        }
    }

    @Before
    public final void setup() throws Exception {
        testFixture.setup(this);
    }

    @After
    public final void tearDown() throws Exception {
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

    /**
     * Get the node id of the geonetwork node under test.  This hook is here primarily for the
     * GeonetworkDataDirectory tests but also useful for any other tests that want to test multi
     * node support.
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
        GeonetContext gc = new GeonetContext(_applicationContext, false);
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
        context.getBean(ServiceManager.class).registerContext(Geonet.CONTEXT_NAME, gc);

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

        return loginAs(user);
    }

    public User loginAs(User user, ServiceContext context) {
        UserSession userSession = new UserSession();
        userSession.loginAs(user);
        context.setUserSession(userSession);
        return user;
    }

    public MockHttpSession loginAs(User user) {
        MockHttpSession session = new MockHttpSession();

        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
            user, null, user.getAuthorities());
        auth.setDetails(user);
        SecurityContextHolder.getContext().setAuthentication(auth);

        UserSession userSession = new UserSession();
        userSession.loginAs(user);
        session.setAttribute(Jeeves.Elem.SESSION, userSession);
        userSession.setsHttpSession(session);

        return session;
    }

    public MockHttpSession loginAsAnonymous() {
        MockHttpSession session = new MockHttpSession();

        AnonymousAuthenticationToken auth = new AnonymousAuthenticationToken( ANONYMOUS_USER, ANONYMOUS_USER,
            createAuthorityList( "ROLE_ANONYMOUS" ) );

        SecurityContextHolder.getContext().setAuthentication(auth);

        UserSession userSession = new UserSession();
        session.setAttribute(Jeeves.Elem.SESSION, userSession);
        userSession.setsHttpSession(session);

        return session;
    }

    private Element getSample(String resource) throws IOException, JDOMException {
        final URL resourceUrl = AbstractCoreIntegrationTest.class.getResource(resource);
        return Xml.loadStream(resourceUrl.openStream());
    }

    public Element getSampleMetadataXml() throws IOException, JDOMException {
        return getSample("kernel/valid-metadata.iso19139.xml");
    }

    public Element getSampleISO19139MetadataXml() throws IOException, JDOMException {
        return getSample("kernel/metadata.iso19139.xml");
    }

    public Element getSampleISO19115MetadataXml() throws IOException, JDOMException {
        return getSample("kernel/metadata.iso19115-3.xml");
    }

    /**
     * @param uuidAction Either: Params.GENERATE_UUID, Params.NOTHING, or Params.OVERWRITE
     */
    public int importMetadataXML(ServiceContext context, String uuid, InputStream xmlInputStream, MetadataType metadataType,
                                 int groupId, String uuidAction) throws Exception {
        final Element metadata = Xml.loadStream(xmlInputStream);
        final DataManager dataManager = _applicationContext.getBean(DataManager.class);
        String schema = dataManager.autodetectSchema(metadata);
        final SourceRepository sourceRepository = _applicationContext.getBean(SourceRepository.class);
        List<Source> sources = sourceRepository.findAll();

        if (sources.isEmpty()) {
            final Source source = sourceRepository.save(new Source().setType(SourceType.portal).setName("localsource").setUuid("uuidOfLocalSource"));
            sources = Lists.newArrayList(source);
        }

        Source source = sources.get(0);
        ArrayList<String> id = new ArrayList<String>(1);
        String createDate = new ISODate().getDateAndTime();
        Importer.importRecord(uuid,
            MEFLib.UuidAction.parse(uuidAction), Lists.newArrayList(metadata), schema, 0,
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
        System.out.println("Executed " + executions + " in " + (TimeUnit.NANOSECONDS.toSeconds(duration * 1000) / 1000) + " seconds.");
        System.out.println("   Average of " + round(((double) TimeUnit.NANOSECONDS.toMillis(duration)) / executions) + "ms per execution;");
        System.out.println("   Average of " + round(executions / TimeUnit.NANOSECONDS.toSeconds(duration)) + " executions per second;");
    }


    protected void addTestSpecificData(GeonetworkDataDirectory geonetworkDataDirectory) throws IOException {

    }

    public boolean resetLuceneIndex() {
        return true;
    }

    protected AbstractMetadata injectMetadataInDbDoNotRefreshHeader(Element sampleMetadataXml, ServiceContext context) throws Exception {
        return injectMetadataInDb(sampleMetadataXml, context, false);
    }

    protected AbstractMetadata injectMetadataInDb(Element sampleMetadataXml, ServiceContext context, boolean resfreshHeader) throws Exception {
        String uuid = UUID.randomUUID().toString();
        String schema = schemaManager.autodetectSchema(sampleMetadataXml);
        Xml.selectElement(sampleMetadataXml,
                "iso19139".equals(schema)
                    ? "gmd:fileIdentifier/gco:CharacterString"
                    : "mdb:metadataIdentifier/*/mcc:code/*",
                "iso19139".equals(schema)
                    ? ISO19139SchemaPlugin.allNamespaces.asList()
                    : ISO19115_3_2018SchemaPlugin.allNamespaces.asList())
            .setText(uuid);

        String source = sourceRepository.findAll().get(0).getUuid();
        final Metadata metadata = new Metadata();
        metadata
            .setDataAndFixCR(sampleMetadataXml)
            .setUuid(uuid);
        metadata.getDataInfo()
            .setRoot(sampleMetadataXml.getQualifiedName())
            .setSchemaId(schema)
            .setType(MetadataType.METADATA)
            .setPopularity(1000);
        metadata.getSourceInfo()
            .setOwner(1)
            .setSourceId(source);
        metadata.getHarvestInfo()
            .setHarvested(false);

        return metadataManager.insertMetadata(context, metadata, sampleMetadataXml, IndexingMode.none, false, UpdateDatestamp.NO,
            false, resfreshHeader);
    }
}
