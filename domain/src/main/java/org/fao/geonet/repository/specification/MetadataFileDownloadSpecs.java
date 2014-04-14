package org.fao.geonet.repository.specification;

import org.fao.geonet.domain.*;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.*;
import java.util.Collection;

/**
 *  Specifications for querying {@link org.fao.geonet.repository.MetadataFileDownloadRepository}.
 *
 * @author Jose García
 */
public class MetadataFileDownloadSpecs {
    private MetadataFileDownloadSpecs() {
        // no instantiation
    }

    public static Specification<MetadataFileDownload> downloadDateBetweenAndByGroups(final ISODate downloadFrom,
                                                                  final ISODate downloadTo,
                                                                  final Collection<Integer> groups) {
        return new Specification<MetadataFileDownload>() {
            @Override
            public Predicate toPredicate(Root<MetadataFileDownload> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                Path<String> downloadDateAttributePath = root.get(MetadataFileDownload_.downloadDate);
                Path<Integer> metadataIdAttributePath = root.get(MetadataFileDownload_.metadataId);

                Predicate downloadDateBetweenPredicate = cb.between(downloadDateAttributePath,
                        downloadFrom.toString(), downloadTo.toString());

                if (!groups.isEmpty()) {
                    final Root<Metadata> metadataRoot = query.from(Metadata.class);
                    final Path<Integer> groupOwnerPath = metadataRoot.get(Metadata_.sourceInfo).get(MetadataSourceInfo_.groupOwner);

                    Predicate inGroups = groupOwnerPath.in(groups);

                    downloadDateBetweenPredicate = cb.and(cb.equal(metadataRoot.get(Metadata_.id),
                            metadataIdAttributePath), cb.and(downloadDateBetweenPredicate, inGroups));
                }

                return downloadDateBetweenPredicate;
            }
        };
    }
}
