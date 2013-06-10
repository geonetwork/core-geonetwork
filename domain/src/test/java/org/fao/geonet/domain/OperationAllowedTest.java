package org.fao.geonet.domain;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.fao.geonet.repository.AbstractOperationsAllowedTest;
import org.junit.Test;

public class OperationAllowedTest extends AbstractOperationsAllowedTest {
    @Test
    public void testGetGroup() {
        OperationAllowed operationAllowed = _opAllowRepo.findOne(_opAllowed1.getId());
        assertNotNull(operationAllowed);
        assertEquals(_opAllowed1.getGroup(), operationAllowed.getGroup());
    }
}
