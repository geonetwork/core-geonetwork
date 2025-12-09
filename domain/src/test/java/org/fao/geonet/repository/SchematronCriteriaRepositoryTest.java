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
import org.fao.geonet.domain.SchematronCriteria_;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.fao.geonet.repository.SchematronCriteriaGroupRepositoryTest.newGroup;
import static org.fao.geonet.repository.SchematronCriteriaGroupRepositoryTest.newSchematronCriteria;
import static org.junit.Assert.*;

/**
 * Test SchematronCriteriaRepository.
 *
 * Created by Jesse on 1/21/14.
 */
public class SchematronCriteriaRepositoryTest extends AbstractSpringDataTest {
    @Autowired
    private SchematronRepository _schematronRepo;
    @Autowired
    private SchematronCriteriaRepository _repo;
    @Autowired
    private SchematronCriteriaGroupRepository criteriaGroupRepository;
    private AtomicInteger _inc = new AtomicInteger(10);

    @Test
    public void testFindOne() throws Exception {
        final SchematronCriteriaGroup criteriaGroup = criteriaGroupRepository.save(newGroup(_inc, _schematronRepo));

        final SchematronCriteria criteria = criteriaGroup.getCriteria().get(0);
        final SchematronCriteria found = _repo.findById(criteria.getId()).get();

        assertSameContents(criteria, found);
    }

    @Test
    public void testDelete() throws Exception {
        final SchematronCriteriaGroup criteriaGroup = criteriaGroupRepository.save(newGroup(_inc, _schematronRepo));
        final SchematronCriteria criteria = criteriaGroup.getCriteria().get(0);

        assertTrue(_repo.existsById(criteria.getId()));
        criteriaGroup.getCriteria().remove(criteria);
        _repo.deleteById(criteria.getId());
        assertFalse(_repo.existsById(criteria.getId()));
    }

    @Test
    public void testDeleteEntity() throws Exception {
        final SchematronCriteriaGroup criteriaGroup = criteriaGroupRepository.save(newGroup(_inc, _schematronRepo));
        final SchematronCriteria criteria = criteriaGroup.getCriteria().get(0);
        assertTrue(_repo.existsById(criteria.getId()));
        criteriaGroup.getCriteria().remove(criteria);
        _repo.delete(criteria);
        assertFalse(_repo.existsById(criteria.getId()));
    }

    @Test
    public void testSave() throws Exception {
        final SchematronCriteriaGroup criteriaGroup = criteriaGroupRepository.save(newGroup(_inc, _schematronRepo));
        final SchematronCriteria criteria = criteriaGroup.getCriteria().get(0);

        final String newValue = "newValue";
        criteria.setValue(newValue);
        _repo.save(criteria);
        final List<SchematronCriteria> all = _repo.findAll(new Specification<SchematronCriteria>() {
            @Override
            public Predicate toPredicate(Root<SchematronCriteria> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                return cb.equal(root.get(SchematronCriteria_.id), criteria.getId());
            }
        });

        assertEquals(1, all.size());
        assertEquals(newValue, all.get(0).getValue());

        final SchematronCriteria newCriteria = newSchematronCriteria(_inc);
        criteriaGroup.addCriteria(newCriteria);
        _repo.save(newCriteria);

        SchematronCriteriaGroup reloaded = criteriaGroupRepository.findById(criteriaGroup.getId()).get();
        assertTrue(reloaded.getCriteria().contains(newCriteria));

        final List<SchematronCriteriaGroup> allGroups = criteriaGroupRepository.findAll();

        assertEquals(1, allGroups.size());
        assertTrue(allGroups.get(0).getCriteria().contains(newCriteria));
    }


    @Test
    public void testDeleteMany() throws Exception {
        final SchematronCriteriaGroup criteriaGroup1 = criteriaGroupRepository.save(newGroup(_inc, _schematronRepo));
        final SchematronCriteriaGroup criteriaGroup2 = criteriaGroupRepository.save(newGroup(_inc, _schematronRepo));
        final SchematronCriteria criteria1 = criteriaGroup1.getCriteria().get(0);
        final SchematronCriteria criteria2 = criteriaGroup2.getCriteria().get(0);
        assertTrue(_repo.existsById(criteria1.getId()));
        assertTrue(_repo.existsById(criteria2.getId()));
        criteriaGroup1.getCriteria().remove(criteria1);
        criteriaGroup2.getCriteria().remove(criteria2);
        _repo.deleteAll(Arrays.asList(criteria1, criteria2));
        assertFalse(_repo.existsById(criteria1.getId()));
        assertFalse(_repo.existsById(criteria2.getId()));
    }


}
