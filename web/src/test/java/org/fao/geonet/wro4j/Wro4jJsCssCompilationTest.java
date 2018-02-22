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

package org.fao.geonet.wro4j;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import org.fao.geonet.AbstractCoreIntegrationTest;
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.Constants;
import org.fao.geonet.GeonetMockServletContext;
import org.fao.geonet.kernel.GeonetworkDataDirectory;
import org.junit.AfterClass;
import org.junit.Test;
import org.springframework.context.support.GenericXmlApplicationContext;
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

import javax.servlet.FilterConfig;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.assertTrue;

/**
 * @author Jesse on 11/20/2014.
 */
public class Wro4jJsCssCompilationTest {
    private static WroModel wro4jModel;
    private static WroManager wro4jManager;

    public void createModel() throws IOException {
        Path webDir = AbstractCoreIntegrationTest.getWebappDir(Wro4jJsCssCompilationTest.class);
        GeonetworkDataDirectory dataDirectory = new GeonetworkDataDirectory();
        dataDirectory.setSchemaPluginsDir(webDir.resolve("WEB-INF/data/config/schema_plugins"));
        dataDirectory.setFormatterDir(webDir.resolve("WEB-INF/data/data/formatter"));
        GenericXmlApplicationContext applicationContext = new GenericXmlApplicationContext();
        applicationContext.refresh();
        applicationContext.getBeanFactory().registerSingleton("geonetworkDataDirectory", dataDirectory);
        ApplicationContextHolder.set(applicationContext);

        GeonetworkMavenWrojManagerFactory managerFactory = new GeonetworkMavenWrojManagerFactory();
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

    @AfterClass
    static public void tearDown() {
        Context.destroy();
        ApplicationContextHolder.clear();
    }

    @Test
    public void testCssCompilation() throws Exception {
        createModel();
        testResourcesOfType(ResourceType.CSS, Predicates.not(Predicates.or(
            Predicates.equalTo("gn_viewer") // currently broken
        )));
        testResourcesOfType(ResourceType.CSS, Predicates.<String>alwaysTrue());
    }

    @Test
    public void testJsCompilation() throws Exception {
        createModel();
        testResourcesOfType(ResourceType.JS, Predicates.<String>alwaysTrue());
    }

    private void testResourcesOfType(ResourceType resourceType, Predicate<String> testFilter) throws IOException {
        Path webDir = AbstractCoreIntegrationTest.getWebappDir(Wro4jJsCssCompilationTest.class);
        GeonetworkDataDirectory dataDirectory = new GeonetworkDataDirectory();
        dataDirectory.setSchemaPluginsDir(webDir.resolve("WEB-INF/data/config/schema_plugins"));
        dataDirectory.setFormatterDir(webDir.resolve("WEB-INF/data/data/formatter"));
        dataDirectory.setNodeLessFiles(webDir.resolve("WEB-INF/data/node_less_files"));
        GenericXmlApplicationContext applicationContext = new GenericXmlApplicationContext();
        applicationContext.refresh();
        applicationContext.getBeanFactory().registerSingleton("geonetworkDataDirectory", dataDirectory);
        ApplicationContextHolder.set(applicationContext);

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
                    if(t.getMessage()!=null) {
                        errors.append(t.getMessage().replaceAll("(\n|\r)+", "\n        > ")).append("\n");
                    }
                }
            }
        }

        assertTrue(errors.toString(), errors.length() == 0);
    }
}
