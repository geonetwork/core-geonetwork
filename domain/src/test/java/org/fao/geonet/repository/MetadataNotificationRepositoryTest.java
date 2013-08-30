package org.fao.geonet.repository;


import org.fao.geonet.domain.MetadataCategory;
import org.fao.geonet.domain.MetadataNotification;
import org.fao.geonet.domain.MetadataNotificationAction;
import org.fao.geonet.domain.MetadataNotificationId;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;

@Transactional
public class MetadataNotificationRepositoryTest extends AbstractSpringDataTest {

    @Autowired
    MetadataNotificationRepository _repo;

    AtomicInteger _inc = new AtomicInteger();
    @Test
    public void testFindOne() {
        MetadataNotification notification1 = newMetadataNotification();
        notification1 = _repo.save(notification1);

        MetadataNotification notification2 = newMetadataNotification();
        notification2 = _repo.save(notification2);

        assertEquals(notification2, _repo.findOne(notification2.getId()));
        assertEquals(notification1, _repo.findOne(notification1.getId()));
    }

    private MetadataNotification newMetadataNotification() {
        int val = _inc.incrementAndGet();
        MetadataNotification metadataNotification = new MetadataNotification();

        MetadataNotificationId mdNotId = new MetadataNotificationId();
        mdNotId.setMetadataId(val);
        mdNotId.setNotifierId(_inc.incrementAndGet());

        metadataNotification.setId(mdNotId);
        metadataNotification.setAction(val % 2 == 0 ? MetadataNotificationAction.UPDATE : MetadataNotificationAction.DELETE);

        metadataNotification.setErrorMessage("errorMessage"+val);
        metadataNotification.setMetadataUuid("uuid" + val);
        metadataNotification.setNotified(val % 2 == 0);

        return metadataNotification;
    }
}
