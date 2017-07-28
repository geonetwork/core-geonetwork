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

import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

/**
 * Specifications for querying {@link org.fao.geonet.repository.UserRepository}.
 *
 * @author Jesse
 */
public final class MetadataSpecs {
    private MetadataSpecs() {
        // no instantiation
    }

    public static Specification<Metadata> hasSchemaId(final String schemaId) {
        return new Specification<Metadata>() {
            @Override
            public Predicate toPredicate(Root<Metadata> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                Path<String> schemaIdPath = root.get(Metadata_.dataInfo).get(MetadataDataInfo_.schemaId);
                return cb.equal(schemaIdPath, cb.literal(schemaId));
            }
        };
    }


    public static Specification<Metadata> hasOwner(final int owner) {
        return new Specification<Metadata>() {
            @Override
            public Predicate toPredicate(Root<Metadata> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                Path<Integer> ownerPath = root.get(Metadata_.sourceInfo).get(MetadataSourceInfo_.owner);
                return cb.equal(ownerPath, cb.literal(owner));
            }
        };
    }

    public static Specification<Metadata> hasMetadataId(final int metadataId) {
        return new Specification<Metadata>() {
            @Override
            public Predicate toPredicate(Root<Metadata> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                Path<Integer> userIdAttributePath = root.get(Metadata_.id);
                return cb.equal(userIdAttributePath, cb.literal(metadataId));
            }
        };
    }

    public static Specification<Metadata> hasMetadataUuid(final String uuid) {
        return new Specification<Metadata>() {
            @Override
            public Predicate toPredicate(Root<Metadata> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                Path<String> userNameAttributePath = root.get(Metadata_.uuid);
                return cb.equal(userNameAttributePath, cb.literal(uuid));
            }
        };
    }

    public static Specification<Metadata> hasHarvesterUuid(final String harvesterUuid) {
        return new Specification<Metadata>() {
            @Override
            public Predicate toPredicate(Root<Metadata> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                Path<String> userNameAttributePath = root.get(Metadata_.harvestInfo).get(MetadataHarvestInfo_.uuid);
                Predicate uuidEqualPredicate = cb.equal(userNameAttributePath, cb.literal(harvesterUuid));
                return uuidEqualPredicate;
            }
        };
    }

    public static Specification<Metadata> isOwnedByUser(final int userId) {
        return new Specification<Metadata>() {
            @Override
            public Predicate toPredicate(Root<Metadata> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                Path<Integer> ownerPath = root.get(Metadata_.sourceInfo).get(MetadataSourceInfo_.owner);
                Predicate equalUserIdPredicate = cb.equal(ownerPath, cb.literal(userId));
                return equalUserIdPredicate;
            }
        };
    }

    public static Specification<Metadata> hasSource(final String sourceUuid) {
        return new Specification<Metadata>() {
            @Override
            public Predicate toPredicate(Root<Metadata> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                Path<String> sourceAttributePath = root.get(Metadata_.sourceInfo).get(MetadataSourceInfo_.sourceId);
                Predicate equalSourceIdPredicate = cb.equal(sourceAttributePath, cb.literal(sourceUuid));
                return equalSourceIdPredicate;
            }
        };
    }

    public static Specification<Metadata> isType(final MetadataType type) {
        return new Specification<Metadata>() {
            @Override
            public Predicate toPredicate(Root<Metadata> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                Path<Character> templateAttributePath = root.get(Metadata_.dataInfo).get(MetadataDataInfo_.type_JPAWorkaround);
                Predicate equalTemplatePredicate = cb.equal(templateAttributePath, cb.literal(type.code));
                return equalTemplatePredicate;
            }
        };
    }

    public static Specification<Metadata> isHarvested(final boolean isHarvested) {
        return new Specification<Metadata>() {
            @Override
            public Predicate toPredicate(Root<Metadata> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                Path<Character> userNameAttributePath = root.get(Metadata_.harvestInfo).get(MetadataHarvestInfo_.harvested_JPAWorkaround);
                Predicate equalHarvestPredicate = cb.equal(userNameAttributePath, cb.literal(Constants.toYN_EnabledChar(isHarvested)));
                return equalHarvestPredicate;
            }
        };
    }


    public static Specification<Metadata> hasMetadataIdIn(final Collection<Integer> mdIds) {
        return new Specification<Metadata>() {
            @Override
            public Predicate toPredicate(Root<Metadata> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                return root.get(Metadata_.id).in(mdIds);
            }
        };
    }

    public static Specification<Metadata> hasMetadataUuidIn(final Collection<String> uuids) {
        return new Specification<Metadata>() {
            @Override
            public Predicate toPredicate(Root<Metadata> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                return root.get(Metadata_.uuid).in(uuids);
            }
        };
    }

    public static Specification<Metadata> hasType(final MetadataType metadataType) {
        return new Specification<Metadata>() {
            @Override
            public Predicate toPredicate(Root<Metadata> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                final Path<Character> typeChar = root.get(Metadata_.dataInfo).get(MetadataDataInfo_.type_JPAWorkaround);
                return cb.equal(typeChar, metadataType.code);
            }
        };
    }

    public static Specification<Metadata> isOwnedByOneOfFollowingGroups(final List<Integer> groups) {
        return new Specification<Metadata>() {
            @Override
            public Predicate toPredicate(Root<Metadata> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                return root.get(Metadata_.sourceInfo).get(MetadataSourceInfo_.groupOwner).in(groups);
            }
        };
    }

    /**
     * Creates a specification for finding all metadata containing a {@link MetadataCategory} with
     * the provided category
     *
     * @param category the category to use in the search
     * @return a specification for finding all metadata containing a {@link MetadataCategory} with
     * the provided category
     */
    public static Specification<Metadata> hasCategory(final MetadataCategory category) {
        return new Specification<Metadata>() {
            @Override
            public Predicate toPredicate(Root<Metadata> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                final Expression<Set<MetadataCategory>> categoriesPath = root.get(Metadata_.metadataCategories);

                return cb.isMember(category, categoriesPath);
            }
        };
    }

    public static Specification<Metadata> hasExtra(final String extra) {
        return new Specification<Metadata>() {
            @Override
            public Predicate toPredicate(Root<Metadata> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                return cb.equal(root.get(Metadata_.dataInfo).get(MetadataDataInfo_.extra), extra);
            }
        };
    }


    public static Specification<Metadata> isIso19139Schema() {
        return new Specification<Metadata>() {
            @Override
            public Predicate toPredicate(Root<Metadata> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                Path<String> schemaIdAttributePath = root.get(Metadata_.dataInfo).get(MetadataDataInfo_.schemaId);
                Predicate likeSchemaIdPredicate = cb.like(schemaIdAttributePath, cb.literal("iso19139"));
                return likeSchemaIdPredicate;
            }
        };
    }
}
