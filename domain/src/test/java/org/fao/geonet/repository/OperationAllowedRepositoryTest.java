package org.fao.geonet.repository;

import static org.junit.Assert.*;

import java.util.List;

import org.fao.geonet.domain.OperationAllowed;
import org.junit.Test;

public class OperationAllowedRepositoryTest extends AbstractOperationsAllowedTest {

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
        List<OperationAllowed> opAllowedFound = _opAllowRepo.findByMetadataId(_md1.getId());
        assertEquals(3, opAllowedFound.size());
        assertTrue(opAllowedFound.contains(_opAllowed1));
        assertFalse(opAllowedFound.contains(_opAllowed2));
        assertTrue(opAllowedFound.contains(_opAllowed3));
        assertTrue(opAllowedFound.contains(_opAllowed4));
    }
    
    @Test
    public void testFindByOperationId() {
        List<OperationAllowed> opAllowedFound = _opAllowRepo.findByOperationId(_viewOp.getId());
        assertEquals(2, opAllowedFound.size());
        assertTrue(opAllowedFound.contains(_opAllowed1));
        assertFalse(opAllowedFound.contains(_opAllowed2));
        assertFalse(opAllowedFound.contains(_opAllowed3));
        assertTrue(opAllowedFound.contains(_opAllowed4));
    }
    
    @Test
    public void testFindByGroupId() {
        List<OperationAllowed> opAllowedFound = _opAllowRepo.findByGroupId(_allGroup.getId());

        assertEquals(1, opAllowedFound.size());
        assertTrue(opAllowedFound.contains(_opAllowed1));
        assertFalse(opAllowedFound.contains(_opAllowed2));
        assertFalse(opAllowedFound.contains(_opAllowed3));
        assertFalse(opAllowedFound.contains(_opAllowed4));
    }

    @Test
    public void testFindByGroupIdAndMetadataIdAndOperationId() {
        OperationAllowed opAllowedFound = _opAllowRepo.findByGroupIdAndMetadataIdAndOperationId(_intranetGroup.getId(), _md1.getId(), _viewOp.getId());
        assertEquals(_opAllowed4.getId(), opAllowedFound.getId());
        
        opAllowedFound = _opAllowRepo.findByGroupIdAndMetadataIdAndOperationId(_allGroup.getId(), _md1.getId(), _viewOp.getId());
        assertEquals(_opAllowed1.getId(), opAllowedFound.getId());
        
        opAllowedFound = _opAllowRepo.findByGroupIdAndMetadataIdAndOperationId(_allGroup.getId(), _md1.getId(), Integer.MAX_VALUE/2);
        assertNull(opAllowedFound);
    }        

    @Test
    public void testFindByGroupIdAndMetadataId() {
        List<OperationAllowed> opAllowedFound = _opAllowRepo.findByGroupIdAndMetadataId(_intranetGroup.getId(), _md1.getId());
        
        assertEquals(2, opAllowedFound.size());
        assertFalse(opAllowedFound.contains(_opAllowed1));
        assertFalse(opAllowedFound.contains(_opAllowed2));
        assertTrue(opAllowedFound.contains(_opAllowed3));
        assertTrue(opAllowedFound.contains(_opAllowed4));
        
        // search intranet + md2
        
        opAllowedFound = _opAllowRepo.findByGroupIdAndMetadataId(_intranetGroup.getId(), _md2.getId());
        
        assertFalse(opAllowedFound.contains(_opAllowed1));
        assertTrue(opAllowedFound.contains(_opAllowed2));
        assertEquals(1, opAllowedFound.size());
        assertFalse(opAllowedFound.contains(_opAllowed3));
        assertFalse(opAllowedFound.contains(_opAllowed4));
        
        // search all + md2
        
        opAllowedFound = _opAllowRepo.findByGroupIdAndMetadataId(_allGroup.getId(), _md2.getId());
        
        assertFalse(opAllowedFound.contains(_opAllowed1));
        assertFalse(opAllowedFound.contains(_opAllowed2));
        assertEquals(0, opAllowedFound.size());
        assertFalse(opAllowedFound.contains(_opAllowed3));
        assertFalse(opAllowedFound.contains(_opAllowed4));
        
        
        // search all + md1
        
        opAllowedFound = _opAllowRepo.findByGroupIdAndMetadataId(_allGroup.getId(), _md1.getId());
        
        assertTrue(opAllowedFound.contains(_opAllowed1));
        assertFalse(opAllowedFound.contains(_opAllowed2));
        assertEquals(1, opAllowedFound.size());
        assertFalse(opAllowedFound.contains(_opAllowed3));
        assertFalse(opAllowedFound.contains(_opAllowed4));
    }
    

    @Test
    public void testDeleteByGroupdIdAndMetadataId() {
        _opAllowRepo.deleteAllByMetadataIdExceptGroupId(_md1.getId(), _allGroup.getId());

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
        assertFalse(opAllowedFound.contains(_opAllowed4));}
}
