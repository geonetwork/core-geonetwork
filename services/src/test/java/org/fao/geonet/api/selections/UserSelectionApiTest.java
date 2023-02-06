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
package org.fao.geonet.api.selections;


import org.fao.geonet.domain.UserMetadataSelectionList;
import org.junit.Test;
import org.springframework.mock.web.MockHttpSession;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertThrows;

public class UserSelectionApiTest extends UserSelectionApiSupport {


    /**
     * Simple test case;
     * a) as anonymous, create list
     * b) login as user1 (same sessionID), create another list
     * * both lists should be visible
     */
    @Test
    public void testCreateGetSimple() throws Exception {
        MockHttpSession session;
        UserMetadataSelectionList list;
        UserMetadataSelectionList one;
        List<UserMetadataSelectionList> all;

        //1. anonymous creates a list
        session = loginAsAnonymous();
        list = create(session,
            "testcase1", UserMetadataSelectionList.ListType.WatchList, new String[]{uuid1, uuid2});

        //1b. call "all" -- should just see the list we created
        all = Arrays.asList(getAllLists(session));
        assertEquals(1, all.size());
        areSame(list, all.get(0));

        //1c. call "get by id" -- should get the list we created
        one = getList(session, list.getId());
        areSame(list, one);

        //2. user1 creates a  list
        session = loginAs(user1, session.getId());
        UserMetadataSelectionList list2 = create(session,
            "testcase2", UserMetadataSelectionList.ListType.WatchList, new String[]{uuid1, uuid2});

        //2a. get "by id" - should get our original id
        one = getList(session, list2.getId());
        areSame(list2, one);

        //2c. get all -- should get both (we keep the same  session id)
        all = Arrays.asList(getAllLists(session));
        assertEquals(2, all.size());
        areSame(Arrays.asList(list, list2), all);
    }


    /**
     * 1. create a session (anonymous)
     * 2. create list with name=testcase (fine)
     * 3. create list with same name (error)
     */
    @Test
    public void testSessionSameName() throws Exception {
        MockHttpSession session;
        UserMetadataSelectionList list;

        //1. anonymous creates a list
        session = loginAsAnonymous();
        list = create(session,
            "testcase", UserMetadataSelectionList.ListType.WatchList, new String[]{uuid1, uuid2});

        //2 try to create list with the same name
        assertThrows(Exception.class, () -> {
            create(session, "testcase", UserMetadataSelectionList.ListType.WatchList, new String[]{uuid1, uuid2});
        });
    }

    /**
     * 1. create a session (user1)
     * 2. create list with name=testcase (fine)
     * 3. create list with same name (error)
     */
    @Test
    public void testUserSameName() throws Exception {
        MockHttpSession session;
        UserMetadataSelectionList list;

        //1. anonymous creates a list
        session = loginAs(user1);
        list = create(session,
            "testcase", UserMetadataSelectionList.ListType.WatchList, new String[]{uuid1, uuid2});

        //2 try to create list with the same name
        assertThrows(Exception.class, () -> {
            create(session, "testcase", UserMetadataSelectionList.ListType.WatchList, new String[]{uuid1, uuid2});
        });
    }

    /**
     * 1. create a session (anonymous)
     * 2. create list with name=testcase (fine)
     * 3. login as user (same sessionid)
     * 4. create list with same name (error)
     */
    @Test
    public void testSessionUserSameName() throws Exception {
        MockHttpSession session;
        UserMetadataSelectionList list;

        //1. anonymous creates a list
        session = loginAsAnonymous();
        list = create(session,
            "testcase", UserMetadataSelectionList.ListType.WatchList, new String[]{uuid1, uuid2});

        session = loginAs(user1, session.getId());


        //2 try to create list with the same name
        MockHttpSession finalSession = session;
        assertThrows(Exception.class, () -> {
            create(finalSession, "testcase", UserMetadataSelectionList.ListType.WatchList, new String[]{uuid1, uuid2});
        });
    }

    /**
     * tests visibility
     * 1. login as anonymous and create private list
     * 2. another anonymous user should not see it (different sessionid)
     * 3. another user should not see it (different sessionid)
     */
    @Test
    public void testVisibilitySession() throws Exception {
        MockHttpSession session;
        UserMetadataSelectionList list;
        List<UserMetadataSelectionList> all;

        //1. anonymous creates a list (private)
        session = loginAsAnonymous();
        list = create(session,
            "testcase", UserMetadataSelectionList.ListType.WatchList, new String[]{uuid1, uuid2});

        //another anonymous user cannot see it
        session = loginAsAnonymous();

        // try by id
        MockHttpSession finalSession = session;
        assertThrows(Exception.class, () -> {
            getList(finalSession, list.getId());
        });

        //get all (for user) contain it?
        all = Arrays.asList(getAllLists(session));
        doesNotContain(all, list);


        session = loginAs(user1);

        // try by id
        MockHttpSession finalSession2 = session;
        assertThrows(Exception.class, () -> {
            getList(finalSession2, list.getId());
        });

        //get all (for user) contain it?
        all = Arrays.asList(getAllLists(session));
        doesNotContain(all, list);
    }

    /**
     * tests visibility
     * 1. login as user1 and create private list
     * 2. anonymous user should not see it (different sessionid)
     * 3. another user (user2) should not see it (different sessionid)
     */
    @Test
    public void testVisibilityUser() throws Exception {
        MockHttpSession session;
        UserMetadataSelectionList list;
        List<UserMetadataSelectionList> all;

        //1. anonymous creates a list (private)
        session = loginAs(user1);
        list = create(session,
            "testcase", UserMetadataSelectionList.ListType.WatchList, new String[]{uuid1, uuid2});

        //another anonymous user cannot see it
        session = loginAsAnonymous();

        // try by id
        MockHttpSession finalSession = session;
        assertThrows(Exception.class, () -> {
            getList(finalSession, list.getId());
        });

        //get all (for user) contain it?
        all = Arrays.asList(getAllLists(session));
        doesNotContain(all, list);


        session = loginAs(user2);

        // try by id
        MockHttpSession finalSession2 = session;
        assertThrows(Exception.class, () -> {
            getList(finalSession2, list.getId());
        });

        //get all (for user) contain it?
        all = Arrays.asList(getAllLists(session));
        doesNotContain(all, list);
    }

    /**
     * update the name
     * 1. login as user
     * 2. change name
     * 3. verify
     */
    @Test
    public void testUpdateNameSimple() throws Exception {
        MockHttpSession session;
        UserMetadataSelectionList list, list2;
        List<UserMetadataSelectionList> all;

        session = loginAs(user1);
        list = create(session,
            "testcase", UserMetadataSelectionList.ListType.WatchList, new String[]{uuid1, uuid2});

        list2 = update(session, list.getId(), "TESTCASE2", new String[0], UserSelectionApi.ActionType.add);
        assertNotEquals(list.getChangeDate(), list2.getChangeDate());
        assertEquals("TESTCASE2", list2.getName());

        //get the list to make sure its fully saved
        list2 = getList(session, list.getId());
        assertEquals("TESTCASE2", list2.getName());
    }

    /**
     * update the name
     * 1. login as user
     * 2. create list1
     * 3. create list2
     * 4. change list2 name to duplicate
     * 5. should throw
     */
    @Test
    public void testUpdateNameUserDuplicate() throws Exception {
        MockHttpSession session;
        UserMetadataSelectionList list1;
        UserMetadataSelectionList list2;
        List<UserMetadataSelectionList> all;

        session = loginAs(user1);
        list1 = create(session,
            "testcase", UserMetadataSelectionList.ListType.WatchList, new String[]{uuid1, uuid2});

        list2 = create(session,
            "testcase2", UserMetadataSelectionList.ListType.WatchList, new String[]{uuid1, uuid2});


        MockHttpSession finalSession2 = session;
        UserMetadataSelectionList finalList = list2;
        assertThrows(Exception.class, () -> {
            update(session, finalList.getId(), "testcase", new String[0], UserSelectionApi.ActionType.add);
        });
    }

    /**
     * 1. create list with uuid1,uuid2
     * 2. add uuid3 -> uuid1,uuid2, uuid3
     * 3. add uuid2, uuid2, uuid3 (should not change the uuids) -> uuid1,uuid2, uuid3
     * 4. add nothing-> uuid1,uuid2, uuid3
     */
    @Test
    public void testAdd() throws Exception {
        MockHttpSession session;
        UserMetadataSelectionList list1;
        UserMetadataSelectionList list2;
        UserMetadataSelectionList list3;

        session = loginAs(user1);
        list1 = create(session,
            "testcase", UserMetadataSelectionList.ListType.WatchList, new String[]{uuid1, uuid2});

        list2 = update(session, list1.getId(), "testcase", new String[]{uuid3}, UserSelectionApi.ActionType.add);
        assertEquals(3, list2.getSelections().size());
        areSameUuids(Arrays.asList(uuid1, uuid2, uuid3), list2);
        assertNotEquals(list1.getChangeDate(), list2.getChangeDate());

        list2 = update(session, list1.getId(), "testcase", new String[]{uuid2, uuid2, uuid3}, UserSelectionApi.ActionType.add);
        assertEquals(3, list2.getSelections().size());
        areSameUuids(Arrays.asList(uuid1, uuid2, uuid3), list2);

        list3 = update(session, list1.getId(), "testcase", new String[]{}, UserSelectionApi.ActionType.add);
        assertEquals(3, list3.getSelections().size());
        areSameUuids(Arrays.asList(uuid1, uuid2, uuid3), list3);
    }

    /**
     * 1. create list with uuid1,uuid2
     * 2. remove uuid3 ->  uuid1,uuid2
     * 3. remove uuid2, uuid2 (both the same)  -> uuid1
     * 4. remove nothing->  uuid2
     */
    @Test
    public void testRemove() throws Exception {
        MockHttpSession session;
        UserMetadataSelectionList list1;
        UserMetadataSelectionList list2;
        UserMetadataSelectionList list3;

        session = loginAs(user1);
        list1 = create(session,
            "testcase", UserMetadataSelectionList.ListType.WatchList, new String[]{uuid1, uuid2});

        list2 = update(session, list1.getId(), "testcase", new String[]{uuid3}, UserSelectionApi.ActionType.remove);
        areSameUuids(Arrays.asList(uuid1, uuid2), list2);

        list2 = update(session, list1.getId(), "testcase", new String[]{uuid2, uuid2}, UserSelectionApi.ActionType.remove);
        areSameUuids(Arrays.asList(uuid1), list2);

        list3 = update(session, list1.getId(), "testcase", new String[]{}, UserSelectionApi.ActionType.remove);
        areSameUuids(Arrays.asList(uuid1), list3);
    }

    /**
     * 1. create list with uuid1,uuid2
     * 2. replace with uuid3 ->  uuid3
     * 4. replace with nothing->  []
     */
    @Test
    public void testReplace() throws Exception {
        MockHttpSession session;
        UserMetadataSelectionList list1;
        UserMetadataSelectionList list2;
        UserMetadataSelectionList list3;

        session = loginAs(user1);
        list1 = create(session,
            "testcase", UserMetadataSelectionList.ListType.WatchList, new String[]{uuid1, uuid2});

        list2 = update(session, list1.getId(), "testcase", new String[]{uuid3}, UserSelectionApi.ActionType.replace);
        areSameUuids(Arrays.asList(uuid3), list2);


        list3 = update(session, list1.getId(), "testcase", new String[]{}, UserSelectionApi.ActionType.replace);
        areSameUuids(Arrays.asList(), list3);
    }

    /**
     * 1. create list (defaults to Private)
     * 2. set the status to public
     * 3. verify
     */
    @Test
    public void changeStatus() throws Exception {
        MockHttpSession session;
        UserMetadataSelectionList list1, list2;

        session = loginAs(user1);
        list1 = create(session,
            "testcase", UserMetadataSelectionList.ListType.WatchList, new String[]{uuid1, uuid2});
        assertEquals(false, list1.getIsPublic());

        list2 = setstatus(session, list1.getId(), true);
        assertEquals(true, list2.getIsPublic());
        assertNotEquals(list1.getChangeDate(), list2.getChangeDate());

        list2 = setstatus(session, list1.getId(), false);
        assertEquals(false, list2.getIsPublic());
    }

    /**
     * 1. create list (private)
     * 2. user1 can see it, user2 and anonymous cannot
     * 3. change to public
     * 4. user1, user2, and anonymous can see it
     */
    @Test
    public void changeStatusPermissions() throws Exception {
        MockHttpSession sessionUser1, sessionUser2, sessionAnonymous;
        UserMetadataSelectionList list1, list2;
        List<UserMetadataSelectionList> all;

        sessionUser1 = loginAs(user1);
        sessionUser2 = loginAs(user2);
        sessionAnonymous = loginAsAnonymous();

        list1 = create(sessionUser1,
            "testcase", UserMetadataSelectionList.ListType.WatchList, new String[]{uuid1, uuid2});

        //user1 can retrieve and see
        all = Arrays.asList(getAllLists(sessionUser1));
        contains(all, list1.getId());
        list2 = getList(sessionUser1, list1.getId());
        assertEquals(list1.getId(), list2.getId());

        //user2 cannot see/retrieve
        all = Arrays.asList(getAllLists(sessionUser2));
        notContains(all, list1.getId());
        UserMetadataSelectionList finalList = list1;
        assertThrows(Exception.class, () -> {
            getList(sessionUser2, finalList.getId());
        });

        //Anonymous cannot see/retrieve
        all = Arrays.asList(getAllLists(sessionUser2));
        notContains(all, list1.getId());
        UserMetadataSelectionList finalList2 = list1;
        assertThrows(Exception.class, () -> {
            getList(sessionUser2, finalList2.getId());
        });


        list1 = setstatus(sessionUser1, list1.getId(), true);
        assertEquals(true, list1.getIsPublic());

        //user1 can retrieve and see
        all = Arrays.asList(getAllLists(sessionUser1));
        contains(all, list1.getId());
        list2 = getList(sessionUser1, list1.getId());
        assertEquals(list1.getId(), list2.getId());


        //user2 can retrieve and see
        all = Arrays.asList(getAllLists(sessionUser2));
        contains(all, list1.getId());
        list2 = getList(sessionUser2, list1.getId());
        assertEquals(list1.getId(), list2.getId());

        //Anonymous can retrieve and see
        all = Arrays.asList(getAllLists(sessionAnonymous));
        contains(all, list1.getId());
        list2 = getList(sessionAnonymous, list1.getId());
        assertEquals(list1.getId(), list2.getId());

    }

    /**
     * 1. create list
     * 2. delete it
     */
    @Test
    public void delete() throws Exception {
        MockHttpSession session;
        UserMetadataSelectionList list1;
        Boolean result;

        session = loginAs(user1);
        list1 = create(session,
            "testcase", UserMetadataSelectionList.ListType.WatchList, new String[]{uuid1, uuid2});

        result = deleteItem(session, list1.getId());
        assertEquals(true, result);

        MockHttpSession finalSession = session;
        assertThrows(Exception.class, () -> {
            getList(finalSession, list1.getId());
        });
    }

    /**
     * 1. user1 creates list
     * 2. user1 makes list public
     * 3. user2 and anonymous should not be allowed to modify (delete, update, change status)
     */
    @Test
    public void permissionOnPublic() throws Exception {
        MockHttpSession sessionUser1, sessionUser2, sessionAnonymous;
        UserMetadataSelectionList list1, list2;

        sessionUser1 = loginAs(user1);
        sessionUser2 = loginAs(user2);
        sessionAnonymous = loginAsAnonymous();

        list1 = create(sessionUser1,
            "testcase", UserMetadataSelectionList.ListType.WatchList, new String[]{uuid1, uuid2});


        list2 = setstatus(sessionUser1, list1.getId(), true);
        assertEquals(true, list2.getIsPublic());


        assertThrows(Exception.class, () -> {
            deleteItem(sessionUser2, list1.getId());
        });

        assertThrows(Exception.class, () -> {
            update(sessionUser2, list1.getId(), "newname", new String[0], UserSelectionApi.ActionType.add);
        });

        assertThrows(Exception.class, () -> {
            setstatus(sessionUser2, list1.getId(), true);
        });


        assertThrows(Exception.class, () -> {
            deleteItem(sessionAnonymous, list1.getId());
        });
        assertThrows(Exception.class, () -> {
            update(sessionAnonymous, list1.getId(), "newname", new String[0], UserSelectionApi.ActionType.add);
        });

        assertThrows(Exception.class, () -> {
            setstatus(sessionAnonymous, list1.getId(), true);
        });
    }


}
