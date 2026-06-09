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
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;

public class UserInfoCacheTest extends TestCase {

    private final Jwt jwt1 = Jwt.withTokenValue("token1")
        .header("alg", "none")
        .claim("sub", "frank")
        .expiresAt(Instant.now().plusSeconds(1000))
        .issuedAt(Instant.now())
        .build();

    private final Jwt jwt2 = Jwt.withTokenValue("token2")
        .header("alg", "none")
        .claim("sub", "jeff")
        .expiresAt(Instant.now().plusSeconds(1000))
        .issuedAt(Instant.now())
        .build();

    private final Jwt jwt3 = Jwt.withTokenValue("token3")
        .header("alg", "none")
        .claim("sub", "jeff")
        .expiresAt(Instant.now().minusSeconds(1000))
        .issuedAt(Instant.now().minusSeconds(2000))
        .build();

    public void testCache() {
        UserInfoCacheItem item1 = new UserInfoCacheItem("a", jwt1, Lists.newArrayList(), "frank");
        UserInfoCacheItem item2 = new UserInfoCacheItem("b", jwt2, Lists.newArrayList(), "jeff");
        UserInfoCacheItem item3 = new UserInfoCacheItem("c", jwt3, Lists.newArrayList(), "jeff");

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
