package org.fao.geonet.repository;

import org.fao.geonet.domain.CswCapabilitiesInfoField;
import org.fao.geonet.domain.CswCapabilitiesInfoField_;

import javax.annotation.Nonnull;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.Collection;
import java.util.List;

/**
 * Implementation of the custom repository methods.
 * <p/>
 * User: Jesse
 * Date: 9/20/13
 * Time: 10:36 AM
 */
public class CswCapabilitiesInfoFieldRepositoryImpl implements CswCapabilitiesInfoFieldRepositoryCustom {
    @PersistenceContext
    private EntityManager _EntityManager;

    @Override
    public CswCapabilitiesInfo findCswCapabilitiesInfo(final String languageCode) {
        final CriteriaBuilder cb = _EntityManager.getCriteriaBuilder();
        final CriteriaQuery<CswCapabilitiesInfoField> query = cb.createQuery(CswCapabilitiesInfoField.class);
        final Root<CswCapabilitiesInfoField> root = query.from(CswCapabilitiesInfoField.class);
        query.where(cb.equal(root.get(CswCapabilitiesInfoField_.langId), languageCode));
        List<CswCapabilitiesInfoField> allFieldsForLang = _EntityManager.createQuery(query).getResultList();
        return new CswCapabilitiesInfo(languageCode, allFieldsForLang);
    }

    @Override
    public void save(@Nonnull final CswCapabilitiesInfo info) {
        Collection<CswCapabilitiesInfoField> fields = info.getFields();

        for (CswCapabilitiesInfoField field : fields) {
            if (field.getId() == 0) {
                _EntityManager.persist(field);
            } else {
                _EntityManager.merge(field);
            }
        }
    }
}
