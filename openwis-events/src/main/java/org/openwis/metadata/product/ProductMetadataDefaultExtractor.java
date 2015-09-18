package org.openwis.metadata.product;

import org.fao.geonet.domain.Metadata;
import org.openwis.products.client.ProductMetadata;
import org.openwis.products.client.UpdateFrequency;

/**
 * Default extractor for product information from a metadata document, applied for non iso19139 metadata.
 *
 */
public class ProductMetadataDefaultExtractor implements IProductMetadataExtractor {
    @Override
    public String extractFncPattern(Metadata metadata) throws Exception {
        return null;
    }

    @Override
    public String extractOriginator(Metadata metadata) throws Exception {
        return null;
    }

    @Override
    public String extractTitle(Metadata metadata) throws Exception {
        return null;
    }

    @Override
    public String extractLocalDataSource(Metadata metadata) throws Exception {
        return null;
    }

    @Override
    public UpdateFrequency extractUpdateFrequency(Metadata metadata) throws Exception {
        return null;
    }

    @Override
    public String extractFileExtension(Metadata metadata) throws Exception {
        return null;
    }

    @Override
    public void extractGTSCategoryGTSPriorityAndDataPolicy(Metadata metadata, ProductMetadata pm) throws Exception {

    }
}
