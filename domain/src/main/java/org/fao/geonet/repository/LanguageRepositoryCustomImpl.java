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

package org.fao.geonet.repository;

import org.fao.geonet.domain.Constants;
import org.fao.geonet.domain.Language;
import org.fao.geonet.domain.Language_;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import java.util.List;

/**
 * Implement custom repository methods in LanguageRepositoryCustom User: Jesse Date: 8/30/13 Time:
 * 8:22 AM
 */
public class LanguageRepositoryCustomImpl implements LanguageRepositoryCustom {
    @PersistenceContext
    EntityManager _entityManager;

    @Override
    public List<Language> findAllByInspireFlag(boolean inspire) {
        char isInspireChar = Constants.toYN_EnabledChar(inspire);
        CriteriaBuilder cb = _entityManager.getCriteriaBuilder();
        CriteriaQuery<Language> query = cb.createQuery(Language.class);
        Root<Language> root = query.from(Language.class);
        query.where(cb.equal(root.get(Language_.inspire_JPAWorkaround), isInspireChar));

        return _entityManager.createQuery(query).getResultList();
    }
}
