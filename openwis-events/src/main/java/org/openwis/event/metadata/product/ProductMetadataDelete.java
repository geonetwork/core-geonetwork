package org.openwis.event.metadata.product;

import org.fao.geonet.Logger;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.events.md.MetadataRemove;
import org.fao.geonet.utils.Log;
import org.openwis.metadata.product.ProductMetadataManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;


@Component
public class ProductMetadataDelete implements ApplicationListener<MetadataRemove> {
    private static Logger log = Log.createLogger("openwis");

    @Autowired
    ProductMetadataManager productMetadataManager;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onApplicationEvent(MetadataRemove event) {
        Metadata metadata = event.getMd();

        try {
            productMetadataManager.delete(metadata.getUuid());
        } catch (Exception ex) {
            log.error(ex.getMessage());
            log.error(ex);
        }
    }

}