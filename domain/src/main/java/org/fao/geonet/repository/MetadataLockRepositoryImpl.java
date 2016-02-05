package org.fao.geonet.repository;

import java.util.Date;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.fao.geonet.domain.MetadataLock;
import org.fao.geonet.domain.MetadataLock_;
import org.fao.geonet.domain.User;

public class MetadataLockRepositoryImpl
        implements MetadataLockRepositoryCustom {
    @PersistenceContext
    EntityManager _entityManager;

    /**
     * @see org.fao.geonet.repository.MetadataLockRepositoryCustom#lock(java.lang.String)
     * @param id
     * @return
     */
    @Override
    public synchronized boolean lock(String id, User user) {
        if (isLocked(id, user)) {
            return false;
        }

        user = getUser(user);

        MetadataLock mdLock = new MetadataLock();
        mdLock.setMetadata(Integer.valueOf(id));
        mdLock.setUser(user);
        mdLock.setTimestamp(new Date(System.currentTimeMillis()));

        _entityManager.persist(mdLock);
        _entityManager.flush();

        return true;
    }

    /**
     * @see org.fao.geonet.repository.MetadataLockRepositoryCustom#isLocked(java.lang.String)
     * @param id
     * @return
     */
    @Override
    public boolean isLocked(String id, User user) {
        removeOldLocks();
        CriteriaBuilder cb = _entityManager.getCriteriaBuilder();
        CriteriaQuery<MetadataLock> cquery = cb.createQuery(MetadataLock.class);
        Root<MetadataLock> root = cquery.from(MetadataLock.class);
        cquery.where(
                cb.equal(root.get(MetadataLock_.metadata), Integer.valueOf(id)));
        cquery.where(
                cb.notEqual(root.get(MetadataLock_.user), user));
        return _entityManager.createQuery(cquery).getResultList().size() > 0;
    }

    /**
     * @see org.fao.geonet.repository.MetadataLockRepositoryCustom#unlock(java.lang.String)
     * @param id
     * @return
     */
    @Override
    public synchronized boolean unlock(String id, User user) {
        if (!isLocked(id, user)) {
            return false;
        }

        user = getUser(user);

        CriteriaBuilder cb = _entityManager.getCriteriaBuilder();
        CriteriaQuery<MetadataLock> cquery = cb.createQuery(MetadataLock.class);
        Root<MetadataLock> root = cquery.from(MetadataLock.class);
        cquery.where(
                cb.equal(root.get(MetadataLock_.metadata), Integer.valueOf(id)));
        for (MetadataLock mdLock : _entityManager.createQuery(cquery)
                .getResultList()) {
            _entityManager.remove(mdLock);
        }

        _entityManager.flush();
        return true;
    }

    private void removeOldLocks() {
        Integer minutes = 5;
        // FIXME move this to settings

        CriteriaBuilder cb = _entityManager.getCriteriaBuilder();
        CriteriaQuery<MetadataLock> cquery = cb.createQuery(MetadataLock.class);
        Root<MetadataLock> root = cquery.from(MetadataLock.class);
        Date date = new Date(
                System.currentTimeMillis() - (minutes * 60 * 1000));
        cquery.where(cb.lessThan(root.get(MetadataLock_.timestamp), date));
        for (MetadataLock mdLock : _entityManager.createQuery(cquery)
                .getResultList()) {
            _entityManager.remove(mdLock);
        }
    }
    
    private User getUser(User user) {
        return _entityManager.find(User.class, user.getId());
    }

}
