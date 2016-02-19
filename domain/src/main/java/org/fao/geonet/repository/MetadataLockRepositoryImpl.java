package org.fao.geonet.repository;

import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.fao.geonet.domain.MetadataLock;
import org.fao.geonet.domain.MetadataLock_;
import org.fao.geonet.domain.Setting;
import org.fao.geonet.domain.SettingDataType;
import org.fao.geonet.domain.Setting_;
import org.fao.geonet.domain.User;

public class MetadataLockRepositoryImpl
        implements MetadataLockRepositoryCustom {
    /**
     * 
     */
    private static final String METADATA_LOCK = "metadata/lock";
    @PersistenceContext
    private EntityManager _entityManager;

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
        _entityManager.flush();
        removeOldLocks();
        CriteriaBuilder cb = _entityManager.getCriteriaBuilder();
        CriteriaQuery<MetadataLock> cquery = cb.createQuery(MetadataLock.class);
        Root<MetadataLock> root = cquery.from(MetadataLock.class);
        cquery.where(cb.equal(root.get(MetadataLock_.metadata),
                Integer.valueOf(id)));
        cquery.where(cb.notEqual(root.get(MetadataLock_.user), user));
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
        cquery.where(cb.equal(root.get(MetadataLock_.metadata),
                Integer.valueOf(id)));
        for (MetadataLock mdLock : _entityManager.createQuery(cquery)
                .getResultList()) {
            _entityManager.remove(mdLock);
        }

        _entityManager.flush();
        return true;
    }

    private void removeOldLocks() {
        Integer minutes = getSetting(METADATA_LOCK);

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

    /**
     * @param string
     * @return
     */
    private Integer getSetting(String string) {
        CriteriaBuilder cb = _entityManager.getCriteriaBuilder();
        CriteriaQuery<Setting> cquery = cb.createQuery(Setting.class);
        Root<Setting> root = cquery.from(Setting.class);

        cquery.where(cb.equal(root.get(Setting_.name), string));

        List<Setting> settings = _entityManager.createQuery(cquery)
                .getResultList();
        if (settings.size() > 0) {
            return Integer.valueOf(settings.get(0).getValue());
        }

        // Default: no lock
        if (string.equals(METADATA_LOCK)) {
            // Some migration failed, let's add it manually
            Setting s = new Setting();
            s.setName(METADATA_LOCK);
            s.setDataType(SettingDataType.INT);
            s.setInternal(false);
            s.setPosition(100003);
            s.setValue("-1");
            _entityManager.persist(s);
            _entityManager.flush();
        }
        return -1;
    }

    private User getUser(User user) {
        return _entityManager.find(User.class, user.getId());
    }

}
