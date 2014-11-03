package org.fao.geonet.repository.specification;

import org.fao.geonet.domain.*;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.*;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Specifications for querying {@link org.fao.geonet.repository.UserRepository}.
 *
 * @author Jesse
 */
public final class MetadataSpecs {
    private MetadataSpecs() {
        // no instantiation
    }

    public static Specification<Metadata> hasMetadataId(final int metadataId) {
        return new Specification<Metadata>() {
            @Override
            public Predicate toPredicate(Root<Metadata> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                Path<Integer> userIdAttributePath = root.get(Metadata_.id);
                Predicate idEqualPredicate = cb.equal(userIdAttributePath, cb.literal(metadataId));
                return idEqualPredicate;
            }
        };
    }

    public static Specification<Metadata> hasMetadataUuid(final String uuid) {
        return new Specification<Metadata>() {
            @Override
            public Predicate toPredicate(Root<Metadata> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                Path<String> userNameAttributePath = root.get(Metadata_.uuid);
                Predicate uuidEqualPredicate = cb.equal(userNameAttributePath, cb.literal(uuid));
                return uuidEqualPredicate;
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
     * Creates a specification for finding all metadata containing a {@link MetadataCategory} with the provided category
     *
     * @param category the category to use in the search
     * @return a specification for finding all metadata containing a {@link MetadataCategory} with the provided category
     */
    public static Specification<Metadata> hasCategory(final MetadataCategory category) {
        return new Specification<Metadata>() {
            @Override
            public Predicate toPredicate(Root<Metadata> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                final Expression<Set<MetadataCategory>> categoriesPath = root.get(Metadata_.categories);

                return cb.isMember(category, categoriesPath);
            }
        };
    }


    public static Specification<Metadata> isIso19139Schema() {
        return new Specification<Metadata>() {
            @Override
            public Predicate toPredicate(Root<Metadata> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                Path<String> schemaIdAttributePath =  root.get(Metadata_.dataInfo).get(MetadataDataInfo_.schemaId);
                Predicate likeSchemaIdPredicate = cb.like(schemaIdAttributePath, cb.literal("iso19139"));
                return likeSchemaIdPredicate;
            }
        };
    }
}
