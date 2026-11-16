/*
 * Copyright (C) 2001-2026 Food and Agriculture Organization of the
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
package org.fao.geonet.api.users;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.password.StandardPasswordEncoder;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * A {@link PasswordEncoder} that behaves like the catalogue one but counts how many times its
 * {@code matches} method is called. Used to check that the change key is verified on every branch
 * of the password update, whether or not the user exists.
 */
public class CountingPasswordEncoder implements PasswordEncoder {
    private static final AtomicInteger MATCHES_COUNT = new AtomicInteger();

    private final PasswordEncoder delegate = new StandardPasswordEncoder("salt");

    public static void resetMatchesCount() {
        MATCHES_COUNT.set(0);
    }

    public static int getMatchesCount() {
        return MATCHES_COUNT.get();
    }

    @Override
    public String encode(CharSequence rawPassword) {
        return delegate.encode(rawPassword);
    }

    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        MATCHES_COUNT.incrementAndGet();
        return delegate.matches(rawPassword, encodedPassword);
    }
}
