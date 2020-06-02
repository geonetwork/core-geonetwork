package org.fao.geonet.wro4j;


import com.google.common.base.Optional;
import com.google.common.collect.Sets;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import ro.isdc.wro.config.Context;
import ro.isdc.wro.config.ReadOnlyContext;
import ro.isdc.wro.manager.factory.standalone.StandaloneContext;
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
import java.net.URLEncoder;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.http.client.utils.URIUtils;
import org.eclipse.jetty.util.URIUtil;

import static org.fao.geonet.wro4j.ClosureDependencyUriLocator.PATH_TO_WEBAPP_BASE_FROM_CLOSURE_BASE_JS_FILE;
import static org.fao.geonet.wro4j.GeonetWroModelFactory.CLASSPATH_PREFIX;
import static org.fao.geonet.wro4j.GeonetWroModelFactory.CSS_SOURCE_EL;
import static org.fao.geonet.wro4j.GeonetWroModelFactory.GROUP_NAME_CLOSURE_DEPS;
import static org.fao.geonet.wro4j.GeonetWroModelFactory.INCLUDE_EL;
import static org.fao.geonet.wro4j.GeonetWroModelFactory.JS_SOURCE_EL;
import static org.fao.geonet.wro4j.GeonetWroModelFactory.PATH_ON_DISK_ATT;
import static org.fao.geonet.wro4j.GeonetWroModelFactory.REQUIRE_EL;
import static org.fao.geonet.wro4j.GeonetWroModelFactory.WEBAPP_ATT;
import static org.fao.geonet.wro4j.GeonetWroModelFactory.WRO_SOURCES_KEY;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Test GeonetWroModelFactory.
 * <p/>
 * User: Jesse Date: 11/22/13 Time: 1:03 PM
 */
public class GeonetWroModelFactoryTest {

    private static final String PATH_TO_ROOT_OF_TEST_RESOURCES = "wro4j/src/test/resources/org/fao/geonet/wro4j";
    private static final String TEMPLATE_URI_PREFIX = "template://";
    @Rule
    public TemporaryFolder tmpDir = new TemporaryFolder();

    public static GeonetworkMavenWrojManagerFactory createManagerFactory() throws IOException {
        final GeonetworkMavenWrojManagerFactory wrojManagerFactory = new GeonetworkMavenWrojManagerFactory();
        File propertiesFile = File.createTempFile("abc", "properties");
        Files.write(propertiesFile.toPath(), (WRO_SOURCES_KEY + "=\n").getBytes("UTF-8"));
        wrojManagerFactory.setExtraConfigFile(propertiesFile);
        Context.set(Context.standaloneContext());
        wrojManagerFactory.initialize(new StandaloneContext());
        return wrojManagerFactory;
    }

    @Test
    public void testCreateUsingRequire() throws Exception {

        String sourcesXml = "<sources><" + REQUIRE_EL + "><" + JS_SOURCE_EL + " " +
            WEBAPP_ATT + "=\"\" " +
            PATH_ON_DISK_ATT + "=\"" + PATH_TO_ROOT_OF_TEST_RESOURCES + "\"/>" +
            "<" + CSS_SOURCE_EL + " " +
            WEBAPP_ATT + "=\"\" " +
            PATH_ON_DISK_ATT + "=\"" + PATH_TO_ROOT_OF_TEST_RESOURCES + "\"/>" +
            "</" + REQUIRE_EL + "></sources>";

        final WroModel wroModel = createRequireModel(sourcesXml);
        assertRequireModel(wroModel, false);
    }

    @Test
    public void testCreateUsingRequireAndGroupHasPathOnDisk() throws Exception {

        String sourcesXml = createSourcesXmlWithPathOnGroup();

        final WroModel wroModel = createRequireModel(sourcesXml);
        assertRequireModel(wroModel);
    }

    @Test
    public void testPathOnDiskIsFullPath() throws Exception {

        String sourcesXml = createSourcesXmlWithPathOnGroup(ClosureRequireDependencyManagerTest.getJsTestBaseDir().getAbsolutePath());

        final WroModel wroModel = createRequireModel(sourcesXml);
        assertRequireModel(wroModel);
    }

    @Test
    public void testClosureJsGroup() throws Exception {

        final String jsTestBaseDir = ClosureRequireDependencyManagerTest.getJsTestBaseDir().getAbsolutePath();
        String sourcesXml = createSourcesXmlWithPathOnGroup(jsTestBaseDir);

        final WroModel wroModel = createRequireModel(sourcesXml);

        final Collection<Group> groups = wroModel.getGroups();

        Group depsGroup = null;
        for (Group group : groups) {
            if (group.getName().equals(GeonetWroModelFactory.GROUP_NAME_CLOSURE_DEPS)) {
                assertNull("There should only be one deps group but found at least 2", depsGroup);
                depsGroup = group;
            }
        }

        assertNotNull(depsGroup);

        final List<Resource> resources = depsGroup.getResources();

        String jsTestBaseDirAsPath = jsTestBaseDir.replace('\\', '/');
        if (jsTestBaseDirAsPath.charAt(0) == '/') {
            jsTestBaseDirAsPath = jsTestBaseDirAsPath.substring(1);

            // let's encode the filesystem path as it should be encoded by wro
            jsTestBaseDirAsPath = URIUtil.encodePath(jsTestBaseDirAsPath);

        }


        assertEquals(7, resources.size());
        for (Resource resource : resources) {
            final UriLocatorFactory uriLocatorFactory = createManagerFactory().newUriLocatorFactory();
            final UriLocator instance = uriLocatorFactory.getInstance(resource.getUri());
            final String deps = IOUtils.toString(instance.locate(resource.getUri()), "UTF-8");
            if (resource.getUri().contains("sampleFile1a.js")) {
                assertEquals("goog.addDependency('" + PATH_TO_WEBAPP_BASE_FROM_CLOSURE_BASE_JS_FILE + "/" + jsTestBaseDirAsPath +
                    "/sampleFile1a.js', ['1a'], ['2a']);\n", deps);
            } else if (resource.getUri().contains("sampleFile1b.js")) {
                assertEquals("goog.addDependency('" + PATH_TO_WEBAPP_BASE_FROM_CLOSURE_BASE_JS_FILE + "/" + jsTestBaseDirAsPath +
                    "/sampleFile1b.js', ['1b'], ['3c','1a','2a']);\n", deps);
            } else if (resource.getUri().contains("sampleFile2a.js")) {
                assertEquals("goog.addDependency('" + PATH_TO_WEBAPP_BASE_FROM_CLOSURE_BASE_JS_FILE + "/" + jsTestBaseDirAsPath +
                    "/jslvl2/sampleFile2a.js', ['2a'], ['3a','2b'," +
                    "'3c','3b']);\n", deps);
            } else if (resource.getUri().contains("sampleFile2b.js")) {
                assertEquals("goog.addDependency('" + PATH_TO_WEBAPP_BASE_FROM_CLOSURE_BASE_JS_FILE + "/" + jsTestBaseDirAsPath +
                    "/jslvl2/sampleFile2b.js', ['2b'], []);\n", deps);
            } else if (resource.getUri().contains("sampleFile3a.js")) {
                assertEquals("goog.addDependency('" + PATH_TO_WEBAPP_BASE_FROM_CLOSURE_BASE_JS_FILE + "/" + jsTestBaseDirAsPath +
                    "/jslvl2/jslvl3/sampleFile3a.js', ['3a'], []);\n", deps);
            } else if (resource.getUri().contains("sampleFile3b.js")) {
                assertEquals("goog.addDependency('" + PATH_TO_WEBAPP_BASE_FROM_CLOSURE_BASE_JS_FILE + "/" + jsTestBaseDirAsPath +
                    "/jslvl2/jslvl3/sampleFile3b.js', ['3b'], []);\n", deps);
            } else if (resource.getUri().contains("sampleFile3c.js")) {
                assertEquals("goog.addDependency('" + PATH_TO_WEBAPP_BASE_FROM_CLOSURE_BASE_JS_FILE + "/" + jsTestBaseDirAsPath +
                    "/jslvl2/jslvl3/sampleFile3c.js', ['3c'], []);\n", deps);
            }
        }

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

        String mainSourcesXml = "<sources><" + INCLUDE_EL + " file=\"" + wroInclude.getAbsolutePath() + "\"/></sources>";
        final WroModel wroModel = createRequireModel(mainSourcesXml);

        assertRequireModel(wroModel);
    }

    @Test
    public void testURIPathInclude() throws Exception {
        String includeSourcesXML = createSourcesXmlWithPathOnGroup();

        final String prefix = "wro-includes";
        final File wroInclude = File.createTempFile(prefix, ".xml");
        FileUtils.write(wroInclude, includeSourcesXML);

        String mainSourcesXml = "<sources><" + INCLUDE_EL + " file=\"" + wroInclude.getAbsoluteFile().toURI() + "\"/></sources>";
        final WroModel wroModel = createRequireModel(mainSourcesXml);

        assertRequireModel(wroModel);
    }

    @Test
    public void testClasspathPathInclude() throws Exception {
        String mainSourcesXml = "<sources><" + INCLUDE_EL + " file=\"" + CLASSPATH_PREFIX + "included-wro-sources.xml\"/></sources>";
        final WroModel wroModel = createRequireModel(mainSourcesXml);

        assertRequireModel(wroModel);
    }

    @Test
    public void testCreateDeclaredGroups() throws Exception {
        String sourcesXml = "<sources>\n"
            + "    <declarative name=\"groupName\" pathOnDisk=\"wro4j/src/test/resources/org/fao/geonet/wro4j\">\n"
            + "        <jsSource webappPath=\"sampleFile1a.js\" pathOnDisk=\"" + PATH_TO_ROOT_OF_TEST_RESOURCES + "\"/>\n"
            + "        <jsSource webappPath=\"jslvl2/sampleFile2a.js\" minimize=\"false\"/>\n"
            + "        <cssSource webappPath=\"1a.css\" minimize=\"false\"/>\n"
            + "        <cssSource webappPath=\"anotherCss.less\" pathOnDisk=\"" + PATH_TO_ROOT_OF_TEST_RESOURCES +
            "\"/>\n"
            + "    </declarative>\n"
            + "</sources>";

        final WroModel wroModel = createRequireModel(sourcesXml);

        assertEquals(1, wroModel.getGroups().size());
        final Group group = wroModel.getGroups().iterator().next();

        assertEquals("groupName", group.getName());
        List<Resource> resources = group.getResources();

        assertEquals(4, resources.size());

        List<String> resourceNames = new ArrayList<String>(resources.size());
        final UriLocatorFactory uriLocatorFactory = createManagerFactory().newUriLocatorFactory();

        for (Resource resource : resources) {
            resourceNames.add(resource.getUri());
            assertCanLoadResource(uriLocatorFactory, resource);

            resourceNames.add(resource.getUri().split(PATH_TO_ROOT_OF_TEST_RESOURCES)[1]);

            if (resource.getUri().endsWith("jslvl2/sampleFile2a.js") || resource.getUri().endsWith("1a.css")) {
                assertFalse(resource.isMinimize());
            } else {
                assertTrue(resource.isMinimize());
            }
        }

        assertTrue(resourceNames.contains(("/sampleFile1a.js").replace('\\', '/')));
        assertTrue(resourceNames.contains(("/jslvl2/sampleFile2a.js").replace('\\', '/')));
        assertTrue(resourceNames.contains(("/1a.css").replace('\\', '/')));
        assertTrue(resourceNames.contains(("/anotherCss.less").replace('\\', '/')));

    }

    private WroModel createRequireModel(String sourcesXml) throws Exception {
        return createRequireModel(sourcesXml, Optional.<File>absent());
    }

    private WroModel createRequireModel(String sourcesXml, Optional<File> sourcesFileOption) throws Exception {


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

    @Test
    public void testTwoCssSameName() throws Exception {
        FileUtils.write(new File(tmpDir.getRoot(), "cssA.css"), "// cssA.css");
        File subdir = new File(tmpDir.getRoot(), "subdir");
        FileUtils.write(new File(subdir, "cssA.css"), "// cssA2.css");

        String sourcesXml = "<sources><" + REQUIRE_EL + "><" + CSS_SOURCE_EL + " " +
            WEBAPP_ATT + "=\"\" " +
            PATH_ON_DISK_ATT + "=\"" + tmpDir.getRoot().getAbsolutePath() + "\"/>" +
            "</" + REQUIRE_EL + "></sources>";

        createRequireModel(sourcesXml);

    }

    private void assertRequireModel(WroModel wroModel) throws IOException {
        assertRequireModel(wroModel, true);
    }

    private void assertRequireModel(WroModel wroModel, boolean testMinimized) throws IOException {
        Set<String> groupNames = new HashSet<String>();
        final GeonetworkMavenWrojManagerFactory wrojManagerFactory = createManagerFactory();
        final UriLocatorFactory uriLocatorFactory = wrojManagerFactory.newUriLocatorFactory();

        Set<String> nonMinifiedFiles = Sets.newHashSet("1a.css", "sampleFile2a.js", "sampleFile1b.js");

        for (Group group : wroModel.getGroups()) {
            groupNames.add(group.getName());

            final List<Resource> resources = group.getResources();
            if (group.getName().equals("1a")) {
                assertCssFileExists("1a.css", resources);
            }

            boolean hasSelfJsFile = false;
            for (Resource resource : resources) {
                final String uri = resource.getUri();
                if (uri.endsWith(group.getName() + ".js")) {
                    hasSelfJsFile = true;
                }
                assertCanLoadResource(uriLocatorFactory, resource);

                if (testMinimized) {
                    if (group.getName().equals(GROUP_NAME_CLOSURE_DEPS) ||
                        nonMinifiedFiles.contains(uri.substring(uri.lastIndexOf("/") + 1)) ||
                        resource.getUri().startsWith(TEMPLATE_URI_PREFIX)) {
                        assertFalse(resource.getUri() + " was minimized but should not be", resource.isMinimize());
                    } else {
                        assertTrue(resource.getUri() + " was not minimized but should be", resource.isMinimize());
                    }
                }
            }

            // closureDeps group is fully tested in a dedicated tests so can ignore it here
            if (!group.getName().equals(GROUP_NAME_CLOSURE_DEPS)) {
                if (!group.getName().equals("anotherCss")) {
                    assertTrue("Group: '" + group.getName() + "' does not have its js file only its dependencies", hasSelfJsFile);
                } else {
                    assertEquals(1, resources.size());
                    assertTrue(resources.get(0).getUri().endsWith("anotherCss.less"));
                }
            }
        }

        assertTrue(groupNames.contains("1a"));
        assertTrue(groupNames.contains("1b"));
        assertTrue(groupNames.contains("2a"));
        assertTrue(groupNames.contains("2b"));
        assertTrue(groupNames.contains("3a"));
        assertTrue(groupNames.contains("3b"));
        assertTrue(groupNames.contains("3c"));
        assertTrue(groupNames.contains(GROUP_NAME_CLOSURE_DEPS));
    }

    private String createSourcesXmlWithPathOnGroup() {
        return createSourcesXmlWithPathOnGroup(PATH_TO_ROOT_OF_TEST_RESOURCES);
    }

    private String createSourcesXmlWithPathOnGroup(String pathOnDisk) {
        return "<sources xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"
            + "         xsi:noNamespaceSchemaLocation=\"../../../../web/src/main/webResources/WEB-INF/wro-sources.xsd\">\n"
            + "    <require pathOnDisk=\"" + pathOnDisk + "\">\n"
            + "        <jsSource webappPath=\"\">\n"
            + "            <notMinimized>\n"
            + "                <file>sampleFile1b.js</file>\n"
            + "                <file>jslvl2/sampleFile2a.js</file>\n"
            + "            </notMinimized>\n"
            + "        </jsSource>\n"
            + "        <cssSource webappPath=\"\">\n"
            + "            <notMinimized>\n"
            + "                <file>1a.css</file>\n"
            + "            </notMinimized>\n"
            + "        </cssSource>\n"
            + "    </require>\n"
            + "</sources>";
    }

    public String getGeonetworkRootDirectory() throws Exception {
        final File jsTestBaseDir = ClosureRequireDependencyManagerTest.getJsTestBaseDir();
        return GeonetWroModelFactory.findGeonetworkRootDirectory(jsTestBaseDir.getAbsolutePath());
    }
}
