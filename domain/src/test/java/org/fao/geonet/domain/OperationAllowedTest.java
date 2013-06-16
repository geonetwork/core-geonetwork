package org.fao.geonet.domain;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.fao.geonet.repository.AbstractOperationsAllowedTest;
import org.junit.Ignore;
import org.junit.Test;

public class OperationAllowedTest extends AbstractOperationsAllowedTest {
    @Test @Ignore
    public void testGetGroup() {
        OperationAllowed operationAllowed = _opAllowRepo.findOne(_opAllowed1.getId());
        assertNotNull(operationAllowed);
        assertEquals(_opAllowed1.getGroup(), operationAllowed.getGroup());
    }
    
    @Test
    public void testNullSetting() {
        assertNotNull(_opAllowed1.getMetadata());
        _opAllowed1.setMetadata(null);
        assertNull(_opAllowed1.getMetadata());
        assertEquals(-1, _opAllowed1.getId().getMetadataId());

        assertNotNull(_opAllowed1.getOperation());
        _opAllowed1.setOperation(null);
        assertNull(_opAllowed1.getOperation());
        assertEquals(-1, _opAllowed1.getId().getOperationId());
        
        assertNotNull(_opAllowed1.getGroup());
        _opAllowed1.setGroup(null);
        assertNull(_opAllowed1.getGroup());
        assertEquals(-1, _opAllowed1.getId().getGroupId());
        
        
    }
    
    @Test
    public void testSetMetadata() {
        assertSame(_md1, _opAllowed1.getMetadata());
        assertTrue(_md1.getOperationsAllowed().contains(_opAllowed1));
        assertEquals(_md1.getId(), _opAllowed1.getId().getMetadataId());
        assertFalse(_md2.getOperationsAllowed().contains(_opAllowed1));

        _opAllowed1.setMetadata(_md2);

        assertSame(_md2, _opAllowed1.getMetadata());
        assertTrue(_md2.getOperationsAllowed().contains(_opAllowed1));
        assertFalse(_md1.getOperationsAllowed().contains(_opAllowed1));
        assertEquals(_md2.getId(), _opAllowed1.getId().getMetadataId());
        
        _opAllowed1.setMetadata(null);
        
        assertNull(_opAllowed1.getMetadata());
        assertFalse(_md2.getOperationsAllowed().contains(_opAllowed1));
    }
}
