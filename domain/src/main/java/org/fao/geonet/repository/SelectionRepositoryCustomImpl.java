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

import org.fao.geonet.domain.Selection;
import org.fao.geonet.domain.Selection_;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Root;

/**
 * Implementation for custom selection methods.
 */
public class SelectionRepositoryCustomImpl implements SelectionRepositoryCustom {

    @PersistenceContext
    private EntityManager _entityManager;


    @Nullable
    @Override
    public Selection findOneByNameIgnoreCase(@Nonnull String name) {
        final CriteriaBuilder cb = _entityManager.getCriteriaBuilder();
        final CriteriaQuery<Selection> cbQuery = cb.createQuery(Selection.class);
        final Root<Selection> root = cbQuery.from(Selection.class);
        final Expression<String> lowerName = cb.lower(root.get(Selection_.name));
        final Expression<String> lowerRequiredName = cb.lower(cb.literal(name));
        cbQuery.where(cb.equal(lowerName, lowerRequiredName));
        return _entityManager.createQuery(cbQuery).getSingleResult();
    }
}
