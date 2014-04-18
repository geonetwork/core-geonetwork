package org.fao.geonet.lib;

import jeeves.server.context.ServiceContext;
import org.fao.geonet.AbstractCoreIntegrationTest;
import org.fao.geonet.repository.MetadataCategoryRepository;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

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
                String filePath = getClassFile(DbLibIntegrationTest.class).getParentFile().getPath();
                String filePrefix = "db-test-";
                new DbLib().insertData(null, context, getWebappDir(DbLibIntegrationTest.class), filePath, filePrefix);
            }
        });

        assertTrue(_metadataCategoryRepository.exists(832983245));
    }
}
