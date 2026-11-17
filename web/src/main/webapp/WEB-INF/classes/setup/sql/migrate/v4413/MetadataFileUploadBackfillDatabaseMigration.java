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
import org.fao.geonet.MetadataFileUploadBackfillTask;
import org.fao.geonet.exceptions.TaskExecutionException;
import org.fao.geonet.migration.DatabaseMigrationException;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Runs {@link MetadataFileUploadBackfillTask} automatically as part of the migration to 4.4.13,
 * instead of requiring an administrator to trigger it manually via
 * {@code PUT /{portal}/api/tools/migration/steps/org.fao.geonet.MetadataFileUploadBackfillTask}.
 * <p>
 * {@code database_migration.xml}'s {@code java:} step runner only invokes
 * {@link DatabaseMigrationTask} subclasses, not {@link org.fao.geonet.ContextAwareTask}
 * implementations like {@code MetadataFileUploadBackfillTask} itself - this thin wrapper exists
 * purely to bridge that gap, so the task's own logic and its manual invocation path are
 * unchanged.
 * <p>
 * The wrapped task walks every metadata record's attachments through the configured store,
 * which - for a remote-backed store (S3/CMIS/JClouds) - can take a while; this runs once, the
 * single startup that carries the database across the 4.4.13 boundary (see
 * {@code DatabaseMigration}'s version-gating), not on every restart.
 */
public class MetadataFileUploadBackfillDatabaseMigration extends DatabaseMigrationTask {
    @Override
    public void update(Connection connection) throws SQLException, DatabaseMigrationException {
        try {
            new MetadataFileUploadBackfillTask().run(applicationContext);
        } catch (TaskExecutionException e) {
            throw new DatabaseMigrationException(
                "Unable to backfill MetadataFileUploads / migrate legacy attachments to the flat layout", e);
        }
    }
}
