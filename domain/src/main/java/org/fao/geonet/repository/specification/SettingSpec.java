package org.fao.geonet.repository.specification;

import org.fao.geonet.domain.Setting;
import org.fao.geonet.domain.Setting_;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

/**
 * Specs for creating setting queries.
 *
 * @author Jesse on 2/4/2015.
 */
public class SettingSpec {
    public static Specification<Setting> nameStartsWith(final String prefix) {
        return new Specification<Setting>() {
            @Override
            public Predicate toPredicate(Root<Setting> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                return cb.like(root.get(Setting_.name), prefix + "%");
            }
        };
    }
}
