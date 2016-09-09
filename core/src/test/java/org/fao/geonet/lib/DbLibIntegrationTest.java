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

package org.fao.geonet.lib;

import jeeves.server.context.ServiceContext;

import org.fao.geonet.AbstractCoreIntegrationTest;
import org.fao.geonet.repository.MetadataCategoryRepository;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.nio.file.Path;

import static org.junit.Assert.assertTrue;

/**
 * Verify that dbtest has a transaction when needed.
 *
 * Created by Jesse on 3/10/14.
 */
public class DbLibIntegrationTest extends AbstractCoreIntegrationTest {

    @Autowired
    private MetadataCategoryRepository _metadataCategoryRepository;

    @Test
    public void testInsertData() throws Exception {
        TransactionlessTesting.get().run(new TestTask() {
            @Override
            public void run() throws Exception {
                ServiceContext context = createServiceContext();
                Path filePath = getClassFile(DbLibIntegrationTest.class).getParentFile().toPath();
                String filePrefix = "db-test-";
                new DbLib().insertData(null, context, getWebappDir(DbLibIntegrationTest.class), filePath, filePrefix);
            }
        });

        assertTrue(_metadataCategoryRepository.exists(832983245));
    }
}
