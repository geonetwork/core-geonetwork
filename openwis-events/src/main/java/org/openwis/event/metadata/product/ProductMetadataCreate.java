package org.openwis.event.metadata.product;

import org.fao.geonet.domain.Metadata;
import org.fao.geonet.domain.MetadataType;
import org.fao.geonet.events.md.MetadataAdd;
import org.fao.geonet.utils.Log;
import org.openwis.metadata.product.ProductMetadataManager;
import org.openwis.products.client.ProductMetadata;
import org.openwis.util.GeonetOpenwis;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;


@Component
public class ProductMetadataCreate implements ApplicationListener<MetadataAdd> {

    @Autowired
    ProductMetadataManager productMetadataManager;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onApplicationEvent(MetadataAdd event) {
        Metadata metadata = event.getMd();

        if (! metadata.getDataInfo().getType().equals(MetadataType.METADATA)) return;

        try {
            Log.info(GeonetOpenwis.PRODUCT_METADATA, "Create - ProductMetadata (urn):" + metadata.getUuid());
            ProductMetadata pm = productMetadataManager.extract(metadata, false);

            productMetadataManager.saveOrUpdate(pm);

        } catch (Exception ex) {
            Log.error(GeonetOpenwis.PRODUCT_METADATA, ex.getMessage(), ex);
        }
    }


}