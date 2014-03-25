package org.fao.geonet.repository.specification;

import org.fao.geonet.domain.SchematronCriteria;
import org.fao.geonet.domain.SchematronCriteriaGroupId_;
import org.fao.geonet.domain.SchematronCriteriaGroup_;
import org.fao.geonet.domain.SchematronCriteria_;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.*;

/**
 * Specifications for selecting {@link org.fao.geonet.domain.SchematronCriteria}
 *
 * Created by Jesse on 2/12/14.
 */
public class SchematronCriteriaSpecs {
    public static Specification<SchematronCriteria> hasSchematronId(final int schematronId) {
        return new Specification<SchematronCriteria>() {
            @Override
            public Predicate toPredicate(Root<SchematronCriteria> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                final Path<Integer> schematronIdPath = root.get(SchematronCriteria_.group).get(SchematronCriteriaGroup_.id).get
                        (SchematronCriteriaGroupId_.schematronId);

                return cb.equal(schematronIdPath, schematronId);
            }
        };
    }

    public static Specification<SchematronCriteria> hasGroupName(final String name) {
        return new Specification<SchematronCriteria>() {
            @Override
            public Predicate toPredicate(Root<SchematronCriteria> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                final Path<String> groupNamePath = root.get(SchematronCriteria_.group).get(SchematronCriteriaGroup_.id).get
                        (SchematronCriteriaGroupId_.name);

                return cb.equal(groupNamePath, name);
            }
        };
    }
}
