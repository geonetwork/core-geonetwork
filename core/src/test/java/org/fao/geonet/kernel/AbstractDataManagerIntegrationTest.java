package org.fao.geonet.kernel;

import com.google.common.base.Optional;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.AbstractCoreIntegrationTest;
import org.fao.geonet.constants.Params;
import org.fao.geonet.domain.AbstractMetadata;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.domain.MetadataType;
import org.fao.geonet.domain.ReservedGroup;
import org.fao.geonet.kernel.datamanager.IMetadataManager;
import org.fao.geonet.repository.MetadataCategoryRepository;
import org.fao.geonet.repository.MetadataRepository;
import org.fao.geonet.repository.Updater;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.springframework.beans.factory.annotation.Autowired;

import jakarta.annotation.Nonnull;

import java.io.ByteArrayInputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public abstract class AbstractDataManagerIntegrationTest extends AbstractCoreIntegrationTest {

    @Autowired
    protected DataManager dataManager;

    @Autowired
    protected IMetadataManager metadataManager;

    @Autowired
    protected MetadataRepository metadataRepository;

    @Autowired
    protected MetadataCategoryRepository metadataCategoryRepository;

    protected void doSetHarvesterDataTest() throws Exception {
        ServiceContext serviceContext = createContextAndLogAsAdmin();
        int metadataId = importMetadata(serviceContext);

        AbstractMetadata metadata = metadataRepository.findById(metadataId).get();

        assertNull(metadata.getHarvestInfo().getUuid());
        assertNull(metadata.getHarvestInfo().getUri());
        assertFalse(metadata.getHarvestInfo().isHarvested());

        String harvesterUuid = "harvesterUuid";
        dataManager.setHarvestedExt(metadataId, harvesterUuid);
        metadata = metadataRepository.findById(metadataId).get();
        assertEquals(harvesterUuid, metadata.getHarvestInfo().getUuid());
        assertTrue(metadata.getHarvestInfo().isHarvested());
        assertNull(metadata.getHarvestInfo().getUri());


        String newSource = "newSource";
        // check that another update doesn't break the last setting
        // there used to a bug where this was the case because entity manager wasn't being flushed
        metadataRepository.update(metadataId, new Updater<Metadata>() {
            @Override
            public void apply(@Nonnull Metadata entity) {
                entity.getSourceInfo().setSourceId(newSource);
            }
        });

        assertEquals(newSource, metadata.getSourceInfo().getSourceId());
        assertEquals(harvesterUuid, metadata.getHarvestInfo().getUuid());
        assertTrue(metadata.getHarvestInfo().isHarvested());
        assertNull(metadata.getHarvestInfo().getUri());

        String harvesterUuid2 = "harvesterUuid2";
        String harvesterUri = "harvesterUri";
        dataManager.setHarvestedExt(metadataId, harvesterUuid2, Optional.of(harvesterUri));
        metadata = metadataRepository.findById(metadataId).get();
        assertEquals(harvesterUuid2, metadata.getHarvestInfo().getUuid());
        assertTrue(metadata.getHarvestInfo().isHarvested());
        assertEquals(harvesterUri, metadata.getHarvestInfo().getUri());

        dataManager.setHarvestedExt(metadataId, null);
        metadata = metadataRepository.findById(metadataId).get();
        assertNull(metadata.getHarvestInfo().getUuid());
        assertNull(metadata.getHarvestInfo().getUri());
        assertFalse(metadata.getHarvestInfo().isHarvested());
    }

    protected int importMetadata(ServiceContext serviceContext) throws Exception {
        Element sampleMetadataXml = getSampleMetadataXml();
        ByteArrayInputStream stream = new ByteArrayInputStream(Xml.getString(sampleMetadataXml).getBytes("UTF-8"));
        return importMetadataXML(serviceContext, "uuid", stream, MetadataType.METADATA,
                ReservedGroup.all.getId(), Params.GENERATE_UUID);
    }

    protected int importMetadata(ServiceContext serviceContext, String uuid) throws Exception {
        Element sampleMetadataXml = getSampleMetadataXml();
        ByteArrayInputStream stream = new ByteArrayInputStream(Xml.getString(sampleMetadataXml).getBytes("UTF-8"));
        return importMetadataXML(serviceContext, uuid, stream, MetadataType.METADATA,
                ReservedGroup.all.getId(), Params.NOTHING);
    }

    protected ServiceContext createContextAndLogAsAdmin() throws Exception {
        ServiceContext context = createServiceContext();
        loginAsAdmin(context);
        return context;
    }

}
