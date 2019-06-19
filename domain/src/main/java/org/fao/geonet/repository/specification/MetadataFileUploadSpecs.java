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

import java.util.Collection;

/**
 * Specifications for querying {@link org.fao.geonet.repository.MetadataFileUploadRepository}.
 *
 * @author Jose García
 */
public class MetadataFileUploadSpecs {
    private MetadataFileUploadSpecs() {
        // no instantiation
    }

    public static Specification<MetadataFileUpload> hasId(final int id) {
        return new Specification<MetadataFileUpload>() {
            @Override
            public Predicate toPredicate(Root<MetadataFileUpload> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                Path<Integer> updloadIdAttributePath = root.get(MetadataFileUpload_.id);
                Predicate pdloadIdEqualPredicate = cb.equal(updloadIdAttributePath, cb.literal(id));
                return pdloadIdEqualPredicate;
            }
        };
    }

    public static Specification<MetadataFileUpload> hasMetadataId(final int id) {
        return new Specification<MetadataFileUpload>() {
            @Override
            public Predicate toPredicate(Root<MetadataFileUpload> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                Path<Integer> updloadIdAttributePath = root.get(MetadataFileUpload_.metadataId);
                Predicate pdloadIdEqualPredicate = cb.equal(updloadIdAttributePath, cb.literal(id));
                return pdloadIdEqualPredicate;
            }
        };
    }

    public static Specification<MetadataFileUpload> uploadDateBetweenAndByGroups(final ISODate uploadFrom,
                                                                                 final ISODate uploadTo,
                                                                                 final Collection<Integer> groups) {
        return new Specification<MetadataFileUpload>() {
            @Override
            public Predicate toPredicate(Root<MetadataFileUpload> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                Path<String> uploadDateAttributePath = root.get(MetadataFileUpload_.uploadDate);
                Path<Integer> metadataIdAttributePath = root.get(MetadataFileUpload_.metadataId);

                Predicate uploadDateBetweenPredicate = cb.between(uploadDateAttributePath,
                    uploadFrom.toString(), uploadTo.toString());

                if (!groups.isEmpty()) {
                    final Root<Metadata> metadataRoot = query.from(Metadata.class);
                    final Path<Integer> groupOwnerPath = metadataRoot.get(Metadata_.sourceInfo).get(MetadataSourceInfo_.groupOwner);

                    Predicate inGroups = groupOwnerPath.in(groups);

                    uploadDateBetweenPredicate = cb.and(cb.equal(metadataRoot.get(Metadata_.id),
                        metadataIdAttributePath), cb.and(uploadDateBetweenPredicate, inGroups));
                }


                return uploadDateBetweenPredicate;
            }
        };
    }

    public static Specification<MetadataFileUpload> isNotDeleted() {
        return new Specification<MetadataFileUpload>() {
            @Override
            public Predicate toPredicate(Root<MetadataFileUpload> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                Path<String> deletedDateAttributePath = root.get(MetadataFileUpload_.deletedDate);
                Predicate deletedPredicate = cb.isNull(deletedDateAttributePath);
                return deletedPredicate;
            }
        };
    }

    public static Specification<MetadataFileUpload> isNotDeletedForMetadata(final int metadataId) {
        return new Specification<MetadataFileUpload>() {
            @Override
            public Predicate toPredicate(Root<MetadataFileUpload> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                Path<String> deletedDateAttributePath = root.get(MetadataFileUpload_.deletedDate);
                Path<Integer> metadataIdAttributePath = root.get(MetadataFileUpload_.metadataId);
                Predicate notDeletedPredicate = cb.and(cb.isNull(deletedDateAttributePath),
                    cb.equal(metadataIdAttributePath, metadataId));

                return notDeletedPredicate;
            }
        };
    }

}
