package org.fao.geonet.repository;

import org.fao.geonet.domain.MetadataNotification;
import org.fao.geonet.domain.MetadataNotificationAction;

import java.util.List;

/**
 * Custom queries for MetadataNotificationRepository..
 * User: Jesse
 * Date: 9/7/13
 * Time: 8:29 PM
 */
public interface MetadataNotificationRepositoryCustom {
    /**
     * Find all the notification that have not yet been sent for a particular notifier.
     *
     * @param notifierId the notifier in question.
     * @param actions    the permitted actions.  these will be turned into an IN clause.  If not actions are specified then all actions
     *                   are
     *                   accepted
     * @return all the notification that have not yet been sent for a particular notifier.
     */
    List<MetadataNotification> findAllNotNotifiedForNotifier(int notifierId, MetadataNotificationAction... actions);

    /**
     * Delete all notifications with the provided notifierId.
     *
     * @param notifierId the notifier id
     * @return the number of notifications deleted.
     */
    int deleteAllWithNotifierId(int notifierId);
}
