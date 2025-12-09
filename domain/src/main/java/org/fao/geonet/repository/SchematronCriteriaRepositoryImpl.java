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

import org.fao.geonet.domain.SchematronCriteria;
import org.fao.geonet.domain.SchematronCriteriaGroup;
import org.fao.geonet.domain.SchematronCriteriaGroup_;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import java.util.Iterator;
import java.util.List;

/**
 * Override behaviour of certain operations.
 *
 * Created by Jesse on 3/7/14.
 */
public class SchematronCriteriaRepositoryImpl {
    @PersistenceContext
    EntityManager _entityManager;

    /**
     * Override method in CRUD repository because criteria need to be removed from Group in order to
     * be deleted.
     *
     * @param id id of criteria to delete
     */
    @Transactional
    @Modifying(clearAutomatically=true)
    public void delete(Integer id) {
        SchematronCriteria criteria = _entityManager.getReference(SchematronCriteria.class, id);
        final CriteriaBuilder builder = _entityManager.getCriteriaBuilder();
        final CriteriaQuery<SchematronCriteriaGroup> query = builder.createQuery(SchematronCriteriaGroup.class);
        final Root<SchematronCriteriaGroup> root = query.from(SchematronCriteriaGroup.class);
        final Predicate criteriaIsAMemberOfGroup = builder.isMember(criteria, root.get(SchematronCriteriaGroup_.criteria));
        query.where(criteriaIsAMemberOfGroup);

        List<SchematronCriteriaGroup> groups = _entityManager.createQuery(query).getResultList();

        for (SchematronCriteriaGroup group : groups) {
            Iterator<SchematronCriteria> iterator = group.getCriteria().iterator();
            while (iterator.hasNext()) {
                if (id == iterator.next().getId()) {
                    iterator.remove();
                }
            }
            _entityManager.persist(group);
        }
    }

    /**
     * Override method in CRUD repository because criteria need to be removed from Group in order to
     * be deleted.
     *
     * @param criteria criteria to delete
     */
    @Transactional
    public void delete(Iterable<SchematronCriteria> criteria) {
        for (SchematronCriteria schematronCriteria : criteria) {
            delete(schematronCriteria.getId());
        }

    }

    /**
     * Override method in CRUD repository because criteria need to be removed from Group in order to
     * be deleted.
     *
     * @param criteria criteria to delete
     */
    @Transactional
    public void delete(SchematronCriteria criteria) {
        delete(criteria.getId());
    }
}
