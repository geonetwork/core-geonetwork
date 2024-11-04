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

import org.fao.geonet.domain.Metadata;
import org.fao.geonet.domain.MetadataCategory;
import org.fao.geonet.domain.MetadataCategory_;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Root;

/**
 * Implementation for custom category methods.
 * <p/>
 * User: Jesse Date: 9/9/13 Time: 8:00 PM
 */
public class MetadataCategoryRepositoryCustomImpl implements MetadataCategoryRepositoryCustom {

    @PersistenceContext
    private EntityManager _entityManager;


    @Nullable
    @Override
    public MetadataCategory findOneByNameIgnoreCase(@Nonnull String name) {
        final CriteriaBuilder cb = _entityManager.getCriteriaBuilder();
        final CriteriaQuery<MetadataCategory> cbQuery = cb.createQuery(MetadataCategory.class);
        final Root<MetadataCategory> root = cbQuery.from(MetadataCategory.class);
        final Expression<String> lowerName = cb.lower(root.get(MetadataCategory_.name));
        final Expression<String> lowerRequiredName = cb.lower(cb.literal(name));
        cbQuery.where(cb.equal(lowerName, lowerRequiredName));
        return _entityManager.createQuery(cbQuery).getSingleResult();
    }

    @Override
    @Transactional
    public void deleteCategoryAndMetadataReferences(int id) {
        /*
         * Start of HACK.
         *
         * The following select seems to be needed so that the delete from below will actually delete elements...
         * At least in the unit tests.
         */
        final Query nativeQuery2 = _entityManager.createNativeQuery("Select * from " + Metadata.METADATA_CATEG_JOIN_TABLE_NAME + " WHERE "
            + Metadata.METADATA_CATEG_JOIN_TABLE_CATEGORY_ID + "= :id");
        nativeQuery2.setParameter("id", id);
        nativeQuery2.setMaxResults(1);
        nativeQuery2.getResultList();
        // END HACK

        final Query nativeQuery = _entityManager.createNativeQuery("DELETE FROM " + Metadata.METADATA_CATEG_JOIN_TABLE_NAME + " WHERE "
            + Metadata.METADATA_CATEG_JOIN_TABLE_CATEGORY_ID + "=?");
        nativeQuery.setParameter(1, id);
        nativeQuery.executeUpdate();


        _entityManager.flush();
        _entityManager.clear();

        final MetadataCategory reference = _entityManager.getReference(MetadataCategory.class, id);
        _entityManager.remove(reference);


        _entityManager.flush();
        _entityManager.clear();
    }
}
