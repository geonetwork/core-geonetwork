package org.fao.geonet.repository;


import org.fao.geonet.domain.MetadataNotification;
import org.fao.geonet.domain.MetadataNotificationAction;
import org.fao.geonet.domain.MetadataNotificationId;
import org.fao.geonet.domain.MetadataNotifier;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;

public class MetadataNotificationRepositoryTest extends AbstractSpringDataTest {

    @Autowired
    MetadataNotificationRepository _repo;
    @Autowired
    MetadataNotifierRepository _notifierRepo;

    @Test
    public void testFindOne() {
        MetadataNotification notification1 = newMetadataNotification();
        notification1 = _repo.save(notification1);

        MetadataNotification notification2 = newMetadataNotification();
        notification2 = _repo.save(notification2);

        assertEquals(notification2, _repo.findOne(notification2.getId()));
        assertEquals(notification1, _repo.findOne(notification1.getId()));
    }

    @Test
    public void testFindAllNotNotifiedNotDeleted() {
        MetadataNotification notification1 = newMetadataNotification();
        notification1.setAction(MetadataNotificationAction.UPDATE);
        notification1.setNotified(false);
        notification1 = _repo.save(notification1);

        assertEquals(1, _repo.findAllNotNotifiedForNotifier(notification1.getId().getNotifierId(),
                MetadataNotificationAction.UPDATE).size());

        notification1.setAction(MetadataNotificationAction.DELETE);
        notification1 = _repo.save(notification1);

        assertEquals(0, _repo.findAllNotNotifiedForNotifier(notification1.getId().getNotifierId(),
                MetadataNotificationAction.UPDATE).size());
    }

    @Test
    public void testFindAllNotNotifiedNotNotified() {
        MetadataNotification notification1 = newMetadataNotification();
        notification1.setAction(MetadataNotificationAction.UPDATE);
        notification1.setNotified(false);
        notification1 = _repo.save(notification1);

        assertEquals(1, _repo.findAllNotNotifiedForNotifier(notification1.getId().getNotifierId()).size());

        notification1.setNotified(true);
        notification1 = _repo.save(notification1);

        assertEquals(0, _repo.findAllNotNotifiedForNotifier(notification1.getId().getNotifierId()).size());
    }

    @Test
    public void testFindAllNotNotifiedByNotifierId() {
        MetadataNotification notification1 = newMetadataNotification();
        notification1.setAction(MetadataNotificationAction.UPDATE);
        notification1.setNotified(false);
        notification1 = _repo.save(notification1);

        assertEquals(1, _repo.findAllNotNotifiedForNotifier(notification1.getId().getNotifierId()).size());
        assertEquals(0, _repo.findAllNotNotifiedForNotifier(notification1.getId().getNotifierId() + 1000).size());
    }

    private MetadataNotification newMetadataNotification() {
        return newMetadataNotification(_inc, _notifierRepo);
    }
    public static MetadataNotification newMetadataNotification(AtomicInteger inc, MetadataNotifierRepository notifierRepo) {

        MetadataNotifier notifier = MetadataNotifierRepositoryTest.newMetadataNotifier(inc);
        notifier = notifierRepo.save(notifier);

        int val = inc.incrementAndGet();
        MetadataNotification metadataNotification = new MetadataNotification();

        MetadataNotificationId mdNotId = new MetadataNotificationId();
        mdNotId.setMetadataId(val);
        mdNotId.setNotifierId(notifier.getId());

        metadataNotification.setId(mdNotId);
        metadataNotification.setAction(val % 2 == 0 ? MetadataNotificationAction.UPDATE : MetadataNotificationAction.DELETE);

        metadataNotification.setErrorMessage("errorMessage" + val);
        metadataNotification.setMetadataUuid("uuid" + val);
        metadataNotification.setNotified(val % 2 == 0);

        return metadataNotification;
    }
}
