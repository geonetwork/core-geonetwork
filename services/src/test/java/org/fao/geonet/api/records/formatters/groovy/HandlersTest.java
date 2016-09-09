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

package org.fao.geonet.api.records.formatters.groovy;

import com.google.common.collect.Lists;

import groovy.lang.Closure;

import org.codehaus.groovy.runtime.GStringImpl;
import org.fao.geonet.api.records.formatters.FormatterParams;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;

public class HandlersTest {

    @Test
    public void testRoots() throws Exception {
        final Handlers handlers = new Handlers(new FormatterParams(), null, null, null);
        handlers.roots("root1", "root2");

        Assert.assertArrayEquals(new String[]{"root1", "root2"}, sort(handlers.getRoots().toArray()));

        handlers.root("newroot");
        Assert.assertArrayEquals(new String[]{"newroot", "root1", "root2"}, sort(handlers.getRoots().toArray()));

        handlers.root(new GStringImpl(new Object[0], new String[]{"newroot2"}));
        Assert.assertArrayEquals(new String[]{"newroot", "newroot2", "root1", "root2"}, sort(handlers.getRoots().toArray()));

        handlers.roots("root1", new GStringImpl(new Object[0], new String[]{"root2"}));
        Assert.assertArrayEquals(new Object[]{"root1", "root2"}, sort(handlers.getRoots().toArray()));

        handlers.roots(new Closure(handlers) {
            @Override
            public Object call() {
                return Lists.newArrayList("cr1", "cr2");
            }

            @Override
            public Object call(Object arguments) {
                return call();
            }
        });
        Assert.assertArrayEquals(new String[]{"cr1", "cr2"}, sort(handlers.getRoots().toArray()));

        handlers.roots(new Closure(handlers) {
            @Override
            public Object call() {
                return Lists.newArrayList(new GStringImpl(new Object[0], new String[]{"gr1"}));
            }

            @Override
            public Object call(Object arguments) {
                return call();
            }
        });
        Assert.assertArrayEquals(new String[]{"gr1"}, sort(handlers.getRoots().toArray()));
    }

    private Object[] sort(Object[] objects) {
        Arrays.sort(objects);
        return objects;
    }
}
