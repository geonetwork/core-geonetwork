package org.fao.geonet.repository;

import static org.fao.geonet.domain.OperationAllowedNamedQueries.PATH_GROUP_ID;
import static org.fao.geonet.domain.OperationAllowedNamedQueries.PATH_METADATA_ID;
import static org.fao.geonet.domain.OperationAllowedNamedQueries.PATH_OPERATION_ID;
import static org.junit.Assert.assertEquals;

import java.util.List;

import com.google.common.base.Optional;
import org.fao.geonet.domain.OperationAllowed;
import org.fao.geonet.repository.specification.OperationAllowedSpecs;
import org.junit.Test;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public class OperationAllowedSpecsTest extends AbstractOperationsAllowedTest {

    @Test
    public void testHasMetadataId() {
        Specification<OperationAllowed> hasMetadataId = OperationAllowedSpecs.hasMetadataId(_md1.getId());
        List<OperationAllowed> found = _opAllowRepo.findAll(hasMetadataId, new Sort(PATH_OPERATION_ID, PATH_METADATA_ID));

        assertEquals(found.size(), 3);
        assertEquals(_viewOp.getId(), found.get(0).getId().getOperationId());
        assertEquals(_viewOp.getId(), found.get(1).getId().getOperationId());
        assertEquals(_downloadOp.getId(), found.get(2).getId().getOperationId());
        
        assertEquals(_allGroup.getId(), found.get(0).getId().getGroupId());

        assertEquals(_md1.getId(), found.get(0).getId().getMetadataId());
        assertEquals(_md1.getId(), found.get(1).getId().getMetadataId());
        assertEquals(_md1.getId(), found.get(2).getId().getMetadataId());
    }
    
    @Test
    public void testHasGroupId() {
        Specification<OperationAllowed> hasMetadataId = OperationAllowedSpecs.hasGroupId(_intranetGroup.getId());
        List<OperationAllowed> found = _opAllowRepo.findAll(hasMetadataId, new Sort(PATH_GROUP_ID, PATH_METADATA_ID));
        
        assertEquals(found.size(), 3);
        assertEquals(_intranetGroup.getId(), found.get(0).getId().getGroupId());
        assertEquals(_intranetGroup.getId(), found.get(1).getId().getGroupId());
        assertEquals(_intranetGroup.getId(), found.get(2).getId().getGroupId());
        
        assertEquals(_viewOp.getId(), found.get(0).getId().getOperationId());
        assertEquals(_downloadOp.getId(), found.get(1).getId().getOperationId());
        assertEquals(_downloadOp.getId(), found.get(2).getId().getOperationId());
        
        assertEquals(_md1.getId(), found.get(0).getId().getMetadataId());
        assertEquals(_md1.getId(), found.get(1).getId().getMetadataId());
        assertEquals(_md2.getId(), found.get(2).getId().getMetadataId());
    }
    
    @Test
    public void testHasOperationId() {
        Specification<OperationAllowed> hasMetadataId = OperationAllowedSpecs.hasOperationId(_viewOp.getId());
        List<OperationAllowed> found = _opAllowRepo.findAll(hasMetadataId, new Sort(PATH_GROUP_ID, PATH_METADATA_ID));
        
        assertEquals(found.size(), 2);
        assertEquals(_allGroup.getId(), found.get(0).getId().getGroupId());

        assertEquals(_viewOp.getId(), found.get(0).getId().getOperationId());
        assertEquals(_viewOp.getId(), found.get(1).getId().getOperationId());
        
        assertEquals(_md1.getId(), found.get(0).getId().getMetadataId());
        assertEquals(_md1.getId(), found.get(1).getId().getMetadataId());
    }

    @Test
    public void testHasOwnerId() {
        List<OperationAllowed> found = _opAllowRepo.findAllWithOwner(2, Optional.<Specification<OperationAllowed>>absent());

        assertEquals(found.size(), 1);
        assertEquals(_intranetGroup.getId(), found.get(0).getId().getGroupId());

        assertEquals(_downloadOp.getId(), found.get(0).getId().getOperationId());

        assertEquals(_md2.getId(), found.get(0).getId().getMetadataId());
    }

    @Test
    public void testHasOwnerIdAndSpec() {
        List<OperationAllowed> found = _opAllowRepo.findAllWithOwner(1, Optional.<Specification<OperationAllowed>>of
                (OperationAllowedSpecs.hasGroupId(_intranetGroup.getId())));

        assertEquals(found.size(), 2);
    }

}
