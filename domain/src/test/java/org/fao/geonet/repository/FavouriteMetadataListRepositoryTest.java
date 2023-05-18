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

import org.fao.geonet.domain.FavouriteMetadataList;
import org.fao.geonet.domain.FavouriteMetadataListItem;
import org.fao.geonet.domain.ISODate;
import org.fao.geonet.domain.User;
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

public class FavouriteMetadataListRepositoryTest extends AbstractSpringDataTest {

    @Autowired
    FavouriteMetadataListRepository repo;

    @Autowired
    UserRepository userRepository;


    private static ISODate date1 = new ISODate("2023-06-01T01:02:02");
    private static ISODate date2 = new ISODate("2023-06-01T01:03:03");

    public static FavouriteMetadataList createSimpleList() {
        FavouriteMetadataList list = new FavouriteMetadataList();
        list.setListType(FavouriteMetadataList.ListType.WatchList);
        list.setCreateDate(date1);
        list.setChangeDate(date2);
        list.setIsPublic(true);
        list.setName("test case list");

        return list;
    }

    public static FavouriteMetadataList createListWithItems(String name) {
        return createListWithItems(name,"");
    }

        public static FavouriteMetadataList createListWithItems(String name, String prefix) {
        FavouriteMetadataList list = createSimpleList();
        list.setName(name);
        FavouriteMetadataListItem item1 = new FavouriteMetadataListItem();
        item1.setMetadataUuid(prefix+"metadataid1");
        FavouriteMetadataListItem item2 = new FavouriteMetadataListItem();
        item2.setMetadataUuid(prefix+"metadataid2");

        list.setSelections(Arrays.asList(item1, item2));
        return list;
    }

    @Test
    public void testSaveSimple() {
        FavouriteMetadataList list = createSimpleList();

        repo.save(list);

        FavouriteMetadataList list2 = repo.findById(list.getId()).get();

        assertEquals(list.getListType(), list2.getListType());
        assertEquals(list.getCreateDate(), list2.getCreateDate());
        assertEquals(list.getChangeDate(), list2.getChangeDate());
        assertEquals(list.getIsPublic(), list2.getIsPublic());
        assertEquals(list.getName(), list2.getName());
        assertNotEquals(0, list2.getId());
    }

    @Test
    public void testSaveWithItems() {
        FavouriteMetadataList list = createListWithItems("test case list");

        repo.save(list);

        FavouriteMetadataList list2 = repo.findById(list.getId()).get();
        assertEquals(list.getSelections().size(), list2.getSelections().size());
        assertEquals(list.getSelections().get(0).getMetadataUuid(), list2.getSelections().get(0).getMetadataUuid());
        assertEquals(list.getSelections().get(1).getMetadataUuid(), list2.getSelections().get(1).getMetadataUuid());

        assertNotEquals(0, list2.getId());
        assertNotEquals(0, list.getSelections().get(0).getId());
        assertNotEquals(0, list.getSelections().get(1).getId());
    }


    @Test
    public void testFindBy() {
        User user1 = userRepository.save(UserRepositoryTest.newUser(_inc));
        User user2 = userRepository.save(UserRepositoryTest.newUser(_inc));
        String sessionId = UUID.randomUUID().toString();

        FavouriteMetadataList list_user1 = createListWithItems("test case list_user1");
        list_user1.setUser(user1);
        repo.save(list_user1);

        FavouriteMetadataList list_user1b = createListWithItems("test case list_user1b");
        list_user1b.setUser(user1);
        repo.save(list_user1b);

        FavouriteMetadataList list_user2 = createListWithItems("test case list_user2");
        list_user2.setUser(user2);
        repo.save(list_user2);

        FavouriteMetadataList list_session1 = createListWithItems("test case list_session1");
        list_session1.setSessionId(sessionId);
        repo.save(list_session1);

        // we saved 2 lists by user1 - list_user1 list_user1b
        List<FavouriteMetadataList> byUser1 = repo.findByUser(user1);
        assertEquals(2, byUser1.size());
        assertTrue(byUser1.contains(list_user1));
        assertTrue(byUser1.contains(list_user1b));

        List<FavouriteMetadataList> byUser2 = repo.findByUser(user2);
        assertEquals(1, byUser2.size());
        assertTrue(byUser2.contains(list_user2));

        List<FavouriteMetadataList> bySession1 = repo.findBySessionId(sessionId);
        assertEquals(1, bySession1.size());
        assertTrue(bySession1.contains(list_session1));

        List<FavouriteMetadataList> bySession1Users1 = repo.findByUserOrSessionId(user1, sessionId);
        assertEquals(3, bySession1Users1.size());
        assertTrue(bySession1Users1.contains(list_user1));
        assertTrue(bySession1Users1.contains(list_user1b));
        assertTrue(bySession1Users1.contains(list_session1));

        List<FavouriteMetadataList> bySession1Users2 = repo.findByUserOrSessionId(user2, sessionId);
        assertEquals(2, bySession1Users2.size());
        assertTrue(bySession1Users2.contains(list_session1));
        assertTrue(bySession1Users2.contains(list_user2));


        FavouriteMetadataList byNameUser1SessionId = repo.findByNameAndUserOrSessionId("test case list_session1", user1, sessionId);
        assertNotNull(byNameUser1SessionId);
        assertEquals(list_session1, byNameUser1SessionId);


        FavouriteMetadataList byNameUser1SessionId_1 = repo.findByNameAndUserOrSessionId("BAD NAME", user1, sessionId);
        assertNull(byNameUser1SessionId_1);


        FavouriteMetadataList byNameUser1SessionId_2 = repo.findByNameAndUserOrSessionId("test case list_user2", user2, sessionId);
        assertNotNull(byNameUser1SessionId_2);
        assertEquals(list_user2, byNameUser1SessionId_2);

        FavouriteMetadataList byNameUser1SessionId_3 = repo.findByNameAndUserOrSessionId("test case list_session1", user2, sessionId);
        assertNotNull(byNameUser1SessionId_3);
        assertEquals(list_session1, byNameUser1SessionId_3);
    }

    @Test
    public void testFindBy2() {
        User user1 = userRepository.save(UserRepositoryTest.newUser(_inc));
        User user2 = userRepository.save(UserRepositoryTest.newUser(_inc));
        User user3 = userRepository.save(UserRepositoryTest.newUser(_inc));

        String sessionId = UUID.randomUUID().toString();
        String sessionId2 = UUID.randomUUID().toString();


        FavouriteMetadataList list_user1 = createListWithItems("test case public list_user1");
        list_user1.setUser(user1);
        repo.save(list_user1);

        FavouriteMetadataList list_user1b = createListWithItems("test case public list_user1b");
        list_user1b.setUser(user1);
        repo.save(list_user1b);

        FavouriteMetadataList list_user1c = createListWithItems("test case private list_user1c");
        list_user1c.setIsPublic(false);
        list_user1c.setUser(user1);
        repo.save(list_user1c);

        FavouriteMetadataList list_user2 = createListWithItems("test case public list_user2");
        list_user2.setUser(user2);
        repo.save(list_user2);

        FavouriteMetadataList list_user3 = createListWithItems("test case private list_user3");
        list_user3.setUser(user3);
        list_user3.setIsPublic(false);
        repo.save(list_user3);

        FavouriteMetadataList list_session1 = createListWithItems("test case public list_session1");
        list_session1.setSessionId(sessionId);
        repo.save(list_session1);

        FavouriteMetadataList list_session2 = createListWithItems("test case private list_session2");
        list_session2.setSessionId(sessionId2);
        list_session2.setIsPublic(false);
        repo.save(list_session2);

        List<FavouriteMetadataList> byUser1SessionId = repo.findByUserOrSessionOrPublic( user1, sessionId);
        assertNotNull(byUser1SessionId);
        assertEquals(5, byUser1SessionId.size());
        assertTrue(!byUser1SessionId.contains(list_user3));  // private
        assertTrue(!byUser1SessionId.contains(list_session2)); // private

        List<FavouriteMetadataList> byUser2 = repo.findByUserOrSessionOrPublic( user2, "BAD SESSION");
        assertNotNull(byUser2);
        assertEquals(4, byUser2.size());
        assertTrue(!byUser2.contains(list_user1c));  // private
        assertTrue(!byUser2.contains(list_user3)); // private
        assertTrue(!byUser2.contains(list_session2)); // private


        List<FavouriteMetadataList> bySession2 = repo.findByUserOrSessionOrPublic( null, sessionId2);
        assertNotNull(bySession2);
        assertEquals(5, bySession2.size());
        assertTrue(!bySession2.contains(list_user1c));  // private
        assertTrue(!bySession2.contains(list_user3)); // private


        List<FavouriteMetadataList> bySession1 = repo.findByUserOrSessionOrPublic( null, sessionId);
        assertNotNull(bySession1);
        assertEquals(4, bySession1.size());
        assertTrue(!bySession1.contains(list_user1c));  // private
        assertTrue(!bySession1.contains(list_user3)); // private
        assertTrue(!bySession1.contains(list_session2)); // private

    }

}
