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

package org.fao.geonet.kernel.search;

import jeeves.server.ServiceConfig;
import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;

import org.fao.geonet.AbstractCoreIntegrationTest;
import org.fao.geonet.TestFunction;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.constants.Params;
import org.fao.geonet.domain.User;
import org.fao.geonet.kernel.mef.MEFLibIntegrationTest;
import org.fao.geonet.kernel.search.index.FSDirectoryFactory;
import org.fao.geonet.repository.UserRepository;
import org.fao.geonet.repository.UserRepositoryTest;
import org.jdom.Element;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import javax.annotation.Nonnull;

@ContextConfiguration(inheritLocations = true, locations = "classpath:perf-repository-test-context.xml")
public class LuceneSearcherPerformanceTest extends AbstractCoreIntegrationTest {

    @Autowired
    private SearchManager searchManager;
    @Autowired
    private UserRepository userRepository;

    @Test
    @Ignore
    public void testSearchAndPresent() throws Exception {
        final ServiceContext context = createServiceContext();
        loginAsAdmin(context);


        final MEFLibIntegrationTest.ImportMetadata importMetadata = new MEFLibIntegrationTest.ImportMetadata(this, context);
        importMetadata.setUuidAction(Params.GENERATE_UUID);
        importMetadata.getMefFilesToLoad().add("mef2-example-2md.zip");
        importMetadata.invoke(100);
        searchManager.forceIndexChanges();

//        loginAsNewUser(context);

        measurePerformance(searchAndPresent(context));
    }

    private void loginAsNewUser(ServiceContext context) {
        final UserSession session = new UserSession();
        context.setUserSession(session);
        User user = UserRepositoryTest.newUser(_inc);
        user = userRepository.save(user);
        session.loginAs(user);
    }

    public TestFunction searchAndPresent(final ServiceContext context) throws Exception {
        return new TestFunction() {

            @Override
            public void exec() throws Exception {
                try (MetaSearcher searcher = searchManager.newSearcher(SearcherType.LUCENE, Geonet.File.SEARCH_LUCENE)) {

                    Element request = new Element("request").addContent(Arrays.asList(
                        new Element("fast").setText("index"),
                        new Element("from").setText("1"),
                        new Element("to").setText("10")
                    ));

                    searcher.search(context, request, new ServiceConfig());
                    final Element results = searcher.present(context, request, new ServiceConfig());
//                System.out.println(results.getChild("summary").getAttributeValue("count"));
                }
            }
        };
    }

    public static class PerfDirectoryFactory extends FSDirectoryFactory {
        Path luceneTempDir;

        @Override
        public synchronized void init() throws IOException {
            if (taxonomyFile == null) {
                luceneTempDir = Files.createTempDirectory("luceneTempDir");

                indexFile = luceneTempDir.resolve(NON_SPATIAL_DIR);
                taxonomyFile = luceneTempDir.resolve(TAXONOMY_DIR);

                Files.createDirectories(indexFile);
                Files.createDirectories(taxonomyFile);
            }
        }

        @Nonnull
        @Override
        protected Path createNewIndexDirectory(String baseName) throws IOException {
            return luceneTempDir.resolve(baseName);
        }
    }
}
