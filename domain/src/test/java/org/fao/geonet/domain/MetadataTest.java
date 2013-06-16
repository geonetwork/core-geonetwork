package org.fao.geonet.domain;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.fao.geonet.repository.AbstractSpringDataTest;
import org.fao.geonet.repository.GroupRepository;
import org.fao.geonet.repository.MetadataRepository;
import org.fao.geonet.repository.OperationAllowedRepository;
import org.fao.geonet.repository.OperationRepository;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

public class MetadataTest extends AbstractSpringDataTest {

    @Autowired
    private MetadataRepository _metadataRepo;

    @Autowired
    private GroupRepository _groupRepo;
    
    @Autowired
    private OperationRepository _opRepo;
    
    @Autowired
    private OperationAllowedRepository _opAllowRepo;

    @PersistenceContext
    private EntityManager _entityManager;

    @Autowired
    private ApplicationContext appContext;

    private Group _group1;

    private Group _group2;

    private Operation _op1;

    private Operation _op2;

    private Metadata _md1;

    private Metadata _md2;

    private OperationAllowed _opAllow1;

    private OperationAllowed _opAllow2;

    @Before
    public void addRequiredEntities() {
        _entityManager.clear();
        _group1 = _groupRepo.save(new Group().setName("1").setEmail("1"));
        _group2 = _groupRepo.save(new Group().setName("2").setEmail("2"));
        
        _op1 = _opRepo.save(new Operation().setId(1).setName("1"));
        _op2 = _opRepo.save(new Operation().setId(2).setName("2"));
        
        _md1 = _metadataRepo.save(new Metadata().setUuid("uuid1"));
        _md2 = _metadataRepo.save(new Metadata().setUuid("uuid2"));
        
        this._opAllow1 = _opAllowRepo.save(new OperationAllowed().setGroup(_group1).setMetadata(_md1).setOperation(_op1));
        this._opAllow2 = _opAllowRepo.save(new OperationAllowed().setGroup(_group2).setMetadata(_md2).setOperation(_op2));
        flushAndClearCaches();
    }

    private void flushAndClearCaches() {
        _groupRepo.flush();
        _opRepo.flush();
        _metadataRepo.flush();
        _opAllowRepo.flush();
        _entityManager.flush();
        _entityManager.clear();
    }

    @Test
    public void testAddOperationAllowedUpdatesMetadataRef() {
        Metadata md2 = _metadataRepo.findOne(_md2.getId());
        OperationAllowed allowed = new OperationAllowed().setGroup(_group1).setMetadata(md2).setOperation(_op1);
        assertTrue(md2.getOperationsAllowed().contains(allowed));

        Metadata md1 = _metadataRepo.findOne(_md1.getId());
        md1.addOperationAllowed(allowed);
        
        assertSame(md1, allowed.getMetadata());
        assertTrue(md1.getOperationsAllowed().contains(allowed));
        assertFalse(md2.getOperationsAllowed().contains(allowed));
        assertTrue(md2 != allowed.getMetadata());
        assertEquals(1, md1.getOperationsAllowed().size());
        
        allowed.setOperation(_op2);
        
        md1.addOperationAllowed(allowed);
        assertEquals(1, md1.getOperationsAllowed().size());
    }
    
    @Test
    public void testRemoveOperationAllowedUpdatesMetadataRef() {
        assertSame(_md1, _opAllow1.getMetadata());
        assertTrue(_md1.getOperationsAllowed().contains(_opAllow1));
        
        _md1.removeOperationAllowed(_opAllow1);
        
        assertNull(_opAllow1.getMetadata());
        assertFalse(_md1.getOperationsAllowed().contains(_opAllow1));
        
    }
    
    @Test @Ignore
    public void testOperationAllowEntitiesLoaded() {
        Metadata loadedMd = _metadataRepo.findOne(_md1.getId());
        
        assertEquals(1, loadedMd.getOperationsAllowed().size());
    }
    @Test  @Ignore
    public void testAddOperationAllowed() {
        OperationAllowed allowed = new OperationAllowed().setGroup(_group2).setMetadata(_md1).setOperation(_op1);
        _md1.addOperationAllowed(allowed);
        _opAllowRepo.save(allowed);
        
        flushAndClearCaches();

        assertEquals(3, _opAllowRepo.count());
        Metadata loadedMd = _metadataRepo.findOne(_md1.getId());
        assertEquals(2, loadedMd.getOperationsAllowed().size());

        OperationAllowed loadedOpAll = loadedMd.getOperationsAllowed().iterator().next();
        assertEquals(_md1.getId(), loadedOpAll.getId().getMetadataId());
        assertEquals(_group1.getId(), loadedOpAll.getId().getGroupId());
        assertEquals(_op1.getId(), loadedOpAll.getId().getOperationId());
        
        assertEquals(_md1.getId(), loadedOpAll.getMetadata().getId());
        assertEquals(_group1.getId(), loadedOpAll.getGroup().getId());
        assertEquals(_op1.getId(), loadedOpAll.getOperation().getId());
    }

    public void testGetOperationAllowedByGroup() {

        OperationAllowed allowed = new OperationAllowed().setGroup(_group1).setMetadata(_md1).setOperation(_op1);
        OperationAllowed allowed2 = new OperationAllowed().setGroup(_group2).setMetadata(_md1).setOperation(_op2);
        _md1.addOperationAllowed(allowed);
        _md1.addOperationAllowed(allowed2);
        
        _metadataRepo.save(_md1);

        flushAndClearCaches();


    }

    @Test(expected=Exception.class)
    public void testAddingNullOperationAllowed() {
        _md1.addOperationAllowed(null);
    }
    @Test(expected=Exception.class)
    public void testAddingNullUUID() {
        _md1.setUuid(null);
    }
//    @Test
//    public void test() {
//        fail("not implemented");
//    }
//    @Test
//    public void test() {
//        fail("not implemented");
//    }
//    @Test
//    public void testSetGroups() {
//        Group g1 = _groups.get(0);
//        Group g2 = _groups.get(1);
//
//        Metadata md = new Metadata().setUuid("uuid").setGroups(Arrays.asList(g1, g2));
//
//        List<Group> mdGroups = md.getGroups();
//        assertEquals(2, mdGroups.size());
//        assertFound(g1, g2, md);
//
//        md = new Metadata().setUuid("uuid").setGroups((List<Group>) null);
//
//        assertEquals(0, md.getGroups().size());
//
//        md = new Metadata().setUuid("uuid").setGroups(new ArrayList<Group>());
//        assertEquals(0, md.getGroups().size());
//    }
//
//    @Test
//    public void testAddGroup() {
//        Group g1 = _groups.get(0);
//        Group g2 = _groups.get(1);
//
//        Metadata md = new Metadata().setUuid("uuid").setGroups(Arrays.asList(g1));
//        assertEquals(1, md.getGroups().size());
//        md.addGroup(g2);
//
//        assertFound(g1, g2, md);
//    }
//
//    private void assertFound(Group g1, Group g2, Metadata md) {
//        assertEquals(2, md.getGroups().size());
//        boolean g1Found = false;
//        boolean g2Found = false;
//        for (Group group : md.getGroups()) {
//            g1Found |= group.getId() == g1.getId();
//            g2Found |= group.getId() == g2.getId();
//        }
//
//        assertTrue(g1 + " should be in metadata", g1Found);
//        assertTrue(g2 + " group should be in metadata", g2Found);
//    }
//
//    @Test(expected = RuntimeException.class)
//    public void addNullGroup() {
//        new Metadata().setUuid("uuid").addGroup(null);
//    }
//    
//    @Test
//    public void saveAndLoadGroups() {
//        Group g1 = _groups.get(0);
//        Group g2 = _groups.get(1);
//
//        Metadata md = new Metadata().setUuid("uuid").setGroups(Arrays.asList(g1, g2));
//        
//        Metadata savedMd = _repo.save(md);
//        
//        _entityManager.flush();
//        _entityManager.clear();
//        
//        Metadata loadedMd = _repo.findOne(savedMd.getId());
//        
//        assertNotNull(loadedMd);
//        
//        assertEquals(2, loadedMd.getGroups().size());
//        assertFound(g1, g2, loadedMd);
//    }

//    @Test
//    public void testSetOperations() {
//        Operation g1 = _ops.get(0);
//        Operation g2 = _ops.get(1);
//
//        Metadata md = new Metadata().setUuid("uuid").setOperations(Arrays.asList(g1, g2));
//
//        List<Operation> mdOperations = md.getOperations();
//        assertEquals(2, mdOperations.size());
//        assertFound(g1, g2, md);
//
//        md = new Metadata().setUuid("uuid").setOperations((List<Operation>) null);
//
//        assertEquals(0, md.getOperations().size());
//
//        md = new Metadata().setUuid("uuid").setOperations(new ArrayList<Operation>());
//        assertEquals(0, md.getOperations().size());
//    }
//
//    @Test
//    public void testAddOperation() {
//        Operation g1 = _ops.get(0);
//        Operation g2 = _ops.get(1);
//
//        Metadata md = new Metadata().setUuid("uuid").setOperations(Arrays.asList(g1));
//        assertEquals(1, md.getOperations().size());
//        md.addOperation(g2);
//
//        assertEquals(2, md.getOperations().size());
//        assertFound(g1, g2, md);
//    }
//
//    private void assertFound(Operation g1, Operation g2, Metadata md) {
//        boolean g1Found = false;
//        boolean g2Found = false;
//        for (Operation op : md.getOperations()) {
//            g1Found |= op.getId() == g1.getId();
//            g2Found |= op.getId() == g2.getId();
//        }
//
//        assertTrue(g1 + " should be in metadata", g1Found);
//        assertTrue(g2 + " op should be in metadata", g2Found);
//    }
//
//    @Test(expected = RuntimeException.class)
//    public void addNullOperation() {
//        new Metadata().setUuid("uuid").addOperation(null);
//    }

//    @Test
//    public void saveAndLoadOperations() {
//        Operation o1 = _ops.get(0);
//        Operation o2 = _ops.get(1);
//
//        Metadata md = new Metadata().setUuid("uuid").setOperations(Arrays.asList(o1, o2));
//        
//        Metadata savedMd = _repo.save(md);
//
//        assertNotNull(savedMd);
//        
//        assertEquals(2, savedMd.getOperations().size());
//        assertFound(o1, o2, savedMd);
//
//        _entityManager.clear();
//        
//        Metadata loadedMd = _repo.findOne(savedMd.getId());
//        
//        assertNotNull(loadedMd);
//        
//        assertEquals(2, loadedMd.getOperations().size());
//        assertFound(o1, o2, loadedMd);
//    }
}
