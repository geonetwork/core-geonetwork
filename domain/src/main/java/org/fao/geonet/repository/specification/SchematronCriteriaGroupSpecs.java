package org.fao.geonet.repository.specification;

import org.fao.geonet.domain.*;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.*;

/**
 * Specifications for selecting {@link org.fao.geonet.domain.SchematronCriteria}
 *
 * Created by Jesse on 2/12/14.
 */
public class SchematronCriteriaGroupSpecs {
    public static Specification<SchematronCriteriaGroup> hasSchematronId(final int schematronId) {
        return new Specification<SchematronCriteriaGroup>() {
            @Override
            public Predicate toPredicate(Root<SchematronCriteriaGroup> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                final Path<Integer> schematronIdPath = root.get(SchematronCriteriaGroup_.id).get(SchematronCriteriaGroupId_.schematronId);

                return cb.equal(schematronIdPath, schematronId);
            }
        };
    }

    public static Specification<SchematronCriteriaGroup> hasGroupName(final String name) {
        return new Specification<SchematronCriteriaGroup>() {
            @Override
            public Predicate toPredicate(Root<SchematronCriteriaGroup> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                final Path<String> groupNamePath = root.get(SchematronCriteriaGroup_.id).get(SchematronCriteriaGroupId_.name);

                return cb.equal(groupNamePath, name);
            }
        };
    }
}
