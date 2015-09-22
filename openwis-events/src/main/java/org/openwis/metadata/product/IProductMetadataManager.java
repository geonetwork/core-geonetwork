package org.openwis.metadata.product;

import org.fao.geonet.domain.Metadata;
import org.openwis.products.client.ProductMetadata;

/**
 * Created by jose on 18/09/15.
 */
public interface IProductMetadataManager {
    /**
     * Get a product metadata by URN.
     *
     * @param urn the metadata urn
     * @return a product metadata
     */
    ProductMetadata getProductMetadataByUrn(String urn);

    /**
     * Save or Update a product metadata.
     *
     * @param pm the product metadata
     */
    void saveOrUpdate(ProductMetadata pm);

    /**
     * Delete a product metadata.
     *
     * @param urn the product metadata URN
     */
    void delete(String urn);

    /**
     * Extracts a product metadata from a metadata handling the schema.
     *
     * @param metadata the object wrapping all elements needed for extraction.
     * @param isExisting <code>true</code> if the element exists, <code>false</code> otherwise.
     * @return the product metadata extracted with all attributes.
     * @throws Exception if an error occurs.
     */
    ProductMetadata extract(Metadata metadata, boolean isExisting) throws Exception;
}
