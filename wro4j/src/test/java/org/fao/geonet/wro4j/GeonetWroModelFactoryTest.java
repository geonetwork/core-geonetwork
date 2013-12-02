package org.fao.geonet.wro4j;

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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * Test GeonetWroModelFactory.
 * <p/>
 * User: Jesse
 * Date: 11/22/13
 * Time: 1:03 PM
 */
public class GeonetWroModelFactoryTest {
    @Test
    public void testCreate() throws Exception {

        File jsRoot = ClosureRequireDependencyManagerTest.getJsTestBaseDir();
        String sourcesXml = "<sources><" + GeonetWroModelFactory.JS_SOURCE + " " +
                            GeonetWroModelFactory.WEBAPP_ATT + "=\"\" " +
                            GeonetWroModelFactory.PATH_ON_DISK_EL + "=\"" + jsRoot.getAbsolutePath() + "\"/>" +
                            "<" + GeonetWroModelFactory.CSS_SOURCE + " " +
                            GeonetWroModelFactory.WEBAPP_ATT + "=\"\" " +
                            GeonetWroModelFactory.PATH_ON_DISK_EL + "=\"" + jsRoot.getAbsolutePath() + "\"/>" +
                            "</sources>";
        final WroModel wroModel = createModel(sourcesXml);
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

    private WroModel createModel(String sourcesXml) throws IOException {

        final File wroSources = File.createTempFile("wro-sources", ".xml");
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

        String sourcesXml = "<sources><" + GeonetWroModelFactory.CSS_SOURCE + " " +
                            GeonetWroModelFactory.WEBAPP_ATT + "=\"\" " +
                            GeonetWroModelFactory.PATH_ON_DISK_EL + "=\"" + tmpDir.getRoot().getAbsolutePath() + "\"/>" +
                            "</sources>";

        createModel(sourcesXml);

    }
}
