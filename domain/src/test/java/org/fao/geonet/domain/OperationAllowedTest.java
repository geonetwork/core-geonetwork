package org.fao.geonet.domain;

import org.fao.geonet.repository.AbstractOperationsAllowedTest;
import org.junit.Test;
import org.springframework.transaction.annotation.Propagation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class OperationAllowedTest extends AbstractOperationsAllowedTest {
    @Test
    public void testGetGroup() {
        OperationAllowed operationAllowed = _opAllowRepo.findOne(_opAllowed1.getId());
        assertNotNull(operationAllowed);
        assertEquals(_opAllowed1.getId().getGroupId(), operationAllowed.getId().getGroupId());
    }

    @Test
    public void testConstructors() {
        int mdId = 1;
        int opId = 20;
        int grpId = 300;
        assertEquals(new OperationAllowedId().setMetadataId(mdId).setGroupId(grpId).setOperationId(opId),
                new OperationAllowedId(mdId, grpId, opId));
    }

}
