package org.fao.geonet.repository;

import org.fao.geonet.domain.Constants;
import org.fao.geonet.domain.HarvestHistory;
import org.fao.geonet.domain.HarvestHistory_;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.CriteriaUpdate;
import javax.persistence.criteria.Root;
import java.util.Collection;

/**
 * Implementation for custom methods for the HarvestHistoryRepository class.
 * <p/>
 * User: Jesse
 * Date: 9/20/13
 * Time: 4:03 PM
 */
public class HarvestHistoryRepositoryImpl implements HarvestHistoryRepositoryCustom {

    @PersistenceContext
    EntityManager _entityManager;

    @Override
    @Transactional
    public int deleteAllById(Collection<Integer> ids) {
        final CriteriaBuilder cb = _entityManager.getCriteriaBuilder();
        CriteriaDelete<HarvestHistory> delete = cb.createCriteriaDelete(HarvestHistory.class);
        final Root<HarvestHistory> root = delete.from(HarvestHistory.class);

        delete.where(root.get(HarvestHistory_.id).in(ids));

        final int deleted = _entityManager.createQuery(delete).executeUpdate();

        _entityManager.flush();
        _entityManager.clear();

        return deleted;
    }

    @Override
    @Transactional
    public int markAllAsDeleted(@Nonnull String harvesterUuid) {
        final CriteriaBuilder cb = _entityManager.getCriteriaBuilder();
        final CriteriaUpdate<HarvestHistory> update = cb.createCriteriaUpdate(HarvestHistory.class);
        final Root<HarvestHistory> root = update.from(HarvestHistory.class);

        update.set(root.get(HarvestHistory_.deleted_JpaWorkaround), Constants.YN_TRUE);
        update.where(cb.equal(root.get(HarvestHistory_.harvesterUuid), harvesterUuid));

        int updated = _entityManager.createQuery(update).executeUpdate();
        _entityManager.flush();
        _entityManager.clear();

        return updated;

    }
}
