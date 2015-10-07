package org.openwis.metadata.product;

import org.jdom.Element;
import org.openwis.products.client.ProductMetadata;

import java.util.Vector;


/**
 * Interface to implement by the ProductMetadata indexers.
 *
 * @author Jose García
 */
public interface IProductMetadataIndexer {
    Vector<Element> index(ProductMetadata pm);
}
