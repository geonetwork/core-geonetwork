//=============================================================================
//===	Copyright (C) 2001-2007 Food and Agriculture Organization of the
//===	United Nations (FAO-UN), United Nations World Food Programme (WFP)
//===	and United Nations Environment Programme (UNEP)
//===
//===	This program is free software; you can redistribute it and/or modify
//===	it under the terms of the GNU General Public License as published by
//===	the Free Software Foundation; either version 2 of the License, or (at
//===	your option) any later version.
//===
//===	This program is distributed in the hope that it will be useful, but
//===	WITHOUT ANY WARRANTY; without even the implied warranty of
//===	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
//===	General Public License for more details.
//===
//===	You should have received a copy of the GNU General Public License
//===	along with this program; if not, write to the Free Software
//===	Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
//===
//===	Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
//===	Rome - Italy. email: geonetwork@osgeo.org
//==============================================================================

package org.fao.geonet.kernel.security;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * Service to limit the number of failed login attempts, blocking the IP after
 * a number of failed login attempts for a number of minutes.
 *
 */
public class LoginAttemptService {
    private static final int DEFAULT_BLOCKTIME_MIN = 60;
    private static final int DEFAULT_MAX_ATTEMPT = 5;
    private LoadingCache<String, Integer> attemptsCache;

    // Number of failed login attempts to block an IP
    private int maxAttempt;
    // Number of minutes to block an IP
    private int blockTimeMinutes;

    public LoginAttemptService() {
      this(DEFAULT_MAX_ATTEMPT, DEFAULT_BLOCKTIME_MIN);
    }

    public LoginAttemptService(int maxAttempt, int blockTimeMinutes) {
        this.maxAttempt = (maxAttempt <= 0) ? DEFAULT_MAX_ATTEMPT: maxAttempt;
        this.blockTimeMinutes = (blockTimeMinutes <= 0) ? DEFAULT_BLOCKTIME_MIN: blockTimeMinutes;

        attemptsCache = CacheBuilder.newBuilder().
            expireAfterWrite(blockTimeMinutes, TimeUnit.MINUTES).build(new CacheLoader<String, Integer>() {
                public Integer load(String key) {
                    return 0;
                }
            });
    }

    public void loginSucceeded(String key) {
        attemptsCache.invalidate(key);
    }

    public void loginFailed(String key) {
        int attempts = 0;
        try {
            attempts = attemptsCache.get(key);
        } catch (ExecutionException e) {
            attempts = 0;
        }
        attempts++;
        attemptsCache.put(key, attempts);
    }

    public boolean isBlocked(String key) {
        try {
            return attemptsCache.get(key) >= maxAttempt;
        } catch (ExecutionException e) {
            return false;
        }
    }
}
