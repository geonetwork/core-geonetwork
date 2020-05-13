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

package org.fao.geonet.api.records.backup;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import jeeves.server.context.ServiceContext;
import jeeves.server.dispatchers.ServiceManager;
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.api.API;
import org.fao.geonet.api.ApiUtils;
import org.fao.geonet.api.exception.ResourceNotFoundException;
import org.fao.geonet.kernel.GeonetworkDataDirectory;
import org.fao.geonet.kernel.backup.ArchiveAllMetadataJob;
import org.fao.geonet.utils.Log;
import org.quartz.Scheduler;
import org.quartz.Trigger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.FileFilter;

import static org.fao.geonet.api.ApiParams.API_CLASS_RECORD_OPS;
import static org.fao.geonet.api.ApiParams.API_CLASS_RECORD_TAG;
import static org.quartz.TriggerBuilder.newTrigger;

@RequestMapping(value = {
        "/{portal}/api/records/backups",
        "/{portal}/api/" + API.VERSION_0_1 +
                "/records/backups"
})
@Api(value = API_CLASS_RECORD_TAG,
        tags = API_CLASS_RECORD_TAG,
        description = API_CLASS_RECORD_OPS)

@Controller
public class BackupArchiveApi {

    @Autowired
    ArchiveAllMetadataJob archiveAllMetadataJob;

    @ApiOperation(value = "Download MEF backup archive",
            notes = "The backup contains all metadata not harvested including templates.",
            nickname = "downloadBackup")
    @PreAuthorize("hasRole('Administrator')")
    @RequestMapping(
            value="/latest",
            method = RequestMethod.GET,
            produces = "application/zip")
    @ApiResponses(value = {
            @ApiResponse(code = 404, message = "Resource not found.")
    })
    @ResponseBody
    public ResponseEntity<FileSystemResource> exec(HttpServletRequest request) throws Exception {

        ServiceContext context = ApiUtils.createServiceContext(request);
        ApplicationContext appContext = ApplicationContextHolder.get();
        GeonetworkDataDirectory dataDirectory = appContext.getBean(GeonetworkDataDirectory.class);
        ServiceManager serviceManager = appContext.getBean(ServiceManager.class);

        Log.info(ArchiveAllMetadataJob.BACKUP_LOG, "User " + context.getUserSession().getUsername() + " from IP: " + context
                .getIpAddress() + " has started to download backup archive");

        File backupDir = dataDirectory.getBackupDir().resolve(ArchiveAllMetadataJob.BACKUP_DIR).toFile();
        if (!backupDir.exists()) {
            throw new ResourceNotFoundException("Backup archive folder does not exist");
        }
        File[] files = backupDir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return !pathname.isDirectory();
            }
        });
        if (files == null || files.length == 0) {
            throw new ResourceNotFoundException("Backup archive file does not yet exist");
        }

        MultiValueMap<String, String> headers = new HttpHeaders();
        headers.add("content-disposition", "attachment; filename=" + files[0].getName());

        final ResponseEntity<FileSystemResource> response = new ResponseEntity<>(new FileSystemResource(files[0]), headers, HttpStatus.OK);

        return response;
    }

    @ApiOperation(value = "Trigger MEF backup archive",
            notes = "The backup contains all metadata not harvested including templates.",
            nickname = "triggerBackup")
    @RequestMapping(
            method = RequestMethod.PUT,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @PreAuthorize("hasRole('Administrator')")
    @ResponseStatus(HttpStatus.OK)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Return succeed message.")
    })
    @ResponseBody
    public String trigger(HttpServletRequest request) throws Exception {
        ServiceContext context = ApiUtils.createServiceContext(request);
        final Trigger trigger = newTrigger().forJob("archiveAllMetadata", "gnBackgroundTasks").startNow().build();
        context.getBean("gnBackgroundJobScheduler", Scheduler.class).scheduleJob(trigger);

        return "{\"success\":true}";
    }

}
