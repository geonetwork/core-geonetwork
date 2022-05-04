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

import static org.fao.geonet.repository.HarvesterSettingRepository.ID_PREFIX;
import static org.fao.geonet.repository.HarvesterSettingRepository.SEPARATOR;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.collections.CollectionUtils;
import org.fao.geonet.domain.HarvesterSetting;
import org.fao.geonet.domain.HarvesterSetting_;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Lists;

public class HarvesterSettingRepositoryImpl implements HarvesterSettingRepositoryCustom {
    @PersistenceContext
    private EntityManager _entityManager;

    @Override
    public List<HarvesterSetting> findAllByPath(String pathToSetting) {
        StringTokenizer stringTokenizer = new StringTokenizer(pathToSetting, SEPARATOR);

        int countTokens = stringTokenizer.countTokens();
        List<String> pathSegments = new ArrayList<String>(countTokens);
        while (stringTokenizer.hasMoreTokens()) {
            String child = stringTokenizer.nextToken();

            if (child.startsWith(ID_PREFIX)) {
                pathSegments.clear();
                pathSegments.add(child);
            } else {
                pathSegments.add(child);
            }
        }

        List<HarvesterSetting> currentSettings = new ArrayList<HarvesterSetting>();
        String firstSegment = pathSegments.get(0);
        if (firstSegment.startsWith(ID_PREFIX)) {
            int id = Integer.parseInt(firstSegment.substring(ID_PREFIX.length()));
            HarvesterSetting setting = _entityManager.find(HarvesterSetting.class, id);
            if (setting == null) {
                pathSegments = new ArrayList<String>();
            } else {
                currentSettings.add(setting);
                pathSegments = pathSegments.subList(1, pathSegments.size());
            }
        } else {
            // get all settings
            currentSettings = findRoots();
            for (HarvesterSetting currentSetting : currentSettings) {
                if (currentSetting.getName().equals(firstSegment)) {
                    pathSegments.remove(0);
                    currentSettings = Arrays.asList(currentSetting);
                    break;
                }
            }
        }

        for (String childName : pathSegments) {
            List<HarvesterSetting> oldSettings = currentSettings;
            currentSettings = new LinkedList<HarvesterSetting>();
            for (HarvesterSetting setting : oldSettings) {
                List<HarvesterSetting> children = findChildrenByName(setting.getId(), childName);
                currentSettings.addAll(children);
            }
        }
        return currentSettings;
    }

    @Override
    public HarvesterSetting findOneByPath(String pathToSetting) {
        List<HarvesterSetting> settings = findAllByPath(pathToSetting);
        if (settings.isEmpty()) {
            return null;
        } else {
            return settings.get(0);
        }
    }

    @Override
    public List<HarvesterSetting> findRoots() {
        CriteriaBuilder criteriaBuilder = _entityManager.getCriteriaBuilder();
        CriteriaQuery<HarvesterSetting> query = criteriaBuilder.createQuery(HarvesterSetting.class);

        Root<HarvesterSetting> root = query.from(HarvesterSetting.class);
        query.where(criteriaBuilder.isNull(root.get(HarvesterSetting_.parent)));

        return _entityManager.createQuery(query).getResultList();
    }

    @Override
    public List<HarvesterSetting> findAllChildren(int parentid) {
        CriteriaBuilder criteriaBuilder = _entityManager.getCriteriaBuilder();
        CriteriaQuery<HarvesterSetting> query = criteriaBuilder.createQuery(HarvesterSetting.class);

        Root<HarvesterSetting> root = query.from(HarvesterSetting.class);
        query.where(criteriaBuilder.equal(root.get(HarvesterSetting_.parent), parentid));

        return _entityManager.createQuery(query).getResultList();
    }

    @Override
    public List<Integer> findAllChildIds(int parentid) {
        CriteriaBuilder criteriaBuilder = _entityManager.getCriteriaBuilder();
        CriteriaQuery<Integer> query = criteriaBuilder.createQuery(Integer.class);

        Root<HarvesterSetting> root = query.from(HarvesterSetting.class);
        query.where(criteriaBuilder.equal(root.get(HarvesterSetting_.parent), parentid));
        query.select(root.get(HarvesterSetting_.id));

        return _entityManager.createQuery(query).getResultList();
    }

    @Override
    public List<HarvesterSetting> findChildrenByName(int parentid, String name) {
        CriteriaBuilder criteriaBuilder = _entityManager.getCriteriaBuilder();
        CriteriaQuery<HarvesterSetting> query = criteriaBuilder.createQuery(HarvesterSetting.class);

        Root<HarvesterSetting> root = query.from(HarvesterSetting.class);
        Predicate equalParentId = criteriaBuilder.equal(root.get(HarvesterSetting_.parent), parentid);
        Predicate equalName = criteriaBuilder.equal(root.get(HarvesterSetting_.name), name);
        query.where(criteriaBuilder.and(equalParentId, equalName));

        return _entityManager.createQuery(query).getResultList();
    }

    @Override
    public List<HarvesterSetting> findAllByNames(List<String> names) {
        CriteriaBuilder criteriaBuilder = _entityManager.getCriteriaBuilder();
        CriteriaQuery<HarvesterSetting> query = criteriaBuilder.createQuery(HarvesterSetting.class);

        Root<HarvesterSetting> root = query.from(HarvesterSetting.class);
        query.select(root);
        if (CollectionUtils.isNotEmpty(names)) {
            query.where(root.get(HarvesterSetting_.name).in(names));
        }

        return _entityManager.createQuery(query).getResultList();
    }

}
