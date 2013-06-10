package org.fao.geonet.repository;

import static org.fao.geonet.repository.SettingRepository.ID_PREFIX;
import static org.fao.geonet.repository.SettingRepository.SEPARATOR;
import static org.fao.geonet.repository.SpringDataTestSupport.assertSameContents;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.fao.geonet.domain.Setting;
import org.fao.geonet.repository.SettingRepository;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

public class SettingRepositoryTest extends AbstractSpringDataTest {
    interface Runnable {
        void run() throws Exception;
    }

    @Autowired
    SettingRepository repo;

    @Autowired
    ApplicationContext context;

    private AtomicInteger nextId = new AtomicInteger(20);

    private String[] skipProps = new String[] { "getValueAsInt", "getValueAsBool" };

    @Test
    public void testFindByName() throws Exception {
        Setting setting = repo.save(newSetting());
        List<Setting> found = repo.findByName(setting.getName());
        assertEquals(1, found.size());
        assertSameContents(setting, found.get(0), skipProps);
        assertEquals(0, repo.findByName("some wrong name").size());
    }

    @Test
    public void testFindRoot() throws Exception {
        Setting actualRoot = repo.save(newSetting());
        List<Setting> roots = repo.findRoots();
        assertEquals(1, roots.size());

        Setting root = roots.get(0);
        assertSameContents(actualRoot, root, skipProps);
        assertNull(root.getParent());
    }

    @Test
    public void testFindChildren() throws Exception {
        Setting parent = repo.save(newSetting());
        Setting child = newSetting().setParent(parent);
        repo.save(child);

        assertEquals(2, repo.count());

        List<Setting> children = repo.findAllChildren(parent.getId());
        assertEquals(1, children.size());
        assertSameContents(child, children.get(0), skipProps);

    }

    @Test
    public void testFindChildByName() throws Exception {
        Setting parent = repo.save(newSetting());
        repo.save(newSetting().setParent(parent));
        Setting child3 = repo.save(newSetting().setParent(parent));

        assertEquals(3, repo.count());

        List<Setting> children = repo.findChildrenByName(parent.getId(), child3.getName());
        assertEquals(1, children.size());
        assertSameContents(child3, children.get(0), skipProps);

    }

    @Test
    public void testFindByPathUsingId() throws Exception {

        Setting parent = repo.save(newSetting());
        Setting child2 = repo.save(newSetting().setParent(parent));
        repo.save(newSetting().setParent(parent));

        List<Setting> found = repo.findByPath("id:" + child2.getId());
        assertEquals(1, found.size());
        assertSameContents(child2, found.get(0), skipProps);

        found = repo.findByPath("id:" + parent.getId());
        assertEquals(1, found.size());
        assertSameContents(parent, found.get(0), skipProps);
    }

    @Test
    public void testFindByPathUsingTwoId() throws Exception {
        Setting parent = repo.save(newSetting());
        Setting child2 = repo.save(newSetting().setParent(parent));
        repo.save(newSetting().setParent(parent));

        List<Setting> found = repo.findByPath("id:" + Integer.MAX_VALUE + "/id:" + child2.getId());
        assertEquals(1, found.size());
        assertSameContents(child2, found.get(0), skipProps);
    }

    @Test
    public void testFindByPathUsingChildNamePath() throws Exception {
        Setting parent = repo.save(newSetting());
        Setting child2 = repo.save(newSetting().setParent(parent).setName("2"));
        Setting child3 = repo.save(newSetting().setParent(parent).setName("3"));
        Setting child4 = repo.save(newSetting().setParent(child3).setName("4"));

        String path = SEPARATOR + child3.getName() + SEPARATOR + child4.getName();
        List<Setting> found = repo.findByPath(path);
        assertEquals(1, found.size());
        assertSameContents(child4, found.get(0), skipProps);

        String path2 = SEPARATOR + child2.getName() + SEPARATOR + child4.getName();
        List<Setting> found2 = repo.findByPath(path2);
        assertEquals(0, found2.size());
    }

    @Test
    public void testFindByPathFindingMany() throws Exception {
        Setting parent = repo.save(newSetting().setName("1"));
        repo.save(newSetting().setParent(parent).setName("2"));
        Setting child3 = repo.save(newSetting().setParent(parent).setName("2"));

        String path = child3.getName();
        List<Setting> found = repo.findByPath(path);
        assertEquals(2, found.size());
    }
    
    @Test
    public void testFindByPathUsingIdAndChildNamePath() throws Exception {
        Setting parent = repo.save(newSetting().setName("2"));
        Setting child2 = repo.save(newSetting().setParent(parent).setName("2"));
        Setting child3 = repo.save(newSetting().setParent(parent).setName("3"));
        Setting child4 = repo.save(newSetting().setParent(child3).setName("4"));
        
        String path = ID_PREFIX + child3.getId() + SEPARATOR + child4.getName();
        List<Setting> found = repo.findByPath(path);
        assertEquals(1, found.size());
        assertSameContents(child4, found.get(0), skipProps);
        
        String path2 = SEPARATOR + child2.getName() + SEPARATOR + child4.getName();
        List<Setting> found2 = repo.findByPath(path2);
        assertEquals(0, found2.size());
    }

    
    @Test
    public void testFindOneByPath() throws Exception {
        Setting parent = repo.save(newSetting().setName("2"));
        Setting child2 = repo.save(newSetting().setParent(parent).setName("2"));
        Setting child3 = repo.save(newSetting().setParent(parent).setName("2"));
        Setting child4 = repo.save(newSetting().setParent(child3).setName("4"));
        
        String path = child2.getName();
        Setting found = repo.findOneByPath(path);
        assertNotNull(found);
        assertSameContents(child4, found, skipProps);
    }
    
    private Setting newSetting() {
        int id = nextId.incrementAndGet();
        return new Setting().setName("name " + id).setValue("value " + id);
    }

}
