/*
 * Copyright (C) 2022 Food and Agriculture Organization of the
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
