/*
 * Copyright (C) 2001-2016 Food and Agriculture Organization of the
 * United Nations (FAO-UN), United Nations World Food Programme (WFP)
 * and United Nations Environment Programme (UNEP)
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or (at
 * your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
 *
 * Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
 * Rome - Italy. email: geonetwork@osgeo.org
 */

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
 * User: Jesse Date: 9/20/13 Time: 3:34 PM
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
