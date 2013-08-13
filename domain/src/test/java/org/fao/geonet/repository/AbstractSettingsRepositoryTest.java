package org.fao.geonet.repository;

import static org.fao.geonet.repository.AbstractSettingRepo.ID_PREFIX;
import static org.fao.geonet.repository.AbstractSettingRepo.SEPARATOR;
import static org.fao.geonet.repository.SpringDataTestSupport.assertSameContents;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.fao.geonet.domain.AbstractSetting;
import org.junit.Test;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public abstract class AbstractSettingsRepositoryTest<T extends AbstractSetting<T>> extends AbstractSpringDataTest {

    protected AtomicInteger nextId = new AtomicInteger(20);
    private String[] skipProps = new String[] { "getValueAsInt", "getValueAsBool" };

    protected abstract AbstractSettingRepo<T> getRepository();

    @Test
    public void testFindByName() throws Exception {
        T setting = getRepository().save(newSetting());
        List<T> found = getRepository().findByName(setting.getName());
        assertEquals(1, found.size());
        assertSameContents(setting, found.get(0), skipProps);
        assertEquals(0, getRepository().findByName("some wrong name").size());
    }

    @Test
    public void testFindRoot() throws Exception {
        T actualRoot = getRepository().save(newSetting());
        List<T> roots = getRepository().findRoots();
        assertEquals(1, roots.size());
    
        T root = roots.get(0);
        assertSameContents(actualRoot, root, skipProps);
        assertNull(root.getParent());
    }

    @Test
    public void testFindChildren() throws Exception {
        T parent = getRepository().save(newSetting());
        T child = newSetting().setParent(parent);
        getRepository().save(child);
    
        assertEquals(2, getRepository().count());
    
        List<T> children = getRepository().findAllChildren(parent.getId());
        assertEquals(1, children.size());
        assertSameContents(child, children.get(0), skipProps);
    
    }

    @Test
    public void testFindChildByName() throws Exception {
        T parent = getRepository().save(newSetting());
        getRepository().save(newSetting().setParent(parent));
        T child3 = getRepository().save(newSetting().setParent(parent));
    
        assertEquals(3, getRepository().count());
    
        List<T> children = getRepository().findChildrenByName(parent.getId(), child3.getName());
        assertEquals(1, children.size());
        assertSameContents(child3, children.get(0), skipProps);
    
    }

    @Test
    public void testFindByPathUsingId() throws Exception {
    
        T parent = getRepository().save(newSetting());
        T child2 = getRepository().save(newSetting().setParent(parent));
        getRepository().save(newSetting().setParent(parent));
    
        List<T> found = getRepository().findByPath("id:" + child2.getId());
        assertEquals(1, found.size());
        assertSameContents(child2, found.get(0), skipProps);
    
        found = getRepository().findByPath("id:" + parent.getId());
        assertEquals(1, found.size());
        assertSameContents(parent, found.get(0), skipProps);
    }

    @Test
    public void testFindByPathUsingTwoId() throws Exception {
        T parent = getRepository().save(newSetting());
        T child2 = getRepository().save(newSetting().setParent(parent));
        getRepository().save(newSetting().setParent(parent));
    
        List<T> found = getRepository().findByPath("id:" + Integer.MAX_VALUE + "/id:" + child2.getId());
        assertEquals(1, found.size());
        assertSameContents(child2, found.get(0), skipProps);
    }

    @Test
    public void testFindByPathUsingChildNamePath() throws Exception {
        T parent = getRepository().save(newSetting());
        T child2 = getRepository().save(newSetting().setParent(parent).setName("2"));
        T child3 = getRepository().save(newSetting().setParent(parent).setName("3"));
        T child4 = getRepository().save(newSetting().setParent(child3).setName("4"));
    
        String path = SEPARATOR + child3.getName() + SEPARATOR + child4.getName();
        List<T> found = getRepository().findByPath(path);
        assertEquals(1, found.size());
        assertSameContents(child4, found.get(0), skipProps);
    
        String path2 = SEPARATOR + child2.getName() + SEPARATOR + child4.getName();
        List<T> found2 = getRepository().findByPath(path2);
        assertEquals(0, found2.size());
    }

    @Test
    public void testFindByPathFindingMany() throws Exception {
        T parent = getRepository().save(newSetting().setName("1"));
        getRepository().save(newSetting().setParent(parent).setName("2"));
        T child3 = getRepository().save(newSetting().setParent(parent).setName("2"));
    
        String path = child3.getName();
        List<T> found = getRepository().findByPath(path);
        assertEquals(2, found.size());
    }

    @Test
    public void testFindByPathUsingIdAndChildNamePath() throws Exception {
        T parent = getRepository().save(newSetting().setName("2"));
        T child2 = getRepository().save(newSetting().setParent(parent).setName("2"));
        T child3 = getRepository().save(newSetting().setParent(parent).setName("3"));
        T child4 = getRepository().save(newSetting().setParent(child3).setName("4"));
        
        String path = ID_PREFIX + child3.getId() + SEPARATOR + child4.getName();
        List<T> found = getRepository().findByPath(path);
        assertEquals(1, found.size());
        assertSameContents(child4, found.get(0), skipProps);
        
        String path2 = SEPARATOR + child2.getName() + SEPARATOR + child4.getName();
        List<T> found2 = getRepository().findByPath(path2);
        assertEquals(0, found2.size());
    }

    @Test
    public void testFindOneByPath() throws Exception {
        T parent = getRepository().save(newSetting().setName("2"));
        T child2 = getRepository().save(newSetting().setParent(parent).setName("2"));
        T child3 = getRepository().save(newSetting().setParent(parent).setName("2"));
        T child4 = getRepository().save(newSetting().setParent(child3).setName("4"));
        
        String path = child2.getName();
        T found = getRepository().findOneByPath(path);
        assertNotNull(found);
        assertSameContents(child4, found, skipProps);
    }

    protected abstract T newSetting();

}