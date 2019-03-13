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

/**
 * Override delete methods in {@link org.springframework.data.jpa.repository.support.SimpleJpaRepository}
 * so that full subtree is deleted and implement {@link HarvesterSettingRepositoryCustom}.
 * <p/>
 * This class is not a typical *Impl because it needs to override the delete methods in {@link
 * org.springframework.data.jpa.repository.support.SimpleJpaRepository}.  In order to do this you
 * have to create a subclass of {@link org.springframework.data.jpa.repository.support.SimpleJpaRepository}
 * (or {@link GeonetRepositoryImpl} which is subclass) and have {@link GeonetRepositoryFactoryBean}
 * return the custom implementation.
 * <p/>
 * An alternative would be to use aspectJ and the around cut-point to modify the behaviour of the
 * method. I decided on this way because it is more common and better understood techniques.
 * <p/>
 * <p/>
 * In addition to overriding the delete methods this class also implements the {@link
 * HarvesterSettingRepositoryCustom} interface.  These methods are not in a normal *Impl class
 * because the extra class is not needed and the delete methods call methods in that interface.
 * <p/>
 * User: Jesse Date: 10/25/13 Time: 7:53 AM
 */
public class HarvesterSettingRepositoryOverridesImpl extends GeonetRepositoryImpl<HarvesterSetting,
    Integer> implements HarvesterSettingRepositoryCustom {
    protected HarvesterSettingRepositoryOverridesImpl(Class<HarvesterSetting> domainClass, EntityManager entityManager) {
        super(domainClass, entityManager);
    }


    /**
     * Overrides the implementation in {@link org.springframework.data.jpa.repository.support.SimpleJpaRepository}.
     * This implementation deleted the thentity and all children.
     *
     * @param setting the entity to delete
     */
    public void delete(final HarvesterSetting setting) {
        delete(setting.getId());
    }

    /**
     * Overrides the implementation in {@link org.springframework.data.jpa.repository.support.SimpleJpaRepository}.
     * This implementation deleted the thentity and all children.
     *
     * @param settingId the id of the entity to delete
     */
    @Transactional
    @Modifying(clearAutomatically=true)
    public void delete(final int settingId) {
        final List<Integer> toRemove = Lists.newArrayList(settingId);
        int i = 0;
        while (i < toRemove.size()) {
            final int nextParentId = toRemove.get(i);
            toRemove.addAll(findAllChildIds(nextParentId));
            i++;
        }

        final CriteriaBuilder cb = _entityManager.getCriteriaBuilder();
        final CriteriaDelete<HarvesterSetting> delete = cb.createCriteriaDelete(HarvesterSetting.class);
        final Root<HarvesterSetting> root = delete.from(HarvesterSetting.class);

        delete.where(root.get(HarvesterSetting_.id).in(toRemove));

        _entityManager.createQuery(delete).executeUpdate();
    }


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
