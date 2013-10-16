package org.fao.geonet.repository;

import org.fao.geonet.domain.Constants;
import org.fao.geonet.domain.Language;
import org.fao.geonet.domain.Language_;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;

/**
 * Implement custom repository methods in LanguageRepositoryCustom
 * User: Jesse
 * Date: 8/30/13
 * Time: 8:22 AM
 */
public class LanguageRepositoryImpl implements LanguageRepositoryCustom {
    @PersistenceContext
    EntityManager _entityManager;

    @Override
    public List<Language> findAllByInspireFlag(boolean inspire) {
        char isInspireChar = Constants.toYN_EnabledChar(inspire);
        CriteriaBuilder cb = _entityManager.getCriteriaBuilder();
        CriteriaQuery<Language> query = cb.createQuery(Language.class);
        Root<Language> root = query.from(Language.class);
        query.where(cb.equal(root.get(Language_.inspire_JPAWorkaround), isInspireChar));

        return _entityManager.createQuery(query).getResultList();
    }

    @Override
    public Language findOneByDefaultLanguage() {
        CriteriaBuilder cb = _entityManager.getCriteriaBuilder();
        CriteriaQuery<Language> query = cb.createQuery(Language.class);
        Root<Language> root = query.from(Language.class);
        query.where(cb.equal(root.get(Language_.defaultLanguage_JPAWorkaround), Constants.YN_TRUE));

        return _entityManager.createQuery(query).getSingleResult();
    }
}
