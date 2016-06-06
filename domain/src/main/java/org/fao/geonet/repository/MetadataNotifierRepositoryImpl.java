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

import org.fao.geonet.domain.MetadataNotifier;
import org.fao.geonet.domain.MetadataNotifier_;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import java.util.List;

import static org.fao.geonet.domain.Constants.toYN_EnabledChar;

/**
 * Implementation for MetadataNotifierRepositoryCustom methods.
 * <p/>
 * User: Jesse Date: 8/28/13 Time: 7:31 AM To change this template use File | Settings | File
 * Templates.
 */
public class MetadataNotifierRepositoryImpl implements MetadataNotifierRepositoryCustom {

    @PersistenceContext
    private EntityManager _entityManager;

    @Override
    public List<MetadataNotifier> findAllByEnabled(boolean enabled) {
        CriteriaBuilder cb = _entityManager.getCriteriaBuilder();
        CriteriaQuery<MetadataNotifier> cquery = cb.createQuery(MetadataNotifier.class);
        Root<MetadataNotifier> root = cquery.from(MetadataNotifier.class);
        char enabledChar = toYN_EnabledChar(enabled);
        cquery.where(cb.equal(root.get(MetadataNotifier_.enabled_JPAWorkaround), enabledChar));
        return _entityManager.createQuery(cquery).getResultList();
    }

}
