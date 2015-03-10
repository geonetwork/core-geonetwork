package org.fao.geonet.services.metadata.format.groovy;

import com.google.common.collect.Lists;
import groovy.lang.Closure;
import org.codehaus.groovy.runtime.GStringImpl;
import org.fao.geonet.services.metadata.format.FormatterParams;
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