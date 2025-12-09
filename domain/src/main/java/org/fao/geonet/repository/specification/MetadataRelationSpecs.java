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

import org.fao.geonet.domain.MetadataRelation;
import org.fao.geonet.domain.MetadataRelationId_;
import org.fao.geonet.domain.MetadataRelation_;
import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.*;

/**
 * Specification for querying {@link org.fao.geonet.repository.UserRepository}.
 *
 * @author Jesse
 */
public final class MetadataRelationSpecs {
    private MetadataRelationSpecs() {
        // no instantiation
    }

    public static Specification<MetadataRelation> hasMetadataId(final Integer metadataId) {
        return new Specification<MetadataRelation>() {
            @Override
            public Predicate toPredicate(Root<MetadataRelation> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                final Path<Integer> metadataIdPath = root.get(MetadataRelation_.id).get(MetadataRelationId_.metadataId);
                return cb.equal(metadataIdPath, metadataId);
            }
        };
    }

    public static Specification<MetadataRelation> hasRelatedId(final Integer relatedId) {
        return new Specification<MetadataRelation>() {
            @Override
            public Predicate toPredicate(Root<MetadataRelation> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                final Path<Integer> relatedIdPath = root.get(MetadataRelation_.id).get(MetadataRelationId_.relatedId);
                return cb.equal(relatedIdPath, relatedId);
            }
        };
    }
}
