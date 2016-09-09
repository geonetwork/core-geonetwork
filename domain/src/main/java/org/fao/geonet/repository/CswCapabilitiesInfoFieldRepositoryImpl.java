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

import org.fao.geonet.domain.CswCapabilitiesInfoField;
import org.fao.geonet.domain.CswCapabilitiesInfoField_;

import javax.annotation.Nonnull;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import java.util.Collection;
import java.util.List;

/**
 * Implementation of the custom repository methods.
 * <p/>
 * User: Jesse Date: 9/20/13 Time: 10:36 AM
 */
public class CswCapabilitiesInfoFieldRepositoryImpl implements CswCapabilitiesInfoFieldRepositoryCustom {
    @PersistenceContext
    private EntityManager _EntityManager;

    @Override
    public CswCapabilitiesInfo findCswCapabilitiesInfo(final String languageCode) {
        final CriteriaBuilder cb = _EntityManager.getCriteriaBuilder();
        final CriteriaQuery<CswCapabilitiesInfoField> query = cb.createQuery(CswCapabilitiesInfoField.class);
        final Root<CswCapabilitiesInfoField> root = query.from(CswCapabilitiesInfoField.class);
        query.where(cb.equal(root.get(CswCapabilitiesInfoField_.langId), languageCode));
        List<CswCapabilitiesInfoField> allFieldsForLang = _EntityManager.createQuery(query).getResultList();
        return new CswCapabilitiesInfo(languageCode, allFieldsForLang);
    }

    @Override
    public void save(@Nonnull final CswCapabilitiesInfo info) {
        Collection<CswCapabilitiesInfoField> fields = info.getFields();

        for (CswCapabilitiesInfoField field : fields) {
            if (field.getId() == 0) {
                _EntityManager.persist(field);
            } else {
                _EntityManager.merge(field);
            }
        }
    }
}
