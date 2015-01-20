package org.fao.geonet.repository.specification;

import com.google.common.base.Optional;
import org.fao.geonet.domain.OperationAllowed;
import org.fao.geonet.domain.OperationAllowedId_;
import org.fao.geonet.domain.OperationAllowed_;
import org.fao.geonet.domain.ReservedGroup;
import org.fao.geonet.domain.ReservedOperation;
import org.fao.geonet.repository.AbstractOperationsAllowedTest;
import org.fao.geonet.repository.SortUtils;
import org.junit.Test;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.util.Arrays;
import java.util.List;

import static org.fao.geonet.repository.SpringDataTestSupport.setId;
import static org.junit.Assert.assertEquals;

public class OperationAllowedSpecsTest extends AbstractOperationsAllowedTest {
    private String metadataIdPath() {
        return SortUtils.createPath(OperationAllowed_.id, OperationAllowedId_.metadataId);
    }

    private String operationIdPath() {
        return SortUtils.createPath(OperationAllowed_.id, OperationAllowedId_.operationId);
    }

    private String groupIdPath() {
        return SortUtils.createPath(OperationAllowed_.id, OperationAllowedId_.groupId);
    }

    @Test
    public void testHasMetadataId() {
        Specification<OperationAllowed> hasMetadataId = OperationAllowedSpecs.hasMetadataId(_md1.getId());
        List<OperationAllowed> found = _opAllowRepo.findAll(hasMetadataId, new Sort(operationIdPath(), metadataIdPath()));

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
    public void testHasMetadataIdIn() {
        Specification<OperationAllowed> hasMetadataId = OperationAllowedSpecs.hasMetadataIdIn(Arrays.asList(_md1.getId(), _md2.getId()));
        List<OperationAllowed> found = _opAllowRepo.findAll(hasMetadataId, new Sort(operationIdPath(), metadataIdPath()));

        assertEquals(found.size(), 4);
    }

    @Test
    public void testHasGroupIdIn() {
        Specification<OperationAllowed> hasGroupIdIn = OperationAllowedSpecs.hasGroupIdIn(Arrays.asList(_intranetGroup.getId()));
        List<OperationAllowed> found = _opAllowRepo.findAll(hasGroupIdIn, new Sort(operationIdPath(), metadataIdPath()));

        assertEquals(found.size(), 3);
    }

    @Test
    public void testHasOperationIdIn() {
        Specification<OperationAllowed> hasGroupIdIn = OperationAllowedSpecs.hasOperationIdIn(Arrays.asList(_downloadOp.getId()));
        List<OperationAllowed> found = _opAllowRepo.findAll(hasGroupIdIn, new Sort(operationIdPath(), metadataIdPath()));

        assertEquals(found.size(), 2);
    }

    @Test
    public void testIsPublic() throws Exception {
        int viewOpId = ReservedOperation.view.getId();
        int allGroupId = ReservedGroup.all.getId();
        try {
            setId(ReservedOperation.view, _viewOp.getId());
            setId(ReservedGroup.all, _allGroup.getId());
            Specification<OperationAllowed> isPublic = OperationAllowedSpecs.isPublic(ReservedOperation.view);
            List<OperationAllowed> found = _opAllowRepo.findAll(isPublic);

            assertEquals(1, found.size());
            assertEquals(_opAllowed1.getId(), found.get(0).getId());
        } finally {
            setId(ReservedOperation.view, viewOpId);
            setId(ReservedGroup.all, allGroupId);

        }
    }

    @Test
    public void testHasGroupId() {
        Specification<OperationAllowed> hasGroupId = OperationAllowedSpecs.hasGroupId(_intranetGroup.getId());
        List<OperationAllowed> found = _opAllowRepo.findAll(hasGroupId, new Sort(groupIdPath(), metadataIdPath()));

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
        List<OperationAllowed> found = _opAllowRepo.findAll(hasMetadataId, new Sort(groupIdPath(), metadataIdPath()));

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
