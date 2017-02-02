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

/**
 *
 */
package org.fao.geonet.repository;

import org.fao.geonet.domain.UserSavedSelection;
import org.fao.geonet.domain.UserSavedSelectionId_;
import org.fao.geonet.domain.UserSavedSelection_;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.ParameterExpression;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementation object for methods in {@link UserSavedSelectionRepositoryCustom}.
 */
public class UserSavedSelectionRepositoryImpl
    implements UserSavedSelectionRepositoryCustom {

    @PersistenceContext
    private EntityManager _entityManager;


    @Override
    public List<String> findMetadata(Integer selectionId, Integer userId) {
        CriteriaBuilder builder = _entityManager.getCriteriaBuilder();
        CriteriaQuery<UserSavedSelection> query = builder.createQuery(UserSavedSelection.class);

        Root<UserSavedSelection> root = query.from(UserSavedSelection.class);
//        query.select();
        ParameterExpression<Integer> selectionParam = builder.parameter(
            Integer.class,
            "selectionParam");
        ParameterExpression<Integer> userParam = builder.parameter(
            Integer.class,
            "userParam");
        query.where(builder.and(builder.equal(
            selectionParam,
            root.get(UserSavedSelection_.id).get(UserSavedSelectionId_.selectionId)
        ), builder.equal(
            userParam,
            root.get(UserSavedSelection_.id).get(UserSavedSelectionId_.userId)
        )));
        List<UserSavedSelection> umsResults = _entityManager.createQuery(query)
            .setParameter("selectionParam", selectionId)
            .setParameter("userParam", userId)
            .getResultList();
        List<String> result = new ArrayList<>();
        umsResults.forEach(e -> result.add(e.getId().getMetadataUuid()));
        return result;
    }

    @Override
    public int deleteAllBySelection(Integer selectionId) {
        final String selectionIdPath =
            SortUtils.createPath(UserSavedSelection_.id, UserSavedSelectionId_.selectionId);
        final String qlString =
            "DELETE FROM " + UserSavedSelection.class.getSimpleName() +
                " WHERE " + selectionIdPath + " = " + selectionId;
        final int deleted = _entityManager.createQuery(qlString).executeUpdate();
        _entityManager.flush();
        _entityManager.clear();
        return deleted;
    }

    @Override
    public int deleteAllByUser(Integer userId) {
        final String userIdPath =
            SortUtils.createPath(UserSavedSelection_.id, UserSavedSelectionId_.userId);
        final String qlString =
            "DELETE FROM " + UserSavedSelection.class.getSimpleName() +
                " WHERE " + userIdPath + " = " + userId;
        final int deleted = _entityManager.createQuery(qlString).executeUpdate();
        _entityManager.flush();
        _entityManager.clear();
        return deleted;
    }

    @Override
    public int deleteAllBySelectionAndUser(Integer selection, Integer userId) {
        final String selectionIdPath =
            SortUtils.createPath(UserSavedSelection_.id, UserSavedSelectionId_.selectionId);
        final String userIdPath =
            SortUtils.createPath(UserSavedSelection_.id, UserSavedSelectionId_.userId);
        final String qlString =
            "DELETE FROM " + UserSavedSelection.class.getSimpleName() +
                " WHERE " + selectionIdPath + " = " + selection +
                " AND " + userIdPath + " = " + userId;
        final int deleted = _entityManager.createQuery(qlString).executeUpdate();
        _entityManager.flush();
        _entityManager.clear();
        return deleted;
    }
}
