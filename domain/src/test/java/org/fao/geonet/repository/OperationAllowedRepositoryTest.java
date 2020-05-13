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


import org.fao.geonet.domain.OperationAllowed;
import org.fao.geonet.domain.OperationAllowedId;
import org.fao.geonet.domain.OperationAllowedId_;
import org.fao.geonet.repository.specification.OperationAllowedSpecs;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class OperationAllowedRepositoryTest extends AbstractOperationsAllowedTest {

    @Test
    public void testSaveById() {
        OperationAllowed unsaved = new OperationAllowed(new OperationAllowedId(_md1.getId(), _allGroup.getId(), _viewOp.getId()));
        OperationAllowed newOp = _opAllowRepo.save(unsaved);

        assertEquals(_md1.getId(), newOp.getId().getMetadataId());
        assertEquals(_allGroup.getId(), newOp.getId().getGroupId());
        assertEquals(_viewOp.getId(), newOp.getId().getOperationId());
    }


    @Test
    public void testFindByMetadataStringId() {
        List<OperationAllowed> opAllowedFound = _opAllowRepo.findByMetadataId(String.valueOf(_md1.getId()));
        assertEquals(3, opAllowedFound.size());
        assertTrue(opAllowedFound.contains(_opAllowed1));
        assertFalse(opAllowedFound.contains(_opAllowed2));
        assertTrue(opAllowedFound.contains(_opAllowed3));
        assertTrue(opAllowedFound.contains(_opAllowed4));
    }

    @Test
    public void testFindByMetadataId() {
        List<OperationAllowed> opAllowedFound = _opAllowRepo.findAllById_MetadataId(_md1.getId());
        assertEquals(3, opAllowedFound.size());
        assertTrue(opAllowedFound.contains(_opAllowed1));
        assertFalse(opAllowedFound.contains(_opAllowed2));
        assertTrue(opAllowedFound.contains(_opAllowed3));
        assertTrue(opAllowedFound.contains(_opAllowed4));
    }

    @Test
    public void testFindByOperationId() {
        List<OperationAllowed> opAllowedFound = _opAllowRepo.findAllById_OperationId(_viewOp.getId());
        assertEquals(2, opAllowedFound.size());
        assertTrue(opAllowedFound.contains(_opAllowed1));
        assertFalse(opAllowedFound.contains(_opAllowed2));
        assertFalse(opAllowedFound.contains(_opAllowed3));
        assertTrue(opAllowedFound.contains(_opAllowed4));
    }

    @Test
    public void testFindByGroupId() {
        List<OperationAllowed> opAllowedFound = _opAllowRepo.findAllById_GroupId(_allGroup.getId());

        assertEquals(1, opAllowedFound.size());
        assertTrue(opAllowedFound.contains(_opAllowed1));
        assertFalse(opAllowedFound.contains(_opAllowed2));
        assertFalse(opAllowedFound.contains(_opAllowed3));
        assertFalse(opAllowedFound.contains(_opAllowed4));
    }

    @Test
    public void testFindByGroupIdAndMetadataIdAndOperationId() {
        OperationAllowed opAllowedFound = _opAllowRepo.findOneById_GroupIdAndId_MetadataIdAndId_OperationId(_intranetGroup.getId(),
            _md1.getId(),
            _viewOp.getId());
        assertEquals(_opAllowed4.getId(), opAllowedFound.getId());

        opAllowedFound = _opAllowRepo.findOneById_GroupIdAndId_MetadataIdAndId_OperationId(_allGroup.getId(), _md1.getId(),
            _viewOp.getId());
        assertEquals(_opAllowed1.getId(), opAllowedFound.getId());

        opAllowedFound = _opAllowRepo.findOneById_GroupIdAndId_MetadataIdAndId_OperationId(_allGroup.getId(), _md1.getId(),
            Integer.MAX_VALUE / 2);
        assertNull(opAllowedFound);
    }

    @Test
    public void deleteAllByMetadataIdExceptGroupId() {
        System.out.println("deleteAllByMdIdNotGroup");
        assertEquals(4, _opAllowRepo.count());
        _opAllowRepo.deleteAllByMetadataIdExceptGroupId(_md1.getId(), new int[]{
            _allGroup.getId()
        });

        assertEquals(2, _opAllowRepo.count());
        List<OperationAllowed> opAllowedFound = _opAllowRepo.findAll();
        assertTrue(opAllowedFound.contains(_opAllowed1));
        assertTrue(opAllowedFound.contains(_opAllowed2));

        assertEquals(2, opAllowedFound.size());
        assertFalse(opAllowedFound.contains(_opAllowed3));
        assertFalse(opAllowedFound.contains(_opAllowed4));
    }


    @Test
    public void testDeleteByMetadataId() {
        _opAllowRepo.deleteAllByMetadataId(_md1.getId());

        List<OperationAllowed> opAllowedFound = _opAllowRepo.findAll();

        assertFalse(opAllowedFound.contains(_opAllowed1));
        assertTrue(opAllowedFound.contains(_opAllowed2));
        assertEquals(1, opAllowedFound.size());
        assertFalse(opAllowedFound.contains(_opAllowed3));
        assertFalse(opAllowedFound.contains(_opAllowed4));

        assertNull(_opAllowRepo.findOne(_opAllowed1.getId()));
    }


    @Test
    public void testDeleteByGroupId() {
        _opAllowRepo.deleteAllByGroupId(_allGroup.getId());

        List<OperationAllowed> opAllowedFound = _opAllowRepo.findAll();

        assertFalse(opAllowedFound.contains(_opAllowed1));
        assertTrue(opAllowedFound.contains(_opAllowed2));
        assertEquals(3, opAllowedFound.size());
        assertTrue(opAllowedFound.contains(_opAllowed3));
        assertTrue(opAllowedFound.contains(_opAllowed4));

        assertNull(_opAllowRepo.findOne(_opAllowed1.getId()));
    }

    @Test
    public void testFindAllIds() {
        List<Integer> opAllowedFound = _opAllowRepo.findAllIds(OperationAllowedSpecs.hasOperationId(_viewOp.getId()),
            OperationAllowedId_.metadataId);
        assertEquals(1, opAllowedFound.size());
        assertTrue(opAllowedFound.contains(_md1.getId()));
    }
}
