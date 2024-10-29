package org.fao.geonet.kernel;

import jeeves.server.context.ServiceContext;
import org.fao.geonet.AbstractCoreIntegrationTest;
import org.fao.geonet.domain.AbstractMetadata;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.domain.MetadataType;
import org.fao.geonet.kernel.datamanager.IMetadataManager;
import org.fao.geonet.kernel.search.IndexingMode;
import org.fao.geonet.kernel.search.submission.DirectIndexSubmittor;
import org.fao.geonet.repository.SourceRepository;
import org.jdom.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import java.util.UUID;

import static org.fao.geonet.domain.MetadataType.METADATA;
import static org.fao.geonet.kernel.UpdateDatestamp.NO;

@ContextConfiguration(
    locations = {"classpath:mocked-core-repository-test-context.xml"}
)
public abstract class AbstractIntegrationTestWithMockedSingletons extends AbstractCoreIntegrationTest {

    private static final int TEST_OWNER_ID = 42;

    @Autowired
    private IMetadataManager metadataManager;

    @Autowired
    private SchemaManager schemaManager;

    @Autowired
    private SourceRepository sourceRepository;

    @Autowired
    protected SpringLocalServiceInvoker springLocalServiceInvoker;

    protected AbstractMetadata insertTemplateResourceInDb(ServiceContext serviceContext, Element element) throws Exception {
        return insertTemplateResourceInDb(serviceContext, element, METADATA);
    }

    protected AbstractMetadata insertTemplateResourceInDb(ServiceContext serviceContext, Element element, MetadataType type) throws Exception {
        loginAsAdmin(serviceContext);

        Metadata metadata = new Metadata();
        metadata.setDataAndFixCR(element)
            .setUuid(UUID.randomUUID().toString());
        metadata.getDataInfo()
            .setRoot(element.getQualifiedName())
            .setSchemaId(schemaManager.autodetectSchema(element))
            .setType(type)
            .setPopularity(1000);
        metadata.getSourceInfo()
            .setOwner(TEST_OWNER_ID)
            .setSourceId(sourceRepository.findAll().get(0).getUuid());
        metadata.getHarvestInfo()
            .setHarvested(false);

        return metadataManager.insertMetadata(
            serviceContext,
            metadata,
            element,
            IndexingMode.full,
            false,
            NO,
            false,
            DirectIndexSubmittor.INSTANCE);
    }
}
