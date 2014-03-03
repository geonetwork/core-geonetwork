package org.fao.geonet.repository.specification;

import org.fao.geonet.domain.HarvestHistory;
import org.fao.geonet.domain.HarvestHistory_;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

/**
 * Specifications for querying HarvestHistoryRepository.
 * <p/>
 * User: Jesse
 * Date: 9/20/13
 * Time: 3:34 PM
 */
public final class HarvestHistorySpecs {

    private HarvestHistorySpecs() {
    }

    public static Specification<HarvestHistory> hasHarvesterUuid(final String uuid) {
        return new Specification<HarvestHistory>() {
            @Override
            public Predicate toPredicate(Root<HarvestHistory> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                return cb.equal(root.get(HarvestHistory_.harvesterUuid), uuid);
            }
        };
    }

}
