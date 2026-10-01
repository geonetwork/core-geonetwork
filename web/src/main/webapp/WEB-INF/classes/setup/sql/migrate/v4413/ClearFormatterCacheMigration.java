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

import java.sql.Connection;
import java.sql.SQLException;

import org.fao.geonet.DatabaseMigrationTask;
import org.fao.geonet.api.records.formatters.cache.FormatterCache;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.GeonetworkDataDirectory;
import org.fao.geonet.utils.Log;
import org.springframework.context.ApplicationContext;

/**
 * Migration task to clear formatter cache after key changes (e.g. portal isolation).
 * Follows the deferred execution pattern: sets a flag so cache is cleared during startup
 * once GeonetworkDataDirectory is initialized.
 */
public class ClearFormatterCacheMigration extends DatabaseMigrationTask {
    private FormatterCache formatterCache;

    @Override
    public void setContext(ApplicationContext applicationContext) {
        super.setContext(applicationContext);
        try {
            formatterCache = applicationContext.getBean(FormatterCache.class);
            formatterCache.setClearCacheRequired(true);
        } catch (Exception e) {
            Log.error(Geonet.GEONETWORK, "Error obtaining FormatterCache bean: " + e.getMessage(), e);
        }
    }

    @Override
    public void update(Connection connection) throws SQLException {
        Log.debug(Geonet.DB, "ClearFormatterCacheMigration");
        if (formatterCache != null) {
            formatterCache.setClearCacheRequired(true);
            try {
                GeonetworkDataDirectory dataDirectory = applicationContext.getBean(GeonetworkDataDirectory.class);
                if (dataDirectory.getSystemDataDir() != null) {
                    formatterCache.clear();
                    formatterCache.setClearCacheRequired(false);
                }
            } catch (Exception e) {
                // If dataDirectory is not yet initialized (startup migration),
                // FormatterCache will clear the cache via GeonetworkDataDirectoryInitializedEvent once dataDirectory is initialized.
            }
        }
    }
}
