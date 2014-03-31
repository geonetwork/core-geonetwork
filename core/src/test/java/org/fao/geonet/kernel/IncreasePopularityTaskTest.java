package org.fao.geonet.kernel;

import static org.junit.Assert.*;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.AbstractCoreIntegrationTest;
import org.fao.geonet.constants.Params;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.domain.MetadataType;
import org.fao.geonet.domain.ReservedGroup;
import org.fao.geonet.repository.MetadataRepository;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.ByteArrayInputStream;

/**
 * Verify that the task works and has a transaction available to it as needed
 * Created by Jesse on 3/11/14.
 */
public class IncreasePopularityTaskTest extends AbstractCoreIntegrationTest {

    @Autowired
    private MetadataRepository _metadataRepository;

    @Test
    public void testRun() throws Exception {
        final int[] metadataId = new int[1];
        // Setup the data in the database
        TransactionlessTesting.get().run(new TestTask() {
            @Override
            public void run() throws Exception {
                final ServiceContext serviceContext = createServiceContext();
                loginAsAdmin(serviceContext);

                final Element sampleMetadataXml = getSampleMetadataXml();
                final ByteArrayInputStream stream = new ByteArrayInputStream(Xml.getString(sampleMetadataXml).getBytes("UTF-8"));
                metadataId[0] = importMetadataXML(serviceContext, "uuidSetStatus", stream, MetadataType.METADATA,
                        ReservedGroup.all.getId(), Params.GENERATE_UUID);

            }
        });
        final int popularity = _metadataRepository.findOne(metadataId[0]).getDataInfo().getPopularity();

        TransactionlessTesting.get().run(new TestTask() {
            @Override
            public void run() throws Exception {
                final IncreasePopularityTask task = _applicationContext.getBean(IncreasePopularityTask.class);
                task.setMetadataId(metadataId[0]);
                task.run();

                assertEquals(popularity, _metadataRepository.findOne(metadataId[0]).getDataInfo().getPopularity());
            }
        });

        // verify that it is committed
        assertEquals(popularity, _metadataRepository.findOne(metadataId[0]).getDataInfo().getPopularity());
    }
}
