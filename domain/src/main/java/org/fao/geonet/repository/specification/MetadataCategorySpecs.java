package org.fao.geonet.repository.specification;

import org.fao.geonet.domain.MetadataCategory;
import org.fao.geonet.domain.MetadataCategory_;
import org.springframework.data.jpa.domain.Specification;

import java.util.Collection;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

/**
 * Specifications for selecting the MetadataCategories.
 *
 * @author Jesse on 11/25/2014.
 */
public class MetadataCategorySpecs {
    public static Specification<MetadataCategory> hasCategoryNameIn(final Collection<String> names) {
        return new Specification<MetadataCategory>() {
            @Override
            public Predicate toPredicate(Root<MetadataCategory> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                return root.get(MetadataCategory_.name).in(names);
            }
        };
    }
}
