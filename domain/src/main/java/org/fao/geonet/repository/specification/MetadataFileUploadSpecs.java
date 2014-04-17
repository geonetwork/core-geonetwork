package org.fao.geonet.repository.specification;

import org.fao.geonet.domain.*;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.*;
import java.util.Collection;

/**
 *  Specifications for querying {@link org.fao.geonet.repository.MetadataFileUploadRepository}.
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
