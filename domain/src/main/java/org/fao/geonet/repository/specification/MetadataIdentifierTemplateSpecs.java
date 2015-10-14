package org.fao.geonet.repository.specification;

import org.fao.geonet.domain.Constants;
import org.fao.geonet.domain.MetadataIdentifierTemplate;
import org.fao.geonet.domain.MetadataIdentifierTemplate_;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.*;

/**
 * Specifications for querying MetadataIdentifierTemplate.
 *
 * @author Jose García
 */
public class MetadataIdentifierTemplateSpecs {
    private MetadataIdentifierTemplateSpecs() {
        // don't permit instantiation
    }

    public static Specification<MetadataIdentifierTemplate> isSystemProvided(final boolean isSystemProvided) {
        return new Specification<MetadataIdentifierTemplate>() {
            @Override
            public Predicate toPredicate(Root<MetadataIdentifierTemplate> root, CriteriaQuery<?> query, CriteriaBuilder cb) {

                Path<Character> defaultAttributePath = root.get(MetadataIdentifierTemplate_.systemProvided_JPAWorkaround);
                Predicate systemProvidedDefaultPredicate = cb.equal(defaultAttributePath,  cb.literal(Constants.toYN_EnabledChar(isSystemProvided)));
                return systemProvidedDefaultPredicate;
            }
        };
    }
}