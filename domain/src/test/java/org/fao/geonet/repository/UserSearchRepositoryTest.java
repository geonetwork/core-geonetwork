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

import org.fao.geonet.domain.*;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;

public class UserSearchRepositoryTest extends AbstractSpringDataTest {
    @Autowired
    UserSearchRepository userSearchRepository;
    @Autowired
    UserRepository userRepository;
    @PersistenceContext
    private EntityManager entityManager;

    public static UserSearch newUserSearch(AtomicInteger inc) {
        String val = String.format("%04d", inc.incrementAndGet());
        UserSearch userSearch = new UserSearch().setUrl("http://search/id" + val)
            .setCreationDate(new Date()).setFeaturedType(UserSearchFeaturedType.HOME);
        return userSearch;
    }

    @Test
    public void testFindByCreator() {
        User user1 = userRepository.save(UserRepositoryTest.newUser(_inc));
        User user2 = userRepository.save(UserRepositoryTest.newUser(_inc));

        UserSearch userSearch = newUserSearch();
        userSearch.setCreator(user1);
        userSearchRepository.save(userSearch);

        UserSearch userSearch2 = newUserSearch();
        userSearch2.setCreator(user1);
        userSearchRepository.save(userSearch2);

        UserSearch userSearch3 = newUserSearch();
        userSearch3.setCreator(user2);
        userSearchRepository.save(userSearch3);


        List<UserSearch> userSearches = userSearchRepository.findAllByCreator(user1);
        assertEquals(2, userSearches.size());

        userSearches = userSearchRepository.findAllByCreator(user2);
        assertEquals(1, userSearches.size());
    }


    @Test
    public void testFindByFeaturedType() {
        User user1 = userRepository.save(UserRepositoryTest.newUser(_inc));
        User user2 = userRepository.save(UserRepositoryTest.newUser(_inc));

        UserSearch userSearch = newUserSearch();
        userSearch.setCreator(user1);
        userSearch.setFeaturedType(UserSearchFeaturedType.HOME);
        userSearchRepository.save(userSearch);

        UserSearch userSearch2 = newUserSearch();
        userSearch2.setCreator(user1);
        userSearch.setFeaturedType(UserSearchFeaturedType.HOME);
        userSearchRepository.save(userSearch2);

        UserSearch userSearch3 = newUserSearch();
        userSearch3.setCreator(user2);
        userSearch.setFeaturedType(UserSearchFeaturedType.EDITOR_BOARD);
        userSearchRepository.save(userSearch3);

        List<UserSearch> userSearches = userSearchRepository.findAllByFeaturedType(UserSearchFeaturedType.HOME);
        assertEquals(2, userSearches.size());

        userSearches = userSearchRepository.findAllByFeaturedType(UserSearchFeaturedType.EDITOR_BOARD);
        assertEquals(1, userSearches.size());
    }

    private UserSearch newUserSearch() {
        UserSearch userSearch = newUserSearch(_inc);
        return userSearch;
    }

}
