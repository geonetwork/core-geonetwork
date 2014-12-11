package org.fao.geonet.wro4j;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import org.fao.geonet.AbstractCoreIntegrationTest;
import org.fao.geonet.Constants;
import org.fao.geonet.GeonetMockServletContext;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.mock.web.MockFilterConfig;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import ro.isdc.wro.config.Context;
import ro.isdc.wro.manager.WroManager;
import ro.isdc.wro.manager.factory.standalone.StandaloneContext;
import ro.isdc.wro.model.WroModel;
import ro.isdc.wro.model.group.Group;
import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.ResourceType;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import javax.servlet.FilterConfig;

import static org.junit.Assert.assertTrue;

/**
 * @author Jesse on 11/20/2014.
 */
public class TestWro4jJsCssCompilation {
    private static WroModel wro4jModel;
    private static WroManager wro4jManager;

    @BeforeClass
    public static void createModel() throws IOException {

        GeonetworkMavenWrojManagerFactory managerFactory = new GeonetworkMavenWrojManagerFactory();
        Path webDir = AbstractCoreIntegrationTest.getWebappDir(TestWro4jJsCssCompilation.class);
        final Charset cs = Charset.forName(Constants.ENCODING);
        final List<String> configuration = Files.readAllLines(webDir.resolve("../webResources/WEB-INF/wro.properties"), cs);

        final Path wroProperties = Files.createTempFile("wro", ".properties");
        try (BufferedWriter writer = Files.newBufferedWriter(wroProperties, cs)) {
            for (String line : configuration) {
                final String updatedLine = line.replace("${wroRefresh}", "-1").replace("${debugProcessors}", "").
                        replace("${build.webapp.resources}", webDir.toString().replace("\\", "/"));
                writer.write(updatedLine);
                writer.write("\n");
            }
        }

        managerFactory.setExtraConfigFile(wroProperties.toFile());
        final Context context = Context.standaloneContext();
        Context.set(context);

        managerFactory.initialize(new StandaloneContext());
        wro4jManager = managerFactory.create();
//
//        final GeonetWroModelFactory wroModelFactory = (GeonetWroModelFactory) ((DefaultWroModelFactoryDecorator) wro4jManager.getModelFactory()).getOriginalDecoratedObject();
//        wroModelFactory.setContext(context);

        wro4jModel = wro4jManager.getModelFactory().create();
    }


    @Test
    public void testCssCompilation() throws Exception {
//        testResourcesOfType(ResourceType.CSS, Predicates.not(Predicates.or(
//                Predicates.equalTo("gn_viewer") // currently broken
//                )));
        testResourcesOfType(ResourceType.CSS, Predicates.<String>alwaysTrue());
    }
    @Test
    public void testJsCompilation() throws Exception {
        testResourcesOfType(ResourceType.JS, Predicates.<String>alwaysTrue());
    }

    private void testResourcesOfType(ResourceType resourceType, Predicate<String> testFilter) throws IOException {
        final Collection<Group> groups = wro4jModel.getGroups();
        StringBuilder errors = new StringBuilder();
        for (Group group : groups) {
            if (!testFilter.apply(group.getName())) {
                continue;
            }
            List<Resource> resources = group.collectResourcesOfType(resourceType).getResources();


            if (!resources.isEmpty()) {
                final String requestURI = "http://server.com/" + group.getName() + "." + resourceType.name().toLowerCase();
                MockHttpServletRequest request = new MockHttpServletRequest("GET", requestURI);
                MockHttpServletResponse response = new MockHttpServletResponse();
                GeonetMockServletContext context = new GeonetMockServletContext();
                context.setTestClass(getClass());
                FilterConfig config = new MockFilterConfig(context);


                Context.set(Context.webContext(request, response, config));
                try {
                    wro4jManager.process();
                } catch (Throwable t) {
                    if (errors.length() == 0) {
                        errors.append("\n\nThe following errors were encountered while compiling the ").
                                append(resourceType).append(" resources");
                    }

                    errors.append("\n* Group Name: ").append(group.getName());
                    errors.append("\n    * Resources: ").append(group.getName());
                    for (Resource resource : resources) {
                        errors.append("\n        - ").append(resource.getUri()).append("\n");
                    }
                    errors.append("    * Error Message:\n        > ");
                    errors.append(t.getMessage().replaceAll("(\n|\r)+", "\n        > ")).append("\n");
                }
            }
        }

        assertTrue(errors.toString(), errors.length() == 0);
    }
}
