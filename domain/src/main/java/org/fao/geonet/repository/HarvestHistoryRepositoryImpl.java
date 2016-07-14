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
import org.fao.geonet.domain.HarvestHistory;
import org.fao.geonet.domain.HarvestHistory_;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.CriteriaUpdate;
import javax.persistence.criteria.Root;

import java.util.Collection;

/**
 * Implementation for custom methods for the HarvestHistoryRepository class.
 * <p/>
 * User: Jesse Date: 9/20/13 Time: 4:03 PM
 */
public class HarvestHistoryRepositoryImpl implements HarvestHistoryRepositoryCustom {

    @PersistenceContext
    EntityManager _entityManager;

    @Override
    @Transactional
    public int deleteAllById(Collection<Integer> ids) {
        final CriteriaBuilder cb = _entityManager.getCriteriaBuilder();
        CriteriaDelete<HarvestHistory> delete = cb.createCriteriaDelete(HarvestHistory.class);
        final Root<HarvestHistory> root = delete.from(HarvestHistory.class);

        delete.where(root.get(HarvestHistory_.id).in(ids));

        final int deleted = _entityManager.createQuery(delete).executeUpdate();

        _entityManager.flush();
        _entityManager.clear();

        return deleted;
    }

    @Override
    @Transactional
    public int markAllAsDeleted(@Nonnull String harvesterUuid) {
        final CriteriaBuilder cb = _entityManager.getCriteriaBuilder();
        final CriteriaUpdate<HarvestHistory> update = cb.createCriteriaUpdate(HarvestHistory.class);
        final Root<HarvestHistory> root = update.from(HarvestHistory.class);

        update.set(root.get(HarvestHistory_.deleted_JpaWorkaround), Constants.YN_TRUE);
        update.where(cb.equal(root.get(HarvestHistory_.harvesterUuid), harvesterUuid));

        int updated = _entityManager.createQuery(update).executeUpdate();
        _entityManager.flush();
        _entityManager.clear();

        return updated;

    }
}
