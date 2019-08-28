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

package org.fao.geonet.api.tools.migration;

import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.ContextAwareTask;
import org.fao.geonet.DatabaseMigrationTask;
import org.fao.geonet.api.API;
import org.fao.geonet.domain.Profile;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.sql.Connection;

import javax.sql.DataSource;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import jeeves.server.context.ServiceContext;

@RequestMapping(value = {
    "/{portal}/api/tools/migration",
    "/{portal}/api/" + API.VERSION_0_1 +
        "/tools/migration"
})
@Api(value = "tools",
    tags = "tools")
@Controller("migration")
public class MigrationApi {


    @ApiOperation(value = "Call a migration step",
        nickname = "callStep")
    @RequestMapping(value = "/steps/{stepName}",
        produces = MediaType.TEXT_PLAIN_VALUE,
        method = RequestMethod.PUT)
    @ResponseStatus(value = HttpStatus.CREATED)
    @PreAuthorize("hasRole('Administrator')")
    public ResponseEntity<String> callStep(
        @ApiParam(value = "Class name to execute corresponding to a migration step. See DatabaseMigrationTask.",
            example = "org.fao.geonet.api.records.attachments.MetadataResourceDatabaseMigration",
            required = true)
        @PathVariable
            String stepName) throws Exception {

        ApplicationContext appContext = ApplicationContextHolder.get();
        final DataSource dataSource = appContext.getBean(DataSource.class);
        try (Connection connection = dataSource.getConnection()) {
            Class<?> clazz = Class.forName(stepName);

            if (DatabaseMigrationTask.class.isAssignableFrom(clazz)) {
                DatabaseMigrationTask task =
                    (DatabaseMigrationTask) clazz.newInstance();
                connection.setAutoCommit(true);
                task.setContext(appContext);
                task.update(connection);
            } else if (ContextAwareTask.class.isAssignableFrom(clazz)) {
                ContextAwareTask task = (ContextAwareTask) clazz.newInstance();
                task.run(ApplicationContextHolder.get());
            } else {
                return new ResponseEntity<>(String.format(
                    "'%s' is not a valid DatabaseMigrationTask or ContextAwareTask. Choose a valid migration step.",
                    stepName), HttpStatus.BAD_REQUEST);
            }
            return new ResponseEntity<>("", HttpStatus.CREATED);
        } catch (ClassNotFoundException e) {
            return new ResponseEntity<>(String.format(
                "Class '%s' not found. Choose a valid migration step.",
                e.getMessage()
            ), HttpStatus.BAD_REQUEST);
        } catch (Exception ex) {
            String error = ex.getMessage();
            if (ex.getCause() != null)
                error = error + ". " + ex.getCause().getMessage();
            return new ResponseEntity<>(String.format(
                "Error occurred during migration step '%s'. %s.",
                stepName, error
            ), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
