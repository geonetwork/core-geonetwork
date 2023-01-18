/*
 * Copyright (C) 2023 Food and Agriculture Organization of the
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

import org.fao.geonet.domain.ISODate;
import org.fao.geonet.domain.User;
import org.fao.geonet.domain.UserMetadataSelection;
import org.fao.geonet.domain.UserMetadataSelectionList;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class UserMetadataSelectionListRepositoryTest extends AbstractSpringDataTest {

    @Autowired
    UserMetadataSelectionListRepository _repo;

    @Autowired
    UserRepository _userRepository;


    ISODate date1 = new ISODate("2023-06-01T01:02:02");
    ISODate date2 = new ISODate("2023-06-01T01:03:03");

    public UserMetadataSelectionList createSimpleList() {
        UserMetadataSelectionList list = new UserMetadataSelectionList();
        list.setListType(UserMetadataSelectionList.ListType.WatchList);
        list.setCreateDate(date1);
        list.setChangeDate(date2);
        list.setIsPublic(true);
        list.setName("test case list");

        return list;
    }

    public UserMetadataSelectionList createListWithItems(String name) {
        UserMetadataSelectionList list = createSimpleList();
        list.setName(name);
        UserMetadataSelection item1 = new UserMetadataSelection();
        item1.setMetadataUuid("metadataid1");
        UserMetadataSelection item2 = new UserMetadataSelection();
        item2.setMetadataUuid("metadataid2");

        list.setSelections(Arrays.asList(item1, item2));
        return list;
    }

    @Test
    public void testSaveSimple() {
        UserMetadataSelectionList list = createSimpleList();

        _repo.save(list);

        UserMetadataSelectionList list2 = _repo.findById(list.getId()).get();

        assertEquals(list.getListType(), list2.getListType());
        assertEquals(list.getCreateDate(), list2.getCreateDate());
        assertEquals(list.getChangeDate(), list2.getChangeDate());
        assertEquals(list.getIsPublic(), list2.getIsPublic());
        assertEquals(list.getName(), list2.getName());
        assertNotEquals(0, list2.getId());
    }

    @Test
    public void testSaveWithItems() {
        UserMetadataSelectionList list = createListWithItems("test case list");

        _repo.save(list);

        UserMetadataSelectionList list2 = _repo.findById(list.getId()).get();
        assertEquals(list.getSelections().size(), list2.getSelections().size());
        assertEquals(list.getSelections().get(0).getMetadataUuid(), list2.getSelections().get(0).getMetadataUuid());
        assertEquals(list.getSelections().get(1).getMetadataUuid(), list2.getSelections().get(1).getMetadataUuid());

        assertNotEquals(0, list2.getId());
        assertNotEquals(0, list.getSelections().get(0).getId());
        assertNotEquals(0, list.getSelections().get(1).getId());
    }


    @Test
    public void testFindBy() {
        User user1 = _userRepository.save(UserRepositoryTest.newUser(_inc));
        User user2 = _userRepository.save(UserRepositoryTest.newUser(_inc));
        String sessionId = UUID.randomUUID().toString();

        UserMetadataSelectionList list_user1 = createListWithItems("test case list_user1");
        list_user1.setUser(user1);
        _repo.save(list_user1);

        UserMetadataSelectionList list_user1b = createListWithItems("test case list_user1b");
        list_user1b.setUser(user1);
        _repo.save(list_user1b);

        UserMetadataSelectionList list_user2 = createListWithItems("test case list_user2");
        list_user2.setUser(user2);
        _repo.save(list_user2);

        UserMetadataSelectionList list_session1 = createListWithItems("test case list_session1");
        list_session1.setSessionId(sessionId);
        _repo.save(list_session1);

        // we saved 2 lists by user1 - list_user1 list_user1b
        List<UserMetadataSelectionList> byUser1 = _repo.findByUser(user1);
        assertEquals(2, byUser1.size());
        assertTrue(byUser1.contains(list_user1));
        assertTrue(byUser1.contains(list_user1b));

        List<UserMetadataSelectionList> byUser2 = _repo.findByUser(user2);
        assertEquals(1, byUser2.size());
        assertTrue(byUser2.contains(list_user2));

        List<UserMetadataSelectionList> bySession1 = _repo.findBySessionId(sessionId);
        assertEquals(1, bySession1.size());
        assertTrue(bySession1.contains(list_session1));

        List<UserMetadataSelectionList> bySession1Users1 = _repo.findByUserOrSessionId(user1, sessionId);
        assertEquals(3, bySession1Users1.size());
        assertTrue(bySession1Users1.contains(list_user1));
        assertTrue(bySession1Users1.contains(list_user1b));
        assertTrue(bySession1Users1.contains(list_session1));

        List<UserMetadataSelectionList> bySession1Users2 = _repo.findByUserOrSessionId(user2, sessionId);
        assertEquals(2, bySession1Users2.size());
        assertTrue(bySession1Users2.contains(list_session1));
        assertTrue(bySession1Users2.contains(list_user2));


        UserMetadataSelectionList byNameUser1SessionId = _repo.findByNameAndUserOrSessionId("test case list_session1", user1, sessionId);
        assertNotNull(byNameUser1SessionId);
        assertEquals(list_session1, byNameUser1SessionId);


        UserMetadataSelectionList byNameUser1SessionId_1 = _repo.findByNameAndUserOrSessionId("BAD NAME", user1, sessionId);
        assertNull(byNameUser1SessionId_1);


        UserMetadataSelectionList byNameUser1SessionId_2 = _repo.findByNameAndUserOrSessionId("test case list_user2", user2, sessionId);
        assertNotNull(byNameUser1SessionId_2);
        assertEquals(list_user2, byNameUser1SessionId_2);

        UserMetadataSelectionList byNameUser1SessionId_3 = _repo.findByNameAndUserOrSessionId("test case list_session1", user2, sessionId);
        assertNotNull(byNameUser1SessionId_3);
        assertEquals(list_session1, byNameUser1SessionId_3);
    }

    @Test
    public void testFindBy2() {
        User user1 = _userRepository.save(UserRepositoryTest.newUser(_inc));
        User user2 = _userRepository.save(UserRepositoryTest.newUser(_inc));
        User user3 = _userRepository.save(UserRepositoryTest.newUser(_inc));

        String sessionId = UUID.randomUUID().toString();
        String sessionId2 = UUID.randomUUID().toString();


        UserMetadataSelectionList list_user1 = createListWithItems("test case public list_user1");
        list_user1.setUser(user1);
        _repo.save(list_user1);

        UserMetadataSelectionList list_user1b = createListWithItems("test case public list_user1b");
        list_user1b.setUser(user1);
        _repo.save(list_user1b);

        UserMetadataSelectionList list_user1c = createListWithItems("test case private list_user1c");
        list_user1c.setIsPublic(false);
        list_user1c.setUser(user1);
        _repo.save(list_user1c);

        UserMetadataSelectionList list_user2 = createListWithItems("test case public list_user2");
        list_user2.setUser(user2);
        _repo.save(list_user2);

        UserMetadataSelectionList list_user3 = createListWithItems("test case private list_user3");
        list_user3.setUser(user3);
        list_user3.setIsPublic(false);
        _repo.save(list_user3);

        UserMetadataSelectionList list_session1 = createListWithItems("test case public list_session1");
        list_session1.setSessionId(sessionId);
        _repo.save(list_session1);

        UserMetadataSelectionList list_session2 = createListWithItems("test case private list_session2");
        list_session2.setSessionId(sessionId2);
        list_session2.setIsPublic(false);
        _repo.save(list_session2);

        List<UserMetadataSelectionList> byUser1SessionId = _repo.findByUserOrSessionOrPublic( user1, sessionId);
        assertNotNull(byUser1SessionId);
        assertEquals(5, byUser1SessionId.size());
        assertTrue(!byUser1SessionId.contains(list_user3));  // private
        assertTrue(!byUser1SessionId.contains(list_session2)); // private

        List<UserMetadataSelectionList> byUser2 = _repo.findByUserOrSessionOrPublic( user2, "BAD SESSION");
        assertNotNull(byUser2);
        assertEquals(4, byUser2.size());
        assertTrue(!byUser2.contains(list_user1c));  // private
        assertTrue(!byUser2.contains(list_user3)); // private
        assertTrue(!byUser2.contains(list_session2)); // private


        List<UserMetadataSelectionList> bySession2 = _repo.findByUserOrSessionOrPublic( null, sessionId2);
        assertNotNull(bySession2);
        assertEquals(5, bySession2.size());
        assertTrue(!bySession2.contains(list_user1c));  // private
        assertTrue(!bySession2.contains(list_user3)); // private


        List<UserMetadataSelectionList> bySession1 = _repo.findByUserOrSessionOrPublic( null, sessionId);
        assertNotNull(bySession1);
        assertEquals(4, bySession1.size());
        assertTrue(!bySession1.contains(list_user1c));  // private
        assertTrue(!bySession1.contains(list_user3)); // private
        assertTrue(!bySession1.contains(list_session2)); // private

    }

}
