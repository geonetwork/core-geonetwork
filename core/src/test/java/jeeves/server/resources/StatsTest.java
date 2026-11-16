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

package jeeves.server.resources;

import org.apache.commons.dbcp2.BasicDataSource;
import org.junit.Test;
import org.springframework.jdbc.datasource.AbstractDataSource;

import java.sql.Connection;

import static org.junit.Assert.assertEquals;

public class StatsTest {

    @Test
    public void testDbcp2UsesMaxTotal() throws Exception {
        try (BasicDataSource source = new BasicDataSource()) {
            source.setMaxTotal(20);
            Stats stats = new Stats(source);
            assertEquals(0, stats.numActive.intValue());
            assertEquals(0, stats.numIdle.intValue());
            assertEquals(20, stats.maxActive.intValue());
        }
    }

    @Test
    public void testDbcp2UnboundedPool() throws Exception {
        try (BasicDataSource source = new BasicDataSource()) {
            source.setMaxTotal(-1);
            Stats stats = new Stats(source);
            assertEquals(0, stats.numActive.intValue());
            assertEquals(-1, stats.maxActive.intValue());
        }
    }

    @Test
    public void testPoolWithMaxActiveOnly() {
        // Shape of commons-dbcp 1.x and Tomcat jdbc-pool
        Stats stats = new Stats(new MaxActivePool());
        assertEquals(3, stats.numActive.intValue());
        assertEquals(4, stats.numIdle.intValue());
        assertEquals(50, stats.maxActive.intValue());
    }

    @Test
    public void testHikariPool() {
        Stats stats = new Stats(new HikariLikePool(new HikariPoolBean()));
        assertEquals(5, stats.numActive.intValue());
        assertEquals(6, stats.numIdle.intValue());
        assertEquals(10, stats.maxActive.intValue());
    }

    @Test
    public void testHikariPoolNotStarted() {
        // getHikariPoolMXBean() returns null until the pool is started
        assertUnavailable(new Stats(new HikariLikePool(null)));
    }

    @Test
    public void testUnknownDataSource() {
        assertUnavailable(new Stats(new UnknownPool()));
    }

    private static void assertUnavailable(Stats stats) {
        assertEquals(-1, stats.numActive.intValue());
        assertEquals(-1, stats.numIdle.intValue());
        assertEquals(-1, stats.maxActive.intValue());
    }

    // Stubs must be public: Stats reads them by reflection.
    public static class UnknownPool extends AbstractDataSource {
        @Override
        public Connection getConnection() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Connection getConnection(String username, String password) {
            throw new UnsupportedOperationException();
        }
    }

    public static class MaxActivePool extends UnknownPool {
        public int getNumActive() {
            return 3;
        }

        public int getNumIdle() {
            return 4;
        }

        public int getMaxActive() {
            return 50;
        }
    }

    public static class HikariPoolBean {
        public int getActiveConnections() {
            return 5;
        }

        public int getIdleConnections() {
            return 6;
        }
    }

    public static class HikariLikePool extends UnknownPool {
        private final HikariPoolBean pool;

        public HikariLikePool(HikariPoolBean pool) {
            this.pool = pool;
        }

        public HikariPoolBean getHikariPoolMXBean() {
            return pool;
        }

        public int getMaximumPoolSize() {
            return 10;
        }
    }
}
