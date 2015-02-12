package org.fao.geonet.repository.specification;

import org.fao.geonet.domain.Constants;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.domain.MetadataCategory;
import org.fao.geonet.domain.MetadataDataInfo_;
import org.fao.geonet.domain.MetadataHarvestInfo_;
import org.fao.geonet.domain.MetadataSourceInfo_;
import org.fao.geonet.domain.MetadataType;
import org.fao.geonet.domain.Metadata_;
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
                Path<String> schemaIdAttributePath =  root.get(Metadata_.dataInfo).get(MetadataDataInfo_.schemaId);
                Predicate likeSchemaIdPredicate = cb.like(schemaIdAttributePath, cb.literal("iso19139"));
                return likeSchemaIdPredicate;
            }
        };
    }
}
