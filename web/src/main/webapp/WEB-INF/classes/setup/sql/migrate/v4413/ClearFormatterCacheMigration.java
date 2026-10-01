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


package v4413;

import org.fao.geonet.DatabaseMigrationTask;
import org.fao.geonet.api.records.formatters.cache.FormatterCache;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.utils.Log;

import java.sql.Connection;

/**
 * The shared formatter cache only stores the output generated for anonymous requests. The cache of
 * previous versions can have output generated for authenticated users, so it is removed.
 */
public class ClearFormatterCacheMigration extends DatabaseMigrationTask {
    @Override
    public void update(Connection connection) {
        Log.info(Geonet.DB, "Executing migration ClearFormatterCacheMigration");
        try {
            applicationContext.getBean(FormatterCache.class).clear();
        } catch (Exception e) {
            // Do not block the upgrade, the cache can be cleared with DELETE /api/formatters/cache
            Log.warning(Geonet.DB, "Could not clear the formatter cache, clear it manually with the "
                + "administration API (DELETE /api/formatters/cache): " + e.getMessage());
        }
    }
}
