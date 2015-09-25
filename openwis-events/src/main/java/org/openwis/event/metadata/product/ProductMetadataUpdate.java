package org.openwis.event.metadata.product;

import org.fao.geonet.Logger;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.events.md.MetadataUpdate;
import org.fao.geonet.utils.Log;
import org.openwis.metadata.product.ProductMetadataManager;
import org.openwis.products.client.ProductMetadata;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;


@Component
public class ProductMetadataUpdate implements ApplicationListener<MetadataUpdate> {
    private static Logger log = Log.createLogger("openwis");

    @Autowired
    ProductMetadataManager productMetadataManager;


    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onApplicationEvent(MetadataUpdate event) {
        Metadata metadata = event.getMd();

        try {
            // TODO: Fill the product metadata information
            ProductMetadata pm = productMetadataManager.extract(metadata, false);

            //pm.setUrn(metadata.getUuid());
            //pm.setFed(false);
            //pm.setIngested(false);

            productMetadataManager.saveOrUpdate(pm);

        } catch (Exception ex) {
            log.error(ex.getMessage());
            log.error(ex);
        }

    }

}