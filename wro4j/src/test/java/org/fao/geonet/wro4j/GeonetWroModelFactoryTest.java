package org.fao.geonet.wro4j;

import com.google.common.base.Optional;
import org.apache.commons.io.FileUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import ro.isdc.wro.config.ReadOnlyContext;
import ro.isdc.wro.model.WroModel;
import ro.isdc.wro.model.group.Group;
import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.locator.UriLocator;
import ro.isdc.wro.model.resource.locator.factory.UriLocatorFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.fao.geonet.wro4j.GeonetWroModelFactory.*;
import static org.junit.Assert.*;

/**
 * Test GeonetWroModelFactory.
 * <p/>
 * User: Jesse
 * Date: 11/22/13
 * Time: 1:03 PM
 */
public class GeonetWroModelFactoryTest {

    private static final String PATH_TO_ROOT_OF_TEST_RESOURCES = "wro4j/src/test/resources/org/fao/geonet/wro4j";

    @Test
    public void testCreateUsingRequire() throws Exception {

        String sourcesXml = "<sources><"+ REQUIRE_EL +"><" + JS_SOURCE_EL + " " +
                            WEBAPP_ATT + "=\"\" " +
                            PATH_ON_DISK_ATT + "=\"" + PATH_TO_ROOT_OF_TEST_RESOURCES + "\"/>" +
                            "<" + CSS_SOURCE_EL + " " +
                            WEBAPP_ATT + "=\"\" " +
                            PATH_ON_DISK_ATT + "=\"" + PATH_TO_ROOT_OF_TEST_RESOURCES + "\"/>" +
                            "</"+ REQUIRE_EL +"></sources>";

        final WroModel wroModel = createRequireModel(sourcesXml);
        assertRequireModel(wroModel);
    }
    @Test
    public void testCreateUsingRequireAndGroupHasPathOnDisk() throws Exception {

        String sourcesXml = createSourcesXmlWithPathOnGroup();

        final WroModel wroModel = createRequireModel(sourcesXml);
        assertRequireModel(wroModel);
    }
    @Test
    public void testPathOnDiskIsFullPath() throws Exception {

        String sourcesXml = createSourcesXmlWithPathOnGroup(RequireDependencyManagerTest.getJsTestBaseDir().getAbsolutePath());

        final WroModel wroModel = createRequireModel(sourcesXml);
        assertRequireModel(wroModel);
    }

    @Test
    public void testRelativePathInclude() throws Exception {
        String includeSourcesXML = createSourcesXmlWithPathOnGroup();

        final String prefix = "wro-includes";
        final File wroInclude = File.createTempFile(prefix, ".xml");
        FileUtils.write(wroInclude, includeSourcesXML);

        String mainSourcesXml = "<sources><" + INCLUDE_EL + " file=\"" + wroInclude.getName() + "\"/></sources>";
        File mainSourcesFile = File.createTempFile("wro-sources", ".xml", wroInclude.getParentFile());
        final WroModel wroModel = createRequireModel(mainSourcesXml, Optional.of(mainSourcesFile));

        assertRequireModel(wroModel);
    }

    @Test
    public void testAbsolutePathInclude() throws Exception {
        String includeSourcesXML = createSourcesXmlWithPathOnGroup();

        final String prefix = "wro-includes";
        final File wroInclude = File.createTempFile(prefix, ".xml");
        FileUtils.write(wroInclude, includeSourcesXML);

        String mainSourcesXml = "<sources><" + INCLUDE_EL + " file=\"" + wroInclude.getAbsolutePath() +"\"/></sources>";
        final WroModel wroModel = createRequireModel(mainSourcesXml);

        assertRequireModel(wroModel);
    }

    @Test
    public void testURIPathInclude() throws Exception {
        String includeSourcesXML = createSourcesXmlWithPathOnGroup();

        final String prefix = "wro-includes";
        final File wroInclude = File.createTempFile(prefix, ".xml");
        FileUtils.write(wroInclude, includeSourcesXML);

        String mainSourcesXml = "<sources><" + INCLUDE_EL + " file=\"" + wroInclude.getAbsoluteFile().toURI() +"\"/></sources>";
        final WroModel wroModel = createRequireModel(mainSourcesXml);

        assertRequireModel(wroModel);
    }

    @Test
    public void testClasspathPathInclude() throws Exception {
        String mainSourcesXml = "<sources><" + INCLUDE_EL + " file=\""+CLASSPATH_PREFIX+"included-wro-sources.xml\"/></sources>";
        final WroModel wroModel = createRequireModel(mainSourcesXml);

        assertRequireModel(wroModel);
    }

    @Test
    public void testCreateDeclaredGroups() throws IOException {
        String sourcesXml = "<sources><"+ DECLARATIVE_EL +" " + DECLARATIVE_NAME_ATT + "=\"groupName\"" + " " +
                            PATH_ON_DISK_ATT + "=\"" + PATH_TO_ROOT_OF_TEST_RESOURCES +"\" >" +
                "<" + JS_SOURCE_EL + " " + WEBAPP_ATT + "=\"sampleFile1a.js\" " + PATH_ON_DISK_ATT + "=\"" + PATH_TO_ROOT_OF_TEST_RESOURCES + "\"/>" +
                "<" + JS_SOURCE_EL + " " + WEBAPP_ATT + "=\"jslvl2/sampleFile2a.js\" />" +
                "<" + CSS_SOURCE_EL + " " + WEBAPP_ATT+ "=\"1a.css\"/>" +
                "<" + CSS_SOURCE_EL + " " + WEBAPP_ATT+ "=\"anotherCss.less\" "+ PATH_ON_DISK_ATT + "=\"" + PATH_TO_ROOT_OF_TEST_RESOURCES + "\"/>" +
            "</"+ DECLARATIVE_EL +"></sources>";

        final WroModel wroModel = createRequireModel(sourcesXml);

        assertEquals(1, wroModel.getGroups().size());
        final Group group = wroModel.getGroups().iterator().next();

        assertEquals("groupName", group.getName());
        List<Resource> resources = group.getResources();

        assertEquals(4, resources.size());

        List<String> resourceNames = new ArrayList<String>(resources.size());
        final UriLocatorFactory uriLocatorFactory = new GeonetworkMavenWrojManagerFactory().newUriLocatorFactory();

        for (Resource resource : resources) {
            resourceNames.add(resource.getUri());
            assertCanLoadResource(uriLocatorFactory, resource);

            resourceNames.add(resource.getUri().split(PATH_TO_ROOT_OF_TEST_RESOURCES)[1]);
        }

        assertTrue(resourceNames.contains(("/sampleFile1a.js").replace('\\', '/')));
        assertTrue(resourceNames.contains(("/jslvl2/sampleFile2a.js").replace('\\', '/')));
        assertTrue(resourceNames.contains(("/1a.css").replace('\\', '/')));
        assertTrue(resourceNames.contains(("/anotherCss.less").replace('\\', '/')));

    }

    private WroModel createRequireModel(String sourcesXml) throws IOException {
        return createRequireModel(sourcesXml, Optional.<File>absent());
    }

    private WroModel createRequireModel(String sourcesXml, Optional<File> sourcesFileOption) throws IOException {


        final File wroSources;
        if (sourcesFileOption.isPresent()) {
            wroSources = sourcesFileOption.get();
        } else {
            wroSources = File.createTempFile("wro-sources", ".xml");
        }
        FileUtils.write(wroSources, sourcesXml);

        final File configFile = File.createTempFile("wro", ".properties");
        FileUtils.write(configFile, "wroSources=" + wroSources.getAbsolutePath().replace(File.separatorChar, '/'));

        final GeonetworkMavenWrojManagerFactory managerFactory = new GeonetworkMavenWrojManagerFactory();
        managerFactory.setExtraConfigFile(configFile);
        final GeonetWroModelFactory wroModelFactory = (GeonetWroModelFactory) managerFactory.newModelFactory();
        final ReadOnlyContext context = (ReadOnlyContext) Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),
                new Class[]{ReadOnlyContext.class}, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                return null;
            }
        });
        wroModelFactory.setContext(context);
        wroModelFactory.setGeonetworkRootDirectory(getGeonetworkRootDirectory());
        return wroModelFactory.create();
    }

    private void assertCssFileExists(String cssFileName, List<Resource> resources) {
        boolean exists = false;
        for (Resource resource : resources) {
            if (resource.getUri().endsWith('/' + cssFileName)) {
                exists = true;
                break;
            }
        }
        assertTrue(exists);
    }

    private void assertCanLoadResource(UriLocatorFactory uriLocatorFactory, Resource resource) throws IOException {
        final UriLocator instance = uriLocatorFactory.getInstance(resource.getUri());
        final InputStream locate = instance.locate(resource.getUri());
        try {
            assertNotNull(locate);

            final int read = locate.read();
            assertTrue(-1 != read);
        } finally {
            if (locate != null) {
                locate.close();
            }
        }
    }

    @Rule
    public TemporaryFolder tmpDir = new TemporaryFolder();

    @Test(expected = IllegalArgumentException.class)
    public void testTwoCssSameName() throws Exception {
        FileUtils.write(new File(tmpDir.getRoot(), "cssA.css"), "// cssA.css");
        File subdir = new File(tmpDir.getRoot(), "subdir");
        FileUtils.write(new File(subdir, "cssA.css"), "// cssA2.css");

        String sourcesXml = "<sources><"+ REQUIRE_EL +"><" + CSS_SOURCE_EL + " " +
                            WEBAPP_ATT + "=\"\" " +
                            PATH_ON_DISK_ATT + "=\"" + tmpDir.getRoot().getAbsolutePath() + "\"/>" +
                            "</"+ REQUIRE_EL +"></sources>";

        createRequireModel(sourcesXml);

    }

    private void assertRequireModel(WroModel wroModel) throws IOException {
        Set<String> groupNames = new HashSet<String>();
        final UriLocatorFactory uriLocatorFactory = new GeonetworkMavenWrojManagerFactory().newUriLocatorFactory();
        for (Group group : wroModel.getGroups()) {
            groupNames.add(group.getName());

            final List<Resource> resources = group.getResources();
            if (group.getName().equals("1a")) {
                assertCssFileExists("1a.css", resources);
            }

            boolean hasSelfJsFile = false;
            for (Resource resource : resources) {
                if (resource.getUri().endsWith(group.getName() + ".js")) {
                    hasSelfJsFile = true;
                }
                assertCanLoadResource(uriLocatorFactory, resource);
            }

            if (!group.getName().equals("anotherCss")) {
                assertTrue("Group: '" + group.getName() + "' does not have its js file only its dependencies", hasSelfJsFile);
            } else {
                assertEquals(1, resources.size());
                assertTrue(resources.get(0).getUri().endsWith("anotherCss.less"));
            }
        }

        assertTrue(groupNames.contains("1a"));
        assertTrue(groupNames.contains("1b"));
        assertTrue(groupNames.contains("2a"));
        assertTrue(groupNames.contains("2b"));
        assertTrue(groupNames.contains("3a"));
        assertTrue(groupNames.contains("3b"));
        assertTrue(groupNames.contains("3c"));
    }

    private String createSourcesXmlWithPathOnGroup() {
        return createSourcesXmlWithPathOnGroup(PATH_TO_ROOT_OF_TEST_RESOURCES);
    }
    private String createSourcesXmlWithPathOnGroup(String pathOnDisk) {
        return "<sources><"+ REQUIRE_EL +" "+
               PATH_ON_DISK_ATT + "=\"" + pathOnDisk+ "\"><" + JS_SOURCE_EL + " " +
               WEBAPP_ATT + "=\"\" />" +
               "<" + CSS_SOURCE_EL + " " +
               WEBAPP_ATT + "=\"\" />" +
               "</"+ REQUIRE_EL +"></sources>";
    }

    public String getGeonetworkRootDirectory() {
        final File jsTestBaseDir = RequireDependencyManagerTest.getJsTestBaseDir();
        return GeonetWroModelFactory.findGeonetworkRootDirectory(jsTestBaseDir.getAbsolutePath());
    }
}
