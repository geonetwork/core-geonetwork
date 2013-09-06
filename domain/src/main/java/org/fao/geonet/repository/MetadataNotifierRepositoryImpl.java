package org.fao.geonet.repository;

import org.fao.geonet.domain.MetadataNotifier;
import org.fao.geonet.domain.MetadataNotifier_;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;

import static org.fao.geonet.domain.Constants.toYN_EnabledChar;

/**
 * Implementation for MetadataNotifierRepositoryCustom methods.
 * <p/>
 * User: Jesse
 * Date: 8/28/13
 * Time: 7:31 AM
 * To change this template use File | Settings | File Templates.
 */
public class MetadataNotifierRepositoryImpl implements MetadataNotifierRepositoryCustom {

    @PersistenceContext
    private EntityManager _entityManager;

    @Override
    public List<MetadataNotifier> findAllByEnabled(boolean enabled) {
        CriteriaBuilder cb = _entityManager.getCriteriaBuilder();
        CriteriaQuery<MetadataNotifier> cquery = cb.createQuery(MetadataNotifier.class);
        Root<MetadataNotifier> root = cquery.from(MetadataNotifier.class);
        char enabledChar = toYN_EnabledChar(enabled);
        cquery.where(cb.equal(root.get(MetadataNotifier_.enabled_JPAWorkaround), enabledChar));
        return _entityManager.createQuery(cquery).getResultList();
    }

}
