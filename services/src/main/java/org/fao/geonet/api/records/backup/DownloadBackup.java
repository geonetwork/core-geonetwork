package org.fao.geonet.api.records.backup;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import jeeves.server.context.ServiceContext;
import jeeves.server.dispatchers.ServiceManager;
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.api.API;
import org.fao.geonet.api.ApiParams;
import org.fao.geonet.api.ApiUtils;
import org.fao.geonet.exceptions.JeevesException;
import org.fao.geonet.kernel.GeonetworkDataDirectory;
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
import java.nio.file.Path;

import static org.fao.geonet.api.ApiParams.API_CLASS_RECORD_OPS;
import static org.fao.geonet.api.ApiParams.API_CLASS_RECORD_TAG;
import static org.quartz.TriggerBuilder.newTrigger;

@RequestMapping(value = {
        "/api/records",
        "/api/" + API.VERSION_0_1 +
                "/records"
})
@Api(value = API_CLASS_RECORD_TAG,
        tags = API_CLASS_RECORD_TAG,
        description = API_CLASS_RECORD_OPS)

@Controller
public class DownloadBackup {

    @Autowired
    ServiceManager serviceManager;

    @Autowired
    ArchiveAllMetadataJob archiveAllMetadataJob;

    @ApiOperation(value = "Download MEF backup archive",
            notes = ".",
            nickname = "downloadBackup")
    @RequestMapping(value="/download.backup",produces = "application/zip")
    @ResponseBody
    public ResponseEntity<FileSystemResource> exec(HttpServletRequest request) throws Exception {

        ServiceContext context = ApiUtils.createServiceContext(request);
        ApplicationContext appContext = ApplicationContextHolder.get();
        GeonetworkDataDirectory dataDirectory = appContext.getBean(GeonetworkDataDirectory.class);

        Log.info(ArchiveAllMetadataJob.BACKUP_LOG, "User " + context.getUserSession().getUsername() + " from IP: " + context
                .getIpAddress() + " has started to download backup archive");

        File backupDir = dataDirectory.getBackupDir().resolve(ArchiveAllMetadataJob.BACKUP_DIR).toFile();
        if (!backupDir.exists()) {
            throw404();
        }
        File[] files = backupDir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return !pathname.isDirectory();
            }
        });
        if (files == null || files.length == 0) {
            throw404();
        }
        MultiValueMap<String, String> headers = new HttpHeaders();
        headers.add("content-disposition", "attachment; filename=" + files[0].getName());

        final ResponseEntity<FileSystemResource> response = new ResponseEntity<>(new FileSystemResource(files[0]), headers, HttpStatus.OK);

        return response;
    }

    @ApiOperation(value = "Trigger MEF backup archive",
            notes = ".",
            nickname = "triggerBackup")
    @RequestMapping(
            value="/trigger.backup",
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
        context.getApplicationContext().getBean("gnBackgroundJobScheduler", Scheduler.class).scheduleJob(trigger);

        return "{\"success\":true}";
    }

    private void throw404() throws JeevesException {
        throw new JeevesException("Backup file does not yet exist", null) {
            private static final long serialVersionUID = 1L;

            {
                this.code = 404;
                this.id = "NoBackup";
            }
        };
    }

}
