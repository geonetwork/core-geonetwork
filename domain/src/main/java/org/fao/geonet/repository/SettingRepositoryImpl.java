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

import static org.fao.geonet.domain.Constants.toYN_EnabledChar;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.fao.geonet.domain.Setting;
import org.fao.geonet.domain.Setting_;

/**
 * Implementation for MetadataNotifierRepositoryCustom methods.
 * <p/>
 * User: francois Date: 8/28/13 Time: 7:31 AM To change this template use File | Settings | File
 * Templates.
 */
public class SettingRepositoryImpl implements SettingRepositoryCustom {

    @PersistenceContext
    private EntityManager _entityManager;


    @Override
    public List<Setting> findAllByInternal(boolean internal) {

        CriteriaBuilder cb = _entityManager.getCriteriaBuilder();
        CriteriaQuery<Setting> cquery = cb.createQuery(Setting.class);
        Root<Setting> root = cquery.from(Setting.class);
        char internalChar = toYN_EnabledChar(internal);
        cquery.where(cb.equal(root.get(Setting_.internal_JpaWorkaround), internalChar));
        return _entityManager.createQuery(cquery).getResultList();
    }

}
