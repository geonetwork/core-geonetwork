package org.fao.geonet.repository;

import static org.fao.geonet.domain.Constants.toYN_EnabledChar;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.fao.geonet.domain.Setting;
import org.fao.geonet.domain.Setting_;

/**
 * Implementation for MetadataNotifierRepositoryCustom methods.
 * <p/>
 * User: francois
 * Date: 8/28/13
 * Time: 7:31 AM
 * To change this template use File | Settings | File Templates.
 */
public class SettingRepositoryImpl implements SettingRepositoryCustom {

    @PersistenceContext
    private EntityManager _entityManager;


    @Override
    public List<Setting> findAllByInternal(boolean internal) {

      CriteriaBuilder cb = _entityManager.getCriteriaBuilder();
      CriteriaQuery<Setting> cquery = cb.createQuery(Setting.class);
      Root<Setting> root = cquery.from(Setting.class);
      char internalChar = toYN_EnabledChar(internal);
      cquery.where(cb.equal(root.get(Setting_.internal_JpaWorkaround), internalChar));
      return _entityManager.createQuery(cquery).getResultList();
    }

}
