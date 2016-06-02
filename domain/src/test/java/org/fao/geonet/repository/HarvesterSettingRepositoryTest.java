/*
 * Copyright (C) 2001-2016 Food and Agriculture Organization of the
 * United Nations (FAO-UN), United Nations World Food Programme (WFP)
 * and United Nations Environment Programme (UNEP)
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or (at
 * your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
 *
 * Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
 * Rome - Italy. email: geonetwork@osgeo.org
 */

package org.fao.geonet.repository;

import org.fao.geonet.domain.HarvesterSetting;
import org.fao.geonet.domain.Setting_;
import org.jdom.Element;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.fao.geonet.repository.HarvesterSettingRepository.ID_PREFIX;
import static org.fao.geonet.repository.HarvesterSettingRepository.SEPARATOR;
import static org.junit.Assert.*;

public class HarvesterSettingRepositoryTest extends AbstractSpringDataTest {

    @Autowired
    HarvesterSettingRepository _repo;

    private String[] _skipProps = new String[]{"getValueAsInt", "getValueAsBool"};

    public static HarvesterSetting newSetting(AtomicInteger inc) {
        int id = inc.incrementAndGet();
        return new HarvesterSetting().setName("name " + id).setValue("value " + id);
    }

    @Test
    public void testFindByName() throws Exception {
        HarvesterSetting setting = _repo.save(newSetting());
        List<HarvesterSetting> found = _repo.findAllByName(setting.getName());
        assertEquals(1, found.size());
        assertSameContents(setting, found.get(0), _skipProps);
        assertEquals(0, _repo.findAllByName("some wrong name").size());
    }

    @Test
    public void testFindRoot() throws Exception {
        HarvesterSetting actualRoot = _repo.save(newSetting());
        List<HarvesterSetting> roots = _repo.findRoots();
        assertEquals(1, roots.size());

        HarvesterSetting root = roots.get(0);
        assertSameContents(actualRoot, root, _skipProps);
        assertNull(root.getParent());
    }

    @Test
    public void testFindChildren() throws Exception {
        HarvesterSetting parent = _repo.save(newSetting());
        HarvesterSetting child = newSetting().setParent(parent);
        _repo.save(child);

        assertEquals(2, _repo.count());

        List<HarvesterSetting> children = _repo.findAllChildren(parent.getId());
        assertEquals(1, children.size());
        assertSameContents(child, children.get(0), _skipProps);

    }

    @Test
    public void testFindChildIds() throws Exception {
        HarvesterSetting parent = _repo.save(newSetting());
        HarvesterSetting child = newSetting().setParent(parent);
        _repo.save(child);

        assertEquals(2, _repo.count());

        List<Integer> children = _repo.findAllChildIds(parent.getId());
        assertEquals(1, children.size());
        assertEquals(child.getId(), children.get(0).intValue());

    }

    @Test
    public void testFindChildByName() throws Exception {
        HarvesterSetting parent = _repo.save(newSetting());
        _repo.save(newSetting().setParent(parent));
        HarvesterSetting child3 = _repo.save(newSetting().setParent(parent));

        assertEquals(3, _repo.count());

        List<HarvesterSetting> children = _repo.findChildrenByName(parent.getId(), child3.getName());
        assertEquals(1, children.size());
        assertSameContents(child3, children.get(0), _skipProps);

    }

    @Test
    public void testFindByPathUsingId() throws Exception {

        HarvesterSetting parent = _repo.save(newSetting());
        HarvesterSetting child2 = _repo.save(newSetting().setParent(parent));
        _repo.save(newSetting().setParent(parent));

        List<HarvesterSetting> found = _repo.findAllByPath("id:" + child2.getId());
        assertEquals(1, found.size());
        assertSameContents(child2, found.get(0), _skipProps);

        found = _repo.findAllByPath("id:" + parent.getId());
        assertEquals(1, found.size());
        assertSameContents(parent, found.get(0), _skipProps);
    }

    @Test
    public void testFindByRootByPath() throws Exception {

        HarvesterSetting parent = _repo.save(newSetting());
        final HarvesterSetting parent2 = _repo.save(newSetting());
        _repo.save(newSetting().setParent(parent));

        List<HarvesterSetting> found = _repo.findAllByPath(parent.getName());
        assertEquals(1, found.size());
        assertSameContents(parent, found.get(0), _skipProps);

        found = _repo.findAllByPath(parent2.getName());
        assertEquals(1, found.size());
        assertSameContents(parent2, found.get(0), _skipProps);
    }

    @Test
    public void testFindByPathUsingTwoId() throws Exception {
        HarvesterSetting parent = _repo.save(newSetting());
        HarvesterSetting child2 = _repo.save(newSetting().setParent(parent));
        _repo.save(newSetting().setParent(parent));

        List<HarvesterSetting> found = _repo.findAllByPath("id:" + Integer.MAX_VALUE + "/id:" + child2.getId());
        assertEquals(1, found.size());
        assertSameContents(child2, found.get(0), _skipProps);
    }

    @Test
    public void testFindByPathUsingChildNamePath() throws Exception {
        HarvesterSetting parent = _repo.save(newSetting());
        HarvesterSetting child2 = _repo.save(newSetting().setParent(parent).setName("2"));
        HarvesterSetting child3 = _repo.save(newSetting().setParent(parent).setName("3"));
        HarvesterSetting child4 = _repo.save(newSetting().setParent(child3).setName("4"));

        String path = SEPARATOR + child3.getName() + SEPARATOR + child4.getName();
        List<HarvesterSetting> found = _repo.findAllByPath(path);
        assertEquals(1, found.size());
        assertSameContents(child4, found.get(0), _skipProps);

        String path2 = SEPARATOR + child2.getName() + SEPARATOR + child4.getName();
        List<HarvesterSetting> found2 = _repo.findAllByPath(path2);
        assertEquals(0, found2.size());
    }

    @Test
    public void testFindByPathFindingMany() throws Exception {
        HarvesterSetting parent = _repo.save(newSetting().setName("1"));
        _repo.save(newSetting().setParent(parent).setName("2"));
        HarvesterSetting child3 = _repo.save(newSetting().setParent(parent).setName("2"));

        String path = child3.getName();
        List<HarvesterSetting> found = _repo.findAllByPath(path);
        assertEquals(2, found.size());
    }

    @Test
    public void testFindByPathUsingIdAndChildNamePath() throws Exception {
        HarvesterSetting parent = _repo.save(newSetting().setName("2"));
        HarvesterSetting child2 = _repo.save(newSetting().setParent(parent).setName("2"));
        HarvesterSetting child3 = _repo.save(newSetting().setParent(parent).setName("3"));
        HarvesterSetting child4 = _repo.save(newSetting().setParent(child3).setName("4"));

        String path = ID_PREFIX + child3.getId() + SEPARATOR + child4.getName();
        List<HarvesterSetting> found = _repo.findAllByPath(path);
        assertEquals(1, found.size());
        assertSameContents(child4, found.get(0), _skipProps);

        String path2 = SEPARATOR + child2.getName() + SEPARATOR + child4.getName();
        List<HarvesterSetting> found2 = _repo.findAllByPath(path2);
        assertEquals(0, found2.size());
    }

    @Test
    public void testFindOneByPath() throws Exception {
        HarvesterSetting parent = _repo.save(newSetting().setName("2"));
        HarvesterSetting child2 = _repo.save(newSetting().setParent(parent).setName("2"));
        HarvesterSetting child3 = _repo.save(newSetting().setParent(parent).setName("2"));
        HarvesterSetting child4 = _repo.save(newSetting().setParent(child3).setName("4"));

        String path = child2.getName();
        HarvesterSetting found = _repo.findOneByPath(path);
        assertNotNull(found);
        assertSameContents(child4, found, _skipProps);
    }

    @Test
    public void testDeleteById() throws Exception {
        HarvesterSetting parent = _repo.save(newSetting().setName("2"));
        _repo.save(newSetting().setParent(parent).setName("2"));
        HarvesterSetting child3 = _repo.save(newSetting().setParent(parent).setName("2"));
        _repo.save(newSetting().setParent(child3).setName("4"));

        _repo.delete(parent.getId());

        assertEquals(0, _repo.count());
    }

    @Test
    public void testDeleteByEntity() throws Exception {
        HarvesterSetting parent = _repo.save(newSetting().setName("2"));
        _repo.save(newSetting().setParent(parent).setName("2"));
        HarvesterSetting child3 = _repo.save(newSetting().setParent(parent).setName("2"));
        _repo.save(newSetting().setParent(child3).setName("4"));

        _repo.delete(parent);

        assertEquals(0, _repo.count());
    }

    @Test
    public void testFindAllAndSort() throws Exception {
        _repo.save(newSetting().setName("4"));
        _repo.save(newSetting().setName("2"));
        _repo.save(newSetting().setName("3"));
        _repo.save(newSetting().setName("1"));

        List<HarvesterSetting> list = _repo.findAll(SortUtils.createSort(Setting_.name));
        assertEquals("1", list.get(0).getName());
        assertEquals("2", list.get(1).getName());
        assertEquals("3", list.get(2).getName());
        assertEquals("4", list.get(3).getName());

        List<HarvesterSetting> list2 = _repo.findAll(new Sort(Direction.DESC, SortUtils.createPath(Setting_.name)));
        assertEquals("4", list2.get(0).getName());
        assertEquals("3", list2.get(1).getName());
        assertEquals("2", list2.get(2).getName());
        assertEquals("1", list2.get(3).getName());
    }

    @Test
    public void testFindAllAsXml() throws Exception {
        _repo.save(newSetting().setName("4"));
        final Element allAsXml = _repo.findAllAsXml();
        //  No Exception... good
    }

    private HarvesterSetting newSetting() {
        return newSetting(_inc);
    }

}
