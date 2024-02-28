package org.fao.geonet.kernel.security.openidconnect.bearer;

import com.google.common.collect.Lists;
import junit.framework.TestCase;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.time.Instant;
import java.util.Collections;

public class UserInfoCacheTest extends TestCase {

    private OAuth2User user1 = new DefaultOAuth2User(Lists.newArrayList(), Collections.singletonMap("name", "frank"), "name");
    private OAuth2User user2 = new DefaultOAuth2User(Lists.newArrayList(), Collections.singletonMap("name", "jeff"), "name");

    public void testCache() {
        UserInfoCacheItem item1 = new UserInfoCacheItem("a", Instant.now().plusSeconds(1000), user1, Lists.newArrayList());
        UserInfoCacheItem item2 = new UserInfoCacheItem("b", Instant.now().plusSeconds(1000), user2, Lists.newArrayList());
        UserInfoCacheItem item3 = new UserInfoCacheItem("c", Instant.now().minusSeconds(1000), user2, Lists.newArrayList());

        UserInfoCache cache = new UserInfoCache();
        cache.putItem(item1);
        cache.putItem(item2);
        cache.putItem(item3);

        assertEquals(3, cache.cache.size());
        assertEquals(item1, cache.getItem("a"));
        assertEquals(item2, cache.getItem("b"));
        assertNull(cache.getItem("c"));
        assertEquals(2, cache.cache.size());
    }
}
