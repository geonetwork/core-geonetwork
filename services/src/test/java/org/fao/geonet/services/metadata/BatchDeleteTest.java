package org.fao.geonet.services.metadata;

import com.google.common.collect.Sets;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.AbstractCoreIntegrationTest;
import org.fao.geonet.kernel.SelectionManager;
import org.fao.geonet.kernel.mef.MEFLibIntegrationTest;
import org.fao.geonet.repository.MetadataRepository;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Set;

import static org.junit.Assert.assertEquals;

public class BatchDeleteTest extends AbstractCoreIntegrationTest {

    @Autowired
    private MetadataRepository repository;

    @Test
    public void testExec() throws Exception {
        ServiceContext context = createServiceContext();
        loginAsAdmin(context);

        final MEFLibIntegrationTest.ImportMetadata importMetadata = new MEFLibIntegrationTest.ImportMetadata(this, context);
        importMetadata.getMefFilesToLoad().add("mef2-example-2md.zip");
        importMetadata.invoke();

        assertEquals(3, repository.count());

        Set<String> uuids = Sets.newHashSet();
        for (String id : importMetadata.getMetadataIds()) {
            final String uuid = repository.findOne(Integer.valueOf(id)).getUuid();
            uuids.add(uuid);
        }
        final SelectionManager manager = SelectionManager.getManager(context.getUserSession());
        manager.addAllSelection(SelectionManager.SELECTION_METADATA, uuids);
        final BatchDelete batchDelete = new BatchDelete();
        batchDelete.exec(createParams(), context);

        assertEquals(0, repository.count());

    }
}