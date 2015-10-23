/**
 *
 */
package org.fao.geonet.repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.fao.geonet.domain.OpenwisDownload;
import org.fao.geonet.domain.User;

/**
 * Implementation object for methods in {@link OpenwisDownloadRepositoryCustom}.
 *
 * @author Jesse
 */
public class OpenwisDownloadRepositoryImpl
        implements OpenwisDownloadRepositoryCustom {

    @PersistenceContext
    private EntityManager _entityManager;

    /**
     * @see org.fao.geonet.repository.OpenwisDownloadRepositoryCustom#findByUserAndUuid(org.fao.geonet.domain.User,
     *      java.lang.String)
     * @param user
     * @param uuid
     * @return
     */
    @Override
    public OpenwisDownload findByUserAndUuid(User user, String uuid) {
        OpenwisDownload entity = null;

        if (user != null && uuid != null) {
            CriteriaBuilder cb = _entityManager.getCriteriaBuilder();
            CriteriaQuery<OpenwisDownload> query = cb
                    .createQuery(OpenwisDownload.class);
            Root<OpenwisDownload> root = query.from(OpenwisDownload.class);

            Predicate uuidEquals = cb.equal(root.get("urn"), uuid);
            Predicate userEquals = cb.equal(root.get("user"), user);

            Predicate predicate = cb.and(uuidEquals, userEquals);

            query.where(predicate);

            entity = _entityManager.createQuery(query).getSingleResult();
        }
        return entity;
    }

    /**
     * @see org.fao.geonet.repository.OpenwisDownloadRepositoryCustom#delete(java.lang.Integer)
     * @param id
     */
    @Override
    public void delete(Integer id) {
        if (id != null) {
            OpenwisDownload entity = _entityManager.find(OpenwisDownload.class,
                    id);
            if (entity != null) {
                _entityManager.remove(entity);
                _entityManager.flush();
                _entityManager.clear();
            }
        }
    }
}
