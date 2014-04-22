package org.fao.geonet.repository.report;

import org.fao.geonet.domain.*;
import org.fao.geonet.repository.*;
import org.fao.geonet.repository.specification.OperationAllowedSpecs;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;

/**
 * Tests for ReportsQueries.
 *
 * @author Jose García
 */
@Transactional
public class ReportsQueriesTest extends AbstractSpringDataTest {

    @Autowired
    UserRepository _userRepository;
    @Autowired
    GroupRepository _groupRepository;
    @Autowired
    MetadataRepository _metadataRepository;
    @Autowired
    OperationRepository _opRepo;
    @Autowired
    protected OperationAllowedRepository _opAllowRepo;

    private AtomicInteger _inc = new AtomicInteger();

    @Test
    public void testGetInactiveMetadata() throws Exception {
        Group group1 = _groupRepository.save(GroupRepositoryTest.newGroup(_inc));
        Group group2 = _groupRepository.save(GroupRepositoryTest.newGroup(_inc));
        Group group3 = _groupRepository.save(GroupRepositoryTest.newGroup(_inc));

        User user1 = _userRepository.save(UserRepositoryTest.newUser(_inc));
        User user2 = _userRepository.save(UserRepositoryTest.newUser(_inc));
        User user3 = _userRepository.save(UserRepositoryTest.newUser(_inc));

        ISODate dateChange1 = new ISODate("2014-01-01T00:00:0");
        ISODate dateChange2 = new ISODate("2014-02-01T00:00:0");
        ISODate dateChange3 = new ISODate("2014-03-01T00:00:0");


        Metadata metadata1g1 = MetadataRepositoryTest.newMetadata(_inc);
        metadata1g1.getSourceInfo().setOwner(user1.getId());
        metadata1g1.getSourceInfo().setGroupOwner(group1.getId());
        metadata1g1.getDataInfo().setChangeDate(dateChange1);
        _metadataRepository.save(metadata1g1);

        Metadata metadata2g1 = MetadataRepositoryTest.newMetadata(_inc);
        metadata2g1.getSourceInfo().setOwner(user2.getId());
        metadata2g1.getSourceInfo().setGroupOwner(group2.getId());
        metadata2g1.getDataInfo().setChangeDate(dateChange2);
        _metadataRepository.save(metadata2g1);

        Metadata metadata3g2 = MetadataRepositoryTest.newMetadata(_inc);
        metadata3g2.getSourceInfo().setOwner(user3.getId());
        metadata3g2.getSourceInfo().setGroupOwner(group3.getId());
        metadata3g2.getDataInfo().setChangeDate(dateChange3);
        _metadataRepository.save(metadata3g2);


        ISODate dateFrom = new ISODate("2014-01-01T00:00:0");
        ISODate dateTo = new ISODate("2014-04-01T00:00:0");
        Set<Integer> groupsSet = new HashSet<Integer>();
        List<Metadata> updatedMetadata = _metadataRepository.getMetadataReports().
                getUpdatedMetadata(dateFrom, dateTo, groupsSet);

        assertEquals(3, updatedMetadata.size());


        dateFrom = new ISODate("2013-01-01T00:00:0");
        dateTo = new ISODate("2013-04-01T00:00:0");

        updatedMetadata = _metadataRepository.getMetadataReports().
                getUpdatedMetadata(dateFrom, dateTo, groupsSet);

        assertEquals(0, updatedMetadata.size());


        dateFrom = new ISODate("2014-02-01T00:00:0");
        dateTo = new ISODate("2014-04-01T00:00:0");

        updatedMetadata = _metadataRepository.getMetadataReports().
                getUpdatedMetadata(dateFrom, dateTo, groupsSet);

        assertEquals(2, updatedMetadata.size());
    }

    @Test
    public void testGetInactiveMetadataWithGroupsFilter() throws Exception {
        Group group1 = _groupRepository.save(GroupRepositoryTest.newGroup(_inc));
        Group group2 = _groupRepository.save(GroupRepositoryTest.newGroup(_inc));
        Group group3 = _groupRepository.save(GroupRepositoryTest.newGroup(_inc));

        User user1 = _userRepository.save(UserRepositoryTest.newUser(_inc));
        User user2 = _userRepository.save(UserRepositoryTest.newUser(_inc));
        User user3 = _userRepository.save(UserRepositoryTest.newUser(_inc));

        ISODate dateChange1 = new ISODate("2014-01-01T00:00:0");
        ISODate dateChange2 = new ISODate("2014-02-01T00:00:0");
        ISODate dateChange3 = new ISODate("2014-03-01T00:00:0");


        Metadata metadata1g1 = MetadataRepositoryTest.newMetadata(_inc);
        metadata1g1.getSourceInfo().setOwner(user1.getId());
        metadata1g1.getSourceInfo().setGroupOwner(group1.getId());
        metadata1g1.getDataInfo().setChangeDate(dateChange1);
        _metadataRepository.save(metadata1g1);

        Metadata metadata2g1 = MetadataRepositoryTest.newMetadata(_inc);
        metadata2g1.getSourceInfo().setOwner(user2.getId());
        metadata2g1.getSourceInfo().setGroupOwner(group2.getId());
        metadata2g1.getDataInfo().setChangeDate(dateChange2);
        _metadataRepository.save(metadata2g1);

        Metadata metadata3g2 = MetadataRepositoryTest.newMetadata(_inc);
        metadata3g2.getSourceInfo().setOwner(user3.getId());
        metadata3g2.getSourceInfo().setGroupOwner(group3.getId());
        metadata3g2.getDataInfo().setChangeDate(dateChange3);
        _metadataRepository.save(metadata3g2);


        // Groups filter
        Set<Integer> groupsSet = new HashSet<Integer>();
        groupsSet.add(group1.getId());

        ISODate dateFrom = new ISODate("2014-01-01T00:00:0");
        ISODate dateTo = new ISODate("2014-04-01T00:00:0");

        List<Metadata> updatedMetadata = _metadataRepository.getMetadataReports().
                getUpdatedMetadata(dateFrom, dateTo, groupsSet);

        assertEquals(1, updatedMetadata.size());


        groupsSet = new HashSet<Integer>();
        groupsSet.add(group1.getId());
        groupsSet.add(group2.getId());

        updatedMetadata = _metadataRepository.getMetadataReports().
                getUpdatedMetadata(dateFrom, dateTo, groupsSet);

        assertEquals(2, updatedMetadata.size());

    }


    @Test
    public void testGetInternalMetadata() throws Exception {
        Group group1 = _groupRepository.save(GroupRepositoryTest.newGroup(_inc));
        Group group2 = _groupRepository.save(GroupRepositoryTest.newGroup(_inc));
        Group group3 = _groupRepository.save(GroupRepositoryTest.newGroup(_inc));
        Group allGroup = _groupRepository.save(ReservedGroup.all.getGroupEntityTemplate());

        User user1 = _userRepository.save(UserRepositoryTest.newUser(_inc));
        User user2 = _userRepository.save(UserRepositoryTest.newUser(_inc));
        User user3 = _userRepository.save(UserRepositoryTest.newUser(_inc));

        ISODate dateChange1 = new ISODate("2014-01-01T00:00:0");
        ISODate dateChange2 = new ISODate("2014-02-01T00:00:0");
        ISODate dateChange3 = new ISODate("2014-03-01T00:00:0");


        Metadata metadata1g1 = MetadataRepositoryTest.newMetadata(_inc);
        metadata1g1.getSourceInfo().setOwner(user1.getId());
        metadata1g1.getSourceInfo().setGroupOwner(group1.getId());
        metadata1g1.getDataInfo().setCreateDate(dateChange1);
        _metadataRepository.save(metadata1g1);

        Metadata metadata2g1 = MetadataRepositoryTest.newMetadata(_inc);
        metadata2g1.getSourceInfo().setOwner(user2.getId());
        metadata2g1.getSourceInfo().setGroupOwner(group2.getId());
        metadata2g1.getDataInfo().setCreateDate(dateChange2);
        _metadataRepository.save(metadata2g1);

        Metadata metadata3g2 = MetadataRepositoryTest.newMetadata(_inc);
        metadata3g2.getSourceInfo().setOwner(user3.getId());
        metadata3g2.getSourceInfo().setGroupOwner(group3.getId());
        metadata3g2.getDataInfo().setCreateDate(dateChange3);
        _metadataRepository.save(metadata3g2);


        Operation viewOp = _opRepo.save(ReservedOperation.view.getOperationEntity().setId(-1));

        // Set metadata1g1 public
        _opAllowRepo.save(new OperationAllowed().setId(metadata1g1, allGroup, viewOp));

        // Set metadata3g2 public
        _opAllowRepo.save(new OperationAllowed().setId(metadata3g2, allGroup, viewOp));


        List<OperationAllowed> allOps = _opAllowRepo.findAll();

        // Update the id for ReservedOperation.view
        setId(ReservedOperation.view, viewOp.getId());
        // Update the id for ReservedGroup.all
        setId(ReservedGroup.all, allGroup.getId());

        ISODate dateFrom = new ISODate("2014-01-01T00:00:0");
        ISODate dateTo = new ISODate("2014-04-01T00:00:0");
        Set<Integer> groupsSet = new HashSet<Integer>();
        List<Metadata> updatedMetadata = _metadataRepository.getMetadataReports().
                getInternalMetadata(dateFrom, dateTo, groupsSet, OperationAllowedSpecs.isPublic(ReservedOperation.view));

        assertEquals(1, updatedMetadata.size());


    }


    private void setId(ReservedOperation view, int normalId) throws Exception {
        Field declaredField = view.getClass().getDeclaredField("_id");
        declaredField.setAccessible(true);
        declaredField.set(view, normalId);
    }

    private void setId(ReservedGroup group, int normalId) throws Exception {
        Field declaredField = group.getClass().getDeclaredField("_id");
        declaredField.setAccessible(true);
        declaredField.set(group, normalId);
    }

}
