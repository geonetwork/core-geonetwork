package org.fao.geonet.repository.specification;

import org.fao.geonet.domain.Constants;
import org.fao.geonet.domain.MetadataUrnTemplate;
import org.fao.geonet.domain.MetadataUrnTemplate_;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.*;

/**
 *
 * @author Jose García
 */
public class MetadataUrnTemplateSpecs {
    private MetadataUrnTemplateSpecs() {
        // don't permit instantiation
    }

    public static Specification<MetadataUrnTemplate> isDefault(final boolean isDefault) {
        return new Specification<MetadataUrnTemplate>() {
            @Override
            public Predicate toPredicate(Root<MetadataUrnTemplate> root, CriteriaQuery<?> query, CriteriaBuilder cb) {

                Path<Character> defaultAttributePath = root.get(MetadataUrnTemplate_.default_JPAWorkaround);
                Predicate equalDefaultPredicate = cb.equal(defaultAttributePath,  cb.literal(Constants.toYN_EnabledChar(isDefault)));
                return equalDefaultPredicate;
            }
        };
    }
}