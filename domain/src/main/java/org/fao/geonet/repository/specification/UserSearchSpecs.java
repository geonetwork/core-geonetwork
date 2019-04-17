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


/**
 * Specifications for querying {@link org.fao.geonet.repository.UserSearchRepository}.
 *
 */
public final class UserSearchSpecs {
    private UserSearchSpecs() {
        // no instantiation
    }

    public static Specification<UserSearch> containsTextInCreatorOrTranslations(String text) {
        if (!text.contains("%")) {
            text = "%" + text + "%";
        }
        final String finalText = text;

        return new Specification<UserSearch>() {
            @Override
            public Predicate toPredicate(Root<UserSearch> root, CriteriaQuery<?> cq, CriteriaBuilder builder) {

                MapJoin<UserSearch, String, String> mapRoot = root.joinMap("labelTranslations");

                Path<String> creatorPath = root.get(UserSearch_.creator).get(User_.username);

                return  builder.or(builder.like(creatorPath, finalText),
                    builder.like(mapRoot.value(), finalText));
            }
        };
    }

}
