package org.fao.geonet.repository;

import org.fao.geonet.domain.*;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.*;
import java.util.List;

/**
 * Implementation for methods in MetadataNotificationRepositoryCustom.
 * <p/>
 * User: Jesse
 * Date: 9/7/13
 * Time: 8:30 PM
 */
public class MetadataNotificationRepositoryImpl implements MetadataNotificationRepositoryCustom {
    @PersistenceContext
    EntityManager _entityManager;

    @Override
    public List<MetadataNotification> findAllNotNotifiedForNotifier(int notifierId, MetadataNotificationAction... actions) {
        final CriteriaBuilder cb = _entityManager.getCriteriaBuilder();
        final CriteriaQuery<MetadataNotification> cbQuery = cb.createQuery(MetadataNotification.class);
        final Root<MetadataNotification> notificationRoot = cbQuery.from(MetadataNotification.class);
        final Path<Integer> notifierIdPath = notificationRoot.get(MetadataNotification_.id).get(MetadataNotificationId_.notifierId);
        final Predicate correctNotifier = cb.equal(notifierIdPath, notifierId);

        final Path<Character> notifiedPath = notificationRoot.get(MetadataNotification_.notified_JPAWorkaround);
        final Predicate notifiedIsNull = cb.isNull(notifiedPath);
        final Predicate notNotified = cb.equal(notifiedPath, Constants.YN_FALSE);

        final Path<MetadataNotificationAction> actionPath = notificationRoot.get(MetadataNotification_.action);

        final Predicate fullClause;
        if (actions != null && actions.length > 0) {
            Predicate actionsPredicate = actionPath.in(actions);
            fullClause = cb.and(correctNotifier, cb.or(notifiedIsNull, notNotified), actionsPredicate);
        } else {
            fullClause = cb.and(correctNotifier, cb.or(notifiedIsNull, notNotified));

        }
        cbQuery.where(fullClause);
        return _entityManager.createQuery(cbQuery).getResultList();
    }

    @Override
    @Transactional
    public int deleteAllWithNotifierId(int notifierId) {
        final CriteriaBuilder cb = _entityManager.getCriteriaBuilder();
        final CriteriaDelete<MetadataNotification> delete = cb.createCriteriaDelete(MetadataNotification.class);
        final Root<MetadataNotification> notificationRoot = delete.from(MetadataNotification.class);

        delete.where(cb.equal(notificationRoot.get(MetadataNotification_.id).get(MetadataNotificationId_.notifierId), notifierId));
        return _entityManager.createQuery(delete).executeUpdate();
    }

}
