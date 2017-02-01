package org.fao.geonet.repository.specification;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.fao.geonet.domain.Constants;
import org.fao.geonet.domain.MetadataCategory;
import org.fao.geonet.domain.MetadataDataInfo_;
import org.fao.geonet.domain.MetadataDraft;
import org.fao.geonet.domain.MetadataDraft_;
import org.fao.geonet.domain.MetadataHarvestInfo_;
import org.fao.geonet.domain.MetadataSourceInfo_;
import org.fao.geonet.domain.MetadataType;
import org.springframework.data.jpa.domain.Specification;

/**
 * Specifications for querying {@link org.fao.geonet.repository.UserRepository}.
 *
 * @author Jesse
 */
public final class MetadataDraftSpecs {
    private MetadataDraftSpecs() {
        // no instantiation
    }

    public static Specification<MetadataDraft> hasSchemaId(final String schemaId) {
        return new Specification<MetadataDraft>() {
            @Override
            public Predicate toPredicate(Root<MetadataDraft> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                Path<String> schemaIdPath = root.get(MetadataDraft_.dataInfo).get(MetadataDataInfo_.schemaId);
                return cb.equal(schemaIdPath, cb.literal(schemaId));
            }
        };
    }


    public static Specification<MetadataDraft> hasOwner(final int owner) {
        return new Specification<MetadataDraft>() {
            @Override
            public Predicate toPredicate(Root<MetadataDraft> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                Path<Integer> ownerPath = root.get(MetadataDraft_.sourceInfo).get(MetadataSourceInfo_.owner);
                return cb.equal(ownerPath, cb.literal(owner));
            }
        };
    }

    public static Specification<MetadataDraft> hasMetadataId(final int metadataId) {
        return new Specification<MetadataDraft>() {
            @Override
            public Predicate toPredicate(Root<MetadataDraft> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                Path<Integer> userIdAttributePath = root.get(MetadataDraft_.id);
                return cb.equal(userIdAttributePath, cb.literal(metadataId));
            }
        };
    }

    public static Specification<MetadataDraft> hasMetadataUuid(final String uuid) {
        return new Specification<MetadataDraft>() {
            @Override
            public Predicate toPredicate(Root<MetadataDraft> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                Path<String> userNameAttributePath = root.get(MetadataDraft_.uuid);
                return cb.equal(userNameAttributePath, cb.literal(uuid));
            }
        };
    }

    public static Specification<MetadataDraft> hasHarvesterUuid(final String harvesterUuid) {
        return new Specification<MetadataDraft>() {
            @Override
            public Predicate toPredicate(Root<MetadataDraft> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                Path<String> userNameAttributePath = root.get(MetadataDraft_.harvestInfo).get(MetadataHarvestInfo_.uuid);
                Predicate uuidEqualPredicate = cb.equal(userNameAttributePath, cb.literal(harvesterUuid));
                return uuidEqualPredicate;
            }
        };
    }

    public static Specification<MetadataDraft> isOwnedByUser(final int userId) {
        return new Specification<MetadataDraft>() {
            @Override
            public Predicate toPredicate(Root<MetadataDraft> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                Path<Integer> ownerPath = root.get(MetadataDraft_.sourceInfo).get(MetadataSourceInfo_.owner);
                Predicate equalUserIdPredicate = cb.equal(ownerPath, cb.literal(userId));
                return equalUserIdPredicate;
            }
        };
    }

    public static Specification<MetadataDraft> hasSource(final String sourceUuid) {
        return new Specification<MetadataDraft>() {
            @Override
            public Predicate toPredicate(Root<MetadataDraft> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                Path<String> sourceAttributePath = root.get(MetadataDraft_.sourceInfo).get(MetadataSourceInfo_.sourceId);
                Predicate equalSourceIdPredicate = cb.equal(sourceAttributePath, cb.literal(sourceUuid));
                return equalSourceIdPredicate;
            }
        };
    }

    public static Specification<MetadataDraft> isType(final MetadataType type) {
        return new Specification<MetadataDraft>() {
            @Override
            public Predicate toPredicate(Root<MetadataDraft> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                Path<Character> templateAttributePath = root.get(MetadataDraft_.dataInfo).get(MetadataDataInfo_.type_JPAWorkaround);
                Predicate equalTemplatePredicate = cb.equal(templateAttributePath, cb.literal(type.code));
                return equalTemplatePredicate;
            }
        };
    }

    public static Specification<MetadataDraft> isHarvested(final boolean isHarvested) {
        return new Specification<MetadataDraft>() {
            @Override
            public Predicate toPredicate(Root<MetadataDraft> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                Path<Character> userNameAttributePath = root.get(MetadataDraft_.harvestInfo).get(MetadataHarvestInfo_.harvested_JPAWorkaround);
                Predicate equalHarvestPredicate = cb.equal(userNameAttributePath, cb.literal(Constants.toYN_EnabledChar(isHarvested)));
                return equalHarvestPredicate;
            }
        };
    }


    public static Specification<MetadataDraft> hasMetadataIdIn(final Collection<Integer> mdIds) {
        return new Specification<MetadataDraft>() {
            @Override
            public Predicate toPredicate(Root<MetadataDraft> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                return root.get(MetadataDraft_.id).in(mdIds);
            }
        };
    }

    public static Specification<MetadataDraft> hasMetadataUuidIn(final Collection<String> uuids) {
        return new Specification<MetadataDraft>() {
            @Override
            public Predicate toPredicate(Root<MetadataDraft> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                return root.get(MetadataDraft_.uuid).in(uuids);
            }
        };
    }

    public static Specification<MetadataDraft> hasType(final MetadataType metadataType) {
        return new Specification<MetadataDraft>() {
            @Override
            public Predicate toPredicate(Root<MetadataDraft> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                final Path<Character> typeChar = root.get(MetadataDraft_.dataInfo).get(MetadataDataInfo_.type_JPAWorkaround);
                return cb.equal(typeChar, metadataType.code);
            }
        };
    }

    public static Specification<MetadataDraft> isOwnedByOneOfFollowingGroups(final List<Integer> groups) {
        return new Specification<MetadataDraft>() {
            @Override
            public Predicate toPredicate(Root<MetadataDraft> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                return root.get(MetadataDraft_.sourceInfo).get(MetadataSourceInfo_.groupOwner).in(groups);
            }
        };
    }

    /**
     * Creates a specification for finding all metadata containing a {@link MetadataCategory} with the provided category
     *
     * @param category the category to use in the search
     * @return a specification for finding all metadata containing a {@link MetadataCategory} with the provided category
     */
    public static Specification<MetadataDraft> hasCategory(final MetadataCategory category) {
        return new Specification<MetadataDraft>() {
            @Override
            public Predicate toPredicate(Root<MetadataDraft> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                final Expression<Set<MetadataCategory>> categoriesPath = root.get(MetadataDraft_.categories);

                return cb.isMember(category, categoriesPath);
            }
        };
    }

    public static Specification<MetadataDraft> hasExtra(final String extra) {
        return new Specification<MetadataDraft>() {
            @Override
            public Predicate toPredicate(Root<MetadataDraft> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                return cb.equal(root.get(MetadataDraft_.dataInfo).get(MetadataDataInfo_.extra), extra);
            }
        };
    }


    public static Specification<MetadataDraft> isIso19139Schema() {
        return new Specification<MetadataDraft>() {
            @Override
            public Predicate toPredicate(Root<MetadataDraft> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                Path<String> schemaIdAttributePath =  root.get(MetadataDraft_.dataInfo).get(MetadataDataInfo_.schemaId);
                Predicate likeSchemaIdPredicate = cb.like(schemaIdAttributePath, cb.literal("iso19139"));
                return likeSchemaIdPredicate;
            }
        };
    }
}
