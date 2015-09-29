package org.openwis.metadata.product;

import org.openwis.products.client.ProductMetadata;


/**
 * Interface to implement by the ProductMetadata indexers.
 *
 * @author Jose García
 */
public interface IProductMetadataIndexer {
    void index(ProductMetadata pm);
}
