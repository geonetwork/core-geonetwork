package org.fao.geonet.repository.specification;

import org.fao.geonet.domain.User;
import org.fao.geonet.domain.UserSearch;
import org.fao.geonet.repository.*;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.junit.Assert.*;

public class UserSearchSpecsTests  extends AbstractSpringDataTest {

    @Autowired
    UserSearchRepository userSearchRepository;
    @Autowired
    UserRepository userRepository;

    @Test
    public void testIsFeatured() {
        User user1 = userRepository.save(UserRepositoryTest.newUser(_inc));

        UserSearch userSearch = UserSearchRepositoryTest.newUserSearch(_inc);
        userSearch.setCreator(user1);
        userSearch.setFeatured(true);
        userSearchRepository.save(userSearch);

        UserSearch userSearch2 = UserSearchRepositoryTest.newUserSearch(_inc);
        userSearch2.setCreator(user1);
        userSearch2.setFeatured(false);
        userSearchRepository.save(userSearch2);

        UserSearch userSearch3 = UserSearchRepositoryTest.newUserSearch(_inc);
        userSearch3.setCreator(user1);
        userSearch3.setFeatured(true);
        userSearchRepository.save(userSearch3);

        List<UserSearch> userSearchList = userSearchRepository.findAll(UserSearchSpecs.isFeatured());
        assertEquals(2, userSearchList.size());
    }
}
