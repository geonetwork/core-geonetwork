package org.fao.geonet.repository;

import static org.junit.Assert.*;

import java.util.List;

import org.fao.geonet.domain.OperationAllowed;
import org.junit.Test;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

public class OperationAllowedSpecsTest extends AbstractOperationsAllowedTest {

    @Test
    public void testHasMetadataId() {
        Specification<OperationAllowed> hasMetadataId = OperationAllowedSpecs.hasMetadataId(_md1.getId());
        List<OperationAllowed> found = _opAllowRepo.findAll(hasMetadataId, new Sort("id.operationId", "id.groupId"));

        assertEquals(found.size(), 3);
        assertEquals(_viewOp.getId(), found.get(0).getId().getOperationId());
        assertEquals(_viewOp.getId(), found.get(1).getOperation().getId());
        assertEquals(_allGroup.getId(), found.get(0).getId().getGroupId());
        assertEquals(_intranetGroup.getId(), found.get(1).getGroup().getId());
        assertEquals(_viewOp.getId(), found.get(0).getId().getOperationId());
        assertEquals(_viewOp.getId(), found.get(1).getId().getOperationId());
        assertEquals(_downloadOp.getId(), found.get(2).getId().getOperationId());
    }

}
