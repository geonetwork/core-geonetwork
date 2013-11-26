package org.fao.geonet.wro4j;

import static org.junit.Assert.*;
import org.apache.commons.io.FileUtils;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.junit.Test;
import ro.isdc.wro.config.Context;
import ro.isdc.wro.config.ReadOnlyContext;
import ro.isdc.wro.model.WroModel;
import ro.isdc.wro.model.factory.WroModelFactory;
import ro.isdc.wro.model.group.Group;
import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.locator.UriLocator;
import ro.isdc.wro.model.resource.locator.factory.UriLocatorFactory;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
        final GeonetworkMavenWrojManagerFactory managerFactory = new GeonetworkMavenWrojManagerFactory();

        final File wroSources = File.createTempFile("wro-sources", ".xml");
        File jsRoot = ClosureRequireDependencyManagerTest.getJsTestBaseDir();
        Element sourcesXml = new Element("sources")
                .addContent(
                        new Element(GeonetWroModelFactory.JS_SOURCE)
                                .setAttribute(GeonetWroModelFactory.WEBAPP_ATT, "")
                                .setAttribute(GeonetWroModelFactory.PATH_ON_DISK_EL, jsRoot.getAbsolutePath()));
        FileUtils.write(wroSources, Xml.getString(sourcesXml));

        final File configFile = File.createTempFile("wro", ".properties");
        FileUtils.write(configFile, "wroSources="+wroSources.getAbsolutePath().replace(File.separatorChar, '/'));
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
        final WroModel wroModel = wroModelFactory.create();
        Set<String> groupNames = new HashSet<String>();
        final UriLocatorFactory uriLocatorFactory = managerFactory.newUriLocatorFactory();
        for (Group group : wroModel.getGroups()) {
            groupNames.add(group.getName());
            final List<Resource> resources = group.getResources();
            for (Resource resource : resources) {
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
        }

        assertTrue(groupNames.contains("1a"));
        assertTrue(groupNames.contains("1b"));
        assertTrue(groupNames.contains("2a"));
        assertTrue(groupNames.contains("2b"));
        assertTrue(groupNames.contains("3a"));
        assertTrue(groupNames.contains("3b"));
        assertTrue(groupNames.contains("3c"));
    }


}
