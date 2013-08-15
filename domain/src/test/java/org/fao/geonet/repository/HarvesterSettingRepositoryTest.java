package org.fao.geonet.repository;

import static org.fao.geonet.repository.HarvesterSettingRepository.ID_PREFIX;
import static org.fao.geonet.repository.HarvesterSettingRepository.SEPARATOR;
import static org.fao.geonet.repository.SpringDataTestSupport.assertSameContents;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.fao.geonet.domain.HarvesterSetting;
import org.fao.geonet.domain.Setting_;
import org.fao.geonet.repository.HarvesterSettingRepository;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public class HarvesterSettingRepositoryTest extends AbstractSpringDataTest {
    interface Runnable {
        void run() throws Exception;
    }

    @Autowired
    HarvesterSettingRepository repo;

    @Autowired
    ApplicationContext context;

    private AtomicInteger nextId = new AtomicInteger(20);

    private String[] skipProps = new String[] { "getValueAsInt", "getValueAsBool" };

    @Test
    public void testFindByName() throws Exception {
        HarvesterSetting setting = repo.save(newSetting());
        List<HarvesterSetting> found = repo.findByName(setting.getName());
        assertEquals(1, found.size());
        assertSameContents(setting, found.get(0), skipProps);
        assertEquals(0, repo.findByName("some wrong name").size());
    }

    @Test
    public void testFindRoot() throws Exception {
        HarvesterSetting actualRoot = repo.save(newSetting());
        List<HarvesterSetting> roots = repo.findRoots();
        assertEquals(1, roots.size());

        HarvesterSetting root = roots.get(0);
        assertSameContents(actualRoot, root, skipProps);
        assertNull(root.getParent());
    }

    @Test
    public void testFindChildren() throws Exception {
        HarvesterSetting parent = repo.save(newSetting());
        HarvesterSetting child = newSetting().setParent(parent);
        repo.save(child);

        assertEquals(2, repo.count());

        List<HarvesterSetting> children = repo.findAllChildren(parent.getId());
        assertEquals(1, children.size());
        assertSameContents(child, children.get(0), skipProps);

    }

    @Test
    public void testFindChildByName() throws Exception {
        HarvesterSetting parent = repo.save(newSetting());
        repo.save(newSetting().setParent(parent));
        HarvesterSetting child3 = repo.save(newSetting().setParent(parent));

        assertEquals(3, repo.count());

        List<HarvesterSetting> children = repo.findChildrenByName(parent.getId(), child3.getName());
        assertEquals(1, children.size());
        assertSameContents(child3, children.get(0), skipProps);

    }

    @Test
    public void testFindByPathUsingId() throws Exception {

        HarvesterSetting parent = repo.save(newSetting());
        HarvesterSetting child2 = repo.save(newSetting().setParent(parent));
        repo.save(newSetting().setParent(parent));

        List<HarvesterSetting> found = repo.findByPath("id:" + child2.getId());
        assertEquals(1, found.size());
        assertSameContents(child2, found.get(0), skipProps);

        found = repo.findByPath("id:" + parent.getId());
        assertEquals(1, found.size());
        assertSameContents(parent, found.get(0), skipProps);
    }

    @Test
    public void testFindByPathUsingTwoId() throws Exception {
        HarvesterSetting parent = repo.save(newSetting());
        HarvesterSetting child2 = repo.save(newSetting().setParent(parent));
        repo.save(newSetting().setParent(parent));

        List<HarvesterSetting> found = repo.findByPath("id:" + Integer.MAX_VALUE + "/id:" + child2.getId());
        assertEquals(1, found.size());
        assertSameContents(child2, found.get(0), skipProps);
    }

    @Test
    public void testFindByPathUsingChildNamePath() throws Exception {
        HarvesterSetting parent = repo.save(newSetting());
        HarvesterSetting child2 = repo.save(newSetting().setParent(parent).setName("2"));
        HarvesterSetting child3 = repo.save(newSetting().setParent(parent).setName("3"));
        HarvesterSetting child4 = repo.save(newSetting().setParent(child3).setName("4"));

        String path = SEPARATOR + child3.getName() + SEPARATOR + child4.getName();
        List<HarvesterSetting> found = repo.findByPath(path);
        assertEquals(1, found.size());
        assertSameContents(child4, found.get(0), skipProps);

        String path2 = SEPARATOR + child2.getName() + SEPARATOR + child4.getName();
        List<HarvesterSetting> found2 = repo.findByPath(path2);
        assertEquals(0, found2.size());
    }

    @Test
    public void testFindByPathFindingMany() throws Exception {
        HarvesterSetting parent = repo.save(newSetting().setName("1"));
        repo.save(newSetting().setParent(parent).setName("2"));
        HarvesterSetting child3 = repo.save(newSetting().setParent(parent).setName("2"));

        String path = child3.getName();
        List<HarvesterSetting> found = repo.findByPath(path);
        assertEquals(2, found.size());
    }
    
    @Test
    public void testFindByPathUsingIdAndChildNamePath() throws Exception {
        HarvesterSetting parent = repo.save(newSetting().setName("2"));
        HarvesterSetting child2 = repo.save(newSetting().setParent(parent).setName("2"));
        HarvesterSetting child3 = repo.save(newSetting().setParent(parent).setName("3"));
        HarvesterSetting child4 = repo.save(newSetting().setParent(child3).setName("4"));
        
        String path = ID_PREFIX + child3.getId() + SEPARATOR + child4.getName();
        List<HarvesterSetting> found = repo.findByPath(path);
        assertEquals(1, found.size());
        assertSameContents(child4, found.get(0), skipProps);
        
        String path2 = SEPARATOR + child2.getName() + SEPARATOR + child4.getName();
        List<HarvesterSetting> found2 = repo.findByPath(path2);
        assertEquals(0, found2.size());
    }

    
    @Test
    public void testFindOneByPath() throws Exception {
        HarvesterSetting parent = repo.save(newSetting().setName("2"));
        HarvesterSetting child2 = repo.save(newSetting().setParent(parent).setName("2"));
        HarvesterSetting child3 = repo.save(newSetting().setParent(parent).setName("2"));
        HarvesterSetting child4 = repo.save(newSetting().setParent(child3).setName("4"));
        
        String path = child2.getName();
        HarvesterSetting found = repo.findOneByPath(path);
        assertNotNull(found);
        assertSameContents(child4, found, skipProps);
    }
    
    @Test
    public void testFindAllAndSort() throws Exception {
        repo.save(newSetting().setName("4"));
        repo.save(newSetting().setName("2"));
        repo.save(newSetting().setName("3"));
        repo.save(newSetting().setName("1"));
        
        List<HarvesterSetting> list = repo.findAll(new Sort(Setting_.name.getName()));
        assertEquals("1", list.get(0).getName());
        assertEquals("2", list.get(1).getName());
        assertEquals("3", list.get(2).getName());
        assertEquals("4", list.get(3).getName());
        
        List<HarvesterSetting> list2 = repo.findAll(new Sort(Direction.DESC, Setting_.name.getName()));
        assertEquals("4", list2.get(0).getName());
        assertEquals("3", list2.get(1).getName());
        assertEquals("2", list2.get(2).getName());
        assertEquals("1", list2.get(3).getName());
    }
    
    private HarvesterSetting newSetting() {
        int id = nextId.incrementAndGet();
        return new HarvesterSetting().setName("name " + id).setValue("value " + id);
    }

}
