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
