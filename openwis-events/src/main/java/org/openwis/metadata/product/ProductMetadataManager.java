package org.openwis.metadata.product;

import org.apache.commons.lang.StringUtils;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.utils.Log;
import org.openwis.products.client.ProductMetadata;
import org.openwis.products.client.ProductMetadataClient;
import org.openwis.util.GeonetOpenwis;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Product metadata manager. Interacts with the Product Metadata web services.
 *
 */
@Component
public class ProductMetadataManager implements IProductMetadataManager {
    
    @Autowired
    private ProductMetadataClient serviceClient;

    @Autowired
    private SettingManager settingManager;

    @Autowired
    ProductMetadataIndexerLucene indexer;


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
            Log.warning(GeonetOpenwis.PRODUCT_METADATA,
                    "Found unexpected ProductMetata with URN " + metadata.getUuid()
                    + "; will be overwritten");
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
            String processType = "LOCAL";
            String catalogueSiteId = settingManager.getSiteId();

            // TODO: Check in actual implementation as is used also the value SYNCHRO
            if (!catalogueSiteId.equals(metadata.getSourceInfo().getSourceId())) {
                processType = "HARVEST";
            }
            pm.setProcess(processType);
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

    public ProductMetadataClient getServiceClient() {
        return serviceClient;
    }

    public void setServiceClient(ProductMetadataClient serviceClient) {
        this.serviceClient = serviceClient;
    }

}
