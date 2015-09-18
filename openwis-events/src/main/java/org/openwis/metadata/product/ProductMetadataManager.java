package org.openwis.metadata.product;

import org.apache.commons.lang.StringUtils;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.Metadata;
import org.openwis.products.client.ProductMetadata;
import org.openwis.products.client.ProductMetadataClient;
import org.openwis.products.client.ProductMetadataConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Component;

/**
 * Product metadata manager. Interacts with the Product Metadata web services.
 *
 */
@Component
public class ProductMetadataManager implements IProductMetadataManager {
    @Override
    public ProductMetadata getProductMetadataByUrn(String urn) {
        return getServiceClient().retrieveProductMetadataByUrn(urn);
    }

    @Override
    public void saveOrUpdate(ProductMetadata pm) {
        if (pm.getId() == null) {
            getServiceClient().createProductMetadata(pm);
        } else {
            getServiceClient().updateProductMetadata(pm);
        }
    }

    @Override
    public void delete(String urn) {
        getServiceClient().deleteProductMetadataByURN(urn);
    }

    @Override
    public ProductMetadata extract(Metadata metadata, boolean isExisting) throws Exception {
        ProductMetadata pmExisting = getProductMetadataByUrn(metadata.getUuid());

        // ProductMetadata was not supposed to be found in case of creation (isExisting=false)
        if (!isExisting && pmExisting != null) {
            //Log.warning(Geonet.DATA_MANAGER,
            //        "Found unexpected ProductMetata with URN " + metadata.getUrn()
            //                + "; will be overwritten");
            pmExisting.setProcess(null);
            pmExisting.setPriority(null);
            pmExisting.setOriginator(null);
        }

        ProductMetadata pm = null;

        if (pmExisting == null) {
            // Initialize a new product metadata
            pm = new ProductMetadata();
            pm.setUrn(metadata.getUuid());

            // Initialize fed and ingested to false
            pm.setFed(false);
            pm.setIngested(false);
        } else {
            //Set the extracted fields to null to ensure that all attributes are well re-extracted.
            pm = new ProductMetadata();
            pm.setId(pmExisting.getId());
            pm.setUrn(pmExisting.getUrn());
            pm.setFed(pmExisting.isFed());
            pm.setIngested(pmExisting.isIngested());
            pm.setProcess(pmExisting.getProcess());
            pm.setOverridenDataPolicy(pmExisting.getOverridenDataPolicy());
            pm.setOverridenFileExtension(pmExisting.getOverridenFileExtension());
            pm.setOverridenFncPattern(pmExisting.getOverridenFncPattern());
            pm.setOverridenPriority(pmExisting.getOverridenPriority());
        }

        IProductMetadataExtractor productMetadataExtractor = ProductMetadataExtractorFactory
                .getProductMetadataExtractor(metadata.getDataInfo().getSchemaId());

        pm.setFncPattern(productMetadataExtractor.extractFncPattern(metadata));

        // Extract the FNC pattern
        pm.setFncPattern(productMetadataExtractor.extractFncPattern(metadata));

        // Extract the originator
        pm.setOriginator(productMetadataExtractor.extractOriginator(metadata));

        // Extract the title
        pm.setTitle(productMetadataExtractor.extractTitle(metadata));

        // Extract the local datasource
        pm.setLocalDataSource(productMetadataExtractor.extractLocalDataSource(metadata));

        // Set the process type
        if (metadata.getSourceInfo() != null) {
            //pm.setProcess(metadata.getSourceInfo().getProcessType().toString());
        }

        // Extract the update frequency
        pm.setUpdateFrequency(productMetadataExtractor.extractUpdateFrequency(metadata));

        // Set the file extension
        pm.setFileExtension(productMetadataExtractor.extractFileExtension(metadata));

        // Extract GTS category, GTS priority, data policy.
        productMetadataExtractor.extractGTSCategoryGTSPriorityAndDataPolicy(metadata, pm);

        // Set default priority if any extracted.
        if (pm.getPriority() == null) {
            pm.setPriority(IProductMetadataExtractor.DEFAULT_PRIORITY);
        }

        // Set default originator if any extracted.
        if (StringUtils.isBlank(pm.getOriginator())) {
            pm.setOriginator(IProductMetadataExtractor.DEFAULT_ORIGINATOR);
        }

        return pm;
    }

    private ProductMetadataClient getServiceClient() {
        ApplicationContext context =
                new AnnotationConfigApplicationContext(ProductMetadataConfiguration.class);
        ProductMetadataClient client = context.getBean(ProductMetadataClient.class);

        return client;
    }
}
